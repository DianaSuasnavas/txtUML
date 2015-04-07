package hu.elte.txtuml.export.uml2.transform;

import hu.elte.txtuml.api.ModelBool;
import hu.elte.txtuml.api.ModelClass;
import hu.elte.txtuml.api.ModelIdentifiedElement;
import hu.elte.txtuml.api.ModelInt;
import hu.elte.txtuml.api.blocks.BlockBody;
import hu.elte.txtuml.api.blocks.Condition;
import hu.elte.txtuml.api.blocks.ParameterizedBlockBody;
import hu.elte.txtuml.export.uml2.utils.FieldValueAccessor;
import hu.elte.txtuml.utils.Pair;

import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.ActivityEdge;
import org.eclipse.uml2.uml.ActivityNode;
import org.eclipse.uml2.uml.Association;
import org.eclipse.uml2.uml.CreateLinkAction;
import org.eclipse.uml2.uml.DecisionNode;
import org.eclipse.uml2.uml.DestroyLinkAction;
import org.eclipse.uml2.uml.DestroyObjectAction;
import org.eclipse.uml2.uml.LinkAction;
import org.eclipse.uml2.uml.LinkEndData;
import org.eclipse.uml2.uml.Property;
import org.eclipse.uml2.uml.SendSignalAction;
import org.eclipse.uml2.uml.Signal;
import org.eclipse.uml2.uml.Type;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.ValuePin;

/**
 * This class is responsible for importing actions (Action.* calls) in method bodies.
 * 
 * @author �d�m Ancsin
 *
 */
public class ActionImporter extends AbstractMethodImporter {

	/**
	 * Enumerates the types of link actions: there are create and destroy link actions.
	 * @author �d�m Ancsin
	 *
	 */
	private enum LinkActionTypes 
	{
		CREATE_LINK_LITERAL,
		DESTROY_LINK_LITERAL
	}
	/**
	 * Imports a create link action in a method body.
	 * @param leftEndClass The class of the left association end.
	 * @param leftEndObj The left end dummy instance you want to link.
	 * @param rightEndClass The class of the right association end.
	 * @param rightEndObj The right end dummy instance you want to link.
	 */
	static void importCreateLinkAction(Class<?> leftEndClass, ModelClass leftEndObj,
			Class<?> rightEndClass, ModelClass rightEndObj)
	{
		ActionImporter.importLinkAction(leftEndClass,leftEndObj,rightEndClass,rightEndObj,LinkActionTypes.CREATE_LINK_LITERAL);
	}
	
	/**
	 * Imports a destroy link action in a method body.
	 * @param leftEndClass The class of the left association end.
	 * @param leftEndObj The left end dummy instance of the link you want to destroy.
	 * @param rightEndClass The class of the right association end.
	 * @param rightEndObj The right end dummy instance of the link you want to destroy.
	 * 
	 * @author �d�m Ancsin
	 */
	static void importDestroyLinkAction(Class<?> leftEndClass, ModelClass leftEndObj,
			Class<?> rightEndClass, ModelClass rightEndObj)
	{
		ActionImporter.importLinkAction(leftEndClass,leftEndObj,rightEndClass,rightEndObj,LinkActionTypes.DESTROY_LINK_LITERAL);
	}
	
	/**
	 * Imports a send signal action in a method body.
	 * @param receiver The dummy instance of the receiving model class.
	 * @param event The dummy instance of the event to be sent.
	 * 
	 * @author �d�m Ancsin
	 */
	static void importSendSignalAction(ModelClass receiver, hu.elte.txtuml.api.Signal event) 
	{
		if(currentActivity != null) 
		{
			Signal signalToSend=(Signal) currentModel.getOwnedType(event.getClass().getSimpleName());
			String instanceName=getObjectIdentifier(receiver);

			Type instanceType=currentModel.getOwnedType(receiver.getClass().getSimpleName());

			SendSignalAction sendSignalAction	=	(SendSignalAction) 
					currentActivity.createOwnedNode("send_"+signalToSend.getName()+"_to_"+instanceName,UMLPackage.Literals.SEND_SIGNAL_ACTION);

			sendSignalAction.setSignal(signalToSend);

			ValuePin target=(ValuePin)sendSignalAction.createTarget(sendSignalAction.getName()+"_target",instanceType,UMLPackage.Literals.VALUE_PIN);

			createAndAddOpaqueExpressionToValuePin(target,instanceName,instanceType);

			addArgumentsToSendSignalAction(sendSignalAction,event,signalToSend);
			createControlFlowBetweenActivityNodes(lastNode,sendSignalAction);

			lastNode=sendSignalAction;
		}
	}

