package io.github.vaibhavsonar.validator.item;

import io.github.vaibhavsonar.validator.helper.TestRule;
import io.github.vaibhavsonar.validator.helper.model.Address;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CompositeItemValidatorTest {
    private static final int ROW = 1;

    @Test
    void validate_whenNoRules_shouldReturnSuccessfulResult() {

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        ItemValidationResult result = validator.validate(new Employee(), ROW);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void addRule_shouldExecuteValidator() {

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addRule(
                TestRule.ID_REQUIRED,
                (employee, row) -> {
                    ItemValidationResult result = new ItemValidationResult();
                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Id is required"),
                            row);
                    return result;
                });

        ItemValidationResult result =
                validator.validate(new Employee(), ROW);

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().get(ROW).size());
    }

    @Test
    void addRules_shouldMergeValidators() {

        CompositeItemValidator<Employee> first =
                new CompositeItemValidator<>();

        CompositeItemValidator<Employee> second =
                new CompositeItemValidator<>();

        second.addRule(
                TestRule.ID_REQUIRED,
                (employee, row) -> {
                    ItemValidationResult result = new ItemValidationResult();
                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Id is required"),
                            row);
                    return result;
                });

        first.addRules(second);

        ItemValidationResult result =
                first.validate(new Employee(), ROW);

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().get(ROW).size());
    }

    @Test
    void validate_multipleRules_shouldAggregateErrors() {

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addRule(
                TestRule.ID_REQUIRED,
                (employee, row) -> {
                    ItemValidationResult result = new ItemValidationResult();
                    result.addError(
                            new Error().setField("id").setMessage("Required"),
                            row);
                    return result;
                });

        validator.addRule(
                TestRule.ID_INVALID,
                (employee, row) -> {
                    ItemValidationResult result = new ItemValidationResult();
                    result.addError(
                            new Error().setField("id").setMessage("Invalid"),
                            row);
                    return result;
                });

        ItemValidationResult result =
                validator.validate(new Employee(), ROW);

        assertEquals(2, result.errors().get(ROW).size());
    }

    @Test
    void addNested_shouldValidateNestedObject() {

        CompositeItemValidator<Address> addressValidator =
                new CompositeItemValidator<>();

        addressValidator.addRule(
                TestRule.COUNTRY_REQUIRED,
                (address, row) -> {
                    ItemValidationResult result = new ItemValidationResult();
                    result.addError(
                            new Error()
                                    .setField("country")
                                    .setMessage("Country required"),
                            row);
                    return result;
                });

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addNested(
                TestRule.COUNTRY_REQUIRED,
                "address",
                Employee::getAddress,
                addressValidator);

        Employee employee = new Employee()
                .setAddress(new Address());

        ItemValidationResult result =
                validator.validate(employee, ROW);

        List<Error> errors = result.errors().get(ROW);

        assertEquals(1, errors.size());
        assertEquals("address.country", errors.get(0).getField());
    }

    @Test
    void validate_whenSkipPredicateMatches_shouldSkipValidation() {

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>(employee -> true);

        validator.addRule(
                TestRule.ID_REQUIRED,
                (employee, row) -> {
                    fail("Validator should not execute.");
                    return new ItemValidationResult();
                });

//        validator.skipWhen(employee -> true);

        ItemValidationResult result =
                validator.validate(new Employee(), ROW);

        assertTrue(result.isValidationSuccessful());
    }

    @Test
    void validate_whenSkipPredicateDoesNotMatch_shouldExecuteValidation() {

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>(employee -> false);

        validator.addRule(
                TestRule.ID_REQUIRED,
                (employee, row) -> {
                    ItemValidationResult result = new ItemValidationResult();
                    result.addError(
                            new Error().setField("id").setMessage("Required"),
                            row);
                    return result;
                });

        //validator.skipWhen(employee -> false);

        ItemValidationResult result =
                validator.validate(new Employee(), ROW);

        assertFalse(result.isValidationSuccessful());
    }

    @Test
    void validate_shouldPropagateRowNumber() {

        int row = 15;

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addRule(
                TestRule.ID_REQUIRED,
                (employee, r) -> {
                    ItemValidationResult result = new ItemValidationResult();
                    result.addError(
                            new Error().setField("id").setMessage("Required"),
                            r);
                    return result;
                });

        ItemValidationResult result =
                validator.validate(new Employee(), row);

        assertTrue(result.errors().containsKey(row));
    }
}
