package com.money.kark_profit.service;

import com.money.kark_profit.cache.ConfigurationCache;
import com.money.kark_profit.constants.ApplicationCache;
import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.exception.DatabaseException;
import com.money.kark_profit.exception.SystemException;
import com.money.kark_profit.model.ConfigurationModel;
import com.money.kark_profit.model.UserProfileModel;
import com.money.kark_profit.repository.ConfigurationRepository;
import com.money.kark_profit.repository.UserProfileRepository;
import com.money.kark_profit.transform.request.ConfigurationRequest;
import com.money.kark_profit.transform.response.ConfigurationResponse;
import com.money.kark_profit.utils.JwtUtils;
import com.money.kark_profit.utils.PayloadUtils;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**FOR MASTER USER ONLY***/
@Service
@AllArgsConstructor
public class ConfigurationService {

    private final JwtUtils jwtUtils;
    private final ConfigurationRepository configurationRepository;
    private final UserProfileRepository userProfileRepository;

    private Integer extractUserId(HttpServletRequest httpServletRequest) {
        String username = jwtUtils.extractUsername(httpServletRequest.getHeader("Authorization"));
        if(username != null || !username.isBlank()) {
            UserProfileModel userProfileModel = userProfileRepository.findByUsername(username).get();
            if(userProfileModel != null)
                return userProfileModel.getId();
        }
        return -1;
    }

    private boolean validatePermission(HttpServletRequest request) {
        int userId = extractUserId(request);
        if(userId == -1) {
            throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);
        }
        UserProfileModel userProfileModel = userProfileRepository.findById(userId).get();
        if(userProfileModel == null) {
            throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);
        }
        if(!userProfileModel.getUsername().equals(ConfigurationCache.getByKeyName(ApplicationCache.MASTER_ADMIN_USERNAME).getValue()))
            throw new DatabaseException(ApplicationCode.DBE_998 ,ApplicationCode.DBE_998_MSG);

        return true;
    }

    public ResponseBuilderUtils<Void> createConfig(ConfigurationRequest configReq, HttpServletRequest request) {
        if(validatePermission(request)){
            PayloadUtils.getNonNullFields(configReq, List.of("name", "value"));

            if(configurationRepository.findByName(configReq.getName()).isPresent())
                throw new DatabaseException(ApplicationCode.DBE_03122, configReq.getName() + " Already exists");

            ConfigurationModel configurationModel = new ConfigurationModel();
            configurationModel.setStatus(true);
            configurationModel.setCreatedAt(new Date());
            configurationModel.setName(configReq.getName());
            configurationModel.setValue(configReq.getValue());
            configurationRepository.save(configurationModel);

            return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.CREATED, null);
        }
        throw new DatabaseException(ApplicationCode.DBE_998 ,ApplicationCode.DBE_998_MSG);
    }

    public ResponseBuilderUtils<Page<ConfigurationModel>> listingConfig(ConfigurationRequest req, HttpServletRequest request) {
        if(validatePermission(request)) {
            int page = req.getPage() == null ? 0 : req.getPage();
            int size = req.getSize() == null ? 10 : req.getSize();

            Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

            Specification<ConfigurationModel> spec = (root, query, cb) -> {
                List<Predicate> predicates = new ArrayList<>();
                if (req.getName() != null)
                    predicates.add(cb.equal(root.get("name"), req.getName()));

                if (req.getValue() != null)
                    predicates.add(cb.equal(root.get("value"), req.getValue()));

                if (req.getStatus() != null)
                    predicates.add(cb.equal(root.get("status"), req.getStatus()));

                if (req.getCreatedAt() != null)
                    predicates.add(cb.equal(root.get("created_at"), req.getCreatedAt()));

                return cb.and(predicates.toArray(new Predicate[0]));
            };

            Page<ConfigurationModel> pageResult = configurationRepository.findAll(spec, pageable);

            ConfigurationResponse transactionResponse = ConfigurationResponse
                    .builder()
                    .totalElement(pageResult.getTotalElements())
                    .numberOfElement(pageResult.getNumberOfElements())
                    .size(pageResult.getSize())
                    .totalPage(pageResult.getTotalPages())
                    .content(pageResult.getContent())
                    .build();

            return new ResponseBuilderUtils<>(
                    ApplicationCode.HTTP_200,
                    ApplicationCode.FETCH,
                    transactionResponse
            );
        }
        throw new DatabaseException(ApplicationCode.DBE_998 ,ApplicationCode.DBE_998_MSG);
    }

    public ResponseBuilderUtils<Void> deleteConfiguration(ConfigurationRequest configurationRequest, HttpServletRequest request) {
        if(validatePermission(request)) {
            PayloadUtils.getNonNullFields(configurationRequest, List.of("sn"));
            try {
                ConfigurationModel configurationModel = configurationRepository.findById(configurationRequest.getId()).get();
                if (configurationModel == null)
                    throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);

                configurationRepository.deleteById(configurationRequest.getId());
                return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.DELETED, null);
            } catch (DataAccessException e) {
                throw new DatabaseException("Failed to delete configuration record");
            } catch (Exception e) {
                throw new SystemException("Unexpected error occurred");
            }
        }
        throw new DatabaseException(ApplicationCode.DBE_998 ,ApplicationCode.DBE_998_MSG);
    }

    public ResponseBuilderUtils<Void> updateConfiguration(ConfigurationRequest configurationRequest, HttpServletRequest request) {
        if(configurationRequest == null)
            return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.UPDATED, null);

        if(validatePermission(request)) {
            try {
                ConfigurationModel transactionModel = configurationRepository.findById(configurationRequest.getId()).get();
                if (transactionModel == null)
                    throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);

                if(configurationRequest.getName() != null)
                    transactionModel.setName(configurationRequest.getName());

                if(configurationRequest.getValue() != null)
                    transactionModel.setValue(configurationRequest.getValue());

                if(configurationRequest.getStatus() != null)
                    transactionModel.setStatus(configurationRequest.getStatus());

                configurationRepository.save(transactionModel);

            } catch (DataAccessException e) {
                throw new DatabaseException("Failed to update configuration record");
            } catch (Exception e) {
                throw new SystemException("Unexpected error occurred");
            }

            return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.UPDATED, null);
        }
        throw new DatabaseException(ApplicationCode.DBE_998 ,ApplicationCode.DBE_998_MSG);
    }
}