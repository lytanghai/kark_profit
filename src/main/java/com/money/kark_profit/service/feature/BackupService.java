package com.money.kark_profit.service.feature;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.money.kark_profit.cache.ConfigurationCache;
import com.money.kark_profit.constants.ApplicationCode;
import com.money.kark_profit.constants.ApplicationConstant;
import com.money.kark_profit.exception.DatabaseException;
import com.money.kark_profit.http.RestTemplateHttpClient;
import com.money.kark_profit.model.ConfigurationModel;
import com.money.kark_profit.model.TransactionModel;
import com.money.kark_profit.model.UserProfileModel;
import com.money.kark_profit.repository.ConfigurationRepository;
import com.money.kark_profit.repository.TransactionRepository;
import com.money.kark_profit.repository.UserProfileRepository;
import com.money.kark_profit.transform.request.EmailRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackupService {

    private final TransactionRepository transactionRepository;
    private final ConfigurationRepository configurationRepository;
    private final UserProfileRepository userProfileRepository;
    private final RestTemplateHttpClient restTemplateHttpClient;
    private String mailTrapUrl = "https://send.api.mailtrap.io/api/send";
    private String fromEmail = "Databae@demomailtrap.co";
    private String subject = "Databae Trading Monthly Backup";
    private String report = "Databae Trading Monthly Backup";

    @Scheduled(cron = "0 0 1 1 * ?") // 1 hour of the first day on the new month
    public void backup() throws InterruptedException {
        String[] tables = {
                ApplicationConstant.CONFIGURATION,
                ApplicationConstant.USER_PROFILE,
                ApplicationConstant.TRANSACTION
        };
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int previousMonth = today.minusMonths(1).getMonthValue();

        for (String table : tables) {
            process(table, previousMonth, year);

            if (!table.equals(tables[tables.length - 1])) {
                Thread.sleep(2 * 60 * 1000); // 2 minutes in milliseconds
            }
        }
    }

    public void process(String tableName, int previousMonth, int year) {
        String[] fields;
        List<?> result;

        if (ApplicationConstant.TRANSACTION.equals(tableName)) {
            fields = getFieldNames(TransactionModel.class);
            result = transactionRepository.queryMonthlyRecord(year, previousMonth);
        } else if (ApplicationConstant.CONFIGURATION.equals(tableName)) {
            fields = getFieldNames(ConfigurationModel.class);
            result = configurationRepository.queryMonthlyRecord(year, previousMonth);
        } else if (ApplicationConstant.USER_PROFILE.equals(tableName)) {
            fields = getFieldNames(UserProfileModel.class);
            result = userProfileRepository.queryMonthlyRecord(year, previousMonth);
        } else {
            throw new DatabaseException(ApplicationCode.DBE_ERR_403, ApplicationCode.DBE_ERR_403_MSG);
        }

        if (result == null || result.isEmpty()) {
            log.info("No records to backup for table: " + tableName);
            return;
        }

        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("INSERT INTO public.").append(tableName);
        queryBuilder.append("(").append(String.join(", ", fields)).append(") VALUES ");

        for (int i = 0; i < result.size(); i++) {
            Object record = result.get(i);
            queryBuilder.append("(");

            for (int j = 0; j < fields.length; j++) {
                try {
                    Field field = record.getClass().getDeclaredField(fields[j]);
                    field.setAccessible(true);
                    Object value = field.get(record);

                    if (value == null) {
                        queryBuilder.append("NULL");
                    } else if (value instanceof String || value instanceof java.time.LocalDate || value instanceof java.time.LocalDateTime) {
                        queryBuilder.append("'").append(value.toString().replace("'", "''")).append("'");
                    } else {
                        queryBuilder.append(value);
                    }

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException("Error accessing field: " + fields[j], e);
                }

                if (j < fields.length - 1) {
                    queryBuilder.append(", ");
                }
            }

            queryBuilder.append(")");
            if (i < result.size() - 1) {
                queryBuilder.append(", ");
            }
        }

        queryBuilder.append(";");

        String sql = queryBuilder.toString();

        EmailRequest emailRequest = new EmailRequest();

        EmailRequest.From from = new EmailRequest.From();
        from.setEmail(fromEmail);
        from.setName("DataBae Team");

        EmailRequest.To to = new EmailRequest.To();
        to.setEmail("tanghaidevops@gmail.com");

        emailRequest.setFrom(from);
        emailRequest.setTo(List.of(to));
        emailRequest.setSubject(subject);
        emailRequest.setCategory(report);
        emailRequest.setText(sql);
        restTemplateHttpClient.post(
                mailTrapUrl,
                emailRequest,
                ConfigurationCache.getByKeyName("MAIL_TRAP_TOKEN").getValue(),
                String.class);
        log.info("Email Request Sent!");
    }

    public static <T> String[] getFieldNames(Class<T> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        String[] jsonNames = new String[fields.length];

        for (int i = 0; i < fields.length; i++) {
            Field field = fields[i];
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);

            if (annotation != null && !annotation.value().isEmpty()) {
                jsonNames[i] = annotation.value();
            } else {
                jsonNames[i] = field.getName();
            }
        }

        return jsonNames;
    }
}
