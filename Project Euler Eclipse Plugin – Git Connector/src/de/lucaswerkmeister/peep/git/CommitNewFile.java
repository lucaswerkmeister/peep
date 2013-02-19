package de.lucaswerkmeister.peep.git;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;

import de.lucaswerkmeister.peep.core.extensionPoints.CreateTask;

public class CommitNewFile implements CreateTask {

	@Override
	public void execute(IProject project, IFile file, int problemNumber) {
		try {
			Repository repo = RepositoryMapping.getMapping(project).getRepository();
			Git git = new Git(repo);
			String problemString = "Problem" + String.format("%03d", problemNumber);
			IPath path = file.getFullPath().makeRelativeTo(new Path(repo.getDirectory().getPath()));
			git.add().addFilepattern(path.toString()).call();
			git.commit().setMessage("Initial commit for " + problemString + ".").call();
		}
		catch (GitAPIException e) {
			e.printStackTrace();
		}
	}
}
