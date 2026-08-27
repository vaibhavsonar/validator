package io.github.vaibhavsonar.validator.collection;

import io.github.vaibhavsonar.validator.CollectionValidator;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.CollectionValidationResult;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

/**
 * Default {@link CollectionValidator} implementation that validates an entire
 * collection using a single validation predicate.
 * <p>
 * A {@code CollectionRuleValidator} evaluates the supplied collection against
 * the configured predicate. When the predicate evaluates to {@code true}, the
 * collection is considered to have failed validation and an {@link Error} is
 * added to the returned {@link CollectionValidationResult}.
 *
 * <p>The predicate follows a failure-oriented convention: it should return
 * {@code true} when the collection violates the validation rule and
 * {@code false} when the collection satisfies the rule.
 *
 * <p>This validator is intended for collection-level constraints that require
 * the collection to be evaluated as a whole, such as duplicate detection,
 * uniqueness checks, collection size validation, or consistency checks across
 * multiple objects.
 *
 * @param <T> the type of objects contained in the collection
 * @author Vaibhav Sonar
 */
@RequiredArgsConstructor
public class CollectionRuleValidator<T> implements CollectionValidator<T> {

    /**
     * Name of the field associated with a validation error produced by this
     * validator.
     * <p>
     * The configured field name is included in the resulting
     * {@link Error} when the collection fails validation.
     */
    private final String fieldName;

    /**
     * Predicate used to validate the collection.
     * <p>
     * The predicate receives the complete collection and should return
     * {@code true} when the collection violates the validation rule and a
     * validation error should be reported.
     */
    private final Predicate<List<T>> rule;

    /**
     * Validation message reported when the collection violates the configured
     * validation rule.
     */
    private final String message;

    /**
     * Validates the supplied collection against the configured validation
     * predicate.
     * <p>
     * The predicate is evaluated against the complete collection. If it
     * evaluates to {@code true}, a validation error containing the configured
     * field name and message is added to the result using the supplied index.
     * If the predicate evaluates to {@code false}, the returned result contains
     * no errors.
     *
     * @param items the collection to validate
     * @param index the index associated with the validation result
     * @return the collection validation result containing an error when the
     * collection violates the configured rule; otherwise a successful
     * validation result
     */
    @Override
    public CollectionValidationResult validate(List<T> items, int index) {
        CollectionValidationResult validationResult = new CollectionValidationResult();
        if (rule.test(items)) {
            validationResult.addError(
                    new Error()
                            .setField(fieldName)
                            .setMessage(message), index
            );
        }
        return validationResult;
    }
}
