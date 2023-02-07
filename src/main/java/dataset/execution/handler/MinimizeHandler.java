package dataset.execution.handler;

import dataset.bug.minimize.ProjectMinimizer;
import dataset.execution.Request;

public class MinimizeHandler extends Handler {
    private final ProjectMinimizer minimizer;

    public MinimizeHandler(Handler nextHandler, String repositoryPath, String projectName, int bugId) {
        super(nextHandler);
        minimizer = MaximizeHandler.createMinimizer(repositoryPath, projectName, bugId);
    }

    public MinimizeHandler(String repositoryPath, String projectName, int bugId) {
        super(new BaseHandler());
        minimizer = MaximizeHandler.createMinimizer(repositoryPath, projectName, bugId);
    }

    @Override
    public boolean individualHandler(Request request) {
        return minimizer.minimize();
    }
}
