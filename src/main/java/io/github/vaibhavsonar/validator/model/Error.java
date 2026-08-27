package io.github.vaibhavsonar.validator.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Represents a validation error produced when a validation rule fails.
 * <p>
 * An {@code Error} contains the field or property associated with the
 * validation failure, the value that was rejected by the validation rule,
 * and a human-readable message describing the failure.
 *
 * <p>The {@code field} may identify a field on the validated object or a
 * collection-level validation target. Nested fields may be represented using
 * dot notation. For example, {@code address.city} identifies the
 * {@code city} field of an {@code address} object.
 *
 * <p>The {@code rejectedValue} contains the value that caused the validation
 * failure. Depending on the validation scope, this may be a field value, an
 * entire object, or a collection.
 *
 * <p>This class uses Lombok's chained accessors, allowing validation errors
 * to be constructed using a fluent API:
 *
 * <pre>{@code
 * Error error = new Error()
 *         .setField("age")
 *         .setRejectedValue(-1)
 *         .setMessage("Age must be greater than or equal to 0");
 * }</pre>
 *
 * @author Vaibhav Sonar
 */
@Data
@Accessors(chain = true)
public class Error {

    /**
     * Name of the field or validation target associated with the failure.
     * <p>
     * For nested objects, the field may use dot notation, such as
     * {@code address.city}. For validation that applies to an entire object
     * or collection, the field may identify the object or collection as a
     * whole.
     */
    private String field;

    /**
     * Value rejected by the validation rule.
     * <p>
     * The rejected value depends on the validation scope. It may represent
     * an individual field value, an entire item, or an entire collection.
     */
    private Object rejectedValue;

    /**
     * Human-readable message describing why the validation failed.
     */
    private String message;
}
