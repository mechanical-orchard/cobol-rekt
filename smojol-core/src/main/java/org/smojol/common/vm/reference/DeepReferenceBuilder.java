package org.smojol.common.vm.reference;

import org.eclipse.lsp.cobol.core.CobolParser;
import org.smojol.common.vm.expression.ArithmeticExpressionVisitor;
import org.smojol.common.vm.expression.CobolExpression;
import org.smojol.common.vm.expression.PrimitiveCobolExpression;
import org.smojol.common.vm.structure.AccessChain;
import org.smojol.common.vm.structure.CobolDataStructure;
import org.smojol.common.vm.type.LiteralResolver;
import org.smojol.common.vm.type.TypedRecord;

import java.util.List;

public class DeepReferenceBuilder {
    public CobolReference getReference(CobolParser.MoveToSendingAreaContext from, CobolDataStructure dataStructure) {
        if (from.generalIdentifier() != null) return getReference(from.generalIdentifier(), dataStructure);
        return new PrimitiveReference(typedValue(from));
    }

    private TypedRecord typedValue(CobolParser.MoveToSendingAreaContext from) {
        String v = new LiteralResolver().resolvedLiteral(from.literal());
        return TypedRecord.typedString(v.toString());
    }

    public CobolReference getReference(CobolParser.AddFromContext from, CobolDataStructure dataStructure) {
        if (from.generalIdentifier() != null) return getReference(from.generalIdentifier(), dataStructure);
        return new PrimitiveReference(TypedRecord.typedNumber(from.literal().getText()));
    }

    public CobolReference getReference(CobolParser.SubtractSubtrahendContext rhs, CobolDataStructure dataStructure) {
        if (rhs.generalIdentifier() != null) return getReference(rhs.generalIdentifier(), dataStructure);
        return new PrimitiveReference(TypedRecord.typedNumber(rhs.literal().getText()));
    }

    public CobolReference getReference(CobolParser.MultiplyLhsContext lhs, CobolDataStructure dataStructure) {
        if (lhs.generalIdentifier() != null) return getReference(lhs.generalIdentifier(), dataStructure);
        return new PrimitiveReference(TypedRecord.typedNumber(lhs.literal().getText()));
    }

    public CobolReference getReference(CobolParser.DivisorContext divisor, CobolDataStructure dataStructure) {
        if (divisor.generalIdentifier() != null) return getReference(divisor.generalIdentifier(), dataStructure);
        return new PrimitiveReference(TypedRecord.typedNumber(divisor.literal().getText()));
    }

    public CobolReference getReference(CobolParser.GeneralIdentifierContext to, CobolDataStructure data) {
        return new VariableCobolReference(resolve(to, data));
    }

    public CobolReference getReference(CobolParser.AddToContext to, CobolDataStructure data) {
        return getReference(to.generalIdentifier(), data);
    }

    public CobolReference getReference(CobolParser.SubtractMinuendContext lhs, CobolDataStructure data) {
        return getReference(lhs.generalIdentifier(), data);
    }

    public CobolReference getReference(CobolParser.QualifiedDataNameContext nameContext, CobolDataStructure data) {
        return new VariableCobolReference(resolve(nameContext, data));
    }

    public CobolDataStructure resolve(CobolParser.GeneralIdentifierContext to, CobolDataStructure data) {
        CobolParser.QualifiedDataNameContext qualifiedDataNameContext = to.qualifiedDataName();
        return resolve(qualifiedDataNameContext, data);
    }

    public CobolReference getShallowReference(CobolParser.QualifiedDataNameContext nameContext, CobolDataStructure data) {
        return new VariableCobolReference(data.reference(nameContext.variableUsageName().getText()));
    }

    private static CobolDataStructure resolve(CobolParser.QualifiedDataNameContext qualifiedDataNameContext, CobolDataStructure data) {
        CobolDataStructure reference = data.reference(qualifiedDataNameContext.variableUsageName().getText());
        if (qualifiedDataNameContext.tableCall() == null) return reference;

        // TODO: Might precompute this
        AccessChain chain = data.chain(qualifiedDataNameContext.variableUsageName().getText());
        List<CobolParser.ArithmeticExpressionContext> indices = qualifiedDataNameContext.tableCall().arithmeticExpression();
        List<Integer> resolvedIndices = indices.stream().map(index -> resolve(data, index)).toList();
        List<Integer> fixedIndices = resolvedIndices.stream().map(i -> i == 0 ? 1 : i).toList();

        CobolDataStructure struct = chain.run(fixedIndices);
        return struct;
    }

    private static int resolve(CobolDataStructure data, CobolParser.ArithmeticExpressionContext index) {
        ArithmeticExpressionVisitor arithmeticExpressionVisitor = new ArithmeticExpressionVisitor();
        index.accept(arithmeticExpressionVisitor);
        CobolExpression evaluatedIndex = arithmeticExpressionVisitor.getExpression().evaluate(data);
        int tableIndex = (int) evaluatedIndex.evalAsNumber(data);
        return tableIndex;
    }

    public CobolReference getReference(PrimitiveCobolExpression value) {
        return new PrimitiveReference(value.data());
    }
}
