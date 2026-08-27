package io.github.vaibhavsonar.validator.collection;

import io.github.vaibhavsonar.validator.CollectionValidator;
import io.github.vaibhavsonar.validator.result.CollectionValidationResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite implementation of {@link CollectionValidator} that executes
 * multiple collection validators and aggregates their validation results.
 * <p>
 * A {@code CompositeCollectionValidator} maintains an ordered collection of
 * {@link CollectionValidator collection validators}. Each registered validator
 * is executed against the supplied collection, and the validation errors
 * produced by all validators are combined into a single
 * {@link CollectionValidationResult}.
 *
 * <p>Validators are executed in the order in which they are registered.
 * This allows multiple independent collection-level validation concerns to
 * be combined into a single validator.
 *
 * <p>This validator is intended for collection-level validation scenarios such
 * as duplicate detection, uniqueness checks, collection size constraints,
 * cross-record validation, and other rules that require access to multiple
 * items at the same time.
 *
 * @param <T> the type of objects contained in the collection
 * @author Vaibhav Sonar
 */
@Slf4j
public class CompositeCollectionValidator<T>
        implements CollectionValidator<T> {

    /**
     * Collection validators registered with this composite validator.
     * <p>
     * Validators are stored in registration order and are executed in that
     * same order when {@link #validate(List, int)} is invoked.
     */
    private final List<CollectionValidator<T>> validators = new ArrayList<>();

    /**
     * Registers a collection validator.
     * <p>
     * The supplied validator is appended to the list of configured validators
     * and will be executed when this composite validator validates a
     * collection.
     *
     * @param validator the collection validator to register
     * @return this validator for method chaining
     */
    public CompositeCollectionValidator<T> addRule(
            CollectionValidator<T> validator) {
        validators.add(validator);
        return this;
    }

    /**
     * Registers all validators from another
     * {@code CompositeCollectionValidator}.
     * <p>
     * The validators contained in the supplied composite are appended to this
     * composite in their existing order. Existing validators in this
     * composite are retained.
     *
     * @param compositeValidator the composite validator whose validators
     *                           should be added
     * @return this validator for method chaining
     */
    public CompositeCollectionValidator<T> addRules(
            CompositeCollectionValidator<T> compositeValidator) {
        compositeValidator.validators.forEach(this::addRule);
        return this;
    }

    /**
     * Validates the supplied collection using all registered collection
     * validators.
     * <p>
     * Each registered validator is executed with the supplied collection and
     * index. The validation errors produced by each validator are aggregated
     * into a single {@link CollectionValidationResult}.
     *
     * <p>If no registered validator produces an error, the returned result
     * represents a successful validation.
     *
     * @param items the collection to validate
     * @param index the index associated with the validation operation
     * @return the aggregated validation result produced by all registered
     * collection validators
     */
    @Override
    public CollectionValidationResult validate(
            List<T> items,
            int index) {

        CollectionValidationResult validationResult = new CollectionValidationResult();

        validators.forEach(rule -> {
            CollectionValidationResult result =
                    rule.validate(items, index);

            validationResult.addErrorsFrom(result);
        });

        return validationResult;
    }
}