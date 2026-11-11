"use client";

import Link from "next/link";
import { ArrowLeft, CalendarDays, MapPin, Ticket } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { confirmMockPayment, getOrder } from "@/lib/api/orders";
import { getErrorMessage } from "@/lib/api/errors";
import type { OrderSummary } from "@/lib/api/types";
import { formatCurrency, formatDate, orderLabels, ticketLabels } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Loading } from "@/components/ui/loading";
import { Notice } from "@/components/ui/notice";

export function OrderDetails({ orderId, created = false }: { orderId: string; created?: boolean }) {
  const [order, setOrder] = useState<OrderSummary | null>(null);
  const [loading, setLoading] = useState(true);
  const [paying, setPaying] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState(created ? "Pedido criado. Conclua o pagamento para confirmar seus ingressos." : "");

  const reload = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      setOrder(await getOrder(orderId));
    } catch (cause) {
      setError(getErrorMessage(cause));
    } finally {
      setLoading(false);
    }
  }, [orderId]);

  useEffect(() => { void reload(); }, [reload]);

  async function pay() {
    setPaying(true);
    setError("");
    setSuccess("");
    try {
      await confirmMockPayment(orderId);
      setOrder(await getOrder(orderId));
      setSuccess("Pagamento confirmado. Seus ingressos estão garantidos.");
    } catch (cause) {
      setError(getErrorMessage(cause));
    } finally {
      setPaying(false);
    }
  }

  if (loading) return <Loading label="Carregando pedido..." />;

  return (
    <section className="mx-auto max-w-4xl px-5 py-12 lg:px-8">
      <Link href="/orders" className="inline-flex items-center gap-2 text-sm font-semibold text-slate-600 hover:text-orange-600">
        <ArrowLeft size={16} /> Meus pedidos
      </Link>
      {error ? <div className="mt-7"><Notice>{error}</Notice></div> : null}
      {success ? <div className="mt-7"><Notice variant="success">{success}</Notice></div> : null}
      {order ? (
        <article className="mt-6 overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm">
          <div className="bg-slate-950 p-6 text-white sm:p-9">
            <p className="text-sm font-bold uppercase tracking-[0.16em] text-orange-300">Detalhes do pedido</p>
            <div className="mt-3 flex flex-wrap items-center justify-between gap-3">
              <h1 className="text-2xl font-black sm:text-3xl">Pedido #{order.orderId.slice(0, 8)}</h1>
              <span className={`rounded-full px-3 py-1 text-xs font-bold ${
                order.status === "PAID" ? "bg-emerald-400/15 text-emerald-200" :
                  order.status === "PENDING" ? "bg-amber-400/15 text-amber-200" :
                    "bg-white/10 text-slate-200"
              }`}>{orderLabels[order.status]}</span>
            </div>
            <p className="mt-3 flex items-center gap-2 text-sm text-slate-300"><CalendarDays size={15} />Criado em {formatDate(order.createdAt)}</p>
          </div>
          <div className="p-6 sm:p-9">
            <h2 className="text-lg font-bold text-slate-950">Ingressos do pedido</h2>
            <div className="mt-4 divide-y divide-slate-100">
              {order.tickets.map((ticket) => (
                <div key={ticket.ticketId} className="flex flex-col justify-between gap-2 py-4 sm:flex-row sm:items-center">
                  <div>
                    <p className="font-semibold text-slate-900">{ticket.eventName}</p>
                    <p className="mt-1 flex items-center gap-2 text-sm text-slate-500"><Ticket size={14} />{ticketLabels[ticket.ticketType]}</p>
                  </div>
                  <p className="font-bold text-slate-900">{formatCurrency(ticket.price)}</p>
                </div>
              ))}
            </div>
            {order.status === "PENDING" ? (
              <div className="mt-5 rounded-2xl bg-orange-50 p-5">
                <p className="font-bold text-orange-950">Pagamento de demonstração</p>
                <p className="mt-1 text-sm leading-6 text-orange-900">Este projeto usa a confirmação mock disponibilizada pelo backend; nenhum pagamento real será processado.</p>
                <Button className="mt-4 rounded-xl" onClick={pay} disabled={paying}>
                  {paying ? "Confirmando..." : `Confirmar pagamento de ${formatCurrency(order.totalAmount)}`}
                </Button>
              </div>
            ) : null}
            <div className="mt-6 flex items-center justify-between border-t border-slate-100 pt-5">
              <span className="text-sm text-slate-600">Total do pedido</span>
              <strong className="text-2xl text-slate-950">{formatCurrency(order.totalAmount)}</strong>
            </div>
          </div>
        </article>
      ) : null}
      {!loading && !order && !error ? <Notice>Pedido não encontrado.</Notice> : null}
      {order?.tickets[0] ? (
        <p className="mt-4 flex items-center gap-2 text-xs text-slate-500"><MapPin size={14} />Consulte a página do evento para ver orientações do local.</p>
      ) : null}
    </section>
  );
}
