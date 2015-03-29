package ca.ubc.jquery.api.tyruba;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.FunctorIdentifier;
import tyRuBa.engine.QueryEngine;
import tyRuBa.engine.RuleBase;
import tyRuBa.engine.RuleBaseBucket;
import tyRuBa.engine.TyRuBaConf;
import tyRuBa.engine.factbase.berkeley_db.BerkeleyDBConf;
import tyRuBa.engine.factbase.hashtable.FileBasedPersistenceConf;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.util.FileQueryLogger;
import ca.ubc.jquery.JQueryBackendPlugin;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryFactGenerator;
import ca.ubc.jquery.api.JQueryPredicateInstaller;
import ca.ubc.jquery.api.JQueryResourceManager;
import ca.ubc.jquery.api.JQueryResourceParser;
import ca.ubc.jquery.api.JQueryResourceStrategy;
import ca.ubc.jquery.preferences.JQueryTyrubaPreferencePage;
import ca.ubc.jquery.tyruba.ast.JQueryFileElementMapping;

/**
 * The rule base manager mangages the buckets and frontend for one working set.
 * It receives messages from the WorkingSetRuleBaseMapper, and adds,removes, or 
 * outdates buckets accordingly. The buckets themselves are responsible for the 
 * actual updates.  
 */
public class RuleBaseManager extends JQueryResourceManager {

	private IPath stateLoc;

	private IPath workingSetPath;

	private FrontEnd frontend;

	/** 
	 * The buckets for resources that were added directly by the user 
	 * (by including them in the working set) 
	 * */
	private Map coreBuckets;

	/**
	 * Buckets which are added by name (instead of by resource)
	 * This kind of bucket allows a more flexible structure that I hope will someday
	 * replace the resouce dependent ones.  For instance, you can create a bucket and 
	 * insert your own facts into it without actually having to parse a resource.  You
	 * could make a bucket for mouse clicks or something.  There are my ideas but they 
	 * have not been used or tested so we'll see what happens
	 * 
	 * @author lmarkle
	 */
	private Map namedBuckets;

	/**
	 * Buckets that are added automatically because dependencies on data
	 * in these buckets is discovered while updating coreBuckets. 
	 */
	private Map dependencyBuckets;

	private RuleBaseBucket rulesBucket;

	private Set resourceStrategies;

	// generate unique fact id stuff
	private int factCounter;

	private Map factIdMap;

	/**
	 * Constructor for RuleBaseManager
	 * @throws TypeModeError 
	 * @throws ParseException 
	 * @throws IOException 
	 */
	protected RuleBaseManager(String wsID, Set codeObjects, Set resourceStrategies, boolean useBDB) throws IOException, ParseException, TypeModeError {
		this.resourceStrategies = resourceStrategies;
		initialize(wsID, codeObjects, false, useBDB);
	}

	protected void initialize(String wsID, Set codeObjects, boolean clean, boolean useBDB) throws IOException, ParseException, TypeModeError {
		factCounter = 0;
		factIdMap = new HashMap();

		namedBuckets = new HashMap();
		coreBuckets = new HashMap();
		dependencyBuckets = new HashMap();

		if (frontend != null) {
			if (clean)
				frontend.crash(); // No need to be nice, since we will delete everything.
			else
				frontend.shutdown();
			frontend = null;
		}
		JQueryBackendPlugin plugin = JQueryBackendPlugin.getDefault();

		// path to plugin's state location folder
		stateLoc = plugin.getStateLocation();
		workingSetPath = stateLoc.append(wsID);
		File workingSetFolder = workingSetPath.toFile();
		if (clean) {
			tyRuBa.util.Files.deleteDirectory(workingSetFolder);
		} else if (!workingSetFolder.exists()) {
			workingSetFolder.mkdir();
		}

		restoreState();

		TyRuBaConf conf = new TyRuBaConf();
		conf.setDumpFacts(true);
		conf.setCleanStart(false);

		if (useBDB) {
			conf.setPersistenceConf(new BerkeleyDBConf());
		} else {
			conf.setPersistenceConf(new FileBasedPersistenceConf());
		}
		conf.setStoragePath(workingSetFolder);

		frontend = new FrontEnd(conf);
		RuleBase.autoUpdate = false;

		// set up the cache size in the frontend
		int cacheSize = plugin.getCacheSize();
		if (cacheSize > 0) {
			frontend.setCacheSize(cacheSize);
		} else {
			frontend.setCacheSize(5000);
		}

		loadCoreRules();

		// Run predicate installers provided by extenders
		for(JQueryPredicateInstaller installer : JQueryBackendPlugin.getPredicateInstallers()) {
			installer.install(frontend);
		}
		
		for (Iterator it = JQueryAPI.getDefinitionFiles().iterator(); it.hasNext();) {
			frontend.load(((File) it.next()).toURI().toURL());
		}

		JQueryTyrubaPreferencePage.createGlobalIncludeFile();

		// create buckets for rules and code
		makeRulesBucket();
		// force update rules buckets...
		// TODO: This really should be handled somewhere else
		frontend.updateBuckets();

		makeBuckets(codeObjects);

		// Must happen after making rules buckets
		frontend.addTypeMapping(new FunctorIdentifier("SourceLocation", 0), new JQueryFileElementMapping());
		//		frontend.addTypeMapping(new FunctorIdentifier("ClassSourceLocation", 0), new JQueryClassFileMapping());
	}

