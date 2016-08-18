package hu.elte.txtuml.api.model.execution.impl.seqdiag;

import java.util.ArrayList;

import hu.elte.txtuml.api.model.error.seqdiag.InvalidMessageError;
import hu.elte.txtuml.api.model.error.seqdiag.ValidationError;
import hu.elte.txtuml.api.model.execution.impl.DefaultModelExecutor;
import hu.elte.txtuml.api.model.seqdiag.BaseSequenceDiagramExecutor;
import hu.elte.txtuml.api.model.seqdiag.FragmentListener;
import hu.elte.txtuml.api.model.seqdiag.Interaction;
import hu.elte.txtuml.api.model.seqdiag.InteractionWrapper;
import hu.elte.txtuml.api.model.seqdiag.RuntimeContext;

public class SequenceDiagramExecutor implements Runnable, BaseSequenceDiagramExecutor {

	protected InvalidMessageSentListener messageListener;
	protected CommunicationListener traceListener;
	protected DefaultModelExecutor executor;
	protected FragmentCreationListener frCreator;

	private Boolean isLocked;
	private SequenceDiagramExecutorThread thread;
	private Interaction base;
	private ArrayList<ValidationError> errors;

	ArrayList<FragmentListener> frListeners;

	public SequenceDiagramExecutor() {
		isLocked = false;
		frListeners = new ArrayList<FragmentListener>();

		messageListener = new InvalidMessageSentListener(this);
		traceListener = new CommunicationListener(this);
		frCreator = new FragmentCreationListener(this);

		executor = new DefaultModelExecutor();
		executor.addWarningListener(messageListener);
		executor.addTraceListener(traceListener);

		this.addFragmentListener(frCreator);

		errors = new ArrayList<ValidationError>();

	}

	public void setInteraction(Interaction interaction) throws Exception {
		if (isLocked)
			throw new Exception("Invalid method call! Executor is currently executing an interaction");
		this.base = interaction;
	}

	public SequenceDiagramExecutor start() {
		thread = new SequenceDiagramExecutorThread(this);
		thread.start();
		return this;
	}

	public void run() {
		this.start().shutdown().awaitTermination();
	}

	public void execute() {
		isLocked = true;

		InteractionWrapper interaction = ((RuntimeContext) Thread.currentThread()).getRuntime()
				.getInteractionWrapper(base);
		this.thread.getRuntime().setCurrentInteraction(interaction);

		executor.setInitialization(new Runnable() {
			public void run() {
				interaction.getWrapped().initialize();
				interaction.prepare();
				interaction.getWrapped().run();
			}
		});

		executor.launch();
		executor.shutdown();
		executor.awaitTermination();

		if (traceListener.suggestedMessagePattern.size() != 0) {
			this.errors
					.add(new InvalidMessageError(this.thread.getInteractionWrapper().getLifelines().get(0).getWrapped(),
							"The pattern given is bigger than the model"));
		}

	}

	public SequenceDiagramExecutor shutdown() {

		isLocked = false;
		return this;
	}

	public void awaitTermination() {
		try {
			this.thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<ValidationError> getErrors() {
		return this.errors;
	}

	public void addError(ValidationError error) {
		this.errors.add(error);
	}

	public SequenceDiagramExecutor self() {
		return this;
	}

	public SequenceDiagramExecutorThread getThread() {
		return this.thread;
	}

	@Override
	public void addFragmentListener(FragmentListener listener) {
		this.frListeners.add(listener);
	}

	@Override
	public void removeFragmentListener(FragmentListener listener) {
		this.frListeners.remove(listener);

	}
}
