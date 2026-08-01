package io.github.vaibhavsonar.validator.collection;

import io.github.vaibhavsonar.validator.CollectionValidator;
import io.github.vaibhavsonar.validator.result.CollectionValidationResult;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

/**
 * A {@link CollectionValidator} that executes another collection validator only
 * when a specified condition is satisfied.
 * <p>
 * This validator enables conditional collection validation, allowing
 * collection-level validation rules to be applied only when the supplied
 * collection meets a specific criterion.
 *
 * <p>If the condition evaluates to {@code false}, validation is skipped and an
 * empty {@link CollectionValidationResult} is returned.
 *
 * <p>Example usage:
 * <pre>{@code
 * CollectionValidator<Person> validator =
 *     new ConditionalCollectionValidator<>(
 *         list -> !list.isEmpty(),
 *         duplicateEmailValidator);
 * }</pre>
 *
 * @param <T> the type of objects contained in the collection
 *
 * @author Vaibhav Sonar
 */
@RequiredArgsConstructor
public class ConditionalCollectionValidator<T> implements CollectionValidator<T> {

    /**
     * Predicate that determines whether the wrapped validator should be
     * executed.
     */
    private final Predicate<List<T>> condition;

    /**
     * Validator to execute when the condition evaluates to {@code true}.
     */
    private final CollectionValidator<T> validator;

    /**
     * Validates the supplied collection if the configured condition is
     * satisfied.
     * <p>
     * If the condition evaluates to {@code false}, validation is skipped and
     * an empty validation result is returned.
     *
     * @param objectToValidate the collection to validate
     * @return the validation result produced by the wrapped validator if the
     *         condition is satisfied; otherwise an empty
     *         {@link CollectionValidationResult}
     */
    @Override
    public CollectionValidationResult validate(List<T> objectToValidate) {
        if (condition.test(objectToValidate)) {
            return validator.validate(objectToValidate);
        }
        return new CollectionValidationResult();
    }
}
