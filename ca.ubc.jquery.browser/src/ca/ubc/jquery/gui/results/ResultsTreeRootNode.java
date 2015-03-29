/*
 * Created on Jun 2, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package ca.ubc.jquery.gui.results;

import ca.ubc.jquery.api.JQueryFactBase;

/**
 * @author wannop
 * 
 * To change the template for this generated type comment go to Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ResultsTreeRootNode extends ResultsTreeNode {
	public static final long serialVersionUID = 1L;

	private transient JQueryFactBase fb;

	public ResultsTreeRootNode(JQueryFactBase fb) {
		this.fb = fb;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.ubc.jquery.gui.results.ResultsTreeNode#getElement()
	 */
	public Object getElement() {
		return fb;
	}

	public void setElement(JQueryFactBase f) {
		fb = f;
	}

	public Object clone() {
		return new ResultsTreeRootNode(fb);
	}
}