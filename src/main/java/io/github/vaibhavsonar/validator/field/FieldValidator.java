package io.github.vaibhavsonar.validator.field;

import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.constants.ValidationType;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import io.github.vaibhavsonar.validator.rule.FieldValidationRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Validates fields of an object using a configured set of
 * {@link FieldValidationRule field validation rules}.
 * <p>
 * A {@code FieldValidator} executes the configured field validation rules
 * against the object being validated. Each rule is responsible for extracting
 * its configured field value and evaluating the field against its validation
 * predicate.
 *
 * <p>Only rules whose {@link FieldValidationRule#validationType()} is
 * {@link ValidationType#FIELD} are evaluated. Rules with any other validation
 * type are ignored.
 *
 * <p>When a field validation rule detects a validation failure, it produces an
 * {@link Error}. All errors produced by the applicable rules are accumulated
 * into a single {@link ItemValidationResult}.
 *
 * <p>All applicable rules are evaluated. Validation does not stop after the
 * first validation failure.
 *
 * @param <T> the type of object being validated
 * @param <F> the type of field value validated by the configured rules
 * @author Vaibhav Sonar
 */
@Slf4j
@RequiredArgsConstructor
public class FieldValidator<T, F> implements ItemValidator<T> {

    /**
     * Field validation rules executed by this validator.
     * <p>
     * Rules are evaluated in the order in which they appear in this list.
     * Only rules with {@link ValidationType#FIELD} as their validation type
     * are executed.
     */
    private final List<FieldValidationRule<T, F>> rules;

    /**
     * Validates the configured field rules against the supplied object.
     * <p>
     * Each configured rule whose validation type is
     * {@link ValidationType#FIELD} is evaluated against the supplied object.
     * The rule extracts its field value using its configured getter and
     * performs the corresponding field validation.
     *
     * <p>If a rule produces a validation error, the error is accumulated in
     * the returned result. All applicable rules are evaluated even when one
     * or more previous rules have failed.
     *
     * <p>Rules with a validation type other than
     * {@link ValidationType#FIELD} are ignored.
     *
     * @param objectToValidate the object whose fields are to be validated
     * @param index            the index associated with the object being validated
     * @return an {@link ItemValidationResult} containing all validation errors
     * produced by the applicable field validation rules
     */
    @Override
    public ItemValidationResult validate(T objectToValidate, int index) {
        ItemValidationResult validationResult =
                new ItemValidationResult();

        List<Error> errors = new ArrayList<>();

        for (FieldValidationRule<T, F> rule : rules) {
            if (ValidationType.FIELD.equals(rule.validationType())) {
                F value = rule.getter().apply(objectToValidate);

                Optional<Error> error =
                        rule.validate(objectToValidate, value, index);

                error.ifPresent(errors::add);
            }
        }

        if (!errors.isEmpty()) {
            validationResult.addErrors(errors, index);
        }

        return validationResult;
    }
}