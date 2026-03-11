package com.money.kark_profit.service;

import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.exception.DatabaseException;
import com.money.kark_profit.exception.SystemException;
import com.money.kark_profit.model.TransactionModel;
import com.money.kark_profit.model.UserProfileModel;
import com.money.kark_profit.repository.TransactionRepository;
import com.money.kark_profit.repository.UserProfileRepository;
import com.money.kark_profit.transform.request.TransactionRequest;
import com.money.kark_profit.transform.response.TransactionResponse;
import com.money.kark_profit.utils.JwtUtils;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import com.money.kark_profit.utils.DateUtils;
import com.money.kark_profit.utils.PayloadUtils;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequiredArgsConstructor
public class TransactionService {

    private final JwtUtils jwtUtils;
    private final TransactionRepository transactionRepository;
    private final UserProfileRepository  userProfileRepository;

    private Integer extractUserId(HttpServletRequest httpServletRequest) {
        String username = jwtUtils.extractUsername(httpServletRequest.getHeader("Authorization"));
        if(username != null || !username.isBlank()) {
            UserProfileModel userProfileModel = userProfileRepository.findByUsername(username).get();
            if(userProfileModel != null) {
                return userProfileModel.getId();
            }
        }
        return null;
    }

    public ResponseBuilderUtils<Void> insertNewPnL(TransactionRequest transactionRequest, HttpServletRequest httpServletRequest) {
        PayloadUtils.getNonNullFields(transactionRequest, List.of("symbol", "lot_size", "pnl", "currency"));

        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setCurrency(transactionRequest.getCurrency());
        transactionModel.setPnl(transactionRequest.getPnl());
        transactionModel.setSymbol(transactionRequest.getSymbol());
        transactionModel.setLotSize(transactionRequest.getLotSize());
        transactionModel.setDate(DateUtils.formatPhnomPenhTime(new Date()));
        transactionModel.setType(transactionRequest.getType());

        Integer userId = extractUserId(httpServletRequest);
        if(userId != null) {
            transactionModel.setUserId(userId);
        } else {
            throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);
        }

        transactionRepository.save(transactionModel);

        return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.CREATED, null);
    }

    public ResponseBuilderUtils<Page<TransactionModel>> listing(TransactionRequest req) {
        int page = req.getPage() == null ? 0 : req.getPage();
        int size = req.getSize() == null ? 10 : req.getSize();

        Pageable pageable = PageRequest.of(page, size, Sort.by("sn").descending());

        Specification<TransactionModel> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (req.getCurrency() != null)
                predicates.add(cb.equal(root.get("currency"), req.getCurrency()));

            if (req.getSymbol() != null)
                predicates.add(cb.equal(root.get("symbol"), req.getSymbol()));

            if (req.getLotSize() != null)
                predicates.add(cb.equal(root.get("lot_size"), req.getLotSize()));

            if (req.getPnl() != null)
                predicates.add(cb.equal(root.get("pnl"), req.getPnl()));

            if (req.getDate() != null)
                predicates.add(cb.equal(root.get("date"), req.getDate()));

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TransactionModel> pageResult = transactionRepository.findAll(spec, pageable);

        TransactionResponse transactionResponse = TransactionResponse
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

    public ResponseBuilderUtils<Void> deletePnL(TransactionRequest transactionRequest) {
        PayloadUtils.getNonNullFields(transactionRequest, List.of("sn"));
        try {
            TransactionModel transactionModel = transactionRepository.findBySn(transactionRequest.getSn());
            if (transactionModel == null) {
                throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);
            }
            transactionRepository.deleteById(transactionRequest.getSn());
            return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.DELETED, null);
        } catch (DataAccessException e) {
            throw new DatabaseException("Failed to delete pnl record");
        } catch (Exception e) {
            throw new SystemException("Unexpected error occurred");
        }
    }

    public ResponseBuilderUtils<Void> updatePnL(TransactionRequest transactionRequest) {
        if(transactionRequest == null)
            return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.UPDATED, null);

        try {
            TransactionModel transactionModel = transactionRepository.findBySn(transactionRequest.getSn());
            if (transactionModel == null) {
                throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);
            }

            if(transactionRequest.getPnl() != null)
                transactionModel.setPnl(transactionRequest.getPnl());

            if(transactionRequest.getLotSize() != null)
                transactionModel.setLotSize(transactionRequest.getLotSize());

            if(transactionRequest.getCurrency() != null)
                transactionModel.setCurrency(transactionRequest.getCurrency());

            if(transactionRequest.getSymbol() != null)
                transactionModel.setSymbol(transactionRequest.getSymbol());

            transactionRepository.save(transactionModel);

        } catch (DataAccessException e) {
            throw new DatabaseException("Failed to update pnl record");
        } catch (Exception e) {
            throw new SystemException("Unexpected error occurred");
        }

        return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.UPDATED, null);
    }
}
