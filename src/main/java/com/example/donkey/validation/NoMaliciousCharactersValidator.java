package com.example.donkey.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class NoMaliciousCharactersValidator implements ConstraintValidator<NoMaliciousCharacters, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if(value == null || value.isBlank()){
            return true;
        }

        return !value.contains("..");
    }
}
