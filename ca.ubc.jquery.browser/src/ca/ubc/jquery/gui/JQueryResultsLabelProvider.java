package ca.ubc.jquery.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jdt.ui.JavaElementLabelProvider;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.progress.UIJob;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryEvent;
import ca.ubc.jquery.api.JQueryEventListener;
import ca.ubc.jquery.gui.results.NoResultNode;
import ca.ubc.jquery.gui.results.QueryNode;
import ca.ubc.jquery.gui.results.ResultsTreeNode;
import ca.ubc.jquery.gui.results.ResultsTreeRootNode;
import ca.ubc.jquery.gui.results.TemporaryResultNode;

/**
 * The label provider for the ResultsView tree. This label provider is mostly a wrapper for the 
 * JavaElementLabelProvider in the JDT. For result nodes that are strings a default image is 
 * retrieved from the plugin's image registry.
 */
public class JQueryResultsLabelProvider extends JavaElementLabelProvider implements JQueryEventListener/*, ISchedulingRule*/ {
	// perform the database hit inside it's own thread to keep from blocking the UI
	private class ImageUpdateJob extends Job /*implements ISchedulingRule*/ {
		private ResultsTreeNode element;

		private boolean doneLabel;

		private boolean doneImage;

		private boolean imageJob;

		public ImageUpdateJob(String name, final ResultsTreeNode node, boolean imageJob) {
			super(name);
			element = node;
			doneLabel = false;
			doneImage = false;
			this.imageJob = imageJob;
			System.out.println("job for "+node);

			addJobChangeListener(new JobChangeAdapter() {
				@Override
				public void done(IJobChangeEvent event) {
					if (ImageUpdateJob.this.imageJob) {
						removeCachingJob(imageCachingJobs, element);
					} else {
						removeCachingJob(labelCachingJobs, element);
					}

					if (!(doneImage && doneLabel) && event.getResult() == Status.OK_STATUS) {
						Job j = new UIJob("Update tree element") {
							public IStatus runInUIThread(IProgressMonitor monitor) {
								if (!(doneImage && doneLabel)) {
									viewer.update(element, null);
								}

								if (ImageUpdateJob.this.imageJob) {
									removeCachingJob(imageCachingJobs, element);
								} else {
									removeCachingJob(labelCachingJobs, element);
								}

								if (sorterJob != null) {
									sorterJob.cancel();
								}
								sorterJob = new UIJob("sort and filter") {
									public IStatus runInUIThread(IProgressMonitor monitor) {
										viewer.update(element, new String[] { "sort", "filter" });
										//										viewer.update(element, null);
										return Status.OK_STATUS;
									}
								};
								sorterJob.setSystem(true);
								sorterJob.setRule(JQueryAPI.getRule());
								sorterJob.setPriority(Job.SHORT);
								// sort the nodes after we've finished caching their names
								sorterJob.schedule();

								return Status.OK_STATUS;
							}
						};
						j.setSystem(true);
						//j.setRule(JQueryAPI.getRule());
						j.schedule();

						if (ImageUpdateJob.this.imageJob) {
							addCachingJob(imageCachingJobs, element, j);
						} else {
							addCachingJob(labelCachingJobs, element, j);
						}
					}
				}
			});
		}

