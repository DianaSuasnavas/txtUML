package hu.elte.txtuml.validation.problems;

public enum ValidationErrorCatalog {

	INVALID_CHILDREN_ELEMENT,

	INVALID_TYPE_IN_MODEL, INVALID_MODEL_CLASS_ELEMENT, INVALID_MODIFIER, INVALID_SIGNAL_CONTENT,

	INVALID_TEMPLATE, INVALID_TYPE_WITH_CLASS_ALLOWED, INVALID_TYPE_WITH_CLASS_NOT_ALLOWED,

	WRONG_COMPOSITION_ENDS, WRONG_NUMBER_OF_ASSOCIATION_ENDS, WRONG_TYPE_IN_ASSOCIATION,

	UNKNOWN_TRANSITION_METHOD, STATE_METHOD_PARAMETERS, UNKNOWN_CLASS_IN_STATE,

	TRANSITION_METHOD_NONVOID_RETURN, TRANSITION_METHOD_PARAMETERS, 
	
	TRANSITION_FROM_OUTSIDE, TRANSITION_TO_OUTSIDE, INITIAL_TRANSITION_WITH_TRIGGER, TRANSITION_WITHOUT_TRIGGER,
	
	MISSING_TRANSITION_SOURCE, MISSING_TRANSITION_TARGET
}
