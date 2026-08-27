package io.github.vaibhavsonar.validator.helper;

import io.github.vaibhavsonar.validator.model.RuleIdentifier;

public enum TestRule implements RuleIdentifier {
    ID_REQUIRED,
    ID_INVALID,
    COUNTRY_REQUIRED,
    DUPLICATE_ID,
    COLLECTION_EMPTY,
    NAME_REQUIRED,
    NAME_INVALID,
    AGE_REQUIRED,
    AGE_INVALID,
    AGE_RANGE_INVALID,
    ACTIVE_REQUIRED,
    ACTIVE_INVALID,
    ADDRESS_REQUIRED,
    CITY_REQUIRED,
    CITY_INVALID,
    COUNTRY_INVALID,
    POSTAL_CODE_REQUIRED,
    POSTAL_CODE_INVALID,
    EMPLOYEE_INVALID,
    EMPLOYEE_REQUIRED;

    @Override
    public String ruleIdentifier() {
        return name();
    }
}
