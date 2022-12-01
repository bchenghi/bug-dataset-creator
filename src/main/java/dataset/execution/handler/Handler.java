package dataset.execution.handler;

import dataset.execution.Request;

public abstract class Handler {
	protected final Handler nextHandler;
	
	public Handler(Handler nextHandler) {
		this.nextHandler = nextHandler;
	}
	
	public void handle(Request request) {
		if (!individualHandler(request)) {
			return;
		}
		nextHandler.handle(request);
	}
	
	protected abstract boolean individualHandler(Request request);
}
