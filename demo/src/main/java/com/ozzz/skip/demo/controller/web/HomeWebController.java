package com.ozzz.skip.demo.controller.web;

import com.ozzz.skip.demo.dto.response.CategoryResponse;
import com.ozzz.skip.demo.dto.response.PageResponse;
import com.ozzz.skip.demo.dto.response.ProductResponse;
import com.ozzz.skip.demo.service.CategoryService;
import com.ozzz.skip.demo.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeWebController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {

        PageResponse<ProductResponse> featured =
                productService.getAllActiveProducts(0, 8);

        List<CategoryResponse> categories =
                categoryService.getTopLevelCategories();

        model.addAttribute("featuredProducts", featured.getContent());
        model.addAttribute("navCategories", categories);
        model.addAttribute("currentPage", "home");

        return "index";
    }
}