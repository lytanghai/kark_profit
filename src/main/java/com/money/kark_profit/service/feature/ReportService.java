package com.money.kark_profit.service.feature;

import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.exception.DatabaseException;
import com.money.kark_profit.model.TransactionModel;
import com.money.kark_profit.model.UserProfileModel;
import com.money.kark_profit.repository.TransactionRepository;
import com.money.kark_profit.repository.UserProfileRepository;
import com.money.kark_profit.service.UserService;
import com.money.kark_profit.transform.request.ReportRequest;
import com.money.kark_profit.transform.response.ReportResponse;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserService userService;

    public ResponseBuilderUtils<List<ReportResponse>> generateReport(
            ReportRequest reportRequest,
            HttpServletRequest httpServletRequest
    ) {
        Integer userId = userService.extractUserId(httpServletRequest);

        // Verify user
        UserProfileModel userProfile = userProfileRepository.findById(userId).orElse(null);
        if (userProfile == null) {
            throw new DatabaseException(ApplicationCode.DBE_001, ApplicationCode.DBE_001_MSG);
        }

        // Fetch transactions for last N days
        List<TransactionModel> transactions = getTransactionsLastNDays(userId, reportRequest.getLastNDay());

        // Group transactions by symbol
        Map<String, List<TransactionModel>> transactionsBySymbol = transactions.stream()
                .collect(Collectors.groupingBy(TransactionModel::getSymbol));

        List<ReportResponse> reports = new ArrayList<>();

        for (Map.Entry<String, List<TransactionModel>> entry : transactionsBySymbol.entrySet()) {
            String symbol = entry.getKey();
            List<TransactionModel> symbolTxns = entry.getValue();

            BigDecimal totalProfit = BigDecimal.ZERO;
            BigDecimal totalLoss = BigDecimal.ZERO;
            BigDecimal netProfit = BigDecimal.ZERO;
            BigDecimal totalDeposit = BigDecimal.ZERO;
            BigDecimal totalWithdrawal = BigDecimal.ZERO;

            Map<LocalDate, BigDecimal> dailyPnL = new HashMap<>();

            for (TransactionModel t : symbolTxns) {
                BigDecimal pnl = Optional.ofNullable(t.getPnl()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO);

                if ("LOSS".equalsIgnoreCase(t.getType())) {
                    totalLoss = totalLoss.add(pnl);
                    pnl = pnl.negate(); // negative for daily PnL
                } else if ("DEPOSIT".equalsIgnoreCase(t.getType())) {
                    totalDeposit = totalDeposit.add(Optional.ofNullable(t.getLotSize()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO));
                } else if ("WITHDRAWAL".equalsIgnoreCase(t.getType())) {
                    totalWithdrawal = totalWithdrawal.add(Optional.ofNullable(t.getLotSize()).map(BigDecimal::valueOf).orElse(BigDecimal.ZERO));
                } else {
                    totalProfit = totalProfit.add(pnl);
                }

                // Daily PnL
                LocalDate date = t.getDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                dailyPnL.put(date, dailyPnL.getOrDefault(date, BigDecimal.ZERO).add(pnl));
            }

            netProfit = totalProfit.subtract(totalLoss);

            // Most gained/loss date
            String mostGainedDate = dailyPnL.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(e -> e.getKey().toString())
                    .orElse(null);

            String mostLossDate = dailyPnL.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(e -> e.getKey().toString())
                    .orElse(null);

            // Build report
            ReportResponse report = ReportResponse.builder()
                    .symbol(symbol)
                    .currency("USD") // assuming USD for all, can be dynamic
                    .totalProfit(totalProfit.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                    .totalLoss(totalLoss.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                    .profit(netProfit.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP))
                    .totalDeposit(totalDeposit)
                    .totalWithdrawal(totalWithdrawal)
                    .mostGainedDate(mostGainedDate)
                    .mostLossDate(mostLossDate)
                    .result(netProfit.compareTo(BigDecimal.ZERO) > 0 ? "WIN" : "LOSS")
                    .build();

            reports.add(report);
        }

        return new ResponseBuilderUtils<>(
                ApplicationCode.HTTP_200,
                ApplicationCode.FETCH,
                reports
        );
    }

    public List<TransactionModel> getTransactionsLastNDays(Integer userId, int n) {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(n);

        LocalDateTime fromDate = startDate.atStartOfDay(); // 00:00:00
        LocalDateTime toDate = LocalDate.now().atTime(23, 59, 59); // today 23:59:59

        // fetch transactions since fromDate
        return transactionRepository.findByUserIdSince(userId, fromDate, toDate);
    }
}
