package io.github.vaibhavsonar.validator.rule;

import io.github.vaibhavsonar.validator.constants.ValidationType;
import io.github.vaibhavsonar.validator.helper.model.Address;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.model.Error;
import io.github.vaibhavsonar.validator.model.RuleIdentifier;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ItemValidationRuleImplTest {

    @Test
    void shouldReturnItemValidationType() {
        ItemValidationRuleImpl<Employee> rule =
                new ItemValidationRuleImpl<>(
                        TestRuleId.EMPLOYEE_AGE,
                        "age",
                        employee -> employee.getAge() < 18,
                        "Employee must be at least 18 years old"
                );

        assertEquals(ValidationType.ITEM, rule.validationType());
    }

    @Test
    void shouldReturnValidationErrorWhenPredicateReturnsTrue() {
        Employee employee = new Employee()
                .setId("1001")
                .setName("John")
                .setAge(16)
                .setActive(true);

        ItemValidationRuleImpl<Employee> rule =
                new ItemValidationRuleImpl<>(
                        TestRuleId.EMPLOYEE_AGE,
                        "age",
                        value -> value.getAge() < 18,
                        "Employee must be at least 18 years old"
                );

        Optional<Error> result = rule.validate(null, employee, 5);

        assertTrue(result.isPresent());

        Error error = result.get();

        assertEquals("age", error.getField());
        assertSame(employee, error.getRejectedValue());
        assertEquals(
                "Employee must be at least 18 years old",
                error.getMessage()
        );
    }

    @Test
    void shouldReturnEmptyWhenPredicateReturnsFalse() {
        Employee employee = new Employee()
                .setId("1001")
                .setName("John")
                .setAge(30)
                .setActive(true);

        ItemValidationRuleImpl<Employee> rule =
                new ItemValidationRuleImpl<>(
                        TestRuleId.EMPLOYEE_AGE,
                        "age",
                        value -> value.getAge() < 18,
                        "Employee must be at least 18 years old"
                );

        Optional<Error> result = rule.validate(null, employee, 5);

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldValidateCompleteEmployeeObject() {
        Employee employee = new Employee()
                .setId("1001")
                .setName("John")
                .setAge(17)
                .setActive(true)
                .setAddress(
                        new Address()
                                .setCity("Pune")
                                .setCountry("India")
                                .setStreet("MG Road")
                                .setPostalCode("411001")
                );

        ItemValidationRuleImpl<Employee> rule =
                new ItemValidationRuleImpl<>(
                        TestRuleId.EMPLOYEE_AGE,
                        "age",
                        value -> value.getAge() < 18,
                        "Employee must be an adult"
                );

        Optional<Error> result = rule.validate(null, employee, 10);

        assertTrue(result.isPresent());
        assertSame(employee, result.get().getRejectedValue());
    }

    @Test
    void shouldValidateMultipleEmployeeAttributesInSinglePredicate() {
        Employee employee = new Employee()
                .setId("1001")
                .setName("")
                .setAge(16)
                .setActive(true);

        ItemValidationRuleImpl<Employee> rule =
                new ItemValidationRuleImpl<>(
                        TestRuleId.EMPLOYEE_REQUIRED,
                        "name-age",
                        value -> value.getName() == null
                                || value.getName().isBlank()
                                || value.getAge() < 18,
                        "Employee data is invalid"
                );

        Optional<Error> result = rule.validate(null, employee, 0);

        assertTrue(result.isPresent());
        assertEquals("name-age", result.get().getField());
        assertSame(employee, result.get().getRejectedValue());
        assertEquals("Employee data is invalid", result.get().getMessage());
    }

    @Test
    void shouldValidateBooleanAttribute() {
        Employee employee = new Employee()
                .setId("1001")
                .setName("John")
                .setAge(30)
                .setActive(false);

        ItemValidationRuleImpl<Employee> rule =
                new ItemValidationRuleImpl<>(
                        TestRuleId.EMPLOYEE_ACTIVE,
                        "active",
                        value -> !Boolean.TRUE.equals(value.getActive()),
                        "Employee must be active"
                );

        Optional<Error> result = rule.validate(null, employee, 2);

        assertTrue(result.isPresent());
        assertEquals("Employee must be active", result.get().getMessage());
    }

    @Test
    void shouldValidateNestedAddressAsPartOfEmployeePredicate() {
        Employee employee = new Employee()
                .setId("1001")
                .setName("John")
                .setAge(30)
                .setActive(true)
                .setAddress(
                        new Address()
                                .setCity("")
                                .setCountry("India")
                                .setStreet("MG Road")
                                .setPostalCode("411001")
                );

        ItemValidationRuleImpl<Employee> rule =
                new ItemValidationRuleImpl<>(
                        TestRuleId.EMPLOYEE_REQUIRED,
                        "employee",
                        value -> value.getAddress() == null
                                || value.getAddress().getCity() == null
                                || value.getAddress().getCity().isBlank(),
                        "Employee address city must not be empty"
                );

        Optional<Error> result = rule.validate(null, employee, 3);

        assertTrue(result.isPresent());

        Error error = result.get();

        assertEquals("employee", error.getField());
        assertSame(employee, error.getRejectedValue());
        assertEquals(
                "Employee address city must not be empty",
                error.getMessage()
        );
    }

    @Test
    void shouldValidateAddressAsItem() {
        Address address = new Address()
                .setCity("")
                .setCountry("India")
                .setStreet("MG Road")
                .setPostalCode("411001");

        ItemValidationRuleImpl<Address> rule =
                new ItemValidationRuleImpl<>(
                        TestRuleId.ADDRESS_REQUIRED,
                        "city",
                        value -> value.getCity() == null
                                || value.getCity().isBlank(),
                        "Address city must not be empty"
                );

        Optional<Error> result = rule.validate(null, address, 1);

        assertTrue(result.isPresent());

        Error error = result.get();

        assertEquals("city", error.getField());
        assertSame(address, error.getRejectedValue());
        assertEquals(
                "Address city must not be empty",
                error.getMessage()
        );
    }

    @Test
    void shouldReturnConfiguredRuleIdentifier() {
        ItemValidationRuleImpl<Employee> rule =
                new ItemValidationRuleImpl<>(
                        TestRuleId.EMPLOYEE_AGE,
                        "age",
                        employee -> employee.getAge() < 18,
                        "Employee must be an adult"
                );

        assertEquals(
                TestRuleId.EMPLOYEE_AGE,
                rule.getRuleIdentifier()
        );

        assertEquals(
                TestRuleId.EMPLOYEE_AGE,
                rule.ruleIdentifier()
        );
    }

    @Test
    void shouldReturnConfiguredPredicate() {
        java.util.function.Predicate<Employee> predicate =
                employee -> employee.getAge() < 18;

        ItemValidationRuleImpl<Employee> rule =
                new ItemValidationRuleImpl<>(
                        TestRuleId.EMPLOYEE_AGE,
                        "age",
                        predicate,
                        "Employee must be an adult"
                );

        assertSame(predicate, rule.getPredicate());
        assertSame(predicate, rule.predicate());
    }

    @Test
    void shouldReturnConfiguredMessage() {
        ItemValidationRuleImpl<Employee> rule =
                new ItemValidationRuleImpl<>(
                        TestRuleId.EMPLOYEE_AGE,
                        "age",
                        employee -> employee.getAge() < 18,
                        "Employee must be an adult"
                );

        assertEquals(
                "Employee must be an adult",
                rule.getMessage()
        );

        assertEquals(
                "Employee must be an adult",
                rule.message()
        );
    }

    private enum TestRuleId implements RuleIdentifier {
        EMPLOYEE_AGE,
        EMPLOYEE_ACTIVE,
        EMPLOYEE_REQUIRED,
        ADDRESS_REQUIRED;

        @Override
        public String ruleIdentifier() {
            return name();
        }
    }
}