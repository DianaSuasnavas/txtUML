package hu.elte.txtuml.export.uml2.transform;

import hu.elte.txtuml.api.ModelElement;
import hu.elte.txtuml.api.ExternalClass;


abstract aspect AbstractImporterAspect
{
	protected pointcut withinProject() : within(hu.elte.txtuml..*) && !within(hu.elte.txtuml.examples..*); // TODO only until the examples package exists
	protected pointcut withinModel() : within(ModelElement+) && !within(ExternalClass+) && !within(hu.elte.txtuml.api..*);
	protected pointcut importing() : if(ModelImporter.isImporting());
	protected pointcut isActive() : withinModel() && importing();
}
