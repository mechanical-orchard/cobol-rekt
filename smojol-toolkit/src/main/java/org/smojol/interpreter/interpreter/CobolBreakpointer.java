package org.smojol.interpreter.interpreter;

import org.smojol.common.ast.FlowNode;
import org.smojol.common.ast.FlowNodeCondition;
import org.smojol.interpreter.debugger.DebuggerShell;
import org.smojol.common.vm.interpreter.Breakpointer;
import org.smojol.common.vm.interpreter.CobolVmSignal;
import org.smojol.common.vm.stack.ExecutionContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.smojol.common.flowchart.ConsoleColors.coloured;

public class CobolBreakpointer implements Breakpointer {
    private final List<FlowNodeCondition> breakpoints = new ArrayList<>();
    private final DebuggerShell shell = new DebuggerShell();

    @Override
    public void addBreakpoint(FlowNodeCondition breakpoint) {
        breakpoints.add(breakpoint);
    }

    @Override
    public CobolVmSignal run(Supplier<CobolVmSignal> execution, ExecutionContext executionContext) {
        FlowNode node = executionContext.node();
        boolean shouldBreak = breakpoints.stream().anyMatch(bp -> bp.apply(node));
        if (!shouldBreak) return execution.get();
        System.out.println(coloured(String.format("Breakpoint hit at %s. Type 'c' to resume, 'q' to halt, 'stack' to print stack. Type any other string to evaluate a variable matching that name.", node.originalText().trim()), 0, 202));
        shell.run(executionContext);
        return execution.get();
    }
}
