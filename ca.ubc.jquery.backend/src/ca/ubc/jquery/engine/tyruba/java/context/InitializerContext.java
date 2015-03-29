/*
 * Created on Jul 15, 2003
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
public class InitializerContext extends ContextTracker {

	Object rep = null;

	int sourceStart;

	int sourceLength;

	public InitializerContext(int initStart, int initLength, int modifiers, ContextTracker containing) {
		super(containing);
		sourceStart = initStart;
		sourceLength = initLength;

		rep = containing.getChildRep(this);

		bucket.assertInitializer(rep, sourceStart, sourceLength);
		bucket.assertModifiers(rep, modifiers);
		containing.assertChild(this);

	}

	public ContextTracker exitContext() {
		bucket.assertMarkers(rep, sourceStart, sourceLength);
		return containing;
	}

	public void assertCall(String callType, IMethodBinding calledMethodBinding, int sourceStart, int sourceLength) {
		bucket.assertCall(callType, rep, this.getName(), bucket.getRepresentation(calledMethodBinding), calledMethodBinding.getName(), sourceStart, sourceLength);
	}

	public void assertInstanceOf(ITypeBinding instanceOfWhat, int sourceStart, int sourceLen) {
		bucket.assertInstanceOf(rep, this.getName(), bucket.getRepresentation(instanceOfWhat), instanceOfWhat.getName(), sourceStart, sourceLen);
	}
	public void assertTypeCast(ITypeBinding castToWhat, int sourceStart, int sourceLen) {
		bucket.assertTypeCast(rep, this.getName(), bucket.getRepresentation(castToWhat), castToWhat.getName(), sourceStart, sourceLen);
	}

	public void assertAccess(String accessType, IVariableBinding accessed, int sourceStart, int sourceLength) {

		bucket.assertAccess(accessType, rep, this.getName(), bucket.getRepresentation(accessed), accessed.getName(), sourceStart, sourceLength);
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#getName()
	 */
	protected String getName() {
		TypeConstructor containingType = containing.getContextType();
		if (containingType.equals(bucket.type_Field)) {
			return "{" + containing.getName() + " = ...}";
		} else if (containingType.equals(bucket.type_Type)) {
			return containing.getName() + " initializer";
		} else
			return "UNKNOWN INITIALIZER TYPE";
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#getRepresentation()
	 */
	protected Object getRepresentation() {
		return rep;
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#getContextType()
	 */
	protected TypeConstructor getContextType() {
		return bucket.type_Initializer;
	}

	private int getSourceStart() {
		return sourceStart;
	}

	private int getSouceLength() {
		return sourceLength;
	}

	public void assertJavadocTag(JavadocTag tag) {
		bucket.assertJavadocTag(rep, tag.getName(), tag.getValue());
	}

	public void assertJavadocComment(JavadocComment comment) {
		if (comment != null) {
			bucket.assertJavadocLocation(rep, comment.getStart(), comment.getLength());
		}
	}

}
