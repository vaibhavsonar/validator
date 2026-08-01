package io.github.vaibhavsonar.validator.helper.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Employee {
    private String id;
    private Address address;
}
