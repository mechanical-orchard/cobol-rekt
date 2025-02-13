package org.smojol.common.vm.expression;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.smojol.common.vm.structure.CobolDataStructure;

public class SimpleConditionExpression extends CobolExpression {
    @Getter private final CobolExpression lhs;
    @Getter private final RelationExpression comparison;
    @Getter private boolean isStandalone = false;

    public SimpleConditionExpression(CobolExpression lhs, CobolExpression comparison) {
        super(ImmutableList.of(lhs, comparison));
        this.lhs = lhs;
        this.comparison = (RelationExpression) comparison;
    }

    public SimpleConditionExpression(CobolExpression arithmeticExpression) {
        super(ImmutableList.of());
        this.lhs = arithmeticExpression;
        this.comparison = null;
    }

    @Override
    public CobolExpression evaluate(CobolDataStructure data) {
        if (comparison != null) return comparison.evaluate(lhs, data);
        System.out.println("Comparison clause not present. Will check for level 88 condition...");

        // Level 88 variable condition
        return PrimitiveCobolExpression.primitive(lhs.evaluate(data));
    }

    public CobolExpression standalone() {
        this.isStandalone = true;
        return this;
    }
}
