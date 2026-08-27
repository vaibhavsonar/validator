package io.github.vaibhavsonar.validator.rule;

import io.github.vaibhavsonar.validator.model.Error;

import java.util.List;
import java.util.function.Predicate;

/**
 * Defines a validation rule that operates on an entire collection of items.
 * <p>
 * A {@code CollectionValidationRule} is a specialized validation rule whose
 * validation value is a complete {@link List} of items rather than an
 * individual item or field value.
 *
 * <p>The rule uses a {@link Predicate} that receives the complete collection.
 * The predicate follows a failure-oriented convention: when it evaluates to
 * {@code true}, the collection is considered to violate the validation rule
 * and a validation {@link Error} is produced. When it evaluates to
 * {@code false}, the collection passes the rule.
 *
 * <p>Collection-level validation is intended for constraints that require
 * multiple items to be considered together, such as duplicate detection,
 * uniqueness checks, minimum or maximum collection size, and cross-item
 * consistency checks.
 *
 * @param <T> the type of items contained in the collection
 * @author Vaibhav Sonar
 */
public interface CollectionValidationRule<T> extends ValidationRule<T, List<T>> {

    /**
     * Returns the name of the field or collection property associated with
     * this validation rule.
     * <p>
     * The field name is used to identify the validation target when a
     * validation error is produced.
     *
     * @return the field or collection property name associated with this rule
     */
    String fieldName();

    /**
     * Returns the predicate used to validate the collection.
     * <p>
     * The predicate receives the complete collection and follows a
     * failure-oriented convention. It must return {@code true} when the
     * collection violates the validation rule and a validation error should
     * be reported.
     *
     * @return the predicate used to evaluate the collection
     */
    Predicate<List<T>> predicate();
}
