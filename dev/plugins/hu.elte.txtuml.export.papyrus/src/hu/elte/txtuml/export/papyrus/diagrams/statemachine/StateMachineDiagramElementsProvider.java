package hu.elte.txtuml.export.papyrus.diagrams.statemachine;

import java.util.Collection;

import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.Region;
import org.eclipse.uml2.uml.State;
import org.eclipse.uml2.uml.StateMachine;
import org.eclipse.uml2.uml.Transition;

public interface StateMachineDiagramElementsProvider {

	Collection<State> getStatesForRegion(Region region);
	
	Collection<Pseudostate> getInitialStatesForRegion(Region region);
	
	Collection<Region> getRegionsOfState(State state);

	Collection<Region> getMainRegions();

	Collection<Transition>  getTransitionsForRegion(Region region);

	StateMachine getMainElement();

	Collection<Element> getElementsOfRegion(Region region);
	
	//State , CompositeState?, PseudoState, FinalState, Choice, Merge etc.
}