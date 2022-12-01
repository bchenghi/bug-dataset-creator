package dataset.execution.handler;

import dataset.execution.Request;

public abstract class Handler {
	private final Handler nextHandler;
	public Handler() {
		nextHandler = new BaseHandler();
	}
	
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
