package ca.ubc.jquery.gui.tree;

import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

import ca.ubc.jquery.gui.results.ResultsTreeNode;

public class RegExpViewerFilter extends ViewerFilter {
	private Pattern expression;

	private ILabelProvider labeler;

	private ITreeContentProvider content;

	public RegExpViewerFilter(ILabelProvider provider, ITreeContentProvider content, Pattern regExp) {
		expression = regExp;
		labeler = provider;
		this.content = content;
	}

	@Override
	public boolean isFilterProperty(Object element, String property) {
		return ("filter").equals(property);
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		ResultsTreeNode parent = (ResultsTreeNode) parentElement;
		ResultsTreeNode node = (ResultsTreeNode) element;

		// if the parent matches, we're good
		// else, check the node and it's children
		// else we fail
		return (matchParent(parent) || match(node) || matchChildren(node));
	}

	private boolean matchParent(ResultsTreeNode node) {
		ResultsTreeNode parent = node.getParent();
		return (match(node) || (parent != null && matchParent(parent)));
	}

	private boolean matchChildren(ResultsTreeNode node) {
		boolean match = false;

		//		for (Iterator it = node.getChildren().iterator(); !match && it.hasNext();) {
		//			ResultsTreeNode n = (ResultsTreeNode) it.next();
		//			match = match(n) || matchChildren(n);
		//		}

		if (content.hasChildren(node)) {
			Object[] children = content.getChildren(node);
			for (int i = 0; !match && i < children.length; i++) {
				ResultsTreeNode n = (ResultsTreeNode) children[i];
				match = match(n) || matchChildren(n);
			}
		}

		return match;
	}

	private boolean match(ResultsTreeNode node) {
		String label = node.cachedLabel;

		if (label == null) {
			label = labeler.getText(node);
		}

		if (label == null) {
			return false;
		} else {
			return expression.matcher(label).matches();
		}
	}
}
