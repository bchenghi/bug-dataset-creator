package dataset.label.io.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import dataset.label.Labeller;
import tregression.model.PairList;
import microbat.model.BreakPoint;
import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import tregression.separatesnapshots.DiffMatcher;
import tregression.tracematch.ControlPathBasedTraceMatcher;

public class LabelFileWriter {
	private final String labelFile;
	private final Trace workingTrace;
	private final Trace buggyTrace;
	private final String workingProjectPath;
	private final String buggyProjectPath;
	private final String srcFolder;
	private final String testFolder;


	public LabelFileWriter(String labelFile, Trace workingTrace, Trace buggyTrace, String workingProjectPath,
			String buggyProjectPath, String srcFolder, String testFolder) {
		super();
		this.labelFile = labelFile;
		this.workingTrace = workingTrace;
		this.buggyTrace = buggyTrace;
		this.workingProjectPath = workingProjectPath;
		this.buggyProjectPath = buggyProjectPath;
		this.srcFolder = srcFolder;
		this.testFolder = testFolder;
		attachFilePathToTrace(workingTrace, String.join(File.separator, workingProjectPath, srcFolder), String.join(File.separator, workingProjectPath, testFolder));
		attachFilePathToTrace(buggyTrace, String.join(File.separator, buggyProjectPath, srcFolder), String.join(File.separator, buggyProjectPath, testFolder));
	}

	public boolean write() {
		DiffMatcher diffMatcher = createDiffMatcher();
		PairList pairList = createPairList(diffMatcher);
		Labeller labeller = new Labeller();
		labeller.assignLabel(workingTrace, buggyTrace, pairList, diffMatcher);
		Map<String, Double> mapOfLabels = createMapOfVarIdToProbs();
		return write(mapOfLabels);
	}

	private boolean write(Map<String, Double> mapOfLabels) {
		File file = new File(labelFile);
		
		JSONObject contents = new JSONObject(mapOfLabels);
		try (FileWriter writer = new FileWriter(file)) {
			contents.write(writer);
		} catch (JSONException | IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private DiffMatcher createDiffMatcher() {
		DiffMatcher result = new DiffMatcher(srcFolder, testFolder, workingProjectPath, buggyProjectPath);
		result.matchCode();
		return result;
	}
	
	private PairList createPairList(DiffMatcher diffMatcher) {
		ControlPathBasedTraceMatcher traceMatcher = new ControlPathBasedTraceMatcher();
		return traceMatcher.matchTraceNodePair(buggyTrace, workingTrace, diffMatcher);
	}
	
	private Map<String, Double> createMapOfVarIdToProbs() {
		Map<String, Double> result = new HashMap<>();
		for (TraceNode node : buggyTrace.getExecutionList()) {
			for (VarValue var : node.getReadVariables()) {
				result.put(var.getVarID(), var.getProbability());
			}
			for (VarValue var : node.getWrittenVariables()) {
				result.put(var.getVarID(), var.getProbability());
			}
		}
		return result;
	}

	private void attachFilePathToTrace(Trace trace, String sourceFolder, String testFolder) {
		for (TraceNode node : trace.getExecutionList()) {
			BreakPoint point = node.getBreakPoint();
			String relativePath = point.getDeclaringCompilationUnitName().replace(".", File.separator) + ".java";
			String sourcePath = sourceFolder + File.separator + relativePath;
			String testPath = testFolder + File.separator + relativePath;
			if(new File(sourcePath).exists()) {
				point.setFullJavaFilePath(sourcePath);
			}
			else if(new File(testPath).exists()) {
				point.setFullJavaFilePath(testPath);
			}
			else {
				System.err.println("cannot find the source code file for " + point);
			}
		}
	}
}
