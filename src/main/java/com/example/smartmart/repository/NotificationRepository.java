package com.example.smartmart.repository;

import com.example.smartmart.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Page<Notification> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);
    long countByCustomerIdAndReadFalse(Long customerId);

    @Modifying
    @Query("UPDATE Notification n SET n.read = true WHERE n.customer.id = :customerId")
    void markAllAsReadByCustomerId(@Param("customerId") Long customerId);
}
