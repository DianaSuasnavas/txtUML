package hu.elte.txtuml.layout.export.impl;

import java.lang.annotation.Annotation;

import hu.elte.txtuml.layout.export.DiagramExporter;
import hu.elte.txtuml.layout.export.DiagramExportationReport;
import hu.elte.txtuml.layout.export.elementinfo.NodeGroupInfo;
import hu.elte.txtuml.layout.export.interfaces.ElementExporter;
import hu.elte.txtuml.layout.export.interfaces.StatementExporter;
import hu.elte.txtuml.layout.export.problems.ErrorMessages;
import hu.elte.txtuml.layout.export.problems.WarningMessages;
import hu.elte.txtuml.layout.lang.Diagram;
import hu.elte.txtuml.layout.lang.Diagram.Layout;
import hu.elte.txtuml.layout.lang.elements.LayoutLinkGroup;
import hu.elte.txtuml.layout.lang.elements.LayoutNodeGroup;
import hu.elte.txtuml.layout.lang.elements.LayoutPhantomNode;
import hu.elte.txtuml.layout.lang.statements.*;
import hu.elte.txtuml.layout.lang.statements.containers.*;

/**
 * Default implementation for {@link DiagramExporter}.
 * 
 * @author G�bor Ferenc Kov�cs
 *
 */
public class DiagramExporterImpl implements DiagramExporter {

	private final DiagramExportationReport report;
	private final Class<? extends Diagram> diagClass;
	private final ElementExporter elementExporter;
	private final StatementExporter statementExporter;

	public DiagramExporterImpl(Class<? extends Diagram> diagClass) {
		this(diagClass, null);
	}

	public DiagramExporterImpl(Class<? extends Diagram> diagClass,
			DiagramExportationReport report) {

		if (report == null) {
			this.report = new DiagramExportationReport();
		} else {
			this.report = report;
		}

		this.diagClass = diagClass;
		this.elementExporter = ElementExporter.create();
		this.statementExporter = StatementExporter.create(elementExporter);

	}

	@Override
	public DiagramExportationReport export() {
		exportDiagram();

		if (report.isSuccessful()) {
			report.setType(elementExporter.getDiagramTypeBasedOnElements());
			report.setStatements(statementExporter.getStatements());
			report.setNodes(elementExporter.getNodesAsObjects());
			report.setLinks(elementExporter.getLinksAsLines());
		}

		return report;
	}

	@SuppressWarnings("unchecked")
	// All casts are checked with reflection.
	private void exportDiagram() {

		Class<? extends Diagram.Layout> layoutClass = null;

		for (Class<?> innerClass : diagClass.getDeclaredClasses()) {

			if (isNodeGroup(innerClass)) {
			    NodeGroupInfo info = elementExporter.exportNodeGroup((Class<? extends LayoutNodeGroup>) innerClass).asNodeGroupInfo();
			    
			    if (info == null) {
			        // TODO show error
			        continue;
			    }
			    
			    if (info.getAlignment() != null) {
			        statementExporter.exportAlignment(info);
			    }
			    
			} else if (isLinkGroup(innerClass)) {
			    elementExporter.exportLinkGroup((Class<? extends LayoutLinkGroup>) innerClass);
			    
			} else if (isPhantom(innerClass)) {
			    elementExporter.exportPhantom((Class<? extends LayoutPhantomNode>) innerClass);
			    
			} else if (isLayout(innerClass)) {

				if (layoutClass != null) {
					report.error(ErrorMessages
							.moreThanOneLayoutInnerClassOfDiagram(diagClass,
									layoutClass, innerClass));
				}
				layoutClass = (Class<? extends Layout>) innerClass;

			} else {
				report.warning(WarningMessages
						.unknownInnerClassOfDiagram(innerClass));
			}

		}

		if (layoutClass == null) {
			report.error(ErrorMessages.noLayoutInnerClassOfDiagram(diagClass));
		} else {
			exportLayout(layoutClass);
		}
	}
	
	private boolean isNodeGroup(Class<?> cls) {
	    return LayoutNodeGroup.class.isAssignableFrom(cls);
	}
	
	private boolean isLinkGroup(Class<?> cls) {
	    return LayoutLinkGroup.class.isAssignableFrom(cls);
	}

	private boolean isLayout(Class<?> cls) {
		return Layout.class.isAssignableFrom(cls);
	}
	
	private boolean isPhantom(Class<?> cls) {
	    return LayoutPhantomNode.class.isAssignableFrom(cls);
	}
	
