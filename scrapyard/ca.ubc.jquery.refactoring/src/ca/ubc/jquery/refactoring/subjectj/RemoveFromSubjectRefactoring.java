package ca.ubc.jquery.refactoring.subjectj;

import java.util.List;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEditGroup;

import ca.ubc.jquery.refactoring.RefactoringTargetSet;
import ca.ubc.jquery.refactoring.RefactoringTargetSet.RefactoringTarget;
import ca.ubc.jquery.refactoring.subjectj.SubjectRefactoring.SubjectAnnotationType;

/**
 * An experimental, probably incomplete refactoring for renaming a @Subject (or at least doing 
 * part of the renaming work in a simple case.)
 * @author awjb
 */
public class RemoveFromSubjectRefactoring extends SubjectRefactoring {
	public RemoveFromSubjectRefactoring(RefactoringTargetSet targets) {
		super(targets);
	}

	@Override
	public String getName() {
		return "Dissociate Code Element from Subject";
	}

	@Override
	protected TextEditGroup handleTargetNode (ASTNode foundNode, RefactoringTarget target, RewritingInfo rewritingInfo) throws JavaModelException {
		String subjectName = (String)target.get(1);
		SubjectAnnotationType targetAnnotationType = getTargetAnnotationType((String)target.get(2));
		
		AST ast = rewritingInfo.getASTCUNode().getAST();
		StringLiteral subjectNameLiteral = ast.newStringLiteral();
		subjectNameLiteral.setLiteralValue(subjectName);
		TextEditGroup group = new TextEditGroup(
				((targetAnnotationType == SubjectAnnotationType.SUBJECT) ? "Remove" : "Unexport") 
				+ " element from subject " + subjectName);
		
		if (foundNode != null && foundNode instanceof BodyDeclaration) {
			BodyDeclaration bodyDecl = (BodyDeclaration)foundNode;
			List modifiers = (List)bodyDecl.getStructuralProperty(bodyDecl.getModifiersProperty());
			for (Object modifier : modifiers) {
				ASTNode node = (ASTNode)modifier;
				if (node instanceof Annotation) {
					Annotation annot = (Annotation)node;
					if ((targetAnnotationType == SubjectAnnotationType.SUBJECT && isSubjectAnnotation(annot)) ||
						(targetAnnotationType == SubjectAnnotationType.EXPORT && isExportAnnotation(annot))) {
						if (annot.isSingleMemberAnnotation()) {
							SingleMemberAnnotation smAnnot = (SingleMemberAnnotation)annot;
							if (smAnnot.getValue() instanceof ArrayInitializer) {
								return createSubjectNameDeletionRewrite((ArrayInitializer)smAnnot.getValue(), 
										rewritingInfo, subjectName, annot, group);
							} else if (smAnnot.getValue() instanceof StringLiteral) {
								StringLiteral lit = (StringLiteral)smAnnot.getValue();
								if (lit.getLiteralValue().equals(subjectName)) {
									rewritingInfo.getASTRewrite().remove(annot, group);
									return group;
								}
								return null;
							}
						} else if (annot.isNormalAnnotation()) {
							NormalAnnotation normalAnnot = (NormalAnnotation)annot;
							for (Object pairObj : normalAnnot.values()) {
								MemberValuePair pair = (MemberValuePair)pairObj;
								if (pair.getName().getIdentifier().equals("value") &&
									pair.getValue() instanceof ArrayInitializer) {
									return createSubjectNameDeletionRewrite((ArrayInitializer)pair.getValue(), 
											rewritingInfo, subjectName, annot, group);
								}
							}
						} else if (annot.isMarkerAnnotation()) {
							throw new IllegalStateException("Encountered marker Subject annotation, which doesn't make sense");
						} else {
							throw new IllegalStateException("Encountered annotation of unknown type");
						}
					}
				}
			}
		}
		
		// FIXME falls through for now
		throw new IllegalStateException("Element not found or not a BodyDeclaration");			
	}
	
	@Override
	protected Class<BodyDeclaration> getMatchingNodesType () {
		return BodyDeclaration.class;
	}
	
	private TextEditGroup createSubjectNameDeletionRewrite (ArrayInitializer array, 
			RewritingInfo rewritingInfo, String subjectName, Annotation annot,
			TextEditGroup group) {
		for (Object expObj : array.expressions()) {
			Expression exp = (Expression)expObj;
			if (exp.resolveConstantExpressionValue().equals(subjectName)) {
				if (array.expressions().size() == 1) {
					// If list is empty, remove the whole annotation
					// TODO: clean up the import if we deleted the last use of the annotation?
					rewritingInfo.getASTRewrite().remove(annot, group);					
				} else {
					rewritingInfo.getASTRewrite().remove(exp, group);
				}
				return group;
			}
		}

		return null;
	}
}
