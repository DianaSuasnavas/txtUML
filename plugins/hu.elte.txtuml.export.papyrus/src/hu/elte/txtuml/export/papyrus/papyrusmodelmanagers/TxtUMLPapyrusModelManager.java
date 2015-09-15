package hu.elte.txtuml.export.papyrus.papyrusmodelmanagers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import hu.elte.txtuml.export.papyrus.elementsarrangers.ArrangeException;
import hu.elte.txtuml.export.papyrus.elementsarrangers.IDiagramElementsArranger;
import hu.elte.txtuml.export.papyrus.elementsarrangers.gmflayout.StateMachineDiagramElementsGmfArranger;
import hu.elte.txtuml.export.papyrus.elementsarrangers.txtumllayout.ClassDiagramElementsTxtUmlArranger;
import hu.elte.txtuml.export.papyrus.elementsmanagers.AbstractDiagramElementsManager;
import hu.elte.txtuml.export.papyrus.elementsmanagers.ClassDiagramElementsManager;
import hu.elte.txtuml.export.papyrus.elementsmanagers.StateMachineDiagramElementsManager;
import hu.elte.txtuml.export.papyrus.layout.txtuml.TxtUMLElementsRegistry;
import hu.elte.txtuml.export.papyrus.layout.txtuml.TxtUMLLayoutDescriptor;
import hu.elte.txtuml.export.papyrus.preferences.PreferencesManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.core.editor.IMultiDiagramEditor;
import org.eclipse.papyrus.uml.diagram.clazz.CreateClassDiagramCommand;
import org.eclipse.papyrus.uml.diagram.statemachine.CreateStateMachineDiagramCommand;
import org.eclipse.papyrus.uml.tools.model.UmlModel;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.StateMachine;

/**
 * 
 *
 * @author Andr�s Dobreff
 */
public class TxtUMLPapyrusModelManager extends AbstractPapyrusModelManager {

	private TxtUMLElementsRegistry txtumlregistry;

	/**
	 * The Constructor
	 * @param editor - The Editor to which the PapyrusModelManager will be attached
	 * @param model - The Uml Model manager
	 */
	public TxtUMLPapyrusModelManager(IMultiDiagramEditor editor, UmlModel model) {
		super(editor, model);
	}

	@Override
	protected void arrangeElementsOfDiagram(Diagram diagram, IProgressMonitor monitor) throws ArrangeException {
		IDiagramElementsArranger diagramElementsArranger;
		DiagramEditPart diagep = diagramManager.getActiveDiagramEditPart();
		if(diagram.getType().equals(diagramType_CD)){                                 
			diagramElementsArranger = new ClassDiagramElementsTxtUmlArranger(diagep, txtumlregistry);
		}else if(diagram.getType().equals(diagramType_SMD)){                                 
			diagramElementsArranger = new StateMachineDiagramElementsGmfArranger(diagep);
		}else{
			return;
		}
		diagramElementsArranger.arrange(monitor);
	}

	@Override
	protected void addElementsToDiagram(Diagram diagram, IProgressMonitor monitor) {
		AbstractDiagramElementsManager diagramElementsManager;

		DiagramEditPart diagep = diagramManager.getActiveDiagramEditPart();
		if(diagram.getType().equals(diagramType_CD)){                                 
			diagramElementsManager = new ClassDiagramElementsManager(modelManager, diagep);
		}else if(diagram.getType().equals(diagramType_SMD)){                                 
			diagramElementsManager = new StateMachineDiagramElementsManager(modelManager, diagep);
		}else{
			return;
		}
		
		List<Element> baseElements = new ArrayList<Element>();
		baseElements.addAll(txtumlregistry.getNodes());
		baseElements.addAll(txtumlregistry.getConnections());

		diagramElementsManager.addElementsToDiagram(baseElements);
	}

	@Override
	protected void createDiagrams(IProgressMonitor monitor) {
		 monitor.beginTask("Generating empty diagrams", 100);
		 monitor.subTask("Creating empty diagrams...");
		 
		 if(PreferencesManager.getBoolean(PreferencesManager.CLASS_DIAGRAM_PREF)){
			 List<Element> packages = modelManager.getElementsOfTypes(Arrays.asList(Model.class));
		 	diagramManager.createDiagrams(packages, new CreateClassDiagramCommand());
		 }
		 
		 if(txtumlregistry.getDescriptor().generateSMDs){
			 List<Element> statemachines = modelManager.getElementsOfTypes(Arrays.asList(StateMachine.class));
			 diagramManager.createDiagrams(statemachines, new CreateStateMachineDiagramCommand());
		 }
		 monitor.worked(100);
	}

	@Override
	public void setLayoutController(Object layoutcontroller) {
		TxtUMLLayoutDescriptor descriptor  = (TxtUMLLayoutDescriptor) layoutcontroller;
		txtumlregistry = new TxtUMLElementsRegistry(modelManager, descriptor);
	}

}
