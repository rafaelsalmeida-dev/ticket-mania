export function Loading({ label = "Carregando..." }: { label?: string }) {
  return (
    <div className="flex items-center justify-center gap-3 py-16 text-sm text-slate-500" role="status">
      <span className="size-5 animate-spin rounded-full border-2 border-orange-500 border-t-transparent" />
      {label}
    </div>
  );
}
