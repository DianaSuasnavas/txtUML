package txtuml.importer;

import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.SuppressAjWarnings;

import txtuml.api.ExternalClass;
import txtuml.api.ModelBool;
import txtuml.api.ModelClass;
import txtuml.api.ModelInt;
import txtuml.api.ModelString;
import txtuml.api.ExternalClass;
import txtuml.api.Association.AssociationEnd;
import txtuml.importer.utils.ImportException;

public privileged aspect InstructionImporterAspect extends AbstractImporterAspect {


	after() returning(ModelInt target) : call((ModelInt).new(int)) && isActive()
	{
		InstructionImporter.createModelTypeLiteral(target);
	}
	after() returning(ModelBool target) : call((ModelBool).new(boolean)) && isActive()
	{
		InstructionImporter.createModelTypeLiteral(target);
	}
	after() returning(ModelString target) : call((ModelString).new(String)) && isActive()
	{
		InstructionImporter.createModelTypeLiteral(target);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	Object around(AssociationEnd target):target(target) && call(ModelClass selectOne()) && isActive()
	{
		return InstructionImporter.importAssociationEnd_SelectOne(target);
	}


	after(ModelClass target): execution((ModelClass+).new(..)) && isActive() && target(target)
	{
		InstructionImporter.importInstanceCreation(target);
	}
	
	
	Object around(ModelClass target): target(target) && call(* *(..))  && isActive() {
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
	
	@SuppressAjWarnings
	Object around(ExternalClass target) : target(target) && call(* (ExternalClass+).*(..)) && isActive() {
		return InstructionImporter.callExternal(target, thisJoinPoint.getSignature().getName(), thisJoinPoint.getArgs());
	}
	@SuppressAjWarnings
	Object around() : call(static * (ExternalClass+).*(..)) && isActive() {
		return InstructionImporter.callStaticExternal(thisJoinPoint.getSignature().getDeclaringType(), thisJoinPoint.getSignature().getName(), thisJoinPoint.getArgs());
	}

	
	
	Object around(ModelClass target, Object newValue) : target(target) && set(* *) && args(newValue) && isActive() && !withincode((ModelClass+).new(..))
	{
		return InstructionImporter.fieldSet(target,thisJoinPoint.getSignature().getName(),newValue);
	}

	Object around(ModelClass target) : target(target) && get(* *) && isActive() {
		Signature signature=thisJoinPoint.getSignature();
		try {
			return InstructionImporter.fieldGet(target,signature.getName(),signature.getDeclaringType().getDeclaredField(signature.getName()).getType());
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		return null;
	}
}
