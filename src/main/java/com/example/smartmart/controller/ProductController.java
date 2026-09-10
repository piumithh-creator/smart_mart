package com.example.smartmart.controller;

import com.example.smartmart.constant.CommonResponse;
import com.example.smartmart.constant.ResponseCode;
import com.example.smartmart.constant.ResponseMessage;
import com.example.smartmart.dto.request.ProductRequest;
import com.example.smartmart.dto.response.PagedResponse;
import com.example.smartmart.dto.response.ProductResponse;
import com.example.smartmart.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping(value = "/api/products", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @PostMapping
    public CommonResponse create(@Valid @RequestBody ProductRequest request) {
        return new CommonResponse(ResponseCode.OPERATION_SUCCESS, productService.create(request),
                ResponseMessage.RESPONSE_MESSAGE);
    }

    @GetMapping
    public CommonResponse findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        PagedResponse<ProductResponse> response;
        if (categoryId != null || minPrice != null || maxPrice != null) {
            response = productService.filter(categoryId, minPrice, maxPrice,
                    PageRequest.of(page, size, Sort.by(sortBy)));
        } else {
            response = productService.findAll(PageRequest.of(page, size, Sort.by(sortBy)));
        }
        return new CommonResponse(ResponseCode.OPERATION_SUCCESS, response, ResponseMessage.RESPONSE_MESSAGE);
    }

    @GetMapping("/search")
    public CommonResponse search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PagedResponse<ProductResponse> response = productService.search(keyword, PageRequest.of(page, size));
        return new CommonResponse(ResponseCode.OPERATION_SUCCESS, response, ResponseMessage.RESPONSE_MESSAGE);
    }

    @GetMapping("/{id}")
    public CommonResponse findById(@PathVariable Long id) {
        return new CommonResponse(ResponseCode.OPERATION_SUCCESS, productService.findById(id),
                ResponseMessage.RESPONSE_MESSAGE);
    }

    @PutMapping("/{id}")
    public CommonResponse update(@PathVariable Long id, @Valid @RequestBody ProductRequest request) {
        return new CommonResponse(ResponseCode.OPERATION_SUCCESS, productService.update(id, request),
                ResponseMessage.RESPONSE_MESSAGE);
    }

    @PatchMapping("/{id}")
    public CommonResponse patch(@PathVariable Long id, @RequestBody ProductRequest request) {
        return new CommonResponse(ResponseCode.OPERATION_SUCCESS, productService.patch(id, request),
                ResponseMessage.RESPONSE_MESSAGE);
    }

    @DeleteMapping("/{id}")
    public CommonResponse delete(@PathVariable Long id) {
        productService.delete(id);
        return new CommonResponse(ResponseCode.OPERATION_SUCCESS, ResponseMessage.RESPONSE_MESSAGE);
    }
}
