package io.github.vaibhavsonar.validator.rule.builder;

import io.github.vaibhavsonar.validator.model.RuleIdentifier;
import io.github.vaibhavsonar.validator.rule.CollectionValidationRule;
import io.github.vaibhavsonar.validator.rule.CollectionValidationRuleImpl;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Builder for configuring validation rules that operate on an entire
 * collection.
 * <p>
 * A {@code CollectionRuleBuilder} provides a fluent API for defining one or
 * more collection-level validation rules. Each configured rule consists of
 * a {@link RuleIdentifier}, a field name, a predicate that evaluates the
 * complete collection, and a validation message.
 *
 * <p>The predicate follows a failure-oriented convention: when it evaluates
 * to {@code true}, the collection is considered to violate the validation
 * rule and the associated validation message is reported.
 *
 * <p>Multiple rules can be configured using {@link #check(String,
 * RuleIdentifier, Predicate, String)}. The configured rules are retained in
 * the order in which they are added and can be obtained using
 * {@link #build()}.
 *
 * <p>Example:
 * <pre>{@code
 * CollectionRuleBuilder<Employee> rules =
 *         CollectionRuleBuilder.newInstance();
 *
 * rules
 *     .check(
 *         "employees",
 *         EmployeeRule.DUPLICATE_ID,
 *         employees -> employees.stream()
 *                 .map(Employee::getId)
 *                 .distinct()
 *                 .count() != employees.size(),
 *         "Employee IDs must be unique"
 *     )
 *     .check(
 *         "employees",
 *         EmployeeRule.MINIMUM_EMPLOYEES,
 *         employees -> employees.size() < 2,
 *         "At least two employees are required"
 *     );
 * }</pre>
 *
 * @param <T> the type of items contained in the collection
 * @author Vaibhav Sonar
 */
@RequiredArgsConstructor
public class CollectionRuleBuilder<T> {

    /**
     * Collection-level validation rules configured by this builder.
     * <p>
     * Rules are stored in the order in which they are added and are returned
     * in that same order by {@link #build()}.
     */
    private final List<CollectionValidationRule<T>> rules = new ArrayList<>();

    /**
     * Creates a new, empty {@code CollectionRuleBuilder}.
     * <p>
     * Individual rule identifiers are supplied when validation rules are
     * added through {@link #check(String, RuleIdentifier, Predicate, String)}.
     *
     * @param <T> the type of items contained in the collection
     * @return a new, empty {@code CollectionRuleBuilder}
     */
    public static <T> CollectionRuleBuilder<T> newInstance() {
        return new CollectionRuleBuilder<>();
    }

    /**
     * Adds a collection-level validation rule.
     * <p>
     * The supplied predicate is evaluated against the complete collection
     * during validation. If the predicate evaluates to {@code true}, the
     * collection is considered to have failed the rule and an
     * {@link Error} containing the configured field name, rejected collection,
     * and validation message is produced.
     *
     * <p>The supplied {@link RuleIdentifier} identifies the validation rule
     * being added.
     *
     * @param fieldName      the name of the field or collection property
     *                       associated with a validation failure
     * @param ruleIdentifier the identifier of the validation rule
     * @param predicate      the predicate used to determine whether the
     *                       collection violates the validation rule
     * @param message        the validation message reported when the rule
     *                       fails
     * @return this builder for method chaining
     */
    public CollectionRuleBuilder<T> check(String fieldName,
                                          RuleIdentifier ruleIdentifier,
                                          Predicate<List<T>> predicate,
                                          String message) {
        rules.add(new CollectionValidationRuleImpl<>(
                ruleIdentifier,
                fieldName,
                predicate,
                message
        ));
        return this;
    }

    /**
     * Builds the configured collection validation rules.
     * <p>
     * The returned list is a copy of the rules configured on this builder.
     * Subsequent changes to this builder do not modify the returned list.
     *
     * @return a new list containing the collection validation rules configured
     * on this builder
     */
    public List<CollectionValidationRule<T>> build() {
        return new ArrayList<>(rules);
    }
}
