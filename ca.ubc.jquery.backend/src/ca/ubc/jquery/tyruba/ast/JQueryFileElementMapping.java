package ca.ubc.jquery.tyruba.ast;

import tyRuBa.modes.TypeMapping;
import ca.ubc.jquery.api.JQueryFileElement;

public class JQueryFileElementMapping extends TypeMapping {

	@Override
	public Class getMappedClass() {
		return JQueryFileElement.class;
	}

	@Override
	public Object toJava(Object term) {
		Object[] parts = (Object[]) term;
		return new JQueryFileElement((String) parts[0], ((Integer) parts[1]).intValue(), ((Integer) parts[2]).intValue());
	}

	@Override
	public Object toTyRuBa(Object obj) {
		JQueryFileElement sl_obj = (JQueryFileElement) obj;
		return new Object[] { sl_obj.locationID, new Integer(sl_obj.start), new Integer(sl_obj.length) };
	}
}
