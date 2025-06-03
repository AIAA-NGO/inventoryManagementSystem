package com.example.inventoryManagementSystem.validation;

import com.example.inventoryManagementSystem.dto.TaxRateDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;


public class TaxRateValidator implements ConstraintValidator<ValidTaxRate, TaxRateDTO> {
    @Override
    public boolean isValid(TaxRateDTO taxRateDTO, ConstraintValidatorContext context) {
        if (taxRateDTO.getRate() == null || taxRateDTO.getRate() < 0 || taxRateDTO.getRate() > 1) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Tax rate must be between 0 and 1")
                    .addPropertyNode("rate")
                    .addConstraintViolation();
            return false;
        }
        return true;
    }
}
