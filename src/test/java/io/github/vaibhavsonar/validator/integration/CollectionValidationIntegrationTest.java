package io.github.vaibhavsonar.validator.integration;

import io.github.vaibhavsonar.validator.CollectionValidator;
import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.collection.CollectionRuleValidator;
import io.github.vaibhavsonar.validator.collection.CompositeCollectionValidator;
import io.github.vaibhavsonar.validator.helper.EmployeeValidator;
import io.github.vaibhavsonar.validator.helper.TestRule;
import io.github.vaibhavsonar.validator.helper.model.Address;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.result.CollectionValidationResult;
import io.github.vaibhavsonar.validator.result.ItemValidationResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CollectionValidationIntegrationTest {
    @Test
    void validate_collectionWithDuplicateEmployeeIds_shouldReturnCollectionError() {

        CollectionValidator<Employee> validator =
                new CompositeCollectionValidator<Employee>()
                        .addRule(
                                TestRule.DUPLICATE_ID,
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
                validator.validate(employees);

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
                                TestRule.DUPLICATE_ID,
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
                validator.validate(employees);

        assertTrue(result.isValidationSuccessful());
        assertTrue(result.errors().isEmpty());
    }

    @Test
    void validate_collectionAndItemValidators_shouldAggregateAllErrors() {

        CollectionValidator<Employee> collectionValidator =
                new CompositeCollectionValidator<Employee>()
                        .addRule(
                                TestRule.DUPLICATE_ID,
                                new CollectionRuleValidator<>(
                                        "employees",
                                        employees -> employees.stream()
                                                .map(Employee::getId)
                                                .distinct()
                                                .count() != employees.size(),
                                        "Duplicate employee ids found"));

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
                collectionValidator.validate(employees);

        for (int row = 0; row < employees.size(); row++) {
            result.addErrorsFrom(
                    itemValidator.validate(employees.get(row), row));
        }

        assertFalse(result.isValidationSuccessful());

        assertTrue(result.errors().containsKey(0));
        assertTrue(result.errors().containsKey(2));

        assertTrue(result.errors().get(0)
                .stream()
                .anyMatch(e -> e.getField().equals("employees")));

        assertTrue(result.errors().get(0)
                .stream()
                .anyMatch(e -> e.getField().equals("employeeId")));

        assertTrue(result.errors().get(2)
                .stream()
                .anyMatch(e -> e.getField().equals("address.city")));

        assertTrue(result.errors().get(2)
                .stream()
                .anyMatch(e -> e.getField().equals("address.country")));
    }
}
