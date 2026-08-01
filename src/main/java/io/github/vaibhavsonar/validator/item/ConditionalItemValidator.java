package io.github.vaibhavsonar.validator.item;

import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

/**
 * An {@link ItemValidator} that executes another validator only when a
 * specified condition is satisfied.
 * <p>
 * This validator enables conditional validation scenarios where validation
 * rules should be applied only if the object being validated meets a specific
 * criterion.
 *
 * <p>If the condition evaluates to {@code false}, validation is skipped and an
 * empty {@link ItemValidationResult} is returned.
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
 *
 * @author Vaibhav Sonar
 */
@RequiredArgsConstructor
public class ConditionalItemValidator<T> implements ItemValidator<T> {

    /**
     * Predicate that determines whether the wrapped validator should be
     * executed.
     */
    private final Predicate<T> condition;

    /**
     * Validator to execute when the condition evaluates to {@code true}.
     */
    private final ItemValidator<T> validator;

    /**
     * Validates the supplied object if the configured condition is satisfied.
     *
     * <p>If the condition evaluates to {@code false}, validation is skipped and
     * an empty validation result is returned.
     *
     * @param objectToValidate the object to validate
     * @param index the row number associated with the object
     * @return the validation result produced by the wrapped validator if the
     *         condition is satisfied; otherwise an empty
     *         {@link ItemValidationResult}
     */
    @Override
    public ItemValidationResult validate(T objectToValidate, int index) {
        if (condition.test(objectToValidate)) {
            return validator.validate(objectToValidate, index);
        }
        return new ItemValidationResult();
    }
}
