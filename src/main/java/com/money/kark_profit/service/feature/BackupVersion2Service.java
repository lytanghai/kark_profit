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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackupVersion2Service {

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

    private Map<String, TableConfig> getTableConfig(int year, int previousMonth) {
        return Map.of(
                ApplicationConstant.TRANSACTION,
                new TableConfig(
                        () -> transactionRepository.queryMonthlyRecord(year, previousMonth),
                        TransactionModel.class
                ),
                ApplicationConstant.CONFIGURATION,
                new TableConfig(
                        () -> configurationRepository.queryMonthlyRecord(year, previousMonth),
                        ConfigurationModel.class
                ),
                ApplicationConstant.USER_PROFILE,
                new TableConfig(
                        () -> userProfileRepository.queryMonthlyRecord(year, previousMonth),
                        UserProfileModel.class
                )
        );
    }

    public void process(String tableName, int previousMonth, int year) {

        TableConfig config = getTableConfig(year, previousMonth).get(tableName);

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

        sendEmailWithAttachment(excelBytes, tableName);

        log.info("Email Request Sent for table: {}", tableName);
    }
    private void sendEmailWithAttachment(byte[] fileBytes, String tableName) {

        String base64 = Base64.getEncoder().encodeToString(fileBytes);

        EmailRequest emailRequest = new EmailRequest();
        emailRequest.setFrom(buildFrom());
        emailRequest.setTo(List.of(buildTo()));
        emailRequest.setSubject(subject);
        emailRequest.setCategory(report);

        emailRequest.setAttachments(List.of(buildAttachment(base64, tableName)));

        restTemplateHttpClient.post(
                mailTrapUrl,
                emailRequest,
                ConfigurationCache.getByKeyName("MAIL_TRAP_TOKEN").getValue(),
                String.class
        );
    }

    public byte[] buildExcel(List<?> result, String[] fields) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Report");

            // Header style (bold)
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Header row
            Row header = sheet.createRow(0);
            for (int i = 0; i < fields.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(fields[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data rows
            int rowIdx = 1;
            for (Object record : result) {
                Row row = sheet.createRow(rowIdx++);

                for (int j = 0; j < fields.length; j++) {
                    Field field = record.getClass().getDeclaredField(fields[j]);
                    field.setAccessible(true);
                    Object value = field.get(record);

                    Cell cell = row.createCell(j);
                    cell.setCellValue(value != null ? value.toString() : "");
                }
            }

            // Auto-size columns
            for (int i = 0; i < fields.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to build Excel file", e);
        }
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

    private EmailRequest.Attachment buildAttachment(String base64, String tableName) {
        EmailRequest.Attachment attachment = new EmailRequest.Attachment();
        attachment.setFilename(tableName + "_backup.xlsx"); // dynamic name
        attachment.setContent(base64);
        attachment.setContentType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        );
        return attachment;
    }
}
