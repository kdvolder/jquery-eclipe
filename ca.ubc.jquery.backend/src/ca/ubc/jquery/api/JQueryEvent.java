package ca.ubc.jquery.api;

import java.util.EventObject;

public class JQueryEvent extends EventObject {
	public final static long serialVersionUID = 1L;

	public enum EventType {
		Refresh, TargetUpdate, RemoveUpdateTarget
	};

	private EventType type;

	private Object data;

	protected JQueryEvent(EventType type, Object source) {
		this(type, source, null);
	}

	protected JQueryEvent(EventType type, Object source, Object data) {
		super(source);
		this.type = type;
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	public EventType getType() {
		return type;
	}
}
