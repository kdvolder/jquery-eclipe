///*
// * Created on Mar 29, 2004
// */
//package ca.ubc.jquery.gui;
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
//import org.eclipse.jdt.core.IJavaElement;
//import org.eclipse.jface.action.IAction;
//import org.eclipse.jface.text.ITextSelection;
//import org.eclipse.jface.viewers.ISelection;
//import org.eclipse.jface.viewers.IStructuredSelection;
//import org.eclipse.swt.SWT;
//import org.eclipse.swt.widgets.MessageBox;
//import org.eclipse.ui.IEditorActionDelegate;
//import org.eclipse.ui.IEditorPart;
//import org.eclipse.ui.IObjectActionDelegate;
//import org.eclipse.ui.IWorkbench;
//import org.eclipse.ui.IWorkbenchPage;
//import org.eclipse.ui.IWorkbenchPart;
//import tyRuBa.engine.RBVariable;
//import tyRuBa.modes.TypeModeError;
//import tyRuBa.parser.ParseException;
//import ca.ubc.jquery.JQueryPlugin;
//import ca.ubc.jquery.gui.results.ResultsTreeNode;
//import ca.ubc.jquery.query.Query;
///**
// * @author andrew
// * 
// * This class handles the action to add the currently selected node to
// * the currently active JQuery query.
// * 
// * Type name: QueryHereAction Created: Mar 29, 2004
// */
//public class QueryHereAction
//        implements
//          IObjectActionDelegate,
//          IEditorActionDelegate {
//  /**
//   * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
//   *      org.eclipse.ui.IWorkbenchPart)
//   */
//  public void setActivePart(IAction action, IWorkbenchPart targetPart) {
//    init();
//  }
//  /**
//   * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
//   * 
//   * Execute a query based on the currently selected nodes in another
//   * view. Add the results to the active node, if there is one, or the
//   * root of the active query, if there is one, or create a new query.
//   * 
//   * The working set of the newly created query is the most recent
//   * working set.
//   * 
//   * If the nodes selected are not a part of the working set of the
//   * selected query, then the query will not find the values.
//   */
//  public void run(IAction action) {
//    if (!_error) {
//      JQueryPlugin.traceQueries("Querying from context menu.");
//      // get the active query control
//      QueryControl active = _view.getActiveQuery();
//      ResultsTreeNode queryNode = null;
//      // check to see if we must create our own active query
//      if (active == null) {
//        // create a new query node, prompting the user to choose a
//        // working set
//        MessageBox newQuery = new MessageBox(_view.getSite().getShell(), SWT.ICON_INFORMATION);
//        newQuery.setMessage("Before executing this query, you must select a working set to execute the query over.");
//        newQuery.open();
//        JQueryPlugin.traceQueries("Creating new query object for an editor initiated query.");
//        active = _view.doNewQueryAction(true);
//        if (active == null) {
//          return;
//        }
//      }  // (active == null)
//      // get active query node
//      queryNode = active.getCurrentNode();
//      // build up the query string
//      StringBuffer queryString = new StringBuffer();
//      for (Iterator iter = _currSel.iterator(); iter.hasNext();) {
//        // there should be a better way of doing this...we want to
//        // query for exactly these elements, and not just go by name
//        queryString.append("name(?N, ");
//        // object is a string if grabbed from java editor or
//        // IJavaElement
//        // if grabbed from browser.
//        Object element = iter.next();
//        if (element instanceof IJavaElement) {
//          queryString.append(((IJavaElement) element).getElementName());
//        } else {
//          queryString.append(element.toString());
//        }
//        queryString.append(")");
//        if (iter.hasNext()) {
//          queryString.append(";\n");
//        }
//      } // end for
//  
//      if (_currSel.size() > 0) {
//        // query name is name of first element in list
//        String queryName;
//        if (_currSel.get(0) instanceof IJavaElement) {
//          queryName = ((IJavaElement) _currSel.get(0)).getElementName();
//        } else {
//          queryName = _currSel.get(0).toString();
//        }
//        // create query
//        Query newQuery = new Query(queryNode, queryString.toString(), queryName);
//        // remove the "?this" variable
//        // we expect that the varList will only have 2 variables,
//        // but we do this anyway just in case
//        List varList = newQuery.getChosenVars();
//        RBVariable toRemove = null;
//        for (Iterator varIter = varList.iterator(); varIter.hasNext();) {
//          RBVariable element = (RBVariable) varIter.next();
//          if (element.name().equals("?this")) {
//            toRemove = element;
//            break;
//          }
//        }
//        varList.remove(toRemove);
//        try {
//          newQuery.setChosenVars(varList);
//        } catch (ParseException e) {
//          JQueryPlugin.traceQueries(e);
//        } catch (TypeModeError e) {
//          JQueryPlugin.traceQueries(e);
//        }
//        // execute query
//        active.createAndExecute(newQuery, queryNode);
//        // activate the jquery window
//        IWorkbench wb = JQueryPlugin.getDefault().getWorkbench();
//        IWorkbenchPage wbp = wb.getActiveWorkbenchWindow().getActivePage();
//        wbp.activate(_view);
//      } else {
//        // there were no elements selected.
//        MessageBox noQuery = new MessageBox(_view.getActiveQuery().getShell(), SWT.ICON_INFORMATION);
//        noQuery.setMessage("Cannot query an editor with nothing selected.  Please select a string or an " +
//            "object to search for.");
//        noQuery.open();
//        JQueryPlugin.traceQueries("Cannot perform a query from an editor with nothing selected.");
//      } // if (_currSel.size() > 0)
//      
//      
//    } else {
//        JQueryPlugin.traceQueries("Cannot perform query from context menu " +
//            "because of an error.");
//    }
//  } 
//  
//  
//  /**
//   * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
//   *      org.eclipse.jface.viewers.ISelection)
//   */
//  public void selectionChanged(IAction action, ISelection selection) {
//    if (selection != null) {
//      if (selection instanceof IStructuredSelection) {
//        // grabbed from a browser (eg- package, hierarchy, etc)
//        IStructuredSelection ss = (IStructuredSelection) selection;
//        _currSel = new ArrayList(ss.size());
//        for (Iterator selIter = ss.iterator(); selIter.hasNext();) {
//          Object element = selIter.next();
//          element.getClass().getName();
//          if (element instanceof IJavaElement) {
//            _currSel.add(element);
//          }
//        }
//      } else if (selection instanceof ITextSelection) {
//        // grabbed from an editor.
//        ITextSelection ts = (ITextSelection) selection;
//        // here is how we determine the element to search:
//        //  find start
//        //    1) if first char of selection is alphanumeric, then travel
//        // back to
//        //       find start of word
//        //    2) otherwise travel forward until first alphanumeric
//        //  find end
//        //    1) search until first non-alphanumeric is found.
//        // this is too difficult for me (andrew),
//        // for now just put in the text itself
//        _currSel = new ArrayList(1);
//        if (ts.getText().length() > 0) {
//          _currSel.add(ts.getText());
//        }
//      }
//    }
//  }
//  
//  
//  private void init() {
//    if (_view == null) {
//      IWorkbench wb = JQueryPlugin.getDefault().getWorkbench();
//      IWorkbenchPage wbp = wb.getActiveWorkbenchWindow().getActivePage();
//      if (wbp!=null)
//      	_view = (JQueryView) wbp.findView("ca.ubc.jquery.gui.JQueryView");
//      _error = (_view == null);
//    }
//  }
//  /**
//   * @see org.eclipse.ui.IEditorActionDelegate#setActiveEditor(org.eclipse.jface.action.IAction,
//   *      org.eclipse.ui.IEditorPart)
//   */
//  public void setActiveEditor(IAction action, IEditorPart targetEditor) {
//    init();
//  }
//  private JQueryView _view = null;
//  private boolean _error = false;
//  private List _currSel = null;
//}