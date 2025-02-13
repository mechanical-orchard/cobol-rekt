package org.smojol.ast;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.lsp.cobol.core.CobolParser;
import org.smojol.common.ast.FlowNode;
import org.smojol.common.ast.FlowNodeService;
import org.smojol.common.ast.FlowNodeType;
import org.smojol.common.ast.SyntaxIdentity;
import org.smojol.common.vm.interpreter.CobolInterpreter;
import org.smojol.common.vm.interpreter.CobolVmSignal;
import org.smojol.common.vm.interpreter.FlowControl;
import org.smojol.common.vm.stack.StackFrames;

import java.util.List;

@Getter
public class AddFlowNode extends CobolFlowNode {
    private List<CobolParser.AddFromContext> froms;
    private List<CobolParser.AddToContext> tos;
    private List<CobolParser.AddGivingContext> givingDestinations;
    private List<CobolParser.AddToGivingContext> tosGiving;

    public AddFlowNode(ParseTree parseTree, FlowNode scope, FlowNodeService nodeService, StackFrames stackFrames) {
        super(parseTree, scope, nodeService, stackFrames);
    }

    @Override
    public void buildInternalFlow() {
        CobolParser.AddStatementContext addStatement = new SyntaxIdentity<CobolParser.AddStatementContext>(executionContext).get();

        if (addStatement.addToStatement() != null) {
            froms = addStatement.addToStatement().addFrom();
            tos = addStatement.addToStatement().addTo();
            tosGiving = ImmutableList.of();
            givingDestinations = ImmutableList.of();
        }
        else if (addStatement.addToGivingStatement() != null) {
            froms = addStatement.addToGivingStatement().addFrom();
            tos = ImmutableList.of();
            tosGiving = addStatement.addToGivingStatement().addToGiving();
            givingDestinations = addStatement.addToGivingStatement().addGiving();
        }
        super.buildInternalFlow();
    }

    @Override
    public FlowNodeType type() {
        return FlowNodeType.ADD;
    }

    @Override
    public CobolVmSignal acceptInterpreter(CobolInterpreter interpreter, FlowControl flowControl) {
        CobolVmSignal signal = interpreter.scope(this).executeAdd(this, nodeService);
        return flowControl.apply(() -> continueOrAbort(signal, interpreter, nodeService), signal);
    }

    @Override
    public boolean isMergeable() {
        return true;
    }
}
