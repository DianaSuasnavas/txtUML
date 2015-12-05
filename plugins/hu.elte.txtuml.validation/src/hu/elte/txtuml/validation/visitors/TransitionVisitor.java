package hu.elte.txtuml.validation.visitors;

import java.util.Arrays;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import hu.elte.txtuml.api.model.From;
import hu.elte.txtuml.api.model.To;
import hu.elte.txtuml.api.model.Trigger;
import hu.elte.txtuml.export.uml2.utils.ElementTypeTeller;
import hu.elte.txtuml.validation.ProblemCollector;
import hu.elte.txtuml.validation.problems.transition.MissingTransitionSource;
import hu.elte.txtuml.validation.problems.transition.MissingTransitionTarget;
import hu.elte.txtuml.validation.problems.transition.MissingTransitionTrigger;
import hu.elte.txtuml.validation.problems.transition.TransitionFromOutside;
import hu.elte.txtuml.validation.problems.transition.TransitionMethodNonVoidReturn;
import hu.elte.txtuml.validation.problems.transition.TransitionMethodParameters;
import hu.elte.txtuml.validation.problems.transition.TransitionToOutside;
import hu.elte.txtuml.validation.problems.transition.TriggerOnInitialTransition;
import hu.elte.txtuml.validation.problems.transition.UnknownTransitionMethod;

public class TransitionVisitor extends VisitorBase {

	public static final Class<?>[] ALLOWED_TRANSITION_DECLARATIONS = new Class<?>[] { MethodDeclaration.class,
			SimpleName.class, SimpleType.class, Modifier.class, Annotation.class };

	private TypeDeclaration transition;
	private ITypeBinding parentElement;

	public TransitionVisitor(TypeDeclaration transition, ProblemCollector collector) {
		super(collector);
		this.transition = transition;
		parentElement = ((TypeDeclaration) transition.getParent()).resolveBinding();
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		boolean invalidName = !ElementTypeTeller.isEffect(node) && !ElementTypeTeller.isGuard(node);
		collector.setProblemStatus(invalidName, new UnknownTransitionMethod(collector.getSourceInfo(), node));
		if (invalidName) {
			return false;
		}
		if (ElementTypeTeller.isEffect(node)) {
			collector.setProblemStatus(!Utils.isVoid(node.getReturnType2()),
					new TransitionMethodNonVoidReturn(collector.getSourceInfo(), node.getReturnType2()));
		} else {
			collector.setProblemStatus(!Utils.isBoolean(node.getReturnType2()),
					new TransitionMethodNonVoidReturn(collector.getSourceInfo(), node.getReturnType2()));
		}
		collector.setProblemStatus(!node.parameters().isEmpty(),
				new TransitionMethodParameters(collector.getSourceInfo(), node));
		loadDirections();

		// TODO: validate body
		return false;
	}

	private void loadDirections() {
		ITypeBinding triggerValue = null;
		Annotation triggerAnnot = null;
		ITypeBinding fromValue = null;
		ITypeBinding toValue = null;
		for (Object mod : transition.modifiers()) {
			if (mod instanceof Annotation) {
				Annotation annot = (Annotation) mod;
				IAnnotationBinding annBinding = annot.resolveAnnotationBinding();
				ITypeBinding value = null;
				for (IMemberValuePairBinding binding : annBinding.getAllMemberValuePairs()) {
					if (binding.getKey() == null && binding.getValue() instanceof ITypeBinding) {
						value = (ITypeBinding) binding.getValue();
					}
				}
				String bindedAnnotationName = annBinding.getAnnotationType().getQualifiedName();
				if (From.class.getCanonicalName().equals(bindedAnnotationName)) {
					checkFrom(annot, value);
					fromValue = value;
				} else if (To.class.getCanonicalName().equals(bindedAnnotationName)) {
					checkTo(annot, value);
					toValue = value;
				} else if (Trigger.class.getCanonicalName().equals(bindedAnnotationName)) {
					triggerValue = value;
					triggerAnnot = annot;
				}
			}
		}
		collector.setProblemStatus(fromValue == null,
				new MissingTransitionSource(collector.getSourceInfo(), transition));
		collector.setProblemStatus(toValue == null, new MissingTransitionTarget(collector.getSourceInfo(), transition));
		checkTrigger(triggerAnnot, triggerValue, fromValue);
	}

	protected void checkFrom(Annotation from, ITypeBinding value) {
		collector.setProblemStatus(!Arrays.asList(parentElement.getDeclaredTypes()).contains(value),
				new TransitionFromOutside(collector.getSourceInfo(), from));
	}

	protected void checkTo(Annotation to, ITypeBinding value) {
		collector.setProblemStatus(!Arrays.asList(parentElement.getDeclaredTypes()).contains(value),
				new TransitionToOutside(collector.getSourceInfo(), to));
	}

	protected void checkTrigger(Annotation signal, ITypeBinding value, ITypeBinding fromValue) {
		collector.setProblemStatus(
				value == null && !ElementTypeTeller.isInitialPseudoState(fromValue)
						&& !ElementTypeTeller.isChoicePseudoState(fromValue),
				new MissingTransitionTrigger(collector.getSourceInfo(), transition));
		collector.setProblemStatus(
				value != null && (ElementTypeTeller.isInitialPseudoState(fromValue)
						|| ElementTypeTeller.isChoicePseudoState(fromValue)),
				new TriggerOnInitialTransition(collector.getSourceInfo(), signal != null ? signal : transition));
	}

}
