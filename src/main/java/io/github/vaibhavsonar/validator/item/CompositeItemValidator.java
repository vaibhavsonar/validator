package io.github.vaibhavsonar.validator.item;

import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Composite implementation of {@link ItemValidator} that combines multiple
 * item validators and aggregates their validation results.
 * <p>
 * A {@code CompositeItemValidator} maintains an ordered collection of
 * {@link ItemValidator item validators}. Each registered validator is executed
 * against the supplied object, and the validation errors produced by all
 * applicable validators are aggregated into a single
 * {@link ItemValidationResult}.
 *
 * <p>This validator also supports:
 * <ul>
 *     <li>Combining validators from another
 *         {@code CompositeItemValidator}.</li>
 *     <li>Conditionally skipping validation using one or more predicates.</li>
 *     <li>Validating nested objects using dedicated
 *         {@link ItemValidator} instances.</li>
 * </ul>
 *
 * <p>Nested validation errors are automatically prefixed with the name of the
 * nested field using dot notation. For example, an error for {@code city}
 * produced while validating an {@code address} object is reported as
 * {@code address.city}.
 *
 * <p>If the object being validated is not {@code null} and any configured
 * skip predicate evaluates to {@code true}, all validation is skipped and an
 * empty {@link ItemValidationResult} is returned.
 *
 * @param <T> the type of object being validated
 * @author Vaibhav Sonar
 */
@Slf4j
public class CompositeItemValidator<T> implements ItemValidator<T> {

    /**
     * Item validators registered with this composite validator.
     * <p>
     * Validators are stored in registration order and are executed in that
     * same order when {@link #validate(Object, int)} is invoked.
     */
    private final List<ItemValidator<T>> validators = new ArrayList<>();

    /**
     * Validators responsible for validating nested objects.
     * <p>
     * The map is keyed by the name of the nested field. Each field can have
     * multiple validators associated with it.
     */
    private final Map<String, List<ItemValidator<T>>> nestedValidators = new HashMap<>();

    /**
     * Predicates used to determine whether validation should be skipped.
     * <p>
     * When the object is not {@code null}, each non-null predicate is evaluated
     * against it. If any predicate returns {@code true}, validation is skipped.
     */
    private final Predicate<T>[] skipPredicates;

    /**
     * Creates a composite validator with optional validation skip predicates.
     * <p>
     * During validation, each non-null predicate is evaluated against the
     * object being validated. If any predicate evaluates to {@code true},
     * validation is skipped and an empty result is returned.
     *
     * @param skipObjectToValidateBasedOn predicates that determine whether
     *                                    validation should be skipped
     */
    @SafeVarargs
    public CompositeItemValidator(Predicate<T>... skipObjectToValidateBasedOn) {
        this.skipPredicates = skipObjectToValidateBasedOn == null
                ? new Predicate[0]
                : skipObjectToValidateBasedOn;
    }

    /**
     * Registers an item validator with this composite validator.
     * <p>
     * The supplied validator is appended to the collection of registered
     * validators and is executed when this composite validator performs
     * validation.
     *
     * @param validator the item validator to register
     * @return this validator for method chaining
     */
    public CompositeItemValidator<T> addRule(ItemValidator<T> validator) {
        validators.add(validator);
        return this;
    }

    /**
     * Registers all validators from another composite validator.
     * <p>
     * The validators contained in the supplied composite are appended to this
     * validator in their existing order. Validators already registered with
     * this composite are retained.
     *
     * @param compositeValidator the composite validator whose registered
     *                           validators should be added
     * @return this validator for method chaining
     */
    public CompositeItemValidator<T> addRules(CompositeItemValidator<T> compositeValidator) {
        validators.addAll(compositeValidator.validators);
        return this;
    }

    /**
     * Registers a validator for a nested object.
     * <p>
     * The nested object is obtained from the parent object using the supplied
     * getter. If the getter returns {@code null}, nested validation is skipped.
     *
     * <p>If the nested object is not {@code null}, the supplied validator is
     * executed against it. Validation errors produced by the nested validator
     * have their field names prefixed with the supplied nested field name using
     * dot notation.
     *
     * <p>For example, if the nested field is {@code address} and the nested
     * validator produces an error for {@code city}, the resulting field name
     * is {@code address.city}.
     *
     * @param fieldName the name of the nested field
     * @param getter    function used to retrieve the nested object
     * @param validator validator responsible for validating the nested object
     * @param <R>       the type of the nested object
     * @return this validator for method chaining
     */
    public <R> CompositeItemValidator<T> addNested(
            String fieldName,
            Function<T, R> getter,
            ItemValidator<R> validator) {

        nestedValidators.computeIfAbsent(fieldName, k -> new ArrayList<>());

        nestedValidators.compute(fieldName, (field, validators) -> {
            if (Objects.isNull(validators)) {
                validators = new ArrayList<>();
            }
            validators.add((objectToValidate, index) -> {
                R nestedObject = getter.apply(objectToValidate);
                if (Objects.isNull(nestedObject)) {
                    return new ItemValidationResult();
                }
                ItemValidationResult nestedResult = validator.validate(nestedObject, index);
                prefixNestedFields(nestedResult, fieldName);
                return nestedResult;
            });
            return validators;
        });

        return this;
    }

    /**
     * Validates the supplied object using all registered item validators and
     * nested validators.
     * <p>
     * If the object is not {@code null} and any configured skip predicate
     * evaluates to {@code true}, validation is skipped and an empty
     * {@link ItemValidationResult} is returned.
     *
     * <p>Otherwise, all registered item validators are executed and their
     * validation errors are aggregated. Nested validators are then executed
     * for their corresponding nested fields, provided the nested objects are
     * not {@code null}.
     *
     * @param objectToValidate the object to validate
     * @param index            the index associated with the object being validated
     * @return the aggregated validation result containing errors produced by
     * all applicable validators
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
        validationResult.addErrorsFrom(apply(validators, objectToValidate, index));

        nestedValidators.forEach((fieldName, ruleMap) -> {
            validationResult.addErrorsFrom(apply(ruleMap, objectToValidate, index));
        });

        return validationResult;
    }

    /**
     * Executes the supplied validators and aggregates their validation
     * results.
     * <p>
     * Each validator is executed with the same object and index. The
     * validation errors produced by every validator are combined into a
     * single {@link ItemValidationResult}.
     *
     * @param validators       the validators to execute
     * @param objectToValidate the object to validate
     * @param index            the index associated with the validation operation
     * @return the aggregated validation result produced by the supplied
     * validators
     */
    private ItemValidationResult apply(
            List<ItemValidator<T>> validators,
            T objectToValidate,
            int index) {
        ItemValidationResult validationResult = new ItemValidationResult();
        validators.stream()
                .map(validator -> validator.validate(objectToValidate, index))
                .toList()
                .forEach(validationResult::addErrorsFrom);
        return validationResult;
    }

    /**
     * Prefixes field names in validation errors produced by a nested validator.
     * <p>
     * Existing field names are prefixed with the nested field name using dot
     * notation. Errors that do not have a field name are left unchanged.
     *
     * @param result    the validation result produced by the nested validator
     * @param fieldName the name of the nested field to prepend
     */
    private void prefixNestedFields(ItemValidationResult result, String fieldName) {
        result.errors().values().forEach(errors ->
                errors.forEach(error -> {
                    if (error.getField() != null) {
                        error.setField(fieldName + "." + error.getField());
                    }
                }));
    }
}