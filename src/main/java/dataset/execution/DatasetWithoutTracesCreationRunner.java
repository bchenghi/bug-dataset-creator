package dataset.execution;

import dataset.bug.creator.BuggyProjectCreator;
import dataset.bug.model.BuggyProject;
import dataset.execution.handler.BugCheckHandler;
import dataset.execution.handler.BuggyProjectCreationHandler;
import dataset.execution.handler.DeleteBugDirHandler;
import dataset.execution.handler.Handler;
import dataset.execution.handler.MinimizeHandler;
import dataset.execution.handler.ZipBugDirHandler;

import java.io.File;

import static dataset.constants.FileNames.CREATED_BUGGY_PROJECT_FILE;

public class DatasetWithoutTracesCreationRunner implements Runnable {
    private final Handler deleteBugDirHandler;
    private final Handler startHandler;
    public DatasetWithoutTracesCreationRunner(String repoPath, String projectName, int bugId,
                                 BuggyProject buggyProject, String pathToBugDir, String pathToOriginalProj) {

        deleteBugDirHandler = new DeleteBugDirHandler(pathToBugDir);
        ZipBugDirHandler zipBugDirHandler = new ZipBugDirHandler(pathToBugDir);
        MinimizeHandler minimizeHandler = new MinimizeHandler(zipBugDirHandler, repoPath, projectName, bugId);
        BuggyProjectCreationHandler buggyProjectCreationHandler =
                new BuggyProjectCreationHandler(minimizeHandler, new BuggyProjectCreator(repoPath,
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
