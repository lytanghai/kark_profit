package com.money.kark_profit.service;

import com.money.kark_profit.model.UserProfileModel;
import com.money.kark_profit.repository.UserProfileRepository;
import com.money.kark_profit.utils.JwtUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final JwtUtils jwtUtils;
    private final UserProfileRepository userProfileRepository;

    public Integer extractUserId(HttpServletRequest httpServletRequest) {
        String username = jwtUtils.extractUsername(httpServletRequest.getHeader("Authorization"));
        if(username != null || !username.isBlank()) {
            UserProfileModel userProfileModel = userProfileRepository.findByUsername(username).get();
            if(userProfileModel != null)
                return userProfileModel.getId();
        }
        return -1;
    }

}
