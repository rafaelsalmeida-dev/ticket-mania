import { NextResponse } from "next/server";
import { getSession } from "@/lib/auth/session";

export async function GET() {
  return NextResponse.json((await getSession()) ?? { authenticated: false });
}
