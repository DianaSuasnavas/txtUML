package hu.elte.txtuml.api.model.blocks;

import hu.elte.txtuml.api.model.ModelBool;
import hu.elte.txtuml.api.model.ModelElement;

/**
 * A functional interface to implement parameterized conditions in the action
 * language.
 * 
 * <p>
 * <b>Represents:</b> condition
 * <p>
 * 
 * See the documentation of {@link hu.elte.txtuml.api.Model} for an overview on
 * modeling in txtUML.
 * 
 * @author Gabor Ferenc Kovacs
 * @param <T>
 *            the type of the parameter of the condition
 *
 */
@FunctionalInterface
public interface ParameterizedCondition<T> extends ModelElement {

	/**
	 * The sole method of <code>ParameterizedCondition</code>. Override this
	 * method to implement the desired condition.
	 * <p>
	 * Overriding methods may not return a <code>null</code> value.
	 * <p>
	 * Overriding methods may only contain a condition evaluation. See the
	 * documentation of {@link hu.elte.txtuml.api.Model} for details about
	 * condition evaluations in the model.
	 * 
	 * @param param
	 *            the parameter of the condition
	 * @return a <code>ModelBool</code> representing <code>true</code> if the
	 *         condition holds, a <code>ModelBool</code> representing
	 *         <code>false</code> otherwise
	 */
	ModelBool check(T param);

}