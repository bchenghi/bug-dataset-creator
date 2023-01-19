package dataset;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import dataset.bug.creator.BuggyProjectCreator;
import dataset.bug.model.path.MutationFrameworkPathConfiguration;
import dataset.bug.model.path.PathConfiguration.InstrumentatorFile;
import dataset.utils.Zipper;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import tregression.empiricalstudy.TestCase;

public class BugDataset {
    private final String projectName;
    private final MutationFrameworkPathConfiguration pathConfig;
    
    public BugDataset(String repoPath, String projectName) {
        super();
        this.projectName = projectName;
        pathConfig = new MutationFrameworkPathConfiguration(repoPath);
    }
    
    public boolean exists(int bugId) {
        return new File(pathConfig.getBugPath(projectName, Integer.toString(bugId))).exists();
    }
    
    public void zip(int bugId) throws IOException {
        String pathToBug = pathConfig.getBugPath(projectName, Integer.toString(bugId));
        Zipper.zip(pathToBug);
        FileUtils.deleteDirectory(new File(pathToBug));
    }
    
    public void unzip(int bugId) throws IOException {
        String pathToBug = pathConfig.getBugPath(projectName, Integer.toString(bugId));
        Zipper.unzip(pathToBug + ".zip", pathConfig.getRepoPath() + File.separator + projectName);
        new File(pathToBug + ".zip").delete();
    }
    
    public BugData getData(int bugId) throws IOException {
        String bugIdStr = Integer.toString(bugId);
        String pathToBug = pathConfig.getBugPath(projectName, bugIdStr);
        String pathToBuggyTrace = pathConfig.getInstrumentatorFilePath(projectName, bugIdStr, InstrumentatorFile.BUGGY_TRACE);
        String pathToWorkingTrace = pathConfig.getInstrumentatorFilePath(projectName, bugIdStr, InstrumentatorFile.TRACE);
        String pathToRootCauseFile = String.join(File.separator, pathToBug, BuggyProjectCreator.ROOTCAUSE_FILE_NAME);
        String pathToTestCaseFile = String.join(File.separator, pathToBug, BuggyProjectCreator.TESTCASE_FILE_NAME);
        Trace buggyTrace = RunningInfo.readFromFile(pathToBuggyTrace).getMainTrace();
        Trace workingTrace = RunningInfo.readFromFile(pathToWorkingTrace).getMainTrace();
        RootCause rootCause = new RootCause(Files.readString(Path.of(pathToRootCauseFile)));
        return new BugData(getRootCauseNode(rootCause, workingTrace), buggyTrace, workingTrace, Files.readString(Path.of(pathToTestCaseFile)));
    }
    
    private int getRootCauseNode(RootCause rootCause, Trace workingTrace) {
        List<TraceNode> executionList = workingTrace.getExecutionList();
        for (TraceNode traceNode : executionList) {
            BreakPoint breakPoint = traceNode.getBreakPoint();
            int lineNum = breakPoint.getLineNumber();
            String classCanonicalName = breakPoint.getDeclaringCompilationUnitName();
            if (classCanonicalName.equals(rootCause.className) && lineNum <= rootCause.endLineNum && lineNum >= rootCause.startLineNum) {
                return traceNode.getOrder();
            }
        }
        return -1;
    }
    
    public static class BugData {
        private final int rootCauseNode;
        private final Trace buggyTrace;
        private final Trace workingTrace;
        private final TestCase testCase;
        
        public BugData(int rootCauseNode, Trace buggyTrace, Trace workingTrace, String testCase) {
            super();
            this.rootCauseNode = rootCauseNode;
            this.buggyTrace = buggyTrace;
            this.workingTrace = workingTrace;
            this.testCase = formTestCase(testCase);
        }

        public int getRootCauseNode() {
            return rootCauseNode;
        }

        public Trace getBuggyTrace() {
            return buggyTrace;
        }

        public Trace getWorkingTrace() {
            return workingTrace;
        }

        public TestCase getTestCase() {
            return testCase;
        }
        
        private TestCase formTestCase(String testCaseStr) {
            // Example: org.apache.commons.math.analysis.ComposableFunctionTest#testComposition(),54,102
            String testClassName = testCaseStr.substring(0, testCaseStr.indexOf("#"));
            String testMethodName = testCaseStr.substring(testCaseStr.indexOf("#") + 1, testCaseStr.indexOf("("));
            return new TestCase(testClassName, testMethodName);
        }
    }
    
    private static class RootCause {
        private final int startLineNum;
        private final int endLineNum;
        private final String className;
        
        public RootCause(String rootCauseStr) {
            // Example: MutationMathOperatorCommand#org.apache.commons.math.analysis.BinaryFunction#lines 37-37#[x + y]
            List<Integer> poundSymbolLocations = new ArrayList<>();
            for (int i = 0; i < rootCauseStr.length(); i++) {
                char c = rootCauseStr.charAt(i);
                if (c == '#') {
                    poundSymbolLocations.add(i);
                }
            }
            className = rootCauseStr.substring(poundSymbolLocations.get(0) + 1, poundSymbolLocations.get(1));
            String lineNumStr = rootCauseStr.substring(poundSymbolLocations.get(1) + 1, poundSymbolLocations.get(2));
            int idxOfDashInLineNumStr = lineNumStr.indexOf("-");
            String startLineStr = lineNumStr.substring(7, idxOfDashInLineNumStr); // skip the "lines" part
            startLineNum = Integer.parseInt(startLineStr);
            String endLineStr = lineNumStr.substring(idxOfDashInLineNumStr + 1);
            endLineNum = Integer.parseInt(endLineStr);
        }
    }
}
