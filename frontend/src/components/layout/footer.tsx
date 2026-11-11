import Link from "next/link";
import { Ticket } from "lucide-react";

export function Footer() {
  return (
    <footer className="mt-auto border-t border-slate-200 bg-white">
      <div className="mx-auto flex max-w-7xl flex-col items-center justify-between gap-3 px-5 py-7 text-sm text-slate-500 sm:flex-row lg:px-8">
        <Link href="/" className="flex items-center gap-2 font-bold text-slate-800">
          <Ticket size={17} className="text-orange-500" /> ticketmania
        </Link>
        <span>Seu próximo grande momento começa aqui.</span>
      </div>
    </footer>
  );
}
