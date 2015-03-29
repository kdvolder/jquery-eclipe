package ca.ubc.jquery.gui;

import org.eclipse.swt.graphics.Image;

import ca.ubc.jquery.api.JQueryAPI;

/**
 * @author wannop
 */
public class FactbaseLabelProvider {

	private int fFlags;

	public FactbaseLabelProvider() {
		this(0);
	}

	public FactbaseLabelProvider(int flags) {
		fFlags = flags;
	}

	private boolean getFlag(int flag) {
		return (fFlags & flag) != 0;
	}

	/**
	 * Turns on the rendering options specified in the given flags.
	 * 
	 * @param flags
	 *            the options; a bitwise OR of <code>SHOW_* </code> constants
	 */
	protected void turnOn(int flags) {
		fFlags |= flags;
	}

	/**
	 * Turns off the rendering options specified in the given flags.
	 * 
	 * @param flags
	 *            the initial options; a bitwise OR of <code>SHOW_* </code> constants
	 */
	protected void turnOff(int flags) {
		fFlags &= (~flags);
	}

	protected String getText(Object element) {
		String labelString;
		labelString = JQueryAPI.getElementLabel(element);
		if (labelString.equals("null")) {
			return null;
		}

		// small optimization... only query methodOrField if we NEED to show type information
		if (getFlag(JQueryResultsLabelProvider.SHOW_RETURN_TYPE) || getFlag(JQueryResultsLabelProvider.SHOW_TYPE)) {
			String type = null;
			type = JQueryAPI.getElementType(element);
			if (getFlag(JQueryResultsLabelProvider.SHOW_RETURN_TYPE) && ("method").equals(type)) {
				labelString = labelString.concat(" : " + JQueryAPI.getStringProperty(element, "returnTypeName"));
			}
			if (getFlag(JQueryResultsLabelProvider.SHOW_TYPE) && ("field").equals(type)) {
				labelString = labelString.concat(" : " + JQueryAPI.getStringProperty(element, "fieldTypeName"));
			}
		}

		return labelString;
	}

	protected Image getImage(Object element) {
		return JQueryAPI.getElementImage(element);
	}
}