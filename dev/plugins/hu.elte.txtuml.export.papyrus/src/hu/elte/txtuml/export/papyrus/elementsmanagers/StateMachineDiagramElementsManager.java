package hu.elte.txtuml.export.papyrus.elementsmanagers;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.Vertex;

import hu.elte.txtuml.export.papyrus.elementproviders.StateMachineDiagramElementsProvider;
import hu.elte.txtuml.export.papyrus.elementsarrangers.StateMachineDiagramElementsArranger;
import hu.elte.txtuml.export.papyrus.notationmanagers.StateMachineDiagramNotationManager;

/**
 * An abstract class for adding/removing elements to StateMachineDiagrams.
 */
public class StateMachineDiagramElementsManager extends AbstractDiagramElementsManager {

	protected StateMachineDiagramNotationManager notationManager;
	protected StateMachineDiagramElementsProvider elementsProvider;

	/**
	 * The Constructor
	 * 
	 * @param modelManager
	 *            - The ModelManager which serves the model elements
	 * @param diagramEditPart
	 *            - The DiagramEditPart of the diagram which is to be handled
	 */
	public StateMachineDiagramElementsManager(Diagram diagram, StateMachineDiagramElementsProvider provider,
			StateMachineDiagramNotationManager notation, StateMachineDiagramElementsArranger arranger,
			IProgressMonitor monitor) {
		super(diagram, monitor);
		this.notationManager = notation;
		this.arranger = arranger;
		this.elementsProvider = provider;

		arrangeWithErrorHandling();
	}

	public StateMachineDiagramElementsManager(Diagram diagram, StateMachineDiagramElementsProvider provider,
			StateMachineDiagramNotationManager notation, StateMachineDiagramElementsArranger arranger) {
		this(diagram, provider, notation, arranger, new NullProgressMonitor());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see hu.elte.txtuml.export.papyrus.elementsmanagers.
	 * AbstractDiagramElementsManager#addElementsToDiagram(java.util.List)
	 */
	@Override
	public void addElementsToDiagram() {
		Collection<Region> regions = this.elementsProvider.getMainRegions();
		addSubelementsRecursively(regions);
	}

	private void addSubelementsRecursively(Collection<Region> regions) {
		regions.stream().forEach(region -> {
			this.elementsProvider.getStatesForRegion(region).forEach(state -> {
				this.notationManager.createStateForRegion(region, state, this.arranger.getBoundsForElement(state),
						this.monitor);
				if (!state.isSimple()) {
					state.getRegions().forEach(reg -> {
						this.notationManager.createRegionForState(state, reg, this.monitor);
					});
					addSubelementsRecursively(this.elementsProvider.getRegionsOfState(state));
				}
			});
			this.elementsProvider.getInitialStatesForRegion(region).forEach(initialState -> {
				this.notationManager.createInitialStateForRegion(region, initialState,
						this.arranger.getBoundsForElement(initialState), monitor);
			});
			this.elementsProvider.getTransitionsForRegion(region).forEach(transition -> {

				Vertex source = transition.getSource();
				Vertex target = transition.getTarget();

				this.notationManager.createTransitionForRegion(region, source, target, transition,
						this.arranger.getRouteForConnection(transition),
						this.arranger.getSourceAnchorForConnection(transition),
						this.arranger.getTargetAnchorForConnection(transition), this.monitor);
			});
		});
	}

}
