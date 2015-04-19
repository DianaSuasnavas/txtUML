package hu.elte.txtuml.api;

import hu.elte.txtuml.api.backend.MultiplicityException;
import hu.elte.txtuml.api.backend.collections.AssociationsMap;
import hu.elte.txtuml.api.backend.messages.ErrorMessages;
import hu.elte.txtuml.api.backend.messages.WarningMessages;
import hu.elte.txtuml.layout.lang.elements.LayoutNode;
import hu.elte.txtuml.utils.InstanceCreator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Base class for classes in the model.
 * 
 * <p>
 * <b>Represents:</b> class
 * <p>
 * <b>Usage:</b>
 * <p>
 * 
 * Inherit from this class to define classes of the model. Fields of the
 * subclass will represent attributes of the model class, methods will represent
 * operations, while inheritance between subclasses of <code>ModelClass</code>
 * will represent inheritance in the model. That means, due to the restrictions
 * of Java, each model class may have at most one base class.
 * <p>
 * See the documentation of {@link StateMachine} about applying state machines
 * to model classes.
 * 
 * <p>
 * <b>Java restrictions:</b>
 * <ul>
 * <li><i>Instantiate:</i> disallowed</li>
 * <li><i>Define subtype:</i> allowed
 * <p>
 * <b>Subtype requirements:</b>
 * <ul>
 * <li>must be the nested class of a subclass of {@link Model}</li>
 * </ul>
 * <p>
 * <b>Subtype restrictions:</b>
 * <ul>
 * <li><i>Be abstract:</i> disallowed</li>
 * <li><i>Generic parameters:</i> disallowed</li>
 * <li><i>Constructors:</i> allowed, with zero parameters, containing only
 * simple assignments to set the default values of its fields</li>
 * <li><i>Initialization blocks:</i> allowed, containing only simple assignments
 * to set the default values of its fields</li>
 * <li><i>Fields:</i> allowed, only of types which are subclasses of
 * {@link ModelClass} or {@link ModelType}; they represent attributes of the
 * model class</li>
 * <li><i>Methods:</i> allowed, only with parameters and return values of types
 * which are subclasses of {@link ModelClass}, {@link ModelType} or
 * Collection<>; they represent operations of the model class</li>
 * <li><i>Nested interfaces:</i> disallowed</li>
 * <li><i>Nested classes:</i> allowed, only non-static and extending either
 * {@link StateMachine.Vertex} or {@link StateMachine.Transition}</li>
 * <li><i>Nested enums:</i> disallowed</li>
 * </ul>
 * <li><i>Inherit from the defined subtype:</i> allowed, to represent class
 * inheritance</li></li>
 * </ul>
 * 
 * <p>
 * <b>Example:</b>
 * <p>
 * 
 * <pre>
 * <code>
 * class Employee extends ModelClass {
 * 
 * 	ModelString name;
 * 
 * 	ModelInt id;
 * 
 * 	void work(ModelInt hours, ModelInt payment) {
 * 		{@literal /}{@literal /} ...
 *  	}
 *  
 *  {@literal /}{@literal /} ...
 *  
 * }
 * </code>
 * </pre>
 * 
 * See the documentation of the {@link StateMachine} for detailed examples about
 * defining state machines.
 * <p>
 * See the documentation of the {@link hu.elte.txtuml.api} package to get an
 * overview on modeling in txtUML.
 *
 * @author Gabor Ferenc Kovacs
 *
 */
public class ModelClass extends Region implements ModelElement, LayoutNode {

