package hu.elte.txtuml.api.layout;

import hu.elte.txtuml.api.layout.containers.SouthContainer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(SouthContainer.class)
public @interface South {
	Class<?>[] val();

	Class<?>[] from();

	LinkEnd end() default LinkEnd.Default;

}