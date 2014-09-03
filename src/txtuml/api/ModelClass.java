package txtuml.api;


import java.lang.Thread;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import txtuml.importer.InstructionImporter;
import txtuml.importer.MethodImporter;
import txtuml.utils.InstanceCreator;

public class ModelClass extends ModelIdentifiedElement {
	public abstract class State {
		public void entry() {
		}
		public void exit() {
		}
	}
	
	public abstract class InitialState extends State {
		public final void entry() {
		}
		public final void exit() {
		}
	}

	public abstract class CompositeState extends State {
	}
	
	public abstract class Transition {
		public void effect() {
		}
		public ModelBool guard() {
			return new ModelBool(true);
		}
		@SuppressWarnings("unchecked")
		protected final <T extends Signal> T getSignal() {
			if (signal ==  null && MethodImporter.isImporting()) {
				signal = MethodImporter.createSignal(getClass());
			}
			return (T)signal;
		}
		final void setSignal(Signal s) {
			signal = s;
		}
		private Signal signal;
	}
	
	
	protected ModelClass() {
		super();
		currentState = null;
		thread = null;
		innerClassInstances.put(getClass(), this);
		
		if(MethodImporter.isImporting())
		{
			InstructionImporter.createInstance(this);
		}
		else 
		{
			startThread();
		}
		
	}
	
	private void startThread() {
		Class<? extends InitialState> initStateClass = getInitialState(getClass());
		if (initStateClass != null) {
			currentState = getInnerClassInstance(initStateClass);
			thread = new ModelClassThread(this);
		}
	}

	void finishThread() {
		synchronized(thread) {
			if (thread != null) {
				thread.interrupt();
				thread = null;
			}
		}
	}

	void send(Signal signal) {
		synchronized(thread) {
			if (thread != null) {
				thread.send(signal);
			}
		}		
	}

	private void processEvent(Signal signal) {
		if (currentState == null) { // no state machine
			return;
		}
		if (Runtime.Settings.runtimeLog() && signal != null) {
			Action.runtimeFormattedLog("%10s %-15s    got signal: %-18s%n",getClass().getSimpleName(),getIdentifier(),signal.getClass().getSimpleName());
		}
		if (searchForTransition(signal)) {
			callEntryAction();
		}
	}

	private synchronized boolean searchForTransition(Signal signal) {
		Class<?> good = null;
		for (Class<?> examinedStateClass = currentState.getClass(), parentClass = examinedStateClass.getEnclosingClass();
				parentClass != null;
				examinedStateClass = parentClass, parentClass = examinedStateClass.getEnclosingClass()) {
			for (Class<?> c : parentClass.getDeclaredClasses()) {
				if (Transition.class.isAssignableFrom(c)) {
					Class<? extends State> from, to;
					try {
						from = c.getAnnotation(From.class).value(); // NullPointerException if no @From is set on the transition
						to = c.getAnnotation(To.class).value(); // NullPointerException if no @To is set on the transition
					} catch (NullPointerException e) {
						continue;
					}
					if (from != examinedStateClass || notGoodTrigger(c, signal)) {
						continue;
					}
					Transition transition = (Transition)getInnerClassInstance(c);
					if (!transition.guard().getValue()) { // check guard
						continue;
					}
					if (good != null) {
						Action.runtimeErrorLog("Error: guards of transitions " + good.getName() + " and " + c.getName() + " from class " + getClass().getSimpleName() + " are overlapping");
						continue;
					}
					good = c;
					if (Runtime.Settings.runtimeLog()) {
						Action.runtimeFormattedLog("%10s %-15s changes state: from: %-10s tran: %-18s to: %-10s%n",getClass().getSimpleName(),getIdentifier(),
								from.getSimpleName(),c.getSimpleName(),to.getSimpleName());
					}
					callExitAction(from);
					transition.setSignal(signal);
					transition.effect();
					currentState = getInnerClassInstance(to);
				}
			}
		}
		return good != null;
	}
	
