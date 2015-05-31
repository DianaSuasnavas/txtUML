package hu.elte.txtuml.api;

import hu.elte.txtuml.api.StateMachine.CompositeState;
import hu.elte.txtuml.api.StateMachine.Transition;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A runtime annotation to define the target vertex of a transition.
 * 
 * <p>
 * <b>Represents:</b> target property of a transition
 * <p>
 * <b>Usage:</b>
 * <p>
 * 
 * Use on a subclass of {@link Transition} and set the value of the
 * {@link #value() value} element to reference the representing class of the
 * desired vertex.
 * <p>
 * If used on class <i>t</i> with the value referencing class <i>v</i>, the
 * following conditions must be met:
 * <ul>
 * <li><i>t</i> is a subclass of <code>Transition</code> and not
 * <code>Transition</code> itself,</li>
 * <li><i>v</i> is a subclass of <code>Vertex</code> and not <code>Vertex</code>
 * itself or any subclasses of <code>Vertex</code> defined as nested classes of
 * {@link StateMachine},
 * <li><i>t</i> and <i>v</i> must be nested classes of the same enclosing class,
 * either a subclass of {@link Region} or {@link CompositeState},
 * <p>
 * <b>Note:</b> {@link ModelClass} is a subclass of <code>Region</code>.</li>
 * <li>neither of <i>t</i> or <i>v</i> is abstract.
 * </ul>
 * <p>
 * <b>Note:</b> using this annotation on any type except for subclasses of
 * <code>Transition</code> has no effect on the model.
 * 
 * <p>
 * <b>Example:</b>
 * 
 * <pre>
 * <code>
 * {@literal @From(SourceState.class) @To(TargetState.class) @Trigger(SampleSignal.class)}
 * class SampleTransition extends Transition {}
 * </code>
 * </pre>
 * 
 * See the documentation of {@link StateMachine} for detailed examples.
 * <p>
 * See the documentation of {@link Model} for an overview on modeling in txtUML.
 *
 * @author Gabor Ferenc Kovacs
 * @see StateMachine.Transition
 * @see From
 * @see Trigger
 * 
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface To {

	/**
	 * The annotation type element to set the target vertex of the transition
	 * this annotation is used on.
	 * 
	 * @return the class representing the target vertex of the transition
	 */
	Class<? extends StateMachine.Vertex> value();

}