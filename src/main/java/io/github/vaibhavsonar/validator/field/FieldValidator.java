package io.github.vaibhavsonar.validator.field;

import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import io.github.vaibhavsonar.validator.rule.ValidationRule;
import lombok.RequiredArgsConstructor;
import io.github.vaibhavsonar.validator.model.Error;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Default {@link ItemValidator} implementation that validates a single field of
 * an object.
 * <p>
 * A {@code FieldValidator} extracts a field value from the object using the
 * supplied getter function and evaluates it against a sequence of
 * {@link ValidationRule PredicateRules}. Each rule that evaluates to
 * {@code true} produces a validation error.
 *
 * <p>All validation errors generated for the field are accumulated and returned
 * as an {@link ItemValidationResult}. Validation rules are executed in the
 * order they are provided.
 *
 * @param <T> the type of object being validated
 * @param <R> the type of the field being validated
 *
 * @author Vaibhav Sonar
 */
@Slf4j
@RequiredArgsConstructor
public class FieldValidator<T, R> implements ItemValidator<T> {

    /**
     * Name of the field being validated.
     */
    private final String fieldName;

    /**
     * Function used to extract the field value from the object being validated.
     */
    private final Function<T, R> getter;

    /**
     * Validation rules applied to the extracted field value.
     */
    private final List<ValidationRule<R>> rules;

    /**
     * Validates the configured field of the supplied object.
     * <p>
     * The field value is extracted using the configured getter and evaluated
     * against each configured validation rule. Every rule that reports a
     * validation failure contributes an {@link Error} to the returned
     * validation result.
     *
     * @param objectToValidate the object whose field is to be validated
     * @param index the row number associated with the object
     * @return the validation result containing any validation errors for the
     *         field
     */
    @Override
    public ItemValidationResult validate(T objectToValidate, int index) {
        R value = getter.apply(objectToValidate);
        ItemValidationResult validationResult = new ItemValidationResult();

        List<Error> errors = new ArrayList<>();
        for (ValidationRule<R> rule: rules) {
            log.info("Validating rule [{}] for row number [{}]", rule.getRuleIdentifier(), index);
            if(rule.getPredicate().test(value)) {
                Error error = new Error()
                        .setField(fieldName)
                        .setRejectedValue(value)
                        .setMessage(rule.getMessage());
                errors.add(error);
                log.info("During validation of rule [{}] for row number [{}], an error found [{}]",
                        rule.getRuleIdentifier(), error, index);
            }
            log.info("Validated rule [{}] for row number [{}]", rule.getRuleIdentifier(), index);
        }

        if (!errors.isEmpty()) {
            validationResult.addErrors(errors, index);
        }

        return validationResult;
    }
}