package io.github.vaibhavsonar.validator.collection;

import io.github.vaibhavsonar.validator.CollectionValidator;
import io.github.vaibhavsonar.validator.model.RuleIdentifier;
import io.github.vaibhavsonar.validator.result.CollectionValidationResult;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A composite implementation of {@link CollectionValidator} that executes
 * multiple collection validation rules and aggregates their results.
 * <p>
 * Validation rules are identified using {@link RuleIdentifier} and are
 * executed in the order they are registered. The validation result contains
 * the combined errors produced by all configured collection validators.
 *
 * <p>This validator is intended for collection-level validation scenarios,
 * such as duplicate detection, cross-record validation, uniqueness checks,
 * and other rules that require access to the entire collection.
 *
 * @param <T> the type of objects contained in the collection
 *
 * @author Vaibhav Sonar
 */
@Slf4j
public class CompositeCollectionValidator<T> implements CollectionValidator<T> {

    /**
     * Collection validation rules associated with their identifiers.
     */
    private final Map<RuleIdentifier, CollectionValidator<T>> rules = new HashMap<>();

    /**
     * Registers a collection validation rule.
     *
     * @param ruleIdentifier the unique identifier of the validation rule
     * @param validator the validator associated with the rule
     * @return this validator for method chaining
     */
    public CompositeCollectionValidator<T> addRule(RuleIdentifier ruleIdentifier, CollectionValidator<T> validator) {
        rules.put(ruleIdentifier, validator);
        return this;
    }

    /**
     * Registers all validation rules from another composite collection validator.
     * <p>
     * All rules configured in the supplied validator are added to this validator.
     * If a rule with the same {@link RuleIdentifier} already exists, it is
     * replaced by the corresponding rule from the supplied validator.
     *
     * @param compositeValidator the composite validator whose validation rules
     *                           should be added
     * @return this validator for method chaining
     */
    public CompositeCollectionValidator<T> addRules(
            CompositeCollectionValidator<T> compositeValidator) {

        compositeValidator.rules.forEach(this::addRule);
        return this;
    }

    /**
     * Validates the supplied collection by executing all registered
     * collection validation rules.
     *
     * <p>The execution of each rule is logged before and after validation.
     * The validation errors produced by all configured validators are
     * aggregated into a single {@link CollectionValidationResult}.
     *
     * @param objectToValidate the collection to validate
     * @return the aggregated validation result
     */
    @Override
    public CollectionValidationResult validate(List<T> objectToValidate) {
        CollectionValidationResult validationResult = new CollectionValidationResult();
        rules.forEach((ruleId, validator) -> {
            log.info("Validating rule = [{}]", ruleId.ruleIdentifier());
            CollectionValidationResult result = validator.validate(objectToValidate);
            validationResult.addErrorsFrom(result);
            log.info("Validated rule = [{}], Validation Result = [{}]", ruleId.ruleIdentifier(), result);
        });
        return validationResult;
    }
}