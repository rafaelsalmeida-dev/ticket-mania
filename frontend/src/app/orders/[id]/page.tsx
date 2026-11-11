import { OrderDetails } from "@/components/orders/order-details";

export default async function OrderPage({
  params,
  searchParams,
}: {
  params: Promise<{ id: string }>;
  searchParams: Promise<{ created?: string }>;
}) {
  const [{ id }, query] = await Promise.all([params, searchParams]);
  return <OrderDetails orderId={id} created={query.created === "1"} />;
}
