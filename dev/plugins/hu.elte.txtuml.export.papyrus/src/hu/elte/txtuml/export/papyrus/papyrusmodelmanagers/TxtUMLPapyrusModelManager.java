package hu.elte.txtuml.export.papyrus.papyrusmodelmanagers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.gmf.runtime.common.core.command.CommandResult;
import org.eclipse.gmf.runtime.diagram.ui.editparts.DiagramEditPart;
import org.eclipse.gmf.runtime.emf.commands.core.command.AbstractTransactionalCommand;
import org.eclipse.gmf.runtime.notation.Diagram;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.infra.core.utils.ServiceUtils;
import org.eclipse.papyrus.uml.diagram.clazz.CreateClassDiagramCommand;
import org.eclipse.papyrus.uml.diagram.statemachine.CreateStateMachineDiagramCommand;
import org.eclipse.uml2.uml.Element;
import org.eclipse.uml2.uml.StateMachine;

import hu.elte.txtuml.export.papyrus.elementsarrangers.ArrangeException;
import hu.elte.txtuml.export.papyrus.elementsarrangers.IDiagramElementsArranger;
import hu.elte.txtuml.export.papyrus.elementsarrangers.gmflayout.StateMachineDiagramElementsGmfArranger;
import hu.elte.txtuml.export.papyrus.elementsarrangers.txtumllayout.ClassDiagramElementsTxtUmlArranger;
import hu.elte.txtuml.export.papyrus.elementsmanagers.AbstractDiagramElementsManager;
import hu.elte.txtuml.export.papyrus.elementsmanagers.ClassDiagramElementsManager;
import hu.elte.txtuml.export.papyrus.elementsmanagers.StateMachineDiagramElementsManager;
import hu.elte.txtuml.export.papyrus.layout.txtuml.TxtUMLElementsRegistry;
import hu.elte.txtuml.export.papyrus.layout.txtuml.TxtUMLLayoutDescriptor;
import hu.elte.txtuml.export.papyrus.preferences.PreferencesManager;
import hu.elte.txtuml.utils.Logger;
import hu.elte.txtuml.utils.Pair;

public class TxtUMLPapyrusModelManager extends AbstractPapyrusModelManager {

	private TxtUMLElementsRegistry txtumlregistry;

	public TxtUMLPapyrusModelManager(ServicesRegistry registry) {
		super(registry);
	}

	@Override
	public void setLayoutController(Object layoutcontroller) {
		TxtUMLLayoutDescriptor descriptor = (TxtUMLLayoutDescriptor) layoutcontroller;
		txtumlregistry = new TxtUMLElementsRegistry(model.getResource(), descriptor);
	}

	@Override
	protected void createDiagrams(IProgressMonitor monitor) {
		monitor.beginTask("Generating empty diagrams", 100);
		monitor.subTask("Creating empty diagrams...");

		if (PreferencesManager.getBoolean(PreferencesManager.CLASS_DIAGRAM_PREF)) {
			List<Pair<String, Element>> classDiagramRoots = txtumlregistry.getDiagramRootsWithDiagramNames();
			CreateClassDiagramCommand cmd = new CreateClassDiagramCommand();
			for (Pair<String, Element> classDiagramRoot : classDiagramRoots) {
				diagramManager.createDiagram(classDiagramRoot.getSecond(), classDiagramRoot.getFirst(), cmd);
			}
		}

		List<Element> statemachines = modelManager.getElementsOfTypes(Arrays.asList(StateMachine.class));
		try {
			TransactionalEditingDomain domain = ServiceUtils.getInstance().getTransactionalEditingDomain(this.registry);
			AbstractTransactionalCommand command = new AbstractTransactionalCommand(domain, "Creating StateMachine",
					statemachines) {

				@Override
				protected CommandResult doExecuteWithResult(IProgressMonitor monitor, IAdaptable info)
						throws ExecutionException {
					diagramManager.createDiagrams(statemachines, new CreateStateMachineDiagramCommand());
					return CommandResult.newOKCommandResult();
				}
			};
			command.execute(monitor, null);
		} catch (ServiceException e) {
			Logger.executor.error("Could not get TransationalEditingDomain", e);
		} catch (ExecutionException e) {
			Logger.executor.error("Could not execute StateMachine Creation Command", e);
		}
		monitor.worked(100);
	}

	@Override
	protected void addElementsToDiagram(Diagram diagram, IProgressMonitor monitor) {
		AbstractDiagramElementsManager diagramElementsManager;

		DiagramEditPart diagep = diagramManager.getActiveDiagramEditPart();
		if (diagram.getType().equals(diagramType_CD)) {
			diagramElementsManager = new ClassDiagramElementsManager(diagep);
		} else if (diagram.getType().equals(diagramType_SMD)) {
			diagramElementsManager = new StateMachineDiagramElementsManager(diagep);
		} else {
			return;
		}

		List<Element> baseElements = new ArrayList<Element>();
		List<Element> nodes = txtumlregistry.getNodes(diagram.getName());
		List<Element> connections = txtumlregistry.getConnections(diagram.getName());
		baseElements.addAll(nodes);
		baseElements.addAll(connections);

		diagramElementsManager.addElementsToDiagram(baseElements);
	}

	@Override
	protected void arrangeElementsOfDiagram(Diagram diagram, IProgressMonitor monitor) throws ArrangeException {
		IDiagramElementsArranger diagramElementsArranger;
		DiagramEditPart diagep = diagramManager.getActiveDiagramEditPart();
		if (diagram.getType().equals(diagramType_CD)) {
			diagramElementsArranger = new ClassDiagramElementsTxtUmlArranger(diagep, txtumlregistry);
		} else if (diagram.getType().equals(diagramType_SMD)) {
			diagramElementsArranger = new StateMachineDiagramElementsGmfArranger(diagep);
		} else {
			return;
		}
		diagramElementsArranger.arrange(monitor);
	}
}
