package ca.ubc.jquery.markers.builder;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryFileElement;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;
import ca.ubc.jquery.markers.Activator;

/**
 * Builder which adds markers to files based on JQuery declareWarning/declareError facts.
 * Based on Eclipse sample code.
 * @author awjb
 */
public class JQueryMarkersBuilder extends IncrementalProjectBuilder {

	class JQueryMarkersDeltaVisitor implements IResourceDeltaVisitor {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
		 */
		public boolean visit(IResourceDelta delta) throws CoreException {
			IResource resource = delta.getResource();
			switch (delta.getKind()) {
			case IResourceDelta.ADDED:
				// handle added resource
				addJQueryMarkers(resource);
				break;
			case IResourceDelta.REMOVED:
				// handle removed resource
				break;
			case IResourceDelta.CHANGED:
				// handle changed resource
				addJQueryMarkers(resource);
				break;
			}
			//return true to continue visiting children.
			return true;
		}
	}

	class JQueryMarkersResourceVisitor implements IResourceVisitor {
		public boolean visit(IResource resource) throws CoreException {
			addJQueryMarkers(resource);
			//return true to continue visiting children.
			return true;
		}
	}

	public static final String BUILDER_ID = "ca.ubc.jquery.markers.jQueryMarkersBuilder";

	public static final String MARKER_TYPE = "ca.ubc.jquery.markers.jQueryDeclaredProblem";
	public static final String ANNOTATION_MARKER_TYPE = "ca.ubc.jquery.markers.jQueryAnnotationDeclaredProblem";
	public static final String TYRUBA_ERROR_MARKER_TYPE = "ca.ubc.jquery.markers.tyrubaProblem";
	
	private static final String JQUERY_RULES_QUERY = "annotation(?A),name(?A,JQueryRule),attribute(?A,value,?rule)";
	private static final String[] JQUERY_RULES_QUERY_CHOSENVARS = {"?rule", "?A"};	
	
	private static final String MARKER_RULES_QUERY = "((declareWarningLocation(?Loc,?Msg),equals(?Sev,warning));(declareErrorLocation(?Loc,?Msg),equals(?Sev,error))),equals(?Loc,SourceLocation<!file,?O,?L>)";
	private static final String[] MARKER_RULES_QUERY_CHOSENVARS = {"?Sev","?O","?L","?Msg"};
	
	private static final String MARKER_ANNOTATIONS_QUERY = "hasAnnotation(?E,?A),name(?A,DeclareMarker),attribute(?A,type,?MType),name(?MType,?Sev),attribute(?A,message,?Msg),attribute(?A,condition,?Cond),attribute(?A,target,?Target)";
	private static final String[] MARKER_ANNOTATIONS_QUERY_CHOSENVARS = {"?Sev","?Msg","?Cond","?Target","?E"};
	
