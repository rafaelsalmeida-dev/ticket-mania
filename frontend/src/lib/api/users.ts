"use client";

import { apiFetch, proxyPath } from "./client";

export type UserRole = "ROLE_ADMIN" | "ROLE_USER";

export function updateUserRole(userId: string, role: UserRole) {
  return apiFetch<void>(proxyPath(`v1/users/${encodeURIComponent(userId)}/role`), {
    method: "PATCH",
    body: JSON.stringify({ role }),
  });
}
