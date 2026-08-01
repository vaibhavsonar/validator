package io.github.vaibhavsonar.validator.rule;

import io.github.vaibhavsonar.validator.model.RuleIdentifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Predicate;

/**
 * Represents a single predicate-based validation rule.
 * <p>
 * A {@code PredicateRule} combines a validation predicate with a unique rule
 * identifier and an error message. The predicate is evaluated against a field
 * value during validation, and if it indicates a validation failure, the
 * associated message is reported as a validation error.
 *
 * <p>Multiple {@code PredicateRule} instances can be associated with a field
 * and are evaluated independently.
 *
 * @param <R> the type of value validated by this rule
 *
 * @author Vaibhav Sonar
 */
@Getter
@RequiredArgsConstructor
public class ValidationRule<R> {

    /**
     * Unique identifier for this validation rule.
     */
    private final RuleIdentifier ruleIdentifier;

    /**
     * Predicate used to evaluate the field value.
     * <p>
     * The predicate should return {@code true} when the value violates the
     * validation rule and a validation error should be reported.
     */
    private final Predicate<R> predicate;

    /**
     * Error message reported when the predicate indicates a validation
     * failure.
     */
    private final String message;
}