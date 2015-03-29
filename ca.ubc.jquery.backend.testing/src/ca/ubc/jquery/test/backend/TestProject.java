package ca.ubc.jquery.test.backend;

import java.io.ByteArrayInputStream;

import junit.framework.Assert;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.IWorkingSetManager;
import org.eclipse.ui.PlatformUI;

/**
 * A class for easily creating a test fixture project in the workspace and set it up
 * with some stuff.
 * 
 * @author kdvolder
 */
public class TestProject {

	private static final String javaNatureID = "org.eclipse.jdt.core.javanature";

	private static final String WORKINGSET_NAME = "test/src";

	private IWorkbench workbench;

	private IWorkspace workspace;

	private IWorkspaceRoot root;

	private IWorkingSetManager workingSetManager;

	private IProject project;

	private IJavaProject javaProject;

	private IWorkingSet sourceSet;

	private IFolder sourceFolder;

	public TestProject() throws CoreException {
		workbench = PlatformUI.getWorkbench();

		workspace = ResourcesPlugin.getWorkspace();

		workingSetManager = workbench.getWorkingSetManager();

		root = workspace.getRoot();

		project = root.getProject("test");
		if (project.exists()) {
			project.delete(true, true, null); // clean slate
		}
		project.create(null);
		project.open(null);

		addJavaNature();

		javaProject = JavaCore.create(project);
		javaProject.setRawClasspath(new IClasspathEntry[0], null); // otherwise we get an error because of "." sourcepath

		addJRELibrary();
		addSourceDir("src");
		createSourceWorkingSet();
		setOutputDir("bin");
	}

	private void createSourceWorkingSet() throws JavaModelException {
		sourceSet = workingSetManager.getWorkingSet(WORKINGSET_NAME);
		if (sourceSet != null) {
			workingSetManager.removeWorkingSet(sourceSet);
		}

		sourceSet = workingSetManager.createWorkingSet(WORKINGSET_NAME, new IAdaptable[] { getSourcePFR() });
		sourceSet.setId("org.eclipse.jdt.ui.JavaWorkingSetPage");
		workingSetManager.addWorkingSet(sourceSet);
	}

	private void addJRELibrary() throws JavaModelException {
		addClassPath(JavaRuntime.getDefaultJREContainerEntry());
	}

	private void addSourceDir(String dirName) throws CoreException {
		sourceFolder = project.getFolder(dirName);
		sourceFolder.create(true, true, null);
		addClassPath(JavaCore.newSourceEntry(sourceFolder.getFullPath()));
	}

	private void addClassPath(IClasspathEntry entry) throws JavaModelException {
		IClasspathEntry[] classPath = javaProject.getRawClasspath();
		IClasspathEntry[] newClassPath = new IClasspathEntry[classPath.length + 1];
		for (int i = 0; i < classPath.length; i++)
			newClassPath[i] = classPath[i];
		newClassPath[classPath.length] = entry;
		javaProject.setRawClasspath(newClassPath, null);
	}

	private void setOutputDir(String dirName) throws CoreException {
		IFolder binFolder = project.getFolder(dirName);
		if (!binFolder.exists())
			binFolder.create(true, true, null);
		javaProject.setOutputLocation(binFolder.getFullPath(), null);
	}

	private void addJavaNature() throws CoreException {
		if (hasJavaNature())
			return;
		IProjectDescription desc = project.getDescription();
		String[] ids = desc.getNatureIds();
		String[] newIds = new String[ids.length + 1];
		for (int i = 0; i < ids.length; i++) {
			newIds[i] = ids[i];
		}
		newIds[ids.length] = javaNatureID;
		desc.setNatureIds(newIds);
		project.setDescription(desc, null);
	}

	public void dispose() throws CoreException {
		if (sourceSet != null)
			workingSetManager.removeWorkingSet(sourceSet);
		if (project != null)
			project.delete(true, true, null);
		javaProject = null;
	}

	public boolean exists() {
		return project.exists();
	}

	public boolean hasJavaNature() throws CoreException {
		return project.hasNature(javaNatureID);
	}

	/**
	 * Returns the name of a workingset that has the project's source code.
	 */
	public String getSourceCode() {
		return project.getName() + "/src";
	}

	public void addSourceFile(String f, String contents) throws CoreException {
		IFile file = getSourceFolder().getFile(f);
		if (file.exists()) {
			file.setContents(new ByteArrayInputStream(contents.getBytes()), true, false, null);
		} else {
			file.create(new ByteArrayInputStream(contents.getBytes()), true, null);
		}
	}

	private IFolder getSourceFolder() {
		return sourceFolder;
	}

	private IPackageFragmentRoot getSourcePFR() throws JavaModelException {
		return javaProject.findPackageFragmentRoot(getSourceFolder().getFullPath());
	}

	public boolean fileExists(String file) {
		return project.getFile(file).exists();
	}

	public void build() throws CoreException {
		project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
	}

	protected void addClassWithMethod(String imports, String className, String methodText) throws CoreException {
		addSourceFile(className + ".java", imports + "\n\n" + "public class " + className + "{\n" + "   " + methodText + "\n" + "}");
	}

}
