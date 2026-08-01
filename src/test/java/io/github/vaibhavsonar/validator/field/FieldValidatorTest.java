package io.github.vaibhavsonar.validator.field;

import io.github.vaibhavsonar.validator.helper.TestRule;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import io.github.vaibhavsonar.validator.rule.ValidationRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FieldValidatorTest {
    private static final int ROW = 1;

    @Test
    void validate_validValue_shouldReturnNoErrors() {

        Employee employee = new Employee()
                .setId("EMP001");

        FieldValidator<Employee, String> validator =
                new FieldValidator<>(
                        "id",
                        Employee::getId,
                        List.of(
                                new ValidationRule<>(
                                        TestRule.ID_REQUIRED,
                                        String::isBlank,
                                        "Id is required")));

        ItemValidationResult result = validator.validate(employee, ROW);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validate_nullValue_shouldReturnValidationError() {

        Employee employee = new Employee();

        FieldValidator<Employee, String> validator =
                new FieldValidator<>(
                        "id",
                        Employee::getId,
                        List.of(
                                new ValidationRule<>(
                                        TestRule.ID_REQUIRED,
                                        value -> value == null,
                                        "Id is required")));

        ItemValidationResult result = validator.validate(employee, ROW);

        assertFalse(result.isValidationSuccessful());

        Error error = result.errors().get(ROW).get(0);

        assertEquals("id", error.getField());
        assertNull(error.getRejectedValue());
        assertEquals("Id is required", error.getMessage());
    }

    @Test
    void validate_invalidValue_shouldReturnValidationError() {

        Employee employee = new Employee()
                .setId("ABC");

        FieldValidator<Employee, String> validator =
                new FieldValidator<>(
                        "id",
                        Employee::getId,
                        List.of(
                                new ValidationRule<>(
                                        TestRule.ID_INVALID,
                                        id -> id.length() < 6,
                                        "Invalid Id")));

        ItemValidationResult result = validator.validate(employee, ROW);

        assertFalse(result.isValidationSuccessful());

        Error error = result.errors().get(ROW).get(0);

        assertEquals("id", error.getField());
        assertEquals("ABC", error.getRejectedValue());
        assertEquals("Invalid Id", error.getMessage());
    }

    @Test
    void validate_multipleRulesFail_shouldReturnMultipleErrors() {

        Employee employee = new Employee()
                .setId("");

        FieldValidator<Employee, String> validator =
                new FieldValidator<>(
                        "id",
                        Employee::getId,
                        List.of(
                                new ValidationRule<>(
                                        TestRule.ID_REQUIRED,
                                        String::isBlank,
                                        "Id is required"),
                                new ValidationRule<>(
                                        TestRule.ID_INVALID,
                                        id -> id.length() < 5,
                                        "Invalid Id")));

        ItemValidationResult result = validator.validate(employee, ROW);

        assertFalse(result.isValidationSuccessful());
        assertEquals(2, result.errors().get(ROW).size());
    }

    @Test
    void validate_onlyOneRuleFails_shouldReturnSingleError() {

        Employee employee = new Employee()
                .setId("EMP");

        FieldValidator<Employee, String> validator =
                new FieldValidator<>(
                        "id",
                        Employee::getId,
                        List.of(
                                new ValidationRule<>(
                                        TestRule.ID_REQUIRED,
                                        String::isBlank,
                                        "Id is required"),
                                new ValidationRule<>(
                                        TestRule.ID_INVALID,
                                        id -> id.length() < 5,
                                        "Invalid Id")));

        ItemValidationResult result = validator.validate(employee, ROW);

        assertEquals(1, result.errors().get(ROW).size());

        Error error = result.errors().get(ROW).get(0);

        assertEquals("Invalid Id", error.getMessage());
    }

    @Test
    void validate_noRules_shouldReturnSuccessfulValidation() {

        Employee employee = new Employee()
                .setId("EMP001");

        FieldValidator<Employee, String> validator =
                new FieldValidator<>(
                        "id",
                        Employee::getId,
                        List.of());

        ItemValidationResult result = validator.validate(employee, ROW);

        assertTrue(result.isValidationSuccessful());
    }

    @Test
    void validate_shouldAssociateErrorsWithSuppliedRowNumber() {

        int rowNumber = 25;

        Employee employee = new Employee();

        FieldValidator<Employee, String> validator =
                new FieldValidator<>(
                        "id",
                        Employee::getId,
                        List.of(
                                new ValidationRule<>(
                                        TestRule.ID_REQUIRED,
                                        value -> value == null,
                                        "Id is required")));

        ItemValidationResult result = validator.validate(employee, rowNumber);

        assertTrue(result.errors().containsKey(rowNumber));
        assertFalse(result.errors().containsKey(1));
    }

}
