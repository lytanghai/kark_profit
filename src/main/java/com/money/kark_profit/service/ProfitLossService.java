package com.money.kark_profit.service;

import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.exception.DatabaseException;
import com.money.kark_profit.exception.SystemException;
import com.money.kark_profit.model.ProfitLossModel;
import com.money.kark_profit.repository.ProfitLossRepository;
import com.money.kark_profit.transform.request.ProfitLossReq;
import com.money.kark_profit.transform.response.ProfitLossResponse;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import com.money.kark_profit.utils.DateUtils;
import com.money.kark_profit.utils.PayloadUtils;
import jakarta.persistence.criteria.Predicate;
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
public class ProfitLossService {

    private final ProfitLossRepository  profitLossRepository;

    public ResponseBuilderUtils<Void> insertNewPnL(ProfitLossReq profitLossReq) {
        PayloadUtils.getNonNullFields(profitLossReq, List.of("symbol", "lot_size", "pnl", "currency"));

        ProfitLossModel profitLossModel = new ProfitLossModel();
        profitLossModel.setCurrency(profitLossReq.getCurrency());
        profitLossModel.setPnl(profitLossReq.getPnl());
        profitLossModel.setSymbol(profitLossReq.getSymbol());
        profitLossModel.setLotSize(profitLossReq.getLotSize());
        profitLossModel.setDate(DateUtils.formatPhnomPenhTime(new Date()));

        profitLossRepository.save(profitLossModel);

        return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.CREATED, null);
    }

    public ResponseBuilderUtils<Page<ProfitLossModel>> listing(ProfitLossReq req) {
        int page = req.getPage() == null ? 0 : req.getPage();
        int size = req.getSize() == null ? 10 : req.getSize();

        Pageable pageable = PageRequest.of(page, size, Sort.by("sn").descending());

        Specification<ProfitLossModel> spec = (root, query, cb) -> {
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

        Page<ProfitLossModel> pageResult = profitLossRepository.findAll(spec, pageable);

        ProfitLossResponse profitLossResponse = ProfitLossResponse
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

    public ResponseBuilderUtils<Void> deletePnL(ProfitLossReq profitLossReq) {
        PayloadUtils.getNonNullFields(profitLossReq, List.of("sn"));
        try {
            ProfitLossModel profitLossModel = profitLossRepository.findBySn(profitLossReq.getSn());
            if (profitLossModel == null) {
                throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);
            }
            profitLossRepository.deleteById(profitLossReq.getSn());
            return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.DELETED, null);
        } catch (DataAccessException e) {
            throw new DatabaseException("Failed to delete pnl record");
        } catch (Exception e) {
            throw new SystemException("Unexpected error occurred");
        }
    }

    public ResponseBuilderUtils<Void> updatePnL(ProfitLossReq profitLossReq) {
        if(profitLossReq == null)
            return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.UPDATED, null);

        try {
            ProfitLossModel profitLossModel = profitLossRepository.findBySn(profitLossReq.getSn());
            if (profitLossModel == null) {
                throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);
            }

            if(profitLossReq.getPnl() != null)
                profitLossModel.setPnl(profitLossReq.getPnl());

            if(profitLossReq.getLotSize() != null)
                profitLossModel.setLotSize(profitLossReq.getLotSize());

            if(profitLossReq.getCurrency() != null)
                profitLossModel.setCurrency(profitLossReq.getCurrency());

            if(profitLossReq.getSymbol() != null)
                profitLossModel.setSymbol(profitLossReq.getSymbol());

            profitLossRepository.save(profitLossModel);

        } catch (DataAccessException e) {
            throw new DatabaseException("Failed to update pnl record");
        } catch (Exception e) {
            throw new SystemException("Unexpected error occurred");
        }

        return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.UPDATED, null);
    }
}
