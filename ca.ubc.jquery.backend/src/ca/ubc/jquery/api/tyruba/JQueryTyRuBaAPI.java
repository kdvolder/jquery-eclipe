package ca.ubc.jquery.api.tyruba;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import tyRuBa.engine.QueryEngine;
import tyRuBa.engine.RBTerm;
import tyRuBa.modes.TypeModeError;
import tyRuBa.parser.ParseException;
import tyRuBa.tdbc.Connection;
import tyRuBa.tdbc.PreparedQuery;
import tyRuBa.tdbc.TyrubaException;
import ca.ubc.jquery.JQueryBackendPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryEvent;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryFactBase;
import ca.ubc.jquery.api.JQueryFileElement;
import ca.ubc.jquery.api.JQueryResourceStrategy;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;
import ca.ubc.jquery.api.JQueryUpdateTarget;

/**
 * A mininal API to allow user to plugin to JQuery and run queries against a TyRuBa 
 * database. This API may be extended in the future with additional functionality.
 * 
 * It's important to note that due to the way we bind variables in this backend, we cannot
 * have an input variable and free variable of the same name.  For instance: we cannot
 * have both !this and ?this in the same query.  Likewise we cannot have simple variables 
 * such as !o and ?o in the same query (though this won't throw an error, it won't give
 * meaningful results).  To truly understand why this restriction is in place, examine 
 * the implementation of {@link TyRuBaQuery#bindVariables()}
 * 
 * @author kdvolder
 * @author lmarkle
 */
public class JQueryTyRuBaAPI extends JQueryAPI {

	private static final boolean USE_BDB = false;

	protected WorkingSetFactBase factbase;

	//	private WorkingSetRuleBaseMapper ruleBaseMapper;

	/** Has to be a seperate field so that updates work properly */
	private Set resourceStrategies;

	public JQueryTyRuBaAPI() {
		resourceStrategies = new HashSet();
		_updateResources();

		factbase = initializeFactBase(resourceStrategies);
	}

	protected WorkingSetFactBase initializeFactBase(Set strategies) {
		return new WorkingSetFactBase(strategies, USE_BDB);
	}

	public static void postRefreshEvent(Object source) {
		postEvent(JQueryEvent.EventType.Refresh, source);
	}

	/**
	 * This is a hook method intended to allow someone to connect to JQuery's underlying TyRuBa queryengine.
	 * <p>
	 * To use this one needs to use the tyruba API in the tyRuBa.tdbc package. The best way to learn about using this API is by inspecting the JUnit tests in tyRuBa.tests.TDBCTest.
	 * <p>
	 * Note that the full source code (including the JUnit tests) for tyruba are included in the JQuery distribution in lib/tyRuBa_src.jar
	 * <p>
	 * 
	 * @param ws
	 *            A workingset indicating what TyRuBa DB one whishes to connect to. There generally is one TyRuBa query engine being created for each workingSet. These databases are created lazyly. That is, a database for a given workingset
	 *            is created the first time a user requests a database for it.
	 * @return a TyRuBa Connection. See the package tyRuBa.tdbc.
	 */
	protected static Connection getTyRuBaConnection() {
		JQueryTyRuBaAPI temp = (JQueryTyRuBaAPI) getInstance();
		QueryEngine queryEngine = temp.factbase.getRuleBaseManager().getRuleBase();
		return new Connection(queryEngine);
	}

	protected static QueryEngine getTyRuBaEngine() {
		JQueryTyRuBaAPI temp = (JQueryTyRuBaAPI) getInstance();
		QueryEngine queryEngine = temp.factbase.getRuleBaseManager().getRuleBase();
		return queryEngine;
	}

	//
	// public query api stuff
	//
	protected void _updateResources() {
		resourceStrategies.clear();
		for (Iterator it = resources.entrySet().iterator(); it.hasNext();) {
			JQueryResourceStrategy g = (JQueryResourceStrategy) ((Map.Entry) it.next()).getValue();
			resourceStrategies.add(g);
		}
	}

	protected JQueryFactBase _getFactBase() {
		return factbase;
	}

	protected JQueryFactBase _selectFactBase() {
		return factbase.selectionDialog();
	}

	protected JQueryResultSet _topLevelQuery() throws JQueryException {
		JQuery q = _createQuery("topQuery(?L,?Q,?V)");
		q.setChosenVars(new String[] { "?L", "?Q", "?V" });
		return q.execute();
	}

	protected JQueryResultSet _menuQuery(Object[] targets) throws JQueryException {
		if (targets == null) {
			targets = new Object[0];
		}
		JQuery query = _createQuery("menuItem(!targets,?P,?Q,?V)");
		query.bind("!targets", targets);
		query.setChosenVars(new String[] { "?P", "?Q", "?V" });
		return query.execute();
	}

