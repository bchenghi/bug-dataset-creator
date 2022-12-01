package dataset.execution.handler;

import dataset.execution.Request;
import dataset.label.LabelRunner;

public class LabelHandler extends Handler {
	private final LabelRunner labelRunner;
	
	public LabelHandler(Handler next, String repositoryPath, String projectName, int bugId) {
		super(next);
		labelRunner = new LabelRunner(repositoryPath, projectName, bugId);
	}
	
	public LabelHandler(String repositoryPath, String projectName, int bugId) {
		super();
		labelRunner = new LabelRunner(repositoryPath, projectName, bugId);
	}
	
	@Override 
	public boolean individualHandler(Request request) {
		labelRunner.run();
		return true;
	}
}
