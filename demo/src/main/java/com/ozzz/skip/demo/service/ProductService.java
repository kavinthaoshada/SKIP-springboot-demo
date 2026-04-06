package com.ozzz.skip.demo.service;

import com.ozzz.skip.demo.dto.request.ProductRequest;
import com.ozzz.skip.demo.dto.response.PageResponse;
import com.ozzz.skip.demo.dto.response.ProductResponse;

import java.math.BigDecimal;
import java.util.List;

public interface ProductService {

    ProductResponse createProduct(ProductRequest request, Long sellerId);

    ProductResponse getProductById(Long id);

    PageResponse<ProductResponse> getAllActiveProducts(int page, int size);

    PageResponse<ProductResponse> getProductsByCategory(Long categoryId, int page, int size);

    PageResponse<ProductResponse> searchProducts(String keyword, int page, int size);

    PageResponse<ProductResponse> filterProducts(
            String keyword, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size);

    List<ProductResponse> getProductsBySeller(Long sellerId);

    ProductResponse updateProduct(Long productId, ProductRequest request, Long sellerId);

    void deleteProduct(Long productId, Long sellerId);
}