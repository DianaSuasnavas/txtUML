package hu.elte.txtuml.export.uml2.utils;

import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.Region;

public final class StateMachineUtils {

	/**
	 * Decides if the given region contains an initial pseudostate.
	 * 
	 * @param region
	 *            The region.
	 * @return The decision.
	 *
	 * @author Adam Ancsin
	 */
	public static boolean containsInitial(Region region) {
		for(Object vert: region.getSubvertices().toArray())	{
			if(	vert instanceof Pseudostate && 
				((Pseudostate) vert).getKind()==PseudostateKind.INITIAL_LITERAL
			)
			return true;
		}
		return false;
	}

}
