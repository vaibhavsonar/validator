package io.github.vaibhavsonar.validator.helper;

import io.github.vaibhavsonar.validator.model.RuleIdentifier;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class TestRuleId implements RuleIdentifier {
    private String ruleId;

    public static TestRuleId of(String ruleId) {
        return new TestRuleId()
                .setRuleId(ruleId);
    }

    @Override
    public String ruleIdentifier() {
        return ruleId;
    }
}
