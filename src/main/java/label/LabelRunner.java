package label;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.Runnable;
import java.util.List;

import dataset.model.path.MutationFrameworkPathConfiguration;
import jmutation.model.mutation.DumpFilePathConfig;
import label.io.writer.LabelFileWriter;
import microbat.instrumentation.output.TraceOutputReader;
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
				buggyTrace, pathConfig.getFixPath(projectName, bugId), getBuggyPath(), 
				"src/main/java", "src/test/java");
		writer.write();
		
	}
	
	private String getBuggyPath() {
		return pathConfig.getBuggyPath(projectName, bugId);
	}

	private String getPathToTrace(boolean isWorkingTrace) {
		String bugPath = getBuggyPath();
		if (isWorkingTrace) {
			return String.join(File.separator, bugPath, DumpFilePathConfig.DEFAULT_TRACE_FILE);
		}
		return String.join(File.separator, bugPath, DumpFilePathConfig.DEFAULT_BUGGY_TRACE_FILE);
	}
	
	private String getPathToLabelFile() {
		return pathConfig.getLabelPath(projectName, bugId);
	}
	
	private Trace getTrace(String path) {
		try (FileInputStream inputStream = new FileInputStream(path)) {
			TraceOutputReader reader = new TraceOutputReader(inputStream);
			List<Trace> traces = reader.readTrace();
			for (Trace trace : traces) {
				if (trace.isMain()) {
					return trace;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
}
