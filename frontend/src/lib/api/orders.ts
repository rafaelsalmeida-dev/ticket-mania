"use client";

import { apiFetch, proxyPath } from "./client";
import type { CreatedOrder, OrderSummary, Page, TicketType } from "./types";

export function listMyOrders(page = 0) {
  return apiFetch<Page<OrderSummary>>(
    `${proxyPath("v1/orders/me")}?page=${page}&size=12&direction=desc`,
  );
}
export function getOrder(id: string) {
  return apiFetch<OrderSummary>(proxyPath(`v1/orders/${encodeURIComponent(id)}`));
}
export function createOrder(input: {
  eventId: string;
  ticketType: TicketType;
  quantity: number;
}) {
  return apiFetch<CreatedOrder>(proxyPath("v1/orders"), {
    method: "POST",
    body: JSON.stringify(input),
  });
}
export function confirmMockPayment(orderId: string) {
  return apiFetch<void>(
    proxyPath(`v1/orders/mock-success/${encodeURIComponent(orderId)}`),
    { method: "PATCH" },
  );
}
