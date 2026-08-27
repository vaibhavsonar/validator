package io.github.vaibhavsonar.validator.item;

import io.github.vaibhavsonar.validator.helper.model.Address;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.model.RuleIdentifier;
import io.github.vaibhavsonar.validator.rule.ItemValidationRule;
import io.github.vaibhavsonar.validator.rule.ItemValidationRuleImpl;
import io.github.vaibhavsonar.validator.rule.builder.ItemRuleBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ItemRuleBuilderTest {
    @Test
    void shouldBuildItemValidationRule() {
        ItemRuleBuilder<Employee> builder = ItemRuleBuilder.newInstance();

        List<ItemValidationRule<Employee>> rules = builder
                .check(
                        TestRuleId.EMPLOYEE_REQUIRED,
                        "employee",
                        employee -> employee == null,
                        "Employee must not be null"
                )
                .build();

        assertEquals(1, rules.size());

        ItemValidationRule<Employee> rule = rules.get(0);

        assertInstanceOf(ItemValidationRuleImpl.class, rule);
        assertEquals(
                TestRuleId.EMPLOYEE_REQUIRED,
                rule.ruleIdentifier()
        );
    }

    @Test
    void shouldConfigurePredicateAndMessage() {
        ItemRuleBuilder<Employee> builder = ItemRuleBuilder.newInstance();

        List<ItemValidationRule<Employee>> rules = builder
                .check(
                        TestRuleId.EMPLOYEE_ADULT,
                        "age",
                        employee -> employee.getAge() < 18,
                        "Employee must be at least 18 years old"
                )
                .build();

        ItemValidationRule<Employee> rule = rules.get(0);

        ItemValidationRuleImpl<Employee> implementation =
                assertInstanceOf(ItemValidationRuleImpl.class, rule);

        Employee adult = new Employee()
                .setName("John")
                .setAge(30);

        Employee minor = new Employee()
                .setName("John")
                .setAge(16);

        assertFalse(implementation.getPredicate().test(adult));
        assertTrue(implementation.getPredicate().test(minor));
        assertEquals(
                "Employee must be at least 18 years old",
                implementation.getMessage()
        );
    }

    @Test
    void shouldAddMultipleRules() {
        ItemRuleBuilder<Employee> builder = ItemRuleBuilder.newInstance();

        List<ItemValidationRule<Employee>> rules = builder
                .check(
                        TestRuleId.EMPLOYEE_REQUIRED,
                        "employee",
                        employee -> employee == null,
                        "Employee must not be null"
                )
                .check(
                        TestRuleId.EMPLOYEE_ADULT,
                        "age",
                        employee -> employee.getAge() < 18,
                        "Employee must be at least 18 years old"
                )
                .check(
                        TestRuleId.EMPLOYEE_ACTIVE,
                        "active",
                        employee -> !Boolean.TRUE.equals(employee.getActive()),
                        "Employee must be active"
                )
                .build();

        assertEquals(3, rules.size());

        assertEquals(
                TestRuleId.EMPLOYEE_REQUIRED,
                rules.get(0).ruleIdentifier()
        );

        assertEquals(
                TestRuleId.EMPLOYEE_ADULT,
                rules.get(1).ruleIdentifier()
        );

        assertEquals(
                TestRuleId.EMPLOYEE_ACTIVE,
                rules.get(2).ruleIdentifier()
        );
    }

    @Test
    void shouldReturnSameBuilderFromCheck() {
        ItemRuleBuilder<Employee> builder = ItemRuleBuilder.newInstance();

        ItemRuleBuilder<Employee> result = builder.check(
                TestRuleId.EMPLOYEE_REQUIRED,
                "employee",
                employee -> employee == null,
                "Employee must not be null"
        );

        assertSame(builder, result);
    }

    @Test
    void shouldReturnEmptyListWhenNoRulesAreConfigured() {
        ItemRuleBuilder<Employee> builder = ItemRuleBuilder.newInstance();

        List<ItemValidationRule<Employee>> rules = builder.build();

        assertNotNull(rules);
        assertTrue(rules.isEmpty());
    }

    @Test
    void shouldReturnCopyFromBuild() {
        ItemRuleBuilder<Employee> builder = ItemRuleBuilder.newInstance();

        builder.check(
                TestRuleId.EMPLOYEE_REQUIRED,
                "employee",
                employee -> employee == null,
                "Employee must not be null"
        );

        List<ItemValidationRule<Employee>> firstBuild = builder.build();

        firstBuild.clear();

        List<ItemValidationRule<Employee>> secondBuild = builder.build();

        assertEquals(1, secondBuild.size());
    }

    @Test
    void shouldSupportDifferentItemTypes() {
        ItemRuleBuilder<Address> builder = ItemRuleBuilder.newInstance();

        List<ItemValidationRule<Address>> rules = builder
                .check(
                        TestRuleId.ADDRESS_REQUIRED,
                        "address",
                        address -> address == null,
                        "Address must not be null"
                )
                .build();

        assertEquals(1, rules.size());
        assertEquals(
                TestRuleId.ADDRESS_REQUIRED,
                rules.get(0).ruleIdentifier()
        );
    }

    private enum TestRuleId implements RuleIdentifier {
        EMPLOYEE_REQUIRED,
        EMPLOYEE_ADULT,
        EMPLOYEE_ACTIVE,
        ADDRESS_REQUIRED;

        @Override
        public String ruleIdentifier() {
            return name();
        }
    }
}
