package com.ozzz.skip.demo.controller;

import com.ozzz.skip.demo.dto.request.OrderRequest;
import com.ozzz.skip.demo.dto.request.UpdateOrderStatusRequest;
import com.ozzz.skip.demo.dto.response.ApiResponse;
import com.ozzz.skip.demo.dto.response.OrderResponse;
import com.ozzz.skip.demo.dto.response.PageResponse;
import com.ozzz.skip.demo.service.OrderService;
import com.ozzz.skip.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @Valid @RequestBody OrderRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long buyerId = userService.getUserByUsername(userDetails.getUsername()).getId();
        OrderResponse order = orderService.placeOrder(request, buyerId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order placed successfully")
                        .data(order)
                        .build());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getMyOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long buyerId = userService.getUserByUsername(userDetails.getUsername()).getId();
        PageResponse<OrderResponse> orders =
                orderService.getOrdersByBuyer(buyerId, page, size);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<OrderResponse>>builder()
                        .success(true)
                        .message("Orders fetched successfully")
                        .data(orders)
                        .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long buyerId = userService.getUserByUsername(userDetails.getUsername()).getId();
        OrderResponse order = orderService.getOrderById(id, buyerId);

        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order fetched successfully")
                        .data(order)
                        .build());
    }

    @GetMapping("/seller")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    public ResponseEntity<ApiResponse<PageResponse<OrderResponse>>> getSellerOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Long sellerId = userService.getUserByUsername(userDetails.getUsername()).getId();
        PageResponse<OrderResponse> orders =
                orderService.getOrdersBySeller(sellerId, page, size);

        return ResponseEntity.ok(
                ApiResponse.<PageResponse<OrderResponse>>builder()
                        .success(true)
                        .message("Seller orders fetched successfully")
                        .data(orders)
                        .build());
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_SELLER') or hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateOrderStatusRequest request) {

        OrderResponse order = orderService.updateOrderStatus(id, request);

        return ResponseEntity.ok(
                ApiResponse.<OrderResponse>builder()
                        .success(true)
                        .message("Order status updated successfully")
                        .data(order)
                        .build());
    }

    @DeleteMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    public ResponseEntity<ApiResponse<Void>> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Long buyerId = userService.getUserByUsername(userDetails.getUsername()).getId();
        orderService.cancelOrder(id, buyerId);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .success(true)
                        .message("Order cancelled successfully")
                        .build());
    }
}