	/**
	 * Imports a "while" statement (Action.While) in a method body.
	 * @param cond The loop condition.
	 * @param body The loop body.
	 *
	 * @author �d�m Ancsin
	 */
	static void importWhileStatement(Condition cond, BlockBody body)
	{
		String condExpr=importCondition(cond);
		DecisionNode decisionNode=createNextDecisionNode();
		createEdgeBetweenActivityNodes(lastNode,decisionNode);

		lastNode=decisionNode;

		Pair<ActivityNode,ActivityEdge> importThenBodyResult=importBlockBody(body);
		ActivityEdge thenFirstEdge=importThenBodyResult.getValue();
		ActivityNode thenLastNode=importThenBodyResult.getKey();
		addGuardToActivityEdge(thenFirstEdge, condExpr);

		createEdgeBetweenActivityNodes(thenLastNode,decisionNode);
		unfinishedDecisionNodes.push(decisionNode);
		lastNode=decisionNode;
	}

	/**
	 * Imports a simple (no "else" body present) "if" statement (Action.If) in a method body.
	 * @param cond The condition.
	 * @param thenBody The body of the "then" block.
	 *
	 * @author �d�m Ancsin
	 */
	static void importIfStatement(Condition cond, BlockBody thenBody)
	{
		String condExpr=importCondition(cond);

		Pair<ActivityNode,ActivityEdge> importBlockBodyResult=importBlockBody(thenBody);
		ActivityEdge thenFirstEdge=importBlockBodyResult.getValue();
		ActivityNode thenLastNode=importBlockBodyResult.getKey();
		
		if(thenFirstEdge!=null)
			addGuardToActivityEdge(thenFirstEdge, condExpr);
		
		lastNode=thenLastNode;
	}

	/**
	 * Imports a complete (with "else" block) "if" statement (Action.If) in a method body.
	 * @param cond The condition.
	 * @param thenBody The body of the "then" block.
	 * @param elseBody The body of the "else" block.
	 *
	 * @author �d�m Ancsin
	 */
	static void importIfStatement(Condition cond, BlockBody thenBody,BlockBody elseBody)
	{
		String condExpr=importCondition(cond);
		DecisionNode decisionNode=createNextDecisionNode();
		createEdgeBetweenActivityNodes(lastNode,decisionNode);

		lastNode=decisionNode;

		Pair<ActivityNode,ActivityEdge> importThenBodyResult=importBlockBody(thenBody);
		ActivityEdge thenFirstEdge=importThenBodyResult.getValue();
		ActivityNode thenLastNode=importThenBodyResult.getKey();
		addGuardToActivityEdge(thenFirstEdge, condExpr);

		lastNode=decisionNode;

		Pair<ActivityNode,ActivityEdge> importElseBodyResult=importBlockBody(elseBody);
		ActivityEdge elseFirstEdge=importElseBodyResult.getValue();
		ActivityNode elseLastNode=importElseBodyResult.getKey();
		addGuardToActivityEdge(elseFirstEdge, "else");

		lastNode=createMergeNode(thenLastNode,elseLastNode);
	}

	/**
	 * Imports a "for" statement (Action.For with simple incrementing - by 1 - counter) in a method body.
	 * @param from The dummy instance of the initial value of the counter. 
	 * @param to The dummy instance of the target value of the counter.
	 * @param body The loop body.
	 *
	 * @author �d�m Ancsin
	 */
	static void importForStatement(ModelInt from, ModelInt to, ParameterizedBlockBody<ModelInt> body) 
	{
		String fromExpression=getExpression(from);
		String toExpression=getExpression(to);
		ModelInt loopVar=new ModelInt();
		String loopVarId=getObjectIdentifier(loopVar);
		String condExpr=loopVarId+"<="+toExpression;

		setVariableValue(loopVar, fromExpression);

		DecisionNode decisionNode=createNextDecisionNode();
		createEdgeBetweenActivityNodes(lastNode,decisionNode);

		lastNode=decisionNode;

		Pair<ActivityNode,ActivityEdge> importThenBodyResult=importParameterizedBlockBody(body,loopVar);
		ActivityEdge thenFirstEdge=importThenBodyResult.getValue();
		
		if(thenFirstEdge!=null)
			addGuardToActivityEdge(thenFirstEdge, condExpr);
		
		setVariableValue(loopVar, loopVarId+" + 1"); // inc loopVar by 1
		createEdgeBetweenActivityNodes(lastNode,decisionNode);
		
		unfinishedDecisionNodes.push(decisionNode);
		lastNode=decisionNode;
	}

