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

The library provides validation at three scopes:

- **Collection** — validates a complete `List<T>`
- **Item** — validates a complete object of type `T`
- **Field** — validates a field value of type `F`
- **Nested** — composes item validation for nested objects
- **Conditional** — executes validation only when a condition is satisfied

It is designed for programmatic validation with no annotations and no reflection during validation.

---
## Requirements

- Java 21 or later

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
# Core Concepts

The validator is organized around three validation scopes:

```text
COLLECTION
    │
    │ validates
    ▼
List<T>
    │
    ├── collection-wide rules
    │   ├── duplicate detection
    │   ├── uniqueness
    │   ├── collection size
    │   └── cross-record consistency
    │
    └── ITEM
          │
          │ validates
          ▼
         T
          │
          ├── item-level rules
          │
          ├── FIELD
          │     │
          │     ▼
          │     F
          │
          └── nested object
                │
                ▼
              ItemValidator<R>
```

The important distinction is:

| Scope | Value being validated | Typical use |
|---|---|---|
| `COLLECTION` | `List<T>` | duplicates, uniqueness, collection size |
| `ITEM` | `T` | cross-field rules on one object |
| `FIELD` | `F` | required, range, format, field-specific rules |
| `NESTED` | another `T`/`R` object | validating object graphs |
---

# Validation Rule Abstraction

All validation rules use the common:

```java
ValidationRule<T, V>
```

contract.

Conceptually:

```text
ValidationRule<T, V>
        │
        ├── CollectionValidationRule<T>
        │       V = List<T>
        │
        ├── ItemValidationRule<T>
        │       V = T
        │
        └── FieldValidationRule<T, F>
                V = F
```

The common validation operation is:

```java
Optional<Error> validate(
        T parentObject,
        V objectToValidate,
        int index);
```

This gives the different validation scopes a common execution model while allowing each scope to define its own value type.

### Field validation

```text
T parent object
      │
      │ getter
      ▼
F field value
      │
      │ predicate
      ▼
Optional<Error>
```

### Item validation

```text
T item
  │
  │ predicate
  ▼
Optional<Error>
```

### Collection validation

```text
List<T>
   │
   │ predicate
   ▼
Optional<Error>
```

Predicates use a **failure-oriented convention**:

```text
predicate returns false → validation passes
predicate returns true  → validation fails
```
## Define Your Models

The following examples use `Employee` and `Address`.

```java
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Address {

    private String city;

    private String country;
}
```

```java
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Employee {

    private String id;

    private Address address;
}
```

---

# Define Rule Identifiers

Validation rules are identified using the `RuleIdentifier` interface.

A type-safe approach is to use an enum:

```java
import io.github.vaibhavsonar.validator.model.RuleIdentifier;

public enum ValidationRules implements RuleIdentifier {

    ID_REQUIRED,
    ID_INVALID,
    CITY_REQUIRED,
    COUNTRY_REQUIRED,
    COUNTRY_INVALID,
    EMPLOYEE_INVALID,
    DUPLICATE_EMPLOYEE_ID,
    MINIMUM_EMPLOYEES;

    @Override
    public String ruleIdentifier() {
        return name();
    }
}
```

A rule identifier provides a stable name for a validation rule and is useful for:

- logging
- debugging
- reporting
- rule configuration
- identifying specific validation failures

---

# Field Validation

`FieldValidationRule<T, F>` validates a specific field of an object.

A `FieldRuleBuilder<T, F>` stores the configured field rules.

```text
FieldRuleBuilder<T, F>
        │
        │ check(...)
        ▼
FieldValidationRuleImpl<T, F>
        │
        │ build()
        ▼
List<FieldValidationRule<T, F>>
        │
        ▼
FieldValidator<T, F>
```

## Configure Field Rules

```java
FieldRuleBuilder<Address, String> cityRules =
        FieldRuleBuilder.<Address, String>newInstance();

cityRules.check(
        ValidationRules.CITY_REQUIRED,
        "city",
        Address::getCity,
        value -> value == null || value.isBlank(),
        "City is required"
);
```

Multiple rules can be added to the same builder:

```java
FieldRuleBuilder<Address, String> countryRules =
        FieldRuleBuilder.<Address, String>newInstance();

countryRules
        .check(
                ValidationRules.COUNTRY_REQUIRED,
                "country",
                Address::getCountry,
                value -> value == null || value.isBlank(),
                "Country is required"
        )
        .check(
                ValidationRules.COUNTRY_INVALID,
                "country",
                Address::getCountry,
                country -> country != null && !"India".equals(country),
                "Only India is supported"
        );
```

