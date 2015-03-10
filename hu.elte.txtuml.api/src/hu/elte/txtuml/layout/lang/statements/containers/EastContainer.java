package hu.elte.txtuml.layout.lang.statements.containers;

import hu.elte.txtuml.layout.lang.statements.East;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EastContainer {
	East[] value() default {};
}