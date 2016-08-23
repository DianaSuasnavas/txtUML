package hu.elte.txtuml.export.cpp.structural;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.uml2.uml.Activity;
import org.eclipse.uml2.uml.Behavior;
import org.eclipse.uml2.uml.Pseudostate;
import org.eclipse.uml2.uml.PseudostateKind;
import org.eclipse.uml2.uml.Transition;
import org.eclipse.uml2.uml.UMLPackage;
import org.eclipse.uml2.uml.Vertex;

import hu.elte.txtuml.export.cpp.activity.ActivityExporter;
import hu.elte.txtuml.export.cpp.templates.ActivityTemplates;
import hu.elte.txtuml.export.cpp.templates.GenerationTemplates;
import hu.elte.txtuml.utils.Pair;

public class TransitionExporter {
	private ActivityExporter activityExporter;
	private GuardExporter guardExporter;
	
	String className;
	List<Transition> transitions;
	
	TransitionExporter(String className,List<Transition> transitions) {
		this.className = className;
		this.transitions = transitions;
	}
	
	String createTransitionFunctionDecl() {
		StringBuilder source = new StringBuilder("");
		for (Transition item : transitions) {
			source.append(GenerationTemplates.transitionActionDecl(item.getName()));
		}
		source.append("\n");
		return source.toString();
	}

	String createTransitionFunctionsDef() {
		StringBuilder source = new StringBuilder("");
		for (Transition item : transitions) {
			String body = "";
			activityExporter.init();
			Behavior b = item.getEffect();
			if (b != null && b.eClass().equals(UMLPackage.Literals.ACTIVITY)) {
				Activity a = (Activity) b;
				body = activityExporter.createfunctionBody(a).toString();
			}

			Pair<String, Boolean> setState = createSetState(item);
			source.append(GenerationTemplates.transitionActionDef(className, item.getName(),
					body + setState.getFirst() + "\n",
					activityExporter.isContainsSignalAcces() || setState.getSecond()));
		}
		source.append("\n");
		return source.toString();
	}
	
	private Pair<String, Boolean> createSetState(Transition transition) {
		String source = "";
		boolean containsChoice = false;
		Vertex targetState = transition.getTarget();

		// choice handling
		if (targetState.eClass().equals(UMLPackage.Literals.PSEUDOSTATE)
				&& ((Pseudostate) targetState).getKind().equals(PseudostateKind.CHOICE_LITERAL)) {
			List<Pair<String, String>> branches = new LinkedList<Pair<String, String>>();
			Pair<String, String> elseBranch = null;
			containsChoice = true;
			for (Transition trans : targetState.getOutgoings()) {

				String guard = guardExporter.getGuard(trans.getGuard()) + "(" + GenerationTemplates.eventParamName()
						+ ")";
				String body = ActivityTemplates.blockStatement(ActivityTemplates.transitionActionCall(trans.getName()))
						.toString();

				if (guard.isEmpty() || guard.equals("else")) {
					elseBranch = new Pair<String, String>(guard, body);
				} else {
					branches.add(new Pair<String, String>(guard, body));
				}
			}
			if (elseBranch != null) {
				branches.add(elseBranch);
			}
			source = ActivityTemplates.elseIf(branches).toString();
		} else if (targetState.eClass().equals(UMLPackage.Literals.STATE)) {
			source = GenerationTemplates.setState(targetState.getName());

		} else {
			source = GenerationTemplates.setState("UNKNOWN_TRANSITION_TARGET");
		}
		return new Pair<String, Boolean>(source, containsChoice);
	}
}
