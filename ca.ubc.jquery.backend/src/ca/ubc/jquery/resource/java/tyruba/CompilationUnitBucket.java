package ca.ubc.jquery.resource.java.tyruba;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import ca.ubc.jquery.JQueryBackendPlugin;
import ca.ubc.jquery.api.JQueryFileElement;
import ca.ubc.jquery.api.JQueryResourceManager;
import ca.ubc.jquery.engine.tyruba.java.context.CUContext;

/**
 * FactMaker is basically a wrapper class for a Tyruba RuleBaseBucket. It provides some convenience 
 * methods for inserting facts into the bucket's database, as well as a method for retrieving objects 
 * from the rulebase. The boolean outdated indicates whether or not the bucket is current with the code.
 * 
 * @author wannop / Doug Janzen
 */
public class CompilationUnitBucket extends CodeFactBucket {

	// protected ASTParser parser;
	protected ICompilationUnit cu;

	public CompilationUnitBucket(IAdaptable adaptable, JQueryResourceManager manager) {
		super(manager);
		this.cu = (ICompilationUnit) adaptable.getAdapter(ICompilationUnit.class);
	}

	protected JQueryFileElement makeSourceLocation(int startPos, int length) {
		String fileName = getCompilationUnit().getResource().getFullPath().toString();
		return new JQueryFileElement(fileName, startPos, length);
	}

	public void parse() {
		try {
			ICompilationUnit cu = (ICompilationUnit) getCompilationUnit();

			if (!cu.isConsistent()) {
				cu.makeConsistent(null);
			}

			CUContext context = new CUContext(this, cu);
			Object cuRep = getRepresentation(cu);

			assertCompilationUnit(cuRep);
			assertName(cuRep, cu.getElementName());

			IPackageFragment pkg = (IPackageFragment) cu.getParent();
			Object pkgRep = getRepresentation(pkg);

			assertChild(pkgRep, cuRep);
			assertPackage(pkgRep);
			assertName(pkgRep, pkg.getElementName());

			FactsGenerator factsGenerator = new FactsGenerator(context);

			try {
				markers = getCompilationUnit().getUnderlyingResource().findMarkers(IMarker.MARKER, true, IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
				throw new Error("If this has happened, eclipse is not happy (CoreException): " + e.getMessage());
			}

			CompilationUnit cuAST = getAST();
			if (cuAST != null) {
				factsGenerator.generate(cuAST);
				// load and install any Java visitors from extension points
				JQueryBackendPlugin.getJavaVisitorHandler().install(cuAST, getGenerator(), this);

				context.exitContext();
			}
		} catch (Throwable e) {
			JQueryBackendPlugin.error("Problem parsing CU: " + getCompilationUnit().getElementName(), e);
		} finally {
			anonClasses.clear();
			anonClassCounter = 0;
		}
	}

	private ASTParser getParser() {
		ASTParser parser;
		// make sure resource exists
		try {
			cu.getCorrespondingResource();
			parser = ASTParser.newParser(AST.JLS3); // handles JLS2 (J2SE 1.5)
			parser.setSource(cu);
			parser.setResolveBindings(true);
			return parser;
		} catch (JavaModelException e) {
			JQueryBackendPlugin.traceQueries("CompilationUnitBucket: Exception while retrieving cu's resource: " + cu.getElementName() + ": " + e.getMessage());
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.ubc.jquery.engine.CodeFactBucket#getCompilationUnit()
	 */
	public IJavaElement getCompilationUnit() {
		return cu;
	}

	private CompilationUnit getAST() {
		ASTParser parser = getParser();
		if (parser == null)
			return null;
		else
			return (CompilationUnit) parser.createAST(null);
	}

	public String toString() {
		return "CompilationUnitBucket(" + cu.getElementName() + ")";
	}
}
