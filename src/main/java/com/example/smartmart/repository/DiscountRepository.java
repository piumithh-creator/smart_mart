package com.example.smartmart.repository;

import com.example.smartmart.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, Long> {
    Optional<Discount> findByCode(String code);
    boolean existsByCode(String code);

    @Query("SELECT d FROM Discount d WHERE d.active = true AND d.startDate <= :today AND d.endDate >= :today")
    List<Discount> findActiveDiscounts(LocalDate today);
}
