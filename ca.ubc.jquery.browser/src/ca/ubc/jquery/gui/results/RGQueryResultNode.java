package ca.ubc.jquery.gui.results;

import ca.ubc.jquery.api.JQueryResultNode;

public class RGQueryResultNode extends QueryResultNode {
	private static final long serialVersionUID = 1L;

	public RGQueryResultNode(Object value, String source) {
		super(value, source);
	}

	public RGQueryResultNode(Object value, String source, int listPosition) {
		super(value, source, listPosition);
	}

	@Override
	public Object getElement() {
		return ((JQueryResultNode) super.getElement()).getValue();
	}

	public JQueryResultNode[] getGraphChildren() {
		return ((JQueryResultNode) super.getElement()).getChildren();
	}
}
