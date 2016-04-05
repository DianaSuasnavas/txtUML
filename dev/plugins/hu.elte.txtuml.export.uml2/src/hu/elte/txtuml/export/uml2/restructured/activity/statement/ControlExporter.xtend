package hu.elte.txtuml.export.uml2.restructured.activity.statement

import hu.elte.txtuml.export.uml2.restructured.BaseExporter
import hu.elte.txtuml.export.uml2.restructured.activity.ActionExporter
import org.eclipse.uml2.uml.Action
import org.eclipse.uml2.uml.ActivityEdge
import org.eclipse.uml2.uml.ActivityNode
import org.eclipse.uml2.uml.AddVariableValueAction
import org.eclipse.uml2.uml.ExecutableNode
import org.eclipse.uml2.uml.StructuredActivityNode
import org.eclipse.uml2.uml.Type
import org.eclipse.uml2.uml.UMLPackage
import org.eclipse.uml2.uml.Variable

abstract class ControlExporter<S, R extends StructuredActivityNode> extends ActionExporter<S, R> {

	new(BaseExporter<?, ?, ?> parent) {
		super(parent)
	}

	def writeVariable(Variable variable, ExecutableNode testExpr) {
		val writevar = result.createNode("write_" + testExpr.name + "_to_" + variable.name,
			UMLPackage.Literals.ADD_VARIABLE_VALUE_ACTION) as AddVariableValueAction
		writevar.isReplaceAll = true
		writevar.createValue(testExpr.name, testExpr.type)
		writevar.variable = variable
		return writevar
	}

	def dispatch Type getType(Action node) {
		node.outputs.get(0).type
	}

	def dispatch Type getType(ExecutableNode node) {
		throw new RuntimeException("Cannot type " + node.toString)
	}

	override storeEdge(ActivityEdge edge) { result.edges += edge }

	override storeNode(ActivityNode node) { result.nodes += node }

	override storeVariable(Variable variable) { result.variables += variable }

	override getVariable(String varName) { 
		result.variables.findFirst[name == varName] ?: super.getVariable(varName)
	}

}