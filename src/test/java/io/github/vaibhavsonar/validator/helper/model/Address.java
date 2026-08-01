package io.github.vaibhavsonar.validator.helper.model;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Address {
    private String city;
    private String country;
}
