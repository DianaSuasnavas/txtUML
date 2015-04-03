package hu.elte.txtuml.export.uml2.transform.aspects;

import hu.elte.txtuml.api.AssociationEnd;
import hu.elte.txtuml.api.ExternalClass;
import hu.elte.txtuml.api.ModelBool;
import hu.elte.txtuml.api.ModelClass;
import hu.elte.txtuml.api.ModelInt;
import hu.elte.txtuml.api.ModelString;
import hu.elte.txtuml.api.Signal;
import hu.elte.txtuml.api.StateMachine.Transition;

import hu.elte.txtuml.export.uml2.transform.backend.ImportException;
import hu.elte.txtuml.export.uml2.transform.backend.DummyInstanceCreator;
import hu.elte.txtuml.export.uml2.transform.InstructionImporter;

import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.SuppressAjWarnings;


/**
 * This aspect contains advices (and some pointcuts) for importing instructions that are not actions (Action.* calls)
 * nor ModelType operations.
 * 
 * @author �d�m Ancsin
 */
public privileged aspect InstructionImporterAspect extends AbstractImporterAspect {

	/**
	 * This pointcut indicates that dummy instance creation is in progress. 
	 *
	 * @author �d�m Ancsin
	 */
	private pointcut creatingDummyInstance() : if(DummyInstanceCreator.isCreating());
	
	/**
	 * This pointcut indicates that "assoc" is being called.
	 * @author �d�m Ancsin
	 */
	private pointcut callingAssocMethod(): if(thisJoinPoint.getSignature().getName().equals("assoc"));
	
	/**
	 * This advice imports ModelInt literal creation.
	 * Runs after the one-parameter constructor (if called during model import from a txtUML method body)
	 * of ModelInt returns with the created instance.
	 * @param created The created instance. 
	 *
	 * @author �d�m Ancsin
	 */
	@SuppressAjWarnings
	after() returning(ModelInt created) : call((ModelInt).new(int)) && isActive() && !creatingDummyInstance()
	{
		InstructionImporter.importModelTypeLiteralCreation(created);
	}
	
	/**
	 * This advice imports ModelBool literal creation.
	 * Runs after the one-parameter constructor (if called during model import from a txtUML method body)
	 * of ModelBool returns with the created instance.
	 * @param created The created instance. 
	 *
	 * @author �d�m Ancsin
	 */
	@SuppressAjWarnings
	after() returning(ModelBool created) : call((ModelBool).new(boolean)) && isActive() && !creatingDummyInstance()
	{
		InstructionImporter.importModelTypeLiteralCreation(created);
	}
	
	/**
	 * This advice imports ModelString literal creation.
	 * Runs after the one-parameter constructor (if called during model import from a txtUML method body)
	 * of ModelString returns with the created instance.
	 * @param created The created instance. 
	 *
	 * @author �d�m Ancsin
	 */
	@SuppressAjWarnings
	after() returning(ModelString created) : call((ModelString).new(String)) && isActive() && !creatingDummyInstance()
	{
		InstructionImporter.importModelTypeLiteralCreation(created);
	}
	
	/**
	 * This advice imports a selectOne call on an association end if called from a txtUML method body
	 * during model import.
	 * 
	 * @param target The target association end.
	 * @return The dummy instance of the result of the call.
	 *
	 * @author �d�m Ancsin
	 */
	@SuppressWarnings( "rawtypes")
	@SuppressAjWarnings
	Object around(AssociationEnd target):target(target) && call(ModelClass selectOne()) && isActive()
	{
		return InstructionImporter.importAssociationEnd_SelectOne(target);
	}

	/**
	 * This advice imports a ModelClass instance creation in a txtUML method body.
	 * Runs after the constructor of a subclass of ModelClass is executed (called from a txtUML method body)
	 * during model import.
	 * @param created The created instance.
	 *
	 * @author �d�m Ancsin
	 */
	@SuppressAjWarnings
	after(ModelClass created): execution((ModelClass+).new(..)) && isActive() && target(created)
	{
		InstructionImporter.importInstanceCreation(created);
	}
	
	/**
	 * This advice provides a proper return value (a dummy instance) for a getSignal call of a
	 * transition during model import.
	 * 
	 * @param target The target transition of the call.
	 * @return The dummy instance of the trigger signal.
	 *
	 * @author �d�m Ancsin
	 */
	@SuppressAjWarnings
	Signal around(Transition target):call(Signal getSignal()) && target(target) &&  importing()
	{
		return InstructionImporter.initAndGetSignalInstanceOfTransition(target);
	}
		
	/**
	 * This advice imports a ModelClass member function call in a txtUML method body.
	 * Runs if the method is called in a txtUML method body during model import.
	 * @param target The target ModelClass (dummy) instance.
	 * @return The dummy instance of the return value of the method.
	 *
	 * @author �d�m Ancsin
	 */
	@SuppressAjWarnings
	Object around(ModelClass target): target(target) && call(* *(..))  && isActive() && !callingAssocMethod()
	{
		try
		{
			return InstructionImporter.importMethodCall(target, thisJoinPoint.getSignature().getName(),thisJoinPoint.getArgs());
		}
		catch(ImportException exc)
		{
			//exc.printStackTrace();
			return null;
		}
	}
	
	/**
	 * This advice imports an external method call (member function of an ExternalClass) in a txtUML method body.
	 * Runs if the method is called in a txtUML method body during model import.
	 * @param target The target ExternalClass (dummy) instance.
	 * @return The dummy instance of the return value of the called method.
	 *
	 * @author �d�m Ancsin
	 */
	@SuppressAjWarnings
	Object around(ExternalClass target) : target(target) && call(* (ExternalClass+).*(..)) && isActive() {
		return InstructionImporter.importExternalMethodCall(
				target, 
				thisJoinPoint.getSignature().getName(), 
				thisJoinPoint.getArgs()
			);
	}
	
	/**
	 * This advice imports a static external method call (static member function of an ExternalClass) in a txtUML method body.
	 * Runs if the method is called in a txtUML method body during model import.
	 *
	 * @return The dummy instance of the return value of the called method.
	 *
	 * @author �d�m Ancsin
	 */
	@SuppressAjWarnings
	Object around() : call(static * (ExternalClass+).*(..)) && isActive() {
		return InstructionImporter.importExternalStaticMethodCall(
				thisJoinPoint.getSignature().getDeclaringType(),
				thisJoinPoint.getSignature().getName(),
				thisJoinPoint.getArgs()
			);
	}

	/**
	 * This advice imports a field set of a ModelClass instance.
	 * Runs if a field of a ModelClass instance is set in a txtUML method body during model import.
	 * 
	 * @param target The ModelClass (dummy) instance
	 * @param newValue The new value to be assigned to the field.
	 * @return The dummy instance of the field.
	 *
	 * @author �d�m Ancsin
	 */
	@SuppressAjWarnings
	Object around(ModelClass target, Object newValue) : target(target) &&
														set(* *) && 
														args(newValue) && 
														isActive() &&
														!withincode((ModelClass+).new(..))
	{
		return InstructionImporter.importModelClassFieldSet(target,thisJoinPoint.getSignature().getName(),newValue);
	}

	/**
	 * This advice imports a field get of a ModelClass instance.
	 * Runs if a field of a ModelClass instance is accessed in a txtUML method body during model import.
	 * 
	 * @param target The ModelClass (dummy) instance
	 * @return The dummy instance of the field.
	 *
	 * @author �d�m Ancsin
	 */
	@SuppressAjWarnings
	Object around(ModelClass target) : target(target) && get(* *) && isActive() {
		Signature signature=thisJoinPoint.getSignature();
		try {
			return InstructionImporter.importModelClassFieldGet(target,signature.getName(),signature.getDeclaringType().getDeclaredField(signature.getName()).getType());
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * This advice imports a field get of an ExternaClass instance.
	 * Runs if a field of an ExternalClass instance is accessed in a txtUML method body during model import.
	 * 
	 * @param target The ExternalClass (dummy) instance
	 * @return The dummy instance of the field.
	 *
	 * @author �d�m Ancsin
	 */
	@SuppressAjWarnings
	Object around(ExternalClass target) : target(target) && get(* *) && isActive() {
		Signature signature=thisJoinPoint.getSignature();
		try {
			return InstructionImporter.importExternalClassFieldGet(target,signature.getName(),signature.getDeclaringType().getDeclaredField(signature.getName()).getType());
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return null;
	}
}
