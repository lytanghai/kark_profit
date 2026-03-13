package com.money.kark_profit.service;

import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.exception.DatabaseException;
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
            UserProfileModel userProfileModel = userProfileRepository.findByUsername(username).orElse(null);
            if(userProfileModel == null) {
                throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);
            } else {
                return userProfileModel.getId();
            }
        }
        return -1;
    }

}
