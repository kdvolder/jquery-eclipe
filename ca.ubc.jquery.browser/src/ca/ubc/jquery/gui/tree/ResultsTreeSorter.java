package ca.ubc.jquery.gui.tree;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

import ca.ubc.jquery.gui.results.QueryResultNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;

/**
 * Provides sorting
 * 
 * @author andrew added the ability to sort by score.
 * 
 * If the name of a variable is score, then the variable immediately above that
 * is sorted by the score automatically. This is just a quick hack.
 */
public class ResultsTreeSorter extends ViewerSorter {
	
	static {
		//This should work, but it doesn't. The property has to be set instead on commandline.
		//Note: why set this. The array sort method used by Eclipse viewersorter in more recent
		//versions of Java is not as tolerant as the legazy merge sort of changing the comparison
		//semantics while sort is running. As jquery is changing/computing labels in bachground
		//and labels factor into sorting... this causes the more modern sorter to throw exceptions.
		//A 'proper' fix for this is likely somewhat complicated and will require that we
		//block label updating while sorting is in progress or vice versa. This may also adversely
		//affect performance if not done carefully (e.g. sorting constantly blocking label updates).
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
	}

	public boolean byCategory = true;

	public boolean byScore = false;

	@Override
	public boolean isSorterProperty(Object element, String property) {
		// force viewer to re-sort when an object is updated
		return ("sort").equals(property);
	}

	@Override
	public int category(Object element) {
		ResultsTreeNode node = (ResultsTreeNode) element;
		Object value = node.getElement();

		// determine if the score needs to be returned instead 
		// of the category.
		if (byScore && node.hasScore()) {
			return node.getScore();
		}

		if (!byCategory) {
			return 0;
		}
		if (node instanceof QueryResultNode) {
			return ((QueryResultNode) node).getCategory();
		} else if (value instanceof IJavaElement) {
			if (value instanceof IJavaProject) {
				return 1;
			} else if (value instanceof IPackageFragmentRoot) {
				return 2;
			} else if (value instanceof IPackageFragment) {
				return 3;
			} else if (value instanceof ICompilationUnit) {
				return 4;
			} else if (value instanceof IClassFile) {
				return 5;
			} else if (value instanceof IPackageDeclaration) {
				return 6;
			} else if (value instanceof IImportDeclaration) {
				return 7;
			} else if (value instanceof IType) {
				return 8;
			}
			/*
				 * reserve value 9 for types also, for tyruba objects we'll
				 */

			else if (value instanceof IField) {
				return 10;
			} else if (value instanceof IMethod) {
				return 11;
			} else {
				return 12;
			}
		}
		//		Default is a String
		return 0;

	}

	@Override
	public int compare(Viewer viewer, Object e1, Object e2)
	{
		ResultsTreeNode n1 = (ResultsTreeNode) e1;
		ResultsTreeNode n2 = (ResultsTreeNode) e2;

		if ((n1 instanceof QueryResultNode) && (n2 instanceof QueryResultNode)) {
			QueryResultNode r1 = (QueryResultNode) n1;
			QueryResultNode r2 = (QueryResultNode) n2;
			
			Comparable k1 = r1.getSortKey();
			Comparable k2 = r2.getSortKey();
			
			if (k1 != null && k2 != null) return k1.compareTo(k2);
		}

		return super.compare(viewer, e1, e2);
	}


}
