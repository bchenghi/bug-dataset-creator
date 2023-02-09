package dataset.execution;

import dataset.bug.creator.BuggyProjectCreator;
import dataset.bug.model.BuggyProject;
import dataset.execution.handler.BugCheckHandler;
import dataset.execution.handler.BuggyProjectCreationHandler;
import dataset.execution.handler.DeleteBugDirHandler;
import dataset.execution.handler.Handler;
import dataset.execution.handler.MinimizeHandler;
import dataset.execution.handler.TraceCollectionHandler;
import dataset.execution.handler.ZipBugDirHandler;

import java.io.File;

public class DataSetCreationRunner implements Runnable {
    private final Handler startHandler;
    private final Handler deleteBugDirHandler;
    private final String CREATED_BUGGY_PROJECT_FILE = "createdBugs.json";

    /**
     * Set up handlers to
     * 1. Create buggy project in repo/projName/bugId/bug
     * 2. Collect working and buggy trace
     * 3. Check if traces were collected successfully
     * 4a. Delete dir if trace collection failed, and return
     * 4b. Else do nothing
     * 5. Zip the repo/projName/bugId dir
     * @param repoPath
     * @param projectName
     * @param bugId
     * @param instrumentationTimeout
     * @param buggyProject
     * @param pathToBugDir
     */
    public DataSetCreationRunner(String repoPath, String projectName, int bugId, int instrumentationTimeout,
                                 BuggyProject buggyProject, String pathToBugDir, String pathToOriginalProj) {

        deleteBugDirHandler = new DeleteBugDirHandler(pathToBugDir);

        ZipBugDirHandler zipBugDirHandler = new ZipBugDirHandler(pathToBugDir);
        MinimizeHandler minimizeHandler = new MinimizeHandler(zipBugDirHandler, repoPath, projectName, bugId);
        BugCheckHandler checkHandler = new BugCheckHandler(minimizeHandler, repoPath, projectName, bugId);
        TraceCollectionHandler traceHandler = new TraceCollectionHandler(checkHandler, repoPath,
                projectName, bugId, instrumentationTimeout);

        BuggyProjectCreationHandler buggyProjectCreationHandler =
                new BuggyProjectCreationHandler(traceHandler, new BuggyProjectCreator(repoPath,
                        pathToOriginalProj, buggyProject,
                        repoPath + File.separator + CREATED_BUGGY_PROJECT_FILE, bugId));
        startHandler = buggyProjectCreationHandler;
    }

    @Override
    public void run() {
        try {
            startHandler.handle(new Request(true));
        } catch (RuntimeException e) {
            e.printStackTrace();
        } finally {
            // Delete the bug dir anyway, since either pass or fail, it is no longer necessary
            deleteBugDirHandler.handle(new Request(true));
        }
    }
}
