package ca.ubc.tyruba.tod;

import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tyRuBa.modes.Type;

public abstract class TodAttribute {
	
	public static final TodAttribute a_timestamp = new TodAttribute("timestamp", Type.number) {
		@Override
		public Object get(ILogEvent event) {
			return event.getTimestamp();
		}
	};
	public static final TodAttribute a_behaviorId = new TodAttribute("behaviorId", Type.number) {
		@Override
		public Object get(ILogEvent event) {
			return ((IBehaviorCallEvent)event).getCalledBehavior().getId();
		}
	};
	public static final TodAttribute a_event = new TodAttribute("event", Type.object) {
		@Override
		public Object get(ILogEvent event) {
			return event;
		}
	};
	
	public final String name;
	public final Type type;
 	
	public TodAttribute(String name, Type type) {
		this.name = name;
		this.type = type;
	}
	
	public abstract Object get(ILogEvent event);	
}
