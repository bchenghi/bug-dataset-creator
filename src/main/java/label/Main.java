package label;

import java.util.concurrent.ExecutorService;

import dataset.DatasetHelper;

public class Main {
	public static void main(String[] args) {
//		createLabels();
	}
	
	private void createLabels() {
		final int startBugId = 1;
		final int endBugId = 1000;
		final String projectName = "math_70";
		final String repoPath = "";
		ExecutorService executor = DatasetHelper.createExecutorService();
		for (int bugId = startBugId; bugId < endBugId; bugId++) {
			executor.submit(new LabelRunner(repoPath, projectName, bugId));
		}
		executor.shutdown();
	}
}
