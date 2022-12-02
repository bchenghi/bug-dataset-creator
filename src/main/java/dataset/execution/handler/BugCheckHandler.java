package dataset.execution.handler;

import dataset.execution.Request;
import dataset.label.LabelRunner;
import dataset.trace.TraceCreator;

public class BugCheckHandler extends Handler {

	private final LabelRunner labelRunner;
	private final TraceCreator traceCreator;
	
	public BugCheckHandler(Handler next, String repositoryPath, String projectName, int bugId) {
		super(next);
		traceCreator = new TraceCreator(repositoryPath, projectName, bugId);
		labelRunner = new LabelRunner(repositoryPath, projectName, bugId);
	}
	
	public BugCheckHandler(String repositoryPath, String projectName, int bugId) {
		super(new BaseHandler());
		traceCreator = new TraceCreator(repositoryPath, projectName, bugId);
		labelRunner = new LabelRunner(repositoryPath, projectName, bugId);
	}
	
	@Override
	public boolean individualHandler(Request request) {
		// Check if label + traces are present.
		if (traceCreator.isDone() && labelRunner.isDone()) {
			return false;
		}
		return true;
	}
}
