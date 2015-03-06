package hu.elte.txtuml.uml2.utils;

import java.lang.reflect.Field;

import hu.elte.txtuml.api.Association;
import hu.elte.txtuml.api.ExternalClass;
import hu.elte.txtuml.api.ModelClass;
import hu.elte.txtuml.api.ModelElement;
import hu.elte.txtuml.api.ModelIdentifiedElement;
import hu.elte.txtuml.api.Signal;

public class ElementTypeTeller {

	public static boolean isModelElement(Class<?> c)
	{
		 
		return ModelElement.class.isAssignableFrom(c) ||
			   isState(c) ||
			   isTransition(c) ;
	}
	public static boolean isModelClass(Class<?> c) {
		return ModelClass.class.isAssignableFrom(c);
	}

	public static boolean isExternalClass(Class<?> c) {
		return ExternalClass.class.isAssignableFrom(c);
	}
	public static boolean isClass(Class<?> c )
	{
		return isModelClass(c) || isExternalClass(c);
	}
	public static boolean isEvent(Class<?> c) {
		return Signal.class.isAssignableFrom(c);
	}

	public static boolean isClassifier(Class<?> c)
	{
		return isClass(c) || isEvent(c);
	}
	
	public static boolean isAssociation(Class<?> c) {
		return Association.class.isAssignableFrom(c);
	}
    
	
	public static boolean isAttribute(Field f) {
		return ModelIdentifiedElement.class.isAssignableFrom(f.getType());
    }
    
	public static boolean isState(Class<?> c) {
        return ModelClass.State.class.isAssignableFrom(c);
    }

	public static boolean isInitialState(Class<?> c) {
        return ModelClass.InitialState.class.isAssignableFrom(c);
    }

	public static boolean isCompositeState(Class<?> c) {
        return ModelClass.CompositeState.class.isAssignableFrom(c);
    }
	
	public static boolean isChoice(Class<?> c)
	{
		return ModelClass.Choice.class.isAssignableFrom(c);
	}

	public static boolean isTransition(Class<?> c) {
        return ModelClass.Transition.class.isAssignableFrom(c);
    }
    
	
}
