package com.money.kark_profit.service.feature;

import com.money.kark_profit.cache.ConfigurationCache;
import com.money.kark_profit.constants.ApplicationCache;
import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.exception.DatabaseException;
import com.money.kark_profit.model.ConfigurationModel;
import com.money.kark_profit.model.TransactionModel;
import com.money.kark_profit.model.UserProfileModel;
import com.money.kark_profit.repository.ConfigurationRepository;
import com.money.kark_profit.repository.TransactionRepository;
import com.money.kark_profit.repository.UserProfileRepository;
import com.money.kark_profit.service.UserService;
import com.money.kark_profit.transform.request.PerformanceRequest;
import com.money.kark_profit.transform.response.MonthlyPnLResponse;
import com.money.kark_profit.transform.response.RecoveryPhaseResponse;
import com.money.kark_profit.utils.DateUtils;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PerformanceService {

    private final TransactionRepository transactionRepository;
    private final UserProfileRepository userProfileRepository;
    private final ConfigurationRepository configurationRepository;
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
                        .add(BigDecimal.valueOf(value));

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

    public ResponseBuilderUtils<RecoveryPhaseResponse> calculateRecoveryDebt(HttpServletRequest request) {
        if(validatePermission(request)) {
            RecoveryPhaseResponse response = RecoveryPhaseResponse.builder().build();

            ConfigurationModel debtEntity = configurationRepository.findByName("DEBT").get();
            if(debtEntity == null)
                throw new DatabaseException(ApplicationCode.DBE_001, ApplicationCode.DBE_001_MSG);

            double totalDebt = Double.parseDouble(debtEntity.getValue().split(" ")[0]);

            String currency = debtEntity.getValue().split(" ")[1];

            if(totalDebt <= 0) {
                response = RecoveryPhaseResponse.builder()
                        .totalDebt(totalDebt)
                        .totalProfit(0)
                        .totalLoss(0)
                        .recoveredAmount(0)
                        .remainingDebt(0)
                        .recoveryPercentage(100)
                        .build();
                return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.CREATED, response);
            }

            Integer currentUserId = userService.extractUserId(request);
            // Get all transactions for this user
            List<TransactionModel> transactions = transactionRepository.findByUserId(currentUserId);

            // Calculate total PROFIT and total LOSS separately
            double totalProfit = transactions.stream()
                    .filter(t -> "PROFIT".equalsIgnoreCase(t.getType()))
                    .mapToDouble(TransactionModel::getPnl)
                    .sum();

            double totalLoss = transactions.stream()
                    .filter(t -> "LOSS".equalsIgnoreCase(t.getType()))
                    .mapToDouble(TransactionModel::getPnl)
                    .sum();

            // Net recovered amount = Total Profit - Total Loss
            double recoveredAmount = totalProfit - totalLoss;

            // Calculate remaining debt and recovery percentage
            double remainingDebt = totalDebt - recoveredAmount;
            double recoveryPercentage = (recoveredAmount / totalDebt) * 100;

            // Create response
            response = RecoveryPhaseResponse.builder()
                    .totalDebt(totalDebt)
                    .totalProfit(totalProfit)
                    .totalLoss(totalLoss)
                    .recoveredAmount(recoveredAmount)
                    .remainingDebt(Math.max(remainingDebt, 0))
                    .recoveryPercentage(Math.min(recoveryPercentage, 100))
                    .build();

            debtEntity.setValue(Math.max(remainingDebt, 0) + " " + currency);
            log.info("Updating new debt!");
            configurationRepository.save(debtEntity);

            return new ResponseBuilderUtils<>(ApplicationCode.HTTP_200, ApplicationCode.CREATED, response);
        }
        throw new DatabaseException(ApplicationCode.DBE_001 ,ApplicationCode.DBE_001_MSG);
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
}
