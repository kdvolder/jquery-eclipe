package ca.ubc.jquery.engine.tyruba.java.context;

import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;

import tyRuBa.modes.TypeConstructor;

public class EnumerationContext extends ContextTracker {

	private Object rep;

	public EnumerationContext(ITypeBinding binding, int sourceStart, int sourceLength, int typeStart, int typeLength, ContextTracker containingContext) {
		super(containingContext);
		rep = bucket.getRepresentation(binding);

		containingContext.assertChild(this);
		bucket.assertModifiers(rep, binding.getModifiers());
		bucket.assertName(rep, binding.getName());
	}

	@Override
	protected TypeConstructor getContextType() {
		return bucket.type_Enum;
	}

	@Override
	protected String getName() {
		return containing.getName();
	}

	@Override
	protected Object getRepresentation() {
		return rep;
	}

	/** 
	 * Kind of a lazy way of getting Enum constants to work.
	 */
	public void assertField(EnumConstantDeclaration node) {
		Object field = bucket.getRepresentation(node.resolveVariable());
		SimpleName n = node.getName();

		bucket.assertChild(rep, field);
		bucket.assertName(field, n.toString());
		bucket.assertField(field, n.getStartPosition(), n.getLength());	
		bucket.assertModifier(field, "public");
		bucket.assertModifier(field, "static");
		bucket.assertModifier(field, "final");
	}
}