The rules are retained in insertion order.

---

# Item Validation

`ItemValidationRule<T>` validates the complete item rather than a specific field.

An `ItemRuleBuilder<T>` is used to configure item-level predicates.

```text
ItemRuleBuilder<T>
        │
        │ check(...)
        ▼
ItemValidationRuleImpl<T>
        │
        │ build()
        ▼
List<ItemValidationRule<T>>
        │
        ▼
ItemValidatorImpl<T>
```

## Example Item Rule

Suppose an employee must have both an ID and an address:

```java
ItemRuleBuilder<Employee> employeeRules =
        ItemRuleBuilder.newInstance();

employeeRules.check(
        ValidationRules.EMPLOYEE_INVALID,
        employee -> employee.getId() == null
                || employee.getId().isBlank()
                || employee.getAddress() == null,
        "Employee must have an ID and an address"
);
```

The predicate receives the complete `Employee`.

```text
Employee
   │
   ▼
Predicate<Employee>
   │
   ├── false → valid
   │
   └── true  → Error
```

For item-level validation, the generated error uses `"this"` as the field because the rule applies to the complete item.

---

# Build an Item Validator

`ItemValidatorBuilder<T>` combines field and item validation into a single `ItemValidator<T>`.

The current builder accepts configured rule builders directly:

```java
ItemValidator<Address> addressValidator =
        ItemValidatorBuilder.<Address>newInstance()
                .field(cityRules)
                .field(countryRules)
                .build();
```

An item validator can also contain item-level rules:

```java
ItemRuleBuilder<Employee> employeeRules =
        ItemRuleBuilder.newInstance();

employeeRules.check(
        ValidationRules.EMPLOYEE_INVALID,
        employee -> employee.getId() == null
                || employee.getId().isBlank(),
        "Employee ID is required"
);

ItemValidator<Employee> employeeValidator =
        ItemValidatorBuilder.<Employee>newInstance()
                .item("employee", employeeRules)
                .build();
```

The builder combines all configured validators through `CompositeItemValidator`.
---

# Nested Object Validation

Nested validation allows an `ItemValidator<T>` to delegate validation of a nested object to another `ItemValidator<R>`.

For example:

```text
Employee
   │
   └── address
          │
          ▼
      Address
          │
          ├── city
          └── country
```

First create the nested validator:

```java
ItemValidator<Address> addressValidator =
        ItemValidatorBuilder.<Address>newInstance()
                .field(cityRules)
                .field(countryRules)
                .build();
```

Then register it with the employee validator:

```java
ItemValidator<Employee> employeeValidator =
        ItemValidatorBuilder.<Employee>newInstance()
                .field(employeeIdRules)
                .nested(
                        "address",
                        Employee::getAddress,
                        addressValidator
                )
                .build();
```

If the nested `Address` is `null`, nested validation is skipped.

If the nested validator reports:

```text
city → City is required
```

the parent validator prefixes the nested field name:

```text
address.city → City is required
```

This allows validation errors to preserve their location within the object graph.
---

# Complete Item Validation Example

The following example combines field, item, and nested validation.

```java
FieldRuleBuilder<Employee, String> employeeIdRules =
        FieldRuleBuilder.<Employee, String>newInstance();

employeeIdRules.check(
        ValidationRules.ID_REQUIRED,
        "id",
        Employee::getId,
        value -> value == null || value.isBlank(),
        "Employee ID is required"
);

FieldRuleBuilder<Address, String> cityRules =
        FieldRuleBuilder.<Address, String>newInstance();

cityRules.check(
        ValidationRules.CITY_REQUIRED,
        "city",
        Address::getCity,
        value -> value == null || value.isBlank(),
        "City is required"
);

FieldRuleBuilder<Address, String> countryRules =
        FieldRuleBuilder.<Address, String>newInstance();

countryRules
        .check(
                ValidationRules.COUNTRY_REQUIRED,
                "country",
                Address::getCountry,
                value -> value == null || value.isBlank(),
                "Country is required"
        )
        .check(
                ValidationRules.COUNTRY_INVALID,
                "country",
                Address::getCountry,
                country -> country != null && !"India".equals(country),
                "Only India is supported"
        );

ItemValidator<Address> addressValidator =
        ItemValidatorBuilder.<Address>newInstance()
                .field(cityRules)
                .field(countryRules)
                .build();

ItemValidator<Employee> employeeValidator =
        ItemValidatorBuilder.<Employee>newInstance()
                .field(employeeIdRules)
                .nested(
                        "address",
                        Employee::getAddress,
                        addressValidator
                )
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
                                .setCountry("USA")
                );

ItemValidationResult result =
        employeeValidator.validate(employee, 1);
```

