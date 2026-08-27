package io.github.vaibhavsonar.validator.collection;

import io.github.vaibhavsonar.validator.CollectionValidator;
import io.github.vaibhavsonar.validator.constants.ValidationType;
import io.github.vaibhavsonar.validator.result.CollectionValidationResult;
import io.github.vaibhavsonar.validator.rule.CollectionValidationRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Default {@link CollectionValidator} implementation that executes
 * collection-level validation rules against a collection of items.
 * <p>
 * A {@code CollectionValidatorImpl} receives a collection of
 * {@link CollectionValidationRule collection validation rules} and evaluates
 * each rule whose {@link ValidationType} is
 * {@link ValidationType#COLLECTION}.
 *
 * <p>Each rule is evaluated against the supplied collection. When a rule
 * reports a validation error, the error is added to the returned
 * {@link CollectionValidationResult} using the supplied index.
 *
 * <p>This class is responsible for executing configured collection validation
 * rules and aggregating their results. Rule configuration is handled by
 * {@link io.github.vaibhavsonar.validator.rule.builder.CollectionRuleBuilder} and higher-level composition is handled by
 * {@link CompositeCollectionValidator}.
 *
 * @param <T> the type of items contained in the collection
 * @author Vaibhav Sonar
 */
@Slf4j
@RequiredArgsConstructor
public class CollectionValidatorImpl<T> implements CollectionValidator<T> {

    /**
     * Collection-level validation rules executed by this validator.
     * <p>
     * Only rules whose {@link ValidationType} is
     * {@link ValidationType#COLLECTION} are executed.
     */
    private final List<CollectionValidationRule<T>> rules;

    /**
     * Validates the supplied collection using all configured collection-level
     * validation rules.
     * <p>
     * Each configured rule is checked for
     * {@link ValidationType#COLLECTION}. Matching rules are evaluated against
     * the supplied collection. Any validation error produced by a rule is
     * added to the resulting {@link CollectionValidationResult}.
     *
     * <p>Rules that have a validation type other than
     * {@link ValidationType#COLLECTION} are ignored.
     *
     * @param items the collection of items to validate
     * @param index the index associated with the validation operation
     * @return the aggregated collection validation result
     */
    @Override
    public CollectionValidationResult validate(List<T> items, int index) {
        CollectionValidationResult result =
                new CollectionValidationResult();

        for (CollectionValidationRule<T> rule : rules) {
            if (ValidationType.COLLECTION.equals(rule.validationType())) {
                rule.validate(null, items, index)
                        .ifPresent(error -> result.addError(error, index));
            }
        }

        return result;
    }
}