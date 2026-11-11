import type { OrderStatus, TicketType } from "@/lib/api/types";

export function formatCurrency(amount: number): string {
  return new Intl.NumberFormat("pt-BR", {
    style: "currency",
    currency: "BRL",
  }).format(amount);
}

export function formatDate(date: string): string {
  return new Intl.DateTimeFormat("pt-BR", {
    dateStyle: "long",
    timeStyle: "short",
  }).format(new Date(date));
}

export const ticketLabels: Record<TicketType, string> = {
  FULL: "Inteira",
  HALF: "Meia-entrada",
  VIP: "VIP",
};

export const orderLabels: Record<OrderStatus, string> = {
  PENDING: "Aguardando pagamento",
  PAID: "Pago",
  EXPIRED: "Expirado",
  CANCELLED: "Cancelado",
};
