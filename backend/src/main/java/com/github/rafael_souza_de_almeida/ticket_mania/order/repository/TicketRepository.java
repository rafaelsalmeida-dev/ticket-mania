package com.github.rafael_souza_de_almeida.ticket_mania.order.repository;

import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.Ticket;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketStatus;
import com.github.rafael_souza_de_almeida.ticket_mania.order.domain.enums.TicketType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, UUID> {
    @Query(value = """
    SELECT * FROM tb_tickets
    WHERE event_id = :eventId
      AND ticket_type = :ticketType
      AND status = 'AVAILABLE'
    ORDER BY id
    LIMIT :quantity
    FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<Ticket> findAvailable(@Param("eventId") UUID eventId,
                               @Param("ticketType") String ticketType,
                               @Param("quantity") int quantity);

    @Modifying
    @Query(value = """
    INSERT INTO tb_tickets (id, event_id, price, ticket_type, status)
    SELECT gen_random_uuid(), :eventId, :price, :type, 'AVAILABLE'
    FROM generate_series(1, :quantity)
    """, nativeQuery = true)
    void generateBatch(@Param("eventId") UUID eventId,
                       @Param("price") BigDecimal price,
                       @Param("type") String type,
                       @Param("quantity") int quantity);

    long countByEventIdAndTicketTypeAndStatus(UUID eventId, TicketType type, TicketStatus status);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    UPDATE tb_tickets
    SET price = :price
    WHERE event_id = :eventId
      AND ticket_type = :type
      AND status = 'AVAILABLE'
    """, nativeQuery = true)
    int updatePriceOfAvailable(@Param("eventId") UUID eventId,
                               @Param("type") String type,
                               @Param("price") BigDecimal price);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
    DELETE FROM tb_tickets
    WHERE id IN (
        SELECT id FROM tb_tickets
        WHERE event_id = :eventId
          AND ticket_type = :type
          AND status = 'AVAILABLE'
        ORDER BY id
        LIMIT :quantity
        FOR UPDATE SKIP LOCKED
    )
    """, nativeQuery = true)
    int deleteAvailable(@Param("eventId") UUID eventId,
                        @Param("type") String type,
                        @Param("quantity") int quantity);
}