		public IStatus run(IProgressMonitor monitor) {
			Image img = element.cachedImage;
			String label = element.cachedLabel;
			Object value = element.getElement();

			if (imageJob) {
				// Get element image
				if (value instanceof Object[]) {
					img = JQueryResultsLabelProvider.this.factbaseLabelProvider.getImage(((Object[]) value)[0]);
				} else {
					img = JQueryResultsLabelProvider.this.factbaseLabelProvider.getImage(value);
				}
			} else {
				// get Element label
				if (value instanceof Object[]) {
					Object a[] = (Object[]) value;
					StringBuffer buff = new StringBuffer("[");
					for (int i = 0; i < a.length; i++) {
						String temp = JQueryAPI.getElementLabel(a[i]);
						buff.append(temp + ", ");
					}
					int len = buff.length();
					if (len > 3) {
						buff.replace(len - 2, len, "]");
					} else {
						buff.append("]");
					}
					label = buff.toString();
				}
				// default case
				else {
					String text = factbaseLabelProvider.getText(value);
					if (text != null) {
						label = text;
					} else {
						// Default is a String
						label = value.toString();
					}
				}
			}

			doneLabel = (label != null && label.equals(element.cachedLabel));
			doneImage = (img != null && img == element.cachedImage);
			element.cachedLabel = label;
			element.cachedImage = img;
			System.out.println("done element = "+element);
			System.out.println("done image = "+doneImage +" => "+img);
			System.out.println("done label = "+doneLabel +" => "+label);
			element.cachedLabel = label;
			element.cachedImage = img;

			if (doneLabel && doneImage) {
				cachedNodes.add(element);
			}

			return Status.OK_STATUS;
		}

//		public boolean contains(ISchedulingRule rule) {
//			return JQueryResultsLabelProvider.this.contains(rule) || (rule == this);
//		}
//
//		public boolean isConflicting(ISchedulingRule rule) {
//			return (rule == this);
//		}
	};

	private FactbaseLabelProvider factbaseLabelProvider;

	private Image queryNodeImage;

	private Map imageCachingJobs;

	private Map labelCachingJobs;

	private List cachedNodes;

	private Job sorterJob;
	
	private boolean showChildCount = false;

	/** 
	 * We need the viewer to refresh the view after we have the images from the query
	 * threads.
	 */
	private StructuredViewer viewer;

	/**
	 * Creates a new label provider with SHOW_DEFAULT flag.
	 */
	public JQueryResultsLabelProvider(StructuredViewer viewer) {
		super();
		factbaseLabelProvider = new FactbaseLabelProvider();
		this.viewer = viewer;

		imageCachingJobs = new HashMap();
		labelCachingJobs = new HashMap();
		cachedNodes = new ArrayList();

		ImageDescriptor desc = JQueryTreeBrowserPlugin.getImageDescriptor("QueryView.gif");
		queryNodeImage = new JavaElementImageDescriptor(desc, 0, JQueryAPI.getImageSize()).createImage();
	}

	/**
	 * Creates a new label provider.
	 */
	public JQueryResultsLabelProvider(StructuredViewer viewer, int flags) {
		super(flags);
		factbaseLabelProvider = new FactbaseLabelProvider(flags);
		this.viewer = viewer;

		imageCachingJobs = new HashMap();
		labelCachingJobs = new HashMap();
		cachedNodes = new ArrayList();

		ImageDescriptor desc = JQueryTreeBrowserPlugin.getImageDescriptor("QueryView.gif");
		queryNodeImage = new JavaElementImageDescriptor(desc, 0, JQueryAPI.getImageSize()).createImage();
	}

	@Override
	public void dispose() {
		queryNodeImage.dispose();

		if (sorterJob != null) {
			sorterJob.cancel();
		}

		flushCache();
	}

//	public boolean contains(ISchedulingRule rule) {
//		// This rule ensures that element label generation and updating is serialized
//		// it also won't allow updates when the database is updating
//		IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
//		return (rule == this || rule instanceof JQuerySchedulingRule || rule.equals(ruleFactory.buildRule()));
//	}
//
//	public boolean isConflicting(ISchedulingRule rule) {
//		return (rule == this);
//	}

	/**
	 * Returns the icon for the given object.
	 */
	@Override
	public Image getImage(Object element) {
		final ResultsTreeNode node = (ResultsTreeNode) element;
		Object value = node.getElement();

		if (element instanceof NoResultNode || element instanceof TemporaryResultNode) {
			Image x = JQueryAPI.getElementImage("String");
			return x;
		}

		if (value instanceof Object[]) {
			value = ((Object[]) value)[0];
		}

		if (node instanceof QueryNode) {
			if (node.cachedImage == null) {
				node.cachedImage = queryNodeImage;
				return queryNodeImage;
			} else {
				return node.cachedImage;
			}
		} else {
			Image img = node.cachedImage;

			if (img == null) {
				img = JQueryAPI.getElementImage("String");
			}

			// don't let the system do this job twice needlessly
			if (imageCachingJobs.get(node) == null && !cachedNodes.contains(node)) {
				Job j = new ImageUpdateJob("JQuery label provider", node, true);
				// force each query to execute in sequence because otherwise JTransformer seems to hang :(
				//j.setRule(JQueryAPI.getRule());
				j.setSystem(true);
				j.setPriority(Job.LONG);
				j.schedule();

				addCachingJob(imageCachingJobs, node, j);
			}

			return img;
		}

	}

