/*
 * Created on Jan 29, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ca.ubc.jquery.tyruba.javadoc;

/**
 * @author dsjanzen
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class JavadocTag {

	private String name;
	private String value;
	

	public JavadocTag(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	protected void setValue(String value) {
		this.value = value;
	}

}