The index is carried into the validation result.

For this employee, the expected errors are conceptually:

```text
Index: 1

id
 └── Employee ID is required

address.city
 └── City is required

address.country
 └── Only India is supported
```

---

# Conditional Validation

Conditional validators wrap another validator and execute it only when a predicate is satisfied.

## Conditional Item Validation

```java
ItemValidator<Employee> validator =
        new ConditionalItemValidator<>(
                employee -> employee.getAddress() != null,
                employeeValidator
        );
```

Execution flow:

```text
Employee
   │
   ▼
condition.test(employee)
   │
   ├── false → empty ItemValidationResult
   │
   └── true
         │
         ▼
   wrapped validator
         │
         ▼
   ItemValidationResult
```

## Conditional Collection Validation

The same concept is available for collection validation:

```java
CollectionValidator<Employee> validator =
        new ConditionalCollectionValidator<>(
                employees -> !employees.isEmpty(),
                duplicateValidator
        );
```

If the condition returns `false`, the wrapped validator is not executed.

---

# Collection Validation

`CollectionValidator<T>` operates on the complete collection rather than one item.

It is intended for rules such as:

- duplicate detection
- uniqueness checks
- minimum/maximum collection size
- cross-record consistency
- relationships between records

The collection rule architecture is:

```text
CollectionRuleBuilder<T>
        │
        │ check(...)
        ▼
CollectionValidationRuleImpl<T>
        │
        │ build()
        ▼
List<CollectionValidationRule<T>>
        │
        ▼
CollectionValidatorImpl<T>
        │
        ▼
CompositeCollectionValidator<T>
        │
        ▼
CollectionValidator<T>
```

---

# Configure Collection Rules

Create a `CollectionRuleBuilder`:

```java
CollectionRuleBuilder<Employee> collectionRules =
        CollectionRuleBuilder.<Employee>newInstance();
```

Add a duplicate-ID rule:

```java
collectionRules.check(
        "employees",
        ValidationRules.DUPLICATE_EMPLOYEE_ID,
        employees -> employees.stream()
                .map(Employee::getId)
                .distinct()
                .count() != employees.size(),
        "Employee IDs must be unique"
);
```

Add another collection-level rule:

```java
collectionRules.check(
        "employees",
        ValidationRules.MINIMUM_EMPLOYEES,
        employees -> employees.size() < 2,
        "At least two employees are required"
);
```

Each call to `check(...)` creates a `CollectionValidationRuleImpl<T>`.

The rule contains:

```text
RuleIdentifier
fieldName
Predicate<List<T>>
message
```

---

# Build a Collection Validator

Pass the configured `CollectionRuleBuilder` to `CollectionValidatorBuilder`:

```java
CollectionValidator<Employee> collectionValidator =
        CollectionValidatorBuilder.<Employee>newInstance()
                .collection(collectionRules)
                .build();
```

The complete flow is:

```text
CollectionRuleBuilder.newInstance()
            │
            ▼
       check(...)
            │
            ▼
CollectionValidationRuleImpl<T>
            │
            ▼
          build()
            │
            ▼
List<CollectionValidationRule<T>>
            │
            ▼
CollectionValidatorImpl<T>
            │
            ▼
CompositeCollectionValidator<T>
            │
            ▼
CollectionValidator<T>
```

---

# Validate a Collection

```java
List<Employee> employees = List.of(
        new Employee()
                .setId("100")
                .setAddress(
                        new Address()
                                .setCity("Pune")
                                .setCountry("India")
                ),

        new Employee()
                .setId("100")
                .setAddress(
                        new Address()
                                .setCity("Mumbai")
                                .setCountry("India")
                )
);

CollectionValidationResult result =
        collectionValidator.validate(employees, 0);
```

The collection-level rule can detect the duplicate ID because it receives the entire list:

```text
List<Employee>
      │
      ▼
Predicate<List<Employee>>
      │
      ▼
duplicate detected
      │
      ▼
Error
```

The `fieldName` identifies the collection/property associated with the error.

---

# Composite Validators

Composite validators combine multiple validators into a single validator.

## Composite Item Validator

```java
CompositeItemValidator<Employee> validator =
        new CompositeItemValidator<>();

validator
        .addRule(employeeIdRulesValidator)
        .addRule(employeeAddressValidator);
```

The composite executes all registered validators and merges their results.

Conceptually:

```text
CompositeItemValidator
        │
        ├── ItemValidator A
        │       └── errors
        │
        ├── ItemValidator B
        │       └── errors
        │
        └── ItemValidator C
                └── errors
                │
                ▼
        ItemValidationResult
```

