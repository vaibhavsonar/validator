package io.github.vaibhavsonar.validator;

import io.github.vaibhavsonar.validator.result.ItemValidationResult;

/**
 * Defines the contract for validating a single object.
 * <p>
 * Implementations validate an object of the specified type and return an
 * {@link ItemValidationResult} containing any validation errors.
 *
 * <p>{@code ItemValidator} is intended for validating an individual object,
 * including its fields and nested objects. Validation rules that operate on an
 * entire collection, such as duplicate detection or cross-record validation,
 * should be implemented using {@code CollectionValidator}.
 *
 * @param <T> the type of object to validate
 *
 * @author Vaibhav Sonar
 */
public interface ItemValidator<T> {

    /**
     * Validates the supplied object.
     *
     * @param objectToValidate the object to validate
     * @param index the row number associated with the object. This value is
     *                  propagated to the validation result so that validation
     *                  errors can be associated with their originating row when
     *                  the object belongs to a larger dataset. For standalone
     *                  object validation, this value is typically {@code 0}.
     * @return the validation result
     */
    ItemValidationResult validate(T objectToValidate, int index);
}
