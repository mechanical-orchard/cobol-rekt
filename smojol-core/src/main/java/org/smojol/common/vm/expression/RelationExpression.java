package org.smojol.common.vm.expression;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.smojol.common.vm.structure.CobolDataStructure;

public class RelationExpression extends CobolExpression {
    @Getter
    private final ComparisonOperator relationalOperation;
    @Getter
    private final CobolExpression rhs;

    public RelationExpression(ComparisonOperator relationalOperation, CobolExpression rhs) {
        super(ImmutableList.of(rhs));
        this.relationalOperation = relationalOperation;
        this.rhs = rhs;
    }

    @Override
    public CobolExpression evaluate(CobolDataStructure data) {
        throw new IllegalArgumentException("Cannot call evaluate() on a relation expression");
    }

    public CobolExpression evaluate(CobolExpression lhs, CobolDataStructure data) {
        return relationalOperation.apply(lhs.evaluate(data), rhs.evaluate(data), data);
    }
}
