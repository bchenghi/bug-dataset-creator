package dataset.label;

import microbat.model.trace.Trace;
import microbat.model.trace.TraceNode;
import microbat.model.value.VarValue;
import sav.common.core.Pair;
import tregression.StepChangeType;
import tregression.StepChangeTypeChecker;
import tregression.empiricalstudy.MatchStepFinder;
import tregression.model.PairList;
import tregression.separatesnapshots.DiffMatcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Labeller {
	
	/**
	 * Assign label (probability) into each variable in buggyTrace. <br><br>
	 * 
	 * Probability = 1.0 mean the variable is correct. <br><br>
	 * Probability = 0.0 mean the variable is wrong. <br><br>
	 * Probability = -1.0 mean the correctness is undetermined. <br><br>
	 * 
	 * There are several reason for undetermined correctness: <br><br>
	 * 1. Control Incorrect:  Cannot find matched node in correct trace.
	 * There are no meaningful information given. <br><br>
	 * 2. For Each Loop: By now the instrumentation of "For-Each" loop
	 * have some problem such that it cannot give correct information. 
	 * 
	 * @param correctTrace Correct version of trace
	 * @param buggyTrace Wrong version of trace
	 * @param pairList PairList of trace pair
	 * @param matcher DiffMatcher of trace pair
	 */
	public void assignLabel(final Trace correctTrace, final Trace buggyTrace, final PairList pairList, final DiffMatcher matcher) {
		
		Set<String> forEachLoopLocations = new HashSet<>();
		final StepChangeTypeChecker checker = new StepChangeTypeChecker(buggyTrace, correctTrace);

		
		for (final TraceNode node : buggyTrace.getExecutionList()) {
			
			// Initialize label to -1.0
			setAllProb(node.getReadVariables(), -1.0);
			setAllProb(node.getWrittenVariables(), -1.0);
			
			// We assume that for each loop is correct, so skip it
			final String nodeLocation = encodeNodeLocation(node);
			if (forEachLoopLocations.contains(forEachLoopLocations)) {
				continue;
			}
			
			// We only need label for the node with both read and written variables
			if (node.getWrittenVariables().isEmpty() || node.getReadVariables().isEmpty()) {
				continue;
			}
			
			if (isForEachLoop(node)) {
				forEachLoopLocations.add(nodeLocation);
				continue;
			}
			
			TraceNode matchedNode = MatchStepFinder.findMatchedStep(true, node, pairList);
			if (matchedNode == null) {
				// Cannot find matched node because of control incorrect
				// Will skip this node as no ground truth information can be given
				continue;
			}
			
			// Get the list of wrong written variables 
			List<Pair<VarValue, VarValue>> wrongPairs = checker.getWrongWrittenVariableList(true, node, matchedNode, pairList, matcher);
			List<VarValue> wrongVarList = new ArrayList<>();
			for (Pair<VarValue, VarValue> wrongPair : wrongPairs) {
				wrongVarList.add(wrongPair.first());
			}
			
			StepChangeType changeType = checker.getType(node, true, pairList, matcher);
			if (changeType.getType() == StepChangeType.SRC) {
				/**
				 * SRC mean root cause
				 * Set all read variable to correct
				 * Set written variable label based on comparison
				 */
				setAllProb(node.getReadVariables(), 1.0);
				for (VarValue writtenVar : node.getWrittenVariables()) {
					writtenVar.setProbability(wrongVarList.contains(writtenVar) ? 0.0 : 1.0);
				}
			} else if (changeType.getType() == StepChangeType.CTL) {
				// Do nothing for CTL (control incorrect)
			} else if (changeType.getType() == StepChangeType.IDT) {
				// IDT mean node is correct
				// Set all variable to correct
				setAllProb(node.getReadVariables(), 1.0);
				setAllProb(node.getWrittenVariables(), 1.0);
			} else {
				// The only type left is data incorrect
				// Set the variable label based on comparison
				
				// Get the list of wrong read variables
				List<Pair<VarValue, VarValue>> wrongReadVarPair = changeType.getWrongVariableList();
				List<VarValue> wrongReadVarList = new ArrayList<>();
				for (Pair<VarValue, VarValue> pair : wrongReadVarPair) {
					wrongReadVarList.add(pair.first());
				}
			
				for (VarValue readVar : node.getReadVariables()) {
					readVar.setProbability(wrongReadVarList.contains(readVar)?0.0:1.0);
				}
				
				for (VarValue writtenVar : node.getWrittenVariables()) {
					writtenVar.setProbability(wrongVarList.contains(writtenVar)?0.0:1.0);
				}
			}
		}
	}
	
	/**
	 * Helper function to set all the given variable into the same label (probability).
	 * @param vars List of variables
	 * @param prob Probability or label
	 */
	private void setAllProb(List<VarValue> vars, double prob) {
		for (VarValue var : vars) {
			var.setProbability(prob);
		}
	}
	
	/**
	 * Check is the given trace a for-each loop code statement. <br><br>
	 * 
	 * By now we just use a naive way to determine is the node a for-each loop:
	 * If the code statement of this node start with "for(" and it contain a ":",
	 * then it is a for each loop statement.
	 * 
	 * @param node Node to check
	 * @return True if the node is for-each loop. False otherwise.
	 */
	private boolean isForEachLoop(TraceNode node) {
		String code = node.getCodeStatement();
		code = code.replaceAll("\\s+", "");
		if (!code.startsWith("for(")) {
			return false;
		}
		
		int count = 0;
		for (int i = 0; i < code.length(); i++) {
		    if (code.charAt(i) == ':') {
		        count++;
		    }
		}
		
		return count == 1;
	}
	
	private String encodeNodeLocation(TraceNode node) {
		return node.getBreakPoint().getFullJavaFilePath() + "_" + node.getLineNumber();
	}
	
	/**
	 * Construct a dictionary for confidence of calling API method <br><br>
	 * 
	 * Confidence of a API method is the probability that the API
	 * is correctly used (output the correct result). <br><br>
	 * 
	 * Confidence=1 mean we are highly confidence that this API is used correctly. <br><br>
	 * 
	 * Confidence=0 mean we are not sure that this API is used correctly. <br><br>
	 * 
	 * Given a trace pair, we will find the total count (i.e. the total number of this
	 * API is called and the correct count (i.e. count how many time the API is called
	 * correctly. Confidence is calculated as correct count / total count. <br><br>
	 * 
	 * The confidence is store as a dictionary where the key is the API
	 * method signature and the value is the correct count and total count.
	 * @param correctTrace Correct version of trace
	 * @param buggyTrace Buggy version of trace
	 * @param pairList PairList of the trace pair
	 * @param matcher DiffMatcher of the trace pair
	 * @return Dictionary that store the confidence
	 */
	public APIConfidenceDict genAPIConfidenceDict(final Trace correctTrace, final Trace buggyTrace, final PairList pairList, final DiffMatcher matcher) {
		final StepChangeTypeChecker checker = new StepChangeTypeChecker(buggyTrace, correctTrace);
		APIConfidenceDict dictionary = new APIConfidenceDict();
		
		for (TraceNode node : buggyTrace.getExecutionList()) {
			
			// We will only handle the node that is calling API method
			if (node.isCallingAPI()) {
				final StepChangeType changeType = checker.getType(node, true, pairList, matcher);
				final String invokingMethod = node.getInvokingMethod();
				
				if (invokingMethod == "") {
					System.out.println("[Warning] node is calling api but do not have invoking method");
					continue;
				}
				
				/*
				 * Given the source variables are correct:
				 * 
				 * if the target variable are correct, then the API is correctly called.
				 * If the target variable are wrong, then the API is used incorrectly.
				 */
				if (changeType.getType() == StepChangeType.IDT) {
					boolean isCorrect = true;
					dictionary.addRecord(invokingMethod, isCorrect);
				} else if (changeType.getType() == StepChangeType.SRC) {
					boolean isCorrect = false;
					dictionary.addRecord(invokingMethod, isCorrect);
				}
			}
		}
		
		return dictionary;
	}
	
	public APIConfidenceDict aggregateDict(Collection<APIConfidenceDict> dictionaries) {
		return APIConfidenceDict.aggreateDict(dictionaries);
	}
}
