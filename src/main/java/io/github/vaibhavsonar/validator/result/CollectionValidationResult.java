package io.github.vaibhavsonar.validator.result;

import io.github.vaibhavsonar.validator.constants.ValidationType;

/**
 * Default {@link ValidationResult} implementation for validating a collection
 * of objects.
 * <p>
 * Validation errors are grouped by row number, where each key represents the
 * row of the input collection (for example, a row in a CSV or Excel file) and
 * the corresponding value contains all validation errors associated with that
 * row.
 *
 * <p>The returned validation type is {@link ValidationType#COLLECTION}.
 *
 * @author Vaibhav Sonar
 */
public class CollectionValidationResult extends AbstractValidationResult {

    /**
     * Returns the validation type represented by this result.
     *
     * @return {@link ValidationType#COLLECTION}
     */
    @Override
    public ValidationType validationType() {
        return ValidationType.COLLECTION;
    }
}
