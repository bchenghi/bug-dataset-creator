package label;

import java.io.File;
import java.lang.Runnable;

import dataset.model.path.MutationFrameworkPathConfiguration;
import jmutation.model.mutation.DumpFilePathConfig;
import label.io.writer.LabelFileWriter;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;

public class LabelRunner implements Runnable {
	private final String projectName;
	private final String bugId;
	MutationFrameworkPathConfiguration pathConfig;
	
	public LabelRunner(String repoPath, String projectName, int bugId) {
		this.projectName = projectName;
		this.bugId = Integer.toString(bugId); 
		pathConfig = new MutationFrameworkPathConfiguration(repoPath);
	}
	
	public void run() {
		String pathToWorkingTrace = getPathToTrace(true);
		String pathToBuggyTrace = getPathToTrace(false);
		Trace workingTrace = getTrace(pathToWorkingTrace);
		Trace buggyTrace = getTrace(pathToBuggyTrace);
		String pathToLabelFile = getPathToLabelFile();
		LabelFileWriter writer = new LabelFileWriter(pathToLabelFile, workingTrace, 
				buggyTrace, pathConfig.getFixPath(projectName, bugId), pathConfig.getBuggyPath(projectName, bugId), 
				String.join(File.separator, "src", "main", "java"), String.join(File.separator, "src", "test", "java"));
		writer.write();
	}
	
	private String getBugPath() {
		return pathConfig.getBugPath(projectName, bugId);
	}

	private String getPathToTrace(boolean isWorkingTrace) {
		String bugPath = getBugPath();
		if (isWorkingTrace) {
			return String.join(File.separator, bugPath, DumpFilePathConfig.DEFAULT_TRACE_FILE);
		}
		return String.join(File.separator, bugPath, DumpFilePathConfig.DEFAULT_BUGGY_TRACE_FILE);
	}
	
	private String getPathToLabelFile() {
		return pathConfig.getLabelPath(projectName, bugId);
	}
	
	private Trace getTrace(String path) {
		RunningInfo runningInfo = RunningInfo.readFromFile(path);
		return runningInfo.getMainTrace();
	}
	
}
