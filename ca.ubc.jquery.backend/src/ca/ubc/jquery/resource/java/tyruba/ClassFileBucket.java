/*
 * Created on Apr 28, 2003
 */
package ca.ubc.jquery.resource.java.tyruba;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;

import ca.ubc.jquery.JQueryBackendPlugin;
import ca.ubc.jquery.api.JQueryFileElement;
import ca.ubc.jquery.api.JQueryResourceManager;

/**
 * @author wannop
 * 
 * To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ClassFileBucket extends CodeFactBucket {
	protected IClassFile classFile;

	// protected ASTParser parser;

	//	public ClassFileBucket(FrontEnd frontend, IClassFile classFile, JQueryResourceManager rbm) throws TypeModeError {
	//		super(frontend, classFile.getHandleIdentifier(), rbm);
	//		this.classFile = classFile;
	//		// parser = ASTParser.newParser(AST.JLS2); // handles JLS2 (J2SE 1.4)
	//		// parser.setSource(classFile);
	//	}

	public ClassFileBucket(IAdaptable resource, JQueryResourceManager rbm) {
		super(rbm);
		classFile = (IClassFile) resource.getAdapter(IClassFile.class);
	}

	public void parse() {
		try {
			if (!classFile.isConsistent()) {
				classFile.makeConsistent(null);
			}

			URL classFileURL = getURL(classFile);

			Object classfileRep = getRepresentation(classFile);
			assertCompilationUnit(classfileRep);
			assertName(classfileRep, classFile.getElementName());

			IPackageFragment pkg = (IPackageFragment) classFile.getParent();
			Object pkgRep = getRepresentation(pkg);

			// if (classfile.getElementName().indexOf('$') == -1) {
			// //Only outer classes are children of packages
			assertChild(pkgRep, classfileRep);
			assertPackage(pkgRep);
			assertName(pkgRep, pkg.getElementName());
			// }

			ClassReader cr;
			try {
				cr = new ClassReader(classFileURL.openStream());
			} catch (IOException e) {
				JQueryBackendPlugin.error("Could not find class file: " + classFile.getElementName(), e);
				return;
			}

			boolean generateFactsForMethodBodies = false;
			int flags = ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES;
			if (!generateFactsForMethodBodies)
				flags += ClassReader.SKIP_CODE;
			cr.accept(new ClassFileFactGenerator(this, classfileRep, generateFactsForMethodBodies), flags);
		} catch (Throwable e) {
			JQueryBackendPlugin.error("Problem parsing file '" + classFile.getElementName() + "' : ", e);
		} finally {
			anonClasses.clear();
			anonClassCounter = 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.ubc.jquery.engine.CodeFactBucket#getCompilationUnit()
	 */
	public IJavaElement getCompilationUnit() {
		return classFile;
	}

	private static URL getURL(IClassFile classfile) {
		try {
			IResource classResource = classfile.getResource();

			if (classResource == null) {
				// the class is in an external jar file (which is outside of the current project)
				StringBuffer innerURLPart = new StringBuffer(classfile.getElementName());
				IJavaElement parent = classfile.getParent();
				while (parent != null) {

					if (parent.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
						if (parent.getPath().toFile().toURI().toURL().toString().endsWith(".jar")) {
							// it's a jar file
							innerURLPart.insert(0, "jar:" + parent.getPath().toFile().toURI().toURL().toString() + "!/");
							parent = null;
						} else {
							// it's a file from another workspace
							innerURLPart.insert(0, parent.getPath().toFile().toURI().toURL().toString() + "/");
							parent = null;
						}
					} else {
						innerURLPart.insert(0, parent.getElementName().replaceAll("\\.", "/") + "/");
						parent = parent.getParent();
					}
				}
				return new URL(innerURLPart.toString());
			} else if (classResource.getFileExtension().equals("class")) {
				return classResource.getLocation().toFile().toURI().toURL();
			} else if (classResource.getFileExtension().equals("jar")) {
				String jarURL = classResource.getLocation().toFile().toURI().toURL().toString();
				String classFilePathInJar = classfile.getParent().getElementName().replaceAll("\\.", "/") + "/" + classfile.getElementName();
				return new URL("jar:" + jarURL + "!/" + classFilePathInJar);
			} else {
				throw new Error("Don't know how to handle this type of classFile: " + classfile);
			}
		} catch (MalformedURLException e) {
			JQueryBackendPlugin.error(e.getMessage(), e);
			throw new Error("Class File does not exist, this is a problem.");
		}
	}

	public String toString() {
		return "ClassFileBucket(" + this.classFile.getElementName() + ")";
	}

	protected JQueryFileElement makeSourceLocation(int startPos, int length) {
		return new JQueryFileElement(getCUHandle(), startPos, length);
	}
}