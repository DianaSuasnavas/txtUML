package hu.elte.txtuml.api.model.execution.impl.seqdiag;

import java.util.LinkedList;

import hu.elte.txtuml.api.model.ModelClass;
import hu.elte.txtuml.api.model.Signal;
import hu.elte.txtuml.api.model.StateMachine.Transition;
import hu.elte.txtuml.api.model.StateMachine.Vertex;
import hu.elte.txtuml.api.model.error.seqdiag.InvalidMessageError;
import hu.elte.txtuml.api.model.execution.TraceListener;
import hu.elte.txtuml.api.model.seqdiag.ImprintedListener;
import hu.elte.txtuml.api.model.seqdiag.MessageWrapper;

public class CommunicationListener extends AbstractSequenceDiagramModelListener implements TraceListener,ImprintedListener  {
	
	protected LinkedList<MessageWrapper> suggestedMessagePattern; 
	
	public CommunicationListener(SequenceDiagramExecutor executor)
	{
		super(executor);
		suggestedMessagePattern = new LinkedList<MessageWrapper>();
	}
	
	public void executionStarted() {
	}

	public void processingSignal(ModelClass object, Signal signal) {
				
		if(suggestedMessagePattern.size() > 0)
		{	
			MessageWrapper required = suggestedMessagePattern.poll();
			
			if(!signal.equals(required.signal))
			{
				executor.addError(new InvalidMessageError(object,"The model diverged from the Sequence-diagram Specified behaviour:\n it sent: " + signal.toString() + " instead of " + required.toString() + "\n" ));
			}
		}
		else
		{
			executor.addError(new InvalidMessageError(object,"The model sent more signals than the pattern ovelapped" ));
		}
	}
	
	public void addToPattern(ModelClass from,Signal sig,ModelClass to)
	{
		this.suggestedMessagePattern.add(new MessageWrapper(from,sig,to));
	}

	public void usingTransition(ModelClass object, Transition transition) {
	}

	public void enteringVertex(ModelClass object, Vertex vertex) {
	}

	public void leavingVertex(ModelClass object, Vertex vertex) {
	}

	public void executionTerminated() {
	}
}
