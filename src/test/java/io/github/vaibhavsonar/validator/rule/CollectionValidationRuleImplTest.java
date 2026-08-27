package io.github.vaibhavsonar.validator.rule;

import io.github.vaibhavsonar.validator.constants.ValidationType;
import io.github.vaibhavsonar.validator.helper.TestRule;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.model.Error;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CollectionValidationRuleImplTest {

    private static final int INDEX = 5;

    @Test
    void validationType_shouldReturnCollection() {
        CollectionValidationRuleImpl<Employee> rule =
                new CollectionValidationRuleImpl<>(
                        TestRule.ID_INVALID,
                        "employees",
                        employees -> false,
                        "Invalid collection"
                );

        assertEquals(
                ValidationType.COLLECTION,
                rule.validationType()
        );
    }

    @Test
    void validate_whenPredicateReturnsFalse_shouldReturnEmpty() {
        CollectionValidationRuleImpl<Employee> rule =
                new CollectionValidationRuleImpl<>(
                        TestRule.ID_INVALID,
                        "employees",
                        employees -> false,
                        "Invalid collection"
                );

        List<Employee> employees = List.of(
                new Employee().setId("EMP-001"),
                new Employee().setId("EMP-002")
        );

        Optional<Error> result =
                rule.validate(null, employees, INDEX);

        assertTrue(result.isEmpty());
    }

    @Test
    void validate_whenPredicateReturnsTrue_shouldReturnError() {
        CollectionValidationRuleImpl<Employee> rule =
                new CollectionValidationRuleImpl<>(
                        TestRule.ID_INVALID,
                        "employees",
                        employees -> true,
                        "Employee collection is invalid"
                );

        List<Employee> employees = List.of(
                new Employee().setId("EMP-001"),
                new Employee().setId("EMP-002")
        );

        Optional<Error> result =
                rule.validate(null, employees, INDEX);

        assertTrue(result.isPresent());

        Error error = result.get();

        assertEquals("employees", error.getField());
        assertSame(employees, error.getRejectedValue());
        assertEquals(
                "Employee collection is invalid",
                error.getMessage()
        );
    }

    @Test
    void validate_shouldPassCompleteCollectionToPredicate() {
        List<Employee> employees = List.of(
                new Employee()
                        .setId("EMP-001")
                        .setAge(30),
                new Employee()
                        .setId("EMP-002")
                        .setAge(40)
        );

        CollectionValidationRuleImpl<Employee> rule =
                new CollectionValidationRuleImpl<>(
                        TestRule.AGE_INVALID,
                        "employees",
                        items -> {
                            assertSame(employees, items);
                            assertEquals(2, items.size());
                            assertEquals(
                                    "EMP-001",
                                    items.get(0).getId()
                            );
                            assertEquals(
                                    40,
                                    items.get(1).getAge()
                            );
                            return false;
                        },
                        "Invalid employee collection"
                );

        Optional<Error> result =
                rule.validate(null, employees, INDEX);

        assertTrue(result.isEmpty());
    }

    @Test
    void validate_shouldSupportEmptyCollection() {
        CollectionValidationRuleImpl<Employee> rule =
                new CollectionValidationRuleImpl<>(
                        TestRule.EMPLOYEE_REQUIRED,
                        "employees",
                        List::isEmpty,
                        "At least one employee is required"
                );

        List<Employee> employees = List.of();

        Optional<Error> result =
                rule.validate(null, employees, INDEX);

        assertTrue(result.isPresent());

        assertEquals(
                "At least one employee is required",
                result.get().getMessage()
        );

        assertSame(
                employees,
                result.get().getRejectedValue()
        );
    }

    @Test
    void validate_shouldSupportNullCollection() {
        CollectionValidationRuleImpl<Employee> rule =
                new CollectionValidationRuleImpl<>(
                        TestRule.EMPLOYEE_REQUIRED,
                        "employees",
                        employees -> employees == null,
                        "Employee collection is required"
                );

        Optional<Error> result =
                rule.validate(null, null, INDEX);

        assertTrue(result.isPresent());

        Error error = result.get();

        assertEquals("employees", error.getField());
        assertNull(error.getRejectedValue());
        assertEquals(
                "Employee collection is required",
                error.getMessage()
        );
    }

    @Test
    void shouldExposeRuleIdentifier() {
        CollectionValidationRuleImpl<Employee> rule =
                new CollectionValidationRuleImpl<>(
                        TestRule.ID_INVALID,
                        "employees",
                        employees -> false,
                        "Invalid ID"
                );

        assertEquals(
                TestRule.ID_INVALID,
                rule.ruleIdentifier()
        );

        assertEquals(
                TestRule.ID_INVALID,
                rule.getRuleIdentifier()
        );
    }

    @Test
    void shouldExposePredicate() {
        java.util.function.Predicate<List<Employee>> predicate =
                employees -> employees.size() > 10;

        CollectionValidationRuleImpl<Employee> rule =
                new CollectionValidationRuleImpl<>(
                        TestRule.EMPLOYEE_INVALID,
                        "employees",
                        predicate,
                        "Too many employees"
                );

        assertSame(predicate, rule.predicate());
        assertSame(predicate, rule.getPredicate());
    }

    @Test
    void shouldExposeMessage() {
        CollectionValidationRuleImpl<Employee> rule =
                new CollectionValidationRuleImpl<>(
                        TestRule.EMPLOYEE_INVALID,
                        "employees",
                        employees -> false,
                        "Too many employees"
                );

        assertEquals(
                "Too many employees",
                rule.message()
        );

        assertEquals(
                "Too many employees",
                rule.getMessage()
        );
    }

    @Test
    void validate_shouldDetectDuplicateEmployeeIds() {
        Employee first =
                new Employee().setId("EMP-001");

        Employee second =
                new Employee().setId("EMP-002");

        Employee duplicate =
                new Employee().setId("EMP-001");

        CollectionValidationRuleImpl<Employee> rule =
                new CollectionValidationRuleImpl<>(
                        TestRule.ID_INVALID,
                        "employees",
                        employees -> employees.stream()
                                .map(Employee::getId)
                                .distinct()
                                .count() != employees.size(),
                        "Employee IDs must be unique"
                );

        assertTrue(
                rule.validate(
                        null,
                        List.of(first, duplicate),
                        INDEX
                ).isPresent()
        );

        assertTrue(
                rule.validate(
                        null,
                        List.of(first, second),
                        INDEX
                ).isEmpty()
        );
    }
}