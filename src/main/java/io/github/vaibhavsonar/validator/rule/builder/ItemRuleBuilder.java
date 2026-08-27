package io.github.vaibhavsonar.validator.rule.builder;

import io.github.vaibhavsonar.validator.model.RuleIdentifier;
import io.github.vaibhavsonar.validator.rule.ItemValidationRule;
import io.github.vaibhavsonar.validator.rule.ItemValidationRuleImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Builder for configuring validation rules that operate on an entire item.
 * <p>
 * An {@code ItemRuleBuilder} provides a fluent API for defining one or more
 * item-level validation rules. Each configured rule consists of a
 * {@link RuleIdentifier}, a {@link Predicate} that evaluates the complete
 * item, and a validation message that is reported when the predicate indicates
 * a validation failure.
 *
 * <p>The predicate follows a failure-oriented convention: when it evaluates
 * to {@code true}, the item is considered to violate the validation rule and
 * the associated validation message is reported. When it evaluates to
 * {@code false}, the item passes that rule.
 *
 * <p>Multiple independent item-level rules can be configured using successive
 * calls to {@link #check(RuleIdentifier, String, Predicate, String)}. The rules are
 * retained in registration order and returned by {@link #build()}.
 *
 * <p>The builder can be created either directly using its public constructor
 * or through the {@link #newInstance()} factory method.
 *
 * <p>Example:
 * <pre>{@code
 * ItemRuleBuilder<Person> rules =
 *         ItemRuleBuilder.newInstance();
 *
 * rules
 *     .check(
 *         PersonRule.AGE_INVALID,
 *         person -> person.getAge() < 18,
 *         "Person must be at least 18 years old"
 *     )
 *     .check(
 *         PersonRule.NAME_AND_AGE_INVALID,
 *         person -> person.getName() == null && person.getAge() == null,
 *         "Name and age cannot both be null"
 *     );
 *
 * List<ItemValidationRule<Person>> validationRules = rules.build();
 * }</pre>
 *
 * @param <T> the type of item being validated
 * @author Vaibhav Sonar
 */
public class ItemRuleBuilder<T> {

    /**
     * Item-level validation rules configured by this builder.
     * <p>
     * Rules are stored in the order in which they are added and are returned
     * in that same order by {@link #build()}.
     */
    private final List<ItemValidationRule<T>> rules = new ArrayList<>();

    /**
     * Creates a new, empty {@code ItemRuleBuilder}.
     * <p>
     * The returned builder contains no validation rules. Rules can be added
     * using {@link #check(RuleIdentifier, String, Predicate, String)}.
     *
     * @param <T> the type of item to be validated
     * @return a new, empty {@code ItemRuleBuilder}
     */
    public static <T> ItemRuleBuilder<T> newInstance() {
        return new ItemRuleBuilder<>();
    }

    /**
     * Adds an item-level validation rule.
     * <p>
     * The supplied predicate is evaluated against the complete item during
     * validation. If the predicate evaluates to {@code true}, the item is
     * considered to have violated the validation rule and the specified
     * validation message is reported.
     *
     * @param ruleIdentifier the identifier of the validation rule
     * @param fieldName the field name for which the rule is being applied
     * @param predicate      the predicate used to determine whether the item
     *                       violates the validation rule
     * @param message        the validation message reported when the rule fails
     * @return this builder for method chaining
     */
    public ItemRuleBuilder<T> check(RuleIdentifier ruleIdentifier,
                                    String fieldName,
                                    Predicate<T> predicate,
                                    String message) {
        rules.add(new ItemValidationRuleImpl<>(
                ruleIdentifier,
                fieldName,
                predicate,
                message
        ));
        return this;
    }

    /**
     * Builds the configured item validation rules.
     * <p>
     * The returned list is a defensive copy of the rules configured on this
     * builder. Subsequent changes to this builder do not modify the returned
     * list, and modifications to the returned list do not modify the builder's
     * internal rule collection.
     *
     * @return a new list containing the configured item validation rules
     */
    public List<ItemValidationRule<T>> build() {
        return new ArrayList<>(rules);
    }
}
