package io.github.vaibhavsonar.validator.collection;

import io.github.vaibhavsonar.validator.CollectionValidator;
import io.github.vaibhavsonar.validator.result.CollectionValidationResult;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Predicate;

/**
 * A conditional {@link CollectionValidator} that executes a wrapped collection
 * validator only when a specified condition is satisfied.
 * <p>
 * A {@code ConditionalCollectionValidator} evaluates the supplied collection
 * using a {@link Predicate}. When the predicate evaluates to {@code true}, the
 * configured validator is executed. When the predicate evaluates to
 * {@code false}, collection validation is skipped.
 *
 * <p>This validator is useful when collection-level validation should only
 * apply under specific conditions. For example, duplicate validation might
 * only be required when the collection contains more than one item.
 *
 * <p>Example usage:
 * <pre>{@code
 * CollectionValidator<Person> validator =
 *     new ConditionalCollectionValidator<>(
 *         people -> !people.isEmpty(),
 *         duplicateEmailValidator);
 * }</pre>
 *
 * @param <T> the type of objects contained in the collection
 * @author Vaibhav Sonar
 */
@RequiredArgsConstructor
public class ConditionalCollectionValidator<T> implements CollectionValidator<T> {

    /**
     * Predicate that determines whether the wrapped validator should be
     * executed.
     * <p>
     * When this predicate evaluates to {@code true}, the wrapped validator is
     * executed. When it evaluates to {@code false}, validation is skipped.
     */
    private final Predicate<List<T>> condition;

    /**
     * Collection validator executed when {@link #condition} evaluates to
     * {@code true}.
     */
    private final CollectionValidator<T> validator;

    /**
     * Validates the supplied collection conditionally.
     * <p>
     * The configured condition is evaluated against the collection. If the
     * condition evaluates to {@code true}, the wrapped validator is executed
     * and its validation result is returned.
     *
     * <p>If the condition evaluates to {@code false}, the wrapped validator is
     * not executed and an empty {@link CollectionValidationResult} is returned.
     *
     * <p>The supplied {@code index} is passed unchanged to the wrapped
     * validator.
     *
     * @param items the collection to validate
     * @param index the index associated with the validation operation
     * @return the validation result produced by the wrapped validator when the
     * condition is satisfied; otherwise an empty validation result
     */
    @Override
    public CollectionValidationResult validate(List<T> items, int index) {
        if (condition.test(items)) {
            return validator.validate(items, index);
        }
        return new CollectionValidationResult();
    }
}