## Composite Collection Validator

The collection equivalent is:

```java
CompositeCollectionValidator<Employee> validator =
        new CompositeCollectionValidator<>();

validator
        .addRule(firstCollectionValidator)
        .addRule(secondCollectionValidator);
```

All registered collection validators are executed and their errors are aggregated into one `CollectionValidationResult`.

---

# Validation Results

Validation results implement the common `ValidationResult` contract.

```text
ValidationResult
       │
       ├── ItemValidationResult
       │
       └── CollectionValidationResult
```

The common storage structure is:

```java
Map<Integer, List<Error>>
```

Errors are grouped by the supplied validation index.

An `Error` contains:

```java
public class Error {
    private String field;
    private Object rejectedValue;
    private String message;
}
```

Example:

```text
{
    1 : [
        {
            field : "id",
            rejectedValue : "",
            message : "Employee ID is required"
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

# Validation Result Operations

`ValidationResult` provides utility methods for working with errors.

## Add One Error

```java
result.addError(error, index);
```

## Add Multiple Errors

```java
result.addErrors(errors, index);
```

## Merge Another Result

```java
result.addErrorsFrom(otherResult);
```

## Check Whether Validation Succeeded

```java
if (result.isValidationSuccessful()) {
    // no validation errors
}
```

The result is considered successful when its error map is empty.

---

# Validating a Collection of Objects

A common use case is validating records imported from CSV, Excel, or a database.

An `ItemValidator` validates one object at a time. The caller can iterate over the collection and pass the appropriate index.

```java
List<Employee> employees = List.of(
        new Employee()
                .setId("100")
                .setAddress(
                        new Address()
                                .setCity("Pune")
                                .setCountry("India")
                ),

        new Employee()
                .setId("ABC")
                .setAddress(
                        new Address()
                                .setCity("")
                                .setCountry("Unknown")
                ),

        new Employee()
                .setId("")
                .setAddress(null)
);

ItemValidationResult validationResult =
        new ItemValidationResult();

for (int index = 0; index < employees.size(); index++) {
    validationResult.addErrorsFrom(
            employeeValidator.validate(
                    employees.get(index),
                    index
            )
    );
}
```

Read the errors:

```java
validationResult.errors().forEach((index, errors) -> {
    System.out.println("Index: " + index);

    errors.forEach(error -> System.out.printf(
            "  Field: %s, Message: %s%n",
            error.getField(),
            error.getMessage()
    ));
});
```

Example output:

```text
Index: 1
  Field: id, Message: Employee ID is required
  Field: address.city, Message: City is required
  Field: address.country, Message: Only India is supported

Index: 2
  Field: id, Message: Employee ID is required
```

> **Tip:** For CSV or Excel imports, pass the actual source-record index when appropriate. This makes it easier to locate invalid records in the original input.

---

# Combining Collection and Item Validation

Collection and item validation solve different problems.

```text
CollectionValidator
        │
        └── validates List<T>
                │
                └── collection-wide rules


ItemValidator<T>
        │
        └── validates T
                │
                ├── item-level rules
                ├── field-level rules
                └── nested validation
```

A typical import workflow is:

1. Validate the complete collection using `CollectionValidator`.
2. Validate each object using `ItemValidator`.
3. Merge the resulting errors into the application's validation/reporting structure.

For example:

```java
CollectionValidationResult collectionResult =
        collectionValidator.validate(employees, 0);

ItemValidationResult itemResult =
        new ItemValidationResult();

for (int index = 0; index < employees.size(); index++) {
    itemResult.addErrorsFrom(
            employeeValidator.validate(
                    employees.get(index),
                    index
            )
    );
}
```

This separates:

```text
Collection validation
    → "Are employee IDs unique?"


Item validation
    → "Is this employee valid?"


Field validation
    → "Is this employee's country valid?"
```

---

# Nested Validation Error Paths

Nested validation preserves the location of an error by prefixing the nested field name.

For:

```java
Employee
    └── address
          └── city
```

an error produced by the `Address` validator:

```text
city
```

becomes:

```text
address.city
```

For deeper nesting:

```text
Employee
    └── address
          └── location
                └── city
```

the resulting field path can be:

```text
address.location.city
```

This makes the validation result usable with nested object graphs and structured import errors.

---

# Custom Validation Rules

The rule interfaces are designed so that custom validation logic can be implemented without changing the validator architecture.

The three primary rule contracts are:

```java
CollectionValidationRule<T>
ItemValidationRule<T>
FieldValidationRule<T, F>
```

Each rule declares:

```java
RuleIdentifier ruleIdentifier();
String message();
ValidationType validationType();
```

and executes through:

```java
Optional<Error> validate(
        T parentObject,
        V objectToValidate,
        int index);
