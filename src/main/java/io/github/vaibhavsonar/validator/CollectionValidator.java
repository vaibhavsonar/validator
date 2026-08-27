package io.github.vaibhavsonar.validator;

import io.github.vaibhavsonar.validator.result.CollectionValidationResult;

import java.util.List;

/**
 * Defines the contract for validating a collection of objects.
 * <p>
 * Unlike {@link ItemValidator}, which validates an individual object,
 * a {@code CollectionValidator} operates on a complete collection of
 * objects. It is intended for validation rules that require access to
 * multiple objects at the same time.
 *
 * <p>Typical collection-level validation includes duplicate detection,
 * uniqueness checks, minimum or maximum collection size, relationships
 * between records, and other constraints that cannot be evaluated using
 * an individual object alone.
 *
 * <p>The validation operation receives an {@code index} that can be used
 * to associate the resulting validation errors with the relevant position
 * or validation context. The interpretation of the index is determined by
 * the validator implementation.
 *
 * @param <T> the type of objects contained in the collection
 * @author Vaibhav Sonar
 */
public interface CollectionValidator<T> {

    /**
     * Validates the supplied collection.
     * <p>
     * The validator evaluates the collection according to its configured
     * collection-level validation logic and returns a
     * {@link CollectionValidationResult} containing any validation errors
     * produced by that operation.
     *
     * @param items the collection of objects to validate
     * @param index the index associated with the validation operation
     * @return the collection validation result containing any validation
     * errors produced during validation
     */
    CollectionValidationResult validate(List<T> items, int index);
}
