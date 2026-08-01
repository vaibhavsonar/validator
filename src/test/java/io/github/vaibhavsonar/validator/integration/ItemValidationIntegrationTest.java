package io.github.vaibhavsonar.validator.integration;

import io.github.vaibhavsonar.validator.helper.EmployeeValidator;
import io.github.vaibhavsonar.validator.helper.model.Address;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemValidationIntegrationTest {

    @Test
    void validate_validEmployee_shouldReturnSuccessfulValidation() {

        Employee employee = new Employee()
                .setId("12345")
                .setAddress(
                        new Address()
                                .setCity("Pune")
                                .setCountry("India"));

        ItemValidationResult result =
                EmployeeValidator.employeeValidator()
                        .validate(employee, 1);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validate_invalidEmployee_shouldReturnAllErrors() {

        Employee employee = new Employee()
                .setId("ABC")
                .setAddress(
                        new Address()
                                .setCity("")
                                .setCountry("Invalid"));

        ItemValidationResult result =
                EmployeeValidator.employeeValidator()
                        .validate(employee, 1);

        assertFalse(result.isValidationSuccessful());

        List<Error> errors = result.errors().get(1);

        assertEquals(3, errors.size());

        assertTrue(errors.stream()
                .anyMatch(e ->
                        e.getField().equals("employeeId")
                                && e.getMessage().equals("Employee ID must contain digits only.")));

        assertTrue(errors.stream()
                .anyMatch(e ->
                        e.getField().equals("address.city")
                                && e.getMessage().equals("City must not be null or empty.")));

        assertTrue(errors.stream()
                .anyMatch(e ->
                        e.getField().equals("address.country")
                                && e.getMessage().equals("Invalid country")));
    }
}
