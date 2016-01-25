package hu.elte.txtuml.api.layout.containers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import hu.elte.txtuml.api.layout.Diamond;

/**
 * The container for repeatable annotation {@link Diamond}.
 * 
 * @author Gabor Ferenc Kovacs
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface DiamondContainer {
	Diamond[] value() default {};
}
