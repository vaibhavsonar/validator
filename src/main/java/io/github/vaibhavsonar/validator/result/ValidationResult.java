package io.github.vaibhavsonar.validator.result;

import io.github.vaibhavsonar.validator.constants.ValidationType;
import io.github.vaibhavsonar.validator.model.Error;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the outcome of a validation operation.
 * <p>
 * A {@code ValidationResult} maintains validation errors grouped by row number
 * and exposes utility methods for adding, merging, and querying validation
 * results.
 *
 * <p>The validation type indicates whether the result contains errors,
 * warnings, or other supported validation outcomes.
 *
 * <p>Implementations are expected to provide the underlying error storage
 * through {@link #errors()} and the associated {@link ValidationType}.
 *
 * @author Vaibhav Sonar
 */
public interface ValidationResult {

    /**
     * Returns the validation errors grouped by row number.
     * <p>
     * For collection or tabular validation, the map key represents the
     * zero-based or one-based row number (depending on the validator
     * implementation) associated with the validation errors.
     *
     * <p>
     * For single-object validation, all validation errors are stored under
     * index.
     *
     * For collection validation, all validation errors are stored under
     * row number {@code 0}.
     *
     * @return a map containing validation errors grouped by row number;
     *         row {@code 0} represents object-level validation
     */
    Map<Integer, List<Error>> errors();

    /**
     * Returns the type of validation represented by this result.
     *
     * @return the validation type
     */
    ValidationType validationType();

    /**
     * Adds a validation error for the specified row.
     *
     * @param error the validation error to add
     * @param rowNumber the row number associated with the error
     */
    default void addError(Error error, int rowNumber) {
        errors().compute(rowNumber, (k, v) -> {
            if (Objects.isNull(v)) {
                v = new ArrayList<>();
            }
            v.add(error);
            return v;
        });
    }

    /**
     * Adds multiple validation errors for the specified row.
     *
     * @param errors the validation errors to add
     * @param rowNumber the row number associated with the errors
     */
    default void addErrors(List<Error> errors, int rowNumber) {
        errors.forEach(error -> addError(error, rowNumber));
    }

    /**
     * Determines whether the validation completed successfully without
     * producing any validation errors.
     *
     * @return {@code true} if no validation errors are present;
     *         {@code false} otherwise
     */
    default boolean isValidationSuccessful() {
        return errors().isEmpty();
    }

    /**
     * Merges validation errors from another validation result into this one.
     *
     * @param other the validation result whose errors are to be merged
     */
    default void addErrorsFrom(ValidationResult other) {
        other.errors().forEach((row, errors) -> {
            addErrors(errors, row);
        });
    }
}
