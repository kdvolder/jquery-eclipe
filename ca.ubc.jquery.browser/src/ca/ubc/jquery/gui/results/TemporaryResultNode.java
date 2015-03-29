package ca.ubc.jquery.gui.results;

public class TemporaryResultNode extends ResultsTreeNode {

	public static final long serialVersionUID = 1L;

	@Override
	public Object clone() {
		return new TemporaryResultNode();
	}

	@Override
	public Object getElement() {
		return "loading...";
	}
}
