package org.smojol.analysis.pipeline;

import java.io.IOException;

public sealed interface AnalysisTaskResult permits AnalysisTaskResultOK, AnalysisTaskResultError {
    boolean isSuccess();
//    static AnalysisTaskResult OK(Object detail) {
//        return OK("<UNKNOWN_TASK>", detail);
//    }

    static AnalysisTaskResult OK(CommandLineAnalysisTask task) {
        return OK(task.name());
    }

    static AnalysisTaskResult OK(String task) {
        return OK(task, new Object());
    }

    static AnalysisTaskResult OK() {
        return OK("<UNKNOWN_TASK>");
    }

    static AnalysisTaskResult OK(CommandLineAnalysisTask task, Object detail) {
        return OK(task.name(), detail);
    }

    static AnalysisTaskResult OK(String task, Object detail) {
        return new AnalysisTaskResultOK(task, detail);
    }

    static AnalysisTaskResult ERROR(Exception detail) {
        return new AnalysisTaskResultError(detail);
    }

    static AnalysisTaskResult ERROR(Exception e, CommandLineAnalysisTask task) {
        return new AnalysisTaskResultError(e, task);
    }
}
