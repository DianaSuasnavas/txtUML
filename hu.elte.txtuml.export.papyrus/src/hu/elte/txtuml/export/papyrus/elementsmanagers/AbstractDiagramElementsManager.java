package hu.elte.txtuml.export.papyrus.elementsmanagers;

import hu.elte.txtuml.export.papyrus.ModelManager;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;
import org.eclipse.emf.edit.command.RemoveCommand;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.requests.DropObjectsRequest;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.uml2.uml.Element;

/**
 * An abstract class for adding/removing elements to diagrams.
 *
 * @author Andr�s Dobreff
 */
public abstract class AbstractDiagramElementsManager{
		
	/**
	 * The ModelManager which serves the model elements
	 */
	protected ModelManager modelManager;
	
	/**
	 * The DiagramEditPart of the diagram which is to be handled
	 */
	protected DiagramEditPart diagramEditPart;
	
	/**
	 * The Constructor
	 * @param modelManager - The ModelManager which serves the model elements
	 * @param diagramEditPart - The DiagramEditPart of the diagram which is to be handled
	 */
	public AbstractDiagramElementsManager(ModelManager modelManager, DiagramEditPart diagramEditPart) {
		this.modelManager = modelManager;
		this.diagramEditPart = diagramEditPart;
	}
	
	/**
	 * Calls the {@link DropObjectsRequest} on an EditPart. 
	 * @param EP - The EditPart that is to be added to
	 * @param diagramElements - The Elements that are to be added
	 */
	protected void addElementsToEditpart(EditPart EP, List<Element> diagramElements) {
		if(!diagramElements.isEmpty()){
			DropObjectsRequest dropObjectsRequest = new DropObjectsRequest();
			dropObjectsRequest.setObjects(diagramElements);
			dropObjectsRequest.setLocation(new Point(0,0));
			Command commandDrop = EP.getCommand(dropObjectsRequest);
			if (commandDrop != null){
				commandDrop.execute();
			}
		}
	}
	
	/**
	 * Calls the {@link RemoveCommand} on an EditPart. 
	 * @param editingDomain - the domain required by {@link RemoveCommand#create(EditingDomain, Object)} 
	 * @param editParts - the EditParts that are to be removed
	 */
	protected void removeEditParts(EditingDomain editingDomain, List<EditPart> editParts) {
			List<Object> modelElements = new LinkedList<Object>();
			for(EditPart editPart : editParts){
				modelElements.add(editPart.getModel());
			}
			
			org.eclipse.emf.common.command.Command command = RemoveCommand.create(editingDomain, modelElements);
			if(command instanceof RemoveCommand){
				RemoveCommand removeCommand = (RemoveCommand) command;
				editingDomain.getCommandStack().execute(removeCommand);
			}
	}

	/**
	 * Adds the Elements to the diagram handled by the instance.
	 * @param elements - The Elements that are to be added
	 * @throws ServiceException
	 */
	public abstract void addElementsToDiagram(List<Element> elements) throws ServiceException;
}