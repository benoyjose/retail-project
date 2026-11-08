package com.retail.support.repository;

import com.retail.common.enums.SupportTicketStatus;
import com.retail.support.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {

    Optional<SupportTicket> findByTicketNumber(String ticketNumber);

    List<SupportTicket> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    List<SupportTicket> findByStatusOrderByCreatedAtDesc(SupportTicketStatus status);

    boolean existsByTicketNumber(String ticketNumber);
}

