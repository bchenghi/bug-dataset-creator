package dataset.execution;

import dataset.execution.handler.BugCheckHandler;
import dataset.execution.handler.Handler;
import dataset.execution.handler.MaximizeHandler;
import dataset.execution.handler.MinimizeHandler;
import dataset.execution.handler.TraceCollectionHandler;

public class Runner implements Runnable {
    private final Handler startHandler;
    private final MinimizeHandler minimizeHandler;
    public Runner(String repoPath, String projectName, int bugId, int instrumentationTimeout) {
        // check if is done (both trace and label) -> maximise -> do trace + label collection (check if is done).
        // Minimize regardless of failure
        
        TraceCollectionHandler traceHandler = new TraceCollectionHandler(repoPath, projectName, bugId, instrumentationTimeout);
        MaximizeHandler maximizeHandler = new MaximizeHandler(traceHandler, repoPath, projectName, bugId);
        BugCheckHandler checkHandler = new BugCheckHandler(maximizeHandler, repoPath, projectName, bugId);
        startHandler = checkHandler;
        
        minimizeHandler = new MinimizeHandler(repoPath, projectName, bugId);
    }

    @Override
    public void run() {
        try {
            startHandler.handle(new Request(true));
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
        minimizeHandler.handle(new Request(true));
    }
}
