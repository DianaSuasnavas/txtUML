package hu.elte.txtuml.export.papyrus;

import hu.elte.txtuml.export.papyrus.elementsarrangers.IDiagramElementsArranger;
import hu.elte.txtuml.export.papyrus.elementsarrangers.gmflayout.ActivityDiagramElementsGmfArranger;
import hu.elte.txtuml.export.papyrus.elementsarrangers.gmflayout.ClassDiagramElementsGmfArranger;
import hu.elte.txtuml.export.papyrus.elementsarrangers.gmflayout.StateMachineDiagramElementsGmfArranger;
import hu.elte.txtuml.export.papyrus.elementsmanagers.AbstractDiagramElementsManager;
import hu.elte.txtuml.export.papyrus.elementsmanagers.ActivityDiagramElementsManager;
import hu.elte.txtuml.export.papyrus.elementsmanagers.ClassDiagramElementsManager;
import hu.elte.txtuml.export.papyrus.elementsmanagers.StateMachineDiagramElementsManager;
import hu.elte.txtuml.export.papyrus.preferences.PreferencesManager;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.core.editor.IMultiDiagramEditor;
import org.eclipse.papyrus.infra.core.resource.NotFoundException;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.uml.diagram.activity.CreateActivityDiagramCommand;
import org.eclipse.papyrus.uml.diagram.clazz.CreateClassDiagramCommand;
import org.eclipse.papyrus.uml.diagram.statemachine.CreateStateMachineDiagramCommand;
import org.eclipse.ui.IEditorPart;
import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Package;
import org.eclipse.uml2.uml.StateMachine;

/**
 * Controls the Papyrus Model
 *
 * @author Andr�s Dobreff
 */
public class PapyrusModelManager {
	
	private DiagramManager diagramManager;
	private ModelManager modelManager;
	private PreferencesManager preferencesManager;
	private IEditorPart editor;

	/**
	 * The Constructor
	 * @param editor - The Editor to which the PapyrusModelManager will be attached
	 * @throws ServiceException
	 * @throws NotFoundException
	 */
	public PapyrusModelManager(IMultiDiagramEditor editor) throws ServiceException, NotFoundException {
		preferencesManager = new PreferencesManager();
		modelManager = new ModelManager(editor);
		diagramManager = new DiagramManager(editor);
		this.editor = editor;
	}

	/**
	 * Creates the diagrams and adds the elements to them
	 * @throws NotFoundException
	 * @throws ServiceException
	 */
	public void createAndFillDiagrams() throws NotFoundException, ServiceException {
		createDiagrams();	
		addElementsToDiagrams();
		editor.doSave(new NullProgressMonitor());
	}

	

	/**
	 * Creates the Papyrus Diagrams for every suitable element of the Model 
	 * @throws ServiceException
	 * @throws ExecutionException
	 * @throws NotFoundException
	 */
	private void createDiagrams() throws ServiceException{
		
		if(preferencesManager.getBoolean(PreferencesManager.CLASS_DIAGRAM_PREF)){
			List<Element> packages = modelManager.getElementsOfTypes(Arrays.asList(Model.class, Package.class));
			diagramManager.createDiagrams(packages, new CreateClassDiagramCommand());
		}
	
		if(preferencesManager.getBoolean(PreferencesManager.ACTIVITY_DIAGRAM_PREF)){
			List<Element> activities = modelManager.getElementsOfTypes(Arrays.asList(Activity.class));
			diagramManager.createDiagrams(activities, new CreateActivityDiagramCommand());
		}
		
		if(preferencesManager.getBoolean(PreferencesManager.STATEMACHINE_DIAGRAM_PREF)){
			List<Element> statemachines = modelManager.getElementsOfTypes(Arrays.asList(StateMachine.class));
			diagramManager.createDiagrams(statemachines, new CreateStateMachineDiagramCommand());
		}
	
	}

	/**
	 * Adds the elements to the diagrams
	 * @throws ServiceException
	 */
	private void addElementsToDiagrams() throws ServiceException{
		
		List<Diagram> diags =  diagramManager.getDiagrams();
		
		for(Diagram diag : diags){
			Diagram diagram = (Diagram) diag;
			
			Element container = diagramManager.getDiagramContainer(diagram);
			diagramManager.openDiagram(diagram);
			DiagramEditPart diagep = diagramManager.getActiveDiagramEditPart();
			AbstractDiagramElementsManager diagramElementsManager;
			IDiagramElementsArranger diagramElementsArranger;  
			
			List<Element> baseElements = modelManager.getAllChildrenOfPackage(container);
			
			if(diagram.getType().equals("PapyrusUMLClassDiagram")){					
				diagramElementsManager = new ClassDiagramElementsManager(modelManager, diagep);
				diagramElementsArranger = new ClassDiagramElementsGmfArranger(diagep);
			}else if(diagram.getType().equals("PapyrusUMLActivityDiagram")){
				diagramElementsManager = new ActivityDiagramElementsManager(modelManager, diagep);
				diagramElementsArranger = new ActivityDiagramElementsGmfArranger(diagep);
			}else if(diagram.getType().equals("PapyrusUMLStateMachineDiagram")){
				diagramElementsManager = new StateMachineDiagramElementsManager(modelManager, diagep);
				diagramElementsArranger = new StateMachineDiagramElementsGmfArranger(diagep);
			}else{
				continue;
			}
			
			diagramElementsManager.addElementsToDiagram(baseElements);	
			diagramElementsArranger.arrange();
		}
		
	}
	
}
