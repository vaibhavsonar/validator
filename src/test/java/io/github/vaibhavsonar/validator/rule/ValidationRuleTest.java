package io.github.vaibhavsonar.validator.rule;

import io.github.vaibhavsonar.validator.helper.model.Address;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.helper.TestRule;
import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationRuleTest {

    @Test
    void constructor_shouldInitializeAllFields() {

        Predicate<String> predicate = String::isBlank;

        ValidationRule<String> rule = new ValidationRule<>(
                TestRule.ID_REQUIRED,
                predicate,
                "Id is required"
        );

        assertEquals(TestRule.ID_REQUIRED, rule.getRuleIdentifier());
        assertSame(predicate, rule.getPredicate());
        assertEquals("Id is required", rule.getMessage());
    }

    @Test
    void predicate_shouldEvaluateCorrectly() {

        ValidationRule<String> rule = new ValidationRule<>(
                TestRule.ID_REQUIRED,
                String::isBlank,
                "Id is required"
        );

        assertTrue(rule.getPredicate().test(""));
        assertTrue(rule.getPredicate().test(" "));
        assertFalse(rule.getPredicate().test("EMP001"));
    }
}
