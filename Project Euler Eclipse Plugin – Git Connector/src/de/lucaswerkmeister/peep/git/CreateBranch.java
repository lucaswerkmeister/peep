package de.lucaswerkmeister.peep.git;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import de.lucaswerkmeister.peep.core.extensionPoints.PeepTask;

public class CreateBranch implements PeepTask {

	@Override
	public void execute(IProject project, int problemNumber) {
		try {
			Repository repo = RepositoryMapping.getMapping(project).getRepository();
			Git git = new Git(repo);
			String branchName = "Problem" + String.format("%03d", problemNumber);
			if (repo.getRef(branchName) == null)
				git.branchCreate().setStartPoint("master").setName(branchName).setForce(true).call();
			git.checkout().setName(branchName).call();
		}
		catch (GitAPIException | IOException e) {
			e.printStackTrace();
		}
	}
}
