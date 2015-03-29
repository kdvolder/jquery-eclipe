/*
 * Created on Feb 4, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ca.ubc.jquery.tyruba.javadoc;

import tyRuBa.engine.RuleBaseBucket;

/**
 * @author dsjanzen
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public interface IJavadocTagProcessor {
	
	public void processTag(RuleBaseBucket bucket, Object element, String tagName, String tagValue);
	
}
