package com.ozzz.skip.demo.controller.web;

import com.ozzz.skip.demo.dto.request.OrderItemRequest;
import com.ozzz.skip.demo.dto.request.OrderRequest;
import com.ozzz.skip.demo.dto.request.UpdateOrderStatusRequest;
import com.ozzz.skip.demo.dto.response.OrderResponse;
import com.ozzz.skip.demo.dto.response.PageResponse;
import com.ozzz.skip.demo.model.OrderStatus;
import com.ozzz.skip.demo.service.OrderService;
import com.ozzz.skip.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderWebController {

    private final OrderService orderService;
    private final UserService userService;

    @PostMapping("/quick-order")
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    public String quickOrder(
            @RequestParam Long productId,
            @RequestParam Integer quantity,
            @RequestParam String shippingAddress,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Long buyerId = userService
                    .getUserByUsername(userDetails.getUsername()).getId();

            OrderItemRequest item = new OrderItemRequest();
            item.setProductId(productId);
            item.setQuantity(quantity);

            OrderRequest request = new OrderRequest();
            request.setItems(List.of(item));
            request.setShippingAddress(shippingAddress);
            request.setPaymentMethod("ONLINE");

            OrderResponse order = orderService.placeOrder(request, buyerId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Order placed successfully! Order #" + order.getId());

            return "redirect:/orders/" + order.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    e.getMessage());
            return "redirect:/products/" + productId;
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    public String myOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Long buyerId = userService
                .getUserByUsername(userDetails.getUsername()).getId();
        PageResponse<OrderResponse> orders =
                orderService.getOrdersByBuyer(buyerId, page, size);

        model.addAttribute("orders", orders);
        return "orders/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    public String orderDetail(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Long buyerId = userService
                .getUserByUsername(userDetails.getUsername()).getId();
        OrderResponse order = orderService.getOrderById(id, buyerId);

        model.addAttribute("order", order);
        return "orders/detail";
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ROLE_BUYER')")
    public String cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Long buyerId = userService
                    .getUserByUsername(userDetails.getUsername()).getId();
            orderService.cancelOrder(id, buyerId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Order #" + id + " cancelled successfully.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    e.getMessage());
        }

        return "redirect:/orders/" + id;
    }

    @GetMapping("/seller")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    public String sellerOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Long sellerId = userService
                .getUserByUsername(userDetails.getUsername()).getId();
        PageResponse<OrderResponse> orders =
                orderService.getOrdersBySeller(sellerId, page, size);

        model.addAttribute("orders",       orders);
        model.addAttribute("orderStatuses", OrderStatus.values());
        return "orders/seller-orders";
    }

    @PostMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ROLE_SELLER') or hasAuthority('ROLE_ADMIN')")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            RedirectAttributes redirectAttributes) {

        try {
            UpdateOrderStatusRequest request = new UpdateOrderStatusRequest();
            request.setStatus(status);
            orderService.updateOrderStatus(id, request);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Order #" + id + " status updated to " + status);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    e.getMessage());
        }

        return "redirect:/orders/seller";
    }
}