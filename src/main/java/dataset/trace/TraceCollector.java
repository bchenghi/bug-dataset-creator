package dataset.trace;

import jmutation.execution.ProjectExecutor;
import jmutation.model.ExecutionResult;
import jmutation.model.MicrobatConfig;
import jmutation.model.PrecheckExecutionResult;
import jmutation.model.TestCase;
import jmutation.model.project.ProjectConfig;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeoutException;

import static jmutation.constants.ResourcesPath.DEFAULT_DROP_INS_DIR;
import static jmutation.constants.ResourcesPath.DEFAULT_RESOURCES_PATH;

public class TraceCollector implements Callable<ExecutionResult> {
    private static final String DROP_INS_DIR = String.join(File.separator, DEFAULT_RESOURCES_PATH, DEFAULT_DROP_INS_DIR);
    private static final String MICROBAT_CONFIG_PATH = String.join(File.separator, DEFAULT_RESOURCES_PATH, "microbatConfig.json");
    private TestCase testCase;
    private String precheckDumpFilePath;
    private String traceDumpFilePath;
    private String traceWithAssertsDumpFilePath;
    private MicrobatConfig microbatConfig;
    private ProjectConfig projectConfig;
    private int instrumentationTimeout;


    public TraceCollector(String projectPath, TestCase testCase, String precheckDumpFilePath, String traceDumpFilePath,
            String traceWithAssertsDumpFilePath) {
        this(projectPath, testCase, precheckDumpFilePath, traceDumpFilePath, traceWithAssertsDumpFilePath, 0);
    }

    public TraceCollector(String projectPath, TestCase testCase, String precheckDumpFilePath, String traceDumpFilePath,
                          String traceWithAssertsDumpFilePath, int timeout) {
        microbatConfig = MicrobatConfig.parse(MICROBAT_CONFIG_PATH, projectPath);
        projectConfig = new ProjectConfig(projectPath, DROP_INS_DIR);
        this.testCase = testCase;
        this.precheckDumpFilePath = precheckDumpFilePath;
        this.traceDumpFilePath = traceDumpFilePath;
        this.traceWithAssertsDumpFilePath = traceWithAssertsDumpFilePath;
        this.instrumentationTimeout = timeout;
    }

    public TraceCollector(ProjectConfig projectConfig, TestCase testCase, String precheckDumpFilePath,
            String traceDumpFilePath, String traceWithAssertsDumpFilePath) {
        microbatConfig = MicrobatConfig.defaultConfig(projectConfig.getProjectPath());
        this.projectConfig = projectConfig;
        this.testCase = testCase;
        this.precheckDumpFilePath = precheckDumpFilePath;
        this.traceDumpFilePath = traceDumpFilePath;
        this.traceWithAssertsDumpFilePath = traceWithAssertsDumpFilePath;
    }

    public ExecutionResult call() throws TimeoutException {
        MicrobatConfig updatedMicrobatConfig = microbatConfig.setDumpFilePath(precheckDumpFilePath);
        ProjectExecutor projectExecutor = new ProjectExecutor(updatedMicrobatConfig, projectConfig);
        PrecheckExecutionResult precheckExecutionResult = projectExecutor.execPrecheck(testCase);
        if (precheckExecutionResult == null) {
            System.out.println("precheckExecutionResult was null");
            return null;
        }
        updatedMicrobatConfig = microbatConfig.setDumpFilePath(traceDumpFilePath).setExpectedSteps(precheckExecutionResult.getTotalSteps());
        projectExecutor.setMicrobatConfig(updatedMicrobatConfig);
        ExecutionResult result = projectExecutor.exec(testCase, false, instrumentationTimeout);
        if (traceWithAssertsDumpFilePath == null) {
            return result;
        }
        MicrobatConfig includeAssertionsMutationMicrobatConfig = addAssertionsToMicrobatConfig(updatedMicrobatConfig);
        includeAssertionsMutationMicrobatConfig =
                includeAssertionsMutationMicrobatConfig.setDumpFilePath(traceWithAssertsDumpFilePath);
        projectExecutor.setMicrobatConfig(includeAssertionsMutationMicrobatConfig);
        return projectExecutor.exec(testCase, false);
    }

    private MicrobatConfig addAssertionsToMicrobatConfig(MicrobatConfig config) {
        String[] assertionsArr = new String[]{"org.junit.Assert", "org.junit.jupiter.api.Assertions", "org.testng.Assert"};
        return config.setIncludes(Arrays.asList(assertionsArr));
    }
}
