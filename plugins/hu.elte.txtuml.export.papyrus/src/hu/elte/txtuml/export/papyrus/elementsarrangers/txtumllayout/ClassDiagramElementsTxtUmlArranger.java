package hu.elte.txtuml.export.papyrus.elementsarrangers.txtumllayout;

import hu.elte.txtuml.export.papyrus.TxtUMLElementsFinder;
import hu.elte.txtuml.export.utils.Dialogs;
import hu.elte.txtuml.layout.visualizer.exceptions.CannotFindAssociationRouteException;
import hu.elte.txtuml.layout.visualizer.exceptions.ConflictException;
import hu.elte.txtuml.layout.visualizer.exceptions.ConversionException;
import hu.elte.txtuml.layout.visualizer.exceptions.InternalException;
import hu.elte.txtuml.layout.visualizer.exceptions.StatementTypeMatchException;
import hu.elte.txtuml.layout.visualizer.exceptions.UnknownStatementException;

import java.util.Arrays;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.AssociationMultiplicitySourceEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.AssociationMultiplicityTargetEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.AssociationNameEditPart;

/**
 * Controls the arranging of a ClassDiagram with txtUML algorithm
 *
 * @author Andr�s Dobreff
 */
public class ClassDiagramElementsTxtUmlArranger extends AbstractDiagramElementsTxtUmlArranger{
	
	/**
	 * Arranges the children of an {@link EditPart} with the txtUML arranging algorithm 
	 * @param diagramEditPart - The children of this EditPart will be arranged
	 * @param finder - The {@link TxtUMLElementsFinder} which specifies the layout
	 */
	public ClassDiagramElementsTxtUmlArranger(DiagramEditPart diagramEditPart, TxtUMLElementsFinder finder) {
		super(diagramEditPart, finder);
	}

	/*
	 * (non-Javadoc)
	 * @see hu.elte.txtuml.export.papyrus.elementsarrangers.IDiagramElementsArranger#arrange()
	 */
	@Override
	public void arrange() {
		@SuppressWarnings("unchecked")
		List<EditPart> listEp = diagep.getChildren();
		try {
			super.arrangeChildren(diagep, listEp);
		} catch (InternalException | ConflictException | ConversionException
				| StatementTypeMatchException
				| CannotFindAssociationRouteException
				| UnknownStatementException e) {
			
			Dialogs.errorMsgb("txtUML Layout Error", e.toString(), e);
		}
		super.hideConnectionLabelsForEditParts(listEp, Arrays.asList(
				AssociationNameEditPart.class,
				AssociationMultiplicityTargetEditPart.class,
				AssociationMultiplicitySourceEditPart.class
				));
	}
	
}
