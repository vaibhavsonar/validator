package io.github.vaibhavsonar.validator.result;

import io.github.vaibhavsonar.validator.constants.ValidationType;
import io.github.vaibhavsonar.validator.model.Error;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Defines the contract for the result of a validation operation.
 * <p>
 * A {@code ValidationResult} maintains validation errors grouped by an
 * integer index and provides operations for adding, merging, and querying
 * those errors.
 *
 * <p>The {@link ValidationType} identifies the validation scope represented
 * by the result, such as collection-level or item-level validation.
 *
 * <p>Implementations provide the underlying error storage through
 * {@link #errors()} and specify their validation scope through
 * {@link #validationType()}.
 *
 * @author Vaibhav Sonar
 */
public interface ValidationResult {

    /**
     * Returns the validation errors grouped by their associated index.
     * <p>
     * The meaning of the index depends on the validation operation. For
     * item-level validation, it typically identifies the item or row being
     * validated. For collection-level validation, it identifies the index
     * associated with the collection validation result according to the
     * validator implementation.
     *
     * <p>The returned map contains one entry for each index for which one or
     * more validation errors have been recorded.
     *
     * @return a map containing validation errors grouped by index
     */
    Map<Integer, List<Error>> errors();

    /**
     * Returns the validation scope represented by this result.
     *
     * @return the {@link ValidationType} associated with this validation result
     */
    ValidationType validationType();

    /**
     * Adds a validation error for the specified index.
     * <p>
     * If errors already exist for the supplied index, the new error is
     * appended to the existing list. Otherwise, a new error list is created
     * for the index.
     *
     * @param error the validation error to add
     * @param index the index associated with the validation error
     */
    default void addError(Error error, int index) {
        errors().compute(index, (k, v) -> {
            if (Objects.isNull(v)) {
                v = new ArrayList<>();
            }
            v.add(error);
            return v;
        });
    }

    /**
     * Adds multiple validation errors for the specified index.
     * <p>
     * Each supplied error is added using {@link #addError(Error, int)}.
     *
     * @param errors the validation errors to add
     * @param index  the index associated with the validation errors
     */
    default void addErrors(List<Error> errors, int index) {
        errors.forEach(error -> addError(error, index));
    }

    /**
     * Determines whether the validation result contains no validation errors.
     *
     * @return {@code true} if no validation errors are present;
     * {@code false} otherwise
     */
    default boolean isValidationSuccessful() {
        return errors().isEmpty();
    }

    /**
     * Merges validation errors from another validation result into this
     * result.
     * <p>
     * Errors are merged using their existing indexes. Existing errors in this
     * result are retained, and errors from the supplied result are appended
     * to the corresponding index.
     *
     * @param other the validation result whose errors should be merged into
     *              this result
     */
    default void addErrorsFrom(ValidationResult other) {
        other.errors().forEach((index, errors) -> {
            addErrors(errors, index);
        });
    }
}
