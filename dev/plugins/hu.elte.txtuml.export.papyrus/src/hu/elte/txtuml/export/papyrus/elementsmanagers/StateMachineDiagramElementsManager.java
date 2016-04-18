package hu.elte.txtuml.export.papyrus.elementsmanagers;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.gmf.runtime.notation.View;
import org.eclipse.papyrus.uml.diagram.statemachine.edit.parts.RegionEditPart;
import org.eclipse.papyrus.uml.diagram.statemachine.edit.parts.StateEditPart;
import org.eclipse.uml2.uml.Comment;
import org.eclipse.uml2.uml.Constraint;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.FinalState;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.Transition;

import hu.elte.txtuml.export.papyrus.UMLModelManager;
import hu.elte.txtuml.export.papyrus.api.StateMachineDiagramElementsController;
import hu.elte.txtuml.export.papyrus.api.elementcreators.ClassDiagramNotationManager;
import hu.elte.txtuml.export.papyrus.api.elementcreators.StateMachineDiagramNotationManager;
import hu.elte.txtuml.export.papyrus.elementproviders.ClassDiagramElementsProvider;
import hu.elte.txtuml.export.papyrus.elementproviders.StateMachineDiagramElementsProvider;
import hu.elte.txtuml.export.papyrus.elementsarrangers.ArrangeException;
import hu.elte.txtuml.export.papyrus.elementsarrangers.ClassDiagramElementsArranger;
import hu.elte.txtuml.export.papyrus.elementsarrangers.StateMachineDiagramElementsArranger;
import hu.elte.txtuml.export.papyrus.preferences.PreferencesManager;

/**
 * An abstract class for adding/removing elements to StateMachineDiagrams.
 */
public class StateMachineDiagramElementsManager extends AbstractDiagramElementsManager {

	
	protected StateMachineDiagramNotationManager notationManager;
	protected StateMachineDiagramElementsProvider elementsProvider;
	protected StateMachineDiagramElementsArranger arranger;
	
	/**
	 * The Constructor
	 * @param modelManager - The ModelManager which serves the model elements
	 * @param diagramEditPart - The DiagramEditPart of the diagram which is to be handled
	 */
	public StateMachineDiagramElementsManager(Diagram diagram, StateMachineDiagramElementsProvider provider,
			TransactionalEditingDomain domain, StateMachineDiagramElementsArranger arranger, IProgressMonitor monitor) {
		super(diagram);
		this.notationManager = new StateMachineDiagramNotationManager(domain); // TODO: Consider DI
		this.arranger = arranger;
		
		try {
			this.arranger.arrange(monitor);
		} catch (ArrangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.elementsProvider = provider;
		this.monitor = monitor;
	}
	
	public StateMachineDiagramElementsManager(Diagram diagram, StateMachineDiagramElementsProvider provider,
			TransactionalEditingDomain domain, StateMachineDiagramElementsArranger arranger) {
		this(diagram, provider, domain, arranger, new NullProgressMonitor());
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see hu.elte.txtuml.export.papyrus.elementsmanagers.AbstractDiagramElementsManager#addElementsToDiagram(java.util.List)
	 */
	@Override
	public void addElementsToDiagram(){
		
		//TODO: Get the main region uml2 element and notation node 
		this.elementsProvider.getStatesForRegion(null).forEach(state -> this.notationManager.createStateForRegion(null, state, this.monitor));
		
		//TODO: Probably a recursive algorithm is needed to build up state hierarchy
	}
	
	/**
	 * Adds the subElements to an EditPart. Then calls the {@link #fillState(EditPart)}
	 * for every state. 
	 * @param region - The EditPart
	 */
	private void addSubElements(RegionEditPart region){
		/*
		EObject parent = ((View) region.getModel()).getElement();
		List<Element> list = ((Element) parent).getOwnedElements();
		
		List<State> states = UMLModelManager.getElementsOfTypeFromList(list, State.class);
		List<Pseudostate> pseudostates = UMLModelManager.getElementsOfTypeFromList(list, Pseudostate.class);
		List<FinalState> finalstates = UMLModelManager.getElementsOfTypeFromList(list, FinalState.class);
		List<Transition> transitions = UMLModelManager.getElementsOfTypeFromList(list, Transition.class);
	
		StateMachineDiagramElementsController.addPseudostatesToRegion(region, pseudostates);
		StateMachineDiagramElementsController.addStatesToRegion(region, states);
		StateMachineDiagramElementsController.addFinalStatesToRegion(region, finalstates);
		StateMachineDiagramElementsController.addTransitionsToRegion(region, transitions);
*/
	}
}