	/**
	 * Imports a delete object action.
	 * @param obj The object to be deleted.
	 *
	 * @author �d�m Ancsin
	 */
	static void importDeleteObjectAction(ModelClass obj) 
	{
	    if(currentActivity != null) 
	    {
	       	DestroyObjectAction destroyAction=	(DestroyObjectAction) 
					currentActivity.createOwnedNode("delete_"+obj.getIdentifier(),UMLPackage.Literals.DESTROY_OBJECT_ACTION);
	
			String instanceName=getObjectIdentifier(obj);
	
			Type type= currentModel.getOwnedType(obj.getClass().getSimpleName());
	
			ValuePin target = (ValuePin) destroyAction.createTarget("target", type, UMLPackage.Literals.VALUE_PIN);
			createAndAddOpaqueExpressionToValuePin(target,instanceName,type);
	
			createControlFlowBetweenActivityNodes(lastNode,destroyAction);
	
			lastNode=destroyAction;
	    }
	}
	/**
	 * Adds the association end to the link (create or destroy link) action
	 * @param linkAction The link action.
	 * @param association The association.
	 * @param phrase The phrase of the association end.
	 * @param instName The name of the instance on the end.
	 * @param obj The dummy instance on the end.
	 * @param endNum Number of the association end. (1 or 2)
	 *
	 * @author �d�m Ancsin
	 */
	private static void addEndToLinkAction(LinkAction linkAction, Association association, 
			String phrase, String instName,ModelClass obj,int endNum)
	{
		Type endType=ModelImporter.importType(obj.getClass());

		ValuePin end_valuePin=(ValuePin) 
				linkAction.createInputValue(linkAction.getName()+"_end"+endNum+"input",endType,UMLPackage.Literals.VALUE_PIN);

		createAndAddOpaqueExpressionToValuePin(end_valuePin,instName,endType);

		LinkEndData end=linkAction.createEndData();
		Property endProp=association.getOwnedEnd(phrase,endType);
		end.setEnd(endProp);
	}

	/**
	 * Imports a link (create or destroy) action in a method body.
	 * @param leftEndClass The class of the left association end.
	 * @param leftEndObj The left end dummy instance of the link you want to create/destroy.
	 * @param rightEndClass The class of the right association end.
	 * @param rightEndObj The right end dummy instance of the link you want to create/destroy.
	 * @param linkActionType The type of the link action (create/destroy).
	 * 
	 * @author �d�m Ancsin
	 */
	private static void importLinkAction(Class<?> leftEndClass, ModelClass leftEndObj,
			Class<?> rightEndClass, ModelClass rightEndObj, LinkActionTypes linkActionType)
	{
		if(currentActivity != null) 
		{
			String leftName=getObjectIdentifier(leftEndObj);
			String rightName=getObjectIdentifier(rightEndObj);
			String leftPhrase=leftEndClass.getSimpleName();
			String rightPhrase=rightEndClass.getSimpleName();

			String assocName=leftEndClass.getDeclaringClass().getSimpleName();

			Association association=(Association)currentModel.getOwnedMember(assocName);

			String linkActionName=null;
			LinkAction linkAction=null;

			switch(linkActionType)
			{
				case CREATE_LINK_LITERAL:
					linkActionName="link_"+leftName+"_and_"+rightName;
					linkAction	=	(CreateLinkAction) 
							currentActivity.createOwnedNode(linkActionName, UMLPackage.Literals.CREATE_LINK_ACTION);
					break;
				case DESTROY_LINK_LITERAL:
					linkActionName="unlink_"+leftName+"_and_"+rightName;
					linkAction	=	(DestroyLinkAction) 
							currentActivity.createOwnedNode(linkActionName, UMLPackage.Literals.DESTROY_LINK_ACTION);
					break;
			}

			addEndToLinkAction(linkAction,association,leftPhrase,leftName,leftEndObj,1);
			addEndToLinkAction(linkAction,association,rightPhrase,rightName,rightEndObj,2);
			createControlFlowBetweenActivityNodes(lastNode,linkAction);
			lastNode=linkAction;
		}
	}

