import { cookies } from "next/headers";
import { AUTH_COOKIE } from "./cookies";

export type UserSession = {
  authenticated: true;
  email: string;
  role: "ROLE_ADMIN" | "ROLE_USER";
  expiresAt: number;
};

export async function getSession(): Promise<UserSession | null> {
  const token = (await cookies()).get(AUTH_COOKIE)?.value;
  if (!token) return null;
  try {
    const payload = JSON.parse(
      Buffer.from(token.split(".")[1] ?? "", "base64url").toString("utf8"),
    ) as { exp?: number; iss?: string; sub?: string; role?: string };
    if (
      payload.iss !== "ticket-mania-api" ||
      typeof payload.exp !== "number" ||
      payload.exp <= Math.floor(Date.now() / 1000) ||
      typeof payload.sub !== "string" ||
      (payload.role !== "ROLE_ADMIN" && payload.role !== "ROLE_USER")
    )
      return null;
    return {
      authenticated: true,
      email: payload.sub,
      role: payload.role,
      expiresAt: payload.exp,
    };
  } catch {
    return null;
  }
}