	/**
	 * \ Returns the text label for the give object.
	 */
	@Override
	public String getText(Object element) {
		ResultsTreeNode node = (ResultsTreeNode) element;
		Object value = node.getElement();

		if (element instanceof NoResultNode || element instanceof TemporaryResultNode) {
			return (String) value;
		}

		if (value == null || node instanceof ResultsTreeRootNode) {
			return "";
		} else if (node instanceof QueryNode) {
			QueryNode qn = (QueryNode) node;
			String label = qn.getLabel();

			if (!qn.getQuery().getFilters().isEmpty()) {
				label = "(+) " + label;
			}

			if (showChildCount) {
				label = label + " (" + qn.getChildren().size() + ")";
			}
			
			return label;
		}

		String label = node.cachedLabel;
		if (label == null) {
			label = value.toString();
		}

		// don't let the system do this job twice needlessly
		if (labelCachingJobs.get(node) == null && !cachedNodes.contains(node)) {
			Job j = new ImageUpdateJob("JQuery label provider", node, false);
			// force each query to execute in sequence because otherwise JTransformer seems to hang :(
			//j.setRule(JQueryAPI.getRule());
			j.setSystem(true);
			j.setPriority(Job.INTERACTIVE);
			j.schedule();

			addCachingJob(labelCachingJobs, node, j);
		}

		return label;

		//		// Output arrays as lists
		//		else if (value instanceof Object[]) {
		//			Object a[] = (Object[]) value;
		//			StringBuffer buff = new StringBuffer("[");
		//			for (int i = 0; i < a.length; i++) {
		//				String temp = JQueryAPI.getElementLabel(a[i]);
		//				buff.append(temp + ", ");
		//			}
		//			int len = buff.length();
		//			if (len > 3) {
		//				buff.replace(len - 2, len, "]");
		//			} else {
		//				buff.append("]");
		//			}
		//
		//			node.label = buff.toString();
		//			return node.label;
		//		}
		//
		//		// default case
		//		else {
		//			String text = factbaseLabelProvider.getText(value);
		//			if (text != null) {
		//				node.label = text;
		//				return text;
		//			}
		//			// Default is a String
		//			node.label = value.toString();
		//			return node.label;
		//		}
	}

	@Override
	public void turnOff(int flags) {
		factbaseLabelProvider.turnOff(flags);
		super.turnOff(flags);
	}

	@Override
	public void turnOn(int flags) {
		factbaseLabelProvider.turnOn(flags);
		super.turnOn(flags);
	}

	public void flushCache() {
		// cancel current caching jobs (because we're going to redo them anyway)
		for (Iterator it = imageCachingJobs.values().iterator(); it.hasNext();) {
			Job j = (Job) it.next();
			// have to remove the element before modifying it or else we get a dreaded
			// concurrent modification exception
			it.remove();
			j.cancel();
		}

		for (Iterator it = labelCachingJobs.values().iterator(); it.hasNext();) {
			Job j = (Job) it.next();
			// have to remove the element before modifying it or else we get a dreaded
			// concurrent modification exception
			it.remove();
			j.cancel();
		}

		imageCachingJobs.clear();
		labelCachingJobs.clear();

		cachedNodes.clear();
	}

	private void addCachingJob(Map map, ResultsTreeNode key, Job value) {
		map.put(key, value);
	}

	private void removeCachingJob(Map map, ResultsTreeNode key) {
		map.remove(key);
	}

	public void handleEvent(JQueryEvent e) {
		if (e.getType().equals(JQueryEvent.EventType.Refresh)) {
			flushCache();
		}
	}

	public boolean showChildCount () {
		return showChildCount;
	}

	public void setShowChildCount (boolean showChildCount) {
		this.showChildCount = showChildCount;
	}
}
