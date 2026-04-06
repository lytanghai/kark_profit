package com.money.kark_profit.service.feature;

import com.money.kark_profit.cache.ConfigurationCache;
import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.exception.DatabaseException;
import com.money.kark_profit.http.RestTemplateHttpClient;
import com.money.kark_profit.model.TransactionModel;
import com.money.kark_profit.model.UserProfileModel;
import com.money.kark_profit.repository.TransactionRepository;
import com.money.kark_profit.repository.UserProfileRepository;
import com.money.kark_profit.transform.interfaze.UserSummary;
import com.money.kark_profit.transform.request.EmailRequest;
import com.money.kark_profit.transform.request.ReportRequest;
import com.money.kark_profit.transform.response.ReportResponse;
import com.money.kark_profit.utils.DateUtils;
import com.money.kark_profit.utils.ResponseBuilderUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
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
@Slf4j
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserProfileRepository userProfileRepository;
    private final RestTemplateHttpClient restTemplateHttpClient;
    private String mailTrapUrl = "https://send.api.mailtrap.io/api/send";
    private String fromEmail = "Databae@demomailtrap.co";
    private String subject = "Databae Trading Monthly Report";
    private String report = "Databae Trading Monthly Report";

    @Cacheable(
            value = "reportCache", // a new cache name
            key = "#userId + '-' + #reportRequest.lastNDay"
    )
    public ResponseBuilderUtils<List<ReportResponse>> generateReport(
            ReportRequest reportRequest,
            int userId,
            HttpServletRequest httpServletRequest) {

        // Verify user
        UserProfileModel userProfile = null;
        try {
            userProfile = userProfileRepository.findById(userId).orElse(null);
        }catch (Exception e) {
            throw new DatabaseException(ApplicationCode.DBE_ERR_001, ApplicationCode.DBE_ERR_001_MSG);
        }
        if (userProfile == null) {
            throw new DatabaseException(ApplicationCode.DBE_001, ApplicationCode.DBE_001_MSG);
        }

        // Fetch transactions for last N days
        List<TransactionModel> transactions = null;
        try {
            transactions  = getTransactionsLastNDays(userId, reportRequest.getLastNDay());

        } catch (Exception e) {
            throw new DatabaseException(ApplicationCode.DBE_ERR_001, ApplicationCode.DBE_ERR_001_MSG);
        }

        /*
         * Calculate total deposit & withdrawal globally
         * (not per symbol)
         */
        BigDecimal totalDeposit = transactions.stream()
                .filter(t -> "DEPOSIT".equalsIgnoreCase(t.getType()))
                .map(t -> Optional.ofNullable(t.getPnl())
                        .map(BigDecimal::valueOf)
                        .orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWithdrawal = transactions.stream()
                .filter(t -> "WITHDRAWAL".equalsIgnoreCase(t.getType()))
                .map(t -> Optional.ofNullable(t.getPnl())
                        .map(BigDecimal::valueOf)
                        .orElse(BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Group transactions by symbol
        Map<String, List<TransactionModel>> transactionsBySymbol = transactions.stream()
                .filter(t -> t.getSymbol() != null) // ignore deposit/withdrawal rows
                .collect(Collectors.groupingBy(TransactionModel::getSymbol));

        List<ReportResponse> reports = new ArrayList<>();

        for (Map.Entry<String, List<TransactionModel>> entry : transactionsBySymbol.entrySet()) {

            String symbol = entry.getKey();
            List<TransactionModel> symbolTxns = entry.getValue();

            BigDecimal totalProfit = BigDecimal.ZERO;
            BigDecimal totalLoss = BigDecimal.ZERO;

            Map<LocalDate, BigDecimal> dailyPnL = new HashMap<>();

            for (TransactionModel t : symbolTxns) {

                BigDecimal pnl = Optional.ofNullable(t.getPnl())
                        .map(BigDecimal::valueOf)
                        .orElse(BigDecimal.ZERO);

                if ("LOSS".equalsIgnoreCase(t.getType())) {
                    totalLoss = totalLoss.add(pnl);
                    pnl = pnl.negate();
                }
                else if ("PROFIT".equalsIgnoreCase(t.getType())) {
                    totalProfit = totalProfit.add(pnl);
                }
                else {
                    continue;
                }

                LocalDate date = t.getDate()
                        .toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();

                dailyPnL.put(date,
                        dailyPnL.getOrDefault(date, BigDecimal.ZERO).add(pnl));
            }

            BigDecimal netProfit = totalProfit.subtract(totalLoss);

            String mostGainedDate = dailyPnL.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(e -> e.getKey().toString())
                    .orElse(null);

            String mostLossDate = dailyPnL.entrySet().stream()
                    .min(Map.Entry.comparingByValue())
                    .map(e -> e.getKey().toString())
                    .orElse(null);

            BigDecimal totalProfitDiv = totalProfit.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal totalLossDiv = totalLoss.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal profitDiv = netProfit.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);

            BigDecimal totalDepositDiv = totalDeposit.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            BigDecimal totalWithdrawalDiv = totalWithdrawal.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP);
            ReportResponse report = ReportResponse.builder()
                    .symbol(symbol)
                    .currency("USD")
                    .totalProfit(totalProfitDiv)
                    .totalLoss(totalLossDiv)
                    .profit(profitDiv)
                    .totalDeposit(totalDepositDiv)
                    .totalWithdrawal(totalWithdrawalDiv)
                    .mostGainedDate(mostGainedDate)
                    .mostLossDate(mostLossDate)
                    .result(profitDiv.compareTo(BigDecimal.ZERO) > 0
                            ? "WIN"
                            : profitDiv.compareTo(BigDecimal.ZERO) < 0
                            ? "LOSE"
                            : "DRAW")
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

    private double normalizeAmount(TransactionModel t) {
        if (t.getPnl() == null) {
            return 0;
        }

        if ("USDC".equalsIgnoreCase(t.getCurrency())) {
            return t.getPnl() * 100; // convert to cents
        }

        return t.getPnl(); // USD stays the same
    }

    private void sendReport(List<TransactionModel> transactions, String username, String email) {
        int totalTransactions = transactions.size();

        double totalDeposit = transactions.stream()
                .filter(t -> "DEPOSIT".equalsIgnoreCase(t.getType()))
                .mapToDouble(this::normalizeAmount)
                .sum();

        double totalWithdrawal = transactions.stream()
                .filter(t -> "WITHDRAWAL".equalsIgnoreCase(t.getType()))
                .mapToDouble(this::normalizeAmount)
                .sum();

        double totalProfit = transactions.stream()
                .filter(t -> t.getPnl() != null && t.getPnl() > 0)
                .mapToDouble(this::normalizeAmount)
                .sum();

        double totalLoss = transactions.stream()
                .filter(t -> t.getPnl() != null && t.getPnl() < 0)
                .mapToDouble(this::normalizeAmount)
                .sum();

        double netPnL = totalProfit + totalLoss;

        String mostTradedSymbol = transactions.stream()
                .collect(Collectors.groupingBy(TransactionModel::getSymbol, Collectors.counting()))
                .entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        String emailBody = String.format("""
        Monthly Trading Account Summary
        
        Dear %s,
        
        Please find below the summary of your account activity for this month.
        
        -----------------------------------------
        Account Activity Summary
        -----------------------------------------
        
        Total Transactions : %d
        Most Traded Symbol : %s
        
        Total Deposit      : %.2f
        Total Withdrawal   : %.2f
        
        
        Trading Performance
        -----------------------------------------
        
        Total Profit       : %.2f
        Total Loss         : %.2f
        
        Net P&L            : %.2f
        
        -----------------------------------------
        
        Thank you for trading with us.
        
        Best regards,
        Databae Team
        """,
                username,
                totalTransactions,
                mostTradedSymbol,
                totalDeposit,
                totalWithdrawal,
                totalProfit,
                totalLoss,
                netPnL
        );

        EmailRequest emailRequest = new EmailRequest();

        EmailRequest.From from = new  EmailRequest.From();
        from.setEmail(fromEmail);
        from.setName("DataBae Team");

        EmailRequest.To to = new EmailRequest.To();
        to.setEmail(email);

        emailRequest.setFrom(from);
        emailRequest.setTo(List.of(to));
        emailRequest.setSubject(subject);
        emailRequest.setCategory(report);
        emailRequest.setText(emailBody);

        restTemplateHttpClient.post(
                mailTrapUrl,
                emailRequest,
                ConfigurationCache.getByKeyName("MAIL_TRAP_TOKEN").getValue(),
                String.class);
        log.info("Email Request Sent!");
    }

    @Scheduled(cron = "0 5 0 1 * ?") // 5 minutes past midnight on 1st day of month
    public void sendMonthlyReports() {
        Date[] dateRange = DateUtils.getPreviousMonthRange();
        Date startDate = dateRange[0];
        Date endDate = dateRange[1];

        log.info("Starting monthly report generation for period: {} to {}",
                startDate, endDate);

        List<UserSummary> userList = userProfileRepository.fetchUsers();

        log.info("Found {} users with transactions in this period", userList.size());

        for (UserSummary user : userList) {
            try {

                // Fetch transactions for this user in the date range
                List<TransactionModel> transactions = transactionRepository
                        .findByUserIdAndDateBetween(user.getId(), startDate, endDate);

                log.info("User {} has {} transactions in this period",
                        user, transactions.size());

                // Send email
                sendReport(transactions, user.getUsername(), user.getEmail());

            } catch (Exception e) {
                log.error("Failed to send email to user {}: {}",
                        user, e.getMessage(), e);
            }
        }

        log.info("Monthly report generation completed");
    }


}
