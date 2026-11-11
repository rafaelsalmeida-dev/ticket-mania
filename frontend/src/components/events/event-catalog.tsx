"use client";

import { Search, Sparkles } from "lucide-react";
import { useEffect, useState, type FormEvent } from "react";
import { listEvents } from "@/lib/api/events";
import type { EventRecord, Page } from "@/lib/api/types";
import { getErrorMessage } from "@/lib/api/errors";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/field";
import { Loading } from "@/components/ui/loading";
import { Notice } from "@/components/ui/notice";
import { EventCard } from "./event-card";

export function EventCatalog() {
  const [result, setResult] = useState<Page<EventRecord> | null>(null);
  const [page, setPage] = useState(0);
  const [term, setTerm] = useState("");
  const [query, setQuery] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    let active = true;
    setLoading(true);
    setError("");
    listEvents(page, query)
      .then((data) => {
        if (active) setResult(data);
      })
      .catch((cause: unknown) => {
        if (active) setError(getErrorMessage(cause));
      })
      .finally(() => {
        if (active) setLoading(false);
      });
    return () => {
      active = false;
    };
  }, [page, query]);

  function search(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setPage(0);
    setQuery(term.trim());
  }

  return (
    <section className="mx-auto max-w-7xl px-5 pb-20 pt-10 lg:px-8">
      <div className="relative overflow-hidden rounded-[2rem] bg-slate-950 px-6 py-10 text-white sm:px-10 sm:py-14 lg:px-16">
        <div className="pointer-events-none absolute -right-12 -top-24 size-80 rounded-full bg-orange-500/30 blur-3xl" />
        <div className="pointer-events-none absolute -bottom-36 left-1/3 size-72 rounded-full bg-fuchsia-500/20 blur-3xl" />
        <div className="relative max-w-3xl">
          <span className="inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/10 px-4 py-2 text-xs font-semibold uppercase tracking-[0.18em] text-orange-200">
            <Sparkles size={14} /> Viva o próximo momento
          </span>
          <h1 className="mt-6 text-4xl font-black leading-tight tracking-tight sm:text-6xl">
            Histórias incríveis começam <span className="text-orange-400">ao vivo.</span>
          </h1>
          <p className="mt-5 max-w-xl text-base leading-7 text-slate-300 sm:text-lg">
            Descubra eventos, escolha seu ingresso e prepare-se para viver algo inesquecível.
          </p>
          <form onSubmit={search} className="mt-8 flex max-w-xl flex-col gap-3 rounded-2xl bg-white p-2 sm:flex-row">
            <label htmlFor="event-search" className="sr-only">Buscar eventos</label>
            <div className="relative flex-1">
              <Search size={18} className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400" />
              <Input
                id="event-search"
                value={term}
                onChange={(event) => setTerm(event.target.value)}
                placeholder="Busque por evento"
                className="mt-0 border-0 pl-11 shadow-none focus:ring-0"
              />
            </div>
            <Button type="submit" className="rounded-xl px-7">Buscar</Button>
          </form>
        </div>
      </div>

      <div className="mt-12 flex flex-wrap items-end justify-between gap-4">
        <div>
          <p className="text-sm font-bold uppercase tracking-[0.16em] text-orange-600">Agenda</p>
          <h2 className="mt-2 text-3xl font-extrabold tracking-tight text-slate-950">
            {query ? `Resultados para “${query}”` : "Eventos em destaque"}
          </h2>
        </div>
        {result ? (
          <p className="text-sm text-slate-500">{result.totalElements} eventos encontrados</p>
        ) : null}
      </div>

      {loading ? <Loading label="Buscando eventos..." /> : null}
      {!loading && error ? <div className="mt-6"><Notice>{error}</Notice></div> : null}
      {!loading && !error && result?.content.length === 0 ? (
        <div className="mt-8 rounded-3xl border border-dashed border-slate-300 bg-white px-6 py-14 text-center">
          <h3 className="text-lg font-bold text-slate-900">Nenhum evento encontrado</h3>
          <p className="mt-2 text-sm text-slate-500">Tente outra busca ou volte para a agenda completa.</p>
          {query ? (
            <Button className="mt-5" variant="outline" onClick={() => { setTerm(""); setQuery(""); setPage(0); }}>
              Ver todos os eventos
            </Button>
          ) : null}
        </div>
      ) : null}
      {!loading && !error && result && result.content.length > 0 ? (
        <>
          <div className="mt-7 grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {result.content.map((event) => <EventCard key={event.id} event={event} />)}
          </div>
          {result.totalPages > 1 ? (
            <nav aria-label="Paginação de eventos" className="mt-9 flex items-center justify-center gap-4">
              <Button variant="outline" size="sm" disabled={page === 0} onClick={() => setPage((value) => value - 1)}>
                Anterior
              </Button>
              <span className="text-sm text-slate-600">Página {page + 1} de {result.totalPages}</span>
              <Button variant="outline" size="sm" disabled={page + 1 >= result.totalPages} onClick={() => setPage((value) => value + 1)}>
                Próxima
              </Button>
            </nav>
          ) : null}
        </>
      ) : null}
    </section>
  );
}
