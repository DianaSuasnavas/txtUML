package hu.elte.txtuml.export.papyrus.papyrusmodelmanagers;

import hu.elte.txtuml.export.papyrus.DiagramManager;
import hu.elte.txtuml.export.papyrus.UMLModelManager;
import hu.elte.txtuml.export.papyrus.preferences.PreferencesManager;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.papyrus.infra.core.editor.IMultiDiagramEditor;
import org.eclipse.papyrus.uml.tools.model.UmlModel;
import org.eclipse.ui.IEditorPart;

/**
 * Controls the Papyrus Model
 *
 * @author Andr�s Dobreff
 */
public abstract class AbstractPapyrusModelManager {

	/**
	 * The DiagramManager controls the diagrams
	 */
	protected DiagramManager diagramManager;
	
	/**
	 * The ModelManager controls the model elements
	 */
	protected UMLModelManager modelManager;
	
	/**
	 * The PereferecesManager gives information what the Diagrams and Elements should be seen
	 */
	protected PreferencesManager preferencesManager;
	
	/**
	 * The Editor in which the the visualization is performed
	 */
	protected IEditorPart editor;

	/**
	 * The Constructor
	 * @param editor - The Editor to which the PapyrusModelManager will be attached
	 * @param model 
	 */
	public AbstractPapyrusModelManager(IMultiDiagramEditor editor, UmlModel model){
		this.preferencesManager = new PreferencesManager();
		this.modelManager = new UMLModelManager(model);
		this.diagramManager = new DiagramManager(editor);
		this.editor = editor;
	}

	/**
	 * Creates the diagrams and adds the elements to them
	 * @param monitor - The monitor that listens the progress
	 */
	public void createAndFillDiagrams(IProgressMonitor monitor){
		monitor.beginTask("Generating Diagrams", 100);
		createDiagrams(new SubProgressMonitor(monitor, 20));	
		addElementsToDiagrams(new SubProgressMonitor(monitor, 80));
		this.editor.doSave(new NullProgressMonitor());
	}

	/**
	 * Adds the elements to the diagrams
	 */
	protected abstract void addElementsToDiagrams(IProgressMonitor monitor);

	/**
	 * Creates the Papyrus Diagrams for every suitable element of the Model
	 */
	protected abstract void createDiagrams(IProgressMonitor monitor);
}
