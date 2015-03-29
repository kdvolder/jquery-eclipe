package ca.ubc.jquery.gui.dialogs;

import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResult;
import ca.ubc.jquery.api.JQueryResultSet;

public class JQueryEditorContentProposer implements IContentProposalProvider {

	private String latest;

	public JQueryEditorContentProposer() {
	}

	public IContentProposal[] getProposals(String contents, int position) {
		latest = contents;
		return getQueryProposals(contents, position);
	}

	private class JQueryContentProposal implements IContentProposal {
		private String rule;

		private String toolTip;

		private int position;

		private int offset;

		public JQueryContentProposal(String r, String tt, int pos, int off) {
			rule = r;
			toolTip = tt;
			position = pos;
			offset = off;
		}

		public String getContent() {
			return latest.substring(0, position - offset) + rule + latest.substring(position);
		}

		public int getCursorPosition() {
			return position + rule.length() - offset;
		}

		public String getDescription() {
			return toolTip;
		}

		public String getLabel() {
			return rule;
		}

	}

	/**
	* Method createQueryMenuItems.
	* 
	* @param defQMenu
	*/
	protected JQueryContentProposal[] getQueryProposals(String input, int position) {
		// TODO: Some kind of language parser in the API?
		//
		// This really needs an actual parser to really help you write code.
		// Perhaps it's easy to extend an Eclipse one to do it?
		//
		// It needs to be a part of the API though because otherwise it won't work for
		// different languages.
		int p1, p2;

		p1 = input.lastIndexOf(";");
		p2 = input.lastIndexOf(",");

		if (p1 > p2) {
			// if we are editing after a ';', we're definitely searching for menu items
			input = input.substring(p1 + 1).trim();
			return getQueryMenuItems(input, position);
		} else {
			// here we're either searching for variables or menu items
			if (input.lastIndexOf(")") > input.lastIndexOf("(")) {
				// searching for menu items if we hit a ) more recently than a (
				input = input.substring(p2 + 1).trim();
				return getQueryMenuItems(input, position);
			} else {
				// searching for variables if we hit a ( before a )
				input = input.substring(p2 + 1).trim();
				return getQueryVariables(input, position);
			}
		}
	}

	private JQueryContentProposal[] getQueryVariables(String input, int position) {
		Vector result = new Vector();
		return (JQueryContentProposal[]) result.toArray(new JQueryContentProposal[result.size()]);
	}

	private JQueryContentProposal[] getQueryMenuItems(String input, int position) {
		Vector result = new Vector();

		SortedMap snippets = new TreeMap();
		JQueryResultSet results = null;

		try {
			JQuery q = JQueryAPI.queryPredicates();
			results = q.execute();

			while (results.hasNext()) {
				JQueryResult r = results.next();
				Object[] value = r.get();
				// get the query snippet
				String rule = (String) value[0];
				// get the tool tip label
				String toolTip = (String) value[1];

				if (rule.contains(input)) {
					snippets.put(rule, toolTip);
				}
			}

			for (Iterator iter = snippets.keySet().iterator(); iter.hasNext();) {

				String rule = (String) iter.next();
				String toolTip = (String) snippets.get(rule);

				result.add(new JQueryContentProposal(rule, toolTip, position, input.length()));
			}
		} catch (JQueryException e) {
			// this shouldn't happen unless rules are written incorrectly
			JQueryTreeBrowserPlugin.error("Editor content proposer: ", e);
		} finally {
			if (results != null) {
				results.close();
			}
		}

		return (JQueryContentProposal[]) result.toArray(new JQueryContentProposal[result.size()]);
	}
}
