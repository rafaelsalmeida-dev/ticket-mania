import { EventEditor } from "@/components/admin/event-editor";
import { TicketInventory } from "@/components/admin/ticket-inventory";

export default async function EditEventPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = await params;
  return (
    <>
      <EventEditor eventId={id} />
      <div className="mx-auto max-w-3xl px-5 pb-12 lg:px-8">
        <TicketInventory eventId={id} />
      </div>
    </>
  );
}
