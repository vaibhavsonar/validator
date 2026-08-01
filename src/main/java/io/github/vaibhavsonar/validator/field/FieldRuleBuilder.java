package io.github.vaibhavsonar.validator.field;

import io.github.vaibhavsonar.validator.model.RuleIdentifier;
import io.github.vaibhavsonar.validator.rule.ValidationRule;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Builder for configuring validation rules for a field.
 * <p>
 * A {@code FieldRuleBuilder} provides a fluent API for defining one or more
 * validation rules for a field. Each rule consists of a unique
 * {@link RuleIdentifier}, a validation predicate, and an error message that is
 * reported when the predicate indicates a validation failure.
 *
 * <p>Instances of this builder are typically created and managed by
 * {@code ItemValidatorBuilder} and supplied to callers through a configuration
 * callback.
 *
 * <p>Example usage:
 * <pre>{@code
 * builder.field("age", Person::getAge, rules ->
 *     rules
 *         .check(PersonRule.AGE_REQUIRED,
 *                Objects::isNull,
 *                "Age is required")
 *         .check(PersonRule.AGE_INVALID,
 *                age -> age != null && age < 18,
 *                "Age must be at least 18"));
 * }</pre>
 *
 * @param <R> the type of the field being validated
 *
 * @author Vaibhav Sonar
 */
public class FieldRuleBuilder<R> {

    /**
     * Validation rules configured for the field.
     */
    @Getter
    private final List<ValidationRule<R>> rules = new ArrayList<>();

    /**
     * Adds a validation rule for the field.
     * <p>
     * The supplied predicate is evaluated against the field value during
     * validation. If the predicate indicates a validation failure, the
     * specified message is included in the validation result.
     *
     * @param ruleIdentifier the unique identifier of the validation rule
     * @param predicate the predicate used to validate the field value
     * @param message the validation message reported when the rule fails
     * @return this builder for method chaining
     */
    public FieldRuleBuilder<R> check(RuleIdentifier ruleIdentifier, Predicate<R> predicate, String message) {
        rules.add(new ValidationRule<>(ruleIdentifier, predicate, message));
        return this;
    }
}
