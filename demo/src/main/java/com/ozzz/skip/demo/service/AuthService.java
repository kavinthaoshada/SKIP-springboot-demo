package com.ozzz.skip.demo.service;

import com.ozzz.skip.demo.dto.request.LoginRequest;
import com.ozzz.skip.demo.dto.request.RegisterRequest;
import com.ozzz.skip.demo.dto.response.AuthResponse;
import com.ozzz.skip.demo.dto.response.UserResponse;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}