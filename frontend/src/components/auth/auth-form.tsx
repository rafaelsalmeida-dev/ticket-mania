"use client";

import Link from "next/link";
import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowRight, KeyRound, Ticket } from "lucide-react";
import { useRouter } from "next/navigation";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { Button } from "@/components/ui/button";
import { Field, Input } from "@/components/ui/field";
import { Notice } from "@/components/ui/notice";
import { getErrorMessage } from "@/lib/api/errors";

const loginSchema = z.object({
  email: z.string().email("Informe um e-mail válido."),
  password: z.string().min(1, "Informe sua senha."),
});

const registerSchema = z.object({
  name: z.string().trim().min(1, "Informe seu nome."),
  email: z.string().email("Informe um e-mail válido."),
  password: z.string().min(1, "Informe uma senha."),
});

type LoginValues = z.infer<typeof loginSchema>;
type RegisterValues = z.infer<typeof registerSchema>;

async function responseMessage(response: Response) {
  const body: unknown = await response.json().catch(() => null);
  if (
    typeof body === "object" &&
    body !== null &&
    "message" in body &&
    typeof body.message === "string"
  ) return body.message;
  return response.status === 401
    ? "E-mail ou senha inválidos."
    : `Não foi possível concluir a solicitação (${response.status}).`;
}

function safeNextPath(): string {
  const requested = new URLSearchParams(window.location.search).get("next");
  return requested?.startsWith("/") && !requested.startsWith("//") ? requested : "/";
}

export function AuthForm({ mode }: { mode: "login" | "register" }) {
  const router = useRouter();
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const isLogin = mode === "login";

  const loginForm = useForm<LoginValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
  });
  const registerForm = useForm<RegisterValues>({
    resolver: zodResolver(registerSchema),
    defaultValues: { name: "", email: "", password: "" },
  });
  const busy = isLogin ? loginForm.formState.isSubmitting : registerForm.formState.isSubmitting;

  async function submit(values: LoginValues | RegisterValues) {
    setError("");
    setSuccess("");
    try {
      const response = await fetch(`/api/auth/${mode}`, {
        method: "POST",
        credentials: "same-origin",
        headers: { "content-type": "application/json" },
        body: JSON.stringify(values),
      });
      if (!response.ok) {
        setError(await responseMessage(response));
        return;
      }
      if (isLogin) {
        router.replace(safeNextPath());
        router.refresh();
      } else {
        setSuccess("Conta criada com sucesso. Agora entre para continuar.");
        registerForm.reset();
      }
    } catch (cause) {
      setError(getErrorMessage(cause));
    }
  }

  return (
    <section className="mx-auto grid min-h-[calc(100vh-10rem)] max-w-7xl place-items-center px-5 py-12 lg:px-8">
      <div className="grid w-full max-w-5xl overflow-hidden rounded-[2rem] border border-slate-200 bg-white shadow-xl shadow-slate-200/50 md:grid-cols-[0.9fr_1.1fr]">
        <div className="relative hidden min-h-[34rem] flex-col justify-between overflow-hidden bg-slate-950 p-10 text-white md:flex">
          <div className="pointer-events-none absolute -right-20 top-10 size-72 rounded-full bg-orange-500/30 blur-3xl" />
          <Link href="/" className="relative inline-flex items-center gap-3 text-lg font-extrabold">
            <span className="grid size-10 place-items-center rounded-2xl bg-orange-500"><Ticket size={21} /></span>
            ticket<span className="-ml-3 text-orange-400">mania</span>
          </Link>
          <div className="relative">
            <p className="text-sm font-bold uppercase tracking-[0.18em] text-orange-300">Seu próximo momento</p>
            <h1 className="mt-4 text-4xl font-black leading-tight">
              {isLogin ? "Entre. A experiência espera por você." : "Crie sua conta e viva mais."}
            </h1>
            <p className="mt-4 leading-7 text-slate-300">
              Encontre eventos, organize seus pedidos e tenha seus ingressos sempre à mão.
            </p>
          </div>
          <p className="relative text-xs text-slate-400">Seus dados de acesso ficam protegidos.</p>
        </div>

        <div className="p-6 sm:p-10 lg:p-14">
          <div className="mb-8 flex items-center gap-2 text-orange-600 md:hidden"><Ticket size={20} /><span className="font-extrabold">ticketmania</span></div>
          <span className="grid size-12 place-items-center rounded-2xl bg-orange-50 text-orange-600"><KeyRound size={22} /></span>
          <h2 className="mt-5 text-3xl font-black tracking-tight text-slate-950">
            {isLogin ? "Bem-vindo de volta" : "Criar sua conta"}
          </h2>
          <p className="mt-2 text-sm text-slate-500">
            {isLogin ? "Acesse seus pedidos e continue de onde parou." : "Preencha seus dados para começar."}
          </p>

          {error ? <div className="mt-6"><Notice>{error}</Notice></div> : null}
          {success ? <div className="mt-6"><Notice variant="success">{success}</Notice></div> : null}

          {isLogin ? (
            <form className="mt-7 space-y-5" onSubmit={loginForm.handleSubmit(submit)}>
              <Field label="E-mail" error={loginForm.formState.errors.email?.message}>
                <Input type="email" autoComplete="email" placeholder="voce@email.com" {...loginForm.register("email")} />
              </Field>
              <Field label="Senha" error={loginForm.formState.errors.password?.message}>
                <Input type="password" autoComplete="current-password" placeholder="Sua senha" {...loginForm.register("password")} />
              </Field>
              <Button type="submit" className="w-full rounded-xl" disabled={busy}>
                {busy ? "Entrando..." : "Entrar"} <ArrowRight size={17} />
              </Button>
            </form>
          ) : (
            <form className="mt-7 space-y-5" onSubmit={registerForm.handleSubmit(submit)}>
              <Field label="Nome" error={registerForm.formState.errors.name?.message}>
                <Input autoComplete="name" placeholder="Seu nome completo" {...registerForm.register("name")} />
              </Field>
              <Field label="E-mail" error={registerForm.formState.errors.email?.message}>
                <Input type="email" autoComplete="email" placeholder="voce@email.com" {...registerForm.register("email")} />
              </Field>
              <Field label="Senha" error={registerForm.formState.errors.password?.message}>
                <Input type="password" autoComplete="new-password" placeholder="Crie uma senha" {...registerForm.register("password")} />
              </Field>
              <Button type="submit" className="w-full rounded-xl" disabled={busy}>
                {busy ? "Criando conta..." : "Criar conta"} <ArrowRight size={17} />
              </Button>
            </form>
          )}

          <p className="mt-7 text-center text-sm text-slate-600">
            {isLogin ? "Ainda não tem uma conta?" : "Já tem uma conta?"}{" "}
            <Link className="font-bold text-orange-600 hover:text-orange-700" href={isLogin ? "/register" : "/login"}>
              {isLogin ? "Cadastre-se" : "Entrar"}
            </Link>
          </p>
        </div>
      </div>
    </section>
  );
}
