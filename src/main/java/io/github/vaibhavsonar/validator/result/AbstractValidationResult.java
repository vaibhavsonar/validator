package io.github.vaibhavsonar.validator.result;

import io.github.vaibhavsonar.validator.model.Error;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base implementation of {@link ValidationResult} that stores validation
 * errors grouped by an integer index.
 * <p>
 * An {@code AbstractValidationResult} provides the common error-storage
 * mechanism used by concrete validation result types. Validation errors are
 * associated with an index, allowing multiple validation operations to be
 * represented in a single result.
 *
 * <p>For item-level validation, the index typically identifies the item or
 * row that produced the validation errors. For collection-level validation,
 * the index can be used according to the collection validation contract.
 *
 * <p>This class is responsible only for storing and exposing validation
 * errors. Concrete subclasses are responsible for identifying their
 * validation scope by implementing {@link ValidationResult#validationType()}.
 *
 * @author Vaibhav Sonar
 */
public abstract class AbstractValidationResult implements ValidationResult {

    /**
     * Validation errors grouped by their associated index.
     * <p>
     * Each map entry associates an index with the validation errors produced
     * for that index. Multiple errors can therefore be associated with the
     * same index.
     */
    private final Map<Integer, List<Error>> errors = new HashMap<>();

    /**
     * Returns the validation errors grouped by index.
     * <p>
     * The returned map is the mutable map maintained by this result. Changes
     * made to the returned map therefore affect the current validation result.
     *
     * @return the validation errors grouped by their associated index
     */
    @Override
    public Map<Integer, List<Error>> errors() {
        return errors;
    }
}
