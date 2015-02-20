package txtuml.api.layout.containers;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import txtuml.api.layout.statements.Priority;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PriorityContainer {
	Priority[] value() default {};
}
