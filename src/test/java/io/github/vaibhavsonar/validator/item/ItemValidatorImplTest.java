package io.github.vaibhavsonar.validator.item;

import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.helper.TestRule;
import io.github.vaibhavsonar.validator.helper.model.Address;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import io.github.vaibhavsonar.validator.rule.ItemValidationRule;
import io.github.vaibhavsonar.validator.rule.ItemValidationRuleImpl;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ItemValidatorImplTest {

    private static final int INDEX = 1;

    @Test
    void validate_whenNoRules_shouldReturnSuccessfulResult() {
        ItemValidator<Employee> validator =
                new ItemValidatorImpl<>(
                        List.of()
                );

        ItemValidationResult result =
                validator.validate(new Employee(), INDEX);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validate_whenItemRulePasses_shouldReturnSuccessfulResult() {
        ItemValidator<Employee> validator =
                new ItemValidatorImpl<>(
                        List.of(new ItemValidationRuleImpl<>(
                                TestRule.ID_REQUIRED,
                                "employee",
                                List.of(Employee::getId),
                                employee -> false,
                                "Employee is invalid"
                        ))
                );

        Employee employee = new Employee()
                .setId("EMP-001")
                .setName("John")
                .setAge(30)
                .setActive(true);

        ItemValidationResult result =
                validator.validate(employee, INDEX);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validate_whenItemRuleFails_shouldReturnValidationError() {
        ItemValidator<Employee> validator =
                new ItemValidatorImpl<>(
                        List.of(new ItemValidationRuleImpl<>(
                                TestRule.ID_REQUIRED,
                                "employee",
                                List.of(employee ->  employee),
                                employee -> true,
                                "Employee is invalid"
                        ))
                );

        Employee employee = new Employee()
                .setId("EMP-001")
                .setName("John")
                .setAge(16);

        ItemValidationResult result =
                validator.validate(employee, INDEX);

        assertFalse(result.isValidationSuccessful());

        List<Error> errors = result.errors().get(INDEX);

        assertNotNull(errors);
        assertEquals(1, errors.size());

        Error error = errors.get(0);

        assertEquals("employee", error.getField());
//        assertSame(employee, error.getRejectedValue());

        assertInstanceOf(List.class, error.getRejectedValue());
        List<Object> rejectedValues = new ArrayList<>((List) error.getRejectedValue());
        assertTrue(rejectedValues.contains(employee));

        assertEquals("Employee is invalid", error.getMessage());
    }

    @Test
    void validate_whenMultipleItemRulesFail_shouldAggregateAllErrors() {
        ItemValidator<Employee> validator =
                new ItemValidatorImpl<>(
                        List.of(new ItemValidationRuleImpl<>(
                                        TestRule.ID_REQUIRED,
                                        "age",
                                        List.of(Employee::getAge),
                                        employee -> employee.getAge() < 18,
                                        "Employee must be at least 18"
                                ),
                                new ItemValidationRuleImpl<>(
                                        TestRule.ACTIVE_INVALID,
                                        "active",
                                        List.of(Employee::getActive),
                                        employee -> !Boolean.TRUE.equals(employee.getActive()),
                                        "Employee must be active"
                                ))
                );

        Employee employee = new Employee()
                .setName("John")
                .setAge(16)
                .setActive(false);

        ItemValidationResult result =
                validator.validate(employee, INDEX);

        assertFalse(result.isValidationSuccessful());

        List<Error> errors = result.errors().get(INDEX);

        assertNotNull(errors);
        assertEquals(2, errors.size());

        assertTrue(
                errors.stream()
                        .anyMatch(error ->
                                "Employee must be at least 18"
                                        .equals(error.getMessage()))
        );

        assertTrue(
                errors.stream()
                        .anyMatch(error ->
                                "Employee must be active"
                                        .equals(error.getMessage()))
        );
    }

    @Test
    void validate_whenOneRulePassesAndOneFails_shouldReturnOnlyFailingRuleError() {
        ItemValidator<Employee> validator =
                new ItemValidatorImpl<>(
                        List.of(new ItemValidationRuleImpl<>(
                                        TestRule.ID_REQUIRED,
                                        "employee",
                                        List.of(Employee::getId),
                                        employee -> false,
                                        "This error should not be returned"
                                ),
                                new ItemValidationRuleImpl<>(
                                        TestRule.ID_INVALID,
                                        "employee",
                                        List.of(employee ->  employee),
                                        employee -> true,
                                        "Employee is invalid"
                                ))
                );

        ItemValidationResult result =
                validator.validate(new Employee(), INDEX);

        assertFalse(result.isValidationSuccessful());

        List<Error> errors = result.errors().get(INDEX);

        assertEquals(1, errors.size());
        assertEquals(
                "Employee is invalid",
                errors.get(0).getMessage()
        );
    }

    @Test
    void validate_shouldExecuteAllItemRules() {
        AtomicInteger executionCount = new AtomicInteger();
        ItemValidator<Employee> validator =
                new ItemValidatorImpl<>(
                        List.of(new ItemValidationRuleImpl<>(
                                        TestRule.ID_REQUIRED,
                                        "id",
                                        List.of(Employee::getId),
                                        employee -> {
                                            executionCount.incrementAndGet();
                                            return false;
                                        },
                                        "First"
                                ),
                                new ItemValidationRuleImpl<>(
                                        TestRule.ID_INVALID,
                                        "id",
                                        List.of(Employee::getId),
                                        employee -> {
                                            executionCount.incrementAndGet();
                                            return false;
                                        },
                                        "Second"
                                ),
                                new ItemValidationRuleImpl<>(
                                        TestRule.AGE_INVALID,
                                        "age",
                                        List.of(Employee::getAge),
                                        employee -> {
                                            executionCount.incrementAndGet();
                                            return false;
                                        },
                                        "Third"
                                ))
                );

        validator.validate(new Employee(), INDEX);

        assertEquals(3, executionCount.get());
    }

    @Test
    void validate_shouldExecuteRulesInConfiguredOrder() {
        List<Integer> executionOrder = new ArrayList<>();
        ItemValidator<Employee> validator =
                new ItemValidatorImpl<>(
                        List.of(new ItemValidationRuleImpl<>(
                                        TestRule.ID_REQUIRED,
                                        "id",
                                        List.of(Employee::getId),
                                        employee -> {
                                            executionOrder.add(1);
                                            return false;
                                        },
                                        "First"
                                ),
                                new ItemValidationRuleImpl<>(
                                        TestRule.ID_INVALID,
                                        "id",
                                        List.of(Employee::getId),
                                        employee -> {
                                            executionOrder.add(2);
                                            return false;
                                        },
                                        "Second"
                                ),
                                new ItemValidationRuleImpl<>(
                                        TestRule.AGE_INVALID,
                                        "age",
                                        List.of(Employee::getAge),
                                        employee -> {
                                            executionOrder.add(3);
                                            return false;
                                        },
                                        "Third"
                                ))
                );

        validator.validate(new Employee(), INDEX);

        assertEquals(List.of(1, 2, 3), executionOrder);
    }

    @Test
    void validate_shouldPropagateIndexToRule() {
        int index = 25;

        ItemValidationRule<Employee> rule =
                new ItemValidationRuleImpl<>(
                        TestRule.ID_REQUIRED,
                        "employee",
                        List.of(Employee::getId),
                        employee -> true,
                        "Employee is invalid"
                );

        ItemValidator<Employee> validator =
                new ItemValidatorImpl<>(
                        List.of(rule)
                );

        ItemValidationResult result =
                validator.validate(new Employee(), index);

        assertTrue(result.errors().containsKey(index));
        assertEquals(1, result.errors().get(index).size());
    }

    @Test
    void validate_whenRuleReturnsEmpty_shouldNotAddErrors() {
        ItemValidationRule<Employee> rule =
                new ItemValidationRuleImpl<>(
                        TestRule.ID_REQUIRED,
                        "employee",
                        List.of(Employee::getId),
                        employee -> false,
                        "No error"
                );

        ItemValidator<Employee> validator =
                new ItemValidatorImpl<>(
                        List.of(rule)
                );

        ItemValidationResult result =
                validator.validate(new Employee(), INDEX);

        assertTrue(result.errors().isEmpty());
        assertTrue(result.isValidationSuccessful());
    }

    @Test
    void validate_whenRuleReturnsError_shouldAddErrorUsingIndex() {
        ItemValidationRule<Employee> rule =
                new ItemValidationRuleImpl<>(
                        TestRule.ID_REQUIRED,
                        "employee",
                        List.of(Employee::getId),
                        employee -> true,
                        "Employee is invalid"
                );

        int index = 42;

        ItemValidator<Employee> validator =
                new ItemValidatorImpl<>(
                        List.of(rule)
                );

        ItemValidationResult result =
                validator.validate(new Employee(), index);

        assertTrue(result.errors().containsKey(index));
        assertEquals(1, result.errors().get(index).size());
    }

    @Test
    void validate_shouldPassCompleteItemToRule() {
        Employee employee = new Employee()
                .setId("EMP-001")
                .setName("John")
                .setAge(30)
                .setActive(true)
                .setAddress(
                        new Address()
                                .setCity("Pune")
                                .setCountry("India")
                );

        AtomicInteger invocationCount = new AtomicInteger();

        ItemValidationRule<Employee> rule =
                new ItemValidationRuleImpl<>(
                        TestRule.ID_REQUIRED,
                        "employee",
                        List.of(Employee::getId),
                        value -> {
                            invocationCount.incrementAndGet();

                            assertSame(employee, value);
                            assertEquals("EMP-001", value.getId());
                            assertEquals("John", value.getName());
                            assertEquals(30, value.getAge());
                            assertTrue(value.getActive());
                            assertNotNull(value.getAddress());
                            assertEquals(
                                    "Pune",
                                    value.getAddress().getCity()
                            );

                            return false;
                        },
                        "Invalid employee"
                );

        ItemValidator<Employee> validator =
                new ItemValidatorImpl<>(
                        List.of(rule)
                );

        ItemValidationResult result =
                validator.validate(employee, INDEX);

        assertTrue(result.isValidationSuccessful());
        assertEquals(1, invocationCount.get());
    }

    @Test
    void validate_shouldSupportNullItem() {
        ItemValidationRule<Employee> rule =
                new ItemValidationRuleImpl<>(
                        TestRule.ID_REQUIRED,
                        "employee",
                        List.of(employee -> employee),
                        employee -> employee == null,
                        "Employee is required"
                );

        ItemValidator<Employee> validator =
                new ItemValidatorImpl<>(
                        List.of(rule)
                );

        ItemValidationResult result =
                validator.validate(null, INDEX);

        assertFalse(result.isValidationSuccessful());

        Error error = result.errors()
                .get(INDEX)
                .get(0);

        assertEquals("Employee is required", error.getMessage());
        assertEquals("employee", error.getField());

        assertInstanceOf(List.class, error.getRejectedValue());
        List<Object> rejectedValues = new ArrayList<>((List) error.getRejectedValue());
        assertTrue(rejectedValues.contains(null));
    }

    @Test
    void validate_shouldSupportNestedEmployeeData() {
        Employee employee = new Employee()
                .setId("EMP-001")
                .setName("John")
                .setAge(30)
                .setActive(true)
                .setAddress(
                        new Address()
                                .setCity("")
                                .setCountry("India")
                );

        ItemValidator<Employee> validator =
                new ItemValidatorImpl<>(
                        List.of(new ItemValidationRuleImpl<>(
                                TestRule.ID_INVALID,
                                "address",
                                List.of(Employee::getAddress),
                                value -> value.getAddress() == null
                                        || value.getAddress().getCity() == null
                                        || value.getAddress().getCity().isBlank(),
                                "Employee address city is required"
                        ))
                );

        ItemValidationResult result =
                validator.validate(employee, INDEX);

        assertFalse(result.isValidationSuccessful());

        Error error = result.errors()
                .get(INDEX)
                .get(0);

        assertEquals("address", error.getField());

        assertInstanceOf(List.class, error.getRejectedValue());
        List<Object> rejectedValues = new ArrayList<>((List) error.getRejectedValue());
        assertTrue(rejectedValues.contains(employee.getAddress()));

        assertEquals(
                "Employee address city is required",
                error.getMessage()
        );
    }

    @Test
    void validate_whenRulesListContainsMultipleFailingRules_shouldPreserveAllErrors() {
        ItemValidator<Employee> validator =
                new ItemValidatorImpl<>(
                        List.of(new ItemValidationRuleImpl<>(
                                        TestRule.ID_REQUIRED,
                                        "id",
                                        List.of(Employee::getId),
                                        employee -> true,
                                        "First error"
                                ),
                                new ItemValidationRuleImpl<>(
                                        TestRule.ID_INVALID,
                                        "id",
                                        List.of(Employee::getId),
                                        employee -> true,
                                        "Second error"
                                ),
                                new ItemValidationRuleImpl<>(
                                        TestRule.AGE_INVALID,
                                        "age",
                                        List.of(Employee::getId),
                                        employee -> true,
                                        "Third error"
                                ))
                );

        ItemValidationResult result =
                validator.validate(new Employee(), INDEX);

        List<Error> errors = result.errors().get(INDEX);

        assertEquals(3, errors.size());

        assertEquals("First error", errors.get(0).getMessage());
        assertEquals("Second error", errors.get(1).getMessage());
        assertEquals("Third error", errors.get(2).getMessage());
    }
}