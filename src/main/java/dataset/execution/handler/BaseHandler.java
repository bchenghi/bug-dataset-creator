package dataset.execution.handler;

import dataset.execution.Request;

public class BaseHandler extends Handler {
	
	@Override
	public void handle(Request request) {
		// NOP
	}
	
	@Override
	public boolean individualHandler(Request request) {
		return false;
	}

}
