/*
 * Created on Jul 11, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ca.ubc.jquery.engine.tyruba.java.context;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import tyRuBa.engine.RBTerm;
import tyRuBa.modes.TypeConstructor;
import ca.ubc.jquery.tyruba.javadoc.JavadocComment;
import ca.ubc.jquery.tyruba.javadoc.JavadocTag;

/**
 * @author wannop
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MethodContext extends ContextTracker {
	Object rep;

	IMethodBinding binding;

	private int methodStart;

	private int methodLength;

	public MethodContext(IMethodBinding binding, int sourceStart, int sourceLength, int methodStart, int methodLength, ContextTracker containingContext) {
		super(containingContext);
		this.binding = binding;
		this.rep = bucket.getRepresentation(binding);
		this.methodStart = methodStart;
		this.methodLength = methodLength;

		containingContext.assertChild(this);

		if (binding != null) {
			if (binding.isConstructor()) {
				bucket.assertConstructor(rep, sourceStart, sourceLength);
				bucket.assertName(rep, binding.getJavaElement().getParent().getElementName());
			} else {
				bucket.assertMethod(rep, sourceStart, sourceLength);
				bucket.assertName(rep, binding.getName());
			}

			bucket.assertReturnType(rep, bucket.getRepresentation(binding.getReturnType()));
			bucket.assertSignature(rep, bucket.getSignatureFromBinding(binding, false));
			bucket.assertModifiers(rep, binding.getModifiers());

			//Generate throws facts.
			ITypeBinding[] exceptions = binding.getExceptionTypes();
			for (int i = 0; i < exceptions.length; i++) {
				bucket.assertThrows(rep, bucket.getRepresentation(exceptions[i]));
			}
			// Generate arg facts.  {We need to somehow maintain the order!}
			ITypeBinding paramTypes[] = binding.getParameterTypes();
			RBTerm[] paramTypeReps = bucket.getRepresentation(paramTypes);
			bucket.assertParams(rep, paramTypeReps);
		}
	}

	public ContextTracker exitContext() {
		bucket.assertMarkers(rep, methodStart, methodLength);
		return containing;
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#getContextType()
	 */
	protected TypeConstructor getContextType() {
		return bucket.type_Method;
	}

	protected Object getRepresentation() {
		return rep;
	}

	protected String getName() {
		return binding == null ? "NULL" : binding.getName();
	}

	public void assertAccess(String accessType, IVariableBinding accessed, int sourceStart, int sourceLength) {

		bucket.assertAccess(accessType, rep, this.getName(), bucket.getRepresentation(accessed), accessed.getName(), sourceStart, sourceLength);
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#assertCall(java.lang.String, org.eclipse.jdt.core.dom.IMethodBinding, int, int)
	 */
	public void assertCall(String callType, IMethodBinding calledMethodBinding, int sourceStart, int sourceLength) {
		bucket.assertCall(callType, rep, getName(), bucket.getRepresentation(calledMethodBinding), calledMethodBinding.getName(), sourceStart, sourceLength);
	}

	public void assertInstanceOf(ITypeBinding instanceOfWhat, int sourceStart, int sourceLen) {
		bucket.assertInstanceOf(rep, this.getName(), bucket.getRepresentation(instanceOfWhat), instanceOfWhat.getName(), sourceStart, sourceLen);
	}
	public void assertTypeCast(ITypeBinding castToWhat, int sourceStart, int sourceLen) {
		bucket.assertTypeCast(rep, this.getName(), bucket.getRepresentation(castToWhat), castToWhat.getName(), sourceStart, sourceLen);
	}

	protected boolean isConstructor() {
		return (binding != null && binding.isConstructor());
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.engine.context.ContextTracker#assertTag(ca.ubc.jquery.ast.JavadocTag)
	 */
	public void assertJavadocTag(JavadocTag tag) {
		bucket.assertJavadocTag(rep, tag.getName(), tag.getValue());
	}

	public void assertJavadocComment(JavadocComment comment) {
		if (comment != null) {
			bucket.assertJavadocLocation(rep, comment.getStart(), comment.getLength());
		}
	}

}
