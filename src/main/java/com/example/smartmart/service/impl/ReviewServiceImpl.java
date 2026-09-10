package com.example.smartmart.service.impl;

import com.example.smartmart.dto.request.ReviewRequest;
import com.example.smartmart.dto.response.PagedResponse;
import com.example.smartmart.dto.response.ReviewResponse;
import com.example.smartmart.entity.Customer;
import com.example.smartmart.entity.Product;
import com.example.smartmart.entity.Review;
import com.example.smartmart.exception.BusinessException;
import com.example.smartmart.exception.DuplicateResourceException;
import com.example.smartmart.exception.ResourceNotFoundException;
import com.example.smartmart.repository.CustomerRepository;
import com.example.smartmart.repository.ProductRepository;
import com.example.smartmart.repository.ReviewRepository;
import com.example.smartmart.service.ReviewService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewServiceImpl implements ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository reviewRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    public ReviewServiceImpl(ReviewRepository reviewRepository,
                              CustomerRepository customerRepository,
                              ProductRepository productRepository) {
        this.reviewRepository = reviewRepository;
        this.customerRepository = customerRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public ReviewResponse create(String email, ReviewRequest request) {
        Customer customer = customerRepository.findByUserEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "email", email));
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", request.getProductId()));

        if (reviewRepository.existsByCustomerIdAndProductId(customer.getId(), product.getId())) {
            throw new DuplicateResourceException("You have already reviewed this product");
        }

        Review review = Review.builder()
                .customer(customer)
                .product(product)
                .rating(request.getRating())
                .comment(request.getComment())
                .build();

        Review saved = reviewRepository.save(review);
        logger.info("Review created by {} for product: {}", email, product.getName());
        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<ReviewResponse> getByProduct(Long productId, Pageable pageable) {
        Page<ReviewResponse> page = reviewRepository.findByProductId(productId, pageable)
                .map(this::mapToResponse);
        return PagedResponse.of(page);
    }

    @Override
    @Transactional
    public ReviewResponse update(String email, Long reviewId, ReviewRequest request) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getCustomer().getUser().getEmail().equals(email)) {
            throw new BusinessException("You can only update your own reviews");
        }

        review.setRating(request.getRating());
        if (request.getComment() != null) review.setComment(request.getComment());
        logger.info("Review updated: {} by {}", reviewId, email);
        return mapToResponse(reviewRepository.save(review));
    }

    @Override
    @Transactional
    public void delete(String email, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResourceNotFoundException("Review", "id", reviewId));

        if (!review.getCustomer().getUser().getEmail().equals(email)) {
            throw new BusinessException("You can only delete your own reviews");
        }

        reviewRepository.delete(review);
        logger.info("Review deleted: {} by {}", reviewId, email);
    }

    private ReviewResponse mapToResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .customerId(review.getCustomer().getId())
                .customerName(review.getCustomer().getUser().getFirstName()
                        + " " + review.getCustomer().getUser().getLastName())
                .productId(review.getProduct().getId())
                .productName(review.getProduct().getName())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
