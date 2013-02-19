package de.lucaswerkmeister.peep.extensionPoints;

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
	 * @param problemNumber
	 *            The number of the new Project Euler Problem.
	 */
	public void execute(int problemNumber);
}
