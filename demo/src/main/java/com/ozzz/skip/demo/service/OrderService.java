package com.ozzz.skip.demo.service;

import com.ozzz.skip.demo.dto.request.OrderRequest;
import com.ozzz.skip.demo.dto.request.UpdateOrderStatusRequest;
import com.ozzz.skip.demo.dto.response.OrderResponse;
import com.ozzz.skip.demo.dto.response.PageResponse;

public interface OrderService {

    OrderResponse placeOrder(OrderRequest request, Long buyerId);

    OrderResponse getOrderById(Long orderId, Long buyerId);

    PageResponse<OrderResponse> getOrdersByBuyer(Long buyerId, int page, int size);

    PageResponse<OrderResponse> getOrdersBySeller(Long sellerId, int page, int size);

    OrderResponse updateOrderStatus(Long orderId, UpdateOrderStatusRequest request);

    void cancelOrder(Long orderId, Long buyerId);
}