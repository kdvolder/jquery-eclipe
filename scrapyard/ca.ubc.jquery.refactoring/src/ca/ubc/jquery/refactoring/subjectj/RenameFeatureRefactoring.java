package ca.ubc.jquery.refactoring.subjectj;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.text.edits.TextEditGroup;

import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResultSet;
import ca.ubc.jquery.refactoring.QueryBasedRefactoring;
import ca.ubc.jquery.refactoring.RefactoringTargetSet;
import ca.ubc.jquery.refactoring.RefactoringTargetSet.RefactoringTarget;

/**
 * An experimental, probably incomplete refactoring for renaming a @Feature (or at least doing 
 * part of the renaming work in a simple case.)
 * @author awjb
 */
@Deprecated
public class RenameFeatureRefactoring extends QueryBasedRefactoring {
	private String from = null;

	private String to = null;

	public RenameFeatureRefactoring(RefactoringTargetSet targets) {
		super(targets);

		if (!targets.isEmpty()) {
			RefactoringTarget target = (RefactoringTarget) targets.iterator().next();
			from = (String) target.get(1);
		}
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (pm != null) {
			pm.beginTask("", 1); //$NON-NLS-1$
			pm.worked(1);
			pm.done();
		}
		if (from != null && to != null && from.length() > 0 && to.length() > 0) {
			try {
				JQuery query = JQueryAPI.createQuery("feature(!featName)");
				query.bind("!featName", to);
				JQueryResultSet rs = query.execute();
				if (rs.hasNext()) {
					rs.close();
					return RefactoringStatus.createErrorStatus("Feature '" + to + "' already exists");
				}
				rs.close();
			} catch (JQueryException e) {
				return RefactoringStatus.createErrorStatus("Error occurred while performing validation check");
			}

			return super.checkFinalConditions(pm);
		} else {
			return RefactoringStatus.createErrorStatus("Both name of feature to refactor and new name for feature must be provided");
		}
	}

	@Override
	public String getName() {
		return "Rename Feature";
	}

	@Override
	protected TextEditGroup handleTargetNode(ASTNode foundNode, RefactoringTarget target, RewritingInfo rewritingInfo) throws JavaModelException {
		if (foundNode != null && foundNode instanceof NormalAnnotation) {
			NormalAnnotation annot = (NormalAnnotation) foundNode;
			// not very rigorous
			if (annot.getTypeName().getFullyQualifiedName().endsWith("Feature") || annot.getTypeName().getFullyQualifiedName().endsWith("Export")) {
				for (Object obj : annot.values()) {
					MemberValuePair pair = (MemberValuePair) obj;
					if (pair.getName().getIdentifier().equals("names") || pair.getName().getIdentifier().equals("to")) {
						if (pair.getValue() instanceof ArrayInitializer) {
							ArrayInitializer array = (ArrayInitializer) pair.getValue();
							for (Object o : array.expressions()) {
								if (o instanceof StringLiteral) {
									StringLiteral strLit = (StringLiteral) o;
									if (strLit.getLiteralValue().equals(from)) {
										TextEditGroup group = new TextEditGroup("Rename feature " + from + " to " + to);

										// Found it
										ListRewrite namesRewrite = rewritingInfo.getASTRewrite().getListRewrite(array, array.EXPRESSIONS_PROPERTY);
										// a bit inefficient to regenerate this every time...
										StringLiteral replacement = strLit.getAST().newStringLiteral();
										replacement.setLiteralValue(to);
										namesRewrite.replace(strLit, replacement, group);

										return group;
									}
								}
							}
						}
					}
				}
			}
		}

		throw new IllegalStateException("Could not find correct feature annotation to replace");
	}

	//	@Override
	//	public UserInputWizardPage getWizardPage() {
	//		return new RenameSubjectInputWizardPage(this);
	//	}

	@Override
	protected Class<NormalAnnotation> getMatchingNodesType() {
		return NormalAnnotation.class;
	}

	/**
	 * @param from the from to set
	 */
	public void setFrom(String from) {
		this.from = from;
	}

	/**
	 * @param to the to to set
	 */
	public void setTo(String to) {
		this.to = to;
	}

	/**
	 * @return the from
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @return the to
	 */
	public String getTo() {
		return to;
	}
}
