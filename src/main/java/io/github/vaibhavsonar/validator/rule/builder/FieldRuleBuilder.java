package io.github.vaibhavsonar.validator.rule.builder;

import io.github.vaibhavsonar.validator.model.RuleIdentifier;
import io.github.vaibhavsonar.validator.rule.FieldValidationRule;
import io.github.vaibhavsonar.validator.rule.FieldValidationRuleImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Builder for configuring validation rules for fields of an item.
 * <p>
 * A {@code FieldRuleBuilder} provides a fluent API for defining one or more
 * {@link FieldValidationRule field validation rules}. Each configured rule
 * contains a {@link RuleIdentifier}, the name of the field being validated,
 * a function used to extract the field value, a validation {@link Predicate},
 * and a validation message.
 *
 * <p>The field getter extracts the value from the item during validation.
 * The predicate is then evaluated against the extracted field value. When the
 * predicate evaluates to {@code true}, the field is considered to have
 * violated the validation rule and the associated validation message is
 * reported.
 *
 * <p>The builder can be used to configure multiple rules for the same field
 * or for different fields, provided their corresponding field types are
 * compatible with the builder's type parameters.
 *
 * <p>Example:
 * <pre>{@code
 * FieldRuleBuilder<Person, Integer> rules =
 *         FieldRuleBuilder.newInstance();
 *
 * rules
 *     .check(
 *         PersonRule.AGE_REQUIRED,
 *         "age",
 *         Person::getAge,
 *         Objects::isNull,
 *         "Age is required"
 *     )
 *     .check(
 *         PersonRule.AGE_INVALID,
 *         "age",
 *         Person::getAge,
 *         age -> age != null && age < 18,
 *         "Age must be at least 18"
 *     );
 * }</pre>
 *
 * @param <T> the type of item containing the fields being validated
 * @param <F> the type of field value validated by this builder
 * @author Vaibhav Sonar
 */
public class FieldRuleBuilder<T, F> {

    /**
     * Field validation rules configured by this builder.
     * <p>
     * Rules are stored in the order in which they are added and are returned
     * in that same order by {@link #build()}.
     */
    private final List<FieldValidationRule<T, F>> rules = new ArrayList<>();

    /**
     * Creates a new, empty {@code FieldRuleBuilder}.
     *
     * @param <T> the type of item containing the fields being validated
     * @param <F> the type of field value being validated
     * @return a new {@code FieldRuleBuilder}
     */
    public static <T, F> FieldRuleBuilder<T, F> newInstance() {
        return new FieldRuleBuilder<>();
    }

    /**
     * Adds a field validation rule.
     * <p>
     * The supplied getter is used to extract the field value from the item
     * during validation. The extracted value is then passed to the supplied
     * predicate.
     *
     * <p>The predicate follows a failure-oriented convention: when it
     * evaluates to {@code true}, the field is considered to violate the
     * validation rule and a validation error containing the supplied message
     * is produced.
     *
     * @param ruleIdentifier the identifier of the validation rule
     * @param fieldName      the name of the field being validated
     * @param getter         function used to extract the field value from the item
     * @param predicate      predicate used to determine whether the extracted field
     *                       value violates the validation rule
     * @param message        validation message reported when the rule fails
     * @return this builder for method chaining
     */
    public FieldRuleBuilder<T, F> check(RuleIdentifier ruleIdentifier,
                                        String fieldName,
                                        Function<T, F> getter,
                                        Predicate<F> predicate,
                                        String message) {
        rules.add(new FieldValidationRuleImpl<>(
                ruleIdentifier,
                fieldName,
                getter,
                predicate,
                message
        ));
        return this;
    }

    /**
     * Builds the configured field validation rules.
     * <p>
     * The returned list is a copy of the rules configured on this builder.
     * Subsequent changes to this builder do not modify the returned list.
     *
     * @return a new list containing the configured field validation rules
     */
    public List<FieldValidationRule<T, F>> build() {
        return new ArrayList<>(rules);
    }
}
