package io.github.vaibhavsonar.validator.rule;

import io.github.vaibhavsonar.validator.constants.ValidationType;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.model.RuleIdentifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Default implementation of {@link FieldValidationRule} for predicate-based
 * validation of a field value.
 * <p>
 * A {@code FieldValidationRuleImpl} associates a
 * {@link RuleIdentifier} with a field name, a field-value extraction
 * {@link Function}, a validation {@link Predicate}, and a validation message.
 *
 * <p>During validation, the configured getter extracts the field value from
 * the parent object. The extracted value is then evaluated by the configured
 * predicate.
 *
 * <p>The predicate follows a failure-oriented convention: when
 * {@link Predicate#test(Object)} returns {@code true}, the field is considered
 * to violate the validation rule and an {@link Error} is produced. When the
 * predicate returns {@code false}, the field passes the rule and no error is
 * produced.
 *
 * <p>The resulting {@link Error} contains the configured field name, the
 * extracted field value as the rejected value, and the configured validation
 * message.
 *
 * <p>This implementation represents field-level validation and therefore
 * returns {@link ValidationType#FIELD} from {@link #validationType()}.
 *
 * @param <T> the type of object containing the field being validated
 * @param <F> the type of field value being validated
 * @author Vaibhav Sonar
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class FieldValidationRuleImpl<T, F> implements FieldValidationRule<T, F> {

    /**
     * Identifier of this validation rule.
     * <p>
     * The identifier provides a stable way to distinguish this rule from
     * other validation rules.
     */
    private final RuleIdentifier ruleIdentifier;

    /**
     * Name of the field being validated.
     * <p>
     * This value identifies the validation target and is included in the
     * {@link Error} produced when the rule fails.
     */
    private final String fieldName;

    /**
     * Function used to extract the field value from the parent object.
     * <p>
     * The getter receives an object of type {@code T} and returns the
     * corresponding field value of type {@code F}.
     */
    private final Function<T, F> getter;

    /**
     * Predicate used to evaluate the extracted field value.
     * <p>
     * The predicate follows a failure-oriented convention. It must return
     * {@code true} when the field value violates the validation rule and a
     * validation error should be reported.
     */
    private final Predicate<F> predicate;

    /**
     * Human-readable message associated with this validation rule.
     * <p>
     * The message is included in the {@link Error} produced when the
     * validation rule fails.
     */
    private final String message;

    /**
     * {@inheritDoc}
     */
    @Override
    public RuleIdentifier ruleIdentifier() {
        return ruleIdentifier;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String fieldName() {
        return fieldName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Function<T, F> getter() {
        return getter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Predicate<F> predicate() {
        return predicate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String message() {
        return message;
    }

    /**
     * Returns the validation scope represented by this rule.
     *
     * @return {@link ValidationType#FIELD}, indicating field-level validation
     */
    @Override
    public ValidationType validationType() {
        return ValidationType.FIELD;
    }

    /**
     * Validates the field associated with the supplied parent object.
     * <p>
     * The configured {@link #getter} extracts the field value from
     * {@code parentObject}. The extracted value is then evaluated using the
     * configured {@link #predicate}.
     *
     * <p>If the predicate returns {@code true}, the field violates the
     * validation rule and an {@link Error} is created containing the field
     * name, rejected field value, and validation message.
     *
     * <p>If the predicate returns {@code false}, the field passes this rule
     * and an empty {@link Optional} is returned.
     *
     * <p>The {@code objectToValidate} parameter represents the field value
     * supplied by the caller. This implementation obtains the field value
     * directly from {@code parentObject} using the configured getter.
     *
     * @param parentObject     the object containing the field being validated
     * @param objectToValidate the field value associated with the validation
     *                         operation
     * @param index            the index associated with the validation operation
     * @return an {@link Optional} containing the validation error when the
     * field violates the rule; otherwise an empty {@link Optional}
     */
    @Override
    public Optional<Error> validate(T parentObject, F objectToValidate, int index) {
        log.info("Validating rule [{}] for index [{}]", ruleIdentifier, index);
        if (predicate.test(objectToValidate)) {
            Error error = new Error()
                    .setField(fieldName)
                    .setRejectedValue(objectToValidate)
                    .setMessage(message);
            log.info("During validation of rule [{}] for index [{}], an error found [{}]",
                    ruleIdentifier, error, index);
            return Optional.of(error);
        }
        log.info("Validated rule [{}] for index [{}]", ruleIdentifier, index);
        return Optional.empty();
    }
}