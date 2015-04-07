package hu.elte.txtuml.export.papyrus.elementsarrangers.gmflayout;

import java.util.Arrays;
import java.util.List;

import org.eclipse.gef.EditPart;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.AssociationMultiplicitySourceEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.AssociationMultiplicityTargetEditPart;
import org.eclipse.papyrus.uml.diagram.clazz.edit.parts.AssociationNameEditPart;

/**
 * Controls the arranging of a ClassDiagram with GMF algorithm
 *
 * @author Andr�s Dobreff
 */
public class ClassDiagramElementsGmfArranger extends AbstractDiagramElementsGmfArranger{

	/**
	 * The Constructor 
	 * @param diagramEditPart - The EditPart of the diagram which elements is to arranged.
	 */
	public ClassDiagramElementsGmfArranger(DiagramEditPart diagramEditPart) {
		super(diagramEditPart);
	}

	/*
	 * (non-Javadoc)
	 * @see hu.elte.txtuml.export.papyrus.elementsarrangers.IDiagramElementsArranger#arrange()
	 */
	@Override
	public void arrange() {
		super.arrangeChildren(diagep);
		@SuppressWarnings("unchecked")
		List<EditPart> listEp = diagep.getChildren();
		super.hideConnectionLabelsForEditParts(listEp, Arrays.asList(
				AssociationNameEditPart.class,
				AssociationMultiplicityTargetEditPart.class,
				AssociationMultiplicitySourceEditPart.class
				));
	}
	
}
