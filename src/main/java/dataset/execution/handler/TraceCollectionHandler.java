package dataset.execution.handler;

import dataset.execution.Request;
import dataset.trace.TraceCreator;

public class TraceCollectionHandler extends Handler {
    private final TraceCreator traceCreator;

    public TraceCollectionHandler(Handler next, String repoPath, String projectName, int bugId, int timeout) {
        super(next);
        traceCreator = new TraceCreator(repoPath, projectName, bugId, timeout);
    }

    @Override
    public boolean individualHandler(Request request) {
        // do trace collection for working and buggy
        try {
            traceCreator.run();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