	private void addMarker(String type, IFile file, String message, int start, int len,
			int severity) throws CoreException {
		IMarker marker = file.createMarker(type);
		marker.setAttribute(IMarker.MESSAGE, message);
		marker.setAttribute(IMarker.SEVERITY, severity);
		marker.setAttribute(IMarker.CHAR_START, start);
		marker.setAttribute(IMarker.CHAR_END, start+len);
		marker.setAttribute(IMarker.LINE_NUMBER, getLineNumber(file, start));
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.internal.events.InternalBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		getProject().deleteMarkers(ANNOTATION_MARKER_TYPE, false, IResource.DEPTH_INFINITE);
		getProject().deleteMarkers(TYRUBA_ERROR_MARKER_TYPE, false, IResource.DEPTH_INFINITE);
		
		loadAnnotRules();
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		} else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			} else {
				incrementalBuild(delta, monitor);
			}
		}
		addAnnotDeclaredMarkers();
		return null;
	}

	private void addJQueryMarkers(IResource resource) throws CoreException {
		if (resource instanceof IFile && resource.getName().endsWith(".java")) {
			IFile file = (IFile) resource;
			deleteMarkers(file);
			try {
				JQuery q = JQueryAPI.createQuery(MARKER_RULES_QUERY);
				q.bind("!file", resource.getFullPath().toString());
				q.setChosenVars(MARKER_RULES_QUERY_CHOSENVARS);
				JQueryResultSet rs = q.execute();
				while (rs.hasNext()) {
					JQueryResult result = rs.next();
					int offset = (Integer)result.get("?O");
					int length = (Integer)result.get("?L");
					String message = (String)result.get("?Msg");
					String severity = (String)result.get("?Sev");
					if (severity.equals("error")) {
						addMarker(MARKER_TYPE, file, message, offset, length, IMarker.SEVERITY_ERROR);
					} else {
						addMarker(MARKER_TYPE, file, message, offset, length, IMarker.SEVERITY_WARNING);						
					}
				}
			} catch (JQueryException e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 1 /* arbitrary */, "Query for JQuery marker rules failed: " + e.getMessage(), e));
			}
			
		}
	}

	private void loadAnnotRules () throws CoreException {
		try {			
			JQuery q = JQueryAPI.createQuery(JQUERY_RULES_QUERY);
			q.setChosenVars(JQUERY_RULES_QUERY_CHOSENVARS);
			JQueryResultSet rs = q.execute();
			
			if (rs.hasNext()) {
				JQueryAPI.getFactBase().reloadRules();

			while (rs.hasNext()) {
				JQueryResult result = rs.next();
				String rule = (String)result.get("?rule");
				try {
					JQueryAPI.addRule(rule);
				} catch (JQueryException e) {
					JQueryFileElement fe = JQueryAPI.getFileElement(result.get("?A"));
					addMarker(TYRUBA_ERROR_MARKER_TYPE, fe.getSourceFile(), e.getMessage(), fe.getStart(), 
							fe.getLength(), IMarker.SEVERITY_ERROR);
				}
			}
			}
		} catch (JQueryException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 1 /* arbitrary */, "Query for annotation-based JQuery rules failed: " + e.getMessage(), e));
		}
	}
	
	private void addAnnotDeclaredMarkers () throws CoreException {
		try {
//			JQuery q = JQueryAPI.createQuery("equals(?Loc,SourceLocation<?file,?,?>),sourceLocation(?E,?Loc),hasAnnotation(?E,?A),name(?A,DeclareMarker),attribute(?A,type,?MType),name(?MType,?Sev),attribute(?A,message,?Msg),attribute(?A,condition,?Cond),attribute(?A,target,?Target)");
//			q.setChosenVars(new String[] {"?Sev","?Msg","?Cond","?Target","?E","?file"});
			JQuery q = JQueryAPI.createQuery(MARKER_ANNOTATIONS_QUERY);
			q.setChosenVars(MARKER_ANNOTATIONS_QUERY_CHOSENVARS);
			JQueryResultSet rs = q.execute();
			while (rs.hasNext()) {
				JQueryResult result = rs.next();
				String severity = (String)result.get("?Sev");
				String message = (String)result.get("?Msg");
				String cond = (String)result.get("?Cond");
				String target = (String)result.get("?Target");
				// String sourceFile = (String)result.get("?file");
				Object codeElt = result.get("?E");

				try {
					JQuery condQ = JQueryAPI.createQuery(cond);
					condQ.bind(JQueryAPI.getThisVar(), codeElt);
					// condQ.addFilter("limit to current file", JQueryAPI.createQuery("equals(?Loc,SourceLocation<!file,?,?>),sourceLocation("+target+",?Loc)"), target, JQuery.NoPosition);
					condQ.setChosenVars(new String [] {target});
					JQueryResultSet condRS = condQ.execute();
					while (condRS.hasNext()) {
						JQueryResult condRes = condRS.next();
						Object targetElt = condRes.get(target);
						JQueryFileElement fe = JQueryAPI.getFileElement(targetElt);
						if (fe != null && fe.getSourceFile() != null) {
							addMarker(ANNOTATION_MARKER_TYPE, fe.getSourceFile(), message, fe.getStart(), fe.getLength(), 
									(severity.equals("ERROR") ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING));
						}
					}
				} catch (JQueryException e) {
					JQueryFileElement cfe = JQueryAPI.getFileElement(codeElt);
					addMarker(TYRUBA_ERROR_MARKER_TYPE, cfe.getSourceFile(), e.getMessage(), cfe.getStart(), 
							cfe.getLength(), IMarker.SEVERITY_ERROR);
				}
			}
		} catch (JQueryException e) {
			throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 1 /* arbitrary */, "Query for annotation-based JQuery markers failed: " + e.getMessage(), e));
		}
	}
	
	private int getLineNumber (IFile file, int offset) throws CoreException {
		try {
			LineNumberReader reader = new LineNumberReader(new InputStreamReader(file.getContents()));
			char[] cbuf = new char[offset];
			reader.read(cbuf, 0, offset);
			// LineNumberReader starts at 0, we want to start at 1
			return reader.getLineNumber() + 1;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.WARNING, Activator.PLUGIN_ID, 1 /* arbitrary */, "Failed to compute line number for file " + file.getFullPath().toOSString() + ", offset " + offset + ": " + e.getMessage(), e));
		}
	}

	private void deleteMarkers(IFile file) throws CoreException {
		file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
	}

	protected void fullBuild(final IProgressMonitor monitor)
			throws CoreException {
		getProject().accept(new JQueryMarkersResourceVisitor());
	}

	protected void incrementalBuild(IResourceDelta delta,
			IProgressMonitor monitor) throws CoreException {
		// the visitor does the work.
		delta.accept(new JQueryMarkersDeltaVisitor());
	}
}
