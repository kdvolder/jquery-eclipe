package ca.ubc.jquery.api.tyruba;

import java.util.Set;


public class TyRuBaBDB extends JQueryTyRuBaAPI {
	@Override
	protected WorkingSetFactBase initializeFactBase(Set strategies) {
		return new WorkingSetFactBase(strategies, true);
	}
}
