import { NextRequest, NextResponse } from "next/server";
import { AUTH_COOKIE, AUTH_MAX_AGE_SECONDS } from "@/lib/auth/cookies";

export async function POST(request: NextRequest) {
  if (request.headers.get("origin") !== new URL(request.url).origin) {
    return NextResponse.json({ message: "Origem da solicitação inválida." }, { status: 403 });
  }
  const baseUrl = process.env.BACKEND_API_URL;
  if (!baseUrl) {
    return NextResponse.json(
      { message: "BACKEND_API_URL não está configurada no servidor." },
      { status: 500 },
    );
  }
  const upstream = await fetch(new URL("/api/v1/auth/login", baseUrl), {
    method: "POST",
    headers: { "content-type": "application/json" },
    body: await request.text(),
    cache: "no-store",
  });
  const payload: unknown = await upstream.json().catch(() => null);
  if (!upstream.ok) {
    return NextResponse.json(payload ?? { message: "Falha na autenticação." }, {
      status: upstream.status,
    });
  }
  if (
    typeof payload !== "object" ||
    payload === null ||
    !("token" in payload) ||
    typeof payload.token !== "string"
  ) {
    return NextResponse.json({ message: "Resposta de autenticação inválida." }, { status: 502 });
  }

  const token = payload.token;
  let claims: { exp?: number; iss?: string; sub?: string; role?: string };
  try {
    claims = JSON.parse(
      Buffer.from(token.split(".")[1] ?? "", "base64url").toString("utf8"),
    ) as typeof claims;
  } catch {
    return NextResponse.json({ message: "Token de autenticação inválido." }, { status: 502 });
  }
  const now = Math.floor(Date.now() / 1000);
  if (
    claims.iss !== "ticket-mania-api" ||
    typeof claims.exp !== "number" ||
    claims.exp <= now ||
    typeof claims.sub !== "string" ||
    (claims.role !== "ROLE_USER" && claims.role !== "ROLE_ADMIN")
  ) {
    return NextResponse.json({ message: "Token de autenticação inválido." }, { status: 502 });
  }

  const response = NextResponse.json({
    authenticated: true,
    email: claims.sub,
    role: claims.role,
    expiresAt: claims.exp,
  });
  response.cookies.set(AUTH_COOKIE, token, {
    httpOnly: true,
    secure: process.env.NODE_ENV === "production",
    sameSite: "lax",
    path: "/",
    maxAge: Math.min(claims.exp - now, AUTH_MAX_AGE_SECONDS),
  });
  return response;
}
