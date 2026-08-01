package io.github.vaibhavsonar.validator.constants;

/**
 * Represents the scope of a validation operation.
 * <p>
 * This enum distinguishes whether validation is performed on a single object
 * or on a collection of objects.
 *
 * @author Vaibhav Sonar
 */
public enum ValidationType {
    /**
     * Indicates validation of a collection of objects, such as records
     * from a CSV, Excel file, or any batch input.
     */
    COLLECTION,

    /**
     * Indicates validation of a single object.
     */
    ITEM
}
