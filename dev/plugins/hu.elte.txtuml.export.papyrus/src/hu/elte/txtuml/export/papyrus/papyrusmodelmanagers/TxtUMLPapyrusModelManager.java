package hu.elte.txtuml.export.papyrus.papyrusmodelmanagers;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.uml.diagram.clazz.CreateClassDiagramCommand;
import org.eclipse.papyrus.uml.diagram.statemachine.CreateStateMachineDiagramCommand;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.StateMachine;

import hu.elte.txtuml.export.papyrus.elementproviders.ClassDiagramElementsProvider;
import hu.elte.txtuml.export.papyrus.elementproviders.TxtUMLClassDiagramElementsProvider;
import hu.elte.txtuml.export.papyrus.elementsarrangers.ArrangeException;
import hu.elte.txtuml.export.papyrus.elementsarrangers.ClassDiagramElementsArranger;
import hu.elte.txtuml.export.papyrus.elementsarrangers.IDiagramElementsArranger;
import hu.elte.txtuml.export.papyrus.elementsmanagers.AbstractDiagramElementsManager;
import hu.elte.txtuml.export.papyrus.elementsmanagers.ClassDiagramElementsManager;
import hu.elte.txtuml.export.papyrus.elementsmanagers.StateMachineDiagramElementsManager;
import hu.elte.txtuml.export.papyrus.layout.txtuml.TxtUMLElementsMapper;
import hu.elte.txtuml.export.papyrus.layout.txtuml.TxtUMLLayoutDescriptor;
import hu.elte.txtuml.export.papyrus.preferences.PreferencesManager;
import hu.elte.txtuml.layout.export.DiagramExportationReport;
import hu.elte.txtuml.utils.Pair;

public class TxtUMLPapyrusModelManager extends AbstractPapyrusModelManager {

	private TxtUMLElementsMapper mapper;
	private TxtUMLLayoutDescriptor descriptor;

	public TxtUMLPapyrusModelManager(ServicesRegistry registry) {
		super(registry);
	}

	@Override
	public void setLayoutController(Object layoutcontroller) {
		TxtUMLLayoutDescriptor descriptor = (TxtUMLLayoutDescriptor) layoutcontroller;
		this.descriptor = descriptor;
		mapper = new TxtUMLElementsMapper(this.model.getResource(), descriptor);
	}

	@Override
	protected void createDiagrams(IProgressMonitor monitor) {
		monitor.beginTask("Generating empty diagrams", 100);
		monitor.subTask("Creating empty diagrams...");

		if (PreferencesManager.getBoolean(PreferencesManager.CLASS_DIAGRAM_PREF)) {
			List<Pair<String, Element>> classDiagramRoots = mapper.getDiagramRootsWithDiagramNames(this.descriptor);
			
			CreateClassDiagramCommand cmd = new CreateClassDiagramCommand();
			for (Pair<String, Element> classDiagramRoot : classDiagramRoots) {
				diagramManager.createDiagram(classDiagramRoot.getSecond(), classDiagramRoot.getFirst(), cmd,
						this.domain);
			}
		}

		List<Element> statemachines = modelManager.getElementsOfTypes(Arrays.asList(StateMachine.class));

		diagramManager.createDiagrams(statemachines, new CreateStateMachineDiagramCommand(), this.domain);

		monitor.worked(100);
	}

	@Override
	protected void addElementsToDiagram(Diagram diagram, IProgressMonitor monitor) {
		AbstractDiagramElementsManager diagramElementsManager;

		DiagramExportationReport report = this.descriptor.getReport(diagram.getName());
		
		if (diagram.getType().equals(diagramType_CD)) {
			ClassDiagramElementsProvider provider = new TxtUMLClassDiagramElementsProvider(report, this.mapper);
			ClassDiagramElementsArranger arranger = new ClassDiagramElementsArranger(report, this.mapper);
			diagramElementsManager = new ClassDiagramElementsManager(diagram,
					provider, this.domain, arranger, monitor);
		} else if (diagram.getType().equals(diagramType_SMD)) {
			diagramElementsManager = new StateMachineDiagramElementsManager(diagram, this.domain, monitor);
		} else {
			return;
		}

		diagramElementsManager.addElementsToDiagram();
	}

	@Override
	protected void arrangeElementsOfDiagram(Diagram diagram, IProgressMonitor monitor) throws ArrangeException {
		IDiagramElementsArranger diagramElementsArranger;
		DiagramEditPart diagep = diagramManager.getActiveDiagramEditPart();
		DiagramExportationReport report =  this.descriptor.getReport(diagram.getName());
		if (diagram.getType().equals(diagramType_CD)) {
			diagramElementsArranger = null;
		} else if (diagram.getType().equals(diagramType_SMD)) {
			diagramElementsArranger = null;
		} else {
			return;
		}
		diagramElementsArranger.arrange(monitor);
	}
}
