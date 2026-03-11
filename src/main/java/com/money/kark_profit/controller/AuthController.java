package com.money.kark_profit.controller;

import com.money.kark_profit.service.AuthService;
import com.money.kark_profit.transform.request.ChangePasswordRequest;
import com.money.kark_profit.transform.request.LoginRequest;
import com.money.kark_profit.transform.request.RegisterRequest;
import com.money.kark_profit.transform.response.AuthResponse;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ResponseBuilderUtils<AuthResponse>> register(@RequestBody RegisterRequest request) {
        return new ResponseEntity<>(authService.register(request), HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<ResponseBuilderUtils<AuthResponse>> login(@RequestBody LoginRequest request) {
        return new ResponseEntity<>(authService.login(request), HttpStatus.OK);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ResponseBuilderUtils> changePassword(@RequestBody ChangePasswordRequest request) {
        return new ResponseEntity<>(authService.changePassword(request), HttpStatus.OK);
    }
}