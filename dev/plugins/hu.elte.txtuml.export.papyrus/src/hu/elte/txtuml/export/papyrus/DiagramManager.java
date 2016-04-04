package hu.elte.txtuml.export.papyrus;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.diagram.ui.parts.IDiagramWorkbenchPart;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.papyrus.commands.ICreationCommand;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.gmfdiag.common.model.NotationModel;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.NamedElement;

/**
 * Controls the diagrams. 
 */
public class DiagramManager {
	private ModelSet modelSet;
	
	/**
	 * The constructor
	 * @param editor - The editor to the instance will be attached.
	 */
	public DiagramManager(ServicesRegistry registry) {

		try {
			this.modelSet = registry.getService(ModelSet.class);
		} catch (ServiceException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Calls the createDiagram method of the {@link ICreationCommand} with the containers.
	 * The ModelSet will be the ModelSet that is attached to the editor of the DiagramManager.
	 * The diagrams name will be the name of the container.
	 * @param containers - the {@link Element}s thats children will be placed on the diagram.
	 * @param diagramCreationCommand - the Command that will be executed.
	 */
	public void createDiagrams(List<Element> containers, ICreationCommand diagramCreationCommand,
			TransactionalEditingDomain domain){
		for(Element container : containers){
			this.createDiagram(container, ((NamedElement)container).getName(), diagramCreationCommand, domain);
		}
	}
	
	/**
	 * Calls the createDiagram method of the {@link ICreationCommand} with the container.
	 * The ModelSet will be the ModelSet that is attached to the editor of the DiagramManager.
	 * 
	 * @param container - the {@link Element} thats children will be placed on the diagram.
	 * @param diagramName - The name of the new Diagram
	 * @param diagramCreationCommand - the Command that will be executed.
	 */
	public void createDiagram(Element container, String diagramName,
			ICreationCommand diagramCreationCommand, TransactionalEditingDomain domain){
		try {
			IProgressService progressService = PlatformUI.getWorkbench()
					.getProgressService();

			progressService.runInUI(progressService, new IRunnableWithProgress() {
				
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					AbstractTransactionalCommand command = 
							new AbstractTransactionalCommand(domain, "Creating StateMachine",null) {
			
								@Override
								protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info)
										throws ExecutionException {
									diagramCreationCommand.createDiagram(DiagramManager.this.modelSet, container, diagramName);
									return CommandResult.newOKCommandResult();
								}
							};
					try {
						command.execute(monitor, null);
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}, ResourcesPlugin.getWorkspace().getRoot());
			
			
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}
	
	
	/**
	 * Gets the previously created diagrams.
	 * @return The list of the the Diagrams
	 */
	public List<Diagram> getDiagrams(){
			NotationModel notationmodel = (NotationModel) this.modelSet.getModel(NotationModel.MODEL_ID);
			Resource notationResource = notationmodel.getResource();
			@SuppressWarnings("unchecked")
			List<Diagram> list = (List<Diagram>)(List<?>) notationResource.getContents();
			return list;
	}
	
	/**
	 * Gets the {@link Element} that is the container of the diagram
	 * @param diagram - The Diagram
	 * @return Container of the diagram
	 */
	public Element getDiagramContainer(Diagram diagram){
		return (Element) diagram.getElement();
	}
	
	/**
	 * Opens the tab of diagram in the editor
	 * @param diag - The diagram is to be opened
	 */
	public void openDiagram(Diagram diag){
		
		//this.registry.getActiveEditor(); //Some kind of magic, but has to be done at least once before selecting different diagrams
		/*
		try{
			IPageManager pageMngr = ServiceUtils.getInstance().getIPageManager(this.registry);
			pageMngr.selectPage(diag);
		}catch(ServiceException e){
			throw new RuntimeException(e);
		}
		*/
	}

	/**
	 * Gets the DiagramEditPart that is referenced to the editor.
	 * @return Returns the DiagramEditPart or null if the editor is not an {@link IDiagramWorkbenchPart}
	 */
	public DiagramEditPart getActiveDiagramEditPart(){
		IEditorPart ied = null; //this.registry.getActiveEditor();
		
		if(ied instanceof IDiagramWorkbenchPart){
			return ((IDiagramWorkbenchPart) ied).getDiagramEditPart();
		}else{
			return null;
		}
	}
}
