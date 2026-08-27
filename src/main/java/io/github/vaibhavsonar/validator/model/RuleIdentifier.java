package io.github.vaibhavsonar.validator.model;

/**
 * Identifies a validation rule.
 * <p>
 * A {@code RuleIdentifier} provides a stable, application-defined identifier
 * for a validation rule. It can be used to distinguish validation rules,
 * associate validation configuration with a rule, and identify a specific
 * validation rule when processing validation results or logs.
 *
 * <p>The interface is functional, allowing rule identifiers to be implemented
 * by enums, classes, or lambda expressions. Enums are recommended when an
 * application has a fixed set of validation rules because they provide
 * type-safe and consistent identifiers.
 *
 * <p>Example:
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
     * Returns the stable identifier of the validation rule.
     *
     * <p>The returned value should uniquely identify the rule within the
     * validation configuration where the identifier is used.
     *
     * @return the identifier of the validation rule
     */
    String ruleIdentifier();
}
