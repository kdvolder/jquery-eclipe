package ca.ubc.jquery.api.gluelog;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaElementImageDescriptor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;

import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryEvent;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryFactBase;
import ca.ubc.jquery.api.JQueryFileElement;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;
import ca.ubc.jquery.api.JQueryUpdateTarget;

/**
 * A mininal API to allow user to plugin to JQuery and run queries. NOTE: We're in testing stages and this API may be extended in the future with additional functionality.
 * 
 * @author lmarkle
 */
public class JQueryGlueLogAPI extends JQueryAPI {

	private GlueLogFactBase factbase;

	public JQueryGlueLogAPI() {
		factbase = new GlueLogFactBase();
		//		setImageSize(22, 16);
	}

//	// package level api ...
//	protected static PrologSession getPrologConnection() throws JQueryException {
//		PrologInterface pif = ((JQueryGlueLogAPI) getInstance()).factbase.current();
//		PrologSession session = null;
//		try {
//			session = pif.getSession();
//		} catch (PrologInterfaceException e) {
//			throw new JQueryJTransformerException(e.getMessage());
//		}
//
//		return session;
//	}
	
	// public api ...
	protected ImageDescriptor _getElementImageDescriptor(Object target) {
		
		// FIXME dummy descriptor for testing;
		ImageDescriptor img = new ImageDescriptor() {
			@Override
			public ImageData getImageData() {
				return new ImageData(16,16,16,new PaletteData(0,0,0));
			}
		};
		return img;
		
		
//		String baseImageName = null;
//		int adornmentFlags = 0;
//		JQueryResultSet temp = null;
//
//		try {
//			JQuery q = JQueryAPI.createQuery("adornmentFlags(T,F),baseImage(T,B)");
//			q.bind("T", target);
//			temp = q.execute();
//
//			if (temp.hasNext()) {
//				JQueryResult r = temp.next();
//				baseImageName = (String) r.get("B");
//				adornmentFlags = Integer.parseInt((String) r.get("F"));
//			}
//		} catch (JQueryException e) {
//			System.err.println(e);
//		} finally {
//			if (temp != null) {
//				temp.close();
//			}
//		}
//
//		ImageDescriptor base = getBaseImageDescriptor(baseImageName);
//		if (base == null) {
//			return null;
//		} else {
//			return new JavaElementImageDescriptor(base, adornmentFlags, getImageSize());
//		}
	}

	private ImageDescriptor getBaseImageDescriptor(String baseImageName) {
		// FIXME dummy descriptor for testing;
		ImageDescriptor img = new ImageDescriptor() {
			@Override
			public ImageData getImageData() {
				return new ImageData(16,16,16,new PaletteData(0,0,0));
			}
		};
		return img;
		
		
//		ImageDescriptor img = null;
//
//		if (baseImageName != null && !baseImageName.equals("null")) {
//			try {
//				Field imageField = imageClass.getField(baseImageName);
//				img = (ImageDescriptor) imageField.get(null);
//			} catch (Exception e) {
//				img = getImageDescriptor(baseImageName);
//
//				if (img == null) {
//						JQueryGlueLogBackendPlugin.getImageDescriptor(baseImageName);
//
//					if (img == null) {
//						System.err.println("ElementLabelProvider.getBaseImageDescriptor: no such image field or illegal field access: " + baseImageName);
//					}
//				}
//			}
//		}
//
//		return img;
	}

	protected JQueryResultSet _topLevelQuery() throws JQueryException {
//		JQuery q = _createQuery("topQuery(L,Q,V)");
//		q.setChosenVars(new String[] { "L", "Q", "V" });
//		return q.execute();
		
		// FIXME testing
		return new GlueLogQueryResultSet();
//		return new GlueLogQueryResultSet(new String[] {"L","Q","V","T","B","F"});
	}

	protected JQueryResultSet _menuQuery(Object[] targets) throws JQueryException {
		// menuItem(?this, labelString, queryString, [varsString0, varsString1, ...]) :- applicabilityExp).
//		StringBuilder b = new StringBuilder();
//		if (targets != null && targets.length > 0) {
//			for (int i = 0; i < targets.length; i++) {
//				b.append("menuItem(" + targets[i] + ",L,Q,V)");
//				if (i < targets.length - 1) {
//					b.append(",");
//				}
//			}
//		} else {
//			b.append("menuItem(_,L,Q,V)");
//		}
//
//		JQuery q = _createQuery(b.toString());
//		q.setChosenVars(new String[] { "L", "Q", "V" });
//		return q.execute();
		
		// FIXME testing
		// FIXME testing
		return new GlueLogQueryResultSet();
//		return new GlueLogQueryResultSet(new String[] {"L","Q","V","T"});
	}		

