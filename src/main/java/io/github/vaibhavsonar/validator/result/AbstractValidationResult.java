package io.github.vaibhavsonar.validator.result;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import io.github.vaibhavsonar.validator.model.Error;

/**
 * Base implementation of {@link ValidationResult} that provides storage for
 * validation errors.
 * <p>
 * Validation errors are grouped by row number, where the key represents the
 * row associated with the validation errors. For single-object validation,
 * row index is used. For collection validation, the key is represented by the {@code 0}.
 *
 * <p>Subclasses are responsible for specifying the validation type by
 * implementing {@link #validationType()}.
 *
 * @author Vaibhav Sonar
 */
public abstract class AbstractValidationResult implements ValidationResult {

    /**
     * Validation errors grouped by row number.
     */
    private final Map<Integer, List<Error>> errors = new HashMap<>();

    /**
     * Returns the validation errors grouped by row number.
     * <p>
     * The returned map is mutable and reflects the current state of the
     * validation result.
     *
     * @return the validation errors grouped by row number
     */
    @Override
    public Map<Integer, List<Error>> errors() {
        return errors;
    }
}
