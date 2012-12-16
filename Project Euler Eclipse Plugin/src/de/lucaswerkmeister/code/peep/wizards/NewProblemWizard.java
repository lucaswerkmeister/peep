package de.lucaswerkmeister.code.peep.wizards;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Scanner;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import de.lucaswerkmeister.code.peep.pages.ProblemNumberPage;

/**
 * This is a sample new wizard. Its role is to create a new file resource in the
 * provided container. If the container resource (a folder or a project) is
 * selected in the workspace when the wizard is opened, it will accept it as the
 * target container. The wizard creates one file with the extension "java". If a
 * sample multi-page editor (also available as a template) is registered for the
 * same extension, it will be able to open it.
 */

public class NewProblemWizard extends Wizard implements INewWizard {
	private ProblemNumberPage page;
	private ISelection selection;

	/**
	 * Constructor for NewProblemWizard.
	 */
	public NewProblemWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new ProblemNumberPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We
	 * will create an operation and run it using wizard as execution context.
	 */
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		final int problemNumber = page.getProblemNumber();
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, problemNumber, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error",
					realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the file if missing
	 * or just replace its contents, and open the editor on the newly created
	 * file.
	 */

	private void doFinish(String containerName, String fileName,
			int problemNumber, IProgressMonitor monitor) throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName
					+ "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream(problemNumber, container);
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
				try {
					IDE.openEditor(page, file, true);
				} catch (PartInitException e) {
				}
			}
		});
		monitor.worked(1);
	}

	/**
	 * We will initialize file contents with a sample text.
	 */

	private InputStream openContentStream(int problemNumber,
			IContainer container) {
		String contents = null;
		byte readTimeoutCounter = 0;

		do {
			try (Scanner scan = new Scanner(container
					.findMember("/Problem.template").getLocation().toFile())) {
				scan.useDelimiter("\\Z");
				contents = scan.next();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			readTimeoutCounter++;
		} while (contents == null && readTimeoutCounter < 10);
		if (contents == null)
			return new ByteArrayInputStream(
					"Unable to find template file, sorry".getBytes());
		if (contents.contains("&PROBLEMTEXT_HTML;")) {
			String problemText_html = null;
			readTimeoutCounter = 0;
			do {
				try (Scanner problemPageScanner = new Scanner(
						new URL("http://www.projecteuler.net/problem="
								+ problemNumber).openStream())) {
					problemPageScanner.useDelimiter("\\Z");
					String fullProblem = problemPageScanner.next();

					String problemWithoutStart = fullProblem
							.substring(fullProblem
									.indexOf("<div class=\"problem_content\" role=\"problem\">")
									+ "<div class=\"problem_content\" role=\"problem\">"
											.length());

					int index;
					// deal with inner DIVs
					for (index = problemWithoutStart.indexOf("</div>"); problemWithoutStart
							.substring(0, index).contains("<div"); index = problemWithoutStart
							.indexOf("</div>", index + 1)) {
						problemWithoutStart = problemWithoutStart.replaceFirst(
								"<div", ">DIV");
					}
					problemText_html = problemWithoutStart.substring(0, index)
							.replaceAll(">DIV", "<div");

					problemText_html = problemText_html.trim();

					readTimeoutCounter++;
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} while (problemText_html == null && readTimeoutCounter < 10);
			if (problemText_html == null)
				problemText_html = "Unable to retreive problem text, sorry";
			contents = contents.replace("&PROBLEMTEXT_HTML;", problemText_html);

			NumberFormat n = NumberFormat.getInstance();
			n.setMinimumIntegerDigits(3);
			contents = contents.replace("&PROBLEMNUMBER;",
					n.format(problemNumber));

			contents = contents.replace("&AUTHOR;", "Lucas"); // TODO this is
																// obviously not
																// the right way
																// to do it
		}

		return new ByteArrayInputStream(contents.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR,
				"Project_Euler_Eclipse_Plugin", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize
	 * from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}