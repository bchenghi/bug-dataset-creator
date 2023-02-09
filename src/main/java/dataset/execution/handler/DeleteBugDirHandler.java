package dataset.execution.handler;

import dataset.execution.Request;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class DeleteBugDirHandler extends Handler {
    private final String dirToDelete;
    public DeleteBugDirHandler(String dirToDelete) {
        super(new BaseHandler());
        this.dirToDelete = dirToDelete;
    }

    public DeleteBugDirHandler(Handler nextHandler, String dirToDelete) {
        super(nextHandler);
        this.dirToDelete = dirToDelete;
    }

    @Override
    protected boolean individualHandler(Request request) {
        // Delete the bug dir if the previous handler tells it to delete
        if (request.hasPassed()) {
            return true;
        }
        delete();
        return false;
    }

    private void delete() {
        try {
            FileUtils.deleteDirectory(new File(dirToDelete));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
