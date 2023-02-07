package dataset.execution.handler;

import dataset.execution.Request;
import dataset.trace.TraceCreator;

public class BugCheckHandler extends Handler {

    private final TraceCreator traceCreator;

    public BugCheckHandler(Handler next, String repositoryPath, String projectName, int bugId) {
        super(next);
        traceCreator = new TraceCreator(repositoryPath, projectName, bugId);
    }

    public BugCheckHandler(String repositoryPath, String projectName, int bugId) {
        super(new BaseHandler());
        traceCreator = new TraceCreator(repositoryPath, projectName, bugId);
    }

    @Override
    public void handle(Request request) {
        if (individualHandler(request)) {
            nextHandler.handle(request);
            return;
        }
        // Tell the next handle that the traces were not collected
        nextHandler.handle(new Request(false));
    }

    @Override
    public boolean individualHandler(Request request) {
        // Check if traces are present.
        if (traceCreator.isDone()) {
            return false;
        }
        return true;
    }
}
