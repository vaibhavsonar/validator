package io.github.vaibhavsonar.validator.rule;

import io.github.vaibhavsonar.validator.constants.ValidationType;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.model.RuleIdentifier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Default {@link CollectionValidationRule} implementation for predicate-based
 * validation of an entire collection.
 * <p>
 * A {@code CollectionValidationRuleImpl} associates a
 * {@link RuleIdentifier} with a field name, a collection
 * {@link Predicate}, and a validation message. The predicate is evaluated
 * against the complete collection supplied for validation.
 *
 * <p>The predicate follows a failure-oriented convention: when it evaluates
 * to {@code true}, the collection is considered to violate the validation
 * rule and an {@link Error} is produced. When it evaluates to {@code false},
 * the collection passes the rule and no error is produced.
 *
 * <p>The resulting validation error contains the configured field name, the
 * collection that failed validation as the rejected value, and the configured
 * validation message.
 *
 * <p>This implementation represents collection-level validation and therefore
 * returns {@link ValidationType#COLLECTION} from
 * {@link #validationType()}.
 *
 * @param <T> the type of items contained in the collection
 * @author Vaibhav Sonar
 */
@Slf4j
@Getter
@RequiredArgsConstructor
public class CollectionValidationRuleImpl<T> implements CollectionValidationRule<T> {

    /**
     * Identifier of this validation rule.
     * <p>
     * The identifier provides a stable way to distinguish this rule from other
     * validation rules.
     */
    private final RuleIdentifier ruleIdentifier;

    /**
     * Name of the field or collection property associated with this rule.
     * <p>
     * This value is used as the field name when a validation error is
     * produced.
     */
    private final String fieldName;

    /**
     * Predicate used to validate the collection.
     * <p>
     * The predicate receives the complete collection and follows a
     * failure-oriented convention. It must return {@code true} when the
     * collection violates the validation rule and a validation error should
     * be reported.
     */
    private final Predicate<List<T>> predicate;

    /**
     * Human-readable message associated with this validation rule.
     * <p>
     * The message is included in the {@link Error} produced when the predicate
     * detects a validation failure.
     */
    private final String message;

    /**
     * Returns the identifier of this validation rule.
     *
     * @return the identifier of this validation rule
     */
    @Override
    public RuleIdentifier ruleIdentifier() {
        return ruleIdentifier;
    }

    /**
     * Returns the field or collection property associated with this rule.
     *
     * @return the field or collection property name
     */
    @Override
    public String fieldName() {
        return fieldName;
    }

    /**
     * Returns the predicate used to validate the collection.
     *
     * @return the collection validation predicate
     */
    @Override
    public Predicate<List<T>> predicate() {
        return predicate;
    }

    /**
     * Returns the validation message associated with this rule.
     *
     * @return the validation message
     */
    @Override
    public String message() {
        return message;
    }

    /**
     * Returns the validation scope represented by this rule.
     *
     * @return {@link ValidationType#COLLECTION}, indicating collection-level
     * validation
     */
    @Override
    public ValidationType validationType() {
        return ValidationType.COLLECTION;
    }

    /**
     * Validates the supplied collection using the configured predicate.
     * <p>
     * The {@code parentObject} provides the parent validation context, while
     * {@code objectToValidate} contains the complete collection being
     * validated. The collection is evaluated using the configured
     * {@link #predicate}.
     *
     * <p>If the predicate evaluates to {@code true}, an {@link Error} is
     * created using the configured field name, rejected collection, and
     * validation message.
     *
     * <p>If the predicate evaluates to {@code false}, the collection passes
     * this rule and an empty {@link Optional} is returned.
     *
     * @param parentObject     the parent object or validation context; not used
     *                         directly by this collection-level rule
     * @param objectToValidate the complete collection to validate
     * @param index            the index associated with the validation operation
     * @return an {@link Optional} containing the validation error when the
     * collection violates the rule; otherwise an empty
     * {@link Optional}
     */
    @Override
    public Optional<Error> validate(T parentObject, List<T> objectToValidate, int index) {
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
