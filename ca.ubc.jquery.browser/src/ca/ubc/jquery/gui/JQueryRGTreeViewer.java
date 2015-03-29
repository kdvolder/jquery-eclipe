package ca.ubc.jquery.gui;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.progress.UIJob;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResultGraph;
import ca.ubc.jquery.api.JQueryResultNode;
import ca.ubc.jquery.gui.results.NoResultNode;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.RGQueryResultNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;
import ca.ubc.jquery.gui.views.JQueryTreeView;

/**
 * Tree Viewer which supports the JQueryResultGraph node structure.
 * 
 * This could in theory be much better at handling big queries if the queries
 * are optimized to only return one variable result at a time.  
 * 
 * @author lmarkle
 */
public class JQueryRGTreeViewer extends JQueryTreeViewer {
	public JQueryRGTreeViewer(Composite parent, int flags, JQueryTreeView view) {
		super(parent, flags, view);
		setupContentProvider();
	}

	public JQueryRGTreeViewer(Composite parent, int flags) {
		super(parent, flags);
		setupContentProvider();
	}

	protected void setupContentProvider() {
		setContentProvider(new ITreeContentProvider() {
			public Object[] getChildren(Object parentElement) {
				ResultsTreeNode n = (ResultsTreeNode) parentElement;
				generateChildren(n);
				Collection result = n.getChildren();

				return result.toArray();
			}

			protected void generateChildren(ResultsTreeNode n) {
				JQueryResultNode[] x = new JQueryResultNode[0];
				if (n instanceof QueryNode) {
					try {
						JQueryResultGraph g = ((QueryNode) n).getQuery().getGraph();
						g.useInfiniteRecursion(true);
						x = g.getChildren();
					} catch (JQueryException e) {
						JQueryTreeBrowserPlugin.error(e);
					}

					if (x.length == 0) {
						n.addChild(new NoResultNode());
					}
				} else if (n instanceof RGQueryResultNode) {
					x = ((RGQueryResultNode) n).getGraphChildren();
				}

				for (int i = 0; i < x.length; i++) {
					n.addChild(new RGQueryResultNode(x[i], ""));
				}
			}

			public Object getParent(Object element) {
				return ((ResultsTreeNode) element).getParent();
			}

			public boolean hasChildren(Object element) {
				ResultsTreeNode n = (ResultsTreeNode) element;
				if (!n.hasChildren()) {
					generateChildren(n);
				}
				return n.hasChildren();
			}

			public Object[] getElements(Object inputElement) {
				return getChildren(inputElement);
			}

			public void dispose() {
			}

			public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			}
		});
	}

	@Override
	public Job execute(final QueryNode queryNode, int startDelay) {
		Job j = new UIJob("refresh node") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				queryNode.beginUpdateResults();
				queryNode.midUpdateResults();
				if (queryNode.getParent() != null) {
					queryNode.getParent().setExpandstate(ResultsTreeNode.NodeExpanded);
				}
				queryNode.setExpandstate(ResultsTreeNode.NodeExpanded);

				refresh(queryNode.getParent());
				expandToLevel(queryNode.getParent(), 1);
				queryNode.endUpdateResults();

				return Status.OK_STATUS;
			}
		};
		j.setSystem(true);
		j.schedule(startDelay);
		return j;
	}
}
