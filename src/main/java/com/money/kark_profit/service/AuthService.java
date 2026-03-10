package com.money.kark_profit.service;

import com.money.kark_profit.model.UserModel;
import com.money.kark_profit.repository.UserRepository;
import com.money.kark_profit.transform.request.ChangePasswordRequest;
import com.money.kark_profit.transform.request.LoginRequest;
import com.money.kark_profit.transform.request.RegisterRequest;
import com.money.kark_profit.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public String register(RegisterRequest request) {

        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }

        UserModel user = new UserModel();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setStatus(true);
        user.setCreatedAt(new Date());
        user.setLastUpdatedAt(new Date());

        userRepository.save(user);

        return jwtUtils.generateToken(user.getUsername());
    }

    public String login(LoginRequest request) {

        UserModel user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        if (!user.getStatus()) {
            throw new RuntimeException("User inactive");
        }

        return jwtUtils.generateToken(user.getUsername());
    }

    public void changePassword(ChangePasswordRequest request) {

        UserModel user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Old password incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setLastUpdatedAt(new Date());

        userRepository.save(user);
    }
}