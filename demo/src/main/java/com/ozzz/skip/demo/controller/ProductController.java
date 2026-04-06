package com.ozzz.skip.demo.controller;

import com.ozzz.skip.demo.dto.request.ProductRequest;
import com.ozzz.skip.demo.dto.response.ApiResponse;
import com.ozzz.skip.demo.dto.response.PageResponse;
import com.ozzz.skip.demo.dto.response.ProductResponse;
import com.ozzz.skip.demo.service.ProductService;
import com.ozzz.skip.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long sellerId = userService.getUserByUsername(userDetails.getUsername()).getId();
        ProductResponse product = productService.createProduct(request, sellerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product created successfully")
                        .data(product)
                        .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<ProductResponse> products =
                productService.getAllActiveProducts(page, size);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<ProductResponse>>builder()
                        .success(true)
                        .message("Products fetched successfully")
                        .data(products)
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
            @PathVariable Long id) {

        ProductResponse product = productService.getProductById(id);

        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product fetched successfully")
                        .data(product)
                        .build());
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<ProductResponse> products =
                productService.searchProducts(keyword, page, size);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<ProductResponse>>builder()
                        .success(true)
                        .message("Search results fetched successfully")
                        .data(products)
                        .build());
    }

    @GetMapping("/filter")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> filterProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<ProductResponse> products =
                productService.filterProducts(keyword, categoryId, minPrice, maxPrice, page, size);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<ProductResponse>>builder()
                        .success(true)
                        .message("Filtered products fetched successfully")
                        .data(products)
                        .build());
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        PageResponse<ProductResponse> products =
                productService.getProductsByCategory(categoryId, page, size);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<ProductResponse>>builder()
                        .success(true)
                        .message("Products by category fetched successfully")
                        .data(products)
                        .build());
    }

    @GetMapping("/my-listings")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getMyProducts(
            @AuthenticationPrincipal UserDetails userDetails) {

        Long sellerId = userService.getUserByUsername(userDetails.getUsername()).getId();
        List<ProductResponse> products = productService.getProductsBySeller(sellerId);

        return ResponseEntity.ok(
                ApiResponse.<List<ProductResponse>>builder()
                        .success(true)
                        .message("Seller products fetched successfully")
                        .data(products)
                        .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long sellerId = userService.getUserByUsername(userDetails.getUsername()).getId();
        ProductResponse product = productService.updateProduct(id, request, sellerId);

        return ResponseEntity.ok(
                ApiResponse.<ProductResponse>builder()
                        .success(true)
                        .message("Product updated successfully")
                        .data(product)
                        .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long sellerId = userService.getUserByUsername(userDetails.getUsername()).getId();
        productService.deleteProduct(id, sellerId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Product deleted successfully")
                        .build());
    }
}