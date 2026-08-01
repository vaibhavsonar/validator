package io.github.vaibhavsonar.validator.model;

import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Represents a validation error for a field.
 * <p>
 * Each instance captures the field that failed validation, the rejected value,
 * and a human-readable validation message describing the reason for the failure.
 * Validation frameworks typically return one or more {@code Error} instances
 * as part of a validation result.
 *
 * <p>This class uses chained accessors, allowing properties to be set fluently:
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
     * Name of the field that failed validation.
     */
    private String field;

    /**
     * Value that was rejected during validation.
     */
    private Object rejectedValue;

    /**
     * Human-readable message describing the validation failure.
     */
    private String message;
}
