"use client";

import { ApiError } from "./errors";

export async function apiFetch<T>(
  path: string,
  init?: RequestInit,
): Promise<T> {
  const response = await fetch(path, {
    ...init,
    credentials: "same-origin",
    headers: {
      ...(init?.body ? { "Content-Type": "application/json" } : {}),
      ...init?.headers,
    },
  });
  const text = await response.text();
  let body: unknown = null;
  if (text) {
    try {
      body = JSON.parse(text);
    } catch {
      throw new ApiError("O servidor retornou uma resposta inválida.", response.status);
    }
  }
  if (!response.ok) {
    if (response.status === 401 && typeof window !== "undefined") {
      const currentPath = `${window.location.pathname}${window.location.search}`;
      if (!currentPath.startsWith("/login") && !currentPath.startsWith("/register")) {
        window.location.assign(`/login?next=${encodeURIComponent(currentPath)}`);
      }
    }
    const message =
      typeof body === "object" &&
      body !== null &&
      "message" in body &&
      typeof body.message === "string"
        ? body.message
        : response.status === 401
          ? "Sua sessão expirou. Entre novamente."
          : `Não foi possível concluir a solicitação (${response.status}).`;
    throw new ApiError(message, response.status);
  }
  return body as T;
}

export function proxyPath(path: string): string {
  return `/api/proxy/${path.replace(/^\/+/, "")}`;
}
