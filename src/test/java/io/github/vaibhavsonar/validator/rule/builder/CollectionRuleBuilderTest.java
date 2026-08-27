package io.github.vaibhavsonar.validator.rule.builder;

import io.github.vaibhavsonar.validator.helper.TestRule;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.rule.CollectionValidationRule;
import io.github.vaibhavsonar.validator.rule.CollectionValidationRuleImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CollectionRuleBuilderTest {

    @Test
    void build_whenNoRulesConfigured_shouldReturnEmptyList() {
        CollectionRuleBuilder<Employee> builder =
                new CollectionRuleBuilder<>();

        List<CollectionValidationRule<Employee>> rules =
                builder.build();

        assertNotNull(rules);
        assertTrue(rules.isEmpty());
    }

    @Test
    void check_shouldAddCollectionValidationRule() {
        CollectionRuleBuilder<Employee> builder =
                new CollectionRuleBuilder<>();

        List<CollectionValidationRule<Employee>> rules =
                builder.check(
                                "employees",
                                TestRule.EMPLOYEE_REQUIRED,
                                employees -> employees.isEmpty(),
                                "At least one employee is required"
                        )
                        .build();

        assertEquals(1, rules.size());
        assertInstanceOf(
                CollectionValidationRuleImpl.class,
                rules.get(0)
        );

        assertEquals(
                TestRule.EMPLOYEE_REQUIRED,
                rules.get(0).ruleIdentifier()
        );
    }

    @Test
    void check_shouldConfigurePredicateAndMessage() {
        CollectionRuleBuilder<Employee> builder =
                new CollectionRuleBuilder<>();

        List<CollectionValidationRule<Employee>> rules =
                builder.check(
                                "employee.age",
                                TestRule.AGE_INVALID,
                                employees -> employees.stream()
                                        .anyMatch(employee -> employee.getAge() < 18),
                                "All employees must be adults"
                        )
                        .build();

        CollectionValidationRuleImpl<Employee> rule =
                assertInstanceOf(
                        CollectionValidationRuleImpl.class,
                        rules.get(0)
                );

        Employee adult = new Employee()
                .setAge(30);

        Employee minor = new Employee()
                .setAge(16);

        assertFalse(
                rule.getPredicate().test(List.of(adult))
        );

        assertTrue(
                rule.getPredicate().test(List.of(adult, minor))
        );

        assertEquals(
                "All employees must be adults",
                rule.getMessage()
        );
    }

    @Test
    void check_shouldReturnSameBuilder() {
        CollectionRuleBuilder<Employee> builder =
                new CollectionRuleBuilder<>();

        CollectionRuleBuilder<Employee> result =
                builder.check(
                        "employees",
                        TestRule.EMPLOYEE_REQUIRED,
                        employees -> employees.isEmpty(),
                        "Employees are required"
                );

        assertSame(builder, result);
    }

    @Test
    void check_shouldSupportMultipleRules() {
        CollectionRuleBuilder<Employee> builder =
                new CollectionRuleBuilder<>();

        List<CollectionValidationRule<Employee>> rules =
                builder
                        .check(
                                "employees",
                                TestRule.EMPLOYEE_REQUIRED,
                                List::isEmpty,
                                "At least one employee is required"
                        )
                        .check(
                                "",
                                TestRule.AGE_INVALID,
                                employees -> employees.stream()
                                        .anyMatch(
                                                employee ->
                                                        employee.getAge() < 18),
                                "All employees must be adults"
                        )
                        .build();

        assertEquals(2, rules.size());

        assertEquals(
                TestRule.EMPLOYEE_REQUIRED,
                rules.get(0).ruleIdentifier()
        );

        assertEquals(
                TestRule.AGE_INVALID,
                rules.get(1).ruleIdentifier()
        );
    }

    @Test
    void build_shouldReturnCopyOfConfiguredRules() {
        CollectionRuleBuilder<Employee> builder =
                new CollectionRuleBuilder<>();

        builder.check(
                "employees",
                TestRule.EMPLOYEE_REQUIRED,
                List::isEmpty,
                "At least one employee is required"
        );

        List<CollectionValidationRule<Employee>> first =
                builder.build();

        first.clear();

        List<CollectionValidationRule<Employee>> second =
                builder.build();

        assertEquals(1, second.size());
    }

    @Test
    void check_shouldSupportComplexCollectionPredicate() {
        CollectionRuleBuilder<Employee> builder =
                new CollectionRuleBuilder<>();

        List<CollectionValidationRule<Employee>> rules =
                builder.check(
                        "",
                        TestRule.ID_INVALID,
                        employees -> employees.stream()
                                .map(Employee::getId)
                                .distinct()
                                .count() != employees.size(),
                        "Employee IDs must be unique"
                ).build();

        CollectionValidationRule<Employee> rule = rules.get(0);

        Employee first = new Employee().setId("EMP-001");
        Employee second = new Employee().setId("EMP-002");
        Employee duplicate = new Employee().setId("EMP-001");

        assertFalse(
                rule.predicate().test(List.of(first, second))
        );

        assertTrue(
                rule.predicate().test(List.of(first, duplicate))
        );
    }
}