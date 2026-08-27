package io.github.vaibhavsonar.validator.item;

import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

/**
 * Conditional {@link ItemValidator} that executes a wrapped item validator
 * only when a specified condition is satisfied.
 * <p>
 * A {@code ConditionalItemValidator} evaluates the supplied object using a
 * {@link Predicate}. When the predicate evaluates to {@code true}, the
 * configured validator is executed. When the predicate evaluates to
 * {@code false}, validation is skipped.
 *
 * <p>This validator is useful when validation rules should only apply to an
 * object under specific conditions. For example, employment-related fields
 * can be validated only when an employee is currently employed.
 *
 * <p>Example usage:
 * <pre>{@code
 * ItemValidator<Person> validator =
 *     new ConditionalItemValidator<>(
 *         Person::isEmployed,
 *         employmentValidator);
 * }</pre>
 *
 * @param <T> the type of object being validated
 * @author Vaibhav Sonar
 */
@RequiredArgsConstructor
public class ConditionalItemValidator<T> implements ItemValidator<T> {

    /**
     * Predicate that determines whether the wrapped validator should be
     * executed.
     * <p>
     * When the predicate evaluates to {@code true}, validation is delegated to
     * the configured validator. When it evaluates to {@code false}, validation
     * is skipped.
     */
    private final Predicate<T> condition;

    /**
     * Item validator executed when {@link #condition} evaluates to
     * {@code true}.
     */
    private final ItemValidator<T> validator;

    /**
     * Validates the supplied object conditionally.
     * <p>
     * The configured condition is evaluated against the object. If the
     * condition evaluates to {@code true}, validation is delegated to the
     * wrapped validator and its result is returned unchanged.
     *
     * <p>If the condition evaluates to {@code false}, the wrapped validator is
     * not executed and an empty {@link ItemValidationResult} is returned.
     *
     * <p>The supplied {@code index} is passed unchanged to the wrapped
     * validator when validation is delegated.
     *
     * @param objectToValidate the object to validate
     * @param index            the index associated with the validation operation
     * @return the validation result produced by the wrapped validator when the
     * condition is satisfied; otherwise an empty validation result
     */
    @Override
    public ItemValidationResult validate(T objectToValidate, int index) {
        if (condition.test(objectToValidate)) {
            return validator.validate(objectToValidate, index);
        }
        return new ItemValidationResult();
    }
}