	protected JQuery _queryPredicates() throws JQueryException {
		JQuery query = _createQuery("pasteSnippet(?Query,?ToolTip)");
		query.setChosenVars(new String[] { "?Query", "?ToolTip" });
		return query;
	}

	protected JQuery _filterQuery(Object[] targets) throws JQueryException {
		if (targets == null) {
			targets = new Object[0];
		}
		JQuery query = _createQuery("filterItem(!targets,?L,?F)");
		query.bind("!targets", targets);
		query.setChosenVars(new String[] { "?L", "?F" });
		return query;
	}

	protected JQuery _createQuery(String q) throws JQueryException {
		return new TyRuBaQuery(q);
	}

	protected String _getThisVar() {
		return "!this";
	}

	protected String _getIdentityQuery() {
		return "equals(!this,?T)";
	}

	protected Object _getFileElement(Object target) {
		QueryEngine queryEngine = getTyRuBaEngine();
		RBTerm result = queryEngine.getProperty(target, "sourceLocation");
		if (result != null) {
			return result.up();
		} else {
			return null;
		}
	}
	
	protected void _addRule(String rule) throws JQueryException {
		try {
			getTyRuBaEngine().parse(rule);
		} catch (ParseException e) {
			throw new JQueryTyRuBaException("TyRuBa parse error: " + e.getMessage(), e);
		} catch (TypeModeError e) {
			throw new JQueryTyRuBaException("TyRuBa type mode error: " + e.getMessage(), e);
		}
	}
 
	protected void _getElementFromFile(String fileName, int offset, int length, Set context, Set element) throws JQueryException {
		// compensate for >= and <=
		offset += 1;
		length -= 2;

		JQuery q = JQueryAPI.createQuery("reverseLocation(?C,?E,!file,!o,!l)");
		q.bind("!file", "/" + fileName);
		q.bind("!o", offset);
		q.bind("!l", length);

		JQueryResultSet rs = q.execute();
		while (rs.hasNext()) {
			JQueryResult r = rs.next();
			String type = (String) r.get("?C");
			if (("context").equals(type)) {
				context.add(r.get("?E"));
			} else {
				element.add(r.get("?E"));
			}
		}
	}

	protected String _getElementLabel(Object target) {
		return _getStringProperty(target, "label");
	}

	protected String _getElementType(Object target) {
		return _getStringProperty(target, "elementType");
	}

	protected Object _getObjectProperty(Object target, String propertyName) {
		Object result = null;
		JQueryResultSet rs = null;

		try {
//			Job.getJobManager().beginRule(JQueryAPI.getRule(), null);

			JQuery q = _createQuery(propertyName + "(!T,?T)");
			q.bind("!T", target);
			rs = q.execute();
			if (rs.hasNext()) {
				result = rs.next().get("?T");
			} else {
				result = null;
			}
			//			QueryEngine queryEngine = getTyRuBaEngine();
			//			result = queryEngine.getStringProperty(target, propertyName);
		} catch (JQueryException e) {
			JQueryBackendPlugin.error("getStringProperty(): ", e);
		} finally {
//			Job.getJobManager().endRule(JQueryAPI.getRule());
			if (rs != null) {
				rs.close();
			}
		}

		return result;
	}

	protected String _getStringProperty(Object target, String propertyName) {
		String s = (String) _getObjectProperty(target, propertyName);
		return s != null ? s : "null";
	}
	
	protected int _getIntProperty(Object target, String propertyName) {
		Number n = (Number) _getObjectProperty(target, propertyName);
		return n != null ? n.intValue() : 0;
	}

	@Override
	protected void _shutdown() {
		//		ruleBaseMapper.shutdown();
		factbase.shutdown();
	}

	private Object queryAndReturnFirst(String query, String var) throws JQueryException {
		Object result = null;
		JQueryResultSet temp = null;

		try {
			JQuery q = JQueryAPI.createQuery(query);
			temp = q.execute();

			if (temp.hasNext()) {
				result = temp.next().get(var);
			}
		} finally {
			if (temp != null) {
				temp.close();
			}
		}

		return result;
	}

