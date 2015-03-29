package ca.ubc.jquery.api;

import org.eclipse.core.resources.IResourceRuleFactory;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.ISchedulingRule;

public class JQuerySchedulingRule implements ISchedulingRule {

	public boolean contains(ISchedulingRule rule) {
		IResourceRuleFactory ruleFactory = ResourcesPlugin.getWorkspace().getRuleFactory();
		return (rule instanceof JQuerySchedulingRule || rule.contains(ruleFactory.buildRule()));
	}

	public boolean isConflicting(ISchedulingRule rule) {
		return (rule instanceof JQuerySchedulingRule);
	}

}
