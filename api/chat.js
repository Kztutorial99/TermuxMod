import { db } from "../backend/lib/db.mjs";
import { requireFirebaseUser } from "../backend/lib/auth.mjs";
import { handleError, json, methodNotAllowed } from "../backend/lib/response.mjs";

const MAX_MESSAGE_LENGTH = 12000;
const MAX_BODY_BYTES = 16000;

async function readJson(req) {
  let body = "";
  for await (const chunk of req) {
    body += chunk;
    if (Buffer.byteLength(body, "utf8") > MAX_BODY_BYTES) {
      const error = new Error("Request body too large");
      error.statusCode = 413;
      throw error;
    }
  }
  try {
    return JSON.parse(body || "{}");
  } catch {
    const error = new Error("Invalid JSON");
    error.statusCode = 400;
    throw error;
  }
}

export default async function handler(req, res) {
  if (req.method !== "POST") return methodNotAllowed(res, ["POST"]);

  try {
    const user = await requireFirebaseUser(req);
    const { message, conversationId } = await readJson(req);

    if (typeof message !== "string" || !message.trim()) {
      const error = new Error("message is required");
      error.statusCode = 400;
      throw error;
    }

    if (message.length > MAX_MESSAGE_LENGTH) {
      const error = new Error("message is too long");
      error.statusCode = 413;
      throw error;
    }

    const baseUrl = process.env.QWEN_BASE_URL;
    const apiKey = process.env.QWEN_API_KEY;
    const model = process.env.QWEN_MODEL;
    if (!baseUrl || !apiKey || !model) throw new Error("Qwen server configuration is incomplete");

    const sql = db();
    const userRows = await sql`
      INSERT INTO app_users (firebase_uid, email, display_name, photo_url)
      VALUES (${user.uid}, ${user.email ?? null}, ${user.name ?? null}, ${user.picture ?? null})
      ON CONFLICT (firebase_uid) DO UPDATE SET updated_at = now()
      RETURNING id
    `;
    const userId = userRows[0].id;

    let activeConversationId = conversationId || null;
    if (activeConversationId) {
      const owned = await sql`
        SELECT id FROM conversations WHERE id = ${activeConversationId} AND user_id = ${userId} LIMIT 1
      `;
      if (!owned.length) {
        const error = new Error("Conversation not found");
        error.statusCode = 404;
        throw error;
      }
    } else {
      const created = await sql`
        INSERT INTO conversations (user_id, title) VALUES (${userId}, ${message.trim().slice(0, 80)}) RETURNING id
      `;
      activeConversationId = created[0].id;
    }

    await sql`
      INSERT INTO messages (conversation_id, role, content)
      VALUES (${activeConversationId}, 'user', ${message.trim()})
    `;

    const history = await sql`
      SELECT role, content
      FROM messages
      WHERE conversation_id = ${activeConversationId}
      ORDER BY created_at DESC
      LIMIT 30
    `;

    const upstream = await fetch(`${baseUrl.replace(/\/$/, "")}/chat/completions`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        "Authorization": `Bearer ${apiKey}`
      },
      body: JSON.stringify({
        model,
        messages: history.reverse(),
        temperature: 0.7
      }),
      signal: AbortSignal.timeout(45000)
    });

    if (!upstream.ok) {
      console.error("Qwen upstream status:", upstream.status);
      const error = new Error("AI provider request failed");
      error.statusCode = 502;
      throw error;
    }

    const data = await upstream.json();
    const answer = data?.choices?.[0]?.message?.content;
    if (typeof answer !== "string" || !answer) throw new Error("AI provider returned an invalid response");

    await sql`
      INSERT INTO messages (conversation_id, role, content)
      VALUES (${activeConversationId}, 'assistant', ${answer})
    `;
    await sql`
      UPDATE conversations SET updated_at = now() WHERE id = ${activeConversationId}
    `;

    return json(res, 200, { conversationId: activeConversationId, answer });
  } catch (error) {
    return handleError(res, error);
  }
}
