package hu.elte.txtuml.export.uml2.restructured.structural

import hu.elte.txtuml.export.uml2.restructured.Exporter
import hu.elte.txtuml.utils.jdt.ElementTypeTeller
import org.eclipse.jdt.core.dom.ITypeBinding
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.eclipse.uml2.uml.Class
import org.eclipse.uml2.uml.Element
import org.eclipse.uml2.uml.Region
import org.eclipse.uml2.uml.StateMachine
import org.eclipse.uml2.uml.Transition
import org.eclipse.uml2.uml.UMLPackage
import org.eclipse.uml2.uml.Vertex

class ClassExporter extends Exporter<TypeDeclaration, ITypeBinding, Class> {

	private Region region

	new(Exporter<?, ?, ?> parent) {
		super(parent)
	}

	override create(ITypeBinding typ) {
		if(ElementTypeTeller.isModelClass(typ)) factory.createClass
	}

	override exportContents(TypeDeclaration typeDecl) {
		val typeBnd = typeDecl.resolveBinding
		result.isAbstract = ElementTypeTeller.isAbstract(typeBnd)
		result.name = typeBnd.name
		val sm = result.createClassifierBehavior(result.name, UMLPackage.Literals.STATE_MACHINE) as StateMachine
		region = sm.createRegion(result.name)
		result.ownedAttributes.addAll(typeBnd.declaredFields.map[exportField])
		result.ownedOperations.addAll(typeDecl.methods.map[exportOperation])
		result.ownedBehaviors.addAll(typeDecl.methods.map[exportActivity])
		typeDecl.types.map[exportElement(it, it.resolveBinding)].forEach[storeSMElement]
	}

	def storeSMElement(Element contained) {
		switch contained {
			Vertex: region.subvertices.add(contained)
			Transition: region.transitions.add(contained)
		}
	}

}