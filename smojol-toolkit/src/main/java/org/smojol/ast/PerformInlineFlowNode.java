package org.smojol.ast;

import org.antlr.v4.runtime.tree.ParseTree;
import org.eclipse.lsp.cobol.core.CobolParser;
import org.smojol.common.ast.*;
import org.smojol.common.vm.stack.StackFrames;

import java.util.List;

import static guru.nidi.graphviz.model.Factory.mutGraph;
import static guru.nidi.graphviz.model.Factory.mutNode;

public class PerformInlineFlowNode extends CompositeCobolFlowNode {
    private FlowNode condition;

    public PerformInlineFlowNode(ParseTree parseTree, FlowNode scope, FlowNodeService nodeService, StackFrames stackFrames) {
        super(parseTree, scope, nodeService, stackFrames);
    }

    @Override
    public void buildInternalFlow() {
        CobolParser.PerformStatementContext performStatement = new SyntaxIdentity<CobolParser.PerformStatementContext>(getExecutionContext()).get();
        CobolParser.PerformInlineStatementContext x = performStatement.performInlineStatement();
        if (isVarying(x)) {
            condition = nodeService.node(x.performType(), this, staticFrameContext);
        }
        super.buildInternalFlow();
    }

    private boolean isVarying(CobolParser.PerformInlineStatementContext performStatement) {
        return performStatement.performType() != null;
    }

    @Override
    public void buildOutgoingFlow() {
        // Call super here because this is still a normal statement which will continue its normal flow, after PERFORM returns
        super.buildOutgoingFlow();
    }

    @Override
    public List<? extends ParseTree> getChildren() {
        CobolParser.PerformStatementContext performStatement = new SyntaxIdentity<CobolParser.PerformStatementContext>(getExecutionContext()).get();
        CobolParser.PerformInlineStatementContext x = performStatement.performInlineStatement();
        return x.conditionalStatementCall();
    }

    @Override
    public void buildControlFlow() {
        CobolParser.PerformStatementContext performStatement = new SyntaxIdentity<CobolParser.PerformStatementContext>(getExecutionContext()).get();
        CobolParser.PerformInlineStatementContext x = performStatement.performInlineStatement();
    }

    @Override
    public void acceptUnvisited(FlowNodeVisitor visitor, int level) {
        super.acceptUnvisited(visitor, level);
        visitor.visitControlTransfer(this, condition, new VisitContext(level));
        // TODO: Make group() work correctly. Right now, it is prone to creating double links.
//        visitor.group(internalTreeRoot);
    }

    @Override
    public String label() {
        return truncated(originalText(), 30);
    }

    @Override
    public FlowNodeType type() {
        return FlowNodeType.PERFORM;
    }
}
