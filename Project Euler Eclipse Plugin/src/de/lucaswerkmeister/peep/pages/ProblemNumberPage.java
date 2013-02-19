package de.lucaswerkmeister.peep.pages;

import java.text.NumberFormat;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (java).
 */
public class ProblemNumberPage extends WizardPage {
	private Spinner problemNumber;
	private IPath containerPath;

	private ISelection selection;

	/**
	 * Constructor for ProblemNumberPage.
	 * 
	 * @param selection
	 *            The selection.
	 */
	public ProblemNumberPage(ISelection selection) {
		super("wizardPage");
		setTitle("Project Euler Problem");
		setDescription("This wizard creates a new Project Euler Problem Java class that can be opened by a Java editor.");
		this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("Problem number:");

		problemNumber = new Spinner(container, SWT.BORDER);
		problemNumber.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		// containerText = new Text(container, SWT.BORDER | SWT.SINGLE);
		// GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		// containerText.setLayoutData(gd);
		// containerText.addModifyListener(new ModifyListener() {
		// public void modifyText(ModifyEvent e) {
		// dialogChanged();
		// }
		// });
		//
		// Button button = new Button(container, SWT.PUSH);
		// button.setText("Browse...");
		// button.addSelectionListener(new SelectionAdapter() {
		// public void widgetSelected(SelectionEvent e) {
		// handleBrowse();
		// }
		// });
		// label = new Label(container, SWT.NULL);
		// label.setText("&File name:");
		//
		// fileText = new Text(container, SWT.BORDER | SWT.SINGLE);
		// gd = new GridData(GridData.FILL_HORIZONTAL);
		// fileText.setLayoutData(gd);
		// fileText.addModifyListener(new ModifyListener() {
		// public void modifyText(ModifyEvent e) {
		// dialogChanged();
		// }
		// });

		initialize();
		dialogChanged();
		setControl(container);
	}

	/**
	 * Tests if the current workbench selection is a suitable container to use.
	 */
	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IPackageFragment)
				containerPath = ((IPackageFragment) obj).getPath();
			else if (obj instanceof IFolder)
				containerPath = ((IFolder) obj).getFullPath()
						.append("problems");
			else if (obj instanceof IJavaProject)
				containerPath = ((IJavaProject) obj).getPath().append("src")
						.append("problems");
		}
		problemNumber.setValues(0, 0, 1000, 4, 1, 0);
	}

	/**
	 * Ensures that the problem number is valid.
	 */
	private void dialogChanged() {
		IResource resource = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName()));

		if (getContainerName().length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (resource == null || (resource.getType() & IResource.FOLDER) == 0) {
			updateStatus("File container must exist");
			return;
		}
		IContainer container = (IContainer) resource;
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
			return;
		}
		if (problemNumber.getSelection() == 0) {
			updateStatus("There is no problem 0");
			return;
		}
		if (container.getFile(new Path(getFileName())).exists()) {
			updateStatus("File " + getFileName() + " already exists");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public String getContainerName() {
		if (containerPath == null)
			return null;
		return containerPath.toString();
	}

	public String getFileName() {
		NumberFormat n = NumberFormat.getIntegerInstance();
		n.setMinimumIntegerDigits(3);
		return "Problem" + n.format(problemNumber.getSelection()) + ".java";
	}

	public int getProblemNumber() {
		return problemNumber.getSelection();
	}
}