```

For a field rule:

```text
T → getter → F → predicate → Error
```

For an item rule:

```text
T → predicate → Error
```

For a collection rule:

```text
List<T> → predicate → Error
```

This makes custom rules compatible with the same validator and result aggregation infrastructure.

---

# Validation Types

The library defines three validation scopes:

```java
public enum ValidationType {
    COLLECTION,
    ITEM,
    FIELD
}
```

### `COLLECTION`

Validates a complete collection.

```java
List<T>
```

Use it for duplicate detection, uniqueness, collection size, and cross-record rules.

### `ITEM`

Validates one complete object.

```java
T
```

Use it for rules involving multiple properties of the same object.

### `FIELD`

Validates one field value.

```java
F
```

Use it for field-specific constraints.

---

# Why Rule Identifiers?

Every validation rule can have a stable `RuleIdentifier`.

```java
ValidationRules.COUNTRY_REQUIRED
```

This allows the application to refer to a rule independently of its human-readable error message.

Rule identifiers are useful for:

- logging
- debugging
- reporting
- identifying failed rules
- future configuration
- selectively controlling validation behavior

An enum is recommended for a fixed application-specific rule set because it provides type-safe identifiers.

---

# No Reflection During Validation

The framework uses Java functions and predicates to access and validate values.

For example:

```java
Employee::getId
```

is supplied as the field getter.

The validation path is therefore explicit:

```text
Employee
   │
   │ Function<T, F>
   ▼
field value
   │
   │ Predicate<F>
   ▼
validation result
```

No runtime property-name lookup is required for field extraction.

---

# Fluent Builder Architecture

The builders separate configuration from validation execution.

## Field

```text
FieldRuleBuilder
        │
        ▼
FieldValidationRuleImpl
        │
        ▼
FieldValidator
```

## Item

```text
ItemRuleBuilder
        │
        ▼
ItemValidationRuleImpl
        │
        ▼
ItemValidatorImpl
```

## Collection

```text
CollectionRuleBuilder
        │
        ▼
CollectionValidationRuleImpl
        │
        ▼
CollectionValidatorImpl
```

## Composite execution

```text
CollectionValidatorBuilder
        │
        ▼
CompositeCollectionValidator
        │
        ├── CollectionValidator
        ├── CollectionValidator
        └── ...
```

and:

```text
ItemValidatorBuilder
        │
        ▼
CompositeItemValidator
        │
        ├── ItemValidator
        ├── FieldValidator
        ├── ConditionalItemValidator
        ├── nested ItemValidator
        └── ...
```

---

# Overall Validation Flow

The complete architecture can be viewed as:

```text
                         Validation
                              │
                 ┌────────────┴────────────┐
                 │                         │
                 ▼                         ▼
          CollectionValidator         ItemValidator<T>
                 │                         │
                 ▼                         ├── Item rules
              List<T>                      │
                 │                         ├── Field rules
                 │                         │       │
                 │                         │       ▼
                 │                         │       F
                 │                         │
                 │                         └── Nested validators
                 │
                 └── Collection rules
```

The rule abstraction underneath is:

```text
                    ValidationRule<T, V>
                            │
              ┌─────────────┼─────────────┐
              │             │             │
              ▼             ▼             ▼
         COLLECTION        ITEM         FIELD
              │             │             │
          V=List<T>        V=T          V=F
```

---

# Recommended Application Flow

For a typical batch import:

```text
Input records
     │
     ▼
List<Employee>
     │
     ├─────────────────────────────┐
     │                             │
     ▼                             ▼
CollectionValidator            ItemValidator<Employee>
     │                             │
     │                             ├── employee rules
     │                             ├── field rules
     │                             └── nested Address validator
     │
     ▼                             ▼
Collection errors              Item errors
     │                             │
     └──────────────┬──────────────┘
                    ▼
             Validation report
```

This lets collection-wide constraints and individual-object constraints remain separate while producing a consistent validation model.

---

# Why This Validator?

Compared with annotation-driven validation approaches, this library focuses on:

- Fluent configuration
- Programmatic validation
- Item-level validation
- Field-level validation
- Collection-wide validation
- Nested object validation
- Conditional validation
- Custom validation rules
- Stable rule identifiers
- Aggregated validation results
- No annotations required
- No runtime reflection during validation
- Straightforward unit testing
- Java 21+

---

# License

Apache License 2.0