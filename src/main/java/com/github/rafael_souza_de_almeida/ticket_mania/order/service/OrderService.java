package com.github.rafael_souza_de_almeida.ticket_mania.order.service;


import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderRequestDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.dto.OrderResponseDto;
import com.github.rafael_souza_de_almeida.ticket_mania.order.exception.TicketUnavailableException;
import com.github.rafael_souza_de_almeida.ticket_mania.user.domain.User;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class OrderService {

    private final RedissonClient redissonClient;
    private final OrderTransactionalService orderTransactionalService;

    public OrderService(RedissonClient redissonClient, OrderTransactionalService orderTransactionalService) {
        this.redissonClient = redissonClient;
        this.orderTransactionalService = orderTransactionalService;
    }

    public OrderResponseDto create(OrderRequestDto dto) {

        var auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !(auth.getPrincipal() instanceof User loggedUser)) {
            throw new InsufficientAuthenticationException("User is not authenticated or session is invalid.");
        }

        String lockKey = "lock:ticket:" + dto.ticketId();
        RLock lock = redissonClient.getLock(lockKey);

        try {
            boolean isLocked = lock.tryLock(1, 10, TimeUnit.SECONDS);

            if (!isLocked) {
                throw new TicketUnavailableException("High traffic volume. This ticket is currently being processed by another user.");
            }

            return orderTransactionalService.createWithTransaction(dto, loggedUser.getId());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting for lock", e);

        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }



}
