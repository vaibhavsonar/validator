package io.github.vaibhavsonar.validator.item;

import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConditionalItemValidatorTest {
    private static final int INDEX = 1;

    @Test
    void validate_whenConditionMatches_shouldExecuteValidator() {

        Employee employee = new Employee()
                .setId("EMP001");

        ItemValidator<Employee> validator = (emp, index) -> {
            ItemValidationResult result = new ItemValidationResult();
            result.addError(
                    new io.github.vaibhavsonar.validator.model.Error()
                            .setField("id")
                            .setMessage("Invalid"),
                    index
            );
            return result;
        };

        ConditionalItemValidator<Employee> conditionalValidator =
                new ConditionalItemValidator<>(
                        employee1 -> true,
                        validator);

        ItemValidationResult result =
                conditionalValidator.validate(employee, INDEX);

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().containsKey(INDEX));
    }

    @Test
    void validate_whenConditionDoesNotMatch_shouldSkipValidation() {

        Employee employee = new Employee()
                .setId("EMP001");

        ItemValidator<Employee> validator = (emp, index) -> {
            fail("Validator should not be invoked.");
            return new ItemValidationResult();
        };

        ConditionalItemValidator<Employee> conditionalValidator =
                new ConditionalItemValidator<>(
                        employee1 -> false,
                        validator);

        ItemValidationResult result =
                conditionalValidator.validate(employee, INDEX);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validate_shouldReturnDelegateValidationResult() {

        Employee employee = new Employee();

        ItemValidationResult expected = new ItemValidationResult();
        expected.addError(
                new io.github.vaibhavsonar.validator.model.Error()
                        .setField("id")
                        .setMessage("Id is required"),
                INDEX
        );

        ItemValidator<Employee> validator = (emp, index) -> expected;

        ConditionalItemValidator<Employee> conditionalValidator =
                new ConditionalItemValidator<>(
                        e -> true,
                        validator);

        ItemValidationResult actual =
                conditionalValidator.validate(employee, INDEX);

        assertSame(expected, actual);
    }
}
