package com.ozzz.skip.demo.controller.web;

import com.ozzz.skip.demo.dto.request.CategoryRequest;
import com.ozzz.skip.demo.dto.response.*;
import com.ozzz.skip.demo.model.OrderStatus;
import com.ozzz.skip.demo.repository.*;
import com.ozzz.skip.demo.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminWebController {

    private final UserRepository     userRepository;
    private final ProductRepository  productRepository;
    private final OrderRepository    orderRepository;
    private final CategoryRepository categoryRepository;

    private final UserService     userService;
    private final ProductService  productService;
    private final CategoryService categoryService;
    private final OrderService    orderService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {

        model.addAttribute("totalUsers",
                userRepository.count());
        model.addAttribute("totalProducts",
                productRepository.count());
        model.addAttribute("totalOrders",
                orderRepository.count());
        model.addAttribute("totalCategories",
                categoryRepository.count());

        model.addAttribute("recentOrders",
                orderRepository.findAll()
                        .stream()
                        .sorted((a, b) -> b.getCreatedAt()
                                .compareTo(a.getCreatedAt()))
                        .limit(5)
                        .toList());

        model.addAttribute("recentUsers",
                userRepository.findAll()
                        .stream()
                        .sorted((a, b) -> b.getCreatedAt()
                                .compareTo(a.getCreatedAt()))
                        .limit(5)
                        .toList());

        model.addAttribute("pendingOrders",
                orderRepository.findByStatus(OrderStatus.PENDING).size());
        model.addAttribute("shippedOrders",
                orderRepository.findByStatus(OrderStatus.SHIPPED).size());
        model.addAttribute("deliveredOrders",
                orderRepository.findByStatus(OrderStatus.DELIVERED).size());

        model.addAttribute("activePage", "dashboard");
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("activePage", "users");
        return "admin/users";
    }

    @PostMapping("/users/{id}/deactivate")
    public String deactivateUser(
            @PathVariable Long id,
            RedirectAttributes ra) {
        try {
            userService.deactivateUser(id);
            ra.addFlashAttribute("successMessage",
                    "User deactivated successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/products")
    public String products(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        PageResponse<ProductResponse> products =
                productService.getAllActiveProducts(page, size);

        model.addAttribute("products",   products);
        model.addAttribute("activePage", "products");
        return "admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(
            @PathVariable Long id,
            RedirectAttributes ra) {
        try {
            productRepository.findById(id).ifPresent(p -> {
                p.setStatus(
                        com.ozzz.skip.demo.model.ProductStatus.DELETED);
                productRepository.save(p);
            });
            ra.addFlashAttribute("successMessage",
                    "Product deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories",
                categoryService.getAllCategories());
        model.addAttribute("categoryRequest",
                new CategoryRequest());
        model.addAttribute("activePage", "categories");
        return "admin/categories";
    }

    @PostMapping("/categories/create")
    public String createCategory(
            @Valid @ModelAttribute CategoryRequest request,
            BindingResult bindingResult,
            RedirectAttributes ra,
            Model model) {

        if (bindingResult.hasErrors()) {
            ra.addFlashAttribute("errorMessage",
                    "Please fix validation errors.");
            return "redirect:/admin/categories";
        }

        try {
            categoryService.createCategory(request);
            ra.addFlashAttribute("successMessage",
                    "Category created successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(
            @PathVariable Long id,
            RedirectAttributes ra) {
        try {
            categoryService.deleteCategory(id);
            ra.addFlashAttribute("successMessage",
                    "Category deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/orders")
    public String orders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        // Fetch all orders paginated
        var allOrders = orderRepository.findAll(
                org.springframework.data.domain.PageRequest.of(
                        page, size,
                        org.springframework.data.domain.Sort
                                .by("createdAt").descending()));

        model.addAttribute("orders",       allOrders);
        model.addAttribute("orderStatuses", OrderStatus.values());
        model.addAttribute("activePage",   "orders");
        return "admin/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus status,
            RedirectAttributes ra) {
        try {
            var req = new com.ozzz.skip.demo.dto.request
                    .UpdateOrderStatusRequest();
            req.setStatus(status);
            orderService.updateOrderStatus(id, req);
            ra.addFlashAttribute("successMessage",
                    "Order #" + id + " updated to " + status);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @GetMapping("/shipping")
    public String shipping(Model model) {

        model.addAttribute("pendingOrders",
                orderRepository.findByStatus(OrderStatus.PENDING));
        model.addAttribute("confirmedOrders",
                orderRepository.findByStatus(OrderStatus.CONFIRMED));
        model.addAttribute("shippedOrders",
                orderRepository.findByStatus(OrderStatus.SHIPPED));
        model.addAttribute("deliveredOrders",
                orderRepository.findByStatus(OrderStatus.DELIVERED));
        model.addAttribute("orderStatuses",
                OrderStatus.values());
        model.addAttribute("activePage", "shipping");
        return "admin/shipping";
    }
}