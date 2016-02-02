package hu.elte.txtuml.export.uml2.transform.exporters.actions;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.uml2.uml.CreateObjectAction;
import org.eclipse.uml2.uml.Operation;
import org.eclipse.uml2.uml.UMLPackage;

import hu.elte.txtuml.export.uml2.transform.exporters.expressions.Expr;
import hu.elte.txtuml.export.uml2.transform.exporters.expressions.ExpressionExporter;

public class CreateObjectActionExporter {

	private final ExpressionExporter expressionExporter;

	public CreateObjectActionExporter(ExpressionExporter expressionExporter) {
		this.expressionExporter = expressionExporter;
	}

	public Expr export(IMethodBinding ctorBinding, List<Expr> args) {
		args.forEach(Expr::evaluate);

		Iterator<Expr> it = args.iterator();
		Expr type = it.next();
		it.remove();
		CreateObjectAction createAction = (CreateObjectAction) expressionExporter
				.createExecutableNode("instantiate " + type.getName(),
						UMLPackage.Literals.CREATE_OBJECT_ACTION);
		Expr target = Expr.ofPin(
				createAction.createResult("new " + type.getName(),
						type.getType()), "new " + type.getName());

		Operation constructor = expressionExporter.getTypeExporter()
				.exportMethodAsOperation(ctorBinding, args);
		
		if (constructor != null) {
			// non-default constructor
			expressionExporter.createCallOperationAction(constructor,
					target, args);			
		}
		return target;

	}
}
