package hu.elte.txtuml.api;

import java.util.HashMap;
import java.util.Map;

import hu.elte.txtuml.api.Association.AssociationEnd;
// import hu.elte.txtuml.importer.MethodImporter;
// import hu.elte.txtuml.importer.ModelImporter;
import hu.elte.txtuml.utils.InstanceCreator;

public abstract class ModelClass extends ModelIdentifiedElementImpl implements
		ModelElement, ModelIdentifiedElement {

	/*
	 * DESTROYED status is currently unreachable, FINALIZED is only by a class
	 * which has no state machine.
	 */
	private enum Status {
		READY, ACTIVE, FINALIZED, DESTROYED
	}

	private static Map<Class<?>, Class<? extends InitialState>> initialStates = new HashMap<>();
	private final Map<Class<?>, Object> innerClassInstances = new HashMap<>();
	private final Map<Class<? extends AssociationEnd<?>>, AssociationEnd<?>> associations = new HashMap<>();
	private State currentState;
	private Status STATUS;

	public abstract class State implements ModelElement {
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

	public abstract class Choice extends State {

		public final void entry() {
		}

		public final void exit() {
		}
	}

	public abstract class Transition implements ModelElement {

		private Signal signal;

		public void effect() {
		}

		public ModelBool guard() {
			return new ModelBool(true);
		}

		@SuppressWarnings("unchecked")
		protected final <T extends Signal> T getSignal() {
// TODO: The following two lines create an unwanted dependency
// between the API and the Importer.
// Can it be eliminated by AspectJ tricks?		
//			if (signal == null && MethodImporter.isImporting()) {
//				signal = MethodImporter.createSignal(getClass());
//			}
			return (T) signal;
		}

		final void setSignal(Signal s) {
			signal = s;
		}
	}

	protected ModelClass() {
		super();
		this.currentState = null;
		this.innerClassInstances.put(getClass(), this);

// TODO: The following two lines create an unwanted dependency
// between the API and the Importer.
// Can it be eliminated by AspectJ tricks?		
//		if (!ModelImporter.isImporting()) {
//			setCurrentStateToInitial();
//		}
	}
//
//	private void setCurrentStateToInitial() {
//		Class<? extends InitialState> initStateClass = getInitialState(getClass());
//		if (initStateClass != null) {
//			currentState = getInnerClassInstance(initStateClass);
//			STATUS = Status.READY;
//		} else {
//			STATUS = Status.FINALIZED;
//		}
//	}

	public <T extends ModelClass, AE extends AssociationEnd<T>> AE assoc(
			Class<AE> otherEnd) {
		@SuppressWarnings("unchecked")
		AE ret = (AE) associations.get(otherEnd);
		if (ret == null) {
			ret = InstanceCreator.createInstance(otherEnd);
			associations.put(otherEnd, ret);
		}
		ret.setOwnerId(this.getIdentifier());
		return ret;
	}

	@SuppressWarnings("unchecked")
	<T extends ModelClass, AE extends AssociationEnd<T>> void addToAssoc(
			Class<AE> otherEnd, T object) {
		associations.put(
				otherEnd,
				(AE) InstanceCreator.createInstanceWithGivenParams(otherEnd,
						(Object) null).init(
						assoc(otherEnd).typeKeepingAdd(object)));
	}

	void start() {
		if (STATUS != Status.READY) {
			return;
		}
		send(null); // to move from initial state
		STATUS = Status.ACTIVE;
	}

	void send(Signal signal) {
		ModelExecutor.send(this, signal);
	}

	void processSignal(Signal signal) {
		if (currentState == null) { // no state machine
			return;
		}
		if (ModelExecutor.Settings.executorLog() && signal != null) {
			Action.executorFormattedLog("%10s %-15s    got signal: %-18s%n",
					getClass().getSimpleName(), getIdentifier(), signal
							.getClass().getSimpleName());
		}
		if (executeTransition(signal)) {
			callEntryAction();
		}
	}

	private boolean executeTransition(Signal signal) {
		Class<?> applicableTransition = null;
		for (Class<?> examinedStateClass = currentState.getClass(), parentClass = examinedStateClass
				.getEnclosingClass(); parentClass != null; examinedStateClass = parentClass, parentClass = examinedStateClass
				.getEnclosingClass()) {
			for (Class<?> c : parentClass.getDeclaredClasses()) {
				if (Transition.class.isAssignableFrom(c)) {
					Class<? extends State> from/* ,to */;
					try {
						from = c.getAnnotation(From.class).value(); // NullPointerException
																	// if no
																	// @From is
																	// set on
																	// the
																	// transition
						/* to = */c.getAnnotation(To.class).value(); // NullPointerException
																		// if no
																		// @To
																		// is
																		// set
																		// on
																		// the
																		// transition
					} catch (NullPointerException e) {
						// TODO show warning
						continue;
					}
					if (from != examinedStateClass
							|| notApplicableTrigger(c, signal)) {
						continue;
					}

					Transition transition = (Transition) getInnerClassInstance(c);
					transition.setSignal(signal);
					if (!transition.guard().getValue()) { // checking guard
						continue;
					}

					if (applicableTransition != null) {
						Action.executorErrorLog("Error: guards of transitions "
								+ applicableTransition.getName() + " and "
								+ c.getName() + " from class "
								+ currentState.getClass().getSimpleName()
								+ " are overlapping");
						continue;
					}
					applicableTransition = c;
				}
			}
		}

		if (applicableTransition == null) { // there was no transition which
											// could be used
			return false;
		}
		useTransition(applicableTransition);

		if (currentState instanceof Choice) {
			executeTransitionFromChoice(signal);
		}

		return true;
	}

	private void executeTransitionFromChoice(Signal signal) {
		Class<?> applicableTransition = null;
		Class<?> elseTransition = null;
		Class<?> examinedChoiceClass = currentState.getClass();
		Class<?> parentClass = examinedChoiceClass.getEnclosingClass();
		for (Class<?> c : parentClass.getDeclaredClasses()) {
			if (Transition.class.isAssignableFrom(c)) {
				Class<? extends State> from/* ,to */;
				try {
					from = c.getAnnotation(From.class).value(); // NullPointerException
																// if no @From
																// is set on the
																// transition
					/* to = */c.getAnnotation(To.class).value(); // NullPointerException
																	// if no @To
																	// is set on
																	// the
																	// transition
				} catch (NullPointerException e) {
					// TODO show warning
					continue;
				}
				if (from != examinedChoiceClass) { // actual transition is from
													// another state
					continue;
				}

				Transition transition = (Transition) getInnerClassInstance(c);
				transition.setSignal(signal);
				ModelBool resultOfGuard = transition.guard();
				if (!resultOfGuard.getValue()) { // check guard
					if (resultOfGuard instanceof ModelBool.Else) { // transition
																	// with else
																	// condition
						if (elseTransition != null) { // there was already a
														// transition with an
														// else condition
							Action.executorErrorLog("Error: there are more than one transitions from choice "
									+ examinedChoiceClass.getSimpleName()
									+ " with an Else condition");
							continue;
						}
						elseTransition = c;
					}
					continue;
				}
				if (applicableTransition != null) { // there was already an
													// applicable transition
					Action.executorErrorLog("Error: guards of transitions "
							+ applicableTransition.getName() + " and "
							+ c.getName() + " from class "
							+ examinedChoiceClass.getSimpleName()
							+ " are overlapping");
					continue;
				}
				applicableTransition = c;
			}
		}
		if (applicableTransition == null) { // there was no transition which
											// could be used
			if (elseTransition != null) { // but there was a transition with an
											// else condition
				useTransition(elseTransition);
			} else {
				Action.executorErrorLog("Error: there was no transition from choice class "
						+ examinedChoiceClass.getSimpleName()
						+ " which could be used");
			}
			return;
		}
		useTransition(applicableTransition);
		if (currentState instanceof Choice) {
			executeTransitionFromChoice(signal);
		}
	}

	private void useTransition(Class<?> transitionClass) {
		/*
		 * The signal has to be already set to the transition instance returned
		 * by the getInnerClassInstance(transitionClass) call.
		 */

		Transition transition = (Transition) getInnerClassInstance(transitionClass);
		Class<? extends State> from = transitionClass.getAnnotation(From.class)
				.value();
		Class<? extends State> to = transitionClass.getAnnotation(To.class)
				.value();
		if (ModelExecutor.Settings.executorLog()) {
			Action.executorFormattedLog(
					"%10s %-15s changes state: from: %-10s tran: %-18s to: %-10s%n",
					getClass().getSimpleName(), getIdentifier(),
					from.getSimpleName(), transitionClass.getSimpleName(),
					to.getSimpleName());
		}
		callExitAction(from);
		transition.effect();
		currentState = getInnerClassInstance(to);
	}

	private boolean notApplicableTrigger(Class<?> transitionClass, Signal signal) {
		Trigger trigger = transitionClass.getAnnotation(Trigger.class);
		if ((signal == null) == (trigger == null)
				&& ((signal == null) || (trigger.value()
						.isAssignableFrom(signal.getClass())))) {
			return false;
		}
		return true;
	}

	private void callExitAction(Class<? extends State> from) {
		while (currentState.getClass() != from) {
			if (ModelExecutor.Settings.executorLog()) {
				Action.executorFormattedLog(
						"%10s %-15s   exits state: %-18s%n", getClass()
								.getSimpleName(), getIdentifier(), currentState
								.getClass().getSimpleName());
			}
			currentState.exit();
			@SuppressWarnings("unchecked")
			Class<? extends State> currentParentState = (Class<? extends State>) currentState
					.getClass().getEnclosingClass();
			currentState = getInnerClassInstance(currentParentState);
		}
		currentState.exit();
	}

	private void callEntryAction() {
		currentState.entry();
		if (currentState instanceof CompositeState) {
			Class<? extends InitialState> initStateClass = getInitialState(currentState
					.getClass());
			if (initStateClass != null) {
				if (ModelExecutor.Settings.executorLog()) {
					Action.executorFormattedLog(
							"%10s %-15s  enters state: %-18s%n", getClass()
									.getSimpleName(), getIdentifier(),
							initStateClass.getSimpleName());
				}
				currentState = getInnerClassInstance(initStateClass);
				// no entry action needs to be called: initial states have none
				processSignal(null); // step forward from initial state
			}
		}
	}

	private <T> T getInnerClassInstance(Class<T> forWhat) {
		if (forWhat == null) {
			Action.executorErrorLog("Error: in class "
					+ getClass().getSimpleName()
					+ " a transition or state is used which is not an inner state of "
					+ getClass().getSimpleName());
			return null;
		}
		@SuppressWarnings("unchecked")
		T ret = (T) innerClassInstances.get(forWhat);
		if (ret == null) {
			ret = InstanceCreator.createInstanceWithGivenParams(forWhat,
					getInnerClassInstance(forWhat.getEnclosingClass()));
			innerClassInstances.put(forWhat, ret);
		}
		return ret;
	}

	private static Class<? extends InitialState> getInitialState(
			Class<?> forWhat) {
		synchronized (initialStates) {
			if (initialStates.containsKey(forWhat)) {
				return initialStates.get(forWhat);
			}
			for (Class<?> c : forWhat.getDeclaredClasses()) {
				if (InitialState.class.isAssignableFrom(c)) {
					@SuppressWarnings("unchecked")
					// it is checked
					Class<? extends InitialState> ret = (Class<? extends InitialState>) c;
					initialStates.put(forWhat, ret);
					return ret;
				}
			}
			initialStates.put(forWhat, null);
			return null;
		}
	}
}
