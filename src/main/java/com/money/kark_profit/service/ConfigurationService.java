package com.money.kark_profit.service;

import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.exception.DatabaseException;
import com.money.kark_profit.exception.SystemException;
import com.money.kark_profit.model.Configuration;
import com.money.kark_profit.model.ProfitLossModel;
import com.money.kark_profit.repository.ConfigRepository;
import com.money.kark_profit.transform.request.ConfigurationRequest;
import com.money.kark_profit.transform.request.ProfitLossReq;
import com.money.kark_profit.transform.response.ConfigurationResponse;
import com.money.kark_profit.transform.response.ProfitLossResponse;
import com.money.kark_profit.utils.PayloadUtils;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import jakarta.persistence.criteria.Predicate;
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

@Service
@AllArgsConstructor
public class ConfigurationService {

    private final ConfigRepository configRepository;

    public ResponseBuilderUtils<Void> createConfig(ConfigurationRequest configReq) {
        PayloadUtils.getNonNullFields(configReq, List.of("name", "value"));

        if(configRepository.findByName(configReq.getName()).isPresent())
            throw new DatabaseException(ApplicationCode.DBE_03122, configReq.getName() + " Already exists");

        Configuration configuration = new Configuration();
        configuration.setStatus(true);
        configuration.setCreatedAt(new Date());
        configuration.setName(configReq.getName());
        configuration.setValue(configReq.getValue());
        configRepository.save(configuration);

        return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.CREATED, null);
    }

    public ResponseBuilderUtils<Page<Configuration>> listingConfig(ConfigurationRequest req) {
        int page = req.getPage() == null ? 0 : req.getPage();
        int size = req.getSize() == null ? 10 : req.getSize();

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Specification<Configuration> spec = (root, query, cb) -> {
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

        Page<Configuration> pageResult = configRepository.findAll(spec, pageable);

        ConfigurationResponse profitLossResponse = ConfigurationResponse
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
                profitLossResponse
        );
    }

    public ResponseBuilderUtils<Void> deleteConfiguration(ConfigurationRequest configurationRequest) {
        PayloadUtils.getNonNullFields(configurationRequest, List.of("sn"));
        try {
            Configuration configuration = configRepository.findById(configurationRequest.getId()).get();
            if (configuration == null)
                throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);

            configRepository.deleteById(configurationRequest.getId());
            return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.DELETED, null);
        } catch (DataAccessException e) {
            throw new DatabaseException("Failed to delete configuration record");
        } catch (Exception e) {
            throw new SystemException("Unexpected error occurred");
        }
    }

    public ResponseBuilderUtils<Void> updateConfiguration(ConfigurationRequest configurationRequest) {
        if(configurationRequest == null)
            return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.UPDATED, null);

        try {
            Configuration profitLossModel = configRepository.findById(configurationRequest.getId()).get();
            if (profitLossModel == null)
                throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);

            if(configurationRequest.getName() != null)
                profitLossModel.setName(configurationRequest.getName());

            if(configurationRequest.getValue() != null)
                profitLossModel.setValue(configurationRequest.getValue());

            if(configurationRequest.getStatus() != null)
                profitLossModel.setStatus(configurationRequest.getStatus());

            configRepository.save(profitLossModel);

        } catch (DataAccessException e) {
            throw new DatabaseException("Failed to update configuration record");
        } catch (Exception e) {
            throw new SystemException("Unexpected error occurred");
        }

        return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.UPDATED, null);
    }
}