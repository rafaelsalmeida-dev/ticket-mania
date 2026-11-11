"use client";

import { zodResolver } from "@hookform/resolvers/zod";
import { ShieldCheck } from "lucide-react";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { z } from "zod";
import { getErrorMessage } from "@/lib/api/errors";
import { updateUserRole } from "@/lib/api/users";
import { Button } from "@/components/ui/button";
import { Field, Input, Select } from "@/components/ui/field";
import { Notice } from "@/components/ui/notice";

const roleSchema = z.object({
  userId: z.string().uuid("Informe um UUID válido."),
  role: z.enum(["ROLE_USER", "ROLE_ADMIN"]),
});

type RoleValues = z.infer<typeof roleSchema>;

export function UserRoleForm() {
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const form = useForm<RoleValues>({
    resolver: zodResolver(roleSchema),
    defaultValues: { userId: "", role: "ROLE_USER" },
  });

  async function submit(values: RoleValues) {
    setError("");
    setSuccess("");
    try {
      await updateUserRole(values.userId, values.role);
      setSuccess("Permissão atualizada com sucesso.");
      form.reset(values);
    } catch (cause) {
      setError(getErrorMessage(cause));
    }
  }

  return (
    <section className="mx-auto max-w-3xl px-5 py-12 lg:px-8">
      <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm sm:p-9">
        <span className="grid size-12 place-items-center rounded-2xl bg-orange-50 text-orange-600"><ShieldCheck size={22} /></span>
        <p className="mt-5 text-sm font-bold uppercase tracking-[0.16em] text-orange-600">Painel administrativo</p>
        <h1 className="mt-2 text-3xl font-black text-slate-950">Gerenciar permissões</h1>
        <p className="mt-3 text-sm leading-6 text-slate-600">O backend permite alterar a role pelo UUID do usuário. Como não existe endpoint para listar ou consultar usuários, informe um UUID conhecido.</p>
        {error ? <div className="mt-6"><Notice>{error}</Notice></div> : null}
        {success ? <div className="mt-6"><Notice variant="success">{success}</Notice></div> : null}
        <form className="mt-7 space-y-5" onSubmit={form.handleSubmit(submit)}>
          <Field label="UUID do usuário" error={form.formState.errors.userId?.message}>
            <Input placeholder="00000000-0000-0000-0000-000000000000" autoComplete="off" {...form.register("userId")} />
          </Field>
          <Field label="Permissão">
            <Select {...form.register("role")}>
              <option value="ROLE_USER">Usuário</option>
              <option value="ROLE_ADMIN">Administrador</option>
            </Select>
          </Field>
          <Button type="submit" className="rounded-xl" disabled={form.formState.isSubmitting}>
            {form.formState.isSubmitting ? "Salvando..." : "Atualizar permissão"}
          </Button>
        </form>
      </div>
    </section>
  );
}
