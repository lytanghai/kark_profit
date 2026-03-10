package com.money.kark_profit.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class PayloadUtils {

    public static Map<String, Object> getNonNullFields(Object dto, List<String> requiredFields) {

        Map<String, Object> result = new HashMap<>();
        if (dto == null) {
            throw new IllegalArgumentException("DTO cannot be null");
        }
        Field[] fields = dto.getClass().getDeclaredFields();

        for (Field field : fields) {
            field.setAccessible(true);
            try {

                Object value = field.get(dto);
                String fieldName = field.getName();

                // Validate required fields
                if (requiredFields != null && requiredFields.contains(fieldName)) {

                    if (value == null ||
                            (value instanceof String && ((String) value).trim().isEmpty())) {

                        throw new IllegalArgumentException(
                                "Field '" + fieldName + "' is required and cannot be null or empty"
                        );
                    }
                }

                // Add non-null values
                if (value != null) {

                    if (value instanceof String str) {
                        if (!str.trim().isEmpty()) {
                            result.put(fieldName, value);
                        }
                    } else {
                        result.put(fieldName, value);
                    }
                }

            } catch (IllegalAccessException e) {
                log.error("Reflection error reading field {}", field.getName(), e);
            }
        }

        return result;
    }

}
