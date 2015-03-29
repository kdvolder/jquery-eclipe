package ca.ubc.jquery.gui.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryFileElement;
import ca.ubc.jquery.api.JQueryUpdateTarget;
import ca.ubc.jquery.gui.JQueryResultsLabelProvider;
import ca.ubc.jquery.gui.JQueryTreeViewer;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.QueryResultNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;
import ca.ubc.jquery.gui.tree.ResultsTreeBuilderJob;
import ca.ubc.jquery.gui.tree.ResultsTreeContentProvider;
import ca.ubc.jquery.gui.tree.ResultsTreeListener;
import ca.ubc.jquery.gui.tree.ResultsTreeSorter;
import ca.ubc.jquery.gui.tree.TreeRegExpFilterJob;

public class JQueryQuickOutline extends PopupDialog {
	private JQueryTreeViewer viewer;

	private ResultsTreeContentProvider contentProvider;

	private JQueryResultsLabelProvider labelProvider;

	private ResultsTreeBuilderJob resultsJob;

	private QueryNode root;

	private Text regExpFilterText;

	private String selectionFilter;

	private JQueryUpdateTarget target;

	public JQueryQuickOutline(Shell parent, QueryNode root, String inputFilter, String selectionFilter, JQueryUpdateTarget target) {
		super(parent, INFOPOPUPRESIZE_SHELLSTYLE, true, true, true, true, "", root.getLabel());

		resultsJob = null;
		contentProvider = null;
		labelProvider = null;
		this.selectionFilter = selectionFilter;
		this.target = target;

		this.root = root;
		try {
			root.getQuery().addFilter("input", JQueryAPI.createQuery(inputFilter), JQueryAPI.getThisVar(), JQuery.NoPosition);
		} catch (JQueryException e) {
			JQueryTreeBrowserPlugin.error("Creating quick view: ", e);
		}
	}

	@Override
	protected boolean hasInfoArea() {
		return true;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 350);
	}

	@Override
	protected Point getInitialLocation(Point initialSize) {
		return super.getInitialLocation(initialSize);
	}

	@Override
	protected Control getFocusControl() {
		return regExpFilterText;
	}

	@Override
	protected void setTabOrder(Composite parent) {
		parent.setTabList(new Control[] { regExpFilterText.getParent(), viewer.getControl().getParent() });
		regExpFilterText.getParent().setTabList(new Control[] { regExpFilterText });
	}

	@Override
	protected Control createTitleControl(Composite parent) {
		Composite result = parent;

		GridLayout layout = GridLayoutFactory.fillDefaults().create();
		layout.numColumns = 2;
		GridData data;

		result.setLayout(layout);
		data = new GridData(SWT.FILL, SWT.NONE, true, false);
		data.horizontalSpan = 1;
		result.setLayoutData(data);

		regExpFilterText = new Text(parent, SWT.NONE);
		data = new GridData(SWT.FILL, SWT.NONE, true, false);
		regExpFilterText.setLayoutData(data);
		regExpFilterText.addModifyListener(new ModifyListener() {

			private UIJob updateJob = null;

			public void modifyText(ModifyEvent e) {
				try {
					String t = ((Text) e.widget).getText();
					Pattern x = Pattern.compile(".*(" + t + ").*");

					if (updateJob != null) {
						updateJob.cancel();
					}

					updateJob = new TreeRegExpFilterJob("Update regular expression text", viewer, x);
					updateJob.schedule(250);
				} catch (PatternSyntaxException ex) {
					// ignore error
				}
			}
		});
		regExpFilterText.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN) {
					JQueryQuickOutline.this.viewer.getControl().setFocus();
				}
			}

			public void keyReleased(KeyEvent e) {
			}
		});

		return result;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite result = new Composite(parent, SWT.NONE);

		GridLayout layout = GridLayoutFactory.fillDefaults().create();
		GridData data;

		result.setLayout(layout);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		result.setLayoutData(data);

		viewer = new JQueryTreeViewer(result, SWT.MULTI);
		data = new GridData(SWT.FILL, SWT.FILL, true, true);
		viewer.getControl().setLayoutData(data);
		contentProvider = new ResultsTreeContentProvider();
		labelProvider = new JQueryResultsLabelProvider(viewer);
		viewer.setContentProvider(contentProvider);
		viewer.setLabelProvider(labelProvider);
		viewer.setSorter(new ResultsTreeSorter());
		viewer.addTreeListener(new ResultsTreeListener());
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				IStructuredSelection selection = (IStructuredSelection) event.getSelection();

				ResultsTreeNode node = (ResultsTreeNode) selection.getFirstElement();
				if (node == null) {
					return;
				}
				close();
				doDoubleClickOpen(node);
			}
		});
		viewer.setInput(root);

		execute(root, 0);
		return result;
	}

	@Override
	public boolean close() {
		if (resultsJob != null) {
			resultsJob.cancel();
		}

		return super.close();
	}

	public Job execute(QueryNode queryNode, int updateDelay) {
		if (resultsJob != null && resultsJob.getState() != Job.NONE) {
			resultsJob.cancel();
		}

		String name = "Query update Job:" + queryNode.getLabel();
		resultsJob = new ResultsTreeBuilderJob(name, queryNode);
		resultsJob.setSystem(true);
		resultsJob.setPriority(Job.INTERACTIVE);

		resultsJob.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent ev) {
				Job j = new UIJob("") {
					public IStatus runInUIThread(IProgressMonitor monitor) {
						selectItems();
						return Status.OK_STATUS;
					}
				};
				j.setSystem(true);
				j.schedule();
			}
		});

		resultsJob.addDefaultChangeAdapter(viewer, queryNode);
		resultsJob.schedule(updateDelay);

		return resultsJob;
	}

	private void doDoubleClickOpen(ResultsTreeNode node) {
		try {
			Object value = node.getElement();
			if (value instanceof JQueryFileElement) {
				((JQueryFileElement) value).openInEditor();
			} else {
				JQueryFileElement loc = JQueryAPI.getFileElement(value);
				if (loc != null) {
					loc.openInEditor();
				}
			}
		} catch (JQueryException ex) {
			JQueryTreeBrowserPlugin.error(ex);
		}
	}

	private void selectItems() {
		// add items for selection highlighting
		//
		// execute selection query on each element

		Object[] targ;
		if (target.getTarget() instanceof Object[]) {
			targ = (Object[]) target.getTarget();
		} else if (target.getTarget() == null) {
			targ = new Object[] { "" };
		} else {
			targ = new Object[] { target.getTarget() };
		}

		JQuery fq = null;
		List selectedItems = new ArrayList();
		if (!selectionFilter.equals("") && selectionFilter != null) {
			try {
				fq = JQueryAPI.createQuery(selectionFilter);
			} catch (JQueryException e) {
				JQueryTreeBrowserPlugin.error("Malformed selection filter", e);
			}
			for (int i = 0; i < targ.length && fq != null; i++) {
				Object t = targ[i];
				try {
					fq.bind(JQueryAPI.getThisVar(), t);
					if (fq.execute().hasNext()) {
						ResultsTreeNode n = new QueryResultNode(t, "");
						selectedItems.add(n);
					}
				} catch (Exception e) {
				}
			}

			// update selection
			viewer.setSelection(new StructuredSelection(selectedItems));
		}
	}
}
