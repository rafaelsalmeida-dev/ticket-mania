import { NextRequest, NextResponse } from "next/server";
import { AUTH_COOKIE } from "@/lib/auth/cookies";

const SAFE_METHODS = new Set(["GET", "HEAD", "OPTIONS"]);
const ALLOWED_METHODS = new Set(["GET", "POST", "PUT", "PATCH", "DELETE"]);
type ProxyRouteContext = { params: Promise<{ path: string[] }> };

function isSameOrigin(request: NextRequest): boolean {
  const origin = request.headers.get("origin");
  return origin === new URL(request.url).origin;
}

export async function GET(request: NextRequest, context: ProxyRouteContext) {
  return proxy(request, context);
}
export async function POST(request: NextRequest, context: ProxyRouteContext) {
  return proxy(request, context);
}
export async function PUT(request: NextRequest, context: ProxyRouteContext) {
  return proxy(request, context);
}
export async function PATCH(request: NextRequest, context: ProxyRouteContext) {
  return proxy(request, context);
}
export async function DELETE(request: NextRequest, context: ProxyRouteContext) {
  return proxy(request, context);
}

async function proxy(
  request: NextRequest,
  context: ProxyRouteContext,
) {
  if (!ALLOWED_METHODS.has(request.method)) {
    return NextResponse.json({ message: "Método não permitido." }, { status: 405 });
  }
  if (!SAFE_METHODS.has(request.method) && !isSameOrigin(request)) {
    return NextResponse.json({ message: "Origem da solicitação inválida." }, { status: 403 });
  }

  const { path } = await context.params;
  if (path.some((segment) => segment === "." || segment === "..")) {
    return NextResponse.json({ message: "Caminho inválido." }, { status: 400 });
  }

  const baseUrl = process.env.BACKEND_API_URL;
  if (!baseUrl) {
    return NextResponse.json(
      { message: "BACKEND_API_URL não está configurada no servidor." },
      { status: 500 },
    );
  }

  const token = request.cookies.get(AUTH_COOKIE)?.value;
  const target = new URL(
    `/api/${path.map(encodeURIComponent).join("/")}${request.nextUrl.search}`,
    baseUrl,
  );
  const headers = new Headers();
  const contentType = request.headers.get("content-type");
  if (contentType) headers.set("content-type", contentType);
  if (token) headers.set("authorization", `Bearer ${token}`);

  const upstream = await fetch(target, {
    method: request.method,
    headers,
    body: SAFE_METHODS.has(request.method) ? undefined : await request.arrayBuffer(),
    cache: "no-store",
    redirect: "manual",
  });
  const responseHeaders = new Headers();
  const upstreamContentType = upstream.headers.get("content-type");
  if (upstreamContentType) responseHeaders.set("content-type", upstreamContentType);
  if (upstream.status === 401) {
    const response = new NextResponse(upstream.body, {
      status: upstream.status,
      headers: responseHeaders,
    });
    response.cookies.delete(AUTH_COOKIE);
    return response;
  }
  return new NextResponse(upstream.body, {
    status: upstream.status,
    headers: responseHeaders,
  });
}
