package txtuml.api;

import txtuml.api.Association.*;
import txtuml.importer.MethodImporter;
import txtuml.utils.InstanceCreator;

public class Action implements ModelElement {

	static final Object lockOnAssociations = new Object();

	protected Action() {
	}

	public static <T extends ModelClass> T create(Class<T> classType) {
		return InstanceCreator.createInstance(classType);
	}

	public static <MODELCLASS1 extends ModelClass, MODELCLASS2 extends ModelClass> void link(
			Class<? extends AssociationEnd<MODELCLASS1>> leftEnd,
			MODELCLASS1 leftObj,
			Class<? extends AssociationEnd<MODELCLASS2>> rightEnd,
			MODELCLASS2 rightObj) {
		// it does not reuse the other link implementation because of
		// optimization issues

		synchronized (lockOnAssociations) {
			leftObj.addToAssoc(rightEnd, rightObj);
			rightObj.addToAssoc(leftEnd, leftObj);
		}
	}

	// TODO upcoming feature
	/*
	 * public static void link(AssocEnd<?>... associationEnds) {
	 * 
	 * synchronized(lockOnAssociations) { for (int i = 0; i <
	 * associationEnds.length - 1; ++i) { for (int j = i + 1; j <
	 * associationEnds.length; ++j) { AssocEnd<?> leftEnd = associationEnds[i];
	 * AssocEnd<?> rightEnd = associationEnds[j]; leftEnd.link(rightEnd);
	 * rightEnd.link(leftEnd); } } } }
	 */
	// TODO upcoming feature
	/*
	 * @SuppressWarnings("unchecked") public static <T extends ModelClass>
	 * AssocEnd<T> assocEnd( Class<? extends AssociationEnd<T>> m, T object,
	 * T... followingObjects) {
	 * 
	 * return new AssocEnd<>(m, object, followingObjects); }
	 */

	public static void start(ModelClass obj) {
		obj.start();
	}

	public static void send(ModelClass receiverObj, Signal event) {
		Runtime.send(receiverObj, event);
	}

	public static void If(Condition cond, BlockBody thenBody, BlockBody elseBody) {

		if (cond.check().getValue()) {
			thenBody.run();
		} else {
			elseBody.run();
		}
	}

	public static void If(Condition cond, BlockBody thenBody) {
		If(cond, thenBody, () -> {
		});
	}

	public static void While(Condition cond, BlockBody body) {

		while (cond.check().getValue()) {
			body.run();
		}
	}

	public static <T extends ModelClass> void For(Collection<T> collection,
			ParameterizedBlockBody<T> body) {
		// TODO import 'For' (foreach) into UML2

		for (T element : collection) {
			body.run(element);
		}
	}

	public static void For(ModelInt begin, ModelInt end,
			ParameterizedBlockBody<ModelInt> body) {
		// TODO import 'For' (simple) into UML2

		for (int i = begin.getValue(); i <= end.getValue(); ++i) {
			body.run(new ModelInt(i));
		}
	}

	public static void log(String message) { // user log
		if (MethodImporter.isImporting()) {

		} else {
			Runtime.log(message);
		}

	}

	public static void logError(String message) { // user log
		if (MethodImporter.isImporting()) {

		} else {
			Runtime.logError(message);
		}

	}

	static void runtimeLog(String message) { // api log
		if (MethodImporter.isImporting()) {

		} else {
			Runtime.runtimeLog(message);
		}

	}

	static void runtimeFormattedLog(String format, Object... args) { // api log
		if (MethodImporter.isImporting()) {

		} else {
			Runtime.runtimeFormattedLog(format, args);
		}

	}

	static void runtimeErrorLog(String message) { // api log
		if (MethodImporter.isImporting()) {

		} else {
			Runtime.runtimeErrorLog(message);
		}

	}
}