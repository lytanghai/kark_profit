package com.money.kark_profit.service.feature;

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
import com.money.kark_profit.transform.request.TableConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;

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
                Thread.sleep(1 * 60 * 500); // 30 seconds in milliseconds
            }
        }
        log.info("process completed");
    }

    private Map<String, TableConfig> getTableConfig(int year, int month) {

        final int y = year;
        final int m = month;

        return Map.of(
                ApplicationConstant.TRANSACTION,
                new TableConfig(
                        () -> transactionRepository.queryMonthlyRecord(y, m),
                        TransactionModel.class
                ),
                ApplicationConstant.CONFIGURATION,
                new TableConfig<>(
                        () -> configurationRepository.queryMonthlyRecord(y, m),
                        ConfigurationModel.class
                ),
                ApplicationConstant.USER_PROFILE,
                new TableConfig(
                        () -> userProfileRepository.queryMonthlyRecord(y, m),
                        UserProfileModel.class
                )
        );
    }

    public void process(String tableName, int previousMonth, int year) {

        TableConfig<?> config = getTableConfig(year, previousMonth).get(tableName);
        if (config == null) {
            throw new DatabaseException(
                    ApplicationCode.DBE_ERR_403,
                    ApplicationCode.DBE_ERR_403_MSG
            );
        }

        List<?> result = config.fetch();

        if (result == null || result.isEmpty()) {
            log.info("No records to backup for table: {}", tableName);
            return;
        }

        byte[] excelBytes = buildExcel(result, config.getFields());
        String sql = buildInsertSql(tableName, result, config.getFields());
        sendEmailWithAttachment(excelBytes, sql, tableName);

        log.info("Email Request Sent for table: {}", tableName);
    }

    private void sendEmailWithAttachment(byte[] excelBytes, String sql, String tableName) {

        String excelBase64 = Base64.getEncoder().encodeToString(excelBytes);
        String sqlBase64 = Base64.getEncoder().encodeToString(sql.getBytes(StandardCharsets.UTF_8));

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setFrom(buildFrom());
        emailRequest.setTo(List.of(buildTo()));
        emailRequest.setSubject(subject);
        emailRequest.setCategory(report);

        emailRequest.setText("Please find attached backup report for " + tableName);

        List<EmailRequest.Attachment> attachments = List.of(
                buildExcelAttachment(excelBase64, tableName),
                buildSqlAttachment(sqlBase64, tableName)
        );

        emailRequest.setAttachments(attachments);

        restTemplateHttpClient.post(
                mailTrapUrl,
                emailRequest,
                ConfigurationCache.getByKeyName("MAIL_TRAP_TOKEN").getValue(),
                String.class
        );
    }

    private EmailRequest.Attachment buildExcelAttachment(String base64, String tableName) {

        EmailRequest.Attachment attachment = new EmailRequest.Attachment();
        attachment.setFilename(tableName + "_backup.xlsx");
        attachment.setContent(base64);
        attachment.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        attachment.setDisposition("attachment");

        return attachment;
    }

    public byte[] buildExcel(List<?> result, List<Field> fields) {

        try (SXSSFWorkbook workbook = new SXSSFWorkbook(100);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Backup Report");

            // Better fixed column width (safe for large data)
            for (int i = 0; i < fields.size(); i++) {
                sheet.setColumnWidth(i, 6000); // ~25 chars
            }

            // Header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Optional: Freeze header row
            sheet.createFreezePane(0, 1);

            // Header row
            Row header = sheet.createRow(0);
            for (int i = 0; i < fields.size(); i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(fields.get(i).getName());
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;

            for (Object record : result) {
                Row row = sheet.createRow(rowIdx++);

                for (int j = 0; j < fields.size(); j++) {
                    Field field = fields.get(j);
                    Cell cell = row.createCell(j);

                    try {
                        Object value = field.get(record);

                        if (value == null) {
                            cell.setBlank();

                        } else if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());

                        } else if (value instanceof Boolean) {
                            cell.setCellValue((Boolean) value);

                        } else {
                            cell.setCellValue(value.toString());
                        }

                    } catch (IllegalAccessException e) {
                        cell.setCellValue("ERROR");
                    }
                }
            }

            workbook.write(out);
            workbook.dispose(); // very important for temp files cleanup

            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Failed to build Excel file", e);
        }
    }

    private String buildInsertSql(String tableName, List<?> result, List<Field> fields) {

        StringBuilder sb = new StringBuilder();

        sb.append("INSERT INTO ").append(tableName).append(" (");

        // column names
        for (int i = 0; i < fields.size(); i++) {
            sb.append(fields.get(i).getName());
            if (i < fields.size() - 1) sb.append(", ");
        }

        sb.append(") VALUES\n");

        for (int i = 0; i < result.size(); i++) {
            Object record = result.get(i);
            sb.append("(");

            for (int j = 0; j < fields.size(); j++) {
                Field field = fields.get(j);

                try {
                    Object value = field.get(record);

                    if (value == null) {
                        sb.append("NULL");

                    } else if (value instanceof Number || value instanceof Boolean) {
                        sb.append(value);

                    } else {
                        // escape quotes
                        String escaped = value.toString().replace("'", "''");
                        sb.append("'").append(escaped).append("'");
                    }

                } catch (IllegalAccessException e) {
                    sb.append("NULL");
                }

                if (j < fields.size() - 1) sb.append(", ");
            }

            sb.append(")");

            if (i < result.size() - 1) {
                sb.append(",\n");
            }
        }

        sb.append(";");

        return sb.toString();
    }

    private EmailRequest.Attachment buildSqlAttachment(String base64, String tableName) {

        EmailRequest.Attachment attachment = new EmailRequest.Attachment();
        attachment.setFilename(tableName + "_backup.sql");
        attachment.setContent(base64);
        attachment.setType("text/plain"); // better compatibility than application/sql
        attachment.setDisposition("attachment");

        return attachment;
    }

    private EmailRequest.From buildFrom() {
        EmailRequest.From from = new EmailRequest.From();
        from.setEmail(fromEmail);
        from.setName("DataBae Team");
        return from;
    }

    private EmailRequest.To buildTo() {
        EmailRequest.To to = new EmailRequest.To();
        to.setEmail("tanghaidevops@gmail.com");
        return to;
    }

}
