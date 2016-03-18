package hu.elte.txtuml.export.uml2.restructured

import hu.elte.txtuml.utils.jdt.ElementTypeTeller
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.eclipse.uml2.uml.Class
import org.eclipse.uml2.uml.Element
import org.eclipse.uml2.uml.Operation
import org.eclipse.uml2.uml.Property

class ClassExporter extends Exporter<TypeDeclaration, Class> {
	
	new(ExporterRegistry registry) {
		super(registry, Class)
	}
	
	override create() { factory.createClass }
	
	override exportContents(Class cls, TypeDeclaration typeDecl) {
		cls.isAbstract = ElementTypeTeller.isAbstract(typeDecl)
		cls.name = typeDecl.name.identifier
//		typeDecl.fields.forEach[ attributeExporter.export ]
	}
	
	override tryStore(Class cls, Element contained) {
		switch contained {
			Operation: cls.ownedOperations.add(contained)
			Property: cls.ownedAttributes.add(contained)
			default: return false
		}
		return true;
	}
	
}