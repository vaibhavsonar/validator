package io.github.vaibhavsonar.validator.helper;

import io.github.vaibhavsonar.validator.model.RuleIdentifier;

public enum TestRule implements RuleIdentifier {
    ID_REQUIRED,
    ID_INVALID,
    COUNTRY_REQUIRED,
    DUPLICATE_ID,
    COLLECTION_EMPTY;

    @Override
    public String ruleIdentifier() {
        return name();
    }
}
