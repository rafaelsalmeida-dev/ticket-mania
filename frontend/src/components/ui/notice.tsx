export function Notice({
  children,
  variant = "error",
}: {
  children: React.ReactNode;
  variant?: "error" | "success" | "info";
}) {
  return (
    <p
      role={variant === "error" ? "alert" : "status"}
      className={`rounded-xl px-4 py-3 text-sm ${
        variant === "error"
          ? "bg-rose-50 text-rose-700"
          : variant === "success"
            ? "bg-emerald-50 text-emerald-700"
            : "bg-orange-50 text-orange-800"
      }`}
    >
      {children}
    </p>
  );
}
