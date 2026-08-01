package io.github.vaibhavsonar.validator.collection;

import io.github.vaibhavsonar.validator.CollectionValidator;
import io.github.vaibhavsonar.validator.helper.TestRule;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.CollectionValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CompositeCollectionValidatorTest {
    @Test
    void validate_whenNoRules_shouldReturnSuccessfulValidation() {

        CompositeCollectionValidator<Employee> validator =
                new CompositeCollectionValidator<>();

        CollectionValidationResult result =
                validator.validate(List.of(new Employee()));

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void addRule_shouldExecuteValidator() {

        CompositeCollectionValidator<Employee> validator =
                new CompositeCollectionValidator<>();

        validator.addRule(
                TestRule.DUPLICATE_ID,
                employees -> {
                    CollectionValidationResult result = new CollectionValidationResult();
                    result.addError(
                            new Error()
                                    .setField("employees")
                                    .setMessage("Duplicate employee id"),
                            0);
                    return result;
                });

        CollectionValidationResult result =
                validator.validate(List.of(new Employee()));

        assertFalse(result.isValidationSuccessful());

        assertEquals(1, result.errors().size());

        Error error = result.errors().get(0).get(0);

        assertEquals("employees", error.getField());
        assertEquals("Duplicate employee id", error.getMessage());
    }

    @Test
    void addRules_shouldMergeValidators() {

        CompositeCollectionValidator<Employee> first =
                new CompositeCollectionValidator<>();

        CompositeCollectionValidator<Employee> second =
                new CompositeCollectionValidator<>();

        second.addRule(
                TestRule.DUPLICATE_ID,
                employees -> {
                    CollectionValidationResult result = new CollectionValidationResult();
                    result.addError(
                            new Error()
                                    .setField("employees")
                                    .setMessage("Duplicate employee id"),
                            0);
                    return result;
                });

        first.addRules(second);

        CollectionValidationResult result =
                first.validate(List.of(new Employee()));

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().get(0).size());
    }

    @Test
    void validate_multipleRules_shouldAggregateErrors() {

        CompositeCollectionValidator<Employee> validator =
                new CompositeCollectionValidator<>();

        validator.addRule(
                TestRule.DUPLICATE_ID,
                employees -> {
                    CollectionValidationResult result = new CollectionValidationResult();
                    result.addError(
                            new Error()
                                    .setField("employees")
                                    .setMessage("Duplicate employee id"),
                            0);
                    return result;
                });

        validator.addRule(
                TestRule.COLLECTION_EMPTY,
                employees -> {
                    CollectionValidationResult result = new CollectionValidationResult();
                    result.addError(
                            new Error()
                                    .setField("employees")
                                    .setMessage("Collection is empty"),
                            0);
                    return result;
                });

        CollectionValidationResult result =
                validator.validate(List.of(new Employee()));

        assertFalse(result.isValidationSuccessful());
        assertEquals(2, result.errors().get(0).size());
    }

    @Test
    void validate_shouldInvokeAllValidators() {

        int[] counter = {0};

        CompositeCollectionValidator<Employee> validator =
                new CompositeCollectionValidator<>();

        CollectionValidator<Employee> countingValidator = employees -> {
            counter[0]++;
            return new CollectionValidationResult();
        };

        validator.addRule(TestRule.DUPLICATE_ID, countingValidator);
        validator.addRule(TestRule.COLLECTION_EMPTY, countingValidator);

        validator.validate(List.of(new Employee()));

        assertEquals(2, counter[0]);
    }
}
