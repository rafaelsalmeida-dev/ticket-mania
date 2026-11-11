"use client";

import { useCallback, useEffect, useState, type FormEvent } from "react";
import { createTicketBatch, deleteAvailableTickets, getTicketAvailability, updateTicketPrice } from "@/lib/api/events";
import { getErrorMessage } from "@/lib/api/errors";
import type { TicketAvailability, TicketType } from "@/lib/api/types";
import { formatCurrency, ticketLabels } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input, Select } from "@/components/ui/field";
import { Loading } from "@/components/ui/loading";
import { Notice } from "@/components/ui/notice";

export function TicketInventory({ eventId }: { eventId: string }) {
  const [tickets, setTickets] = useState<TicketAvailability[]>([]);
  const [type, setType] = useState<TicketType>("FULL");
  const [price, setPrice] = useState("");
  const [quantity, setQuantity] = useState("1");
  const [quantities, setQuantities] = useState<Record<string, string>>({});
  const [prices, setPrices] = useState<Record<string, string>>({});
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  const reload = useCallback(async () => {
    setLoading(true);
    setError("");
    try {
      const data = await getTicketAvailability(eventId);
      setTickets(data);
      setPrices(Object.fromEntries(data.map((ticket) => [ticket.ticketType, String(ticket.price)])));
    } catch (cause) {
      setError(getErrorMessage(cause));
    } finally {
      setLoading(false);
    }
  }, [eventId]);

  useEffect(() => { void reload(); }, [reload]);

  async function createBatch(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    const amount = Number(price);
    const count = Number(quantity);
    if (!Number.isFinite(amount) || amount < 0 || !Number.isInteger(count) || count < 1 || count > 50_000) {
      setError("Informe um preço não negativo e uma quantidade entre 1 e 50.000.");
      return;
    }
    setSaving(true);
    setError("");
    setSuccess("");
    try {
      const result = await createTicketBatch(eventId, { type, price: amount, quantity: count });
      setSuccess(`${result.created} ingresso(s) criado(s). Estoque disponível: ${result.totalAvailableOfType}.`);
      setPrice("");
      setQuantity("1");
      await reload();
    } catch (cause) {
      setError(getErrorMessage(cause));
    } finally {
      setSaving(false);
    }
  }

  async function savePrice(ticketType: TicketType) {
    const nextPrice = Number(prices[ticketType]);
    if (!Number.isFinite(nextPrice) || nextPrice < 0) {
      setError("Informe um preço válido, igual ou maior que zero.");
      return;
    }
    setSaving(true);
    setError("");
    setSuccess("");
    try {
      const result = await updateTicketPrice(eventId, ticketType, nextPrice);
      setSuccess(`Preço atualizado. Estoque disponível: ${result.totalAvailableOfType}.`);
      await reload();
    } catch (cause) {
      setError(getErrorMessage(cause));
    } finally {
      setSaving(false);
    }
  }

  async function removeAvailable(ticketType: TicketType) {
    const rawQuantity = quantities[ticketType];
    const count = rawQuantity ? Number(rawQuantity) : undefined;
    if (count !== undefined && (!Number.isInteger(count) || count < 1)) {
      setError("A quantidade a remover deve ser um número inteiro maior que zero.");
      return;
    }
    setSaving(true);
    setError("");
    setSuccess("");
    try {
      const result = await deleteAvailableTickets(eventId, ticketType, count);
      setSuccess(`${result.affected} ingresso(s) removido(s). Estoque disponível: ${result.totalAvailableOfType}.`);
      setQuantities((current) => ({ ...current, [ticketType]: "" }));
      await reload();
    } catch (cause) {
      setError(getErrorMessage(cause));
    } finally {
      setSaving(false);
    }
  }

  return (
    <section className="mt-8 rounded-3xl border border-slate-200 bg-white p-6 shadow-sm sm:p-8">
      <p className="text-sm font-bold uppercase tracking-[0.16em] text-orange-600">Estoque</p>
      <h2 className="mt-2 text-2xl font-black text-slate-950">Ingressos e preços</h2>
      {error ? <div className="mt-5"><Notice>{error}</Notice></div> : null}
      {success ? <div className="mt-5"><Notice variant="success">{success}</Notice></div> : null}

      <form className="mt-6 grid gap-4 rounded-2xl bg-slate-50 p-4 sm:grid-cols-[1fr_1fr_1fr_auto] sm:items-end" onSubmit={createBatch}>
        <label className="block text-sm font-medium text-slate-700">Tipo
          <Select value={type} onChange={(event) => setType(event.target.value as TicketType)}>
            {(["FULL", "HALF", "VIP"] as const).map((ticketType) => <option key={ticketType} value={ticketType}>{ticketLabels[ticketType]}</option>)}
          </Select>
        </label>
        <label className="block text-sm font-medium text-slate-700">Preço (R$)
          <Input type="number" min={0} step="0.01" value={price} onChange={(event) => setPrice(event.target.value)} required placeholder="0,00" />
        </label>
        <label className="block text-sm font-medium text-slate-700">Quantidade
          <Input type="number" min={1} max={50000} step={1} value={quantity} onChange={(event) => setQuantity(event.target.value)} required />
        </label>
        <Button type="submit" className="rounded-xl" disabled={saving}>{saving ? "Criando..." : "Adicionar lote"}</Button>
      </form>

      {loading ? <Loading label="Carregando estoque..." /> : null}
      {!loading && tickets.length === 0 ? <p className="mt-6 rounded-2xl border border-dashed border-slate-300 p-6 text-center text-sm text-slate-500">Ainda não há ingressos. Crie o primeiro lote acima.</p> : null}
      {!loading && tickets.length > 0 ? (
        <div className="mt-6 space-y-3">
          {tickets.map((ticket) => (
            <article key={ticket.ticketType} className="grid gap-4 rounded-2xl border border-slate-200 p-4 lg:grid-cols-[1fr_1fr_1.2fr] lg:items-center">
              <div>
                <h3 className="font-bold text-slate-950">{ticketLabels[ticket.ticketType]}</h3>
                <p className="mt-1 text-sm text-slate-500">{ticket.available} disponíveis · Atual: {formatCurrency(ticket.price)}</p>
              </div>
              <div className="flex gap-2">
                <label className="sr-only" htmlFor={`price-${ticket.ticketType}`}>Novo preço para {ticketLabels[ticket.ticketType]}</label>
                <Input id={`price-${ticket.ticketType}`} type="number" min={0} step="0.01" value={prices[ticket.ticketType] ?? ""} onChange={(event) => setPrices((current) => ({ ...current, [ticket.ticketType]: event.target.value }))} />
                <Button variant="outline" size="sm" onClick={() => void savePrice(ticket.ticketType)} disabled={saving}>Atualizar preço</Button>
              </div>
              <div className="flex gap-2">
                <label className="sr-only" htmlFor={`quantity-${ticket.ticketType}`}>Quantidade a remover; vazio remove todos os disponíveis</label>
                <Input id={`quantity-${ticket.ticketType}`} type="number" min={1} step={1} placeholder="Tudo" value={quantities[ticket.ticketType] ?? ""} onChange={(event) => setQuantities((current) => ({ ...current, [ticket.ticketType]: event.target.value }))} />
                <Button variant="danger" size="sm" onClick={() => void removeAvailable(ticket.ticketType)} disabled={saving || ticket.available === 0}>Remover</Button>
              </div>
            </article>
          ))}
        </div>
      ) : null}
    </section>
  );
}
