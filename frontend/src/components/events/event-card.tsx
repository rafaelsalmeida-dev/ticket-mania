import Image from "next/image";
import Link from "next/link";
import { CalendarDays, MapPin, Ticket } from "lucide-react";
import type { EventRecord } from "@/lib/api/types";
import { formatDate } from "@/lib/utils";

export function EventCard({ event }: { event: EventRecord }) {
  return (
    <article className="group overflow-hidden rounded-3xl border border-slate-200 bg-white shadow-sm transition hover:-translate-y-1 hover:shadow-lg">
      <Link href={`/events/${event.id}`} className="block">
        <div className="relative aspect-[16/10] overflow-hidden bg-gradient-to-br from-orange-100 via-rose-50 to-indigo-100">
          {event.imageUrl ? (
            <Image
              src={event.imageUrl}
              alt=""
              fill
              unoptimized
              sizes="(max-width: 640px) 100vw, (max-width: 1024px) 50vw, 33vw"
              className="object-cover transition duration-500 group-hover:scale-105"
            />
          ) : (
            <div className="grid h-full place-items-center text-orange-500">
              <Ticket size={42} strokeWidth={1.4} />
            </div>
          )}
          <span className="absolute left-4 top-4 rounded-full bg-white/90 px-3 py-1 text-xs font-bold uppercase tracking-wide text-orange-700 backdrop-blur">
            Evento
          </span>
        </div>
        <div className="space-y-3 p-5">
          <h2 className="line-clamp-2 text-xl font-bold tracking-tight text-slate-950 group-hover:text-orange-600">
            {event.name}
          </h2>
          <p className="flex items-center gap-2 text-sm text-slate-600">
            <CalendarDays size={16} className="shrink-0 text-orange-500" />
            <time dateTime={event.date}>{formatDate(event.date)}</time>
          </p>
          <p className="flex items-center gap-2 text-sm text-slate-600">
            <MapPin size={16} className="shrink-0 text-orange-500" />
            <span className="truncate">{event.place}</span>
          </p>
        </div>
      </Link>
    </article>
  );
}