	/**
	 * Adds the actual parameters of the signal to a send signal action activity node.
	 * @param sendSignalAction The send signal action activity node.
	 * @param event The dummy instance of the txtUML event.
	 * @param signalToSend The UML2 signal to be sent.
	 *
	 * @author �d�m Ancsin
	 */
	private static void addArgumentsToSendSignalAction
		(SendSignalAction sendSignalAction, hu.elte.txtuml.api.Signal event, Signal signalToSend)
	{
		for(Property signalAttribute:signalToSend.getAllAttributes())
		{
			String attributeName=signalAttribute.getName();
			Type attributeType=signalAttribute.getType();

			ValuePin argValuePin = (ValuePin)
					sendSignalAction.createArgument(attributeName,attributeType,UMLPackage.Literals.VALUE_PIN);

			try
			{
				ModelIdentifiedElement attributeInstance=
						(ModelIdentifiedElement) FieldValueAccessor.getObjectFieldVal(event,attributeName);
						
				createAndAddValueExpressionToValuePin(argValuePin,attributeInstance,attributeType);	
			}
			catch(Exception e)
			{
				
			}
		}
	}
	
	/**
	 * Imports a condition of an "if"/"for"/"while" statement.
	 * @param cond The condition.
	 * @return The condition converted to an expression.
	 *
	 * @author �d�m Ancsin
	 */
	private static String importCondition(Condition cond)
	{
		Activity currActivityBackup=currentActivity;
		currentActivity=null;
		ModelBool checkedCond=cond.check();
		String ret= getExpression(checkedCond);
		currentActivity=currActivityBackup;
		return ret;
	}

	/**
	 * Creates the return value of the importBlockBody method
	 * @return The created return value.
	 *
	 * @author �d�m Ancsin
	 */
	private static Pair<ActivityNode,ActivityEdge> createImportBlockBodyRetVal()
	{
		ActivityEdge firstEdge;
		if(blockBodyFirstEdges.size()==cntBlockBodiesBeingImported)
			firstEdge=blockBodyFirstEdges.pop();
		else
			firstEdge=null;

		Pair<ActivityNode,ActivityEdge> ret=new Pair<>(lastNode,firstEdge);
		return ret;
	}
	
	/**
	 * Imports a block body of an "if"/"while" statement.
	 * @param body The block body.
	 * @return A pair containing the last activity node (left) and the first activity edge (right) of the imported block. 
	 *
	 * @author �d�m Ancsin
	 */
	private static Pair<ActivityNode,ActivityEdge> importBlockBody(BlockBody body)
	{
		++cntBlockBodiesBeingImported;

		body.run();		
		Pair<ActivityNode,ActivityEdge>  ret=createImportBlockBodyRetVal();

		--cntBlockBodiesBeingImported;
		return ret;
	}

	/**
	 * Imports a parameterized block body of a "for" statement.
	 * @param body The block body.
	 * @param param Dummy instance of the parameter of the block.
	 * @return A pair containing the last activity node (left) and the first activity edge (right) of the imported block. 
	 *
	 * @author �d�m Ancsin
	 */
	private static <T> Pair<ActivityNode,ActivityEdge> importParameterizedBlockBody(ParameterizedBlockBody<T> body, T param)
	{
		++cntBlockBodiesBeingImported;

		body.run(param);		
		Pair<ActivityNode,ActivityEdge>  ret=createImportBlockBodyRetVal();

		--cntBlockBodiesBeingImported;
		return ret;
	}

	/**
	 * Creates the next decision node in the current activity.
	 * @return The created decision node.
	 *
	 * @author �d�m Ancsin
	 */
	private static DecisionNode createNextDecisionNode()
	{
		++cntDecisionNodes;
		String name="decision"+cntDecisionNodes;
		DecisionNode decisionNode=(DecisionNode) currentActivity.createOwnedNode(name,UMLPackage.Literals.DECISION_NODE);
		
		return decisionNode;
	}
}
