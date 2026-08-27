package io.github.vaibhavsonar.validator.integration;

import io.github.vaibhavsonar.validator.CollectionValidator;
import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.collection.CollectionRuleValidator;
import io.github.vaibhavsonar.validator.collection.CollectionValidatorBuilder;
import io.github.vaibhavsonar.validator.collection.CompositeCollectionValidator;
import io.github.vaibhavsonar.validator.helper.EmployeeValidator;
import io.github.vaibhavsonar.validator.helper.TestRule;
import io.github.vaibhavsonar.validator.helper.model.Address;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.CollectionValidationResult;
import io.github.vaibhavsonar.validator.rule.builder.CollectionRuleBuilder;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class CollectionValidationIntegrationTest {
    private static final int INDEX = 0;

    @Test
    void validate_collectionWithDuplicateEmployeeIds_shouldReturnCollectionError() {

        CollectionValidator<Employee> validator =
                new CompositeCollectionValidator<Employee>()
                        .addRule(
                                new CollectionRuleValidator<>(
                                        "employees",
                                        employees -> employees.stream()
                                                .map(Employee::getId)
                                                .distinct()
                                                .count() != employees.size(),
                                        "Duplicate employee ids found"));

        List<Employee> employees = List.of(
                new Employee().setId("100"),
                new Employee().setId("200"),
                new Employee().setId("100"));

        CollectionValidationResult result =
                validator.validate(employees, INDEX);

        assertFalse(result.isValidationSuccessful());

        Error error = result.errors().get(0).get(0);

        assertEquals("employees", error.getField());
        assertEquals("Duplicate employee ids found", error.getMessage());
    }

    @Test
    void validate_collectionWithValidEmployees_shouldReturnSuccessfulValidation() {

        CollectionValidator<Employee> validator =
                new CompositeCollectionValidator<Employee>()
                        .addRule(
                                new CollectionRuleValidator<>(
                                        "employees",
                                        employees -> employees.stream()
                                                .map(Employee::getId)
                                                .distinct()
                                                .count() != employees.size(),
                                        "Duplicate employee ids found"));

        List<Employee> employees = List.of(
                new Employee().setId("100"),
                new Employee().setId("200"),
                new Employee().setId("300"));

        CollectionValidationResult result =
                validator.validate(employees, INDEX);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validate_collectionAndItemValidators_shouldAggregateAllErrors() {

        CollectionValidator<Employee> collectionValidator =
                new CompositeCollectionValidator<Employee>()
                        .addRule(
                                CollectionValidatorBuilder.<Employee>newInstance()
                                        .collection(
                                                duplicateIdRule()
                                        )
                                        .build());

        ItemValidator<Employee> itemValidator =
                EmployeeValidator.employeeValidator();

        List<Employee> employees = List.of(

                new Employee()
                        .setId("")
                        .setAddress(
                                new Address()
                                        .setCity("Pune")
                                        .setCountry("India")),

                new Employee()
                        .setId("100")
                        .setAddress(
                                new Address()
                                        .setCity("Mumbai")
                                        .setCountry("India")),

                new Employee()
                        .setId("100")
                        .setAddress(
                                new Address()
                                        .setCity("")
                                        .setCountry("Invalid"))
        );

        CollectionValidationResult result =
                collectionValidator.validate(employees, INDEX);

        for (int index = 0; index < employees.size(); index++) {
            result.addErrorsFrom(
                    itemValidator.validate(employees.get(index), index));
        }

        assertFalse(result.isValidationSuccessful());

        assertTrue(result.errors().containsKey(0));
        assertTrue(result.errors().containsKey(2));

        assertTrue(result.errors().get(0)
                .stream()
                .anyMatch(e -> e.getField().equals("employees")));

        assertTrue(result.errors().get(0)
                .stream()
                .anyMatch(e -> e.getField().equals("id")));

        assertTrue(result.errors().get(2)
                .stream()
                .anyMatch(e -> e.getField().equals("address.city")));

        assertTrue(result.errors().get(2)
                .stream()
                .anyMatch(e -> e.getField().equals("address.country")));
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
