"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { ArrowLeft, CalendarDays, MapPin, ShieldCheck, Ticket } from "lucide-react";
import { useEffect, useState } from "react";
import { getTicketAvailability, findEventById } from "@/lib/api/events";
import { createOrder } from "@/lib/api/orders";
import type { EventRecord, TicketAvailability, TicketType } from "@/lib/api/types";
import { getErrorMessage } from "@/lib/api/errors";
import { formatCurrency, formatDate, ticketLabels } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Select } from "@/components/ui/field";
import { Loading } from "@/components/ui/loading";
import { Notice } from "@/components/ui/notice";

export function EventDetails({ eventId }: { eventId: string }) {
  const router = useRouter();
  const [event, setEvent] = useState<EventRecord | null>(null);
  const [tickets, setTickets] = useState<TicketAvailability[]>([]);
  const [ticketType, setTicketType] = useState<TicketType | "">("");
  const [quantity, setQuantity] = useState(1);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    Promise.all([findEventById(eventId), getTicketAvailability(eventId)])
      .then(([eventData, availability]) => {
        if (!active) return;
        setEvent(eventData);
        setTickets(availability);
        setTicketType(availability.find((item) => item.available > 0)?.ticketType ?? "");
      })
      .catch((cause: unknown) => {
        if (active) setError(getErrorMessage(cause));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => { active = false; };
  }, [eventId]);

  const selectedTicket = tickets.find((ticket) => ticket.ticketType === ticketType);
  const maxQuantity = Math.min(10, selectedTicket?.available ?? 0);

  async function reserveTicket() {
    if (!selectedTicket || quantity > selectedTicket.available) return;
    setSubmitting(true);
    setError("");
    try {
      const sessionResponse = await fetch("/api/auth/session", { credentials: "same-origin" });
      if (!sessionResponse.ok) throw new Error("Não foi possível verificar sua sessão.");
      const session = (await sessionResponse.json()) as { authenticated: boolean };
      if (!session.authenticated) {
        router.push(`/login?next=${encodeURIComponent(`/events/${eventId}`)}`);
        return;
      }
      const order = await createOrder({ eventId, ticketType: selectedTicket.ticketType, quantity });
      router.push(`/orders/${order.orderId}?created=1`);
    } catch (cause) {
      setError(getErrorMessage(cause));
    } finally {
      setSubmitting(false);
    }
  }

  if (loading) return <Loading label="Carregando evento..." />;
  if (!event) {
    return (
      <section className="mx-auto max-w-4xl px-5 py-16">
        <Notice>{error || "Este evento não foi encontrado."}</Notice>
        <Link className="mt-6 inline-flex items-center gap-2 text-sm font-semibold text-orange-600" href="/">
          <ArrowLeft size={16} /> Voltar aos eventos
        </Link>
      </section>
    );
  }

  return (
    <section className="mx-auto max-w-7xl px-5 py-8 lg:px-8">
      <Link className="inline-flex items-center gap-2 text-sm font-semibold text-slate-600 hover:text-orange-600" href="/">
        <ArrowLeft size={16} /> Todos os eventos
      </Link>
      <div className="mt-6 grid gap-8 lg:grid-cols-[1.4fr_0.8fr]">
        <article className="overflow-hidden rounded-3xl border border-slate-200 bg-white">
          <div className="relative min-h-64 bg-gradient-to-br from-orange-100 via-rose-50 to-indigo-100 sm:aspect-[16/8]">
            {event.imageUrl ? (
              <div role="img" aria-label={`Imagem de ${event.name}`} className="absolute inset-0 bg-cover bg-center" style={{ backgroundImage: `url("${event.imageUrl.replace(/["\\]/g, "")}")` }} />
            ) : (
              <div className="grid h-full min-h-64 place-items-center text-orange-500"><Ticket size={64} strokeWidth={1.2} /></div>
            )}
          </div>
          <div className="space-y-5 p-6 sm:p-9">
            <span className="rounded-full bg-orange-50 px-3 py-1 text-xs font-bold uppercase tracking-wide text-orange-700">Evento</span>
            <h1 className="text-3xl font-black tracking-tight text-slate-950 sm:text-5xl">{event.name}</h1>
            <div className="grid gap-4 border-t border-slate-100 pt-5 sm:grid-cols-2">
              <p className="flex items-start gap-3 text-sm text-slate-600">
                <CalendarDays size={19} className="mt-0.5 shrink-0 text-orange-500" />
                <span><strong className="block text-slate-900">Data e horário</strong><time dateTime={event.date}>{formatDate(event.date)}</time></span>
              </p>
              <p className="flex items-start gap-3 text-sm text-slate-600">
                <MapPin size={19} className="mt-0.5 shrink-0 text-orange-500" />
                <span><strong className="block text-slate-900">Local</strong>{event.place}</span>
              </p>
            </div>
          </div>
        </article>

        <aside className="h-fit rounded-3xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
          <p className="text-sm font-bold uppercase tracking-[0.16em] text-orange-600">Garanta seu lugar</p>
          <h2 className="mt-2 text-2xl font-extrabold text-slate-950">Ingressos disponíveis</h2>
          {tickets.length === 0 ? <p className="mt-5 rounded-xl bg-slate-50 p-4 text-sm text-slate-600">Ainda não há ingressos disponíveis para este evento.</p> : null}
          {tickets.length > 0 ? (
            <div className="mt-6 space-y-4">
              <label className="block text-sm font-medium text-slate-700">
                Tipo de ingresso
                <Select value={ticketType} onChange={(value) => { setTicketType(value.target.value as TicketType); setQuantity(1); }}>
                  {tickets.map((ticket) => (
                    <option key={ticket.ticketType} value={ticket.ticketType} disabled={ticket.available < 1}>
                      {ticketLabels[ticket.ticketType]} — {formatCurrency(ticket.price)} ({ticket.available} disponíveis)
                    </option>
                  ))}
                </Select>
              </label>
              {selectedTicket ? (
                <>
                  <label className="block text-sm font-medium text-slate-700">
                    Quantidade
                    <Select value={quantity} onChange={(value) => setQuantity(Number(value.target.value))} disabled={maxQuantity < 1}>
                      {Array.from({ length: maxQuantity }, (_, index) => index + 1).map((count) => <option key={count} value={count}>{count}</option>)}
                    </Select>
                  </label>
                  <div className="flex items-center justify-between border-t border-slate-100 pt-4 text-sm">
                    <span className="text-slate-600">Total</span>
                    <strong className="text-xl text-slate-950">{formatCurrency(selectedTicket.price * quantity)}</strong>
                  </div>
                </>
              ) : null}
              {error ? <Notice>{error}</Notice> : null}
              <Button className="w-full rounded-xl py-3.5" onClick={reserveTicket} disabled={!selectedTicket || maxQuantity < 1 || submitting}>
                {submitting ? "Reservando..." : "Continuar para compra"}
              </Button>
              <p className="flex items-center justify-center gap-2 text-center text-xs text-slate-500">
                <ShieldCheck size={15} className="text-emerald-600" /> Reserva segura pela sua conta
              </p>
            </div>
          ) : null}
          <div className="mt-6 border-t border-slate-100 pt-5">
            <p className="text-xs leading-5 text-slate-500">Capacidade do local: {event.capacity} pessoas.</p>
          </div>
        </aside>
      </div>
    </section>
  );
}
