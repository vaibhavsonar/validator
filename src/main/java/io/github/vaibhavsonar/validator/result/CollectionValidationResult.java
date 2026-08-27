package io.github.vaibhavsonar.validator.result;

import io.github.vaibhavsonar.validator.constants.ValidationType;

/**
 * Validation result implementation for collection-level validation.
 * <p>
 * A {@code CollectionValidationResult} stores validation errors produced while
 * validating a collection of objects. Errors are inherited from
 * {@link AbstractValidationResult} and are grouped by their associated index.
 *
 * <p>For collection validation, an index typically identifies the position or
 * row associated with the validation error, such as a row in a CSV, Excel
 * file, or other batch input.
 *
 * <p>This result identifies its validation scope as
 * {@link ValidationType#COLLECTION}.
 *
 * @author Vaibhav Sonar
 */
public class CollectionValidationResult extends AbstractValidationResult {

    /**
     * Returns the validation scope represented by this result.
     *
     * @return {@link ValidationType#COLLECTION}, indicating that this result
     * contains collection-level validation results
     */
    @Override
    public ValidationType validationType() {
        return ValidationType.COLLECTION;
    }
}
