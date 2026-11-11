"use client";

import Link from "next/link";
import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowLeft, Save } from "lucide-react";
import { useRouter } from "next/navigation";
import { useCallback, useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { createEvent, findEventById, updateEvent } from "@/lib/api/events";
import { getErrorMessage } from "@/lib/api/errors";
import type { EventInput } from "@/lib/api/types";
import { Button } from "@/components/ui/button";
import { Field, Input } from "@/components/ui/field";
import { Loading } from "@/components/ui/loading";
import { Notice } from "@/components/ui/notice";

const eventSchema = z.object({
  name: z.string().trim().min(1, "Informe o nome do evento."),
  date: z.string().min(1, "Informe a data e o horário.").refine(
    (value) => Number.isFinite(new Date(value).getTime()) && new Date(value).getTime() > Date.now(),
    "A data do evento deve estar no futuro.",
  ),
  place: z.string().trim().min(1, "Informe o local."),
  capacity: z.coerce.number().int("Informe um número inteiro.").positive("A capacidade deve ser maior que zero."),
  imageUrl: z.union([z.literal(""), z.string().url("Informe uma URL válida.")]),
});

type EventFormInput = z.input<typeof eventSchema>;
type EventFormValues = z.output<typeof eventSchema>;

function localDateInput(date?: string) {
  if (date) return date.slice(0, 16);
  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
  return now.toISOString().slice(0, 16);
}

export function EventEditor({ eventId }: { eventId?: string }) {
  const router = useRouter();
  const [loading, setLoading] = useState(Boolean(eventId));
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const form = useForm<EventFormInput, unknown, EventFormValues>({
    resolver: zodResolver(eventSchema),
    defaultValues: { name: "", date: localDateInput(), place: "", capacity: 1, imageUrl: "" },
  });

  const loadEvent = useCallback(async () => {
    if (!eventId) return;
    setLoading(true);
    setError("");
    try {
      const current = await findEventById(eventId);
      if (!current) throw new Error("Evento não encontrado.");
      form.reset({
        name: current.name,
        date: localDateInput(current.date),
        place: current.place,
        capacity: current.capacity,
        imageUrl: current.imageUrl ?? "",
      });
    } catch (cause) {
      setError(getErrorMessage(cause));
    } finally {
      setLoading(false);
    }
  }, [eventId, form]);

  useEffect(() => { void loadEvent(); }, [loadEvent]);

  async function save(values: EventFormValues) {
    setError("");
    setSuccess("");
    const input: EventInput = {
      name: values.name.trim(),
      date: values.date.length === 16 ? `${values.date}:00` : values.date,
      place: values.place.trim(),
      capacity: values.capacity,
      imageUrl: values.imageUrl.trim() || null,
    };
    try {
      if (eventId) {
        await updateEvent(eventId, input);
        setSuccess("Evento atualizado com sucesso.");
        await loadEvent();
      } else {
        const created = await createEvent(input);
        setSuccess("Evento criado com sucesso.");
        router.replace(`/admin/events/${created.id}`);
        router.refresh();
      }
    } catch (cause) {
      setError(getErrorMessage(cause));
    }
  }

  if (loading) return <Loading label="Carregando evento..." />;

  return (
    <section className="mx-auto max-w-3xl px-5 py-12 lg:px-8">
      <Link href="/admin/events" className="inline-flex items-center gap-2 text-sm font-semibold text-slate-600 hover:text-orange-600">
        <ArrowLeft size={16} /> Voltar aos eventos
      </Link>
      <div className="mt-6 rounded-3xl border border-slate-200 bg-white p-6 shadow-sm sm:p-9">
        <p className="text-sm font-bold uppercase tracking-[0.16em] text-orange-600">Painel administrativo</p>
        <h1 className="mt-2 text-3xl font-black text-slate-950">{eventId ? "Editar evento" : "Novo evento"}</h1>
        {error ? <div className="mt-6"><Notice>{error}</Notice></div> : null}
        {success ? <div className="mt-6"><Notice variant="success">{success}</Notice></div> : null}
        <form className="mt-7 space-y-5" onSubmit={form.handleSubmit(save)}>
          <Field label="Nome do evento" error={form.formState.errors.name?.message}>
            <Input placeholder="Ex.: Festival de música" {...form.register("name")} />
          </Field>
          <div className="grid gap-5 sm:grid-cols-2">
            <Field label="Data e horário" error={form.formState.errors.date?.message}>
              <Input type="datetime-local" {...form.register("date")} />
            </Field>
            <Field label="Capacidade" error={form.formState.errors.capacity?.message}>
              <Input type="number" min={1} step={1} {...form.register("capacity")} />
            </Field>
          </div>
          <Field label="Local" error={form.formState.errors.place?.message}>
            <Input placeholder="Cidade, espaço ou endereço" {...form.register("place")} />
          </Field>
          <Field label="URL da imagem (opcional)" error={form.formState.errors.imageUrl?.message}>
            <Input type="url" placeholder="https://exemplo.com/imagem.jpg" {...form.register("imageUrl")} />
          </Field>
          <Button type="submit" className="rounded-xl" disabled={form.formState.isSubmitting}>
            <Save size={17} />{form.formState.isSubmitting ? "Salvando..." : "Salvar evento"}
          </Button>
        </form>
      </div>
    </section>
  );
}
