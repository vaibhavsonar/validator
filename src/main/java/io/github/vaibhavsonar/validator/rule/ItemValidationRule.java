package io.github.vaibhavsonar.validator.rule;

import java.util.function.Predicate;

/**
 * Defines a validation rule that operates on an entire item.
 * <p>
 * An {@code ItemValidationRule} is a specialization of
 * {@link ValidationRule} in which the parent object and the value being
 * validated are both of type {@code T}. The rule therefore evaluates the
 * complete item rather than an individual field.
 *
 * <p>The validation predicate follows a failure-oriented convention: when
 * it evaluates to {@code true}, the item is considered to violate the
 * validation rule and a validation error is produced. When it evaluates to
 * {@code false}, the item passes the rule.
 *
 * <p>This rule represents {@link io.github.vaibhavsonar.validator.constants.ValidationType#ITEM item-level validation}.
 *
 * @param <T> the type of item being validated
 * @author Vaibhav Sonar
 */
public interface ItemValidationRule<T> extends ValidationRule<T, T> {

    /**
     * Returns the predicate used to validate the complete item.
     * <p>
     * The predicate receives the entire item and follows a failure-oriented
     * convention. It must return {@code true} when the item violates the
     * validation rule and a validation error should be reported.
     *
     * @return the item validation predicate
     */
    Predicate<T> predicate();


}
