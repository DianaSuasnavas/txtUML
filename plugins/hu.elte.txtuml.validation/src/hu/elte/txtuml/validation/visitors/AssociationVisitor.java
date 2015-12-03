package hu.elte.txtuml.validation.visitors;

import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import hu.elte.txtuml.export.uml2.utils.ElementTypeTeller;
import hu.elte.txtuml.validation.ProblemCollector;
import hu.elte.txtuml.validation.problems.association.WrongNumberOfAssociationEnds;
import hu.elte.txtuml.validation.problems.association.WrongTypeInAssociation;

public class AssociationVisitor extends VisitorBase {

	public static final Class<?>[] ALLOWED_ASSOCIATION_DECLARATIONS = new Class<?>[] { TypeDeclaration.class,
			SimpleName.class, SimpleType.class, Modifier.class };
			
	private TypeDeclaration root;
	private int members = 0;

	public AssociationVisitor(TypeDeclaration root, ProblemCollector collector) {
		super(collector);
		this.root = root;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		if (!ElementTypeTeller.isAssociationeEnd(node)) {
			collector.setProblemStatus(true, new WrongTypeInAssociation(collector.getSourceInfo(), node));
		}
		++members;
		return false;
	}

	@Override
	public void check() {
		if (members != 2) {
			collector.setProblemStatus(true, new WrongNumberOfAssociationEnds(collector.getSourceInfo(), root));
		}
	}

}
