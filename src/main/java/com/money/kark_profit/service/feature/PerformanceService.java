package com.money.kark_profit.service.feature;

import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.model.TransactionModel;
import com.money.kark_profit.repository.TransactionRepository;
import com.money.kark_profit.service.UserService;
import com.money.kark_profit.transform.request.PerformanceRequest;
import com.money.kark_profit.transform.response.MonthlyPnLResponse;
import com.money.kark_profit.utils.DateUtils;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    public List<TransactionModel> getMonthlyTransactions(Integer userId, int year, int month) {
        return transactionRepository.queryMonthlyTxn(userId, year, month);
    }

    public ResponseBuilderUtils<MonthlyPnLResponse> groupPnLByDay(HttpServletRequest request, PerformanceRequest performanceRequest) {
        int year = performanceRequest.getYear();
        int month = performanceRequest.getMonth();

        if (year == -1)
            year = DateUtils.getYear(new Date());

        if (month == -1)
            month = DateUtils.getMonth(new Date());

        Map<Integer, Double> dailyPnL = new HashMap<>();

        int daysInMonth = java.time.Month.of(month).length(java.time.Year.isLeap(year));

        for (int day = 1; day <= daysInMonth; day++) {
            dailyPnL.put(day, 0.0);
        }

        List<TransactionModel> transactions =
                getMonthlyTransactions(userService.extractUserId(request), year, month);

        for (TransactionModel txn : transactions) {

            if (!"profit".equalsIgnoreCase(txn.getType()) &&
                    !"loss".equalsIgnoreCase(txn.getType())) {
                continue;
            }

            LocalDate localDate = txn.getDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            if (localDate.getYear() == year && localDate.getMonthValue() == month) {

                int day = localDate.getDayOfMonth();

                double value = txn.getPnl();

                if ("loss".equalsIgnoreCase(txn.getType())) {
                    value = -value;
                }

                BigDecimal total = BigDecimal.valueOf(dailyPnL.get(day))
                        .add(BigDecimal.valueOf(value))
                        .setScale(2, RoundingMode.HALF_UP);

                dailyPnL.put(day, total.doubleValue());
            }
        }

        List<MonthlyPnLResponse.DailyPnLDto> days = new ArrayList<>();

        for (Map.Entry<Integer, Double> entry : dailyPnL.entrySet()) {
            days.add(new MonthlyPnLResponse.DailyPnLDto(entry.getKey(), entry.getValue()));
        }

        MonthlyPnLResponse response = MonthlyPnLResponse.builder()
                .year(year)
                .month(month)
                .days(days)
                .build();

        return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.CREATED, response);
    }

}
