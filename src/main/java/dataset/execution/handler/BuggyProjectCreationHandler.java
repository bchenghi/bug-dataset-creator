package dataset.execution.handler;

import dataset.bug.creator.BuggyProjectCreator;
import dataset.execution.Request;

public class BuggyProjectCreationHandler extends Handler {
    private final BuggyProjectCreator buggyProjectCreator;
    public BuggyProjectCreationHandler(BuggyProjectCreator projectCreator) {
        super(new BaseHandler());
        buggyProjectCreator = projectCreator;
    }

    public BuggyProjectCreationHandler(Handler nextHandler, BuggyProjectCreator projectCreator) {
        super(nextHandler);
        this.buggyProjectCreator = projectCreator;
    }

    @Override
    protected boolean individualHandler(Request request) {
        buggyProjectCreator.run();
        return true;
    }
}
