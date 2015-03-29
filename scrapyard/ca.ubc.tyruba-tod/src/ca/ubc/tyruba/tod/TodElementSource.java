package ca.ubc.tyruba.tod;

import tod.core.database.browser.IEventBrowser;
import tyRuBa.util.ElementSource;
import tyRuBa.util.PrintingState;

public class TodElementSource extends ElementSource {

	private IEventBrowser events;

	public TodElementSource(IEventBrowser events) {
		this.events = events;
	}

	@Override
	public Object nextElement() {
		return events.next();
	}

	@Override
	public void print(PrintingState p) {
		p.print("TodElementSource("+events+")");
	}

	@Override
	public int status() {
		if (events.hasNext()) 
			return ELEMENT_READY;
		else 
			return NO_MORE_ELEMENTS;
	}

	public static TodElementSource make(IEventBrowser events) {
		return new TodElementSource(events);
	}

}