	protected JQuery _queryPredicates() throws JQueryException {
		JQuery q = _createQuery("simplePredicate(X,Y)");
		q.setChosenVars(new String[] { "Y", "X" });
		return q;
	}

	protected JQuery _filterQuery(Object[] targets) throws JQueryException {
		StringBuilder b = new StringBuilder();
		if (targets != null && targets.length > 0) {
			for (int i = 0; i < targets.length; i++) {
				b.append("filterItem(" + targets[i] + ",A,B)");
				if (i < targets.length - 1) {
					b.append(",");
				}
			}
		} else {
			b.append("filterItem(_,A,B)");
		}

		JQuery q = _createQuery(b.toString());
		q.setChosenVars(new String[] { "A", "B" });
		return q;
	}

	protected JQuery _createQuery(String q) throws JQueryException {
		return new GlueLogQuery(q, factbase);
	}

	protected String _getThisVar() {
		return "This";
	}

	protected String _getIdentityQuery() {
		return "This=T";
	}

	protected Object _getFileElement(Object target) {
		JQueryFileElement result = null;
		JQueryResultSet temp = null;

		try {
			JQuery q = JQueryAPI.createQuery("locationJQ(" + target + ",F,S,L)");
			temp = q.execute();
			if (temp.hasNext()) {
				// TODO Make this work for multiple results...
				JQueryResult r = temp.next();

				String f = (String) r.get("F");
				Integer s = new Integer((String) r.get("S"));
				Integer l = new Integer((String) r.get("L"));

				result = new JQueryFileElement(f, s.intValue(), l.intValue());
			}
		} catch (JQueryException e) {
			System.err.println(e);
		} finally {
			if (temp != null) {
				temp.close();
			}
		}

		return result;
	}

