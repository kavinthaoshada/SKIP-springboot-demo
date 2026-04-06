package com.ozzz.skip.demo.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Slf4j
@Component
public class CustomAuthenticationSuccessHandler
        implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {

        Collection<? extends GrantedAuthority> authorities =
                authentication.getAuthorities();

        String redirectUrl = determineTargetUrl(authorities);

        log.info("User '{}' logged in → redirecting to {}",
                authentication.getName(), redirectUrl);

        response.sendRedirect(request.getContextPath() + redirectUrl);
    }

    private String determineTargetUrl(
            Collection<? extends GrantedAuthority> authorities) {

        for (GrantedAuthority authority : authorities) {
            switch (authority.getAuthority()) {
                case "ROLE_ADMIN"  -> { return "/admin/dashboard"; }
                case "ROLE_SELLER" -> { return "/products/my-listings"; }
                case "ROLE_BUYER"  -> { return "/"; }
            }
        }

        return "/";
    }
}