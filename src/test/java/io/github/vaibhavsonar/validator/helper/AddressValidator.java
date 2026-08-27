package io.github.vaibhavsonar.validator.helper;

import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.helper.model.Address;
import io.github.vaibhavsonar.validator.item.ItemValidatorBuilder;
import io.github.vaibhavsonar.validator.rule.builder.FieldRuleBuilder;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

public class AddressValidator {

    public static ItemValidator<Address> addressValidator() {
        return ItemValidatorBuilder.<Address>newInstance()
                .field(
                        FieldRuleBuilder.<Address, String>newInstance()
                                .check(
                                        TestRuleId.of("city-rule-1"),
                                        "city",
                                        Address::getCity,
                                        city -> Objects.isNull(city) || city.isBlank(),
                                        "City must not be null or empty."
                                )
                )
                .field(
                        FieldRuleBuilder.<Address, String>newInstance()
                                .check(
                                        TestRuleId.of("country-rule-2"),
                                        "country",
                                        Address::getCountry,
                                        country -> Objects.isNull(country) || country.isBlank(),
                                        "Country must not be null or empty."
                                )
                                .check(
                                        TestRuleId.of("country-rule-3"),
                                        "country",
                                        Address::getCountry,
                                        country -> !countries().contains(country),
                                        "Invalid country"
                                )
                )
                .build();
    }

    private static List<String> countries() {
        return Stream.of(Locale.getISOCountries())
                .map(iso -> Locale.of("", iso).getDisplayCountry())
                .toList();
    }
}
