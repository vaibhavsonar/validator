package io.github.vaibhavsonar.validator.field;

import io.github.vaibhavsonar.validator.helper.TestRule;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.rule.FieldValidationRule;
import io.github.vaibhavsonar.validator.rule.builder.FieldRuleBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FieldRuleBuilderTest {

    @Test
    void check_shouldAddSingleRule() {

        FieldRuleBuilder<Employee, String> builder = new FieldRuleBuilder<>();

        builder.check(
                TestRule.ID_REQUIRED,
                "id",
                Employee::getId,
                String::isBlank,
                "Id is required");

        List<FieldValidationRule<Employee, String>> rules = builder.build();

        assertEquals(1, rules.size());

        FieldValidationRule<Employee, String> rule = rules.get(0);

        assertEquals(TestRule.ID_REQUIRED, rule.ruleIdentifier());
        assertEquals("Id is required", rule.message());

        assertTrue(rule.predicate().test(""));
        assertFalse(rule.predicate().test("EMP001"));
    }

    @Test
    void check_shouldSupportMultipleRules() {

        FieldRuleBuilder<Employee, String> builder = new FieldRuleBuilder<>();

        builder.check(
                TestRule.ID_REQUIRED,
                "id",
                Employee::getId,
                String::isBlank,
                "Id is required");

        builder.check(
                TestRule.ID_INVALID,
                "id",
                Employee::getId,
                id -> id.length() < 5,
                "Invalid Id");

        List<FieldValidationRule<Employee, String>> rules = builder.build();

        assertEquals(2, rules.size());

        assertEquals(TestRule.ID_REQUIRED, rules.get(0).ruleIdentifier());
        assertEquals(TestRule.ID_INVALID, rules.get(1).ruleIdentifier());
    }

    @Test
    void check_shouldReturnSameBuilderForMethodChaining() {

        FieldRuleBuilder<Employee, String> builder = new FieldRuleBuilder<>();

        FieldRuleBuilder<Employee, String> returned = builder.check(
                TestRule.ID_REQUIRED,
                "id",
                Employee::getId,
                String::isBlank,
                "Id is required");

        assertSame(builder, returned);
    }
}
