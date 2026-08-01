package io.github.vaibhavsonar.validator.result;

import io.github.vaibhavsonar.validator.constants.ValidationType;

/**
 * Default {@link ValidationResult} implementation for validating a single object.
 * <p>
 * Validation errors are stored in a map keyed by row number. Since this result
 * represents validation of a single object rather than a collection, all
 * validation errors are associated with given row index.
 *
 * <p>The returned validation type is {@link ValidationType#ITEM}.
 *
 * @author Vaibhav Sonar
 */
public class ItemValidationResult extends AbstractValidationResult {

    /**
     * Returns the validation type represented by this result.
     *
     * @return {@link ValidationType#ITEM}
     */
    @Override
    public ValidationType validationType() {
        return ValidationType.ITEM;
    }
}
