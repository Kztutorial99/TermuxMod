export function json(res, status, body) {
  res.statusCode = status;
  res.setHeader("Content-Type", "application/json; charset=utf-8");
  res.setHeader("Cache-Control", "no-store");
  res.end(JSON.stringify(body));
}

export function methodNotAllowed(res, methods = ["GET"]) {
  res.setHeader("Allow", methods.join(", "));
  return json(res, 405, { error: "Method not allowed" });
}

export function handleError(res, error) {
  const status = Number.isInteger(error?.statusCode) ? error.statusCode : 500;
  if (status >= 500) console.error("API error:", error?.message || error);
  return json(res, status, {
    error: status >= 500 ? "Internal server error" : (error?.message || "Request failed")
  });
}
