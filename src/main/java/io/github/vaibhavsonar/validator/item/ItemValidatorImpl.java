package io.github.vaibhavsonar.validator.item;

import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.constants.ValidationType;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import io.github.vaibhavsonar.validator.rule.ItemValidationRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Default {@link ItemValidator} implementation that validates an entire item
 * using a configured set of {@link ItemValidationRule item validation rules}.
 * <p>
 * An {@code ItemValidatorImpl} evaluates the complete object being validated
 * against each configured item-level validation rule. Each rule is responsible
 * for determining whether the object violates its validation condition.
 *
 * <p>Only rules whose {@link ItemValidationRule#validationType()} is
 * {@link ValidationType#ITEM} are evaluated by this validator. Rules with
 * any other validation type are ignored.
 *
 * <p>When an applicable rule detects a validation failure, it produces an
 * {@link Error}. All errors produced by the applicable rules are accumulated
 * into a single {@link ItemValidationResult}.
 *
 * <p>All applicable rules are evaluated. Validation does not stop after the
 * first validation failure.
 *
 * @param <T> the type of object being validated
 * @author Vaibhav Sonar
 */
@Slf4j
@RequiredArgsConstructor
public class ItemValidatorImpl<T> implements ItemValidator<T> {

    /**
     * Item-level validation rules executed by this validator.
     * <p>
     * Rules are evaluated in the order in which they appear in this list.
     * Only rules with {@link ValidationType#ITEM} as their validation type
     * are executed.
     */
    private final List<ItemValidationRule<T>> rules;

    /**
     * Validates the supplied object using the configured item validation
     * rules.
     * <p>
     * Each configured rule whose validation type is
     * {@link ValidationType#ITEM} is evaluated against the complete object.
     * Any validation errors produced by the applicable rules are accumulated
     * into the returned {@link ItemValidationResult}.
     *
     * <p>Rules with a validation type other than
     * {@link ValidationType#ITEM} are ignored.
     *
     * <p>All applicable rules are evaluated even when one or more previous
     * rules have produced validation errors.
     *
     * @param objectToValidate the object to validate
     * @param index            the index associated with the object being validated
     * @return an {@link ItemValidationResult} containing all validation errors
     * produced by the applicable item validation rules
     */
    @Override
    public ItemValidationResult validate(T objectToValidate, int index) {
        ItemValidationResult validationResult = new ItemValidationResult();

        List<Error> errors = new ArrayList<>();
        for (ItemValidationRule<T> rule : rules) {
            if (ValidationType.ITEM.equals(rule.validationType())) {
                Optional<Error> error = rule.validate(objectToValidate, objectToValidate, index);
                error.ifPresent(errors::add);
            }
        }

        if (!errors.isEmpty()) {
            validationResult.addErrors(errors, index);
        }

        return validationResult;
    }
}