	protected void _getElementFromFile(String fileName, int offset, int length, Set context, Set element) throws JQueryException {
		List result = new ArrayList();

		// compensate for <= and >= matching
		offset = offset + 1;
		length = length - 2;

		JQuery q = JQueryAPI.createQuery("reverseLocation(T,E,'/" + fileName + "'," + offset + "," + length + ")");
		JQueryResultSet rs = null;
		try {
			rs = q.execute();
			while (rs.hasNext()) {
				JQueryResult r = rs.next();
				if (("context").equals(r.get("T"))) {
					context.add(r.get("E"));
				} else {
					element.add(r.get("E"));
				}
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}
	}

	protected String _getStringProperty(Object obj, String propertyName) {
		String result = null;
		JQueryResultSet temp = null;

		try {
			if (obj instanceof String) {
				try {
					Integer.parseInt((String) obj);
					JQuery q = JQueryAPI.createQuery(propertyName + "(" + obj + ",T)");
					temp = q.execute();
				} catch (NumberFormatException e) {
					JQuery q = JQueryAPI.createQuery(propertyName + "('" + obj + "',T)");
					temp = q.execute();
				}
			} else {
				JQuery q = JQueryAPI.createQuery(propertyName + "(" + obj + ",T)");
				temp = q.execute();
			}

			if (temp.hasNext()) {
				result = (String) temp.next().get("T");
			}
		} catch (JQueryException e) {
			System.err.println(e);
		} finally {
			if (temp != null) {
				temp.close();
			}
		}

		return result;
	}

	protected String _getElementType(Object target) {
		String result = null;
		JQueryResultSet temp = null;

		try {
			JQuery q = JQueryAPI.createQuery("elementType(" + target + ",R)");
			temp = q.execute();

			if (temp.hasNext()) {
				result = (String) temp.next().get("R");
			}
		} catch (JQueryException e) {
			System.err.println(e);
		} finally {
			if (temp != null) {
				temp.close();
			}
		}

		return result;
	}

	protected String _getElementLabel(Object target) {
//		String result = null;
//		result = _getStringProperty(target, "label");
//		return result;
		
		// FIXME testing; need something here for now to avoid endless errors when viewing results
		return target.toString();
	}

	protected int _getIntProperty(Object obj, String propertyName) {
		
		// FIXME testing
		return 0;
		
//		Integer result = new Integer(_getStringProperty(obj, propertyName));
//		return result.intValue();
	}

	private Object queryAndReturnFirst(String query, String var) throws JQueryException {
		String result = null;
		JQueryResultSet temp = null;

		try {
			JQuery q = JQueryAPI.createQuery(query);
			temp = q.execute();

			if (temp.hasNext()) {
				result = (String) temp.next().get(var);
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
			return queryAndReturnFirst("package(P),label(P," + element.getElementName() + ")", "P");
		} else if (element instanceof IType) {
			return queryAndReturnFirst("typeJQ(T),label(T," + element.getElementName() + ")", "T");
		} else if (element instanceof IMethod) {
			IMethod m = (IMethod) element;
			return queryAndReturnFirst("label(C," + m.getDeclaringType().getElementName() + "),methodName(M,_," + m.getElementName() + "),child(C,M)", "M");
		} else if (element instanceof IField) {
			IField f = (IField) element;
			return queryAndReturnFirst("label(C," + f.getDeclaringType().getElementName() + "),label(F," + f.getElementName() + ",child(C,F),field(F)", "F");
		} else {
			return null;
		}
	}

	protected IJavaElement _getJavaModelElement(Object target) throws JQueryException {
		IJavaElement result = null;

		String type = JQueryAPI.getElementType(target);
		String label = null;
		String packageName = null;
		String parentName = null;

		JQueryResultSet rs = null;
		try {
			JQuery q = JQueryAPI.createQuery("jq2jm(" + target + ",T,L,Pname,Cname)");
			rs = q.execute();
			if (rs.hasNext()) {
				JQueryResult r = rs.next();
				type = (String) r.get("T");
				label = (String) r.get("L");
				packageName = (String) r.get("Pname");
				parentName = (String) r.get("Cname");
			}
		} finally {
			if (rs != null) {
				rs.close();
			}
		}

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
				throw new JQueryGlueLogException("Java model error: ", e);
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

		if (packageName == null) {
			return null;
		}

		try {
			if (("class").equals(type)) {
				result = p.findType(packageName, label);
			} else if (("interface").equals(type)) {
				result = p.findType(packageName, label);
			} else if (("field").equals(type)) {
				IType t = p.findType(packageName, parentName);

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
				IType t = p.findType(packageName, parentName);

				if (t != null) {
					IMethod[] m = t.getMethods();
					String mName = label;
					for (int i = 0; i < m.length; i++) {
						if (getMethodName(m[i]).contains(mName)) {
							result = m[i];
						}
					}
				}
			} else if (("compilationUnit").equals(type)) {
				result = p.findElement(f.getProjectRelativePath().removeFirstSegments(1));
			} else {
				throw new JQueryGlueLogException("Unkown type: " + type);
			}
		} catch (JavaModelException e) {
			throw new JQueryGlueLogException("Java model error: ", e);
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

	protected JQueryFactBase _selectFactBase() throws JQueryException {
		JQueryFactBase t = factbase.selectionDialog();
		return t;
	}

	protected JQueryFactBase _getFactBase() {
		return factbase;
	}

	protected void _shutdown() {
		// does nothing
	}

	protected void _updateResources() {
		// does nothing... for now !!! muhuhahahaha... I need sleep :(
	}

	protected static void postRefreshEvent() {
		JQueryAPI.postEvent(JQueryEvent.EventType.Refresh, getInstance());
	}

	protected JQueryUpdateTarget _createUpdateTarget(String targetName) {
		return new GlueLogUpdateTarget(targetName);
	}

	@Override
	protected void _addRule(String rule) throws JQueryException {
		System.err.println("adding rule " + rule);
	}

//	@Override
//	protected JQueryResultSet _refactoringQuery(Object[] menuPath) throws JQueryException {
//		// TODO These should be implemented to actually make use of Alex's refactorings...
//		return null;
//	}

//	@Override
//	protected JQueryResultSet _topLevelRefactoring() throws JQueryException {
//		// TODO These should be implemented to actually make use of Alex's refactorings...
//		return null;
//	}
}
