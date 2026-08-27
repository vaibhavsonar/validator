package io.github.vaibhavsonar.validator.collection;

import io.github.vaibhavsonar.validator.CollectionValidator;
import io.github.vaibhavsonar.validator.rule.builder.CollectionRuleBuilder;

/**
 * Builder for creating {@link CollectionValidator} instances.
 * <p>
 * A {@code CollectionValidatorBuilder} provides a fluent API for configuring
 * validation rules that operate on an entire collection of items.
 *
 * <p>Collection-level validation is intended for constraints that require
 * access to multiple items simultaneously and therefore cannot be evaluated
 * against an individual item. Examples include duplicate detection,
 * uniqueness checks, collection size constraints, and consistency checks
 * across multiple items.
 *
 * <p>Collection validation rules are configured using a
 * {@link CollectionRuleBuilder}. Each configured rule builder is converted
 * into a {@link CollectionValidatorImpl} and registered with the internal
 * {@link CompositeCollectionValidator}. All registered validators are
 * combined into a single {@link CollectionValidator} when {@link #build()}
 * is called.
 *
 * <p>Example usage:
 * <pre>{@code
 * CollectionRuleBuilder<Employee> rules =
 *         CollectionRuleBuilder.<Employee>newInstance(
 *                 EmployeeRule.DUPLICATE_ID);
 *
 * rules.check(
 *         "employees",
 *         employees -> employees.stream()
 *                 .map(Employee::getId)
 *                 .distinct()
 *                 .count() != employees.size(),
 *         "Employee IDs must be unique");
 *
 * CollectionValidator<Employee> validator =
 *         CollectionValidatorBuilder.<Employee>newInstance()
 *                 .collection(rules)
 *                 .build();
 * }</pre>
 *
 * @param <T> the type of items contained in the collection
 * @author Vaibhav Sonar
 */
public class CollectionValidatorBuilder<T> {

    /**
     * Composite validator that stores and executes all collection validators
     * configured through this builder.
     */
    private final CompositeCollectionValidator<T> compositeCollectionValidator = new CompositeCollectionValidator<>();

    /**
     * Creates a new {@code CollectionValidatorBuilder}.
     *
     * @param <T> the type of items contained in the collection
     * @return a new, empty {@code CollectionValidatorBuilder}
     */
    public static <T> CollectionValidatorBuilder<T> newInstance() {
        return new CollectionValidatorBuilder<>();
    }

    /**
     * Builds a {@link CollectionValidator} containing all collection validators
     * configured on this builder.
     * <p>
     * The returned validator is the internal
     * {@link CompositeCollectionValidator}, which executes all collection
     * validators registered through {@link #collection(CollectionRuleBuilder)}.
     *
     * @return the configured collection validator
     */
    public CollectionValidator<T> build() {
        return compositeCollectionValidator;
    }

    /**
     * Registers a configured collection rule builder.
     * <p>
     * The supplied {@link CollectionRuleBuilder} contains one or more
     * collection-level validation rules. These rules are built into a
     * {@link CollectionValidatorImpl} and registered with the internal
     * {@link CompositeCollectionValidator}.
     *
     * <p>The supplied rule builder is not retained by this method. Its
     * configured rules are obtained through
     * {@link CollectionRuleBuilder#build()} and used to create the
     * corresponding collection validator.
     *
     * @param collectionRuleBuilder the configured builder containing the
     *                              collection validation rules to register
     * @return this builder for method chaining
     */
    public CollectionValidatorBuilder<T> collection(CollectionRuleBuilder<T> collectionRuleBuilder) {
        compositeCollectionValidator.addRule(new CollectionValidatorImpl<>(
                collectionRuleBuilder.build()));
        return this;
    }
}
