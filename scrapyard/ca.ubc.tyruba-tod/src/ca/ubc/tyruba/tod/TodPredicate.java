package ca.ubc.tyruba.tod;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static ca.ubc.tyruba.tod.TodAttribute.*;

import junit.framework.Assert;
import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.IEventFilter;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.IBehaviorCallEvent;
import tod.core.database.event.ILogEvent;
import tod.core.database.structure.IBehaviorInfo;
import tyRuBa.engine.Frame;
import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.Implementation;
import tyRuBa.engine.ModedRuleBaseIndex;
import tyRuBa.engine.NativePredicate;
import tyRuBa.engine.PredicateIdentifier;
import tyRuBa.engine.QueryEngine;
import tyRuBa.engine.RBComponent;
import tyRuBa.engine.RBContext;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBTuple;
import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.modes.BindingList;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Mode;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.PredInfo;
import tyRuBa.modes.PredInfoProvider;
import tyRuBa.modes.PredicateMode;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.Type;
import tyRuBa.modes.TypeConstructor;
import tyRuBa.modes.TypeModeError;
import tyRuBa.util.Action;
import tyRuBa.util.ElementSource;
import tyRuBa.util.RegularExpression;

/** 
 * A NativePredicate is a predicate that uses java methods in its evaluation.
 * There is an Implementation for each predicate mode that this predicate can
 * be evaluated in. NativePredicates should disappear before they are inserted
 * into a Rulebase since they should have been "convertedToMode" into Implementations
 * before insertion.
 */
public class TodPredicate extends RBComponent {
	
	private static ILogBrowser theTodDatabase = null;
	
	public static void setTodDatabase(ILogBrowser todDatabase) {
		TodPredicate.theTodDatabase = todDatabase;
	}

	private PredInfo predinfo;
	private Map<BindingList,TodPredicateImplementation> implementations = new HashMap<BindingList, TodPredicateImplementation>();
	private PredicateIdentifier predId;
	private TodAttribute[] attributes;
	
	public Mode getMode() {
		return null; // Not yet converted, only specific Implementations have a mode
	}

	/** Constructor */	
	public TodPredicate(String name, TodAttribute... attributes) {
		this.attributes = attributes;
 		predId = new PredicateIdentifier(name, attributes.length);
		Type[] types = new Type[attributes.length];
		for (int i = 0; i < attributes.length; i++) {
			types[i] = attributes[i].type;
		}
		predinfo = Factory.makePredInfo(null, name, Factory.makeTupleType(types));
	}
	
	/** add a predicate mode to this native predicate along with the
	 *  implementation for this mode */
	public void addMode(TodPredicateImplementation imp) {
		predinfo.addPredicateMode(imp.getPredicateMode());
		implementations.put(imp.getBindingList(), imp);
		imp.setAttributes(attributes);
	}

	/** add this predicate to rb */
	public void addToRuleBase(ModedRuleBaseIndex rb) throws TypeModeError {
		rb.insert(predinfo);
		rb.insert(this);
	}

	public Compiled compile(CompilationContext c) {
		throw new Error("Compilation only works after this has been converted "
			+ "to a proper mode.");
	}

	public TupleType typecheck(PredInfoProvider predinfo) throws TypeModeError {
		return getPredType();
	}
	
	public TupleType getPredType() {
		return predinfo.getTypeList();
	}
	
	public PredicateIdentifier getPredId() {
		return predId; 
	}
	
	public RBTuple getArgs() {
		throw new Error("getArgs cannot be called until an implementation has been selected");
	}
	
	public RBComponent convertToMode(PredicateMode mode, ModeCheckContext context) 
	throws TypeModeError {
		BindingList targetBindingList = mode.getParamModes();
		TodPredicateImplementation result = implementations.get(targetBindingList);
		if (result==null) {
			for (TodPredicateImplementation candidate : implementations.values()) {
				//TODO: This code does not try to find the implementation that
				// has the least number of conversions from B to F
				if (targetBindingList.satisfyBinding(candidate.getBindingList())) {
					if (result == null 
							|| candidate.getMode().isBetterThan(result.getMode())) {
						result = candidate;
					}
				}
			}
		}
		if (result == null) {
			throw new TypeModeError("Cannot find an implementation for "
					+ getPredName() + " :: " + mode);
		} else {
			return result; 
		}
	}
	
	public String toString() {
		StringBuffer result = new StringBuffer(predinfo.getPredId().toString());
		for (Iterator<TodPredicateImplementation> iter = implementations.values().iterator(); iter.hasNext();) {
			TodPredicateImplementation element = iter.next();
			result.append("\n" + element);
		}
		return result.toString();
	}

	public static void defineBehaviorCall(ModedRuleBaseIndex qe) throws TypeModeError {
		TodPredicate behaviorCall =	new TodPredicate("behaviorCall", 
			new TodAttribute[] {
				a_timestamp, a_behaviorId, a_event } 
		);

		behaviorCall.addMode(new TodPredicateImplementation("FFF", "NONDET") {
			public IEventBrowser doit(Object[] args) {
				IEventFilter filter = theTodDatabase.createBehaviorCallFilter();
				return theTodDatabase.createBrowser(filter);
			}
		}); 

		behaviorCall.addMode(new TodPredicateImplementation("FBF", "NONDET") {
			public IEventBrowser doit(Object[] args) {
				int id = (Integer) args[0];
				IBehaviorInfo behavior = theTodDatabase.getStructureDatabase().getBehavior(id, false);
				IEventFilter filter = theTodDatabase.createBehaviorCallFilter(behavior);
				return theTodDatabase.createBrowser(filter);
			}
		}); 
		
		behaviorCall.addToRuleBase(qe);
	}
	
	public static void defineTodPredicates(QueryEngine qe) 
	throws TypeModeError {
		ModedRuleBaseIndex rules = qe.rulebase();
		defineBehaviorCall(rules);
	}

	
}
