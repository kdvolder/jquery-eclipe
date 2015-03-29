package ca.ubc.jquery.api;

public abstract class JQueryUpdateTarget {
	protected Filter createFilter(String name, String filter) {
		return new Filter(name, filter);
	}

	public class Filter {
		protected Filter(String name, String filter) {
			this.name = name;
			this.filter = filter;
		}

		public String name;

		public String filter;
	}

	private static int counter = 0;

	private String name;

	private int id;

	private Object target;

	abstract public Filter[] getFilters() throws JQueryException;

	protected JQueryUpdateTarget(String name) {
		this.name = name;
		target = null;

		id = counter;
		counter = counter + 1;
	}

	protected int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public void updateTarget(Object newTarget) {
		target = newTarget;
		updateTargetListeners(newTarget);
	}

	public Object getTarget() {
		return target;
	}

	private void updateTargetListeners(Object target) {
		JQueryEvent e = new JQueryEvent(JQueryEvent.EventType.TargetUpdate, this, target);
		JQueryAPI.postEvent(e);
	}
}
