package com.example.inventoryManagementSystem.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = TaxRateValidator.class)
public @interface ValidTaxRate {
    String message() default "Invalid tax rate";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}


