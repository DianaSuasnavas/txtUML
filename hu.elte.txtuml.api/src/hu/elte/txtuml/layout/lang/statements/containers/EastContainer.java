package hu.elte.txtuml.layout.lang.statements.containers;

import hu.elte.txtuml.layout.lang.statements.East;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The container for repeteable annotation {@link East}.
 * 
 * @author G�bor Ferenc Kov�cs
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EastContainer {
	East[] value() default {};
}