import { db } from "../backend/lib/db.mjs";
import { requireFirebaseUser } from "../backend/lib/auth.mjs";
import { handleError, json, methodNotAllowed } from "../backend/lib/response.mjs";

export default async function handler(req, res) {
  if (req.method !== "GET") return methodNotAllowed(res, ["GET"]);

  try {
    const user = await requireFirebaseUser(req);
    const sql = db();

    await sql`
      INSERT INTO app_users (firebase_uid, email, display_name, photo_url)
      VALUES (${user.uid}, ${user.email ?? null}, ${user.name ?? null}, ${user.picture ?? null})
      ON CONFLICT (firebase_uid) DO UPDATE SET
        email = EXCLUDED.email,
        display_name = EXCLUDED.display_name,
        photo_url = EXCLUDED.photo_url,
        updated_at = now()
    `;

    const rows = await sql`
      SELECT id, firebase_uid, email, display_name, photo_url, role, created_at, updated_at
      FROM app_users
      WHERE firebase_uid = ${user.uid}
      LIMIT 1
    `;

    return json(res, 200, { user: rows[0] ?? null });
  } catch (error) {
    return handleError(res, error);
  }
}