	/**
	 * The life cycle of a model object consists of steps represented by the
	 * constants of this enumeration type.
	 * <p>
	 * See the documentation of the {@link hu.elte.txtuml.api} package to get an
	 * overview on modeling in txtUML.
	 * 
	 * @see Status#READY
	 * @see Status#ACTIVE
	 * @see Status#FINALIZED
	 * @see Status#DELETED
	 */
	public enum Status {
		/**
		 * This status of a <code>ModelClass</code> object indicates that the
		 * represented model object's state machine is not yet started. It will
		 * not react to any asynchronous events, for example, sending signals to
		 * it. However, sending signal to a <code>READY</code> object is legal
		 * in the model, therefore no error or warning messages are shown if it
		 * is done.
		 * <p>
		 * See the documentation of the {@link hu.elte.txtuml.api} package to
		 * get an overview on modeling in txtUML.
		 * 
		 * @see Status#ACTIVE
		 */
		READY,
		/**
		 * This status of a <code>ModelClass</code> object indicates that the
		 * represented model object's state machine is currently running.
		 * <p>
		 * It may be reached by starting the state machine of this object
		 * manually with the {@link Action#start(ModelClass)} method.
		 * <p>
		 * See the documentation of the {@link hu.elte.txtuml.api} package to
		 * get an overview on modeling in txtUML.
		 */
		ACTIVE,
		/**
		 * This status of a <code>ModelClass</code> object indicates that the
		 * represented model object either has no state machine or its state
		 * machine is already stopped but the object itself is not yet deleted
		 * from the model. Its fields and methods might be used but it will not
		 * react to any asynchronous events, for example, sending signals to it.
		 * However, sending signal to a <code>FINALIZED</code> object is legal
		 * in the model, therefore no error or warning messages are shown if it
		 * is done.
		 * <p>
		 * <b>Note:</b> currently there is no way to stop the state machine of a
		 * model object without deleting it. So the only way to reach this
		 * status is to implement a model class without a state machine.
		 * <p>
		 * See the documentation of the {@link hu.elte.txtuml.api} package to
		 * get an overview on modeling in txtUML.
		 */
		FINALIZED,
		/**
		 * This status of a <code>ModelClass</code> object indicates that the
		 * represented model object is deleted. No further use of this object is
		 * allowed, however, using its fields or methods do not result in any
		 * error messages because of the limitations of the Java language.
		 * <p>
		 * An object may only be in this status when all of its associations are
		 * unlinked and its state machine is stopped.
		 * <p>
		 * See the documentation of the {@link hu.elte.txtuml.api} package to
		 * get an overview on modeling in txtUML.
		 * 
		 * @see Action#delete(ModelClass)
		 */
		DELETED
	}

	/**
	 * A static counter to give different identifiers to each created model
	 * object instance.
	 */
	private static AtomicLong counter = new AtomicLong(0);

	/**
	 * The current status of this model object.
	 * 
	 * @see Status
	 */
	private Status status;

	/**
	 * A unique identifier of this object.
	 */
	private final String identifier;

	/**
	 * A map of the associations of this model object.
	 */
	private final AssociationsMap associations = AssociationsMap.create();

	/**
	 * Sole constructor of <code>ModelClass</code>. Creates the unique
	 * identifier of this object and after setting its current vertex to its
	 * initial pseudostate (if any), it goes to either {@link Status#READY
	 * READY} or {@link Status#FINALIZED FINALIZED} status depending on whether
	 * it has any state machine or not (any initial pseudostate or not).
	 * <p>
	 * <b>Implementation note:</b>
	 * <p>
	 * Protected because this class is intended to be inherited from but not
	 * instantiated. However, <code>ModelClass</code> has to be a non-abstract
	 * class to make sure that it is instantiatable when that is needed for the
	 * API or the model exportation.
	 */
	protected ModelClass() {
		super();

		this.identifier = "obj_" + counter.addAndGet(1);

		if (getCurrentVertex() == null) {
			status = Status.FINALIZED;
		} else {
			status = Status.READY;
		}
	}

	/**
	 * Each model object has a unique identifier created upon the creation of
	 * the model object.
	 * 
	 * @return the unique identifier of this model object
	 */
	@Override
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Gets the collection containing the objects in association with this
	 * object and being on the specified opposite association end. May only be
	 * called with a navigable association end as its parameter.
	 * 
	 * @param otherEnd
	 *            the opposite association end
	 * @return collection containing the objects in association with this object
	 *         and being on <code>otherEnd</code>
	 */
	public <T extends ModelClass, AE extends AssociationEnd<T> & hu.elte.txtuml.api.assocends.Navigability.Navigable> AE assoc(
			Class<AE> otherEnd) {

		return assocPrivate(otherEnd);

	}

	/**
	 * Gets the collection containing the objects in association with this
	 * object and being on the specified opposite association end.
	 * 
	 * @param otherEnd
	 *            the opposite association end
	 * @return collection containing the objects in association with this object
	 *         and being on <code>otherEnd</code>
	 */
	private <T extends ModelClass, AE extends AssociationEnd<T>> AE assocPrivate(
			Class<AE> otherEnd) {

		@SuppressWarnings("unchecked")
		AE ret = (AE) associations.get(otherEnd);
		if (ret == null) {
			ret = InstanceCreator.createInstance(otherEnd);
			associations.put(otherEnd, ret);
		}
		ret.setOwner(this);
		return ret;
	}

