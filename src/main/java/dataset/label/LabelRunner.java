package dataset.label;

import java.io.File;
import java.lang.Runnable;
import java.util.logging.Level;
import java.util.logging.Logger;

import dataset.bug.Log;
import dataset.bug.model.path.MutationFrameworkPathConfiguration;
import dataset.label.io.writer.LabelFileWriter;
import jmutation.model.mutation.DumpFilePathConfig;
import microbat.instrumentation.output.RunningInfo;
import microbat.model.trace.Trace;

public class LabelRunner implements Runnable {
	private static final Logger logger = Log.createLogger(LabelRunner.class);
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
		if (!fileExists(pathToWorkingTrace)) {
			logger.log(Level.INFO, pathToWorkingTrace +" does not exist");
			return;
		}
		String pathToBuggyTrace = getPathToTrace(false);		
		if (!fileExists(pathToBuggyTrace)) {
			logger.log(Level.INFO, pathToBuggyTrace +" does not exist");
			return;
		}
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
	
	private boolean fileExists(String path) {
		File file = new File(path);
		return file.exists();
	}
}
