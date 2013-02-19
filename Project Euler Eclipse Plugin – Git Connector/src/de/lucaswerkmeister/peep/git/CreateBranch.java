package de.lucaswerkmeister.peep.git;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.egit.core.project.RepositoryFinder;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import de.lucaswerkmeister.peep.core.extensionPoints.PeepTask;

public class CreateBranch implements PeepTask {

	@Override
	public void execute(IProject project, int problemNumber) {
		try {
			RepositoryMapping m = new RepositoryFinder(project).find(null).iterator().next();
			Repository r = m.getRepository();
			Git git = new Git(r);
			git.checkout().setName("Problem" + String.format("%03d", problemNumber)).setCreateBranch(true).call();
		}
		catch (CoreException | GitAPIException e) {
			e.printStackTrace();
		}
	}
}
