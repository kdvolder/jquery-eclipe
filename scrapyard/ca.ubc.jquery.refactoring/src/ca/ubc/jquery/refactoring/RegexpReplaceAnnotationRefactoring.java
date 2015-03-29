package ca.ubc.jquery.refactoring;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.corext.dom.ASTFlattener;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.text.edits.TextEditGroup;

import ca.ubc.jquery.refactoring.RefactoringTargetSet.RefactoringTarget;

/**
 * @author awjb
 */
public class RegexpReplaceAnnotationRefactoring extends QueryBasedRefactoring {
	private String from = null;

	private String to = null;

	public RegexpReplaceAnnotationRefactoring(RefactoringTargetSet targets) {
		super(targets);
		// TODO: constructor that takes from
	}

	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (pm != null) {
			pm.beginTask("", 1); //$NON-NLS-1$
			pm.worked(1);
			pm.done();
		}
		if (from != null && to != null && from.length() > 0 && to.length() > 0) {
			return super.checkFinalConditions(pm);
		} else {
			return RefactoringStatus.createErrorStatus("Both string to find in annotation and replacement string must be provided");
		}
	}

	@Override
	public String getName() {
		return "Rewrite Annotation By Regular Expression";
	}

	@Override
	protected TextEditGroup handleTargetNode(ASTNode foundNode, RefactoringTarget target, RewritingInfo rewritingInfo) throws JavaModelException {
		if (foundNode != null && foundNode instanceof Annotation) {
			// ASTFlattener "discouraged", but using for now...
			ASTFlattener printer = new ASTFlattener();
			foundNode.accept(printer);
			String oldAnnotation = printer.getResult();
			String newAnnotation = oldAnnotation.replaceAll(from, to);
			if (!oldAnnotation.equals(newAnnotation)) {
				Annotation newAnnot = makeAnnotationFromString(newAnnotation);

				ASTNode parent = foundNode.getParent();

				if (parent != null && parent instanceof BodyDeclaration) {
					TextEditGroup group = new TextEditGroup("Perform replacement");
					BodyDeclaration bodyDecl = (BodyDeclaration) parent;
					ListRewrite typeDeclRewrite = rewritingInfo.getASTRewrite().getListRewrite(bodyDecl, bodyDecl.getModifiersProperty());
					typeDeclRewrite.replace(foundNode, newAnnot, group);
					return group;
				} else {
					throw new IllegalStateException("Do not know how to handle annotation parent");
				}
			}

			return null;
		} else {
			throw new IllegalStateException("Element not found or not an annotation");
		}
	}

	@Override
	public UserInputWizardPage getWizardPage() {
		return new RegexpReplaceAnnotationInputWizardPage(this);
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

	public Annotation makeAnnotationFromString(String annotString) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
		parser.setSource((annotString + " int x;").toCharArray());
		ASTNode node = parser.createAST(null);

		if (node.getNodeType() == ASTNode.TYPE_DECLARATION) {
			FieldDeclaration[] fields = ((TypeDeclaration) node).getFields();
			if (fields.length == 1) {
				List modifiers = fields[0].modifiers();
				if (modifiers.size() == 1 && modifiers.get(0) instanceof Annotation) {
					return (Annotation) modifiers.get(0);
				}
			}
		}

		throw new IllegalArgumentException(annotString + " is not a syntatically valid annotation");
	}
}
