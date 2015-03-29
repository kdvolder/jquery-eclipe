package ca.ubc.jquery.tyruba.ast;

import ca.ubc.jquery.api.tyruba.JQueryClassFileElement;

public class JQueryClassFileMapping extends JQueryFileElementMapping {
	public static final long serialVersionUID = 1L;

	@Override
	public Class getMappedClass() {
		return JQueryClassFileElement.class;
	}

	@Override
	public Object toJava(Object term) {
		Object[] parts = (Object[]) term;
		return new JQueryClassFileElement((String) parts[0], ((Integer) parts[1]).intValue(), ((Integer) parts[2]).intValue());
	}

	@Override
	public Object toTyRuBa(Object obj) {
		JQueryClassFileElement sl_obj = (JQueryClassFileElement) obj;
		return new Object[] { sl_obj.locationID, new Integer(sl_obj.start), new Integer(sl_obj.length) };
	}

}
