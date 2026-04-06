package com.ozzz.skip.demo.controller.web;

import com.ozzz.skip.demo.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class BaseWebController {

    private final CategoryService categoryService;

    @ModelAttribute
    public void addGlobalAttributes(Model model) {
        try {
            model.addAttribute("navCategories",
                    categoryService.getTopLevelCategories());
        } catch (Exception e) {
            // Silently fail — DB might not be ready during startup
        }
    }
}