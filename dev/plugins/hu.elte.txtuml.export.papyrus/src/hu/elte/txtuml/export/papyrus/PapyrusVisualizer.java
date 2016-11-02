package hu.elte.txtuml.export.papyrus;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.papyrus.infra.core.resource.ModelMultiException;
import org.eclipse.papyrus.infra.core.services.ServiceException;

import hu.elte.txtuml.export.papyrus.utils.EditorOpener;
import hu.elte.txtuml.utils.Logger;
import hu.elte.txtuml.utils.eclipse.ProjectUtils;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @param <ViewPrototype>
 * @see IWorkbenchWindowActionDelegate
 */
public class PapyrusVisualizer implements IRunnableWithProgress {
	private String projectName;
	private String modelName;
	private String sourceUMLPath;
	private PapyrusModelManager papyrusModelManager;
	private Object layoutDescriptor;

	/**
	 * The constructor.
	 * 
	 * @param projectName
	 *            - The name of the project in which the Papyrus Model will be
	 *            created.
	 * @param modelName
	 *            - The Name of the Papyrus Model
	 * @param sourceUMLpath
	 *            - Sourcepath of the Eclipse UML2 model
	 * @param layoutDescripton
	 *            - A descriptor file which adds constraints to the layout and
	 *            diagram generation
	 */
	public PapyrusVisualizer(String projectName, String modelName, String sourceUMLpath, Object layoutDescripton) {
		this.projectName = projectName;
		this.modelName = modelName;
		this.sourceUMLPath = sourceUMLpath;
		this.layoutDescriptor = layoutDescripton;
	}

	/**
	 * @param projectName
	 * @param modelName
	 * @param sourceUMLpath
	 */
	public PapyrusVisualizer(String projectName, String modelName, String sourceUMLpath) {
		this(projectName, modelName, sourceUMLpath, null);
	}

	/**
	 * Executes the visualization process. Creates the project (if not exists)
	 * and sets up the Papyrus Model
	 * 
	 * @param monitor
	 * @return
	 * @throws ServiceException
	 * @throws ModelMultiException
	 */
	@Override
	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask("Visualization", 100);
		monitor.subTask("Creating new Papyrus project...");

		IProject project = ProjectUtils.createProject(projectName);
		ProjectUtils.openProject(project);

		monitor.worked(20);
		try {
			createAndOpenPapyrusModel(new SubProgressMonitor(monitor, 80));
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Creates the Papyrus Model and fills the diagrams. If the Model already
	 * exists, then loads it.
	 * 
	 * @throws ModelMultiException
	 *             - If the loading of existing model fails
	 * @throws ServiceException
	 */
	private void createAndOpenPapyrusModel(IProgressMonitor monitor)
			throws ModelMultiException, ServiceException, InvocationTargetException, InterruptedException {
		monitor.beginTask("Generating Papyrus Model", 100);
		PapyrusModelCreator papyrusModelCreator = new PapyrusModelCreator(projectName + "/" + modelName);
		papyrusModelCreator.setUpUML(sourceUMLPath);
		if (!papyrusModelCreator.diExists()) {

			monitor.subTask("Generating Papyrus model...");
			papyrusModelCreator.createPapyrusModel();

			papyrusModelManager = new PapyrusModelManager(papyrusModelCreator.getServiceRegistry());
			papyrusModelManager.setLayoutController(layoutDescriptor);
			monitor.worked(10);

			papyrusModelManager.createAndFillDiagrams(new SubProgressMonitor(monitor, 90));
		}

		if (papyrusModelCreator.diExists()) {
			EditorOpener.openPapyrusEditor(papyrusModelCreator.getDi());
		} else {
			Logger.user.error(".di File does not exist. Papyrus Model can't be opened.");
		}

	}
}