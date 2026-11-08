package com.retail.common.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {

    @Override
    public String convertToDatabaseColumn(String attribute) {
        return Aes256Util.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        return Aes256Util.decrypt(dbData);
    }
}

