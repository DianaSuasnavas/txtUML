package txtuml.importer;

import java.lang.reflect.*;
import java.util.LinkedList;
import txtuml.importer.MethodImporter;
import txtuml.api.*;

public privileged aspect ImporterAspect {
	private pointcut withinProject() : within(txtuml..*) && !within(txtuml.examples..*); // TODO only until the examples package exists
	private pointcut withinModel() : within(ModelElement+) && !within(txtuml.api..*);
	private pointcut importing() : if(MethodImporter.instructionImport());
	private pointcut isActive() : withinModel() && importing();
	
	Object around(ModelClass target): target(target) && call(* *(..)) && isActive() {
		return MethodImporter.call(target, thisJoinPoint.getSignature().getName(),thisJoinPoint.getArgs());
	}
	
	Object around() : call(static * (!ModelElement+).*()) && isActive() {
		return MethodImporter.callExternal(thisJoinPoint.getSignature().getDeclaringType(), thisJoinPoint.getSignature().getName(), thisJoinPoint.getArgs());
	}
	
	before(ModelClass target, Object newValue) : target(target) && set(* *) && args(newValue) && isActive() {
		MethodImporter.fieldSet(target,thisJoinPoint.getSignature().getName(),newValue);
	}
	
	before(ModelClass target) : target(target) && get(* *) && isActive() {
		MethodImporter.fieldGet(target,thisJoinPoint.getSignature().getName());
	}
	
	/*
	 * This advice hides all the synthetic methods from the result of Class.getDeclaredMethods() calls.
	 * It is needed to hide the private methods generated by AspectJ.
	 */
	Method[] around(Object c) : target(c) && call(Method[] Class.getDeclaredMethods()) && withinProject() {
		LinkedList<Method> methods = new LinkedList<>();
		for(Method m : proceed(c)) {
			if (!m.isSynthetic()) {
				methods.add(m);
			}
		}
		return methods.toArray(new Method[0]);
	}
	
}