package io.github.vaibhavsonar.validator.item;

import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.model.RuleIdentifier;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A composite implementation of {@link ItemValidator} that executes multiple
 * validation rules and aggregates their results.
 * <p>
 * Validation rules are identified using {@link RuleIdentifier} and are executed
 * in the order they are registered. The validation result contains the combined
 * errors produced by all configured validators.
 *
 * <p>This validator also supports:
 * <ul>
 *     <li>Combining multiple validators into a single validator.</li>
 *     <li>Conditional skipping of validation using skip predicates.</li>
 *     <li>Validation of nested objects using dedicated validators.</li>
 * </ul>
 *
 * <p>If any configured skip predicate evaluates to {@code true}, validation is
 * skipped and an empty {@link ItemValidationResult} is returned.
 *
 * @param <T> the type of object being validated
 *
 * @author Vaibhav Sonar
 */
@Slf4j
public class CompositeItemValidator<T> implements ItemValidator<T> {

    /**
     * Validation rules associated with their identifiers.
     */
    private final Map<RuleIdentifier, ItemValidator<T>> rules = new HashMap<>();

    /**
     * Validators for nested objects grouped by the nested field name.
     */
    private final Map<String, Map<RuleIdentifier, ItemValidator<T>>> nestedValidators = new HashMap<>();

    /**
     * Predicates used to determine whether validation should be skipped.
     */
    private final Predicate<T>[] skipPredicates;

    /**
     * Creates a composite validator with optional skip predicates.
     *
     * <p>If any predicate evaluates to {@code true} for the object being
     * validated, validation is skipped.
     *
     * @param skipObjectToValidateBasedOn predicates that determine whether
     *                                   validation should be skipped
     */
    public CompositeItemValidator(Predicate<T>... skipObjectToValidateBasedOn) {
        this.skipPredicates = skipObjectToValidateBasedOn == null
                ? new Predicate[0]
                : skipObjectToValidateBasedOn;
    }

    /**
     * Registers a validation rule.
     *
     * @param ruleIdentifier the unique identifier of the validation rule
     * @param validator the validator associated with the rule
     * @return this validator for method chaining
     */
    public CompositeItemValidator<T> addRule(RuleIdentifier ruleIdentifier, ItemValidator<T> validator) {
        rules.put(ruleIdentifier, validator);
        return this;
    }

    /**
     * Registers all validation rules from another composite validator.
     *
     * @param compositeValidator the validator whose rules should be added
     * @return this validator for method chaining
     */
    public CompositeItemValidator<T> addRules(CompositeItemValidator<T> compositeValidator) {
        compositeValidator.rules.forEach(this::addRule);
        return this;
    }

    /**
     * Registers a validator for a nested object.
     * <p>
     * If the nested object is {@code null}, validation is skipped.
     * Otherwise, the supplied validator is executed for the nested object.
     *
     * @param ruleIdentifier the unique identifier of the nested validation rule
     * @param fieldName the name of the nested field
     * @param getter function used to retrieve the nested object
     * @param validator validator responsible for validating the nested object
     * @param <R> the nested object type
     * @return this validator for method chaining
     */
    public <R> CompositeItemValidator<T> addNested(
            RuleIdentifier ruleIdentifier,
            String fieldName,
            Function<T, R> getter,
            ItemValidator<R> validator) {

        nestedValidators
                .computeIfAbsent(fieldName, k -> new HashMap<>())
                .put(ruleIdentifier, (objectToValidate, rowNumber) -> {
                    R nestedObject = getter.apply(objectToValidate);
                    if(Objects.isNull(nestedObject)) {
                        return new ItemValidationResult();
                    }
                    ItemValidationResult nestedResult = validator.validate(nestedObject, rowNumber);
                    prefixNestedFields(nestedResult, fieldName);
                    return nestedResult;
                });

        return this;
    }

    /**
     * Validates the supplied object by executing all registered validation
     * rules and nested validators.
     *
     * <p>If any configured skip predicate evaluates to {@code true}, validation
     * is skipped and an empty validation result is returned.
     *
     * @param objectToValidate the object to validate
     * @param index the row number associated with the object
     * @return the aggregated validation result
     */
    @Override
    public ItemValidationResult validate(T objectToValidate, int index) {
        if (objectToValidate != null
                && Arrays.stream(skipPredicates)
                .filter(Objects::nonNull)
                .anyMatch(p -> p.test(objectToValidate))) {

            return new ItemValidationResult(); // skip validation
        }

        ItemValidationResult validationResult = new ItemValidationResult();
        validationResult.addErrorsFrom(apply(rules, objectToValidate, index));

        nestedValidators.forEach((ruleId, ruleMap) -> {
            validationResult.addErrorsFrom(apply(ruleMap, objectToValidate, index));
        });

        return validationResult;
    }

    /**
     * Executes the supplied validation rules and aggregates their results.
     *
     * <p>The execution of each rule is logged before and after validation.
     *
     * @param rules the validation rules to execute
     * @param objectToValidate the object to validate
     * @param rowNumber the row number associated with the object
     * @return the combined validation result
     */
    private ItemValidationResult apply(
            Map<RuleIdentifier, ItemValidator<T>> rules,
            T objectToValidate,
            int rowNumber) {

        ItemValidationResult validationResult = new ItemValidationResult();

        rules.entrySet()
                .stream()
                .map(entry -> {
                    log.info(
                            "Validating rule = [{}], Row number = [{}]",
                            entry.getKey().ruleIdentifier(),
                            rowNumber);

                    ItemValidationResult result =
                            entry.getValue().validate(objectToValidate, rowNumber);

                    log.info(
                            "Validated rule = [{}], Row number = [{}], Validation Result = [{}]",
                            entry.getKey().ruleIdentifier(),
                            rowNumber,
                            result);

                    return result;
                })
                .toList()
                .forEach(validationResult::addErrorsFrom);

        return validationResult;
    }

    private void prefixNestedFields(ItemValidationResult result, String fieldName) {
        result.errors().values().forEach(errors ->
                errors.forEach(error -> {
                    if (error.getField() != null) {
                        error.setField(fieldName + "." + error.getField());
                    }
                }));
    }
}