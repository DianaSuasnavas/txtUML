package hu.elte.txtuml.export.uml2.restructured.activity.expression

import hu.elte.txtuml.export.uml2.restructured.BaseExporter
import hu.elte.txtuml.export.uml2.restructured.activity.statement.ControlExporter
import org.eclipse.jdt.core.dom.Expression
import org.eclipse.uml2.uml.Action
import org.eclipse.uml2.uml.Class
import org.eclipse.uml2.uml.SequenceNode

abstract class CreationExporter<T> extends ControlExporter<T, SequenceNode> {
	
	new(BaseExporter<?, ?, ?> parent) {
		super(parent)
	}
	
	def createObject(String name, Class createdType) {
		val create = factory.createCreateObjectAction
		create.classifier = createdType
		create.name = '''instantiate «name»'''
		create.createResult(create.name, create.classifier)
		storeNode(create)
		return create
	}
	
}