	/**
	 * Adds the specified object to the collection containing the objects in
	 * association with this object and being on the specified opposite
	 * association end.
	 * 
	 * @param otherEnd
	 *            the opposite association end
	 * @param object
	 *            the object to add to the collection (should not be
	 *            <code>null</code>)
	 * @throws MultiplicityException
	 *             if the upper bound of the multiplicity of the opposite
	 *             association end is offended
	 */
	<T extends ModelClass, AE extends AssociationEnd<T>> void addToAssoc(
			Class<AE> otherEnd, T object) throws MultiplicityException {

		AssociationEnd<?> newValue = InstanceCreator
				.createInstanceWithGivenParams(otherEnd, (Object) null).init(
						assocPrivate(otherEnd).typeKeepingAdd(object));

		associations.put(otherEnd, newValue);

	}

	/**
	 * Removes the specified object from the collection containing the objects
	 * in association with this object and being on the specified opposite
	 * association end.
	 * 
	 * @param otherEnd
	 *            the opposite association end
	 * @param object
	 *            the object to remove from the collection
	 */
	<T extends ModelClass, AE extends AssociationEnd<T>> void removeFromAssoc(
			Class<AE> otherEnd, T object) {

		AssociationEnd<?> newValue = InstanceCreator
				.createInstanceWithGivenParams(otherEnd, (Object) null).init(
						assocPrivate(otherEnd).typeKeepingRemove(object));

		associations.put(otherEnd, newValue);

		if (ModelExecutor.Settings.dynamicChecks()
				&& !newValue.checkLowerBound()) {
			ModelExecutor.checkLowerBoundInNextExecutionStep(object, otherEnd);
		}

	}

	/**
	 * Checks if the specified object is element of the collection containing
	 * the objects in association with this object and being on the specified
	 * opposite association end.
	 * 
	 * @param otherEnd
	 *            the opposite association end
	 * @param object
	 *            the object to check in the collection
	 * @return <code>true</code> if <code>object</code> is included in the
	 *         collection related to <code>otherEnd</code>, <code>false</code>
	 *         otherwise
	 */
	<T extends ModelClass, AE extends AssociationEnd<T>> boolean hasAssoc(
			Class<AE> otherEnd, T object) {

		AssociationEnd<?> actualOtherEnd = associations.get(otherEnd);
		return actualOtherEnd == null ? false : actualOtherEnd.contains(object)
				.getValue();
	}

	/**
	 * Checks the lower bound of the specified association end's multiplicity.
	 * Shows a message in case of an error.
	 * 
	 * @param assocEnd
	 *            the association end to check
	 */
	void checkLowerBound(Class<? extends AssociationEnd<?>> assocEnd) {

		if (!assocPrivate(assocEnd).checkLowerBound()) {
			ModelExecutor
					.logError(ErrorMessages
							.getLowerBoundOfMultiplicityOffendedMessage(this,
									assocEnd));
		}

	}

	@Override
	void process(Signal signal) {
		if (isDeleted()) {
			ModelExecutor.executorErrorLog(WarningMessages
					.getSignalArrivedToDeletedObjectMessage(this));
			return;
		}
		super.process(signal);
	}

	/**
	 * TODO check doc
	 * Starts the state machine of this object.
	 * <p>
	 * If this object is <i>not</i> in {@link Status#READY READY} status, this
	 * method does nothing. Otherwise, it sends an asynchronous request to
	 * itself to step forward from its initial pseudostate and also changes its
	 * status to {@link Status#ACTIVE ACTIVE}</code>.
	 * <p>
	 * If the optional dynamic checks are switched on in
	 * {@link ModelExecutor.Settings}, this method also initializes the defined
	 * association ends of this model object by calling the
	 * {@link #initializeAllDefinedAssociationEnds} method.
	 */
	void start() {
		if (status != Status.READY) {
			return;
		}
		send(null); // to move from initial state
		status = Status.ACTIVE;

		if (ModelExecutor.Settings.dynamicChecks()) {
			initializeAllDefinedAssociationEnds();
		}
	}