	protected Object _getElementFromJavaModel(IJavaElement element) throws JQueryException {
		if (element instanceof IPackageFragment) {
			return queryAndReturnFirst("package(?P),name(?P," + element.getElementName() + ")", "?P");
		} else if (element instanceof IType) {
			return queryAndReturnFirst("type(?T),name(?T," + element.getElementName() + ")", "?T");
		} else if (element instanceof IMethod) {
			IMethod m = (IMethod) element;
			return queryAndReturnFirst("name(?C," + m.getDeclaringType().getElementName() + "),child(?C,?M),method(?M),name(?M," + m.getElementName() + ")", "?M");
		} else if (element instanceof IField) {
			IField f = (IField) element;
			return queryAndReturnFirst("name(?C," + f.getDeclaringType().getElementName() + "),child(?C,?F),field(?F),name(?F," + f.getElementName() + ")", "?F");
		} else {
			return null;
		}
	}

	protected IJavaElement _getJavaModelElement(Object target) throws JQueryException {
		IJavaElement result = null;
		String type = JQueryAPI.getElementType(target);

		// packages don't have source locations so we have to handle this differently
		if (("package").equals(type)) {
			try {
				String pName = JQueryAPI.getElementLabel(target);
				IJavaModel m = JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
				IJavaProject[] proj = m.getJavaProjects();
				for (int i = 0; i < proj.length; i++) {
					IPackageFragment[] frag = proj[i].getPackageFragments();
					for (int j = 0; j < frag.length; j++) {
						if (pName.equals(frag[j].getElementName())) {
							return frag[j];
						}
					}
				}
			} catch (JavaModelException e) {
				throw new JQueryTyRuBaException("Java model error: ", e);
			}
		}

		JQueryFileElement loc = JQueryAPI.getFileElement(target);
		if (loc == null) {
			return result;
		}

		IFile f = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(loc.locationID));
		IAdaptable a = f;
		if (f == null) {
			return null;
		}

		IJavaElement element = ((IJavaElement) a.getAdapter(IJavaElement.class));
		if (element == null) {
			return null;
		}

		IJavaProject p = element.getJavaProject();
		if (p == null) {
			return null;
		}

		try {
			if (("class").equals(type)) {
				result = p.findType(getPackage(target), JQueryAPI.getElementLabel(target));
			} else if (("interface").equals(type)) {
				result = p.findType(getPackage(target), JQueryAPI.getElementLabel(target));
			} else if (("field").equals(type)) {
				IType t = p.findType(getPackage(target), getParent(target));

				if (t != null) {
					IField[] m = t.getFields();
					String mName = JQueryAPI.getElementLabel(target);
					for (int i = 0; i < m.length; i++) {
						if (m[i].getElementName().equals(mName)) {
							result = m[i];
						}
					}
				}
			} else if (("method").equals(type) || ("constructor").equals(type)) {
				IType t = p.findType(getPackage(target), getParent(target));

				if (t != null) {
					IMethod[] m = t.getMethods();
					String mName = JQueryAPI.getElementLabel(target);
					for (int i = 0; i < m.length; i++) {
						if (getMethodName(m[i]).contains(mName)) {
							result = m[i];
						}
					}
				}
			} else if (("compilationUnit").equals(type)) {
				result = p.findElement(f.getProjectRelativePath().removeFirstSegments(1));
			} else {
				throw new JQueryTyRuBaException("Unkown type: " + type);
			}
		} catch (JavaModelException e) {
			throw new JQueryTyRuBaException("Java model error: ", e);
		}
		return result;
	}

	private static String getPackage(Object target) throws JQueryException {
		String result = null;
		JQueryResultSet rs = null;
		try {
			JQuery q = JQueryAPI.createQuery("package(!this,?P)");
			q.bind("!this", target);
			rs = q.execute();
			if (rs.hasNext()) {
				result = JQueryAPI.getElementLabel(rs.next().get("?P"));
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		return result;
	}

	private static String getParent(Object target) throws JQueryException {
		String result = null;
		JQueryResultSet rs = null;
		try {
			JQuery q = JQueryAPI.createQuery("child(?C,!this)");
			q.bind("!this", target);
			rs = q.execute();
			if (rs.hasNext()) {
				result = JQueryAPI.getElementLabel(rs.next().get("?C"));
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

		return result;
	}

	private static String getMethodName(IMethod m) throws JavaModelException {
		// so this is kind of "hack-ish" but it seems to work...
		String result = m.toString();
		result = result.substring(0, result.indexOf("["));
		// remove white spaces
		result = result.replaceAll("\\p{Space}", "");

		return result;
	}

//	public static String queryToProlog(String tyrubaQry) throws JQueryException {
//		try {
//			PreparedQuery qry = getTyRuBaConnection().prepareQuery(tyrubaQry);
//			return qry.toProlog();
//		} catch (TyrubaException e) {
//			throw new JQueryTyRuBaException("Converting query to prolog: ", e);
//		}
//	}

	protected JQueryUpdateTarget _createUpdateTarget(String targetName) {
		return new TyRuBaUpdateTarget(targetName);
	}
}
