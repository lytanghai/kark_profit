package com.money.kark_profit.service;

import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.exception.DatabaseException;
import com.money.kark_profit.exception.SystemException;
import com.money.kark_profit.model.TransactionModel;
import com.money.kark_profit.repository.TransactionRepository;
import com.money.kark_profit.transform.request.CommonRequest;
import com.money.kark_profit.transform.request.TransactionRequest;
import com.money.kark_profit.transform.response.TransactionListingResponse;
import com.money.kark_profit.transform.response.TransactionResponse;
import com.money.kark_profit.utils.DateUtils;
import com.money.kark_profit.utils.PayloadUtils;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Transactional
    public ResponseBuilderUtils<Void> insertNewPnL(TransactionRequest transactionRequest, HttpServletRequest httpServletRequest) {
        PayloadUtils.getNonNullFields(transactionRequest, List.of("type", "currency"));

        TransactionModel transactionModel = new TransactionModel();
        transactionModel.setCurrency(transactionRequest.getCurrency());
        transactionModel.setPnl(transactionRequest.getPnl());
        transactionModel.setSymbol(transactionRequest.getSymbol() == null ? "XAU" : transactionRequest.getSymbol());
        transactionModel.setLotSize(transactionRequest.getLotSize());
        transactionModel.setDate(DateUtils.formatPhnomPenhTime(new Date()));
        transactionModel.setType(transactionRequest.getType());

        if(transactionRequest.getInpDate() != null){
            transactionModel.setDate(DateUtils.parseDateWithCurrentTime(transactionRequest.getInpDate()));
        }

        Integer userId = userService.extractUserId(httpServletRequest);
        if(userId != null) {
            transactionModel.setUserId(userId);
        } else {
            throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);
        }

        transactionRepository.save(transactionModel);
        return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.CREATED, null);
    }

    @Transactional(readOnly = true)
    public ResponseBuilderUtils<Page<TransactionListingResponse>> listing(TransactionRequest req, HttpServletRequest request) {
        Integer userId = userService.extractUserId(request);
        if(userId == -1)
            throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);

        int page = req.getPage() == null ? 0 : req.getPage();
        int size = req.getSize() == null ? 10 : req.getSize();

        Pageable pageable = PageRequest.of(page, size, Sort.by("sn").descending());

        Specification<TransactionModel> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (userId != null)
                predicates.add(cb.equal(root.get("userId"), userId));

            if(req.getType() != null)
                predicates.add(cb.equal(root.get("type"), req.getType()));

            if (req.getCurrency() != null)
                predicates.add(cb.equal(root.get("currency"), req.getCurrency()));

            if (req.getSymbol() != null)
                predicates.add(cb.equal(root.get("symbol"), req.getSymbol()));

            if (req.getLotSize() != null)
                predicates.add(cb.equal(root.get("lotSize"), req.getLotSize()));

            if (req.getPnl() != null)
                predicates.add(cb.equal(root.get("pnl"), req.getPnl()));

            if (req.getDate() != null) {
                LocalDate localDate = req.getDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                LocalDateTime startOfDay = localDate.atStartOfDay();
                LocalDateTime endOfDay = localDate.atTime(LocalTime.MAX);

                predicates.add(cb.between(
                        root.get("date"),
                        java.sql.Timestamp.valueOf(startOfDay),
                        java.sql.Timestamp.valueOf(endOfDay)
                ));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<TransactionModel> pageResult = transactionRepository.findAll(spec, pageable);

        TransactionResponse transactionResponse = TransactionResponse.builder()
                .totalElement(pageResult.getTotalElements())
                .numberOfElement(pageResult.getNumberOfElements())
                .size(pageResult.getSize())
                .totalPage(pageResult.getTotalPages())
                .content(pageResult.getContent().stream()
                        .map(m -> {
                            return TransactionListingResponse.builder()
                                    .sn(m.getSn())
                                    .currency(m.getCurrency())
                                    .date(m.getDate())
                                    .lotSize(m.getLotSize())
                                    .pnl(m.getPnl()) // transformed value
                                    .symbol(m.getSymbol())
                                    .type(m.getType())
                                    .userId(m.getUserId())
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .build();

        return new ResponseBuilderUtils<>(
                ApplicationCode.HTTP_200,
                ApplicationCode.FETCH,
                transactionResponse
        );
    }

    @Transactional(rollbackFor = {DatabaseException.class, SystemException.class})
    public ResponseBuilderUtils<Void> deletePnL(TransactionRequest transactionRequest, HttpServletRequest request) {
        Integer userId = userService.extractUserId(request);

        if(userId == -1)
            throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);

        PayloadUtils.getNonNullFields(transactionRequest, List.of("sn"));
        try {
            TransactionModel transactionModel = transactionRepository.findBySnAndUserId(transactionRequest.getSn(), userId);
            if (transactionModel == null)
                throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);

            transactionRepository.deleteBySn(transactionRequest.getSn());
            return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.DELETED, null);
        } catch (DataAccessException e) {
            throw new DatabaseException("Failed to delete pnl record");
        } catch (Exception e) {
            throw new SystemException("Unexpected error occurred");
        }
    }

    @Transactional
    public ResponseBuilderUtils<Void> updatePnL(TransactionRequest transactionRequest, HttpServletRequest request) {
        if(transactionRequest == null)
            return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.UPDATED, null);

        Integer userId = userService.extractUserId(request);
        if(userId == -1)
            throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);

        try {
            TransactionModel transactionModel = transactionRepository.findBySnAndUserId(transactionRequest.getSn(), userId);
            if (transactionModel == null)
                throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);

            if(transactionRequest.getPnl() != null)
                transactionModel.setPnl(transactionRequest.getPnl());

            if(transactionRequest.getLotSize() != null)
                transactionModel.setLotSize(transactionRequest.getLotSize());

            if(transactionRequest.getCurrency() != null)
                transactionModel.setCurrency(transactionRequest.getCurrency());

            if(transactionRequest.getSymbol() != null)
                transactionModel.setSymbol(transactionRequest.getSymbol());

            if(transactionRequest.getType() != null)
                transactionModel.setSymbol(transactionRequest.getType());

            transactionRepository.save(transactionModel);

        } catch (DataAccessException e) {
            throw new DatabaseException("Failed to update pnl record");
        } catch (Exception e) {
            throw new SystemException("Unexpected error occurred");
        }

        return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.UPDATED, null);
    }

    @Transactional
    public ResponseBuilderUtils<Void>  mergeTransaction(HttpServletRequest request, CommonRequest commonRequest) throws ParseException {

        Integer userId = userService.extractUserId(request);
        if(userId == -1)
            throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);

        Date[] dateRange = DateUtils.getFullDay(commonRequest.getDate());

        List<TransactionModel> transactions = transactionRepository
                .findByUserIdAndDateBetween(userId, dateRange[0], dateRange[1]);

        Map<String, List<TransactionModel>> grouped = transactions.stream()
                .collect(Collectors.groupingBy(t ->
                        (t.getSymbol() == null ? "" : t.getSymbol()) + "|" +
                                (t.getCurrency() == null ? "" : t.getCurrency()) + "|" +
                                (t.getType() == null ? "" : t.getType())
                ));

        List<TransactionModel> aggregatedList = new ArrayList<>();

        for (Map.Entry<String, List<TransactionModel>> entry : grouped.entrySet()) {
            List<TransactionModel> group = entry.getValue();
            TransactionModel base = group.get(0);
            TransactionModel newTransaction = new TransactionModel();
            newTransaction.setUserId(base.getUserId());
            newTransaction.setSymbol(base.getSymbol());
            newTransaction.setCurrency(base.getCurrency());
            newTransaction.setType(base.getType());
            newTransaction.setDate(new Date());
            newTransaction.setPnl(group.stream()
                    .mapToDouble(t -> t.getPnl() == null ? 0 : t.getPnl())
                    .sum());
            newTransaction.setLotSize(group.stream()
                    .mapToDouble(t -> t.getLotSize() == null ? 0 : t.getLotSize())
                    .sum());

            aggregatedList.add(newTransaction);
        }

        try {
            transactionRepository.saveAll(aggregatedList);
            transactionRepository.deleteBySnIn(transactions.stream()
                    .map(TransactionModel::getSn)
                    .filter(Objects::nonNull)
                    .toList());
        } catch (Exception e) {
            throw new DatabaseException(ApplicationCode.DBE_ERR_001 ,ApplicationCode.DBE_ERR_001_MSG);
        }
        return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.MERGE, null);
    }
}
