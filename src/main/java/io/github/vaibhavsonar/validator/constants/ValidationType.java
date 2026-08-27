package io.github.vaibhavsonar.validator.constants;

/**
 * Defines the scope at which a validation rule is applied.
 * <p>
 * Validation can be performed at three levels:
 * <ul>
 *     <li>{@link #COLLECTION} &mdash; validation of an entire collection
 *     of items</li>
 *     <li>{@link #ITEM} &mdash; validation of a single item</li>
 *     <li>{@link #FIELD} &mdash; validation of a field belonging to a
 *     single item</li>
 * </ul>
 *
 * <p>The validation type is used to distinguish the scope of a validation
 * rule and to determine which validator is responsible for executing it.
 *
 * @author Vaibhav Sonar
 */
public enum ValidationType {
    /**
     * Indicates validation of an entire collection of items.
     * <p>
     * Collection-level validation is appropriate for rules that require
     * access to multiple items at the same time, such as duplicate detection,
     * uniqueness checks, collection size constraints, and cross-item
     * consistency checks.
     */
    COLLECTION,

    /**
     * Indicates validation of a single item.
     * <p>
     * Item-level validation evaluates the complete object independently of
     * the values of other items in the collection.
     */
    ITEM,

    /**
     * Indicates validation of a field belonging to a single item.
     * <p>
     * Field-level validation evaluates an individual field value extracted
     * from the item being validated.
     */
    FIELD
}