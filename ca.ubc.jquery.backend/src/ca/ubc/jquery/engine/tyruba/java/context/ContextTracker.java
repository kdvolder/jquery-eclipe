/*
 * Created on Jul 10, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ca.ubc.jquery.engine.tyruba.java.context;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

import tyRuBa.engine.RBTerm;
import tyRuBa.modes.TypeConstructor;
import annotations.Export;
import annotations.Feature;
import ca.ubc.jquery.engine.tyruba.java.CodeFactBucket;
import ca.ubc.jquery.tyruba.javadoc.JavadocComment;
import ca.ubc.jquery.tyruba.javadoc.JavadocTag;

@Feature(names={"./annotations","./NONE"})
public abstract class ContextTracker {

	@Export(to={"./annotations"})
	CodeFactBucket bucket;
	
	@Export(to={"./annotations"})
	ContextTracker containing;

	public ContextTracker(CodeFactBucket bucket) {
		this.bucket = bucket;
		containing = null;
	}

	@Export(to={"./annotations"}) 
	public ContextTracker(ContextTracker containingContext) {
		this.bucket = containingContext.bucket;
		this.containing = containingContext;

	}
	/** 
	 * Subclasses should override this method if there is work to do upon leaving
	 * this context. 
	 * 
	 * IMPORTANT: the base implementation's return value MUST be returned, factsGenerator depends on it. 
	 */ 
	@Export(to={"./annotations"})
	public ContextTracker exitContext() {
		return containing;
	}
	
	@Export(to={"./annotations"})
	protected abstract String getName();

	@Export(to={"./annotations"})
	protected abstract Object getRepresentation();
	
	@Export(to={"./annotations"})
	protected abstract TypeConstructor getContextType();
	
	/**
	 * It is this method's responsibility to insert a child fact about the given child context.  Subclasses
	 * may override this method if they wish to track/count children or insert additional facts, but a super call
	 * to ContextTracker's implementation should be included if the overriding method doesn't explicitly 
	 * insert a child fact for childContext.    
	 *
	 * @param childContext
	 */
	public void assertChild(ContextTracker childContext) {
		bucket.assertChild(getRepresentation(), childContext.getRepresentation());
	}
	
//	public void assertThrow(ITypeBinding thrownType, int sourceStart, int sourceLength) {
//		bucket.assertThrow(getRepresentation(), getName(), bucket.getRepresentation(thrownType), thrownType.getName(), sourceStart, sourceLength);
////			throw new Error("assertThrow: method not implemented for context "  +this.getClass().getName());	
//		}
	
	/**
	 * Contexts depending on a parent context for their representation (initializers, or others without bindings)  may call this method,
	 * but be careful to override this method in every possible parent context.  Base implementation just throws an error.   
	 */
	protected Object getChildRep(ContextTracker childContext) {
// 		switch (childContext.getContextType()) {
// 		}
 		throw new Error("getChildRep: method not implemented by "  +this.getClass().getName() + " for child context " + childContext.getClass().getName());
	}
		
	
	
	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.CodeFactBucket#assertAccess(java.lang.String, java.lang.Object, java.lang.String, java.lang.Object, java.lang.String, int, int)
	 */
	public void assertAccess(	String accessType,
								IVariableBinding accessed,
								int sourceStart,
								int sourceLength) {
		
		throw new Error("assertAccess: method not implemented for context "  +this.getClass().getName());
//		bucket.assertAccess(accessType, this.getRepresentation(), this.getName(), accessedField, accessedName, sourceStart, sourceLength);				
			
	}
	

	public void assertCall(String callType,
							 IMethodBinding calledMethodBinding, 
							 int sourceStart, int sourceLength) {
							 	
		throw new Error("assertCall: method not implemented for context "  +this.getClass().getName());
	}

	public void assertInstanceOf(ITypeBinding instanceOfWhat, int sourceStart, int sourceLen) {
		throw new Error("assertInstanceOf: method not implemented for context "  +this.getClass().getName());
	}

	public void assertTypeCast(ITypeBinding castToWhat, int startPosition, int length) {
		throw new Error("assertTypeCast: method not implemented for context "+ this.getClass().getName());
	}

	public void assertJavadocTag(JavadocTag tag) {							 	
		throw new Error("assertJavadocTag: method not implemented for context "  +this.getClass().getName());
	}

	public void assertJavadocComment(JavadocComment comment) {							 	
		throw new Error("assertJavadocComment: method not implemented for context "  +this.getClass().getName());
	}
	
	public void assertElementLocation(int start, int length) {							 	
		bucket.assertElementLocation(getRepresentation(), start, length);
	}

	@Feature(names="./annotations")
	public void assertAnnotation(AnnotationContext annot) {
		bucket.assertAnnotation(annot.getRepresentation());
		bucket.assertHasAnnotation(getRepresentation(),annot.getRepresentation());
	}
	
	/**
	 * Record the default value of an annotation declaration member element.
	 * @param value String representation of default value
	 */
	public void assertDefaultValue(String value) {
		bucket.assertHasDefault(getRepresentation());
		bucket.assertDefaultValue(getRepresentation(), value);
	}
	
	/**
	 * Record the default value of an annotation declaration member element.
	 * @param value String representation of default value
	 */
	public void assertDefaultValue(IBinding binding) {
		bucket.assertHasDefault(getRepresentation());
		bucket.assertDefaultValue(getRepresentation(), bucket.getRepresentation(binding));
	}

	@Export(to={"./annotations"}) 
	public void assertStringLiteral(String literalValue) {
		//Does nothing in most contexts. (But would be useul to know about String literals in program
		//text, so could do something if so desired.)
	}

	@Export(to={"./annotations"}) 
	public void assertNumericLiteral(String token) {
		// Used for annotations, otherwise does nothing
	}

	@Export(to={"./annotations"}) 
	public void assertBooleanLiteral(boolean booleanValue) {
		// Used for annotations, otherwise does nothing
	}

	@Export(to={"./annotations"}) 
	public void assertCharacterLiteral(char charValue) {
		// Used for annotations, otherwise does nothing
	}

}
