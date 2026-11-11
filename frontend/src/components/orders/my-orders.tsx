"use client";

import Link from "next/link";
import { ArrowRight, CalendarDays, Ticket } from "lucide-react";
import { useEffect, useState } from "react";
import { getErrorMessage } from "@/lib/api/errors";
import { listMyOrders } from "@/lib/api/orders";
import type { OrderSummary, Page } from "@/lib/api/types";
import { formatCurrency, formatDate, orderLabels, ticketLabels } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Loading } from "@/components/ui/loading";
import { Notice } from "@/components/ui/notice";

export function MyOrders() {
  const [result, setResult] = useState<Page<OrderSummary> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    listMyOrders(page)
      .then((data) => { if (active) setResult(data); })
      .catch((cause: unknown) => { if (active) setError(getErrorMessage(cause)); })
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [page]);

  return (
    <section className="mx-auto max-w-5xl px-5 py-12 lg:px-8">
      <p className="text-sm font-bold uppercase tracking-[0.16em] text-orange-600">Sua conta</p>
      <h1 className="mt-2 text-4xl font-black tracking-tight text-slate-950">Meus pedidos</h1>
      <p className="mt-3 text-slate-600">Acompanhe seus ingressos e o status de cada compra.</p>
      {loading ? <Loading label="Carregando pedidos..." /> : null}
      {!loading && error ? <div className="mt-8"><Notice>{error}</Notice></div> : null}
      {!loading && !error && result?.content.length === 0 ? (
        <div className="mt-8 rounded-3xl border border-dashed border-slate-300 bg-white px-6 py-14 text-center">
          <Ticket size={32} className="mx-auto text-orange-500" />
          <h2 className="mt-4 text-xl font-bold text-slate-900">Você ainda não tem pedidos</h2>
          <p className="mt-2 text-sm text-slate-500">Explore a agenda e encontre seu próximo evento.</p>
          <Link className="mt-5 inline-flex" href="/"><Button>Explorar eventos</Button></Link>
        </div>
      ) : null}
      {!loading && !error && result && result.content.length > 0 ? (
        <div className="mt-8 space-y-4">
          {result.content.map((order) => {
            const event = order.tickets[0];
            return (
              <article key={order.orderId} className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm sm:p-7">
                <div className="flex flex-col justify-between gap-4 sm:flex-row sm:items-start">
                  <div>
                    <span className={`inline-flex rounded-full px-3 py-1 text-xs font-bold ${
                      order.status === "PAID" ? "bg-emerald-50 text-emerald-700" :
                        order.status === "PENDING" ? "bg-amber-50 text-amber-700" :
                          "bg-slate-100 text-slate-600"
                    }`}>{orderLabels[order.status]}</span>
                    <h2 className="mt-3 text-xl font-bold text-slate-950">{event?.eventName ?? "Pedido Ticket Mania"}</h2>
                    <p className="mt-2 flex items-center gap-2 text-sm text-slate-500">
                      <CalendarDays size={15} /> Pedido em {formatDate(order.createdAt)}
                    </p>
                  </div>
                  <div className="text-left sm:text-right">
                    <p className="text-xs text-slate-500">Total</p>
                    <p className="mt-1 text-xl font-extrabold text-slate-950">{formatCurrency(order.totalAmount)}</p>
                  </div>
                </div>
                <div className="mt-5 flex flex-col gap-3 border-t border-slate-100 pt-4 sm:flex-row sm:items-center sm:justify-between">
                  <p className="text-sm text-slate-600">
                    {order.tickets.length} ingresso(s) · {order.tickets.map((ticket) => ticketLabels[ticket.ticketType]).join(", ")}
                  </p>
                  <Link href={`/orders/${order.orderId}`} className="inline-flex items-center gap-2 text-sm font-bold text-orange-600 hover:text-orange-700">
                    Ver pedido <ArrowRight size={16} />
                  </Link>
                </div>
              </article>
            );
          })}
          {result.totalPages > 1 ? (
            <nav aria-label="Paginação de pedidos" className="flex items-center justify-center gap-4 pt-3">
              <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage((value) => value - 1)}>Anterior</Button>
              <span className="text-sm text-slate-600">Página {page + 1} de {result.totalPages}</span>
              <Button variant="outline" size="sm" disabled={page + 1 >= result.totalPages} onClick={() => setPage((value) => value + 1)}>Próxima</Button>
            </nav>
          ) : null}
        </div>
      ) : null}
    </section>
  );
}
