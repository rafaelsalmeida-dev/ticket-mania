import { NextRequest, NextResponse } from "next/server";
import { AUTH_COOKIE } from "@/lib/auth/cookies";

function readClaims(token: string) {
  try {
    return JSON.parse(
      Buffer.from(token.split(".")[1] ?? "", "base64url").toString("utf8"),
    ) as { exp?: number; iss?: string; role?: string };
  } catch {
    return null;
  }
}

export function middleware(request: NextRequest) {
  const pathname = request.nextUrl.pathname;
  const isOrdersPage = pathname === "/orders" || pathname.startsWith("/orders/");
  const isAdminPage = pathname === "/admin" || pathname.startsWith("/admin/");
  if (!isOrdersPage && !isAdminPage) return NextResponse.next();

  const token = request.cookies.get(AUTH_COOKIE)?.value;
  const claims = token ? readClaims(token) : null;
  const valid =
    claims?.iss === "ticket-mania-api" &&
    typeof claims.exp === "number" &&
    claims.exp > Math.floor(Date.now() / 1000) &&
    (claims.role === "ROLE_USER" || claims.role === "ROLE_ADMIN");
  if (!valid) {
    const login = new URL("/login", request.url);
    login.searchParams.set("next", pathname);
    return NextResponse.redirect(login);
  }
  if (isAdminPage && claims.role !== "ROLE_ADMIN") {
    return NextResponse.redirect(new URL("/orders", request.url));
  }
  return NextResponse.next();
}

export const config = {
  matcher: ["/orders/:path*", "/admin/:path*"],
};
