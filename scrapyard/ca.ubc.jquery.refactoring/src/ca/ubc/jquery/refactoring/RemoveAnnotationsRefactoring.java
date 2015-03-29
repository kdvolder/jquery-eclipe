package ca.ubc.jquery.refactoring;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEditGroup;

import ca.ubc.jquery.refactoring.RefactoringTargetSet.RefactoringTarget;

/**
 * @author awjb
 */
public class RemoveAnnotationsRefactoring extends QueryBasedRefactoring {
	public RemoveAnnotationsRefactoring(RefactoringTargetSet targets) {
		super(targets);
	}

	@Override
	public String getName() {
		return "Remove Annotations";
	}

	@Override
	protected TextEditGroup handleTargetNode (ASTNode foundNode, RefactoringTarget target, RewritingInfo rewritingInfo) throws JavaModelException {
		if (foundNode != null && foundNode instanceof Annotation) {
			ASTNode parent = foundNode.getParent();

			if (parent != null && parent instanceof BodyDeclaration) {
				Annotation annot = (Annotation)foundNode;
				BodyDeclaration bodyDecl = (BodyDeclaration) parent;
				ListRewrite typeDeclRewrite = rewritingInfo.getASTRewrite().getListRewrite(bodyDecl, bodyDecl.getModifiersProperty());
				TextEditGroup group = new TextEditGroup("Remove @" + annot.getTypeName());
				typeDeclRewrite.remove(annot, group);
				return group;
			} else {
				throw new IllegalStateException("Do not know how to handle annotation parent");
			}
		} else {
			throw new IllegalStateException("Element not found or not an annotation");
		}
	}
	
	@Override
	protected Class<Annotation> getMatchingNodesType () {
		return Annotation.class;
	}
}
