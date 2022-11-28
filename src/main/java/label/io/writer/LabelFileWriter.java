package label.io.writer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import label.Labeller;
import tregression.model.PairList;
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
		return new DiffMatcher(srcFolder, testFolder, workingProjectPath, buggyProjectPath);
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
}
