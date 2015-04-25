package hu.elte.txtuml.export.uml2.transform;

import hu.elte.txtuml.export.uml2.transform.backend.AssociationEnd;
import hu.elte.txtuml.export.uml2.transform.backend.ImportException;
import hu.elte.txtuml.export.uml2.utils.MultiplicityProvider;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Type;

/**
 * Instances of this class are responsible for importing associations.
 * @author Adam Ancsin
 *
 */
class AssociationImporter extends AbstractImporter{
	
	/**
	 * Creates an AssocationImporter instance.
	 * @param sourceClass The class representing the txtUML association.
	 * @param currentModel The current UML2 model.
	 */
	AssociationImporter(Class<?> sourceClass, Model currentModel)
	{
		this.sourceClass=sourceClass;
		this.currentModel=currentModel;
	}
	
	/**
	 * Gets the current UML2 association.
	 * @return The current UML2 association.
	 *
	 * @author Adam Ancsin
	 */
	Association getAssociation()
	{
		return currentAssociation;
	}
	
	/**
	 * Imports the association.
	 * @return The imported UML2 association.
	 * @throws ImportException
	 *
	 * @author Adam Ancsin
	 */
	Association importAssociation() throws ImportException
	{
	    List<Class<?> > classes = new LinkedList<Class<?> >(Arrays.asList(sourceClass.getDeclaredClasses()));
	    
	    currentAssociation= createAssociation(classes);
	    return currentAssociation;
	}

	/**
	 * Creates the UML2 association.
	 * @param classes The declared classes (representing the association ends of the association) of the txtUML association.
	 * @return The created UML2 association.
	 * @throws ImportException
	 *
	 * @author Adam Ancsin
	 */
	private Association createAssociation(List<Class<?> > classes) throws ImportException
	{
		if(classes.size()!=2)
		{
			throw new ImportException(
					"The following association doesn't have exactly 2 association ends: " + 
					sourceClass.getCanonicalName()
				);
		}
		
		AssociationEnd end1=importAssociationEnd(classes.get(0));
	    AssociationEnd end2=importAssociationEnd(classes.get(1));
	    
	    Association assoc=end1.getType().createAssociation
	     		(end2.isNavigable(), end2.getAggregationKind(), end2.getName() ,end2.getLowerBound(), end2.getUpperBound(),
	      		 end2.getType(), end1.isNavigable(), end1.getAggregationKind(), end1.getName() , end1.getLowerBound(), end1.getUpperBound());
	    
	    assoc.setName(sourceClass.getSimpleName());
	    return assoc;
	}
	
	/**
	 * Imports an association end.
	 * @param sourceClass The class representing the txtUML association end.
	 * @return The imported association end.
	 * @throws ImportException
	 *
	 * @author Adam Ancsin
	 */
	@SuppressWarnings("rawtypes")
	private  AssociationEnd importAssociationEnd(Class sourceClass) throws ImportException
	{
	    String phrase = sourceClass.getSimpleName();
	    Class genericParameter0 =(Class)
	    		((ParameterizedType)sourceClass.getGenericSuperclass())
	    		.getActualTypeArguments()[0];
	    	    
	    
	    String className = genericParameter0.getSimpleName();

		int lowerBound = MultiplicityProvider.getLowerBound(sourceClass);
		int upperBound = MultiplicityProvider.getUpperBound(sourceClass);
		
	    if(MultiplicityProvider.hasInvalidMultiplicity(sourceClass))
			throw new ImportException("Association end "+sourceClass.getName()+" has invalid multiplicity.");            
	    
	    boolean navigable;
	    
	    if(hu.elte.txtuml.api.assocends.Navigability.Navigable.class.isAssignableFrom(sourceClass))
	    	navigable = true;
	    else if(hu.elte.txtuml.api.assocends.Navigability.NonNavigable.class.isAssignableFrom(sourceClass))
	    	navigable = false;
	    else
	    	throw new ImportException("Association end "+sourceClass.getName()+" has invalid navigability.");    
	    
	    org.eclipse.uml2.uml.Type participant = (Type) currentModel.getMember(className);
	    
	    if(participant == null)
	        throw new ImportException(phrase + ": No class " + className + " found in this model.");
	   
	    return new AssociationEnd(participant,phrase,lowerBound,upperBound,AggregationKind.NONE_LITERAL, navigable);
	}

	private Class<?> sourceClass;
	private Model currentModel;
	private Association currentAssociation;
}
