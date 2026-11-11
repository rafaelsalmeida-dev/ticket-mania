"use client";

import Link from "next/link";
import { CalendarDays, MapPin, Pencil, Plus, Trash2 } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import { deleteEvent, listEvents } from "@/lib/api/events";
import { getErrorMessage } from "@/lib/api/errors";
import type { EventRecord, Page } from "@/lib/api/types";
import { formatDate } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Loading } from "@/components/ui/loading";
import { Notice } from "@/components/ui/notice";

export function EventAdminList() {
  const [result, setResult] = useState<Page<EventRecord> | null>(null);
  const [page, setPage] = useState(0);
  const [loading, setLoading] = useState(true);
  const [deleting, setDeleting] = useState("");
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const load = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      setResult(await listEvents(page));
    } catch (cause) {
      setError(getErrorMessage(cause));
    } finally {
      setLoading(false);
    }
  }, [page]);

  useEffect(() => { void load(); }, [load]);

  async function remove(event: EventRecord) {
    if (!window.confirm(`Excluir o evento “${event.name}”? Esta ação não pode ser desfeita.`)) return;
    setDeleting(event.id);
    setError("");
    setSuccess("");
    try {
      await deleteEvent(event.id);
      setSuccess("Evento excluído com sucesso.");
      await load();
    } catch (cause) {
      setError(getErrorMessage(cause));
    } finally {
      setDeleting("");
    }
  }

  return (
    <section className="mx-auto max-w-6xl px-5 py-12 lg:px-8">
      <div className="flex flex-col justify-between gap-5 sm:flex-row sm:items-end">
        <div>
          <p className="text-sm font-bold uppercase tracking-[0.16em] text-orange-600">Painel administrativo</p>
          <h1 className="mt-2 text-4xl font-black tracking-tight text-slate-950">Eventos</h1>
          <p className="mt-3 text-slate-600">Crie eventos e gerencie seus ingressos.</p>
        </div>
        <Link href="/admin/events/new"><Button className="rounded-xl"><Plus size={18} /> Novo evento</Button></Link>
      </div>
      {success ? <div className="mt-6"><Notice variant="success">{success}</Notice></div> : null}
      {error ? <div className="mt-6"><Notice>{error}</Notice></div> : null}
      {loading ? <Loading label="Carregando eventos..." /> : null}
      {!loading && !error && result?.content.length === 0 ? (
        <div className="mt-8 rounded-3xl border border-dashed border-slate-300 bg-white px-6 py-14 text-center">
          <h2 className="text-lg font-bold text-slate-900">Nenhum evento cadastrado</h2>
          <p className="mt-2 text-sm text-slate-500">Crie o primeiro evento para começar a vender ingressos.</p>
          <Link className="mt-5 inline-flex" href="/admin/events/new"><Button>Novo evento</Button></Link>
        </div>
      ) : null}
      {!loading && result?.content.length ? (
        <div className="mt-8 space-y-3">
          {result.content.map((event) => (
            <article key={event.id} className="flex flex-col justify-between gap-5 rounded-3xl border border-slate-200 bg-white p-5 sm:flex-row sm:items-center sm:p-6">
              <div>
                <h2 className="text-lg font-bold text-slate-950">{event.name}</h2>
                <div className="mt-2 flex flex-wrap gap-x-5 gap-y-2 text-sm text-slate-500">
                  <span className="inline-flex items-center gap-2"><CalendarDays size={15} />{formatDate(event.date)}</span>
                  <span className="inline-flex items-center gap-2"><MapPin size={15} />{event.place}</span>
                  <span>Capacidade: {event.capacity}</span>
                </div>
              </div>
              <div className="flex items-center gap-2">
                <Link href={`/admin/events/${event.id}`}>
                  <Button variant="outline" size="sm"><Pencil size={15} /> Gerenciar</Button>
                </Link>
                <Button variant="danger" size="sm" onClick={() => void remove(event)} disabled={deleting === event.id} aria-label={`Excluir ${event.name}`}>
                  <Trash2 size={15} />{deleting === event.id ? "Excluindo..." : "Excluir"}
                </Button>
              </div>
            </article>
          ))}
          {result.totalPages > 1 ? (
            <nav aria-label="Paginação de eventos" className="flex items-center justify-center gap-4 pt-3">
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
