package com.example.smartmart.service;

import com.example.smartmart.dto.request.LoginRequest;
import com.example.smartmart.dto.request.RegisterRequest;
import com.example.smartmart.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
