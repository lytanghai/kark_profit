package com.money.kark_profit.service;

import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.model.UserProfileModel;
import com.money.kark_profit.repository.ConfigurationRepository;
import com.money.kark_profit.repository.TransactionRepository;
import com.money.kark_profit.repository.UserProfileRepository;
import com.money.kark_profit.transform.request.ChangePasswordRequest;
import com.money.kark_profit.transform.request.LoginRequest;
import com.money.kark_profit.transform.request.RegisterRequest;
import com.money.kark_profit.transform.response.AuthResponse;
import com.money.kark_profit.utils.JwtUtils;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtils jwtUtils;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponseBuilderUtils<AuthResponse> register(RegisterRequest request) {

        if (userProfileRepository.findByUsername(request.getUsername()).isPresent())
            throw new RuntimeException("Username already exists");

        UserProfileModel user = new UserProfileModel();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(true);
        user.setCreatedAt(new Date());
        user.setLastUpdatedAt(new Date());

        userProfileRepository.save(user);

        jwtUtils.generateToken(user.getUsername());

        AuthResponse authResponse = AuthResponse.builder()
                .username(request.getUsername())
                .token(jwtUtils.generateToken(user.getUsername()))
                .build();

        return new ResponseBuilderUtils<>(
                ApplicationCode.HTTP_200,
                ApplicationCode.REGISTERED,
                authResponse
        );
    }

    public ResponseBuilderUtils<AuthResponse> login(LoginRequest request) {

        UserProfileModel user = userProfileRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid password");

        if (!user.getStatus())
            throw new RuntimeException("User inactive");

        AuthResponse authResponse = AuthResponse.builder()
                .username(request.getUsername())
                .token(jwtUtils.generateToken(user.getUsername()))
                .build();

        return new ResponseBuilderUtils<>(
                ApplicationCode.HTTP_200,
                ApplicationCode.LOGIN,
                authResponse
        );
    }

    public ResponseBuilderUtils<Void> changePassword(ChangePasswordRequest request) {
        UserProfileModel user = userProfileRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLastUpdatedAt(new Date());

        userProfileRepository.save(user);

        return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.MODIFY_USER, null);
    }
}