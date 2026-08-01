package io.github.vaibhavsonar.validator.collection;

import io.github.vaibhavsonar.validator.CollectionValidator;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.CollectionValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ConditionalCollectionValidatorTest {

    @Test
    void validate_whenConditionMatches_shouldExecuteValidator() {

        CollectionValidator<Employee> validator = employees -> {
            CollectionValidationResult result = new CollectionValidationResult();
            result.addError(
                    new Error()
                            .setField("employees")
                            .setMessage("Duplicate employee id"),
                    0
            );
            return result;
        };

        ConditionalCollectionValidator<Employee> conditionalValidator =
                new ConditionalCollectionValidator<>(
                        employees -> true,
                        validator);

        CollectionValidationResult result =
                conditionalValidator.validate(List.of(new Employee()));

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().size());
        assertTrue(result.errors().containsKey(0));
    }

    @Test
    void validate_whenConditionDoesNotMatch_shouldSkipValidation() {

        CollectionValidator<Employee> validator = employees -> {
            fail("Validator should not be invoked.");
            return new CollectionValidationResult();
        };

        ConditionalCollectionValidator<Employee> conditionalValidator =
                new ConditionalCollectionValidator<>(
                        employees -> false,
                        validator);

        CollectionValidationResult result =
                conditionalValidator.validate(List.of(new Employee()));

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validate_shouldReturnDelegateValidationResult() {

        CollectionValidationResult expected = new CollectionValidationResult();
        expected.addError(
                new Error()
                        .setField("employees")
                        .setMessage("Duplicate employee id"),
                0
        );

        CollectionValidator<Employee> validator = employees -> expected;

        ConditionalCollectionValidator<Employee> conditionalValidator =
                new ConditionalCollectionValidator<>(
                        employees -> true,
                        validator);

        CollectionValidationResult actual =
                conditionalValidator.validate(List.of(new Employee()));

        assertSame(expected, actual);
    }
}
