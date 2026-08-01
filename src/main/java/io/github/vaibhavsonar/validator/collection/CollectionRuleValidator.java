package io.github.vaibhavsonar.validator.collection;

import io.github.vaibhavsonar.validator.CollectionValidator;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.CollectionValidationResult;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

/**
 * Default {@link CollectionValidator} implementation that validates an entire
 * collection using a single predicate.
 * <p>
 * A {@code CollectionRuleValidator} evaluates the supplied collection against
 * a collection-level validation rule. If the rule indicates a validation
 * failure, a validation error is added to the returned
 * {@link CollectionValidationResult}.
 *
 * <p>This validator is intended for collection-wide constraints such as
 * duplicate detection, uniqueness checks, collection size validation, or
 * consistency checks across multiple objects.
 *
 * @param <T> the type of objects contained in the collection
 *
 * @author Vaibhav Sonar
 */
@RequiredArgsConstructor
public class CollectionRuleValidator<T> implements CollectionValidator<T> {

    /**
     * Name of the field associated with the validation error.
     */
    private final String fieldName;

    /**
     * Predicate used to validate the collection.
     * <p>
     * The predicate should return {@code true} when the collection violates
     * the validation rule and a validation error should be reported.
     */
    private final Predicate<List<T>> rule;

    /**
     * Validation message reported when the rule fails.
     */
    private final String message;

    /**
     * Validates the supplied collection.
     * <p>
     * If the configured rule indicates a validation failure, a validation
     * error is added to the result. Since the validation applies to the
     * collection as a whole, the error is associated with row {@code 0}.
     *
     * @param objectToValidate the collection to validate
     * @return the validation result containing any collection-level validation
     *         errors
     */
    @Override
    public CollectionValidationResult validate(List<T> objectToValidate) {
        CollectionValidationResult validationResult = new CollectionValidationResult();
        if (rule.test(objectToValidate)) {
            validationResult.addError(
                    new Error()
                            .setField(fieldName)
                            .setMessage(message), 0
            );
        }
        return validationResult;
    }
}
