package io.github.vaibhavsonar.validator.item;

import io.github.vaibhavsonar.validator.helper.model.Address;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

class CompositeItemValidatorTest {

    private static final int INDEX = 1;

    @Test
    void validate_whenNoRules_shouldReturnSuccessfulResult() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        ItemValidationResult result =
                validator.validate(new Employee(), INDEX);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void addRule_shouldReturnSameValidatorForChaining() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        CompositeItemValidator<Employee> result = validator.addRule(
                (employee, index) -> new ItemValidationResult()
        );

        assertSame(validator, result);
    }

    @Test
    void addRule_shouldExecuteValidator() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addRule(
                (employee, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Id is required"),
                            index
                    );

                    return result;
                });

        ItemValidationResult result =
                validator.validate(new Employee(), INDEX);

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().get(INDEX).size());
        assertEquals(
                "id",
                result.errors().get(INDEX).get(0).getField()
        );
        assertEquals(
                "Id is required",
                result.errors().get(INDEX).get(0).getMessage()
        );
    }

    @Test
    void addRule_multipleValidatorsWithSameRuleIdentifier_shouldExecuteAllValidators() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addRule(
                (employee, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Id is required"),
                            index
                    );

                    return result;
                }
        );

        validator.addRule(
                (employee, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("name")
                                    .setMessage("Name is required"),
                            index
                    );

                    return result;
                }
        );

        ItemValidationResult result =
                validator.validate(new Employee(), INDEX);

        List<Error> errors = result.errors().get(INDEX);

        assertNotNull(errors);
        assertEquals(2, errors.size());

        assertTrue(
                errors.stream()
                        .anyMatch(error -> "id".equals(error.getField()))
        );

        assertTrue(
                errors.stream()
                        .anyMatch(error -> "name".equals(error.getField()))
        );
    }

    @Test
    void addRule_multipleValidatorsWithSameRuleIdentifier_shouldExecuteEachValidatorExactlyOnce() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        AtomicInteger executionCount = new AtomicInteger();

        validator.addRule(
                (employee, index) -> {
                    executionCount.incrementAndGet();
                    return new ItemValidationResult();
                }
        );

        validator.addRule(
                (employee, index) -> {
                    executionCount.incrementAndGet();
                    return new ItemValidationResult();
                }
        );

        validator.validate(new Employee(), INDEX);

        assertEquals(2, executionCount.get());
    }

    @Test
    void addRules_shouldMergeValidators() {
        CompositeItemValidator<Employee> first =
                new CompositeItemValidator<>();

        CompositeItemValidator<Employee> second =
                new CompositeItemValidator<>();

        second.addRule(
                (employee, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Id is required"),
                            index
                    );

                    return result;
                });

        first.addRules(second);

        ItemValidationResult result =
                first.validate(new Employee(), INDEX);

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().get(INDEX).size());
    }

    @Test
    void addRules_shouldPreserveExistingValidators() {
        CompositeItemValidator<Employee> first =
                new CompositeItemValidator<>();

        CompositeItemValidator<Employee> second =
                new CompositeItemValidator<>();

        first.addRule(
                (employee, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Id required"),
                            index
                    );

                    return result;
                }
        );

        second.addRule(
                (employee, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Id invalid"),
                            index
                    );

                    return result;
                }
        );

        first.addRules(second);

        ItemValidationResult result =
                first.validate(new Employee(), INDEX);

        assertEquals(2, result.errors().get(INDEX).size());
    }

    @Test
    void addRules_shouldReturnSameValidatorForChaining() {
        CompositeItemValidator<Employee> first =
                new CompositeItemValidator<>();

        CompositeItemValidator<Employee> second =
                new CompositeItemValidator<>();

        CompositeItemValidator<Employee> result =
                first.addRules(second);

        assertSame(first, result);
    }

    @Test
    void validate_multipleRules_shouldAggregateErrors() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addRule(
                (employee, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Required"),
                            index
                    );

                    return result;
                });

        validator.addRule(
                (employee, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Invalid"),
                            index
                    );

                    return result;
                });

        ItemValidationResult result =
                validator.validate(new Employee(), INDEX);

        assertEquals(2, result.errors().get(INDEX).size());
    }

    @Test
    void validate_whenValidatorReturnsSuccessfulResult_shouldRemainSuccessful() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addRule(
                (employee, index) -> new ItemValidationResult()
        );

        ItemValidationResult result =
                validator.validate(new Employee(), INDEX);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void addNested_shouldReturnSameValidatorForChaining() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        CompositeItemValidator<Employee> result =
                validator.addNested(
                        "address",
                        Employee::getAddress,
                        new CompositeItemValidator<>()
                );

        assertSame(validator, result);
    }

    @Test
    void addNested_shouldValidateNestedObject() {
        CompositeItemValidator<Address> addressValidator =
                new CompositeItemValidator<>();

        addressValidator.addRule(
                (address, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("country")
                                    .setMessage("Country required"),
                            index
                    );

                    return result;
                });

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addNested(
                "address",
                Employee::getAddress,
                addressValidator
        );

        Employee employee = new Employee()
                .setAddress(new Address());

        ItemValidationResult result =
                validator.validate(employee, INDEX);

        List<Error> errors = result.errors().get(INDEX);

        assertNotNull(errors);
        assertEquals(1, errors.size());
        assertEquals("address.country", errors.get(0).getField());
        assertEquals("Country required", errors.get(0).getMessage());
    }

    @Test
    void addNested_whenNestedObjectIsNull_shouldSkipNestedValidation() {
        CompositeItemValidator<Address> addressValidator =
                new CompositeItemValidator<>();

        addressValidator.addRule(
                (address, index) -> {
                    fail("Nested validator should not execute.");
                    return new ItemValidationResult();
                });

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addNested(
                "address",
                Employee::getAddress,
                addressValidator
        );

        Employee employee = new Employee()
                .setAddress(null);

        ItemValidationResult result =
                validator.validate(employee, INDEX);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void addNested_shouldPrefixNestedFieldName() {
        CompositeItemValidator<Address> addressValidator =
                new CompositeItemValidator<>();

        addressValidator.addRule(
                (address, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("country")
                                    .setMessage("Invalid country"),
                            index
                    );

                    return result;
                });

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addNested(
                "address",
                Employee::getAddress,
                addressValidator
        );

        Employee employee = new Employee()
                .setAddress(
                        new Address()
                                .setCountry("Invalid")
                );

        ItemValidationResult result =
                validator.validate(employee, INDEX);

        assertEquals(
                "address.country",
                result.errors().get(INDEX).get(0).getField()
        );
    }

    @Test
    void addNested_whenNestedValidatorProducesMultipleErrors_shouldPrefixAllFields() {
        CompositeItemValidator<Address> addressValidator =
                new CompositeItemValidator<>();

        addressValidator.addRule(
                (address, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("city")
                                    .setMessage("City invalid"),
                            index
                    );

                    result.addError(
                            new Error()
                                    .setField("country")
                                    .setMessage("Country invalid"),
                            index
                    );

                    return result;
                });

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addNested(
                "address",
                Employee::getAddress,
                addressValidator
        );

        Employee employee = new Employee()
                .setAddress(new Address());

        ItemValidationResult result =
                validator.validate(employee, INDEX);

        List<Error> errors = result.errors().get(INDEX);

        assertEquals(2, errors.size());

        assertTrue(
                errors.stream()
                        .anyMatch(error ->
                                "address.city".equals(error.getField()))
        );

        assertTrue(
                errors.stream()
                        .anyMatch(error ->
                                "address.country".equals(error.getField()))
        );
    }

    @Test
    void addNested_whenNestedErrorHasNoField_shouldNotPrefixField() {
        CompositeItemValidator<Address> addressValidator =
                new CompositeItemValidator<>();

        addressValidator.addRule(
                (address, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField(null)
                                    .setMessage("Address invalid"),
                            index
                    );

                    return result;
                });

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addNested(
                "address",
                Employee::getAddress,
                addressValidator
        );

        Employee employee = new Employee()
                .setAddress(new Address());

        ItemValidationResult result =
                validator.validate(employee, INDEX);

        Error error = result.errors().get(INDEX).get(0);

        assertNull(error.getField());
        assertEquals("Address invalid", error.getMessage());
    }

    @Test
    void validate_shouldAggregateParentAndNestedErrors() {
        CompositeItemValidator<Address> addressValidator =
                new CompositeItemValidator<>();

        addressValidator.addRule(
                (address, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("country")
                                    .setMessage("Country required"),
                            index
                    );

                    return result;
                });

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addRule(
                (employee, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Id required"),
                            index
                    );

                    return result;
                });

        validator.addNested(
                "address",
                Employee::getAddress,
                addressValidator
        );

        Employee employee = new Employee()
                .setAddress(new Address());

        ItemValidationResult result =
                validator.validate(employee, INDEX);

        List<Error> errors = result.errors().get(INDEX);

        assertEquals(2, errors.size());

        assertTrue(
                errors.stream()
                        .anyMatch(error -> "id".equals(error.getField()))
        );

        assertTrue(
                errors.stream()
                        .anyMatch(error ->
                                "address.country".equals(error.getField()))
        );
    }

    @Test
    void validate_whenSkipPredicateMatches_shouldSkipValidation() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>(employee -> true);

        validator.addRule(
                (employee, index) -> {
                    fail("Validator should not execute.");
                    return new ItemValidationResult();
                });

        ItemValidationResult result =
                validator.validate(new Employee(), INDEX);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validate_whenSkipPredicateDoesNotMatch_shouldExecuteValidation() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>(employee -> false);

        validator.addRule(
                (employee, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Required"),
                            index
                    );

                    return result;
                });

        ItemValidationResult result =
                validator.validate(new Employee(), INDEX);

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().get(INDEX).size());
    }

    @Test
    void validate_whenMultipleSkipPredicates_shouldSkipWhenAnyPredicateMatches() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>(
                        employee -> false,
                        employee -> true,
                        employee -> false
                );

        validator.addRule(
                (employee, index) -> {
                    fail("Validator should not execute.");
                    return new ItemValidationResult();
                });

        ItemValidationResult result =
                validator.validate(new Employee(), INDEX);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validate_whenAllSkipPredicatesDoNotMatch_shouldExecuteValidation() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>(
                        employee -> false,
                        employee -> false
                );

        validator.addRule(
                (employee, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Required"),
                            index
                    );

                    return result;
                });

        ItemValidationResult result =
                validator.validate(new Employee(), INDEX);

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().get(INDEX).size());
    }

    @Test
    void validate_whenSkipPredicatesContainNull_shouldIgnoreNullPredicates() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>(
                        null,
                        employee -> false,
                        null
                );

        validator.addRule(
                (employee, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Required"),
                            index
                    );

                    return result;
                });

        ItemValidationResult result =
                validator.validate(new Employee(), INDEX);

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().get(INDEX).size());
    }

    @Test
    void validate_whenSkipPredicatesAreNull_shouldExecuteValidation() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>((Predicate<Employee>[]) null);

        validator.addRule(
                (employee, index) -> {
                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Required"),
                            index
                    );

                    return result;
                });

        ItemValidationResult result =
                validator.validate(new Employee(), INDEX);

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().get(INDEX).size());
    }

    @Test
    void validate_whenObjectIsNull_shouldNotEvaluateSkipPredicates() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>(
                        employee -> {
                            fail("Skip predicate should not execute for null object.");
                            return true;
                        }
                );

        ItemValidationResult result =
                validator.validate(null, INDEX);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validate_whenObjectIsNull_shouldExecuteRegisteredValidators() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addRule(
                (employee, index) -> {
                    assertNull(employee);

                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Employee is required"),
                            index
                    );

                    return result;
                });

        ItemValidationResult result =
                validator.validate(null, INDEX);

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().get(INDEX).size());
    }

    @Test
    void validate_shouldPropagateIndex() {
        int index = 15;

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addRule(
                (employee, errorIndex) -> {
                    assertEquals(index, errorIndex);

                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("id")
                                    .setMessage("Required"),
                            index
                    );

                    return result;
                });

        ItemValidationResult result =
                validator.validate(new Employee(), index);

        assertTrue(result.errors().containsKey(index));
        assertEquals(1, result.errors().get(index).size());
    }

    @Test
    void addNested_shouldPropagateIndexToNestedValidator() {
        int index = 25;

        CompositeItemValidator<Address> addressValidator =
                new CompositeItemValidator<>();

        addressValidator.addRule(
                (address, errorIndex) -> {
                    assertEquals(index, errorIndex);

                    ItemValidationResult result = new ItemValidationResult();

                    result.addError(
                            new Error()
                                    .setField("country")
                                    .setMessage("Country required"),
                            errorIndex
                    );

                    return result;
                });

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addNested(
                "address",
                Employee::getAddress,
                addressValidator
        );

        Employee employee =
                new Employee().setAddress(new Address());

        ItemValidationResult result =
                validator.validate(employee, index);

        assertTrue(result.errors().containsKey(index));
        assertEquals(
                "address.country",
                result.errors().get(index).get(0).getField()
        );
    }

    @Test
    void addNested_multipleValidatorsForSameRule_shouldExecuteAll() {
        CompositeItemValidator<Address> addressValidator =
                new CompositeItemValidator<>();

        AtomicInteger executionCount = new AtomicInteger();

        addressValidator.addRule(
                (address, index) -> {
                    executionCount.incrementAndGet();
                    return new ItemValidationResult();
                }
        );

        addressValidator.addRule(
                (address, index) -> {
                    executionCount.incrementAndGet();
                    return new ItemValidationResult();
                }
        );

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addNested(
                "address",
                Employee::getAddress,
                addressValidator
        );

        validator.validate(
                new Employee().setAddress(new Address()),
                INDEX
        );

        assertEquals(2, executionCount.get());
    }

    @Test
    void addNested_multipleNestedFields_shouldValidateEachNestedObject() {
        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        AtomicInteger executionCount = new AtomicInteger();

        validator.addNested(
                "address",
                Employee::getAddress,
                (address, index) -> {
                    executionCount.incrementAndGet();
                    return new ItemValidationResult();
                }
        );

        validator.addNested(
                "billingAddress",
                Employee::getAddress,
                (address, index) -> {
                    executionCount.incrementAndGet();
                    return new ItemValidationResult();
                }
        );

        validator.validate(
                new Employee().setAddress(new Address()),
                INDEX
        );

        assertEquals(2, executionCount.get());
    }

    @Test
    void addNested_whenNestedValidatorReturnsNoErrors_shouldRemainSuccessful() {
        CompositeItemValidator<Address> addressValidator =
                new CompositeItemValidator<>();

        addressValidator.addRule(
                (address, index) -> new ItemValidationResult()
        );

        CompositeItemValidator<Employee> validator =
                new CompositeItemValidator<>();

        validator.addNested(
                "address",
                Employee::getAddress,
                addressValidator
        );

        Employee employee =
                new Employee().setAddress(new Address());

        ItemValidationResult result =
                validator.validate(employee, INDEX);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }
}