	private boolean notGoodTrigger(Class<?> transitionClass, Signal signal) {
		Trigger trigger = transitionClass.getAnnotation(Trigger.class);
		if ( (signal == null) == (trigger == null) &&
				 ( (signal == null) || (trigger.value().isAssignableFrom(signal.getClass())) ) ) {
				return false;
		}
		return true;			
	}

	private void callExitAction(Class<? extends State> from) {
		while (currentState.getClass() != from) {
			if (Runtime.Settings.runtimeLog()) {
				Action.runtimeFormattedLog("%10s %-15s   exits state: %-18s%n",getClass().getSimpleName(),getIdentifier(),currentState.getClass().getSimpleName());
			}
			currentState.exit();
			@SuppressWarnings("unchecked")
			Class<? extends State> currentParentState = (Class<? extends State>)currentState.getClass().getEnclosingClass(); 
			currentState = getInnerClassInstance(currentParentState);
		}
		currentState.exit();
	}
	
	private synchronized void callEntryAction() {
		currentState.entry();
		if (currentState instanceof CompositeState) {
			Class<? extends InitialState> initStateClass = getInitialState(currentState.getClass());
			if (initStateClass != null) {
				if (Runtime.Settings.runtimeLog()) {
					Action.runtimeFormattedLog("%10s %-15s  enters state: %-18s%n",getClass().getSimpleName(),getIdentifier(),initStateClass.getSimpleName());
				}
				currentState = getInnerClassInstance(initStateClass);
				// no entry action needs to be called: initial states have none
				processEvent(null); // step forward from initial state
			}
		}
	}
	
	private class ModelClassThread extends Thread {
		private ModelClassThread(ModelClass p) { // called from enclosing ModelClass
			parent = p;
			mailbox = new LinkedBlockingQueue<>();
			start();
		}

		private void send(Signal signal) { // called from enclosing ModelClass
			try {
				mailbox.put(signal);
			} catch (InterruptedException e) {
			}
		}

		public void run() {
			processEvent(null); // step forward from the initial state
			try {
				while (true) {	// TODO stop loop after everything is finished
					Signal signal = mailbox.take();
					parent.processEvent(signal);
				}
			} catch (InterruptedException e) {
				// do nothing (finish thread)
			}
		}

		private LinkedBlockingQueue<Signal> mailbox;
		private ModelClass parent;
	}

	private <T> T getInnerClassInstance(Class<T> forWhat) {
		if (forWhat == null) {
			Action.runtimeErrorLog("Error: in class " + getClass().getSimpleName() + " a transition or state is used which is not an inner state of " + getClass().getSimpleName());
			return null;
		}
		@SuppressWarnings("unchecked")
        T ret = (T)innerClassInstances.get(forWhat);
        if (ret == null) { 
        	ret = InstanceCreator.createInstance(forWhat, 1, getInnerClassInstance(forWhat.getEnclosingClass()));
        }
        innerClassInstances.put(forWhat, ret);
        return ret;
	}
	
	private static Class<? extends InitialState> getInitialState(Class<?> forWhat) {
		synchronized(initialStates) {
	        if (initialStates.containsKey(forWhat)) {
	    		return initialStates.get(forWhat);
	        }        
			for (Class<?> c : forWhat.getDeclaredClasses()) {
				if (InitialState.class.isAssignableFrom(c)) {
					@SuppressWarnings("unchecked") // it is checked
					Class<? extends InitialState> ret = (Class<? extends InitialState>) c; 
			        initialStates.put(forWhat, ret);
			        return ret; 
				} 
			}
	        initialStates.put(forWhat, null);
	        return null;
		}
	}
	private State currentState;
	private ModelClassThread thread;
	private Map<Class<?>, Object> innerClassInstances = new HashMap<>(); 
	private static Map<Class<?>, Class<? extends InitialState>> initialStates = new HashMap<>();
}