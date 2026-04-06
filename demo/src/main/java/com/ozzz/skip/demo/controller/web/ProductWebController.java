package com.ozzz.skip.demo.controller.web;

import com.ozzz.skip.demo.dto.request.ProductRequest;
import com.ozzz.skip.demo.dto.response.CategoryResponse;
import com.ozzz.skip.demo.dto.response.PageResponse;
import com.ozzz.skip.demo.dto.response.ProductResponse;
import com.ozzz.skip.demo.service.CategoryService;
import com.ozzz.skip.demo.service.ProductService;
import com.ozzz.skip.demo.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductWebController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping
    public String listProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        PageResponse<ProductResponse> products;

        boolean hasFilters = keyword != null || categoryId != null
                || minPrice != null || maxPrice != null;

        if (hasFilters) {
            products = productService.filterProducts(
                    keyword, categoryId, minPrice, maxPrice, page, size);
        } else {
            products = productService.getAllActiveProducts(page, size);
        }

        List<CategoryResponse> categories = categoryService.getAllCategories();

        model.addAttribute("products",    products);
        model.addAttribute("categories",  categories);
        model.addAttribute("keyword",     keyword);
        model.addAttribute("categoryId",  categoryId);
        model.addAttribute("minPrice",    minPrice);
        model.addAttribute("maxPrice",    maxPrice);
        model.addAttribute("currentPage", "all");

        return "products/list";
    }

    @GetMapping("/search")
    public String searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        PageResponse<ProductResponse> products =
                productService.searchProducts(keyword, page, size);

        model.addAttribute("products",   products);
        model.addAttribute("keyword",    keyword);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "products/list";
    }

    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {

        ProductResponse product = productService.getProductById(id);
        model.addAttribute("product", product);

        return "products/detail";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    public String newProductForm(Model model) {

        model.addAttribute("productRequest", new ProductRequest());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("editMode", false);

        return "products/form";
    }

    @PostMapping(value = "/new",
            consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    public String createProduct(
            @Valid @ModelAttribute("productRequest") ProductRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("editMode", false);
            return "products/form";
        }

        try {
            Long sellerId = userService
                    .getUserByUsername(userDetails.getUsername()).getId();
            ProductResponse created =
                    productService.createProduct(request, sellerId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Product \"" + created.getName()
                            + "\" listed successfully!");

            return "redirect:/products/" + created.getId();

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("editMode", false);
            return "products/form";
        }
    }

    @PostMapping(value = "/{id}/edit",
            consumes = "multipart/form-data")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute("productRequest") ProductRequest request,
            BindingResult bindingResult,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("editMode", true);
            return "products/form";
        }

        try {
            Long sellerId = userService
                    .getUserByUsername(userDetails.getUsername()).getId();
            productService.updateProduct(id, request, sellerId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Product updated successfully!");

            return "redirect:/products/" + id;

        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories",
                    categoryService.getAllCategories());
            model.addAttribute("editMode", true);
            return "products/form";
        }
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    public String editProductForm(@PathVariable Long id, Model model) {

        ProductResponse product = productService.getProductById(id);

        ProductRequest request = new ProductRequest();
        request.setName(product.getName());
        request.setDescription(product.getDescription());
        request.setPrice(product.getPrice());
        request.setStockQuantity(product.getStockQuantity());
        request.setImageUrl(product.getImageUrl());
        request.setCategoryId(product.getCategoryId());

        model.addAttribute("productRequest", request);
        model.addAttribute("product",        product);
        model.addAttribute("categories",     categoryService.getAllCategories());
        model.addAttribute("editMode",       true);

        return "products/form";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    public String deleteProduct(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        try {
            Long sellerId = userService
                    .getUserByUsername(userDetails.getUsername()).getId();
            productService.deleteProduct(id, sellerId);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Product deleted successfully.");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/products/my-listings";
    }

    @GetMapping("/my-listings")
    @PreAuthorize("hasAuthority('ROLE_SELLER')")
    public String myListings(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        Long sellerId = userService
                .getUserByUsername(userDetails.getUsername()).getId();
        List<ProductResponse> products =
                productService.getProductsBySeller(sellerId);

        model.addAttribute("products", products);

        return "products/my-listings";
    }
}