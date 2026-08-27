package io.github.vaibhavsonar.validator.item;

import io.github.vaibhavsonar.validator.constants.ValidationType;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ItemValidatorBuilderTest {

    private static final int INDEX = 1;

    @Test
    void validationType_shouldReturnItem() {
        ItemValidationResult result = new ItemValidationResult();

        assertEquals(ValidationType.ITEM, result.validationType());
    }

    @Test
    void addError_shouldStoreErrorAgainstIndex() {
        ItemValidationResult result = new ItemValidationResult();

        Error error = new Error()
                .setField("id")
                .setRejectedValue("123")
                .setMessage("Invalid");

        result.addError(error, INDEX);

        assertEquals(1, result.errors().size());

        List<Error> errors = result.errors().get(INDEX);

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

        result.addErrors(List.of(error1, error2), INDEX);

        List<Error> errors = result.errors().get(INDEX);

        assertNotNull(errors);
        assertEquals(2, errors.size());
    }

    @Test
    void addErrorsFrom_shouldMergeValidationResults() {
        ItemValidationResult first = new ItemValidationResult();
        ItemValidationResult second = new ItemValidationResult();

        first.addError(
                new Error().setField("id").setMessage("Invalid"),
                INDEX);

        second.addError(
                new Error().setField("address").setMessage("Required"),
                INDEX);

        first.addErrorsFrom(second);

        List<Error> errors = first.errors().get(INDEX);

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
                INDEX);

        assertFalse(result.isValidationSuccessful());
    }
}
