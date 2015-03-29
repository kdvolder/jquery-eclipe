package ca.ubc.jquery.engine.tyruba.java.context;

import java.util.HashSet;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Name;

import tyRuBa.engine.RBRepAsJavaObjectCompoundTerm;
import tyRuBa.modes.TypeConstructor;
import annotations.Export;
import annotations.Feature;

/**
 * @author kdvolder
 */
public @Feature(names={"./annotations","./NONE"}) class AnnotationContext extends ContextTracker {
	
	@Feature(names="./annotations")
	Object rep;

	@Feature(names="./annotations")
	private int start, length;
	
	@Feature(names="./annotations")
	private Annotation annotation;
	
	private HashSet declaredAttributes = new HashSet();
	
	@Feature(names="./annotations")
	public AnnotationContext(Annotation node, int sourceStart, int sourceLength, ContextTracker containingContext) {
		super(containingContext);
		this.annotation = node;
		this.rep = bucket.makeTypeCast(bucket.type_Annotation, 
				((RBRepAsJavaObjectCompoundTerm)containingContext.getRepresentation()).getValue()+":"+sourceStart);
        this.start = sourceStart;
        this.length = sourceLength;
	
        containingContext.assertAnnotation(this);
        
        assertSourceLocation();
        assertTypeName(annotation.getTypeName());
        
        IAnnotationBinding annBinding = node.resolveAnnotationBinding();
        if (annBinding != null) {
        	bucket.assertTypeOfAnnotation(rep, bucket.getRepresentation(annBinding.getAnnotationType()));
        }
        
     }
    
	@Feature(names="./annotations")
	public ContextTracker exitContext() {
        bucket.assertMarkers(rep, start, length);   
        
        // Default values now handled as properties of the annotation type declaration.
        
//		IMethodBinding[] attributes = annotation.resolveAnnotationBinding().getAnnotationType().getDeclaredMethods();
//		for (IMethodBinding attr : attributes) {
//			if (!declaredAttributes.contains(attr.getName()) && attr.getDefaultValue() != null) {
//				Object defaultValue = attr.getDefaultValue();
////				if (defaultValue instanceof IAnnotationBinding) {
////					// FIXME We don't really support annotations as default values yet. This
////					// just shows a stringified version of the annotation as the value.
////					bucket.assertAnnotationAttribute(getRepresentation(), attr.getName(), 
////							defaultValue.toString());					
////				} else if (defaultValue instanceof Character || defaultValue instanceof Boolean) {
////				    // Kludge: JQuery browser doesn't display these types cleanly
////					bucket.assertAnnotationAttribute(getRepresentation(), attr.getName(), 
////							defaultValue.toString());
////				} else {
////					bucket.assertAnnotationAttribute(getRepresentation(), attr.getName(), 
////							defaultValue);
////				}
//				
//				// I'm not sure there are any benefits for using the above code.  The
//				// else case also causes some errors for the database because it can
//				// insert objects that are not serializable.  
//				//
//				// Another way of handling that is to test for Serializable but you would
//				// have to throw an error or something which seems messy too.  For now,
//				// this will fix the software until the above FIXME can be addressed.
//				bucket.assertAnnotationAttribute(getRepresentation(), attr.getName(), 
//				defaultValue.toString());
//			}
//		}

        return containing;
    }
    
    
    
	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#getContextType()
	 */
	@Feature(names="./annotations")
	protected @Export(to={"./NONE"}) TypeConstructor getContextType() {
		return bucket.type_Annotation;
	}

	@Feature(names="./annotations")
	@Override protected @Export(to={"./NONE"}) Object getRepresentation() {
		return rep;
	}

	@Feature(names="./annotations")
	protected @Export(to={"./NONE"}) String getName() {
		return annotation.getTypeName().getFullyQualifiedName();
	}

	@Feature(names="./annotations")
	private void assertTypeName(Name typeName) {
		bucket.assertName(rep, typeName.getFullyQualifiedName());
	}

	@Feature(names="./annotations")
	private void assertSourceLocation() {
		bucket.assertSourceLocation(rep, start, length);
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.engine.tyruba.java.context.ContextTracker#assertAccess(java.lang.String, org.eclipse.jdt.core.dom.IVariableBinding, int, int)
	 */
	@Override
	@Feature(names="./annotations")
	public void assertAccess(String accessType, IVariableBinding accessed,
			int sourceStart, int sourceLength) {
		bucket.assertAccess(accessType, 
				rep, this.getName(), 
				bucket.getRepresentation(accessed), accessed.getName(), 
				sourceStart, sourceLength);
	}

	public ITypeBinding getAnnotationMemberType (String name) {
		IMethodBinding[] attributes = annotation.resolveAnnotationBinding().getAnnotationType().getDeclaredMethods();
		for (IMethodBinding attr : attributes) {
			if (attr.getName().equals(name)) {
				return attr.getReturnType();
			}
		}
		throw new Error("Annotation " + getName() + " has no attribute with name " + name);
	}
	
	public void markDeclared (String name) {
		declaredAttributes.add(name);
	}	
}
