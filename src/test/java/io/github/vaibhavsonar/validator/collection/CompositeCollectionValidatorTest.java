package io.github.vaibhavsonar.validator.collection;

import io.github.vaibhavsonar.validator.CollectionValidator;
import io.github.vaibhavsonar.validator.helper.TestRule;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.CollectionValidationResult;
import io.github.vaibhavsonar.validator.rule.builder.CollectionRuleBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class CompositeCollectionValidatorTest {
    private static final int INDEX = 0;

    @Test
    void validate_whenNoRules_shouldReturnSuccessfulValidation() {

        CompositeCollectionValidator<Employee> validator =
                new CompositeCollectionValidator<>();

        CollectionValidationResult result =
                validator.validate(List.of(new Employee()), INDEX);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void addRule_shouldExecuteValidator() {

        CompositeCollectionValidator<Employee> validator =
                new CompositeCollectionValidator<>();

        validator.addRule(
                CollectionValidatorBuilder.<Employee>newInstance()
                        .collection(
                                duplicateIdRule()
                        )
                        .build());

        CollectionValidationResult result =
                validator.validate(List.of(new Employee().setId("123"),
                        new Employee().setId("123")), INDEX);

        assertFalse(result.isValidationSuccessful());

        assertEquals(1, result.errors().size());

        Error error = result.errors().get(0).get(0);

        assertEquals("employees", error.getField());
        assertEquals("Duplicate employee id", error.getMessage());
    }

    @Test
    void addRules_shouldMergeValidators() {

        CompositeCollectionValidator<Employee> first =
                new CompositeCollectionValidator<>();

        CompositeCollectionValidator<Employee> second =
                new CompositeCollectionValidator<>();

        second.addRule(
                CollectionValidatorBuilder.<Employee>newInstance()
                        .collection(
                                duplicateIdRule()
                        )
                        .build());

        first.addRules(second);

        CollectionValidationResult result =
                first.validate(List.of(new Employee().setId("123"),
                        new Employee().setId("123")), INDEX);

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().get(0).size());
    }

    @Test
    void validate_multipleRules_shouldAggregateErrors() {

        CompositeCollectionValidator<Employee> validator =
                new CompositeCollectionValidator<>();

        validator.addRule(
                CollectionValidatorBuilder.<Employee>newInstance()
                        .collection(
                                duplicateIdRule()
                        )
                        .build());

        validator.addRule(
                CollectionValidatorBuilder.<Employee>newInstance()
                        .collection(
                                CollectionRuleBuilder.<Employee>newInstance()
                                        .check(
                                                "employees",
                                                TestRule.COLLECTION_EMPTY,
                                                employees -> employees.isEmpty(),
                                                "Collection is empty")
                        )
                        .build());

        CollectionValidationResult result =
                validator.validate(List.of(), INDEX);

        assertFalse(result.isValidationSuccessful());
        assertEquals(1, result.errors().get(0).size());
    }

    @Test
    void validate_shouldInvokeAllValidators() {

        int[] counter = {0};

        CompositeCollectionValidator<Employee> validator =
                new CompositeCollectionValidator<>();

        CollectionValidator<Employee> countingValidator = (employees, index) -> {
            counter[0]++;
            return new CollectionValidationResult();
        };

        validator.addRule(countingValidator);
        validator.addRule(countingValidator);

        validator.validate(List.of(new Employee()), INDEX);

        assertEquals(2, counter[0]);
    }

    private CollectionRuleBuilder<Employee> duplicateIdRule() {
        return CollectionRuleBuilder.<Employee>newInstance()
                .check(
                        "employees",
                        TestRule.DUPLICATE_ID,
                        employees -> !employees.stream()
                                .collect(Collectors.groupingBy(Employee::getId))
                                .values().stream()
                                .filter(group -> group.size() > 1)
                                .flatMap(Collection::stream).toList().isEmpty(),
                        "Duplicate employee id");
    }
}
