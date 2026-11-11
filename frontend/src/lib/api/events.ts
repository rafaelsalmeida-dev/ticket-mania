"use client";

import { apiFetch, proxyPath } from "./client";
import type {
  EventInput,
  EventRecord,
  Page,
  TicketAvailability,
  TicketBatchResult,
  TicketType,
  TicketUpdateResult,
} from "./types";

export function listEvents(page = 0, search = "") {
  const path = search
    ? `v1/events/search/${encodeURIComponent(search)}`
    : "v1/events";
  return apiFetch<Page<EventRecord>>(
    `${proxyPath(path)}?page=${page}&size=12&direction=asc`,
  );
}

export async function findEventById(id: string): Promise<EventRecord | null> {
  const firstPage = await listEvents(0);
  const found = firstPage.content.find((event) => event.id === id);
  if (found || firstPage.totalPages < 2) return found ?? null;
  for (let page = 1; page < firstPage.totalPages; page += 1) {
    const result = await listEvents(page);
    const event = result.content.find((item) => item.id === id);
    if (event) return event;
  }
  return null;
}

export function getTicketAvailability(eventId: string) {
  return apiFetch<TicketAvailability[]>(
    proxyPath(`v1/events/${encodeURIComponent(eventId)}/tickets`),
  );
}
export function createEvent(input: EventInput) {
  return apiFetch<EventRecord>(proxyPath("v1/events"), {
    method: "POST",
    body: JSON.stringify(input),
  });
}
export function updateEvent(id: string, input: EventInput) {
  return apiFetch<EventRecord>(proxyPath(`v1/events/${encodeURIComponent(id)}`), {
    method: "PUT",
    body: JSON.stringify(input),
  });
}
export function deleteEvent(id: string) {
  return apiFetch<void>(proxyPath(`v1/events/${encodeURIComponent(id)}`), {
    method: "DELETE",
  });
}
export function createTicketBatch(
  eventId: string,
  input: { type: TicketType; price: number; quantity: number },
) {
  return apiFetch<TicketBatchResult>(
    proxyPath(`v1/events/${encodeURIComponent(eventId)}/tickets`),
    { method: "POST", body: JSON.stringify(input) },
  );
}
export function updateTicketPrice(eventId: string, type: TicketType, price: number) {
  return apiFetch<TicketUpdateResult>(
    proxyPath(`v1/events/${encodeURIComponent(eventId)}/tickets/${encodeURIComponent(type)}`),
    { method: "PUT", body: JSON.stringify({ price }) },
  );
}
export function deleteAvailableTickets(
  eventId: string,
  type: TicketType,
  quantity?: number,
) {
  const query = quantity ? `?quantity=${quantity}` : "";
  const endpoint = proxyPath(
    `v1/events/${encodeURIComponent(eventId)}/tickets/${encodeURIComponent(type)}`,
  );
  return apiFetch<TicketUpdateResult>(`${endpoint}${query}`, { method: "DELETE" });
}
