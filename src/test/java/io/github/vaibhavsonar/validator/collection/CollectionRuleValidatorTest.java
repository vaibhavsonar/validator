package io.github.vaibhavsonar.validator.collection;

import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.CollectionValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollectionRuleValidatorTest {
    @Test
    void validate_whenRulePasses_shouldReturnSuccessfulValidation() {

        CollectionRuleValidator<Employee> validator =
                new CollectionRuleValidator<>(
                        "employees",
                        employees -> false,
                        "Validation failed");

        CollectionValidationResult result =
                validator.validate(List.of(new Employee()));

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validate_whenRuleFails_shouldReturnValidationError() {

        CollectionRuleValidator<Employee> validator =
                new CollectionRuleValidator<>(
                        "employees",
                        employees -> true,
                        "Duplicate employee id");

        CollectionValidationResult result =
                validator.validate(List.of(new Employee()));

        assertFalse(result.isValidationSuccessful());

        Error error = result.errors().get(0).get(0);

        assertEquals("employees", error.getField());
        assertEquals("Duplicate employee id", error.getMessage());
        assertNull(error.getRejectedValue());
    }

    @Test
    void validate_shouldAssociateErrorWithRowZero() {

        CollectionRuleValidator<Employee> validator =
                new CollectionRuleValidator<>(
                        "employees",
                        employees -> true,
                        "Validation failed");

        CollectionValidationResult result =
                validator.validate(List.of(new Employee()));

        assertTrue(result.errors().containsKey(0));
        assertEquals(1, result.errors().size());
    }

    @Test
    void validate_shouldInvokePredicateWithCollection() {

        List<Employee> employees = List.of(
                new Employee().setId("1"),
                new Employee().setId("2"));

        CollectionRuleValidator<Employee> validator =
                new CollectionRuleValidator<>(
                        "employees",
                        list -> list.size() == 2,
                        "Expected size");

        CollectionValidationResult result = validator.validate(employees);

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().get(0).size());
    }
}
