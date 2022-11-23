package label;

import microbat.model.trace.Trace;
import tregression.model.PairList;
import tregression.separatesnapshots.DiffMatcher;
import tregression.tracematch.ControlPathBasedTraceMatcher;

/*
Temporary file
 */
public class Main {
	
	public void assignLabel(Trace correctTrace, Trace buggyTrace) {
		
		// Below are the setup 
		final String srcFolderPath = "";
		final String testFolderPath = "";
		final String mutatedProjPath = "";
		final String originalProjectPath = "";
		
		final DiffMatcher matcher = new DiffMatcher(srcFolderPath, testFolderPath, mutatedProjPath, originalProjectPath);
		matcher.matchCode();
		
		ControlPathBasedTraceMatcher traceMatcher = new ControlPathBasedTraceMatcher();
		final PairList pairList = traceMatcher.matchTraceNodePair(buggyTrace, correctTrace, matcher);
	}
}
