package io.github.vaibhavsonar.validator.helper;

import io.github.vaibhavsonar.validator.ItemValidator;
import io.github.vaibhavsonar.validator.item.ItemValidatorBuilder;
import io.github.vaibhavsonar.validator.helper.model.Address;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Stream;

public class AddressValidator {

    public static ItemValidator<Address> addressValidator() {
        return ItemValidatorBuilder.consider(Address.class)
                .field(
                        "city",
                        Address::getCity,
                        fieldRuleBuilder -> fieldRuleBuilder.check(
                                TestRuleId.of("city-rule-1"),
                                city -> Objects.isNull(city) || city.isBlank(),
                                "City must not be null or empty."
                                )
                )
                .field(
                        "country",
                        Address::getCountry,
                        fieldRuleBuilder -> {
                            fieldRuleBuilder.check(
                                    TestRuleId.of("country-rule-1"),
                                    country -> Objects.isNull(country) || country.isBlank(),
                                    "Country must not be null or empty."
                            );
                            fieldRuleBuilder.check(
                                    TestRuleId.of("country-rule-2"),
                                    country -> !countries().contains(country),
                                    "Invalid country"
                            );
                        }
                )
                .build();
    }

    private static List<String> countries() {
        return Stream.of(Locale.getISOCountries())
                .map(iso -> Locale.of("", iso).getDisplayCountry())
                .toList();
    }
}
