/*
 * Created on Nov 20, 2008
 */
package ca.ubc.jquery.api;

import tyRuBa.engine.FrontEnd;

/**
 * This interface is to be implemented by extenders of the
 * predicates extension point. It allows extenders to install their
 * own predicates.
 * @author gpothier
 */
public interface JQueryPredicateInstaller
{
	public void install(FrontEnd fe);
}
