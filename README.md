# validator [![Build](https://github.com/vaibhavsonar/validator/actions/workflows/build.yml/badge.svg)](https://github.com/vaibhavsonar/validator/actions/workflows/build.yml)
Generic validator for your POJOs.



A lightweight, fluent validation framework for Java that supports:

- ✔ Item (object) validation
- ✔ Collection validation
- ✔ Nested object validation
- ✔ Conditional validation
- ✔ Custom validation rules
- ✔ Rule identifiers
- ✔ Zero reflection during validation
- ✔ Java 21+

---

## Installation

### Maven

```xml
<dependency>
    <groupId>io.github.vaibhavsonar</groupId>
    <artifactId>validator</artifactId>
    <version>latest-version</version>
</dependency>
```

---

## Features

- Fluent builder API
- Nested object validation
- Collection-level validation
- Conditional validation
- Validation rule identifiers
- Aggregated validation results
- Lightweight with no runtime reflection

---

## Define Your Models

```java
@Data
@Accessors(chain = true)
public class Address {

    private String city;

    private String country;
}
```

```java
@Data
@Accessors(chain = true)
public class Employee {

    private String id;

    private Address address;
}
```

---

# Define Rule Identifiers

```java
public enum ValidationRules implements RuleIdentifier {

    ID_REQUIRED,
    CITY_REQUIRED,
    COUNTRY_REQUIRED,
    COUNTRY_INVALID,
    DUPLICATE_EMPLOYEE_ID;

    @Override
    public String ruleIdentifier() {
        return name();
    }
}
```

---

# Create an Item Validator

```java
ItemValidator<Address> addressValidator =
        ItemValidatorBuilder.consider(Address.class)

                .field(
                        "city",
                        Address::getCity,
                        rules -> rules.check(
                                ValidationRules.CITY_REQUIRED,
                                Objects::isNull,
                                "City is required"))

                .field(
                        "country",
                        Address::getCountry,
                        rules -> rules
                                .check(
                                        ValidationRules.COUNTRY_REQUIRED,
                                        Objects::isNull,
                                        "Country is required")
                                .check(
                                        ValidationRules.COUNTRY_INVALID,
                                        country -> !"India".equals(country),
                                        "Only India is supported"))

                .build();
```

---

# Nested Validation

```java
ItemValidator<Employee> employeeValidator =
        ItemValidatorBuilder.consider(Employee.class)

                .field(
                        "id",
                        Employee::getId,
                        rules -> rules.check(
                                ValidationRules.ID_REQUIRED,
                                String::isBlank,
                                "Employee Id is required"))

                .nested(
                        "address",
                        Employee::getAddress,
                        addressValidator)

                .build();
```

---

# Validate an Object

```java
Employee employee =
        new Employee()
                .setId("")
                .setAddress(
                        new Address()
                                .setCountry("USA"));

ItemValidationResult result =
        employeeValidator.validate(employee, 1);
```

---

# Validation Result

```text
Row : 1

id
 └── Employee Id is required

address.city
 └── City is required

address.country
 └── Only India is supported
```

---

# Conditional Validation

```java
ItemValidator<Employee> validator =
        new ConditionalItemValidator<>(
                employee -> employee.getAddress() != null,
                employeeValidator);
```

The wrapped validator executes only when the supplied condition evaluates to `true`.

---

# Collection Validation

```java
CollectionValidator<Employee> duplicateValidator =
        new CompositeCollectionValidator<Employee>()

                .addRule(
                        ValidationRules.DUPLICATE_EMPLOYEE_ID,

                        new CollectionFieldValidator<>(

                                "employees",

                                employees -> {

                                    Set<String> ids = new HashSet<>();

                                    return employees.stream()
                                            .map(Employee::getId)
                                            .anyMatch(id -> !ids.add(id));

                                },

                                "Duplicate employee ids found"));
```

Validate the collection:

```java
CollectionValidationResult result =
        duplicateValidator.validate(employeeList);
```

---

# Composite Validators

Multiple validators can be combined into a single validator.

```java
CompositeItemValidator<Employee> validator =
        new CompositeItemValidator<>();

validator
        .addRule(
                ValidationRules.ID_REQUIRED,
                employeeIdValidator)

        .addRule(
                ValidationRules.ADDRESS_REQUIRED,
                employeeAddressValidator);
```

---

# Validation Result Structure

Validation errors are grouped by row number.

Typically, you can go with 0 as row number or any value supplied during validation.

```java
Map<Integer, List<Error>>
```

Example:

```text
{
    1 : [
        {
            field : "id",
            rejectedValue : "",
            message : "Employee Id is required"
        },
        {
            field : "address.country",
            rejectedValue : "USA",
            message : "Only India is supported"
        }
    ]
}
```

---
## Validating a Collection of Objects

A common use case is validating records imported from a CSV, Excel file, or database. While
`ItemValidator` validates a single object, you can iterate over a collection and provide the
row number (or index) for each item.

The row number is stored with every validation error, making it easy to identify which record
failed validation.

```java
ItemValidator<Employee> validator = EmployeeValidator.employeeValidator();

List<Employee> employees = List.of(
        new Employee()
                .setId("100")
                .setAddress(new Address()
                        .setCity("Pune")
                        .setCountry("India")),

        new Employee()
                .setId("ABC")
                .setAddress(new Address()
                        .setCity("")
                        .setCountry("Unknown")),

        new Employee()
                .setId("")
                .setAddress(null)
);

ItemValidationResult validationResult = new ItemValidationResult();

for (int rowNumber = 0; rowNumber < employees.size(); rowNumber++) {
    validationResult.addErrorsFrom(
            validator.validate(employees.get(rowNumber), rowNumber)
    );
}
```

Validation errors are grouped by row number.

```java
validationResult.errors().forEach((row, errors) -> {
    System.out.println("Row: " + row);

    errors.forEach(error -> System.out.printf(
            "  Field: %s, Message: %s%n",
            error.getField(),
            error.getMessage()
    ));
});
```

Example output:

```text
Row: 1
  Field: employeeId, Message: Employee ID must contain digits only.
  Field: address.city, Message: City must not be null or empty.
  Field: address.country, Message: Invalid country.

Row: 2
  Field: employeeId, Message: Employee ID must not be null or empty.
  Field: address, Message: Address must not be null.
```

> **Tip:** When validating records imported from CSV or Excel, pass the actual file row number
> instead of the zero-based index. This makes it easier for users to locate invalid records in
> the original file.

### Combining Item and Collection Validation

`ItemValidator` validates individual objects, while `CollectionValidator` validates rules that
require access to the entire collection (for example duplicate IDs, uniqueness checks, or
cross-record consistency).

A typical import workflow is:

1. Execute `CollectionValidator` once for the entire collection.
2. Execute `ItemValidator` for each object, passing its row number.
3. Merge all validation errors into a single validation result.

This approach produces a single validation report containing both collection-level and
field-level validation errors.
---

# Rule Identifiers

Each validation rule has a unique identifier.

```java
ValidationRules.COUNTRY_REQUIRED
```

Rule identifiers help with:

- logging
- debugging
- enabling/disabling rules
- reporting
- future rule configuration

---

# Why Java Validator?

Unlike Bean Validation (JSR-380), this library focuses on:

- Fluent API
- Programmatic validation
- Nested validation
- Collection-wide validation
- Conditional validation
- No annotations required
- No reflection during validation
- Easy unit testing

---

# Requirements

- Java 21 or later

---

# License

Apache License 2.0