	/*
    	@SuppressWarnings("unused") // TODO remove when used
    	private void exportGroup(Class<? extends LayoutGroup> groupClass) {
    		for (Annotation annot : groupClass.getAnnotations()) {
    			if (isOfType(Contains.class, annot)) {
    				statementExporter.exportContains(groupClass, (Contains) annot);
    				
    			} else if (isOfType(Alignment.class, annot)) {
    				statementExporter
    						.exportAlignment(groupClass, (Alignment) annot);
    				
    			} else {
    				
    				report.warning(WarningMessages.unknownStatementOnLayoutGroup(groupClass, annot));
    				
    			}
    		}
    	}
	*/

	private void exportLayout(Class<? extends Layout> layoutClass) {
		for (Annotation annot : layoutClass.getAnnotations()) {
			if (isOfType(Above.class, annot)) {
				statementExporter.exportAbove((Above) annot);				
			
			} else if (isOfType(Below.class, annot)) {
				statementExporter.exportBelow((Below) annot);
				
			} else if (isOfType(Right.class, annot)) {
				statementExporter.exportRight((Right) annot);
				
			} else if (isOfType(Left.class, annot)) {
				statementExporter.exportLeft((Left) annot);
				
			} else if (isOfType(North.class, annot)) {
				statementExporter.exportNorth((North) annot);
				
			} else if (isOfType(South.class, annot)) {
				statementExporter.exportSouth((South) annot);
		
			} else if (isOfType(East.class, annot)) {
				statementExporter.exportEast((East) annot);
		
			} else if (isOfType(West.class, annot)) {
				statementExporter.exportWest((West) annot);
		
			} else if (isOfType(TopMost.class, annot)) {
				statementExporter.exportTopMost((TopMost) annot);
		
			} else if (isOfType(BottomMost.class, annot)) {
				statementExporter.exportBottomMost((BottomMost) annot);
		
			} else if (isOfType(RightMost.class, annot)) {
				statementExporter.exportRightMost((RightMost) annot);
		
			} else if (isOfType(LeftMost.class, annot)) {
				statementExporter.exportLeftMost((LeftMost) annot);
		
			} else if (isOfType(Priority.class, annot)) {
				statementExporter.exportPriority((Priority) annot);
		
			} else if (isOfType(Show.class, annot)) {
				statementExporter.exportShow((Show) annot);
			
			} else if (isOfType(Column.class, annot)) {
				statementExporter.exportColumn((Column) annot);
			
			} else if (isOfType(Row.class, annot)) {
				statementExporter.exportRow((Row) annot);
			
			} else if (isOfType(Diamond.class, annot)) {
				statementExporter.exportDiamond((Diamond) annot);
			
			} else if (isOfType(AboveContainer.class, annot)) {
				statementExporter.exportAboveContainer((AboveContainer) annot);
		
			} else if (isOfType(BelowContainer.class, annot)) {
				statementExporter.exportBelowContainer((BelowContainer) annot);
		
			} else if (isOfType(RightContainer.class, annot)) {
				statementExporter.exportRightContainer((RightContainer) annot);
		
			} else if (isOfType(LeftContainer.class, annot)) {
				statementExporter.exportLeftContainer((LeftContainer) annot);
		
			} else if (isOfType(NorthContainer.class, annot)) {
				statementExporter.exportNorthContainer((NorthContainer) annot);
		
			} else if (isOfType(SouthContainer.class, annot)) {
				statementExporter.exportSouthContainer((SouthContainer) annot);
		
			} else if (isOfType(EastContainer.class, annot)) {
				statementExporter.exportEastContainer((EastContainer) annot);
		
			} else if (isOfType(WestContainer.class, annot)) {
				statementExporter.exportWestContainer((WestContainer) annot);
		
			} else if (isOfType(PriorityContainer.class, annot)) {
				statementExporter.exportPriorityContainer((PriorityContainer) annot);
			
			} else if (isOfType(ShowContainer.class, annot)) {
				statementExporter.exportShowContainer((ShowContainer) annot);
			
			} else if (isOfType(ColumnContainer.class, annot)) {
				statementExporter.exportColumnContainer((ColumnContainer) annot);
			
			} else if (isOfType(RowContainer.class, annot)) {
				statementExporter.exportRowContainer((RowContainer) annot);
			
			} else if (isOfType(DiamondContainer.class, annot)) {
				statementExporter.exportDiamondContainer((DiamondContainer) annot);
			
			} else {				
				report.warning(WarningMessages.unknownStatementOnLayout(layoutClass, annot));
				
			}
			
		}
		
		// exportation finalizers 
		statementExporter.resolveMosts();
		statementExporter.exportPhantoms();
		elementExporter.exportImpliedLinks();
	}

	private boolean isOfType(Class<? extends Annotation> annotationClass, Annotation annot) {
		return annot.annotationType() == annotationClass;
	}

}