	/**
	 * TODO check doc
	 * Looks up all the defined associations of the model class this object is
	 * an instance of and initializes them by assigning empty {@link Collection
	 * Collections} to them. If any of them has a lower bound higher than zero,
	 * then registers that association end to be checked in the next execution
	 * step.
	 * <p>
	 * Shows an error about a bad model if any exception is thrown during the
	 * above described process as this method and all the methods this calls,
	 * assume that the model is well-defined.
	 */
	@SuppressWarnings("unchecked")
	private void initializeAllDefinedAssociationEnds() {
		try {
			Class<?> modelClass = getClass().getEnclosingClass();

			for (Class<?> assocClass : modelClass.getDeclaredClasses()) {
				if (Association.class.isAssignableFrom(assocClass)) {
					Class<?>[] assocEnds = assocClass.getDeclaredClasses();
					if (checkIfEndIsOwnedByThisClass(assocEnds[0])) {
						initializeDefinedAssociationEnd((Class<? extends AssociationEnd<?>>) assocEnds[1]);
					}
					if (checkIfEndIsOwnedByThisClass(assocEnds[1])) {
						initializeDefinedAssociationEnd((Class<? extends AssociationEnd<?>>) assocEnds[0]);
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(ErrorMessages.getBadModel(), e);
		}
	}

	/**
	 * Checks if the owner of the specified association end is the model class
	 * this object is an instance of.
	 * <p>
	 * Exceptions might be thrown in case of a model error as this method
	 * assumes that the model is well-defined.
	 * 
	 * @param assocEnd
	 *            the association end to check if its owner is the model class
	 *            this object is an instance of
	 * @return whether the owner of <code>assocEnd</code> is the model class
	 *         this object is an instance of
	 */
	private boolean checkIfEndIsOwnedByThisClass(Class<?> assocEnd) {
		if (AssociationEnd.class.isAssignableFrom(assocEnd)
				&& getOwnerOfAssocEnd(assocEnd) == getClass()) {
			return true;
		}
		return false;
	}

	/**
	 * Analyzes the specified association end to get its owner
	 * <code>ModelClass</code> type (its first generic parameter).
	 * <p>
	 * Exceptions might be thrown in case of a model error as this method
	 * assumes that the model is well-defined.
	 * 
	 * @param assocEnd
	 *            the association end the owner of which is sought
	 * @return the first generic parameter of <code>assocEnd</code>
	 */
	private Type getOwnerOfAssocEnd(Class<?> assocEnd) {
		return ((ParameterizedType) assocEnd.getGenericSuperclass())
				.getActualTypeArguments()[0];
	}

	/**
	 * TODO check doc
	 * Initializes the specified association end by assigning an empty
	 * {@link Collection} to it. If it has a lower bound higher than zero then
	 * registers that association end to be checked in the next execution step.
	 * <p>
	 * Exceptions might be thrown in case of a model error as this method
	 * assumes that the model is well-defined.
	 * 
	 * @param assocEnd
	 *            the association end to initialize
	 */
	private void initializeDefinedAssociationEnd(
			Class<? extends AssociationEnd<?>> assocEnd) {

		if (associations.get(assocEnd) != null) {
			return;
		}
		
		AssociationEnd<?> value = InstanceCreator
				.createInstanceWithGivenParams(assocEnd, (Object) null);

		associations.put(assocEnd, value);

		if (!value.checkLowerBound()) {
			ModelExecutor.checkLowerBoundInNextExecutionStep(this, assocEnd);
		}

	}

	/**
	 * Sends a signal to this object asynchronously.
	 * 
	 * @param signal
	 *            the signal to send to this object
	 */
	void send(Signal signal) {
		ModelExecutor.send(this, signal);
	}

	/**
	 * Checks whether this model object is in {@link Status#DELETED DELETED}
	 * status.
	 * 
	 * @return <code>true</code> if this model object is in <code>DELETED</code>
	 *         status, <code>false</code> otherwise
	 */
	boolean isDeleted() {
		return status == Status.DELETED;
	}

	/**
	 * Checks if this model object is ready to be deleted. If it is already
	 * deleted, this method automatically returns <code>true</code>. Otherwise,
	 * it checks whether all associations to this object have already been
	 * unlinked.
	 * 
	 * @return <code>true</code> if this model object is ready to be deleted,
	 *         <code>false</code> otherwise
	 */
	boolean isDeletable() {
		if (isDeleted()) {
			return true;
		}
		for (AssociationEnd<?> assocEnd : this.associations.values()) {
			if (!assocEnd.isEmpty().getValue()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Deletes the specified model object.
	 * <p>
	 * Might only be called if all associations of the specified model object
	 * are already unlinked. Shows an error otherwise.
	 * <p>
	 * See {@link ModelClass.Status#DELETED DELETED} status of model objects for
	 * more information about model object deletion.
	 */
	void forceDelete() {
		if (!isDeletable()) {
			ModelExecutor.executorErrorLog(ErrorMessages
					.getObjectCannotBeDeletedMessage(this));
			return;
		}

		this.inactivate();

		status = Status.DELETED;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + ":" + getIdentifier();
	}

}
