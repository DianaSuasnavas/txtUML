package txtuml.api.layout.statements;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import txtuml.api.layout.containers.LeftMostContainer;
import txtuml.api.layout.elements.LayoutAbstractNode;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(LeftMostContainer.class)
public @interface LeftMost {
	Class<? extends LayoutAbstractNode> value();

}
