package dataset;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import dataset.bug.creator.BuggyProjectCreator;
import dataset.bug.minimize.ProjectMinimizer;
import dataset.bug.model.path.MutationFrameworkPathConfiguration;
import dataset.bug.model.path.PathConfiguration.InstrumentatorFile;
import dataset.utils.Zipper;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import sav.common.core.SavRtException;

public class BugDataset {
    private final String projectName;
    private final String repoPath;
    private final MutationFrameworkPathConfiguration pathConfig;
    
    public BugDataset(String repoPath, String projectName) {
        super();
        this.repoPath = repoPath;
        this.projectName = projectName;
        pathConfig = new MutationFrameworkPathConfiguration(repoPath);
    }
    
    public BugDataset(String pathToProject) {
        super();
        Path path = Path.of(pathToProject);
        projectName = path.getName(path.getNameCount() - 1).toString();
        repoPath = path.getParent().toString();
        pathConfig = new MutationFrameworkPathConfiguration(repoPath);
    }

    public static void main(String[] args) throws IOException {
        int largestBugId = 17426;
        BugDataset bugdataset = new BugDataset("D:\\chenghin\\NUS\\math_70");
        for (int i = 1; i <= largestBugId; i++) {
            ProjectMinimizer minimizer = bugdataset.createMinimizer(i);
            if (bugdataset.exists(i, true)) {
                try {
                    bugdataset.unzip(i);
                    minimizer.maximise();
                    bugdataset.getData(i);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    minimizer.minimize();
                    bugdataset.zip(i);
                }
            }
        }
    }
    
    // Create bug id ranges based on specified size.
    private List<int[]> createBugRanges(long gbSize, int largestBugId) throws IOException {
        List<int[]> result = new ArrayList<>();
        double oneGb = Math.pow(2, 30);
        double currSize = 0;
        int minBugId = 1;
        for (int i = 1; i <= largestBugId; i++) {
            if (new File(pathConfig.getBugPath(projectName, Integer.toString(i)) + ".zip").exists()) {
                currSize += Files.size(Path.of(pathConfig.getBugPath(projectName, Integer.toString(i)) + ".zip")) / oneGb;
                if (currSize >= gbSize) {
                    result.add(new int[] {minBugId, i});
                    currSize = 0;
                    minBugId = i + 1;
                }
            }
        }
        result.add(new int[] {minBugId, largestBugId});
        return result;
    }
    
    public boolean exists(int bugId, boolean isZipped) {
        String path = pathConfig.getBugPath(projectName, Integer.toString(bugId));
        if (isZipped) path += ".zip";
        return new File(path).exists();
    }
    
    public String getBugIdPath(int bugId) {
        return pathConfig.getBugPath(projectName, Integer.toString(bugId));
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
        Trace buggyTrace;
        Trace workingTrace;
        try {
            buggyTrace = RunningInfo.readFromFile(pathToBuggyTrace).getMainTrace();
            workingTrace = RunningInfo.readFromFile(pathToWorkingTrace).getMainTrace();
        } catch (SavRtException e) {
            throw new IOException(e);
        }
        try {
            RootCause rootCause = new RootCause(Files.readString(Path.of(pathToRootCauseFile)));
            return new BugData(getRootCauseNode(rootCause, workingTrace), buggyTrace, workingTrace, Files.readString(Path.of(pathToTestCaseFile)));
        } catch (NoSuchFileException e) {
            throw new IOException(e);
        }
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
    
    private ProjectMinimizer createMinimizer(int bugId) {
        String bugIdStr = Integer.toString(bugId);
        return new ProjectMinimizer(repoPath, pathConfig.getRelativeBuggyPath(projectName, bugIdStr), 
                pathConfig.getRelativeFixPath(projectName, bugIdStr), pathConfig.getRelativeMetadataPath(projectName, bugIdStr));
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