	private void loadCoreRules() {
		File coreFile = new File(JQueryBackendPlugin.getInstallPath().toString().concat("/core-rules/initcore.rub"));

		if (coreFile.exists() && coreFile.isFile()) {
			try {
				frontend.load(coreFile.toURI().toURL());
			} catch (Exception e) {
				JQueryBackendPlugin.error("Error loading rules file: " + coreFile + ":\n", e);
				throw new Error(e.getMessage());
			}
		}
	}

	protected int getFactId(String strRep) {
		Object rep = factIdMap.get(strRep);
		if (rep == null) {
			factIdMap.put(strRep, factCounter);
			return factCounter++;
		} else {
			return ((Integer) rep).intValue();
		}
	}

	private void makeBuckets(Set codeObjects) {
		for (Iterator iter = codeObjects.iterator(); iter.hasNext();) {
			IAdaptable element = (IAdaptable) iter.next();
			getOrCreateBucket(coreBuckets, element);
		}
		//		getTagBucket();
	}

	protected JQueryFactGenerator getResource(String name) {
		JQueryFactGenerator result = (JQueryFactGenerator) namedBuckets.get(name);
		if (result == null) {
			result = new TyRuBaFactBaseResource(frontend, name);

			namedBuckets.put(result, name);
		}

		return result;
	}

	protected JQueryFactGenerator getResource(IAdaptable resource) {
		return getOrCreateBucket(coreBuckets, resource);
	}

	/**
	 * Try to get an existing bucket and returns null if there is no existing bucket
	 */
	private JQueryFactGenerator getBucket(IAdaptable icu) {
		JQueryFactGenerator bucket = (JQueryFactGenerator) coreBuckets.get(icu);
		if (bucket == null) {
			bucket = (JQueryFactGenerator) dependencyBuckets.get(icu);
		}
		
		return bucket;
	}

	/**
	 * @author wannop
	 * Returns the bucket corresponding to the given IAdapter.  
	 * Creates a new bucket if one does not already exist.   
	 */
	private JQueryFactGenerator getOrCreateBucket(Map bucketMap, IAdaptable adaptable) {
		JQueryFactGenerator bucket = (JQueryFactGenerator) bucketMap.get(adaptable);
		if (bucket != null) {
			return bucket;
		}

		JQueryBackendPlugin.traceQueries("Creating bucket for : " + adaptable);
		for (Iterator iter = resourceStrategies.iterator(); iter.hasNext();) {
			JQueryResourceStrategy strat = (JQueryResourceStrategy) iter.next();
			if (strat.rightType(adaptable)) {
				JQueryResourceParser x = strat.makeParser(adaptable, this);
				bucket = new TyRuBaFactBaseResource(frontend, x.getName());
				((TyRuBaFactBaseResource) bucket).setParser(x);
				x.initialize(bucket);
				break;
			}
		}

		if (bucket == null) {
			JQueryBackendPlugin.traceQueries("RuleBaseManager.getBucket: invalid type for bucket creation: " + adaptable.getClass().getName());
			throw new Error("RuleBaseManager.getBucket: invalid type for bucket creation: " + adaptable.getClass().getName());
		}

		bucketMap.put(adaptable, bucket);

		return bucket;
	}

