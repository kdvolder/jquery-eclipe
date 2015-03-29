package ca.ubc.jquery.api;

import org.eclipse.core.runtime.IAdaptable;

abstract public class JQueryResourceManager {
	abstract public void dependencyFound(IAdaptable resource);

	abstract protected JQueryFactGenerator getResource(String name);
}
