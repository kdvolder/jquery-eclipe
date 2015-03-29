/*
 * Created on 12-Sep-2003
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package ca.ubc.jquery.gui.results;

public class NoResultNode extends ResultsTreeNode {
	public static final long serialVersionUID = 1;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see ca.ubc.jquery.gui.results.ResultsTreeNode#getElement()
	 */
	public Object getElement() {
		return "Query returned no results";
	}
	
	public Object clone() {
		return new NoResultNode();
	}
};
