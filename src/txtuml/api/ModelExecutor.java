package txtuml.api;

import java.io.PrintStream;

import txtuml.importer.MethodImporter;
import txtuml.utils.InstanceCreator;

public final class ModelExecutor<MODEL extends Model> implements ModelElement {
	/*
	 * This class has to be thread-safe as every component will communicate with
	 * it.
	 */

	// settings of execution

	private PrintStream userOutStream = System.out;
	private PrintStream userErrorStream = System.err;
	private PrintStream executorOutStream = System.out;
	private PrintStream executorErrorStream = System.err;
	private boolean executorLog = false;

	private final Object lockOnExecutionTimeMultiplier = new Object();
	private long executionTimeMultiplier = 1;
	private boolean canChangeExecutionTimeMultiplier = true;

	// own private attributes

	private final MODEL model;
	private final ModelExecutorThread thread;

	public ModelExecutor(Class<MODEL> modelClass) {

		if (modelClass == null) {
			throw new IllegalArgumentException(
					"ModelExecutor instance cannot be created with null parameter.");
		}
		this.model = InstanceCreator.createInstanceWithGivenParams(modelClass); // null
																				// parameter
																				// constructor
																				// is
																				// called
		if (this.model == null) {
			throw new IllegalArgumentException(
					"Instantiation of given modelClass type failed: given type cannot be instantiated with null parameter constructor.");
		}

		this.thread = new ModelExecutorThread(this);
	}

	// static getters

	/*
	 * Should only be called from model.
	 */
	public static ModelExecutor<?> getExecutorStatic() {
		return getExecutorThread().getExecutor();
	}

	private static ModelExecutorThread getExecutorThread() {
		try {
			return ((ModelExecutorThread) Thread.currentThread());
		} catch (ClassCastException ex) {
			// TODO: show error
			throw ex;
		}
	}

	// public methods of settings

	/*
	 * In the following setter methods, no synchronization is needed because the
	 * assignment is atomic and if a printing operation is currently active,
	 * that will finish properly either way.
	 */

	public void setUserOutStream(PrintStream userOutStream) {
		this.userOutStream = userOutStream;
	}

	public void setUserErrorStream(PrintStream userErrorStream) {
		this.userErrorStream = userErrorStream;
	}

	public void setExecutorOutStream(PrintStream executorOutStream) {
		this.executorOutStream = executorOutStream;
	}

	public void setExecutorErrorStream(PrintStream executorErrorStream) {
		this.executorErrorStream = executorErrorStream;
	}

	public void setExecutorLog(boolean newValue) {
		executorLog = newValue;
	}

	public void setExecutionTimeMultiplier(long newMultiplier) {
		synchronized (lockOnExecutionTimeMultiplier) {
			if (canChangeExecutionTimeMultiplier) {
				executionTimeMultiplier = newMultiplier;
			} else {
				// TODO show error
			}
		}
	}

	public long getExecutionTimeMultiplier() {
		/*
		 * Reading a long value is not atomic, so synchronization is needed.
		 */
		synchronized (lockOnExecutionTimeMultiplier) {
			return executionTimeMultiplier;
		}
	}

	public void lockExecutionTimeMultiplier() {
		canChangeExecutionTimeMultiplier = false;
	}

	boolean executorLog() {
		return executorLog;
	}

	// EXECUTION

	public void start() {
		this.thread.start();
	}

	void send(ModelClass target, Signal signal) {
		thread.send(target, signal);
	}

	// LOGGING METHODS

	void log(String message) { // user log
		logOnStream(userOutStream, message);
	}

	void logError(String message) { // user log
		logOnStream(userErrorStream, message);
	}

	void executorLog(String message) { // api log
		if (MethodImporter.isImporting()) {

		} else {
			logOnStream(executorOutStream, message);
		}
	}

	void executorFormattedLog(String format, Object... args) { // api
																// log
		if (MethodImporter.isImporting()) {

		} else {
			PrintStream printStream = executorOutStream;
			synchronized (printStream) {
				printStream.format(format, args);
			}
		}
	}

	void executorErrorLog(String message) { // api log
		if (MethodImporter.isImporting()) {

		} else {
			logOnStream(executorErrorStream, message);
		}
	}

	private void logOnStream(PrintStream printStream, String message) {
		synchronized (printStream) {
			printStream.println(message);
		}
	}
}
