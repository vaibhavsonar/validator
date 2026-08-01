package io.github.vaibhavsonar.validator.item;

import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ConditionalItemValidatorTest {
    private static final int ROW = 1;

    @Test
    void validate_whenConditionMatches_shouldExecuteValidator() {

        Employee employee = new Employee()
                .setId("EMP001");

        ItemValidator<Employee> validator = (emp, row) -> {
            ItemValidationResult result = new ItemValidationResult();
            result.addError(
                    new io.github.vaibhavsonar.validator.model.Error()
                            .setField("id")
                            .setMessage("Invalid"),
                    row
            );
            return result;
        };

        ConditionalItemValidator<Employee> conditionalValidator =
                new ConditionalItemValidator<>(
                        employee1 -> true,
                        validator);

        ItemValidationResult result =
                conditionalValidator.validate(employee, ROW);

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().containsKey(ROW));
    }

    @Test
    void validate_whenConditionDoesNotMatch_shouldSkipValidation() {

        Employee employee = new Employee()
                .setId("EMP001");

        ItemValidator<Employee> validator = (emp, row) -> {
            fail("Validator should not be invoked.");
            return new ItemValidationResult();
        };

        ConditionalItemValidator<Employee> conditionalValidator =
                new ConditionalItemValidator<>(
                        employee1 -> false,
                        validator);

        ItemValidationResult result =
                conditionalValidator.validate(employee, ROW);

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
                ROW
        );

        ItemValidator<Employee> validator = (emp, row) -> expected;

        ConditionalItemValidator<Employee> conditionalValidator =
                new ConditionalItemValidator<>(
                        e -> true,
                        validator);

        ItemValidationResult actual =
                conditionalValidator.validate(employee, ROW);

        assertSame(expected, actual);
    }
}