	private void makeRulesBucket() {
		rulesBucket = new RuleBaseBucket(frontend, null) {
			public void update() {
				// Load one core file for initializing user interface stuff... we have to load it here instead of in
				// the actual core because we can redefine predicates and erase their old return values.
				File coreFile = new File(JQueryBackendPlugin.getInstallPath().toString().concat("/core-rules/user-interface.rub"));
				File[] fileList = new File[] { coreFile, new File(JQueryBackendPlugin.getGlobalUserIncludeFile()), new File(getUserIncludeFile()) };

				for (int i = 0; i < fileList.length; i++) {
					File file = fileList[i];
					if (file.exists() && file.isFile()) {
						try {
							load(file.toURI().toURL());
						} catch (Exception e) {
							JQueryBackendPlugin.error("Error loading rules file: " + fileList[i] + ":\n", e);
							// Under some circumstances (e.g. unknown predicate in LabelProvider.rub), this was not getting caught
							// and ended up stopping Eclipse from starting.
							// throw new Error(e.getMessage());
						}
					}
				}
			}
		};

		if (JQueryBackendPlugin.isLoggingQueries()) {
			try {
				rulesBucket.setLogger(new FileQueryLogger(new File(workingSetPath.toOSString() + "_log_" + new Date().toString().replace(' ', '_') + ".qry"), false));
			} catch (IOException e) {
				JQueryBackendPlugin.error("Failed to create log file:", e);
			}
		}
	}

	/**
	 * Recreates the facts in the rule base.
	 */
	protected void reloadRulesFiles() {
		rulesBucket.setOutdated();
	}

	/** 
	 * Sets the bucket with the specified name outdated.	
	 * @author wannop
	 */
	private void setOutdated(IAdaptable icu) {
		JQueryFactGenerator bucket = getBucket(icu);
		if (bucket != null) {
			JQueryBackendPlugin.traceQueries("Set bucket outdated: " + icu);
			//			bucket.setOutdated();
			bucket.removeAll();
		} else
			JQueryBackendPlugin.traceQueries("Warning: setOutdated, unknown bucket " + icu);
	}

	/**
	 * Sets outdated the RuleBaseBuckets for the given Compilation Unit 
	 * @author wannop
	 */
	protected void fileChanged(IAdaptable icu) {
		setOutdated(icu);
	}

	protected void fileAdded(IAdaptable icu) {
		JQueryBackendPlugin.traceQueries("A CU has been added: " + icu);
		getOrCreateBucket(coreBuckets, icu);
		// TODO: remove from dependency buckets if needed?
	}

	/**
	 * This method should be called when a bucket for a resource discovers it has
	 * dependencies on another resource and wants to ensure the dependency resource
	 * is included in the factbase.
	 */
	public void dependencyFound(IAdaptable icu) {
		if (JQueryBackendPlugin.parseDependenciesEnabled()) {
			JQueryFactGenerator existingBucket = getBucket(icu);
			if (existingBucket == null) {
				getOrCreateBucket(dependencyBuckets, icu);
				JQueryBackendPlugin.traceQueries("CodeFactBucket.addDependencies: " + icu);
			}
		}
	}

	protected void fileRemoved(IAdaptable icu) {
		//remove the bucket from the collection 
		RuleBaseBucket bucket = (RuleBaseBucket) coreBuckets.remove(icu);
		if (bucket != null) {
			bucket.destroy();
			JQueryBackendPlugin.traceQueries("A CU has been Removed: " + icu);
		} else {
			JQueryBackendPlugin.traceQueries("Attempted to Remove CU: " + icu + ": Bucket does not exist. no change made");
		}
		//TODO: Implement dependency tracking so we may remove dependencyBuckets that
		// were added because of this Bucket.
	}

	/**
	 * 	Returns the QueryEngine that can be used to query this rulebase.
	 **/
	protected QueryEngine getRuleBase() {
		return rulesBucket;
	}

	/**
	 * Returns String rep of the path to the set-specific rules file
	 **/
	protected String getUserIncludeFile() {
		return workingSetPath.append("setSpecific.rub").toString();
	}

	/**
	 * Returns the set of IAdaptables that 
	 * have a corresponding RuleBaseBucket (ie: those that we currently have facts for)
	 * @return
	 */
	protected Set getCodeObjects() {
		return coreBuckets.keySet(); //TODO: should this merge core and dependencies?
	}

	protected void shutdown() {
		frontend.shutdown();
		frontend = null;

		// save fact info
		saveState();
	}

	private void restoreState() {
		File f = workingSetPath.append("factmap").toFile();
		if (f.exists()) {
			try {
				ObjectInputStream ois = new ObjectInputStream(new BufferedInputStream(new FileInputStream(f)));

				factCounter = ((Integer) ois.readObject()).intValue();
				factIdMap = (Map) ois.readObject();

				ois.close();
			} catch (Exception e) {
				throw new Error("Error occurred while reading connection file: " + e);
			}
		}
	}

	private void saveState() {
		File f = workingSetPath.append("factmap").toFile();
		try {
			ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(f)));

			oos.writeObject(factCounter);
			oos.writeObject(factIdMap);

			oos.close();
		} catch (IOException e) {
			throw new Error("Error occurred while writing file: " + e);
		}
	}

	/**
	 * @return Returns the frontend.
	 */
	protected FrontEnd getFrontend() {
		return frontend;
	}
}
