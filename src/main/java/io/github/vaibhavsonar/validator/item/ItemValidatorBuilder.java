package io.github.vaibhavsonar.validator.item;

import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.field.FieldValidator;
import io.github.vaibhavsonar.validator.rule.builder.FieldRuleBuilder;
import io.github.vaibhavsonar.validator.rule.builder.ItemRuleBuilder;

import java.util.function.Function;

/**
 * Builder for creating and configuring {@link ItemValidator} instances.
 * <p>
 * An {@code ItemValidatorBuilder} provides a fluent API for defining
 * validation for an object of type {@code T}. Validation can be configured
 * at the item level, at the field level, and for nested objects.
 *
 * <p>Field-level validation is configured through a
 * {@link FieldRuleBuilder}, while item-level validation is configured
 * through an {@link ItemRuleBuilder}. Nested object validation is delegated
 * to another {@link ItemValidator}.
 *
 * <p>All configured validators are registered with a
 * {@link CompositeItemValidator} and combined into a single
 * {@link ItemValidator} when {@link #build()} is called.
 *
 * <p>Example:
 * <pre>{@code
 * ItemValidator<Person> validator =
 *         ItemValidatorBuilder.<Person>newInstance()
 *                 .field(
 *                         PersonFieldRules.name(),
 *                         )
 *                 .item(
 *                         "person",
 *                         PersonItemRules.validation())
 *                 .nested(
 *                         "address",
 *                         Person::getAddress,
 *                         addressValidator)
 *                 .build();
 * }</pre>
 *
 * @param <T> the type of object being validated
 * @author Vaibhav Sonar
 */
public class ItemValidatorBuilder<T> {

    /**
     * Composite validator containing all validators configured through this
     * builder.
     * <p>
     * Field validators, item validators, and nested validators are registered
     * with this composite and executed when the resulting
     * {@link ItemValidator} is used for validation.
     */
    private final CompositeItemValidator<T> compositeItemValidator = new CompositeItemValidator<>();

    /**
     * Creates a new {@code ItemValidatorBuilder}.
     *
     * @param <T> the type of object to be validated
     * @return a new, empty {@code ItemValidatorBuilder}
     */
    public static <T> ItemValidatorBuilder<T> newInstance() {
        return new ItemValidatorBuilder<>();
    }

    /**
     * Builds an {@link ItemValidator} containing all validators configured
     * through this builder.
     * <p>
     * The returned validator delegates validation to the composite validator
     * maintained by this builder. All configured item, field, and nested
     * validators are therefore executed by the resulting validator.
     *
     * @return the configured item validator
     */
    public ItemValidator<T> build() {
        return compositeItemValidator;
    }

    /**
     * Registers a configured set of field validation rules.
     * <p>
     * The supplied {@link FieldRuleBuilder} contains the rules used to create
     * a {@link FieldValidator}. The resulting field validator is registered
     * with the composite item validator.
     *
     * <p>Multiple field validators can be registered by invoking this method
     * multiple times with different {@link FieldRuleBuilder} instances.
     *
     * @param fieldRuleBuilder the builder containing the field validation
     *                         rules to register
     * @param <R>              the type of the field value validated by the supplied rules
     * @return this builder for method chaining
     */
    public <R> ItemValidatorBuilder<T> field(FieldRuleBuilder<T, R> fieldRuleBuilder) {
        compositeItemValidator.addRule(
                new FieldValidator<>(
                        fieldRuleBuilder.build()));
        return this;
    }

    /**
     * Registers a configured set of item-level validation rules.
     * <p>
     * The supplied {@link ItemRuleBuilder} contains validation rules that
     * operate on the complete object being validated. The rules are used to
     * create an {@link ItemValidatorImpl}, which is then registered with the
     * composite item validator.
     *
     * @param itemRuleBuilder the builder containing the item-level validation
     *                        rules to register
     * @return this builder for method chaining
     */
    public ItemValidatorBuilder<T> item(
            ItemRuleBuilder<T> itemRuleBuilder) {
        compositeItemValidator.addRule(
                new ItemValidatorImpl<>(
                        itemRuleBuilder.build()));
        return this;
    }

    /**
     * Registers a validator for a nested object.
     * <p>
     * The supplied {@code getter} extracts the nested object from the parent
     * object. If the extracted nested object is {@code null}, nested
     * validation is skipped by the composite validator.
     *
     * <p>If the nested object is not {@code null}, the supplied
     * {@link ItemValidator} is executed against it.
     *
     * <p>Validation errors produced by the nested validator are propagated to
     * the parent validation result. Field names in nested validation errors
     * are automatically prefixed with the nested field name using dot
     * notation.
     *
     * <p>For example, if the nested field is {@code address} and the nested
     * validator reports an error for {@code city}, the resulting field name
     * becomes {@code address.city}.
     *
     * @param fieldName       the name of the nested field
     * @param getter          function used to extract the nested object from the parent
     *                        object
     * @param nestedValidator validator used to validate the nested object
     * @param <R>             the type of the nested object
     * @return this builder for method chaining
     */
    public <R> ItemValidatorBuilder<T> nested(
            String fieldName,
            Function<T, R> getter,
            ItemValidator<R> nestedValidator) {
        compositeItemValidator.addNested(
                fieldName,
                getter,
                nestedValidator
        );
        return this;
    }
}