package hu.elte.txtuml.validation.problem.state;

import org.eclipse.jdt.core.dom.ASTNode;

import hu.elte.txtuml.validation.SourceInfo;
import hu.elte.txtuml.validation.problems.ValidationErrorBase;
import hu.elte.txtuml.validation.problems.ValidationErrorCatalog;

public class UnknownClassInState extends ValidationErrorBase {

	public UnknownClassInState(SourceInfo sourceInfo, ASTNode node) {
		super(sourceInfo, node);
	}

	@Override
	public int getID() {
		return ValidationErrorCatalog.UNKNOWN_CLASS_IN_STATE.ordinal();
	}

	@Override
	public String getMessage() {
		return "In composite states only states and transitions can be declared.";
	}

}
