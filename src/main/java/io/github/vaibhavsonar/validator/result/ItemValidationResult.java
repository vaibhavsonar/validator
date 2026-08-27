package io.github.vaibhavsonar.validator.result;

import io.github.vaibhavsonar.validator.constants.ValidationType;

/**
 * Validation result implementation for item-level validation.
 * <p>
 * An {@code ItemValidationResult} stores validation errors produced while
 * validating a single object. The errors are inherited from
 * {@link AbstractValidationResult} and are grouped by the index associated
 * with the validation operation.
 *
 * <p>Because this result represents validation of a single item, the supplied
 * index identifies the item or row for which the validation errors were
 * produced.
 *
 * <p>This result identifies its validation scope as
 * {@link ValidationType#ITEM}.
 *
 * @author Vaibhav Sonar
 */
public class ItemValidationResult extends AbstractValidationResult {

    /**
     * Returns the validation scope represented by this result.
     *
     * @return {@link ValidationType#ITEM}, indicating that this result
     * contains item-level validation results
     */
    @Override
    public ValidationType validationType() {
        return ValidationType.ITEM;
    }
}
