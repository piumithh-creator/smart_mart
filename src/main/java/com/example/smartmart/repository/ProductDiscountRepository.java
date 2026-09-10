package com.example.smartmart.repository;

import com.example.smartmart.entity.ProductDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductDiscountRepository extends JpaRepository<ProductDiscount, Long> {
    Optional<ProductDiscount> findByProductIdAndDiscountId(Long productId, Long discountId);
    boolean existsByProductIdAndDiscountId(Long productId, Long discountId);

    @Query("SELECT pd FROM ProductDiscount pd WHERE pd.product.id = :productId " +
           "AND pd.discount.active = true " +
           "AND pd.discount.startDate <= :today AND pd.discount.endDate >= :today")
    List<ProductDiscount> findActiveDiscountsForProduct(@Param("productId") Long productId,
                                                         @Param("today") LocalDate today);
}
