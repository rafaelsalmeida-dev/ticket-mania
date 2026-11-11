"use client";

import Link from "next/link";
import { useRouter } from "next/navigation";
import { CalendarDays, LogOut, Ticket } from "lucide-react";
import { useCallback, useEffect, useState } from "react";
import type { AuthSession } from "@/lib/api/types";
import { Button } from "@/components/ui/button";

export function Header() {
  const router = useRouter();
  const [session, setSession] = useState<AuthSession | null>(null);
  const [sessionState, setSessionState] = useState<"loading" | "ready" | "error">("loading");
  const [message, setMessage] = useState("");

  const loadSession = useCallback(async () => {
    setSessionState("loading");
    setMessage("");
    try {
      const response = await fetch("/api/auth/session", { credentials: "same-origin" });
      if (!response.ok) throw new Error("Não foi possível verificar sua sessão.");
      const data = (await response.json()) as AuthSession | { authenticated: false };
      setSession(data.authenticated ? data : null);
      setSessionState("ready");
    } catch {
      setMessage("Não foi possível verificar sua sessão.");
      setSessionState("error");
    }
  }, []);

  useEffect(() => { void loadSession(); }, [loadSession]);

  async function logout() {
    try {
      const response = await fetch("/api/auth/logout", {
        method: "POST",
        credentials: "same-origin",
      });
      if (!response.ok) {
        setMessage("Não foi possível encerrar a sessão. Tente novamente.");
        return;
      }
      setSession(null);
      setMessage("");
      router.push("/");
      router.refresh();
    } catch {
      setMessage("Não foi possível encerrar a sessão. Verifique sua conexão.");
    }
  }

  return (
    <header className="border-b border-slate-200/80 bg-white/90 backdrop-blur">
      <div className="mx-auto flex h-20 max-w-7xl items-center justify-between px-5 lg:px-8">
        <Link href="/" className="flex items-center gap-3" aria-label="Ticket Mania, início">
          <span className="grid size-10 place-items-center rounded-2xl bg-orange-500 text-white">
            <Ticket size={21} />
          </span>
          <span className="text-xl font-extrabold tracking-tight text-slate-950">
            ticket<span className="text-orange-500">mania</span>
          </span>
        </Link>
        <nav className="flex items-center gap-2 sm:gap-5">
          <Link className="hidden items-center gap-2 text-sm font-medium text-slate-600 hover:text-orange-600 sm:flex" href="/">
            <CalendarDays size={16} /> Eventos
          </Link>
          {message ? <span role="alert" className="hidden text-xs text-rose-700 md:inline">{message}</span> : null}
          {sessionState === "error" ? (
            <Button variant="ghost" size="sm" onClick={() => void loadSession()}>Tentar novamente</Button>
          ) : null}
          {sessionState === "loading" ? (
            <span className="hidden text-xs text-slate-400 sm:inline" role="status">Verificando sessão...</span>
          ) : session ? (
            <>
              {session.role === "ROLE_ADMIN" ? (
                <Link className="text-sm font-semibold text-slate-600 hover:text-orange-600" href="/admin/events">
                  Administração
                </Link>
              ) : null}
              <Link className="text-sm font-semibold text-slate-600 hover:text-orange-600" href="/orders">
                Meus pedidos
              </Link>
              <Button variant="ghost" size="sm" onClick={logout} aria-label="Sair">
                <LogOut size={17} /> <span className="hidden sm:inline">Sair</span>
              </Button>
            </>
          ) : (
            <>
              <Link className="text-sm font-semibold text-slate-600 hover:text-orange-600" href="/login">Entrar</Link>
              <Link className="rounded-full bg-slate-950 px-5 py-2.5 text-sm font-semibold text-white transition hover:bg-orange-500" href="/register">
                Criar conta
              </Link>
            </>
          )}
        </nav>
      </div>
    </header>
  );
}
