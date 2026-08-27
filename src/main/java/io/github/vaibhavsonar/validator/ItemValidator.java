package io.github.vaibhavsonar.validator;

import io.github.vaibhavsonar.validator.result.ItemValidationResult;

/**
 * Defines the contract for validating a single item.
 * <p>
 * An {@code ItemValidator} validates an object of type {@code T} and returns
 * an {@link ItemValidationResult} containing the validation errors produced
 * during that operation.
 *
 * <p>An item validator can perform validation at different levels within the
 * item, including:
 * <ul>
 *     <li>validation of the item as a whole,</li>
 *     <li>validation of individual fields, and</li>
 *     <li>validation of nested objects.</li>
 * </ul>
 *
 * <p>Validation rules that require access to multiple items simultaneously,
 * such as duplicate detection, uniqueness checks, or cross-record
 * consistency checks, should be implemented using a
 * {@link CollectionValidator}.
 *
 * @param <T> the type of item to validate
 * @author Vaibhav Sonar
 */
public interface ItemValidator<T> {

    /**
     * Validates the supplied item.
     * <p>
     * The validator applies its configured item-level, field-level, and
     * nested-object validation logic and returns an
     * {@link ItemValidationResult} containing all validation errors produced
     * during the operation.
     *
     * <p>The supplied index is propagated to the validation result so that
     * errors can be associated with the item or validation position from
     * which they originated. For standalone validation, callers may use
     * {@code 0} or another appropriate index according to their application
     * context.
     *
     * @param objectToValidate the item to validate
     * @param index            the index associated with the validation operation
     * @return the validation result containing any validation errors produced
     * while validating the item
     */
    ItemValidationResult validate(T objectToValidate, int index);
}
