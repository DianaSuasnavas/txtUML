package hu.elte.txtuml.export.papyrus.wizardz;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.search.JavaSearchScope;
import org.eclipse.jdt.internal.ui.dialogs.PackageSelectionDialog;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

import hu.elte.txtuml.api.layout.ClassDiagram;
import hu.elte.txtuml.api.layout.StateMachineDiagram;
import hu.elte.txtuml.export.papyrus.preferences.PreferencesManager;
import hu.elte.txtuml.utils.eclipse.NotFoundException;
import hu.elte.txtuml.utils.eclipse.PackageUtils;
import hu.elte.txtuml.utils.eclipse.ProjectUtils;
import hu.elte.txtuml.utils.eclipse.WizardUtils;

/**
 * The Page for txtUML visualization.
 */
@SuppressWarnings("restriction")
public class VisualizeTxtUMLPage extends WizardPage {

	private static final String browseButtonText = "Browse...";
	private static final Class<?>[] diagramTypes = { StateMachineDiagram.class, ClassDiagram.class };

	private Composite container;
	private Text txtUMLModel;
	private List<IType> txtUMLLayout = new LinkedList<>();
	private Text txtUMLProject;
	private ScrolledComposite sc;

	/**
	 * The Constructor
	 */
	public VisualizeTxtUMLPage() {
		super("Visualize txtUML page");
		setTitle("Visualize txtUML page");
		setDescription("Provide the txtUML project, model package and diagrams to be visualized.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.
	 * widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		sc = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		container = new Composite(sc, SWT.NONE);

		GridLayout layout = new GridLayout(4, false);
		container.setLayout(layout);

		Label label1 = new Label(container, SWT.NONE);
		label1.setText("txtUML Project: ");
		txtUMLProject = new Text(container, SWT.BORDER | SWT.SINGLE);
		txtUMLProject.setText(PreferencesManager.getString(PreferencesManager.TXTUML_VISUALIZE_TXTUML_PROJECT));
		Button browseProject = new Button(container, SWT.NONE);
		browseProject.setText(browseButtonText);
		browseProject.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(container.getShell(),
						new WorkbenchLabelProvider(), new WorkbenchContentProvider() {
							@Override
							public Object[] getChildren(Object element) {
								if (element instanceof IWorkspaceRoot) {
									return Stream.of(((IWorkspaceRoot) element).getProjects()).filter(pr -> pr.isOpen())
											.toArray();
								}
								return new Object[0];
							}
						});
				dialog.setInput(ResourcesPlugin.getWorkspace().getRoot());
				dialog.setTitle("Project Selection");
				dialog.open();
				Object[] result = dialog.getResult();
				if (result != null && result.length > 0) {
					txtUMLProject.setText(((IProject) result[0]).getName());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Label label2 = new Label(container, SWT.NONE);
		label2.setText("txtUML Model: ");
		txtUMLModel = new Text(container, SWT.BORDER | SWT.SINGLE);
		txtUMLModel.setText(PreferencesManager.getString(PreferencesManager.TXTUML_VISUALIZE_TXTUML_MODEL));
		Button browseModel = new Button(container, SWT.NONE);
		browseModel.setText(browseButtonText);
		browseModel.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {

				PackageSelectionDialog dialog = getModelBrowserDialog();

				dialog.setTitle("Project Selection");
				dialog.open();
				Object[] result = dialog.getResult();
				if (result != null && result.length > 0) {
					txtUMLModel.setText(((IPackageFragment) result[0]).getElementName());
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		final Label label = new Label(container, SWT.TOP);
		label.setText("txtUML Diagrams: ");

		addInitialLayoutFields();

		// diagram descriptions tree
		ScrolledComposite treeComposite = new ScrolledComposite(container, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		CheckboxTreeViewer tree = getDiagramTreeViewer(treeComposite);

		tree.expandAll();
		List<Object> checkedElements = new ArrayList<>(txtUMLLayout);
		List<Object> checkedProjects = txtUMLLayout.stream().map(diagramLayout -> diagramLayout.getJavaProject())
				.collect(Collectors.toList());
		checkedElements.addAll(checkedProjects);
		tree.setCheckedElements(checkedElements.toArray());
		checkedProjects.stream().forEach(elem -> tree.setGrayed(elem, true));
		tree.collapseAll();

		try {
			tree.setExpandedElements(new IJavaProject[] { ProjectUtils.findJavaProject(txtUMLProject.getText()) });
		} catch (NotFoundException ex) {
		}

		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1);
		GridData treeGd = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		treeGd.heightHint = 200;
		treeGd.widthHint = 150;
		GridData labelGd = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		labelGd.verticalIndent = 5;
		txtUMLModel.setLayoutData(gd);
		txtUMLProject.setLayoutData(gd);
		label.setLayoutData(labelGd);
		treeComposite.setLayoutData(treeGd);

		treeComposite.setContent(tree.getControl());
		treeComposite.setExpandHorizontal(true);
		treeComposite.setExpandVertical(true);
		sc.setContent(container);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		container.setSize(container.computeSize(450, 300, true));
		sc.setMinSize(container.getSize());
		sc.setSize(container.getSize());

		setControl(parent);
		setPageComplete(true);
	}

	/**
	 * Returns the txtUML ModelClass
	 * 
	 * @return Returns the txtUML ModelClass
	 */
	public String getTxtUmlModelClass() {
		return txtUMLModel.getText();
	}

	/**
	 * Returns the txtUML project's name
	 * 
	 * @return Returns the txtUML project's name
	 */
	public String getTxtUmlProject() {
		return txtUMLProject.getText();
	}

	/**
	 * Returns the txtUML model layout classes and containing project names
	 * 
	 * @return
	 */
	public Map<String, String> getTxtUmlLayout() {
		HashMap<String, String> result = new HashMap<String, String>();
		for (IType layout : txtUMLLayout) {
			String layoutName = layout.getFullyQualifiedName();
			if (!"".equals(layoutName)) {
				result.put(layoutName, layout.getJavaProject().getElementName());
			}
		}
		return result;
	}

	private CheckboxTreeViewer getDiagramTreeViewer(ScrolledComposite treeComposite) {
		CheckboxTreeViewer tree = new CheckboxTreeViewer(treeComposite, SWT.FULL_SELECTION);
		IContentProvider cp = new WorkbenchContentProvider() {
			@Override
			public Object[] getChildren(Object element) {
				if (element instanceof IWorkspaceRoot) {
					List<IJavaProject> javaProjects = new ArrayList<>();
					IProject[] allProjects = ((IWorkspaceRoot) element).getProjects();
					for (IProject pr : allProjects) {
						try {
							IJavaProject javaProject = ProjectUtils.findJavaProject(pr.getName());
							if (WizardUtils.containsClassesWithSuperTypes(javaProject, diagramTypes)) {
								javaProjects.add(ProjectUtils.findJavaProject(pr.getName()));
							}
						} catch (NotFoundException e) {
						}
					}
					return javaProjects.toArray();
				}
				if (element instanceof IJavaProject) {
					List<IPackageFragment> packageFragments = null;
					try {
						packageFragments = PackageUtils.findAllPackageFragmentsAsStream((IJavaProject) element)
								.collect(Collectors.toList());
					} catch (JavaModelException ex) {
					}
					List<IType> descriptionTypes = packageFragments.stream()
							.flatMap(pf -> getDiagramDescriptions(pf).stream()).collect(Collectors.toList());
					return descriptionTypes.toArray();
				}
				return new Object[0];
			}

			@Override
			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			@Override
			public Object getParent(Object element) {
				if (element instanceof IResource) {
					return ((IResource) element).getParent();
				}
				return null;
			}

			@Override
			public boolean hasChildren(Object element) {
				try {
					return getChildren(element).length > 0;
				} catch (NullPointerException ex) {
					return false;
				}
			}
		};

		tree.getTree().addListener(SWT.Selection, event -> {
			if (event.detail == SWT.CHECK) {
				TreeItem item = (TreeItem) event.item;
				if (item.getData() instanceof IType) {
					IType type = (IType) item.getData();
					if (!txtUMLLayout.contains(type)) {
						txtUMLLayout.add(type);
					} else {
						txtUMLLayout.remove(type);
					}
					updateParentCheck(item.getParentItem());
				} else {
					checkItems(item, item.getChecked());
				}
			}
		});

		tree.setContentProvider(cp);
		tree.setLabelProvider(new JavaElementLabelProvider(JavaElementLabelProvider.SHOW_POST_QUALIFIED | JavaElementLabelProvider.SHOW_SMALL_ICONS));
		tree.setInput(ResourcesPlugin.getWorkspace().getRoot());

		return tree;
	}

	private List<IType> getDiagramDescriptions(IPackageFragment packageFragment) {
		return WizardUtils.getTypesBySuperclass(packageFragment, diagramTypes);
	}

	private void addInitialLayoutFields() {
		Collection<String> layouts = PreferencesManager.getStrings(PreferencesManager.TXTUML_VISUALIZE_TXTUML_LAYOUT);
		for (String layout : layouts) {
			if (!layout.isEmpty())
				addLayoutField(layout);
		}
	}

	private void addLayoutField(String qualifiedName) {
		// find type by qualified name
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject pr : allProjects) {
			IJavaProject javaProject = null;
			try {
				javaProject = ProjectUtils.findJavaProject(pr.getName());
				List<IType> types = PackageUtils.findAllPackageFragmentsAsStream(javaProject)
						.flatMap(pf -> getDiagramDescriptions(pf).stream()).collect(Collectors.toList());
				types.stream().filter(type -> type.getFullyQualifiedName().equals(qualifiedName))
						.forEach(type -> txtUMLLayout.add(type));
			} catch (NotFoundException | JavaModelException ex) {
				continue;
			}
		}
	}

	private PackageSelectionDialog getModelBrowserDialog() {
		JavaSearchScope scope = new JavaSearchScope();
		try {
			IJavaProject javaProject = ProjectUtils.findJavaProject(txtUMLProject.getText());
			List<IPackageFragment> allPackageFragments = PackageUtils.findAllPackageFragmentsAsStream(javaProject)
					.collect(Collectors.toList());

			List<IPackageFragment> modelPackages = WizardUtils.getModelPackages(allPackageFragments);
			for (IPackageFragment modelPackage : modelPackages) {
				scope.add(modelPackage);
			}
		} catch (NotFoundException | JavaModelException ex) {
		}

		return new PackageSelectionDialog(getShell(), getContainer(),
				PackageSelectionDialog.F_HIDE_DEFAULT_PACKAGE | PackageSelectionDialog.F_REMOVE_DUPLICATES, scope);
	}

	private void updateParentCheck(TreeItem parentItem) {
		boolean isAnyChecked = Stream.of(parentItem.getItems()).anyMatch(child -> child.getChecked());
		boolean isAllChecked = Stream.of(parentItem.getItems()).allMatch(child -> child.getChecked());

		if (isAllChecked) {
			parentItem.setGrayed(false);
			parentItem.setChecked(true);
		} else if (isAnyChecked) {
			parentItem.setChecked(true);
			parentItem.setGrayed(true);
		} else {
			parentItem.setGrayed(false);
			parentItem.setChecked(false);
		}
	}

	private void checkItems(TreeItem item, boolean checked) {
		item.setGrayed(false);
		item.setChecked(checked);
		TreeItem[] items = item.getItems();
		for (int i = 0; i < items.length; ++i) {
			TreeItem child = items[i];
			child.setChecked(checked);
			IType childData = (IType) child.getData();
			if (checked) {
				if (!txtUMLLayout.contains(childData)) {
					txtUMLLayout.add(childData);
				}
			} else {
				txtUMLLayout.remove(childData);
			}
		}
	}

}
