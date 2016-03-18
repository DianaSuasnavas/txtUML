package hu.elte.txtuml.export.uml2.restructured.structural

import hu.elte.txtuml.export.uml2.restructured.Exporter
import org.eclipse.jdt.core.dom.IVariableBinding
import org.eclipse.uml2.uml.Element
import org.eclipse.uml2.uml.NamedElement
import org.eclipse.uml2.uml.Parameter
import org.eclipse.uml2.uml.Property
import org.eclipse.uml2.uml.TypedElement

abstract class TypedNamedExporter<T extends TypedElement & NamedElement> extends Exporter<IVariableBinding, IVariableBinding, T> {

	new(Exporter<?, ?, ?> parent) {
		super(parent);
	}

	override exportContents(IVariableBinding binding) {
		result.name = binding.name
		result.type = fetchType(binding.type)
	}

	override tryStore(Element contained) {
		return false;
	}
}

class FieldExporter extends TypedNamedExporter<Property> {

	new(Exporter<?, ?, ?> parent) {
		super(parent)
	}

	override create(IVariableBinding source) {
		if(source.isField) factory.createProperty
	}
}

class ParameterExporter extends TypedNamedExporter<Parameter> {

	new(Exporter<?, ?, ?> parent) {
		super(parent)
	}

	override create(IVariableBinding source) {
		if(source.isParameter) factory.createParameter
	}

}