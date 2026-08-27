package io.github.vaibhavsonar.validator.rule;

import io.github.vaibhavsonar.validator.constants.ValidationType;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.model.RuleIdentifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.Predicate;

/**
 * Default implementation of {@link ItemValidationRule} for predicate-based
 * validation of an entire item.
 * <p>
 * An {@code ItemValidationRuleImpl} associates a {@link RuleIdentifier} with
 * a {@link Predicate} and a human-readable validation message. The predicate
 * is evaluated against the complete item being validated.
 *
 * <p>The predicate follows a failure-oriented convention: when it evaluates
 * to {@code true}, the item is considered to violate the validation rule and
 * an {@link Error} is produced. When it evaluates to {@code false}, the item
 * passes the rule and no validation error is produced.
 *
 * <p>The resulting {@link Error} identifies the validation target as
 * {@code "this"}, because the rule applies to the complete item rather than
 * to a specific field. The rejected value is the item that failed
 * validation.
 *
 * <p>This implementation represents item-level validation and therefore
 * returns {@link ValidationType#ITEM} from {@link #validationType()}.
 *
 * @param <T> the type of item being validated
 * @author Vaibhav Sonar
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class ItemValidationRuleImpl<T> implements ItemValidationRule<T> {

    /**
     * Identifier of this validation rule.
     * <p>
     * The identifier provides a stable way to distinguish this rule from
     * other validation rules.
     */
    private final RuleIdentifier ruleIdentifier;

    /**
     * Predicate used to validate the complete item.
     * <p>
     * The predicate follows a failure-oriented convention. It must return
     * {@code true} when the item violates the validation rule and a
     * validation error should be reported.
     */
    private final Predicate<T> predicate;

    /**
     * Human-readable message associated with this validation rule.
     * <p>
     * The message is included in the {@link Error} produced when the
     * validation rule fails.
     */
    private final String message;

    /**
     * Returns the validation scope represented by this rule.
     *
     * @return {@link ValidationType#ITEM}, indicating item-level validation
     */
    @Override
    public ValidationType validationType() {
        return ValidationType.ITEM;
    }

    /**
     * Validates the supplied item using the configured predicate.
     * <p>
     * Because this is an item-level rule, both {@code parentObject} and
     * {@code objectToValidate} are of type {@code T}. The complete item is
     * evaluated by the configured {@link #predicate}.
     *
     * <p>If the predicate returns {@code true}, an {@link Error} is created
     * with {@code "this"} as the field, the item as the rejected value, and
     * the configured validation message.
     *
     * <p>If the predicate returns {@code false}, the item passes this rule and
     * an empty {@link Optional} is returned.
     *
     * @param parentObject     the parent object or validation context; for an
     *                         item-level rule, this is the item being validated
     * @param objectToValidate the item being validated; for an item-level
     *                         rule, this is the same logical value as
     *                         {@code parentObject}
     * @param index            the index associated with the validation operation
     * @return an {@link Optional} containing the validation error when the
     * item violates the rule; otherwise an empty {@link Optional}
     */
    @Override
    public Optional<Error> validate(T parentObject, T objectToValidate, int index) {
        log.info("Validating rule [{}] for index [{}]", ruleIdentifier, index);
        if (predicate.test(objectToValidate)) {
            Error error = new Error()
                    .setField("this")
                    .setRejectedValue(objectToValidate)
                    .setMessage(message);
            log.info("During validation of rule [{}] for index [{}], an error found [{}]",
                    ruleIdentifier, error, index);
            return Optional.of(error);
        }
        log.info("Validated rule [{}] for index [{}]", ruleIdentifier, index);
        return Optional.empty();
    }

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
    public Predicate<T> predicate() {
        return predicate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String message() {
        return message;
    }
}