package ca.ubc.jquery.engine.tyruba.java.context;

import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SimpleName;

import tyRuBa.engine.RBTerm;
import tyRuBa.modes.TypeConstructor;
import annotations.Export;
import annotations.Feature;

public @Feature(names={"./annotations","./NONE"}) class AnnotationAttributeContext extends ContextTracker {

	@Feature(names="./annotations")
	private String name;

	public @Feature(names={"./annotations"}) AnnotationAttributeContext(SimpleName name, ContextTracker context) {
		super(context);
		this.name = name.getIdentifier();
	}
	
	public @Feature(names={"./annotations"}) AnnotationAttributeContext(String name, ContextTracker context) {
		super(context);
		this.name = name;
	}

	@Override
	@Feature(names={"./annotations"})
	protected @Export(to={"./NONE"}) TypeConstructor getContextType() {
		return containing.getContextType();
	}

	@Override
	@Feature(names={"./annotations"})
	protected @Export(to={"./NONE"}) String getName() {
		return name;
	}

	@Override
	@Feature(names={"./annotations"})
	protected @Export(to={"./NONE"}) Object getRepresentation() {
		return containing.getRepresentation();
	}

	@Override
	@Feature(names={"./annotations"})
	public void assertStringLiteral(String literalValue) {
		bucket.assertAnnotationAttribute(getRepresentation(),name,literalValue);
		getAnnotationContext().markDeclared(name);
	}
	
	/* (non-Javadoc)
	 * @see ca.ubc.jquery.engine.tyruba.java.context.ContextTracker#assertBooleanLiteral(boolean)
	 */
	@Override
	@Feature(names={"./annotations"})
	public void assertBooleanLiteral(boolean booleanValue) {
		// Kludge: JQuery browser doesn't handle booleans properly
		bucket.assertAnnotationAttribute(getRepresentation(),name,String.valueOf(booleanValue));
		getAnnotationContext().markDeclared(name);
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.engine.tyruba.java.context.ContextTracker#assertCharacterLiteral(char)
	 */
	@Override
	@Feature(names={"./annotations"})
	public void assertCharacterLiteral(char charValue) {
		// Kludge: JQuery browser doesn't handle chars properly
		bucket.assertAnnotationAttribute(getRepresentation(),name,String.valueOf(charValue));
		getAnnotationContext().markDeclared(name);
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.engine.tyruba.java.context.ContextTracker#assertNumericLiteral(java.lang.String)
	 */
	@Override
	@Feature(names={"./annotations"})
	public void assertNumericLiteral(String token) {
		ITypeBinding literalType = getAnnotationContext().getAnnotationMemberType(name);
		
		if (literalType.isPrimitive()) {
			if (literalType.getName().equals("byte")) {
				bucket.assertAnnotationAttribute(getRepresentation(),name,Byte.parseByte(token));				
				getAnnotationContext().markDeclared(name);
			} else if (literalType.getName().equals("short")) {
				bucket.assertAnnotationAttribute(getRepresentation(),name,Short.parseShort(token));				
				getAnnotationContext().markDeclared(name);
			} else if (literalType.getName().equals("int")) {
				bucket.assertAnnotationAttribute(getRepresentation(),name,Integer.parseInt(token));				
				getAnnotationContext().markDeclared(name);
			} else if (literalType.getName().equals("long")) {
				bucket.assertAnnotationAttribute(getRepresentation(),name,Long.parseLong(token));				
				getAnnotationContext().markDeclared(name);
			} else if (literalType.getName().equals("float")) {
				bucket.assertAnnotationAttribute(getRepresentation(),name,Float.parseFloat(token));				
				getAnnotationContext().markDeclared(name);
			} else if (literalType.getName().equals("double")) {
				bucket.assertAnnotationAttribute(getRepresentation(),name,Double.parseDouble(token));				
				getAnnotationContext().markDeclared(name);
			} else {
				throw new Error("Numeric literal " + token + " for " + name + " is of unknown primitive type");
			}
		} else {
			throw new Error("Numeric literal " + token + " for " + name + " is not a primitive");
		}
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.engine.tyruba.java.context.ContextTracker#assertAccess(java.lang.String, org.eclipse.jdt.core.dom.IVariableBinding, int, int)
	 */
	@Override
	@Feature(names="./annotations")
	public void assertAccess(String accessType, IVariableBinding accessed,
			int sourceStart, int sourceLength) {
		bucket.assertAccess(accessType, 
				this.getRepresentation(), containing.getName(), 
				bucket.getRepresentation(accessed), accessed.getName(), 
				sourceStart, sourceLength);
		bucket.assertAnnotationAttribute(getRepresentation(), name, bucket.getRepresentation(accessed));		
		getAnnotationContext().markDeclared(name);
	}

	@Override
	@Feature(names="./annotations")
	public void assertAnnotation(AnnotationContext annot) {
		// In this context, we're using the annotation annot as the *value* of an annotation
		// attribute. Feature annotations use this construction.
		bucket.assertAnnotation(annot.getRepresentation());
		bucket.assertAnnotationAttribute(getRepresentation(), name, annot.getRepresentation());		
		getAnnotationContext().markDeclared(name);
	}
	
	private AnnotationContext getAnnotationContext () {
		ContextTracker parent = containing;
		while (parent != null) {
			if (parent instanceof AnnotationContext)
				return (AnnotationContext)parent;
			
			parent = parent.containing;
		}
		throw new Error("Could not find enclosing annotation for annotation attribute " + name);
	}
}
