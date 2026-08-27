package io.github.vaibhavsonar.validator.field;

import io.github.vaibhavsonar.validator.helper.TestRule;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import io.github.vaibhavsonar.validator.rule.builder.FieldRuleBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FieldValidatorTest {
    private static final int INDEX = 1;

    @Test
    void validate_validValue_shouldReturnNoErrors() {

        Employee employee = new Employee()
                .setId("EMP001");

        FieldValidator<Employee, String> validator =
                new FieldValidator<>(
                        FieldRuleBuilder.<Employee, String>newInstance()
                                .check(
                                        TestRule.ID_REQUIRED,
                                        "id",
                                        Employee::getId,
                                        String::isBlank,
                                        "Id is required")
                                .build()
                );

        ItemValidationResult result = validator.validate(employee, INDEX);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validate_nullValue_shouldReturnValidationError() {

        Employee employee = new Employee();

        FieldValidator<Employee, String> validator =
                new FieldValidator<>(
                        FieldRuleBuilder.<Employee, String>newInstance()
                                .check(
                                        TestRule.ID_REQUIRED,
                                        "id",
                                        Employee::getId,
                                        value -> value == null,
                                        "Id is required")
                                .build()
                );

        ItemValidationResult result = validator.validate(employee, INDEX);

        assertFalse(result.isValidationSuccessful());

        Error error = result.errors().get(INDEX).get(0);

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
                        FieldRuleBuilder.<Employee, String>newInstance()
                                .check(
                                        TestRule.ID_INVALID,
                                        "id",
                                        Employee::getId,
                                        id -> id.length() < 6,
                                        "Invalid Id")
                                .build()
                );

        ItemValidationResult result = validator.validate(employee, INDEX);

        assertFalse(result.isValidationSuccessful());

        Error error = result.errors().get(INDEX).get(0);

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
                        FieldRuleBuilder.<Employee, String>newInstance()
                                .check(
                                        TestRule.ID_REQUIRED,
                                        "id",
                                        Employee::getId,
                                        String::isBlank,

                                        "Id is required")
                                .check(
                                        TestRule.ID_INVALID,
                                        "id",
                                        Employee::getId,
                                        id -> id.length() < 5,
                                        "Invalid Id")
                                .build()
                );

        ItemValidationResult result = validator.validate(employee, INDEX);

        assertFalse(result.isValidationSuccessful());
        assertEquals(2, result.errors().get(INDEX).size());
    }

    @Test
    void validate_onlyOneRuleFails_shouldReturnSingleError() {

        Employee employee = new Employee()
                .setId("EMP");

        FieldValidator<Employee, String> validator =
                new FieldValidator<>(
                        FieldRuleBuilder.<Employee, String>newInstance()
                                .check(
                                        TestRule.ID_REQUIRED,
                                        "id",
                                        Employee::getId,
                                        String::isBlank,
                                        "Id is required")
                                .check(
                                        TestRule.ID_INVALID,
                                        "id",
                                        Employee::getId,
                                        id -> id.length() < 5,
                                        "Invalid Id")
                                .build()
                );

        ItemValidationResult result = validator.validate(employee, INDEX);

        assertEquals(1, result.errors().get(INDEX).size());

        Error error = result.errors().get(INDEX).get(0);

        assertEquals("Invalid Id", error.getMessage());
    }

    @Test
    void validate_noRules_shouldReturnSuccessfulValidation() {

        Employee employee = new Employee()
                .setId("EMP001");

        FieldValidator<Employee, String> validator =
                new FieldValidator<>(
                        FieldRuleBuilder.<Employee, String>newInstance()
                                .build()
                );

        ItemValidationResult result = validator.validate(employee, INDEX);

        assertTrue(result.isValidationSuccessful());
    }

    @Test
    void validate_shouldAssociateErrorsWithSuppliedIndex() {

        int index = 25;

        Employee employee = new Employee();

        FieldValidator<Employee, String> validator =
                new FieldValidator<>(
                        FieldRuleBuilder.<Employee, String>newInstance()
                                .check(
                                        TestRule.ID_REQUIRED,
                                        "id",
                                        Employee::getId,
                                        value -> value == null,
                                        "Id is required")
                                .build()
                );

        ItemValidationResult result = validator.validate(employee, index);

        assertTrue(result.errors().containsKey(index));
        assertFalse(result.errors().containsKey(1));
    }

}
