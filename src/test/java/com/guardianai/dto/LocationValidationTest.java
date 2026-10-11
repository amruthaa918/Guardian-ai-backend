package com.guardianai.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LocationValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    @DisplayName("Should pass validation for valid coordinates")
    void testValidCoordinates() {
        LocationRequest request = new LocationRequest(17.3850, 78.4867);
        Set<ConstraintViolation<LocationRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty(), "Valid coordinates should have no violations");
    }

    @Test
    @DisplayName("Should fail validation for latitude out of range")
    void testInvalidLatitude() {
        LocationRequest request = new LocationRequest(95.0, 78.4867);
        Set<ConstraintViolation<LocationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Latitude > 90 should produce violation");
    }

    @Test
    @DisplayName("Should fail validation for longitude out of range")
    void testInvalidLongitude() {
        LocationRequest request = new LocationRequest(17.3850, 195.0);
        Set<ConstraintViolation<LocationRequest>> violations = validator.validate(request);
        assertFalse(violations.isEmpty(), "Longitude > 180 should produce violation");
    }
}
