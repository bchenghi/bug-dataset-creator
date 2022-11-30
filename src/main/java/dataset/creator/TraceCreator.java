package dataset.creator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dataset.TraceCollector;
import dataset.minimize.ProjectMinimizer;
import dataset.model.path.MutationFrameworkPathConfiguration;
import dataset.model.path.PathConfiguration;
import dataset.model.path.PathConfiguration.InstrumentatorFile;
import dataset.model.project.DatasetProject;
import dataset.model.project.MutationFrameworkDatasetProject;
import jmutation.model.TestCase;

public class TraceCreator implements Runnable {
    private final String repositoryPath;
    private final String projectName;
    private final int bugId;
    private final PathConfiguration pathConfig;

    public TraceCreator(String repositoryPath, String projectName, int bugId) {
        this.repositoryPath = repositoryPath;
        this.projectName = projectName;
        this.bugId = bugId;
        pathConfig = new MutationFrameworkPathConfiguration(repositoryPath);
    }
    
    private boolean minimize(String buggyProjectPath, int bugId) {
        ProjectMinimizer minimizer = createMinimizer(buggyProjectPath, bugId);
        return minimizer.minimize();
    }
    
    private void maximize(String buggyProjectPath, int bugId) {
        ProjectMinimizer minimizer = createMinimizer(buggyProjectPath, bugId);
        minimizer.maximise();
    }
    
    private ProjectMinimizer createMinimizer(String buggyProjectPath, int bugId) {
        PathConfiguration pathConfiguration = new MutationFrameworkPathConfiguration(repositoryPath);
        String metadataPath = pathConfiguration.getRelativeMetadataPath(projectName, Integer.toString(bugId));
        buggyProjectPath.substring(repositoryPath.length());
        return new ProjectMinimizer(repositoryPath, String.join(File.separator,
                projectName, Integer.toString(bugId), MutationFrameworkDatasetCreator.BUGGY_PROJECT_DIR),
                String.join(File.separator, projectName, MutationFrameworkDatasetCreator.WORKING_PROJECT_DIR),
                metadataPath);
    }
    
    public void run() {
    	if (isDone()) return;
    	StringBuilder mutatedProjPath = new StringBuilder(repositoryPath + File.separator +
            projectName + File.separator);
	    mutatedProjPath.append(bugId);
	    mutatedProjPath.append(File.separator);
	    int mutatedBugPathLen = mutatedProjPath.length();
	    mutatedProjPath.append(MutationFrameworkDatasetCreator.BUGGY_PROJECT_DIR);
	    mutatedProjPath.delete(mutatedBugPathLen, mutatedBugPathLen + 3);
    	maximize(mutatedProjPath.toString(), bugId);
    	runTraceCollection();
    	minimize(mutatedProjPath.toString(), bugId);
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
        		pathConfig.getInstrumentatorFilePath(projectName, bugId, InstrumentatorFile.TRACE_W_ASSERTS));
        workingTraceCollector.call();
        TraceCollector buggyTraceCollector = new TraceCollector(buggyPath, testCase,
        		pathConfig.getInstrumentatorFilePath(projectName, bugId, InstrumentatorFile.BUGGY_PRECHECK),
        		pathConfig.getInstrumentatorFilePath(projectName, bugId, InstrumentatorFile.BUGGY_TRACE),
        		pathConfig.getInstrumentatorFilePath(projectName, bugId, InstrumentatorFile.BUGGY_TRACE_W_ASSERTS));
        buggyTraceCollector.call();
    }
    
    public boolean isDone() {
    	String bugId = Integer.toString(this.bugId);
    	List<String> filePaths = new ArrayList<>();
    	for (InstrumentatorFile fileType : InstrumentatorFile.values()) {
    		filePaths.add(pathConfig.getInstrumentatorFilePath(projectName, bugId, fileType));
    	}
    	for (String filePath : filePaths) {
    		File file = new File(filePath);
    		if (!file.exists()) {
    			return false;
    		}
    	}
    	System.out.println(bugId + " is done");
    	return true;
    }
}
