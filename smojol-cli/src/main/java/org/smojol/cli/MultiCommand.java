package org.smojol.cli;

import org.eclipse.lsp.cobol.common.error.SyntaxError;
import org.smojol.analysis.LanguageDialect;
import org.smojol.analysis.pipeline.*;
import org.smojol.common.flowchart.ConsoleColors;
import org.smojol.common.id.UUIDProvider;
import org.smojol.interpreter.FlowchartGenerationStrategy;
import org.smojol.interpreter.structure.OccursIgnoringFormat1DataStructureBuilder;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.function.Function;

@Command(name = "run", mixinStandardHelpOptions = true, version = "graph 0.1",
        description = "Implements various operations useful for reverse engineering Cobol code")
public class MultiCommand implements Callable<Integer> {
    @Option(names = {"-dp", "--dialectJarPath"},
            defaultValue = "che-che4z-lsp-for-cobol-integration/server/dialect-idms/target/dialect-idms.jar",
            description = "Path to dialect .JAR")
    private String dialectJarPath;

    @Option(names = {"-s", "--srcDir"},
            required = true,
            description = "The Cobol source directory")
    private String sourceDir;

    @Option(names = {"-c", "--commands"},
            required = true,
            description = "The commands to run (INJECT_INTO_NEO4J, EXPORT_TO_GRAPHML, WRITE_RAW_AST, DRAW_FLOWCHART, WRITE_FLOW_AST, WRITE_CFG, ATTACH_COMMENTS, WRITE_DATA_STRUCTURES, BUILD_PROGRAM_DEPENDENCIES, COMPARE_CODE)", split = " ")
    private List<String> commands;

    @CommandLine.Parameters(index = "0..*",
            description = "The programs to analyse", split = " ")
    private List<String> programNames;

    // Can be replaced with a File[] (and the later conversion removed) if we skip default arguments.
    @Option(names = {"-cp", "--copyBooksDir"},
            required = true,
            description = "Copybook directories (repeatable)", split = ",")
    private List<String> copyBookDirs;

    @Option(names = {"-r", "--reportDir"},
            required = true,
            description = "Output report directory")
    private String reportRootDir;

    @Option(names = {"-d", "--dialect"},
            defaultValue = "COBOL",
            description = "The COBOL dialect (COBOL, IDMS)")
    private String dialect;

    @Option(names = {"-g", "--generation"},
            defaultValue = "PROGRAM",
            description = "The flowchart generation strategy. Valid values are SECTION, PROGRAM, and NODRAW")
    private String flowchartGenerationStrategy;

    @Option(names = {"-v", "--validate"},
            defaultValue = "false",
            description = "Only run syntax validation on the input")
    private boolean isValidate;

    @Option(names = {"-f", "--flowchartOutputFormat"},
            defaultValue = "svg",
            description = "Format of the flowchart output (PNG, SVG)")
    private String flowchartOutputFormat;

    @Override
    public Integer call() throws IOException {
        List<File> copyBookPaths = copyBookDirs.stream().map(c -> Paths.get(c).toAbsolutePath().toFile()).toList();
        Integer returnCode = processPrograms(copyBookPaths);
        if (returnCode != null) return returnCode;
        return 0;
    }

    private Integer processPrograms(List<File> copyBookPaths) throws IOException {
        CodeTaskRunner taskRunner = new CodeTaskRunner(sourceDir, reportRootDir, copyBookPaths, dialectJarPath, LanguageDialect.dialect(dialect), FlowchartGenerationStrategy.strategy(flowchartGenerationStrategy, flowchartOutputFormat), new UUIDProvider(), new OccursIgnoringFormat1DataStructureBuilder());
        copyBookPaths.forEach(cpp -> System.out.println(cpp.getAbsolutePath()));
        Map<String, List<AnalysisTaskResult>> runResults = taskRunner.generateForPrograms(toGraphTasks(commands), programNames, isValidate ? TaskRunnerMode.DIAGNOSTIC_MODE : TaskRunnerMode.PRODUCTION_MODE);
        if (!isValidate) return processResults(runResults);
        System.out.println("Only validating, all other tasks were ignored");
        output(taskRunner.getErrorMap(), SyntaxError::toString);
        output(taskRunner.getErrorMap(), e -> e.getErrorCode() + ": " + e.getSuggestion());
        return null;
    }

    private Integer processResults(Map<String, List<AnalysisTaskResult>> runResults) {
        List<String> results = runResults.entrySet().stream().map(this::taskResults).toList();
        results.forEach(System.out::println);
        return 0;
    }

    private String taskResults(Map.Entry<String, List<AnalysisTaskResult>> taskResult) {
        return String.join("\n", taskResult.getValue().stream().map(e -> taskResult.getKey()
                + " -> " + e.toString()).toList()) + "\n";
    }

    private void output(Map<String, List<SyntaxError>> errorMap, Function<SyntaxError, String> format) {
        if (errorMap.isEmpty()) System.out.println(ConsoleColors.green("No errors found for programs " + String.join(",", programNames)));
        errorMap.forEach((programName, errors) -> {
            System.out.printf("Program %s%n----------------------%n", programName);
            errors.forEach(e -> System.out.printf("%s%n", format.apply(e)));
        });
    }

    private List<CommandLineAnalysisTask> toGraphTasks(List<String> commands) {
        return commands.stream().map(CommandLineAnalysisTask::valueOf).toList();
    }
}
