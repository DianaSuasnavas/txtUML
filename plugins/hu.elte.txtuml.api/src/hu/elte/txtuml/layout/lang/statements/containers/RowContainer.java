package hu.elte.txtuml.layout.lang.statements.containers;

import hu.elte.txtuml.layout.lang.statements.Row;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The container for repeatable annotation {@link Row}.
 * 
 * @author Gabor Ferenc Kovacs
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface RowContainer {
	Row[] value() default {};
}
