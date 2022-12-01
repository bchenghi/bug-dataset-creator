package dataset.trace;

import java.io.File;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

import dataset.bug.Log;
import dataset.bug.model.path.MutationFrameworkPathConfiguration;
import dataset.bug.model.path.PathConfiguration;
import dataset.bug.model.path.PathConfiguration.InstrumentatorFile;
import dataset.bug.model.project.DatasetProject;
import dataset.bug.model.project.MutationFrameworkDatasetProject;
import jmutation.model.TestCase;

public class TraceCreator implements Runnable {
	private final Logger logger = Log.createLogger(TraceCreator.class);
    private final String projectName;
    private final int bugId;
    private final PathConfiguration pathConfig;

    public TraceCreator(String repositoryPath, String projectName, int bugId) {
        this.projectName = projectName;
        this.bugId = bugId;
        pathConfig = new MutationFrameworkPathConfiguration(repositoryPath);
    }
    
    public void run() {
    	if (isDone()) return;
    	runTraceCollection();
    }
    
    private void runTraceCollection() {
        // Get the path to buggy
        // Get names of the trace files
        String buggyPath = pathConfig.getBuggyPath(projectName, Integer.toString(bugId));
        String workingPath = pathConfig.getFixPath(projectName, Integer.toString(bugId));
        // Get test case file
        DatasetProject project = new MutationFrameworkDatasetProject(buggyPath);
        TestCase testCase = project.getFailingTests().get(0);
    	String bugId = Integer.toString(this.bugId);
        TraceCollector workingTraceCollector = new TraceCollector(workingPath, testCase,
        		pathConfig.getInstrumentatorFilePath(projectName, bugId, InstrumentatorFile.PRECHECK),
        		pathConfig.getInstrumentatorFilePath(projectName, bugId, InstrumentatorFile.TRACE),
        		null);
    	logger.info(bugId + " : start working trace collection");
        workingTraceCollector.call();
    	logger.info(bugId + " : end working trace collection");
        TraceCollector buggyTraceCollector = new TraceCollector(buggyPath, testCase,
        		pathConfig.getInstrumentatorFilePath(projectName, bugId, InstrumentatorFile.BUGGY_PRECHECK),
        		pathConfig.getInstrumentatorFilePath(projectName, bugId, InstrumentatorFile.BUGGY_TRACE),
        		null);
    	logger.info(bugId + " : start buggy trace collection");
        buggyTraceCollector.call();
    	logger.info(bugId + " : end buggy trace collection");
    }
    
    public boolean isDone() {
    	String bugId = Integer.toString(this.bugId);
    	List<String> filePaths = new ArrayList<>();
    	for (InstrumentatorFile fileType : InstrumentatorFile.values()) {
    		if (fileType.equals(InstrumentatorFile.PRECHECK) || fileType.equals(InstrumentatorFile.BUGGY_PRECHECK) ||
    				fileType.equals(InstrumentatorFile.BUGGY_TRACE_W_ASSERTS) || fileType.equals(InstrumentatorFile.TRACE_W_ASSERTS)) continue;
    		filePaths.add(pathConfig.getInstrumentatorFilePath(projectName, bugId, fileType));
    	}
    	for (String filePath : filePaths) {
    		File file = new File(filePath);
    		if (!file.exists()) {
    			return false;
    		}
    	}
    	logger.info(bugId + " trace collection is done");
    	return true;
    }
}
