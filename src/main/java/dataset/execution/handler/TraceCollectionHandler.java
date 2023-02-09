package dataset.execution.handler;

import dataset.execution.Request;
import dataset.trace.TraceCreator;

public class TraceCollectionHandler extends Handler {
    private final TraceCreator traceCreator;
    
    public TraceCollectionHandler(String repositoryPath, String projectName, int bugId, int timeout) {
        super(new BaseHandler());
        traceCreator = new TraceCreator(repositoryPath, projectName, bugId, timeout);
    }
    
    public TraceCollectionHandler(Handler next, String repoPath, String projectName, int bugId, int timeout) {
        super(next);
        traceCreator = new TraceCreator(repoPath, projectName, bugId, timeout);
    }

    @Override
    public boolean individualHandler(Request request) {
        // do trace collection for working and buggy
        try {
            traceCreator.run();
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            return true;
        }
    }
}
