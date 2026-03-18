package com.money.kark_profit.service;

import com.money.kark_profit.cache.ConfigurationCache;
import com.money.kark_profit.constants.ApplicationCache;
import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.exception.DatabaseException;
import com.money.kark_profit.model.ConfigurationModel;
import com.money.kark_profit.model.UserProfileModel;
import com.money.kark_profit.repository.ConfigurationRepository;
import com.money.kark_profit.repository.TransactionRepository;
import com.money.kark_profit.repository.UserProfileRepository;
import com.money.kark_profit.transform.request.ChangePasswordRequest;
import com.money.kark_profit.transform.request.LoginRequest;
import com.money.kark_profit.transform.request.RegisterRequest;
import com.money.kark_profit.transform.response.AuthResponse;
import com.money.kark_profit.transform.response.ConfigurationResponse;
import com.money.kark_profit.transform.response.UserProfileResponse;
import com.money.kark_profit.utils.JwtUtils;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtils jwtUtils;
    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserService userService;
    private final ConfigurationRepository configurationRepository;

    public ResponseBuilderUtils<AuthResponse> register(RegisterRequest request) {

        if (userProfileRepository.findByUsername(request.getUsername()).isPresent())
            throw new RuntimeException("Username already exists");

        UserProfileModel user = new UserProfileModel();
        user.setUsername(request.getUsername());
        if(!Objects.isNull(request.getEmail()))
            user.setEmail(request.getEmail());
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
        UserProfileModel user;
        if(request.getUsername().contains("@")) {
            user = userProfileRepository.findByEmail(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        } else {
            user = userProfileRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new RuntimeException("Invalid password");

        if (!user.getStatus())
            throw new RuntimeException("User inactive");

        String token = jwtUtils.generateToken(user.getUsername());
        Date expiresAt = jwtUtils.getExpirationDate(token);


        AuthResponse authResponse = AuthResponse.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .token(token)
                .expiresAt(expiresAt)
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


    private boolean validatePermission(HttpServletRequest request) {
        int userId = userService.extractUserId(request);
        if(userId == -1)
            throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);

        UserProfileModel userProfileModel = userProfileRepository.findById(userId).get();
        if(userProfileModel == null)
            throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);

        if(!userProfileModel.getUsername().equals(ConfigurationCache.getByKeyName(ApplicationCache.MASTER_ADMIN_USERNAME).getValue()))
            throw new DatabaseException(ApplicationCode.DBE_998 ,ApplicationCode.DBE_998_MSG);

        return true;
    }

    public ResponseBuilderUtils<Page<UserProfileModel>> listing(RegisterRequest req, HttpServletRequest request) {
        if(validatePermission(request)) {
            int page = req.getPage() == null ? 0 : req.getPage();
            int size = req.getSize() == null ? 10 : req.getSize();
            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

            Specification<UserProfileModel> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (req.getUsername() != null)
                    predicates.add(cb.equal(root.get("username"), req.getUsername()));

                if (req.getEmail() != null)
                    predicates.add(cb.equal(root.get("email"), req.getEmail()));

                if (req.getStatus() != null)
                    predicates.add(cb.equal(root.get("status"), req.getStatus()));

                if (req.getCreatedAt() != null)
                    predicates.add(cb.equal(root.get("created_at"), req.getCreatedAt()));

                return cb.and(predicates.toArray(new Predicate[0]));
            };
            Page<UserProfileModel> pageResult = userProfileRepository.findAll(spec, pageable);

            UserProfileResponse transactionResponse = UserProfileResponse
                    .builder()
                    .totalElement(pageResult.getTotalElements())
                    .numberOfElement(pageResult.getNumberOfElements())
                    .size(pageResult.getSize())
                    .totalPage(pageResult.getTotalPages())
                    .content(pageResult.getContent()
                            .stream()
                            .peek(k -> k.setPassword("xxx"))
                            .toList())
                    .build();

            return new ResponseBuilderUtils<>(
                    ApplicationCode.HTTP_200,
                    ApplicationCode.FETCH,
                    transactionResponse
            );
        }
        throw new DatabaseException(ApplicationCode.DBE_998 ,ApplicationCode.DBE_998_MSG);
    }

    public ResponseBuilderUtils delete(RegisterRequest registerRequest, HttpServletRequest request) {
        if(validatePermission(request)) {
            userProfileRepository.deleteById(registerRequest.getId());
        }
        return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.DELETED, null);
    }
}