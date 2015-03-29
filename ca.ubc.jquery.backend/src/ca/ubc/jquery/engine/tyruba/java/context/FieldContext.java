/*
 * Created on Jul 14, 2003
 */
package ca.ubc.jquery.engine.tyruba.java.context;

import org.eclipse.jdt.core.dom.IVariableBinding;

import tyRuBa.engine.RBRepAsJavaObjectCompoundTerm;
import tyRuBa.engine.RBTerm;
import tyRuBa.modes.TypeConstructor;
import annotations.Feature;

@Feature(names={"./NONE","./annotations"})
public class FieldContext extends ContextTracker {
	
	private RBRepAsJavaObjectCompoundTerm rep = null;
	private IVariableBinding binding = null;
    private int fieldStart;
    private int fieldLength;
	
	public FieldContext(IVariableBinding binding, int sourceStart, int sourceLength,int fieldStart, int fieldLength, ContextTracker containingContext) {
		super(containingContext);
		this.binding = binding;
		rep = (RBRepAsJavaObjectCompoundTerm) bucket.getRepresentation(binding);
        this.fieldStart = fieldStart;
        this.fieldLength = fieldLength;

		containingContext.assertChild(this);
		
		bucket.assertField(rep, sourceStart, sourceLength);
		bucket.assertModifiers(rep, binding.getModifiers());
		bucket.assertTypeOf(rep, bucket.getRepresentation(binding.getType()));
		bucket.assertName(rep, binding.getName());
	}
    
    public ContextTracker exitContext() {
        bucket.assertMarkers(rep, fieldStart, fieldLength);        
        return containing;
    }


	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#getContextType()
	 */
    protected TypeConstructor getContextType() {
		return bucket.type_Field;
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#getRepresentation()
	 */
    protected RBTerm getRepresentation() {
		return rep;
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#getName()
	 */
	protected String getName() {
		return binding.getName();
	}
	
	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#getChildRep(ca.ubc.jquery.query.ContextTracker)
	 */
	protected Object getChildRep(ContextTracker childContext) {
		if (childContext.getContextType().equals(bucket.type_Initializer)) {
			return bucket.makeTypeCast(bucket.type_Initializer, rep.getValue());
		}
		//fall through switch means unsupported
		return super.getChildRep(childContext);
	}

	public void assertTags() {
		((FieldDeclarationContext) containing).assertTags(rep);
	}
	
}
