package io.github.vaibhavsonar.validator.field;

import io.github.vaibhavsonar.validator.helper.TestRule;
import io.github.vaibhavsonar.validator.rule.ValidationRule;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FieldRuleBuilderTest {

    @Test
    void check_shouldAddSingleRule() {

        FieldRuleBuilder<String> builder = new FieldRuleBuilder<>();

        builder.check(
                TestRule.ID_REQUIRED,
                String::isBlank,
                "Id is required");

        List<ValidationRule<String>> rules = builder.getRules();

        assertEquals(1, rules.size());

        ValidationRule<String> rule = rules.get(0);

        assertEquals(TestRule.ID_REQUIRED, rule.getRuleIdentifier());
        assertEquals("Id is required", rule.getMessage());

        assertTrue(rule.getPredicate().test(""));
        assertFalse(rule.getPredicate().test("EMP001"));
    }

    @Test
    void check_shouldSupportMultipleRules() {

        FieldRuleBuilder<String> builder = new FieldRuleBuilder<>();

        builder.check(
                TestRule.ID_REQUIRED,
                String::isBlank,
                "Id is required");

        builder.check(
                TestRule.ID_INVALID,
                id -> id.length() < 5,
                "Invalid Id");

        List<ValidationRule<String>> rules = builder.getRules();

        assertEquals(2, rules.size());

        assertEquals(TestRule.ID_REQUIRED, rules.get(0).getRuleIdentifier());
        assertEquals(TestRule.ID_INVALID, rules.get(1).getRuleIdentifier());
    }

    @Test
    void check_shouldReturnSameBuilderForMethodChaining() {

        FieldRuleBuilder<String> builder = new FieldRuleBuilder<>();

        FieldRuleBuilder<String> returned = builder.check(
                TestRule.ID_REQUIRED,
                String::isBlank,
                "Id is required");

        assertSame(builder, returned);
    }
}
