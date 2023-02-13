package dataset.execution;

import dataset.execution.handler.BugCheckHandler;
import dataset.execution.handler.Handler;
import dataset.execution.handler.MaximizeHandler;
import dataset.execution.handler.MinimizeHandler;
import dataset.execution.handler.PrecheckTraceCollectionHandler;
import dataset.execution.handler.TraceCollectionHandler;
import jmutation.model.PrecheckExecutionResult;

public class MinimizeRunner implements Runnable {
    private final Handler startHandler;
    private final MinimizeHandler minimizeHandler;
    public MinimizeRunner(String repoPath, String projectName, int bugId, int instrumentationTimeout) {
        // check if is done (both trace and label) -> maximise -> do precheck & trace + label collection (check if is done).
        // Minimize regardless of failure
        PrecheckTraceCollectionHandler traceHandler = new PrecheckTraceCollectionHandler(repoPath, projectName, bugId,
                instrumentationTimeout);
        MaximizeHandler maximizeHandler = new MaximizeHandler(traceHandler, repoPath, projectName, bugId);
        BugCheckHandler checkHandler = new BugCheckHandler(maximizeHandler, repoPath, projectName, bugId);
        startHandler = checkHandler;
        minimizeHandler = new MinimizeHandler(repoPath, projectName, bugId);
    }

    @Override
    public void run() {
//        startHandler.handle(new Request(true));
        minimizeHandler.handle(new Request(true));
    }
}
