package com.ozzz.skip.demo.controller.web;

import com.ozzz.skip.demo.dto.response.UserResponse;
import com.ozzz.skip.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserWebController {

    private final UserService userService;

    @GetMapping("/profile")
    public String profile(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {

        UserResponse user = userService
                .getUserByUsername(userDetails.getUsername());
        model.addAttribute("user", user);

        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String fullName,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String phoneNumber,
            RedirectAttributes redirectAttributes) {

        try {
            UserResponse current = userService
                    .getUserByUsername(userDetails.getUsername());

            userService.updateUser(
                    current.getId(), fullName, address, phoneNumber);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Profile updated successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    e.getMessage());
        }

        return "redirect:/user/profile";
    }
}