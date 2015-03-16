package hu.elte.txtuml.export.uml2.transform;

import hu.elte.txtuml.export.uml2.transform.backend.AssociationEnd;
import hu.elte.txtuml.export.uml2.utils.ImportException;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.uml2.uml.AggregationKind;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Type;

class AssociationImporter extends AbstractImporter{
	
	
	AssociationImporter(Class<?> sourceClass, Model currentModel)
	{
		this.sourceClass=sourceClass;
		this.currentModel=currentModel;
	}
	
	Association getAssociation()
	{
		return currentAssociation;
	}
	Association importAssociation() throws ImportException
	{
	    List<Class<?> > classes = new LinkedList<Class<?> >(Arrays.asList(sourceClass.getDeclaredClasses()));
	    
	    currentAssociation= createAssociation(classes);
	    return currentAssociation;
	}

	private Association createAssociation(List<Class<?> > classes) throws ImportException
	{
		AssociationEnd end1=importAssociationEnd(classes.get(0));
	    AssociationEnd end2=importAssociationEnd(classes.get(1));
	    
	    Association assoc=end1.getType().createAssociation
	     		(end2.isNavigable(), end2.getAggregationKind(), end2.getName() ,end2.getLowerBound(), end2.getUpperBound(),
	      		 end2.getType(), end1.isNavigable(), end1.getAggregationKind(), end1.getName() , end1.getLowerBound(), end1.getUpperBound());
	    
	    assoc.setName(sourceClass.getSimpleName());
	    return assoc;
	}
	
	@SuppressWarnings("rawtypes")
	private  AssociationEnd importAssociationEnd(Class sourceClass) throws ImportException
	{
	    String phrase = sourceClass.getSimpleName();
	    Class genericParameter0 =(Class)
	    		((ParameterizedType)sourceClass.getGenericSuperclass())
	    		.getActualTypeArguments()[0];
	    	    
	    
	    String className = genericParameter0.getSimpleName();
	   
	    int lowerBound; 
		int upperBound; 
	    
	    if(hu.elte.txtuml.api.semantics.Multiplicity.One.class.isAssignableFrom(sourceClass))
		{
			lowerBound=upperBound=1;
		}
		else if(hu.elte.txtuml.api.semantics.Multiplicity.ZeroToOne.class.isAssignableFrom(sourceClass))
		{
			lowerBound=0;
			upperBound=1;
		}
		else if(hu.elte.txtuml.api.semantics.Multiplicity.ZeroToUnlimited.class.isAssignableFrom(sourceClass))
		{
			lowerBound=0;
			upperBound= org.eclipse.uml2.uml.LiteralUnlimitedNatural.UNLIMITED;
		}
		else if(hu.elte.txtuml.api.semantics.Multiplicity.OneToUnlimited.class.isAssignableFrom(sourceClass))
		{
			lowerBound=1;
			upperBound= org.eclipse.uml2.uml.LiteralUnlimitedNatural.UNLIMITED;
		}
		else
		{
			throw new ImportException("Invalid multiplicity.");            
		}
	    
	    boolean navigable;
	    
	    if(hu.elte.txtuml.api.semantics.Navigability.Navigable.class.isAssignableFrom(sourceClass))
	    {
	    	navigable = true;
	    }
	    else if(hu.elte.txtuml.api.semantics.Navigability.NonNavigable.class.isAssignableFrom(sourceClass))
	    {
	    	navigable = false;
	    }
	    else
	    {
	    	throw new ImportException("Invalid navigability");
	    }
	    
	    org.eclipse.uml2.uml.Type participant = (Type) currentModel.getMember(className);
	    
	    if(participant == null)
	    {
	        throw new ImportException(phrase + ": No class " + className + " found in this model.");
	    }
	   
	    return new AssociationEnd(participant,phrase,navigable,AggregationKind.NONE_LITERAL,lowerBound,upperBound);
	}

	private Class<?> sourceClass;
	private Model currentModel;
	private Association currentAssociation;
}
