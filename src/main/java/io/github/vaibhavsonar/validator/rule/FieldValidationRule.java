package io.github.vaibhavsonar.validator.rule;

import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.model.RuleIdentifier;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Defines a validation rule for a specific field of an object.
 * <p>
 * A {@code FieldValidationRule} is a specialization of
 * {@link ValidationRule} in which the value being validated is a field value
 * of type {@code F}. The parent object is of type {@code T}.
 *
 * <p>A field validation rule associates a {@link RuleIdentifier} with the
 * name of the field, a {@link Function} used to extract the field value, a
 * validation {@link Predicate}, and a validation message.
 *
 * <p>During validation, the configured getter extracts the field value from
 * the parent object. The extracted value is then evaluated by the predicate.
 * The predicate follows a failure-oriented convention: when it evaluates to
 * {@code true}, the field is considered to violate the validation rule and
 * the rule produces a validation {@link Error}. When it evaluates to
 * {@code false}, the field passes the rule and no error is produced.
 *
 * <p>This rule represents
 * {@link io.github.vaibhavsonar.validator.constants.ValidationType#FIELD field-level validation}.
 *
 * @param <T> the type of object containing the field being validated
 * @param <F> the type of the field value being validated
 * @author Vaibhav Sonar
 */
public interface FieldValidationRule<T, F> extends ValidationRule<T, F> {

    /**
     * Returns the name of the field associated with this validation rule.
     * <p>
     * The field name is used to identify the validation target when a
     * validation error is produced.
     *
     * @return the name of the field being validated
     */
    String fieldName();

    /**
     * Returns the function used to extract the field value from the parent
     * object.
     * <p>
     * The getter receives an object of type {@code T} and returns the
     * corresponding field value of type {@code F}.
     *
     * @return the field value extraction function
     */
    Function<T, F> getter();

    /**
     * Returns the predicate used to validate the field value.
     * <p>
     * The predicate follows a failure-oriented convention. It must return
     * {@code true} when the field value violates the validation rule and a
     * validation error should be reported.
     *
     * @return the field validation predicate
     */
    Predicate<F> predicate();
}
