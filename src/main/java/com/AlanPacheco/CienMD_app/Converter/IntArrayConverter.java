package com.AlanPacheco.CienMD_app.Converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class IntArrayConverter implements AttributeConverter<int[], String> {

    private static final String DEFAULT = "1,1,2,2,3";

    @Override
    public String convertToDatabaseColumn(int[] attribute) {
        if (attribute == null || attribute.length == 0) {
            return DEFAULT;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < attribute.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(attribute[i]);
        }
        return sb.toString();
    }

    @Override
    public int[] convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return new int[]{1, 1, 2, 2, 3};
        }
        String[] parts = dbData.split(",");
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
    }
}
