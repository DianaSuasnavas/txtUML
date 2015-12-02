package hu.elte.txtuml.validation.problems.transition;

import org.eclipse.jdt.core.dom.ASTNode;

import hu.elte.txtuml.validation.SourceInfo;
import hu.elte.txtuml.validation.problems.ValidationErrorBase;
import hu.elte.txtuml.validation.problems.ValidationErrorCatalog;

public class TransitionMethodParameters extends ValidationErrorBase {

	public TransitionMethodParameters(SourceInfo sourceInfo, ASTNode node) {
		super(sourceInfo, node);
	}

	@Override
	public int getID() {
		return ValidationErrorCatalog.TRANSITION_METHOD_PARAMETERS.ordinal();
	}

	@Override
	public String getMessage() {
		return "A transition effect action cannot have parameters";
	}

}
