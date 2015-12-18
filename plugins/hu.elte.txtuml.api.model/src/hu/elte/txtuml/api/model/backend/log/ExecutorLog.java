package hu.elte.txtuml.api.model.backend.log;

import hu.elte.txtuml.api.model.ModelExecutor;
import hu.elte.txtuml.utils.Logger;

/**
 * This class manages the runtime log of the executor.
 * 
 * @author Gabor Ferenc Kovacs
 *
 */
public final class ExecutorLog {

	private volatile boolean logEvents = false;

	public ExecutorLog() {
		ModelExecutor.Report.addModelExecutionEventsListener(new ModelExecutionEventsListenerImpl(this));
		ModelExecutor.Report.addRuntimeWarningsListener(new RuntimeWarningsListenerImpl(this));
		ModelExecutor.Report.addRuntimeErrorsListener(new RuntimeErrorsListenerImpl(this));
	}

	/**
	 * Sets whether or not valid events should be logged.
	 */
	public void setLogEvents(boolean logEvents) {
		this.logEvents = logEvents;
	}

	/**
	 * Prints a simple message.
	 */
	void out(String message) {
		if (logEvents) {
			Logger.logInfo(message);
		}
	}

	/**
	 * Prints an error message.
	 */
	void err(String message) {
		Logger.logError(message);
	}

	/**
	 * Prints a warning message.
	 */
	void warn(String message) {
		Logger.logWarning(message);
	}

}
