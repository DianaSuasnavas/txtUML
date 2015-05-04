package hu.elte.txtuml.export.papyrus;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.CommonPlugin;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.impl.BasicEObjectImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.transaction.RecordingCommand;
import org.eclipse.papyrus.infra.core.resource.ModelMultiException;
import org.eclipse.papyrus.infra.core.resource.ModelSet;
import org.eclipse.papyrus.infra.core.sasheditor.contentprovider.IPageManager;
import org.eclipse.papyrus.infra.core.services.ExtensionServicesRegistry;
import org.eclipse.papyrus.infra.core.services.ServiceException;
import org.eclipse.papyrus.infra.core.services.ServicesRegistry;
import org.eclipse.papyrus.uml.diagram.wizards.category.NewPapyrusModelCommand;
import org.eclipse.uml2.uml.Model;
import org.eclipse.uml2.uml.Profile;
import org.eclipse.uml2.uml.UMLPackage;

/**
 * Controls the creation of a Papyrus Model
 *
 * @author Andr�s Dobreff
 */
public class PapyrusModelCreator {
	private IFile diFile;
	private IFile umlFile;
	private String diFilePath;
	private String umlFilePath;
	private ServicesRegistry registry;

	/**
	 * Checks if the .di file exists
	 * @return Returns true if the .di exists
	 */
	public boolean diExists(){
		return diFile.exists();
	}
	
	/**
	 * Returns the .di File
	 * @return Returns the .di File 
	 */
	public IFile getDi(){
		return diFile;
	}

	/**
	 * Initializes the instance	
	 * @param modelpath - The path of the Papyrus model (at the end is the name of the Papyrus model)
	 * @param sourceUMLPath - The path of the source .uml File
	 * @throws CoreException
	 * @throws IOException 
	 */
	public void init(String modelpath) throws CoreException {
		diFilePath = modelpath+".di";
		umlFilePath = modelpath+".uml";
		
		diFile = fileFromPath(diFilePath);
		umlFile = fileFromPath(umlFilePath);
	}
	
	public void setUpUML( String sourceUMLPath) throws CoreException, IOException{
		if(!umlFile.exists()){
			setUpUMLFile(sourceUMLPath);
		}
	}
	/**
	 * Copies the content of the sourceUMLPath to the umlFile
	 * The referenced Profile files will be also copied
	 * @param sourceUMLPath - The path of the source .uml File
	 * @throws CoreException
	 * @throws IOException 
	 */
	private void setUpUMLFile(String sourceUMLPath) throws CoreException, IOException {
			copyFile(sourceUMLPath, umlFile);
			
			Model m = loadModel(URI.createPlatformResourceURI(umlFilePath, false));
			EList<Profile> profiles = m.getAllAppliedProfiles();
			for(Profile profile : profiles){
						String filestring = ((BasicEObjectImpl) profile).eProxyURI().toPlatformString(false);
						Path profileFilepath = new Path(filestring);
						
						String oldFolder = new Path(new Path(sourceUMLPath).toFile().getParent().toString()).toString();
						String oldFileName = oldFolder+Path.SEPARATOR+profileFilepath.toFile().getName();
						
						int index = umlFilePath.lastIndexOf(Path.SEPARATOR);
						String newFileName = umlFilePath.substring(0, index)+Path.SEPARATOR+profileFilepath.toFile().getName();
						IFile newFile = fileFromPath(newFileName);
						
						copyFile(oldFileName, newFile);	
			}
	}
	
	private Model loadModel(URI uri) {
		ResourceSetImpl RESOURCE_SET = new ResourceSetImpl();
		Resource resource = RESOURCE_SET.getResource(uri, true);

		Model model = (Model) EcoreUtil.getObjectByType(resource.getContents(), UMLPackage.Literals.MODEL);
		return model;
	}
	
	private void copyFile(String sourcepath, IFile newFile) throws FileNotFoundException, CoreException{
		File oldFile = new File(java.net.URI.create(sourcepath));
		FileInputStream is2 = new FileInputStream(oldFile);
		newFile.create(null, true, null);
		newFile.setContents(is2, true, false, new NullProgressMonitor());
	}

	
	/**
	 * Creates an empty Papyrus Model
	 * @throws ServiceException
	 * @throws IOException
	 */
	public void createPapyrusModel() throws ServiceException, IOException{
		ModelSet modelSet = getModelSet();
		URI diPResURI =  URI.createPlatformResourceURI(diFilePath, true);
		RecordingCommand command = new NewPapyrusModelCommand(modelSet, diPResURI);
		modelSet.getTransactionalEditingDomain().getCommandStack().execute(command);
		initRegistry(registry);
		modelSet.save(new NullProgressMonitor());
	}
	
	/**
	 * Loads a Papyrus Model 
	 * @throws ServiceException
	 * @throws ModelMultiException
	 */
	public void loadPapyrusModel() throws ServiceException, ModelMultiException{
		ModelSet modelSet = getModelSet();
		URI diPResURI = URI.createPlatformResourceURI(umlFilePath, true);
		modelSet.loadModels(diPResURI);
	}
	
	/**
	 * Gets the ModelSet
	 * @return The ModelSet
	 * @throws ServiceException
	 */
	private ModelSet getModelSet() throws ServiceException{
		registry = createServicesRegistry();
		ModelSet modelSet = registry.getService(ModelSet.class);
		return modelSet;
	}
	
	/**
	 * Initializes a registry
	 * @param registry - The ServiceRegistry to be Initialized 
	 * @throws ServiceException
	 */
	private void initRegistry(ServicesRegistry registry) throws ServiceException{
		try {
			registry.startRegistry();
		} catch (ServiceException ex) {
			// Ignore this exception: some services may not have been loaded,
			// which is probably normal at this point
		}
		registry.getService(IPageManager.class);
	}

	/**
	 * Gets an IFile from the given String path
	 * @param path - The path of the File
	 * @return - The File
	 */
	private IFile fileFromPath(String path){
		URI FileURI =  URI.createFileURI(path);
		URI resolvedFile = CommonPlugin.resolve(FileURI);
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(resolvedFile.toFileString()));
		return file;
	}
	
	/**
	 * Creates a ServicesRegistry with ModelSet class key.
	 * @return The ServicesRegistry
	 * @throws ServiceException
	 */
	private ServicesRegistry createServicesRegistry() throws ServiceException {
		ServicesRegistry result = null;
		result = new ExtensionServicesRegistry(org.eclipse.papyrus.infra.core.Activator.PLUGIN_ID);
		
		try {
			// have to create the model set and populate it with the DI model
			// before initializing other services that actually need the DI
			// model, such as the SashModel Manager service
			result.startServicesByClassKeys(ModelSet.class);
		} catch (ServiceException ex) {
			// Ignore this exception: some services may not have been loaded,
			// which is probably normal at this point
		}
		return result;
	}
	
}
