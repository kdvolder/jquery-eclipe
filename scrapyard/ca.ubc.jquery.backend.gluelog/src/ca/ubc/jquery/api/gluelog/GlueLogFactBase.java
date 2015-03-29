package ca.ubc.jquery.api.gluelog;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import ca.ubc.gluelog.compiled.CompCompilationUnit;
import ca.ubc.gluelog.compiled.CompFactory;
import ca.ubc.gluelog.compiled.PredicateLookup;
import ca.ubc.gluelog.compiler.GLCompiler;
import ca.ubc.gluelog.parser.ParseException;
import ca.ubc.gluelog.runtime.GLRuntime;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryFactBase;
import ca.ubc.jquery.api.JQueryFactGenerator;
import ca.ubc.jquery.api.JQueryResourceStrategy;
import ca.ubc.jquery.gluelog.JQueryGlueLogBackendPlugin;

/**
 * An interface to communicate with the fact base.
 * 
 * @author lmarkle
 */
public class GlueLogFactBase implements JQueryFactBase {
	private String factbaseName;

//	private PrologInterface pif;
	
	private PredicateLookup lookup;
	private GLRuntime runtime;
	
	public PredicateLookup getLookupEnv() {
		return lookup;
	}
	
	public GLRuntime getRuntime() {
		return runtime;
	}
	
	protected GlueLogFactBase() {
		factbaseName = "EMPTY";

		setupRules();

		// Only here to notify views to update after JTransformer has finished generating facts.
		// If we ever get resource plugins working properly, we may be able to remove this.
		IResourceChangeListener buildListener = new IResourceChangeListener() {
			public void resourceChanged(IResourceChangeEvent event) {
				if (event.getBuildKind() == IncrementalProjectBuilder.AUTO_BUILD) {
					JQueryGlueLogAPI.postRefreshEvent();
				}
			}
		};

		ResourcesPlugin.getWorkspace().addResourceChangeListener(buildListener, IResourceChangeEvent.POST_BUILD);
		// testing: arbitrary facts and predicates
		GLCompiler compiler = new GLCompiler();
		try {
			CompCompilationUnit compiledFactBase = compiler.compileDefs(CompFactory.emptyDefs(),
					"append/3 MODES \n" +
					"  (B,B,F) \n" +
					"  (F,F,B) \n" +
					"  (F,B,B) \n" +
					"END \n" +
					"\n" +
					"append([], ?x, ?x).\n" +
			"append([?l | ?is], ?t, [?l | ?ist]) :- append(?is,?t,?ist).\n");
			lookup = compiledFactBase;
			runtime = compiler.makeRuntime();
			runtime.run(compiledFactBase);
		} catch (ParseException e) {
			// FIXME 
			System.err.println("error compiling/running arbitrary fact base");
			e.printStackTrace();
			lookup = CompFactory.emptyDefs();
		}

	}

//	protected PrologInterface current() {
//		return pif;
//	}

	/** @return the fact base selected from a dialog */
	public JQueryFactBase selectionDialog() {
		FactbaseSelectionDialog f = new FactbaseSelectionDialog(null);
		int result = f.open();

		if (result == Window.OK) {
			factbaseName = f.getSelected();
			setupRules();
		}

		return this;
	}

	/** @return gets the name of the current fact base */
	public String getName() {
		return factbaseName;
	}

	public void setFactBaseByName(String name) throws JQueryException {
		// TODO Do I want this? It seems to cause errors on startup...
		// PrologInterfaceRegistry reg = PrologRuntimePlugin.getDefault().getPrologInterfaceRegistry();
		// Set keys = reg.getAllKeys();
		// for (Iterator it = keys.iterator(); it.hasNext();) {
		// String key = (String) it.next();
		// if (key == name) {
		// factbaseName = name;
		// setupRules();
		// return;
		// }
		// }
		//
		// throw new JQueryJTransformerException("Fact base doesn't exist: " + name);
		factbaseName = name;
		setupRules();
	}

	public void addProject(IProject project) {
		// TODO implement me!
	}

	public JQueryFactGenerator createResource(String name, JQueryResourceStrategy strategy) {
		throw new Error("JTransformerFactBase.createResource(String,JQueryResourceManager) not implemented yet!");
	}

	public void reloadRules() {
		setupRules();
	}

	public void reloadFacts() {
		// TODO this should be possible... ??
		// FIXME using jquerybackendplugin.getShell for now for testing
		MessageBox m = new MessageBox(JQueryGlueLogBackendPlugin.getShell(), SWT.ICON_WARNING);
		
		m.setText("JQuery - JTransformer");
		m.setMessage("Unable to reload fact base from here.  Refer to JTransformer documentation to reload the fact base.");
		m.open();
	}

	private void setupRules() {
		// FIXME testing
		
//		pif = PrologRuntimePlugin.getDefault().getPrologInterface(factbaseName);
//
//		String rulesFile = JQueryGlueLogBackendPlugin.getDefault().getPluginPreferences().getString(JTransformerPreferencePage.P_RULES_LOCATION);
//		rulesFile = rulesFile.replace(File.separator.charAt(0), '/');
//
//		// add some rules
//		PrologSession s = null;
//		try {
//			s = pif.getSession();
//			s.queryOnce("consult( '" + rulesFile + "' )");
//		} catch (PrologInterfaceException e) {
//			JQueryGlueLogBackendPlugin.error("Error in JTransformerFactBase(): ", e);
//		} catch (PrologException e) {
//			JQueryGlueLogBackendPlugin.error("Prolog error: " + e);
//		} finally {
//			if (s != null) {
//				s.dispose();
//			}
//		}
	}

	public JQueryFactGenerator createResource(IAdaptable adaptable) {
		return new GlueLogFactGenerator();
	}
}
