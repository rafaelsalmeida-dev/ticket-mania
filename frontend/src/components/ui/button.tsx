import type { ButtonHTMLAttributes } from "react";

type Props = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "outline" | "ghost" | "danger";
  size?: "default" | "sm";
};

export function Button({
  variant = "primary",
  size = "default",
  className = "",
  ...props
}: Props) {
  return (
    <button
      className={`inline-flex items-center justify-center gap-2 rounded-full font-semibold transition-colors disabled:pointer-events-none disabled:opacity-50 ${
        size === "sm" ? "px-4 py-2 text-sm" : "px-6 py-3 text-sm"
      } ${
        variant === "primary"
          ? "bg-orange-500 text-white hover:bg-orange-400"
          : variant === "outline"
            ? "border border-slate-300 bg-white text-slate-800 hover:border-orange-400 hover:text-orange-600"
            : variant === "danger"
              ? "bg-rose-600 text-white hover:bg-rose-500"
              : "text-slate-600 hover:bg-slate-100"
      } ${className}`}
      {...props}
    />
  );
}
