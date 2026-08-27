package io.github.vaibhavsonar.validator.rule;

import io.github.vaibhavsonar.validator.constants.ValidationType;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.model.RuleIdentifier;

import java.util.Optional;

/**
 * Defines the common contract for a validation rule.
 * <p>
 * A {@code ValidationRule} represents a validation operation performed within
 * the context of a parent object. The rule is identified by a
 * {@link RuleIdentifier}, declares its {@link ValidationType}, and evaluates
 * a value of type {@code V}.
 *
 * <p>The parent object and validation value are represented separately so
 * that a rule can operate either on the complete object or on a value
 * extracted from that object. This allows the same validation rule contract
 * to support different validation scopes such as item-level and
 * field-level validation.
 *
 * <p>A validation rule produces an {@link Error} when its validation logic
 * detects a validation failure. When validation succeeds, the rule returns
 * an empty {@link Optional}.
 *
 * @param <T> the type of the parent object or validation context
 * @param <V> the type of value being validated
 * @author Vaibhav Sonar
 */
public interface ValidationRule<T, V> {
    /**
     * Returns the identifier of this validation rule.
     * <p>
     * The identifier provides a stable way to distinguish the rule from other
     * validation rules.
     *
     * @return the identifier of this validation rule
     */
    RuleIdentifier ruleIdentifier();

    /**
     * Returns the validation message associated with this rule.
     * <p>
     * The message is included in the {@link Error} produced when the rule
     * detects a validation failure.
     *
     * @return the validation message
     */
    String message();

    /**
     * Returns the validation scope represented by this rule.
     *
     * @return the {@link ValidationType} of this rule
     */
    ValidationType validationType();

    /**
     * Validates the supplied value within the context of its parent object.
     * <p>
     * The {@code parentObject} provides the object or context from which the
     * value being validated originates. The {@code objectToValidate} contains
     * the actual value evaluated by the rule.
     *
     * <p>The {@code index} identifies the item or validation position
     * associated with the operation and can be propagated to the resulting
     * validation result.
     *
     * <p>If the validation fails, the rule returns an {@link Optional}
     * containing the corresponding {@link Error}. If validation succeeds,
     * {@link Optional#empty()} is returned.
     *
     * @param parentObject     the parent object or validation context
     * @param objectToValidate the value being validated
     * @param index            the index associated with the validation operation
     * @return an {@link Optional} containing a validation error when
     * validation fails; otherwise an empty {@link Optional}
     */
    Optional<Error> validate(T parentObject, V objectToValidate, int index);
}