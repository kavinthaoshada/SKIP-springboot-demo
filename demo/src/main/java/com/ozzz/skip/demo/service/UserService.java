package com.ozzz.skip.demo.service;

import com.ozzz.skip.demo.dto.response.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse getUserById(Long id);

    UserResponse getUserByUsername(String username);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, String fullName, String address, String phoneNumber);

    void deactivateUser(Long id);
}