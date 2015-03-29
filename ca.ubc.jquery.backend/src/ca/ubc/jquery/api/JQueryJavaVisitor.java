package ca.ubc.jquery.api;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import tyRuBa.engine.RBTerm;

/**
 * A class to extend the Java visitor.  This visitor is applied to
 * every Java file JQuery manages.
 *  
 * @author lmarkle
 *
 */
public abstract class JQueryJavaVisitor extends ASTVisitor {
	private JQueryFactGenerator generator;

	private ca.ubc.jquery.engine.tyruba.java.CodeFactBucket bucket;

	public JQueryJavaVisitor() {
		generator = null;
		bucket = null;
	}

	/**
	 * @return the compilation unit this visitor is visiting
	 */
	protected ICompilationUnit getCU() {
		return (ICompilationUnit) bucket.getCompilationUnit();
	}

	/**
	 * @param je
	 * @return The factbase representation of the given IJavaElement
	 */
	protected Object getRepresentation(IJavaElement je) {
		return bucket.getRepresentation(je);
	}

	/**
	 * @param binding
	 * @return The factbase representation of the given bindings
	 */
	protected RBTerm[] getRepresentation(ITypeBinding[] paramTypes) {
		return bucket.getRepresentation(paramTypes);
	}

	/**
	 * @param binding
	 * @return The factbase representation of the given binding
	 */
	protected Object getRepresentation(IBinding binding) {
		return bucket.getRepresentation(binding);
	}

	/**
	 * @return fact generator for this class
	 */
	protected JQueryFactGenerator getGenerator() {
		return generator;
	}

	public final void reset() {
		doReset();
		generator = null;
		bucket = null;
	}

	/**
	 * Method is called each time before visiting the AST.  This allows the visitor
	 * to reset any state for a once through pass.  
	 */
	protected void doReset() {
		// default does nothing
	}

	/**
	 * IMPORTANT: only the first call to this method actually does something.  All
	 * other calls are ignored.  The first call to this method is called right before
	 * the visitor is accepted.
	 */
	public void setBucket(ca.ubc.jquery.engine.tyruba.java.CodeFactBucket b) {
		if (bucket == null) {
			bucket = b;
		}
	}

	/**
	 * IMPORTANT: only the first call to this method actually does something.  All
	 * other calls are ignored.  The first call to this method is called right before
	 * the visitor is accepted.
	 */
	public void setGenerator(JQueryFactGenerator g) {
		if (generator == null) {
			generator = g;
		}
	}
}
