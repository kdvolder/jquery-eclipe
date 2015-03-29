/*
 * Created on Feb 4, 2004
 *
 */
package ca.ubc.jquery.tyruba.javadoc;

import tyRuBa.engine.RuleBaseBucket;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import ca.ubc.jquery.JQueryBackendPlugin;

/**
 * @author dsjanzen
 */
public class JQueryTagProcessor implements IJavadocTagProcessor {

	public void processTag(RuleBaseBucket bucket, Object element, String tagName, String tagValue) {

		try {
			bucket.parse(tagValue);
		} catch (ParseException e) {
			JQueryBackendPlugin.error(e.getMessage());
			return;
		} catch (TypeModeError e) {
			JQueryBackendPlugin.error(e.getMessage());
			return;
		}
	}
}
