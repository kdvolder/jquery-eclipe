/*
 * Created on Jul 14, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ca.ubc.jquery.engine.tyruba.java.context;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import tyRuBa.engine.RBTerm;
import tyRuBa.modes.TypeConstructor;
import ca.ubc.jquery.JQueryBackendPlugin;
import ca.ubc.jquery.tyruba.javadoc.JavadocComment;
import ca.ubc.jquery.tyruba.javadoc.JavadocTag;

/**
 * @author wannop
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TypeContext extends ContextTracker {

	/** The logger. */
	private Logger logger = Logger.getLogger(TypeContext.class);

	Object rep = null;

	ITypeBinding binding = null;

	String name = null;

	int sourceStart, sourceLength;

	int typeStart, typeLength;

	boolean hasConstructor = false;

	//	Map initializerMap = null;
	int initializerCount = 0;

	public TypeContext(ITypeBinding binding, int sourceStart, int sourceLength, int typeStart, int typeLength, ContextTracker containingContext) {
		super(containingContext);
		this.binding = binding;
		this.rep = bucket.getRepresentation(binding);
		this.sourceStart = sourceStart;
		this.sourceLength = sourceLength;
		this.typeStart = typeStart;
		this.typeLength = typeLength;

		if (binding.isGenericType()) {
			ITypeBinding[] params = binding.getTypeParameters();
			for (int i = 0; i < params.length; i++) {
				assertTypeParameter(params[i], sourceStart, sourceLength);
			}
		}

		containingContext.assertChild(this);

		if (binding.isInterface()) {
			bucket.assertInterface(rep, sourceStart, sourceLength);
			if (binding.isAnnotation()) {
				bucket.assertAnnotationDeclaration(rep);
			}
		} else {
			bucket.assertClass(rep, sourceStart, sourceLength);
			ITypeBinding superClassBinding = binding.getSuperclass();
			if (superClassBinding != null) {
				bucket.assertExtends(rep, bucket.getRepresentation(superClassBinding));
			} else {
				logger.debug("Binding " + binding + " has no superclass");
			}

		}

		//		name = (binding.isAnonymous()) ? bucket.getQNameFromBinding(binding) : binding.getName();   
		name = binding.getName();

		//		inserting a name for anonymous types would break anonType predicate.
		if (!binding.isAnonymous()) {
			bucket.assertName(rep, name);
			bucket.assertFullyQualifiedName(rep, binding.getQualifiedName());
		}

		bucket.assertModifiers(rep, binding.getModifiers());

		// Generate interface facts.
		ITypeBinding interfaces[];
		try {
			interfaces = binding.getInterfaces();
		} // Workaround for bug in M7
		catch (NullPointerException e) {
			interfaces = new ITypeBinding[] {};
		}
		for (int i = 0; i < interfaces.length; i++) {
			if (binding.isInterface()) {
				bucket.assertExtends(rep, bucket.getRepresentation(interfaces[i]));
			} else {
				bucket.assertImplements(rep, bucket.getRepresentation(interfaces[i]));
			}
		}
	}

	private void assertTypeParameter(ITypeBinding param, int sourceStart, int sourceLen) {
		Object paramRep = bucket.getRepresentation(param);
		bucket.assertTypeParameter(rep, paramRep, sourceStart, sourceLen);
		bucket.assertName(paramRep, param.getName());
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#getContextType()
	 */
	protected TypeConstructor getContextType() {
		return bucket.type_Type;
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#getRepresentation()
	 */
	protected Object getRepresentation() {
		return rep;
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#getName()
	 */
	protected String getName() {
		return name;
	}

	public void assertChild(ContextTracker childContext) {
		super.assertChild(childContext);
		if (childContext.getContextType().equals(bucket.type_Method)) {
			if (((MethodContext) childContext).isConstructor()) {
				hasConstructor = true;
			}
		}
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#exitContext()
	 */
	public ContextTracker exitContext() {

		bucket.assertMarkers(rep, typeStart, typeLength);

		//if no explicit constructors were encountered, find/insert default constructor fact
		if (!binding.isInterface() && !hasConstructor) {
			//TODO an npe may occur here if there are compilation problems with the type.  
			IMethodBinding methods[] = binding.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
				IMethodBinding method = methods[i];
				if (method.isConstructor()) {
					Object consRep = bucket.getRepresentation(method);
					bucket.assertChild(rep, consRep);
					bucket.assertConstructor(consRep, sourceStart, 0);
					//TODO decide what facts to keep/add about default constructor
					bucket.assertReturnType(consRep, bucket.getRepresentation(method.getReturnType()));
					bucket.assertSignature(consRep, bucket.getSignatureFromBinding(method, false));
					bucket.assertModifiers(consRep, method.getModifiers());

					hasConstructor = true;
					break;
				}
			}
			if (!hasConstructor) {
				JQueryBackendPlugin.traceQueries("TypeContext for " + getName() + " has no explicit/default constructor bindings");
			}
		}
		return super.exitContext();
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#getChildRep(ca.ubc.jquery.query.ContextTracker)
	 */
	protected Object getChildRep(ContextTracker childContext) {
		String childRep;
		TypeConstructor type;

		if (childContext.getContextType().equals(bucket.type_Initializer)) {
			childRep = ((RBTerm) rep).up() + ".init" + initializerCount++;
			type = bucket.type_Initializer;
			return bucket.makeTypeCast(type, childRep);
		}

		//fall through switch means unsupported
		return super.getChildRep(childContext);
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

	public String toString() {
		return "TypeContext(" + this.getName() + ")";
	}
}
