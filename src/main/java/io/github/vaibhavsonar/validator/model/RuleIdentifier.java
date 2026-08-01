package io.github.vaibhavsonar.validator.model;

/**
 * Represents a unique identifier for a validation rule.
 * <p>
 * A {@code RuleIdentifier} is used to uniquely identify individual validation
 * rules within a validator. It can be implemented using an enum, lambda
 * expression, or any class that provides a unique identifier.
 *
 * <p>Using an enum is recommended for type safety and consistency:
 * <pre>{@code
 * public enum PersonRule implements RuleIdentifier {
 *     NAME_REQUIRED,
 *     AGE_RANGE;
 *
 *     @Override
 *     public String ruleIdentifier() {
 *         return name();
 *     }
 * }
 * }</pre>
 *
 * @author Vaibhav Sonar
 */
@FunctionalInterface
public interface RuleIdentifier {

    /**
     * Returns the unique identifier for the validation rule.
     *
     * @return the unique rule identifier
     */
    String ruleIdentifier();
}
