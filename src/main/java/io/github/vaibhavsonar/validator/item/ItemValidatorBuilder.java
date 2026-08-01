package io.github.vaibhavsonar.validator.item;

import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.field.FieldRuleBuilder;
import io.github.vaibhavsonar.validator.field.FieldValidator;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Builder for creating {@link ItemValidator} instances.
 * <p>
 * An {@code ItemValidatorBuilder} provides a fluent API for defining validation
 * rules for a single object. Validation rules can be configured for individual
 * fields as well as nested objects, and are executed in the order they are
 * added to the builder.
 *
 * <p>Example usage:
 * <pre>{@code
 * ItemValidator<Person> validator = ItemValidatorBuilder
 *         .consider(Person.class)
 *         .field("name", Person::getName,
 *                 rule -> rule.notBlank("Name is required"))
 *         .field("age", Person::getAge,
 *                 rule -> rule.greaterThanOrEqualTo(18, "Age must be at least 18"))
 *         .build();
 * }</pre>
 *
 * @param <T> the type of object being validated
 *
 * @author Vaibhav Sonar
 */
public class ItemValidatorBuilder<T> {

    private final List<ItemValidator<T>> validators = new ArrayList<>();

    /**
     * Creates a new validator builder for the specified type.
     *
     * @param clazz the class of the object to be validated
     * @param <T> the object type
     * @return a new {@code ItemValidatorBuilder}
     */
    public static <T> ItemValidatorBuilder<T> consider(Class<T> clazz) {
        return new ItemValidatorBuilder<>();
    }

    /**
     * Builds an {@link ItemValidator} using all configured validation rules.
     *
     * <p>The returned validator executes each configured validator in the order
     * they were added and combines all validation errors into a single
     * {@link ItemValidationResult}.
     *
     * @return a fully configured {@link ItemValidator}
     */
    public ItemValidator<T> build() {
        return (objectToValidate, rowNumber) -> {
            ItemValidationResult validationResult = new ItemValidationResult();
            validators.forEach(v -> {
                ItemValidationResult result = v.validate(objectToValidate, rowNumber);
                validationResult.addErrorsFrom(result);
            });
            return validationResult;
        };
    }

    /**
     * Configures validation rules for a field.
     *
     * <p>The supplied consumer receives a {@link FieldRuleBuilder} that can be
     * used to define one or more validation rules for the field.
     *
     * @param fieldName the name of the field
     * @param getter function that extracts the field value from the object
     * @param consumer configures the validation rules for the field
     * @param <R> the field type
     * @return this builder
     */
    public <R> ItemValidatorBuilder<T> field(
            String fieldName,
            Function<T, R> getter,
            Consumer<FieldRuleBuilder<R>> consumer) {

        FieldRuleBuilder<R> fieldRuleBuilder = new FieldRuleBuilder<>();
        consumer.accept(fieldRuleBuilder);

        validators.add(
                new FieldValidator<>(
                        fieldName,
                        getter,
                        fieldRuleBuilder.getRules()));

        return this;
    }

    /**
     * Adds a validator for a nested object.
     *
     * <p>If the nested object is {@code null}, validation is skipped.
     * Otherwise, the supplied validator is executed and any validation errors
     * are propagated to the parent validation result. Nested field names are
     * automatically prefixed using dot notation.
     *
     * <p>For example, if the nested validator reports an error for field
     * {@code city} and the nested field name is {@code address}, the resulting
     * field name becomes {@code address.city}.
     *
     * @param fieldName the name of the nested field
     * @param getter function that extracts the nested object
     * @param nestedValidator validator used to validate the nested object
     * @param <R> the nested object type
     * @return this builder
     */
    public <R> ItemValidatorBuilder<T> nested(
            String fieldName,
            Function<T, R> getter,
            ItemValidator<R> nestedValidator) {

        validators.add((objectToValidate, rowNumber) -> {

            R nested = getter.apply(objectToValidate);
            ItemValidationResult validationResult = new ItemValidationResult();

            if (Objects.isNull(nested)) {
                return validationResult;
            }

            ItemValidationResult nestedResult =
                    nestedValidator.validate(nested, rowNumber);

            nestedResult.errors().forEach((row, rowErrors) -> {
                List<Error> updatedErrors = rowErrors.stream()
                        .map(error -> new Error()
                                .setField(fieldName + "." + error.getField())
                                .setRejectedValue(error.getRejectedValue())
                                .setMessage(error.getMessage()))
                        .toList();

                validationResult.addErrors(updatedErrors, row);
            });

            return validationResult;
        });

        return this;
    }
}