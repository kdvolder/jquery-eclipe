/*
 * Created on Jul 14, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ca.ubc.jquery.engine.tyruba.java.context;

import org.eclipse.jdt.core.IJavaElement;

import tyRuBa.engine.RBTerm;
import tyRuBa.modes.TypeConstructor;
import ca.ubc.jquery.engine.tyruba.java.CodeFactBucket;

/**
 * @author wannop
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class CUContext extends ContextTracker {
	IJavaElement cu;
	Object rep;
	
	/**
	 * @param bucket
	 */
	public CUContext(CodeFactBucket bucket, IJavaElement cu) {
		super(bucket);
		this.cu = cu;
        rep = bucket.getRepresentation(cu);    
    }
    
    public ContextTracker exitContext() {
        bucket.assertMarkers(rep, 0, Integer.MAX_VALUE);
        return containing;
    }

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.query.ContextTracker#getContextType()
	 */
	protected TypeConstructor getContextType() {
		return bucket.type_CU;
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
		return cu.getElementName();
	}

}
