package com.ozzz.skip.demo.service.impl;

import com.ozzz.skip.demo.dto.request.OrderItemRequest;
import com.ozzz.skip.demo.dto.request.OrderRequest;
import com.ozzz.skip.demo.dto.request.UpdateOrderStatusRequest;
import com.ozzz.skip.demo.dto.response.OrderItemResponse;
import com.ozzz.skip.demo.dto.response.OrderResponse;
import com.ozzz.skip.demo.dto.response.PageResponse;
import com.ozzz.skip.demo.exception.BusinessException;
import com.ozzz.skip.demo.exception.ResourceNotFoundException;
import com.ozzz.skip.demo.exception.UnauthorizedException;
import com.ozzz.skip.demo.model.*;
import com.ozzz.skip.demo.repository.OrderRepository;
import com.ozzz.skip.demo.repository.ProductRepository;
import com.ozzz.skip.demo.repository.UserRepository;
import com.ozzz.skip.demo.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public OrderResponse placeOrder(OrderRequest request, Long buyerId) {

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", buyerId));

        // Only buyers can place orders
        if (buyer.getRole() != Role.ROLE_BUYER) {
            throw new UnauthorizedException("Only buyers can place orders");
        }

        // Build order shell first
        Order order = Order.builder()
                .buyer(buyer)
                .status(OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .paymentMethod(request.getPaymentMethod())
                .notes(request.getNotes())
                .orderItems(new ArrayList<>())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        // Process each item in the order
        for (OrderItemRequest itemRequest : request.getItems()) {

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Product", "id", itemRequest.getProductId()));

            // Business rule: product must be active
            if (product.getStatus() != ProductStatus.ACTIVE) {
                throw new BusinessException(
                        "Product is not available: " + product.getName());
            }

            // Business rule: enough stock must exist
            if (product.getStockQuantity() < itemRequest.getQuantity()) {
                throw new BusinessException(
                        "Insufficient stock for product: " + product.getName() +
                                ". Available: " + product.getStockQuantity());
            }

            // Deduct stock
            product.setStockQuantity(product.getStockQuantity() - itemRequest.getQuantity());

            // Auto mark as sold out if stock hits zero
            if (product.getStockQuantity() == 0) {
                product.setStatus(ProductStatus.SOLD_OUT);
            }
            productRepository.save(product);

            // Build order item — snapshot the price at time of purchase
            BigDecimal unitPrice = product.getPrice();
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .build();

            order.getOrderItems().add(orderItem);
            totalAmount = totalAmount.add(
                    unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        order.setTotalAmount(totalAmount);
        return mapToResponse(orderRepository.save(order));
    }

    @Override
    public OrderResponse getOrderById(Long orderId, Long buyerId) {
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));
        return mapToResponse(order);
    }

    @Override
    public PageResponse<OrderResponse> getOrdersByBuyer(Long buyerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findByBuyerId(buyerId, pageable);
        return mapToPageResponse(orderPage);
    }

    @Override
    public PageResponse<OrderResponse> getOrdersBySeller(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Order> orderPage = orderRepository.findOrdersBySellerId(sellerId, pageable);
        return mapToPageResponse(orderPage);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Business rule: cannot update a cancelled or delivered order
        if (order.getStatus() == OrderStatus.CANCELLED ||
                order.getStatus() == OrderStatus.DELIVERED) {
            throw new BusinessException(
                    "Cannot update status of a " + order.getStatus() + " order");
        }

        order.setStatus(request.getStatus());
        return mapToResponse(orderRepository.save(order));
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long buyerId) {
        Order order = orderRepository.findByIdAndBuyerId(orderId, buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Business rule: only PENDING orders can be cancelled
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException(
                    "Only pending orders can be cancelled. Current status: " + order.getStatus());
        }

        // Restore stock for each item
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            if (product.getStatus() == ProductStatus.SOLD_OUT) {
                product.setStatus(ProductStatus.ACTIVE);
            }
            productRepository.save(product);
        }

        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
    }

    // ── Mappers ────────────────────────────────────────────────────────────
    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getOrderItems()
                .stream()
                .map(this::mapItemToResponse)
                .collect(Collectors.toList());

        return OrderResponse.builder()
                .id(order.getId())
                .buyerId(order.getBuyer().getId())
                .buyerUsername(order.getBuyer().getUsername())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .shippingAddress(order.getShippingAddress())
                .paymentMethod(order.getPaymentMethod())
                .notes(order.getNotes())
                .orderItems(itemResponses)
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    private OrderItemResponse mapItemToResponse(OrderItem item) {
        return OrderItemResponse.builder()
                .id(item.getId())
                .productId(item.getProduct().getId())
                .productName(item.getProduct().getName())
                .productImageUrl(item.getProduct().getImageUrl())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .subTotal(item.getSubTotal())
                .build();
    }

    private PageResponse<OrderResponse> mapToPageResponse(Page<Order> page) {
        return PageResponse.<OrderResponse>builder()
                .content(page.getContent().stream().map(this::mapToResponse).collect(Collectors.toList()))
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .lastPage(page.isLast())
                .build();
    }
}