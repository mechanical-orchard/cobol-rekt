package org.smojol.ast;

import lombok.Getter;
import org.smojol.common.ast.*;

import java.util.ArrayList;
import java.util.List;

public class TransferControlCollectorVisitor implements FlowNodeVisitor {
    @Getter private final List<ControlFlowNode> transfers = new ArrayList<>();

    @Override
    public void visit(FlowNode node, List<FlowNode> outgoingNodes, List<FlowNode> incomingNodes, VisitContext context, FlowNodeService nodeService) {
        if (node instanceof ControlFlowNode) transfers.add((ControlFlowNode) node);
    }

    @Override
    public void visitParentChildLink(FlowNode parent, FlowNode internalTreeRoot, VisitContext ctx, FlowNodeService nodeService) {
    }

    @Override
    public void visitParentChildLink(FlowNode parent, FlowNode internalTreeRoot, VisitContext ctx, FlowNodeService nodeService, FlowNodeCondition hideStrategy) {
    }

    @Override
    public void visitControlTransfer(FlowNode from, FlowNode to, VisitContext visitContext) {

    }

    @Override
    public FlowNodeVisitor newScope(FlowNode enclosingScope) {
        return this;
    }

    @Override
    public void group(FlowNode root) {

    }
}
