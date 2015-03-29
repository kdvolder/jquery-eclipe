package ca.ubc.jquery.api;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;

/**
 * An interface to communicate with the fact base.
 * 
 * @author lmarkle
 */
public interface JQueryFactBase {
	/** @return the fact base selected from a dialog */
	abstract public JQueryFactBase selectionDialog();

	/** @return gets the name of the current fact base */
	abstract public String getName();

	/**
	 * Sets the current fact base.
	 * 
	 * @throws JQueryException
	 *             if the fact base doesn't exist
	 */
	abstract public void setFactBaseByName(String name) throws JQueryException;

	/**
	 * Used to insert/remove facts from the factbase
	 * @param name name of resource
	 * @param strategy manager to manage this resource
	 */
	abstract public JQueryFactGenerator createResource(String name, JQueryResourceStrategy strategy);

	abstract public JQueryFactGenerator createResource(IAdaptable adaptable);

	/**
	 * Reloads all rules files associated with this fact base.
	 */
	abstract public void reloadRules();

	/**
	 * Reloads all facts in this fact base (in case something didn't refresh properly?)
	 */
	abstract public void reloadFacts();

	/**
	 * Adds the given project to this fact base
	 * @param project to add
	 */
	abstract public void addProject(IProject project);
}
