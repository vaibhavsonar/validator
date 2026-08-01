package io.github.vaibhavsonar.validator;

import io.github.vaibhavsonar.validator.result.CollectionValidationResult;

import java.util.List;

/**
 * Defines the contract for validating an entire collection of objects.
 * <p>
 * Unlike {@link ItemValidator}, which validates an individual object,
 * a {@code CollectionValidator} evaluates the collection as a whole.
 * This is useful for validation rules that depend on multiple objects,
 * such as detecting duplicate values, ensuring uniqueness, validating
 * relationships between records, or enforcing collection-level constraints.
 *
 * <p>Validation errors are associated with the corresponding row numbers
 * of the objects that violate the collection-level rules.
 *
 * @param <T> the type of objects contained in the collection
 *
 * @author Vaibhav Sonar
 */
public interface CollectionValidator<T> {

    /**
     * Validates the supplied collection.
     *
     * @param objectToValidate the collection to validate
     * @return the validation result containing any collection-level validation
     *         errors
     */
    CollectionValidationResult validate(List<T> objectToValidate);
}
