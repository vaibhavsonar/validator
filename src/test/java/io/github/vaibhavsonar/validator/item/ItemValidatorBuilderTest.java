package io.github.vaibhavsonar.validator.item;

import io.github.vaibhavsonar.validator.constants.ValidationType;
import io.github.vaibhavsonar.validator.helper.AddressValidator;
import io.github.vaibhavsonar.validator.helper.EmployeeValidator;
import io.github.vaibhavsonar.validator.helper.model.Address;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import org.junit.jupiter.api.Test;
import io.github.vaibhavsonar.validator.model.Error;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemValidatorBuilderTest {

    private static final int ROW = 1;

    @Test
    void validationType_shouldReturnItem() {
        ItemValidationResult result = new ItemValidationResult();

        assertEquals(ValidationType.ITEM, result.validationType());
    }

    @Test
    void addError_shouldStoreErrorAgainstRow() {
        ItemValidationResult result = new ItemValidationResult();

        Error error = new Error()
                .setField("id")
                .setRejectedValue("123")
                .setMessage("Invalid");

        result.addError(error, ROW);

        assertEquals(1, result.errors().size());

        List<Error> errors = result.errors().get(ROW);

        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertSame(error, errors.get(0));
    }

    @Test
    void addErrors_shouldStoreAllErrors() {
        ItemValidationResult result = new ItemValidationResult();

        Error error1 = new Error()
                .setField("id")
                .setMessage("Invalid id");

        Error error2 = new Error()
                .setField("address")
                .setMessage("Invalid address");

        result.addErrors(List.of(error1, error2), ROW);

        List<Error> errors = result.errors().get(ROW);

        assertNotNull(errors);
        assertEquals(2, errors.size());
    }

    @Test
    void addErrorsFrom_shouldMergeValidationResults() {
        ItemValidationResult first = new ItemValidationResult();
        ItemValidationResult second = new ItemValidationResult();

        first.addError(
                new Error().setField("id").setMessage("Invalid"),
                ROW);

        second.addError(
                new Error().setField("address").setMessage("Required"),
                ROW);

        first.addErrorsFrom(second);

        List<Error> errors = first.errors().get(ROW);

        assertNotNull(errors);
        assertEquals(2, errors.size());
    }

    @Test
    void isValidationSuccessful_whenNoErrors_shouldReturnTrue() {
        ItemValidationResult result = new ItemValidationResult();

        assertTrue(result.isValidationSuccessful());
    }

    @Test
    void isValidationSuccessful_whenErrorsExist_shouldReturnFalse() {
        ItemValidationResult result = new ItemValidationResult();

        result.addError(
                new Error().setField("id").setMessage("Invalid"),
                ROW);

        assertFalse(result.isValidationSuccessful());
    }
}
