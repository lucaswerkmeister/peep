package de.lucaswerkmeister.peep.core.extensionPoints;

import org.eclipse.core.resources.IProject;

/**
 * Represents a PEEP-related task that can be executed.
 * 
 * @author Lucas Werkmeister
 * @version 1.0
 */
public interface PeepTask {

	/**
	 * Executes the task.
	 * 
	 * @param project
	 *            The enclosing project.
	 * @param problemNumber
	 *            The number of the new Project Euler Problem.
	 */
	public void execute(IProject project, int problemNumber);
}
