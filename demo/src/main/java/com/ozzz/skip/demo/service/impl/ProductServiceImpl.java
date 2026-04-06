package com.ozzz.skip.demo.service.impl;

import com.ozzz.skip.demo.dto.request.ProductRequest;
import com.ozzz.skip.demo.dto.response.PageResponse;
import com.ozzz.skip.demo.dto.response.ProductResponse;
import com.ozzz.skip.demo.exception.BusinessException;
import com.ozzz.skip.demo.exception.ResourceNotFoundException;
import com.ozzz.skip.demo.exception.UnauthorizedException;
import com.ozzz.skip.demo.model.*;
import com.ozzz.skip.demo.repository.CategoryRepository;
import com.ozzz.skip.demo.repository.ProductRepository;
import com.ozzz.skip.demo.repository.UserRepository;
import com.ozzz.skip.demo.service.FileStorageService;
import com.ozzz.skip.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    @Override
    @Transactional
    public ProductResponse createProduct(ProductRequest request, Long sellerId) {

        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User", "id", sellerId));

        if (seller.getRole() != Role.ROLE_SELLER) {
            throw new UnauthorizedException("Only sellers can create products");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category", "id", request.getCategoryId()));

        String imageUrl = resolveImageUrl(request, null);

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(imageUrl)
                .status(ProductStatus.ACTIVE)
                .seller(seller)
                .category(category)
                .build();

        return mapToResponse(productRepository.save(product));
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
        return mapToResponse(product);
    }

    @Override
    public PageResponse<ProductResponse> getAllActiveProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
        return mapToPageResponse(productPage);
    }

    @Override
    public PageResponse<ProductResponse> getProductsByCategory(Long categoryId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.findByCategoryIdAndStatus(
                categoryId, ProductStatus.ACTIVE, pageable);
        return mapToPageResponse(productPage);
    }

    @Override
    public PageResponse<ProductResponse> searchProducts(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.searchProducts(keyword, pageable);
        return mapToPageResponse(productPage);
    }

    @Override
    public PageResponse<ProductResponse> filterProducts(
            String keyword, Long categoryId,
            BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Product> productPage = productRepository.findWithFilters(
                keyword, categoryId, minPrice, maxPrice, pageable);
        return mapToPageResponse(productPage);
    }

    @Override
    public List<ProductResponse> getProductsBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(
            Long productId, ProductRequest request, Long sellerId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Product", "id", productId));

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new UnauthorizedException(
                    "You are not authorized to update this product");
        }

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category", "id", request.getCategoryId()));

        String imageUrl = resolveImageUrl(request, product.getImageUrl());

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(imageUrl);
        product.setCategory(category);

        if (request.getStockQuantity() == 0) {
            product.setStatus(ProductStatus.SOLD_OUT);
        } else {
            product.setStatus(ProductStatus.ACTIVE);
        }

        return mapToResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId, Long sellerId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        if (!product.getSeller().getId().equals(sellerId)) {
            throw new UnauthorizedException("You are not authorized to delete this product");
        }

        product.setStatus(ProductStatus.DELETED);
        productRepository.save(product);
    }

    private String resolveImageUrl(ProductRequest request,
                                   String existingImageUrl) {

        if (request.getImageFile() != null
                && !request.getImageFile().isEmpty()) {

            if (existingImageUrl != null
                    && existingImageUrl.startsWith("/uploads/")) {
                fileStorageService.deleteFile(existingImageUrl);
            }

            return fileStorageService.storeFile(request.getImageFile());
        }

        if (request.getImageUrl() != null
                && !request.getImageUrl().isBlank()) {
            return request.getImageUrl();
        }

        return existingImageUrl;
    }

    private ProductResponse mapToResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .status(product.getStatus().name())
                .sellerId(product.getSeller().getId())
                .sellerUsername(product.getSeller().getUsername())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private PageResponse<ProductResponse> mapToPageResponse(Page<Product> page) {
        return PageResponse.<ProductResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .lastPage(page.isLast())
                .build();
    }
}