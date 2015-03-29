package ca.ubc.jquery.refactoring.subjectj;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.text.edits.TextEditGroup;

import ca.ubc.jquery.api.JQuery;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryResultSet;
import ca.ubc.jquery.refactoring.RefactoringTargetSet;
import ca.ubc.jquery.refactoring.RefactoringTargetSet.RefactoringTarget;

/**
 * An experimental, probably incomplete refactoring for renaming a @Subject (or at least doing 
 * part of the renaming work in a simple case.)
 * @author awjb
 */
public class RenameSubjectRefactoring extends SubjectRefactoring {
	private String from = null;

	private String to = null;

	public RenameSubjectRefactoring(RefactoringTargetSet targets) {
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
				JQuery query = JQueryAPI.createQuery("subject(!subjName)");
				query.bind("!subjName", to);
				JQueryResultSet rs = query.execute();
				if (rs.hasNext()) {
					rs.close();
					return RefactoringStatus.createErrorStatus("Subject '" + to + "' already exists");
				}
				rs.close();
			} catch (JQueryException e) {
				return RefactoringStatus.createErrorStatus("Error occurred while performing validation check");
			}

			return super.checkFinalConditions(pm);
		} else {
			return RefactoringStatus.createErrorStatus("Both name of subject to refactor and new name for subject must be provided");
		}
	}

	@Override
	public String getName() {
		return "Rename Subject";
	}

	@Override
	protected TextEditGroup handleTargetNode(ASTNode foundNode, RefactoringTarget target, RewritingInfo rewritingInfo) throws JavaModelException {
		if (foundNode != null && foundNode instanceof Annotation) {
			ArrayInitializer array = null;

			if (foundNode instanceof NormalAnnotation) {
				NormalAnnotation annot = (NormalAnnotation) foundNode;
				if (isSubjectAnnotation(annot) || isExportAnnotation(annot)) {
					for (Object obj : annot.values()) {
						MemberValuePair pair = (MemberValuePair) obj;
						if (pair.getName().getIdentifier().equals("value")) {
							if (pair.getValue() instanceof ArrayInitializer) {
								array = (ArrayInitializer) pair.getValue();
							}
						}
					}
				}
			} else if (foundNode instanceof SingleMemberAnnotation) {
				SingleMemberAnnotation annot = (SingleMemberAnnotation) foundNode;
				if (isSubjectAnnotation(annot) || isExportAnnotation(annot)) {
					if (annot.getValue() instanceof ArrayInitializer) {
						array = (ArrayInitializer) annot.getValue();
					} else if (annot.getValue() instanceof StringLiteral) {
						StringLiteral strLit = (StringLiteral) annot.getValue();
						if (strLit.getLiteralValue().equals(from)) {
							TextEditGroup group = new TextEditGroup("Rename subject " + from + " to " + to);

							// a bit inefficient to regenerate this every time...
							StringLiteral replacement = strLit.getAST().newStringLiteral();
							replacement.setLiteralValue(to);
							rewritingInfo.getASTRewrite().replace(strLit, replacement, group);

							return group;

						}
					}
				}

			}

			if (array != null) {
				for (Object o : array.expressions()) {
					if (o instanceof StringLiteral) {
						StringLiteral strLit = (StringLiteral) o;
						if (strLit.getLiteralValue().equals(from)) {
							TextEditGroup group = new TextEditGroup("Rename subject " + from + " to " + to);

							// Found it
							ListRewrite namesRewrite = rewritingInfo.getASTRewrite().getListRewrite(array, ArrayInitializer.EXPRESSIONS_PROPERTY);
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

		throw new IllegalStateException("Could not find correct subject annotation to replace");
	}

	@Override
	public UserInputWizardPage getWizardPage() {
		return new RenameSubjectInputWizardPage(this);
	}

	@Override
	protected Class<Annotation> getMatchingNodesType() {
		return Annotation.class;
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
