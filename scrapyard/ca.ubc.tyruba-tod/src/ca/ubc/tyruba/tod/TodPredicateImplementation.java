package ca.ubc.tyruba.tod;

import java.util.ArrayList;

import tod.core.database.browser.IEventBrowser;
import tod.core.database.browser.ILogBrowser;
import tod.core.database.event.ILogEvent;
import tyRuBa.engine.Frame;
import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.PredicateIdentifier;
import tyRuBa.engine.RBComponent;
import tyRuBa.engine.RBCompoundTerm;
import tyRuBa.engine.RBContext;
import tyRuBa.engine.RBTerm;
import tyRuBa.engine.RBTuple;
import tyRuBa.engine.compilation.CompilationContext;
import tyRuBa.engine.compilation.Compiled;
import tyRuBa.engine.compilation.SemiDetCompiled;
import tyRuBa.modes.BindingList;
import tyRuBa.modes.Factory;
import tyRuBa.modes.Mode;
import tyRuBa.modes.ModeCheckContext;
import tyRuBa.modes.Multiplicity;
import tyRuBa.modes.PredInfoProvider;
import tyRuBa.modes.PredicateMode;
import tyRuBa.modes.TupleType;
import tyRuBa.modes.TypeModeError;
import tyRuBa.util.Action;
import tyRuBa.util.ElementSource;

public abstract class TodPredicateImplementation extends RBComponent {
	
	private PredicateMode mode;
	private int[] argIdxs;
	private int[] resultIdxs;
	private TodAttribute[] attributes = null;
		
	public void setAttributes(TodAttribute[] attributes) {
		this.attributes = attributes;
	}

	/** calculates result and calls addSolution to store the results */
	public final IEventBrowser doit(RBTuple args)  {
		Object[] objs = new Object[args.getNumSubterms()];
		for (int i = 0; i < objs.length; i++) {
			objs[i] = args.getSubterm(i).up();
		}
		return doit(objs);
	}
	
	public abstract IEventBrowser doit(Object[] args) ;
	
	public TodPredicateImplementation(String paramModesString, String modeString) {
		mode = Factory.makePredicateMode(paramModesString, modeString);
		BindingList bindings = mode.getParamModes();
		argIdxs = bindings.getBoundIndexes();
		resultIdxs = bindings.getFreeIndexes();
	}
		
	public PredicateMode getPredicateMode() {
		return mode;
	}

	public Mode getMode() {
		return getPredicateMode().getMode();
	}

	public BindingList getBindingList() {
		return getPredicateMode().getParamModes();
	}
	
	public PredicateIdentifier getPredId() {
		throw new Error("This should not happen");
	}
	
	public TupleType typecheck(PredInfoProvider predinfo) throws TypeModeError {
		throw new Error("This should not happen");
	}
	
	public RBComponent convertToMode(PredicateMode mode, ModeCheckContext context)
	throws TypeModeError {
		if (mode.equals(getPredicateMode())) {
			return this;
		} else {
			throw new Error("This should not happen");
		}
	}

	public IEventBrowser eval(RBContext rb, final Frame f, final Frame callFrame) {
//		solutions = new ArrayList();
//		RBTerm[] args = new RBTerm[getNumArgs()];
//		for (int i = 0; i < getNumArgs(); i++) {
//			args[i] = getArgAt(i).substitute(f);
//		}
//		IEventBrowser results = doit(args);
//		ArrayList results = new ArrayList();
//		for (int i = 0; i < solutions.size(); i++) {
//			Frame result = (Frame) f.clone();
//			RBTerm[] sols = (RBTerm[]) solutions.get(i);
//			for(int j = 0; j < sols.length; j++) {
//				result = getResultAt(j).substitute(result).unify(sols[j], result);
//				if (result == null) {
//					j = sols.length;
//				}
//			}
//			if (result != null) {
//				results.add(callFrame.callResult(result));
//			}
//		}
//		return results;
		return null;
	}
	
	public String toString() {
		return "Implementation in mode: " + mode;
	}
	
	public Compiled compile(CompilationContext c) {
		return new Compiled(getMode()) {
			public ElementSource runNonDet(Object _input, RBContext context) {
				RBTuple input = (RBTuple)_input;
				RBTuple args = input.project(argIdxs);
				final RBTuple results = input.project(resultIdxs);
				TodElementSource events = TodElementSource.make(doit(args));
				return events.map(new Action() {
					@Override
					public Object compute(Object arg) {
						ILogEvent event = (ILogEvent) arg;
						RBTerm[] elements = new RBTerm[resultIdxs.length];
						for (int i = 0; i < elements.length; i++) {
							elements[i] = RBCompoundTerm.makeJava(attributes[i].get(event));
						}
						RBTuple tup = FrontEnd.makeTuple(elements);
						return results.unify(tup, new Frame());
					}
				});
			}
		};
	}

	@Override
	public RBTuple getArgs() {
		return null; //TODO: is this ok? If so maybe getArgs is obsolote.
	}

}
