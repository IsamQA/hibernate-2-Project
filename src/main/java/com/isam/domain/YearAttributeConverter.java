package com.isam.domain;

import jakarta.persistence.AttributeConverter;

import java.time.Year;

public class YearAttributeConverter implements AttributeConverter<Year, Short> {
    @Override
    public Short convertToDatabaseColumn(Year attribute) {
        if (attribute != null) {
            return (short) attribute.getValue();
        }
        return 0;
    }

    @Override
    public Year convertToEntityAttribute(Short dbData) {
        if (dbData != null) {
            return Year.of(dbData);
        }
        return null;
    }
}
