package ca.ubc.jquery.gui;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;

import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.gui.results.QueryResultNode;

/** 
 * This class is supposed to be equivalent to the JQueryTreeViewer but it offers a table
 * view instead.  Unfortunately the code has not been used for a considerable amount of
 * time and it is unsure if it still works the way that it is supposed to.  
 * 
 * It will be worth maintaining this in the future and allowing JQuery to provide a 
 * standard set of widgets which a user can simple create and use.
 *  
 * @author lmarkle
 */
public class JQueryTableViewer extends TableViewer implements JQueryViewer {
	public JQueryTableViewer(Composite parent, int flags) {
		super(parent, flags);
	}

	public void cancelAllJobs() {

	}

	public ISelection getJMSelection() {
		ISelection s = super.getSelection();
		IStructuredSelection selection = (IStructuredSelection) s;
		List result = new ArrayList();

		for (Iterator it = selection.iterator(); it.hasNext();) {
			Object target = it.next();
			if (target instanceof QueryResultNode) {
				Object o = ((QueryResultNode) target).getElement();
				try {
					IJavaElement e = JQueryAPI.getJavaModelElement(o);
					if (e != null) {
						result.add(e);
					} else {
						result.add(o);
					}
				} catch (JQueryException e) {
					result.add(o);
				}
			}
		}

		return new StructuredSelection(result);
	}
}
