package de.lucaswerkmeister.peep.core.extensionPoints;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;

/**
 * Represents a task that can be executed when a new problem file is created.
 * 
 * @author Lucas Werkmeister
 * @version 1.1
 */
public interface CreateTask {

	/**
	 * Executes the task.
	 * 
	 * @param project
	 *            The enclosing project.
	 * @param file
	 *            The file that is or will be created.
	 * @param problemNumber
	 *            The number of the new Project Euler Problem.
	 */
	public void execute(IProject project, IFile file, int problemNumber);
}
