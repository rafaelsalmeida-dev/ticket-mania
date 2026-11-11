export type Page<T> = {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
};

export type EventRecord = {
  id: string;
  name: string;
  date: string;
  place: string;
  capacity: number;
  imageUrl: string | null;
};
export type EventInput = Omit<EventRecord, "id">;
export type TicketType = "FULL" | "HALF" | "VIP";
export type OrderStatus = "PENDING" | "PAID" | "EXPIRED" | "CANCELLED";
export type TicketAvailability = {
  ticketType: TicketType;
  price: number;
  available: number;
};
export type TicketItem = {
  ticketId: string;
  eventId: string;
  eventName: string;
  ticketType: TicketType;
  price: number;
};
export type OrderSummary = {
  orderId: string;
  status: OrderStatus;
  totalAmount: number;
  createdAt: string;
  tickets: TicketItem[];
};
export type CreatedOrder = {
  orderId: string;
  ticketIds: string[];
  totalAmount: number;
  payment: { provider: string; reference: string };
};
export type TicketBatchResult = {
  created: number;
  totalAvailableOfType: number;
};
export type TicketUpdateResult = {
  affected: number;
  totalAvailableOfType: number;
};
export type AuthSession = {
  authenticated: true;
  email: string;
  role: "ROLE_ADMIN" | "ROLE_USER";
  expiresAt: number;
};
