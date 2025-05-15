package com.example.service;


import com.example.dto.SignUpRequest;
import com.example.dto.UserResponse;

public interface UserService {

    UserResponse registerUser(SignUpRequest request);

    UserResponse login(String token);
}
