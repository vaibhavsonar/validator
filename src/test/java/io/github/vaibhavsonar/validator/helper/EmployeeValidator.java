package io.github.vaibhavsonar.validator.helper;

import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.helper.model.Employee;
import io.github.vaibhavsonar.validator.item.ItemValidatorBuilder;
import io.github.vaibhavsonar.validator.rule.builder.FieldRuleBuilder;

import java.util.Objects;

public class EmployeeValidator {

    public static ItemValidator<Employee> employeeValidator() {
        return ItemValidatorBuilder.<Employee>newInstance()
                .field(
                        FieldRuleBuilder.<Employee, String>newInstance()
                                .check(
                                        TestRuleId.of("employee-id-rule-1"),
                                        "id",
                                        Employee::getId,
                                        id -> Objects.isNull(id) || id.isBlank(),
                                        "Employee ID must not be null or empty."
                                )
                                .check(
                                        TestRuleId.of("employee-id-rule-2"),
                                        "id",
                                        Employee::getId,
                                        id -> !id.matches("\\d+"),
                                        "Employee ID must contain digits only."
                                )
                )
                .nested(
                        "address",
                        Employee::getAddress,
                        AddressValidator.addressValidator()
                )
                .build();
    }
}
