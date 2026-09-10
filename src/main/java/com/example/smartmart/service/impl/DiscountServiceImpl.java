package com.example.smartmart.service.impl;

import com.example.smartmart.dto.request.DiscountRequest;
import com.example.smartmart.dto.response.DiscountResponse;
import com.example.smartmart.entity.Discount;
import com.example.smartmart.entity.Product;
import com.example.smartmart.entity.ProductDiscount;
import com.example.smartmart.exception.BusinessException;
import com.example.smartmart.exception.DuplicateResourceException;
import com.example.smartmart.exception.ResourceNotFoundException;
import com.example.smartmart.repository.DiscountRepository;
import com.example.smartmart.repository.ProductDiscountRepository;
import com.example.smartmart.repository.ProductRepository;
import com.example.smartmart.service.DiscountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DiscountServiceImpl implements DiscountService {

    private static final Logger logger = LoggerFactory.getLogger(DiscountServiceImpl.class);

    private final DiscountRepository discountRepository;
    private final ProductRepository productRepository;
    private final ProductDiscountRepository productDiscountRepository;

    public DiscountServiceImpl(DiscountRepository discountRepository,
                                ProductRepository productRepository,
                                ProductDiscountRepository productDiscountRepository) {
        this.discountRepository = discountRepository;
        this.productRepository = productRepository;
        this.productDiscountRepository = productDiscountRepository;
    }

    @Override
    @Transactional
    public DiscountResponse create(DiscountRequest request) {
        if (discountRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Discount with code '" + request.getCode() + "' already exists");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("End date must be after start date");
        }
        Discount discount = Discount.builder()
                .code(request.getCode())
                .description(request.getDescription())
                .percentage(request.getPercentage())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(request.getActive() != null ? request.getActive() : true)
                .build();
        Discount saved = discountRepository.save(discount);
        logger.info("Discount created: {}", saved.getCode());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountResponse> findAll() {
        return discountRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DiscountResponse findById(Long id) {
        return mapToResponse(findDiscountById(id));
    }

    @Override
    @Transactional
    public DiscountResponse update(Long id, DiscountRequest request) {
        Discount discount = findDiscountById(id);
        if (!discount.getCode().equalsIgnoreCase(request.getCode())
                && discountRepository.existsByCode(request.getCode())) {
            throw new DuplicateResourceException("Discount with code '" + request.getCode() + "' already exists");
        }
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BusinessException("End date must be after start date");
        }
        discount.setCode(request.getCode());
        discount.setDescription(request.getDescription());
        discount.setPercentage(request.getPercentage());
        discount.setStartDate(request.getStartDate());
        discount.setEndDate(request.getEndDate());
        if (request.getActive() != null) discount.setActive(request.getActive());
        logger.info("Discount updated: {}", discount.getCode());
        return mapToResponse(discountRepository.save(discount));
    }

    @Override
    @Transactional
    public DiscountResponse toggleActive(Long id) {
        Discount discount = findDiscountById(id);
        discount.setActive(!discount.isActive());
        return mapToResponse(discountRepository.save(discount));
    }

    @Override
    @Transactional
    public void assignToProduct(Long discountId, Long productId) {
        Discount discount = findDiscountById(discountId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (productDiscountRepository.existsByProductIdAndDiscountId(productId, discountId)) {
            throw new DuplicateResourceException("Discount already assigned to this product");
        }

        ProductDiscount pd = ProductDiscount.builder()
                .product(product)
                .discount(discount)
                .build();
        productDiscountRepository.save(pd);
        logger.info("Discount {} assigned to product {}", discount.getCode(), product.getName());
    }

    @Override
    @Transactional
    public void removeFromProduct(Long discountId, Long productId) {
        ProductDiscount pd = productDiscountRepository
                .findByProductIdAndDiscountId(productId, discountId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductDiscount not found"));
        productDiscountRepository.delete(pd);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Discount discount = findDiscountById(id);
        discountRepository.delete(discount);
        logger.info("Discount deleted: {}", discount.getCode());
    }

    private Discount findDiscountById(Long id) {
        return discountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Discount", "id", id));
    }

    private DiscountResponse mapToResponse(Discount discount) {
        return DiscountResponse.builder()
                .id(discount.getId())
                .code(discount.getCode())
                .description(discount.getDescription())
                .percentage(discount.getPercentage())
                .startDate(discount.getStartDate())
                .endDate(discount.getEndDate())
                .active(discount.isActive())
                .createdAt(discount.getCreatedAt())
                .build();
    }
}
