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

/**
 * An experimental, probably incomplete refactoring for renaming a @Subject (or at least doing 
 * part of the renaming work in a simple case.)
 * @author awjb
 */
public class AddToSubjectRefactoring extends SubjectRefactoring {
	public AddToSubjectRefactoring(RefactoringTargetSet targets) {
		super(targets);
	}

	@Override
	public String getName() {
		return "Associate Code Element with Subject";
	}

	@Override
	protected TextEditGroup handleTargetNode (ASTNode foundNode, RefactoringTarget target, RewritingInfo rewritingInfo) throws JavaModelException {
		String subjectName = (String)target.get(1);
		SubjectAnnotationType targetAnnotationType = getTargetAnnotationType((String)target.get(2));
		
		AST ast = rewritingInfo.getASTCUNode().getAST();
		StringLiteral subjectNameLiteral = ast.newStringLiteral();
		subjectNameLiteral.setLiteralValue(subjectName);
		TextEditGroup group = new TextEditGroup(
				((targetAnnotationType == SubjectAnnotationType.SUBJECT) ? "Add" : "Export") 
				  + " element to subject " + subjectName);
		
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
								return createSubjectNameInsertionRewrite((ArrayInitializer)smAnnot.getValue(), 
										rewritingInfo, subjectNameLiteral, group);
							} else if (smAnnot.getValue() instanceof StringLiteral) {
								StringLiteral lit = (StringLiteral)smAnnot.getValue();
								if (lit.getLiteralValue().equals(subjectName)) {
									return null;
								} else {
									ArrayInitializer array = ast.newArrayInitializer();
									array.expressions().add(ASTNode.copySubtree(ast, lit));
									array.expressions().add(subjectNameLiteral);
									rewritingInfo.getASTRewrite().replace(lit, array, group);
									
									return group;
								}
							}
						} else if (annot.isNormalAnnotation()) {
							NormalAnnotation normalAnnot = (NormalAnnotation)annot;
							for (Object pairObj : normalAnnot.values()) {
								MemberValuePair pair = (MemberValuePair)pairObj;
								if (pair.getName().getIdentifier().equals("value") &&
									pair.getValue() instanceof ArrayInitializer) {
									return createSubjectNameInsertionRewrite((ArrayInitializer)pair.getValue(), 
											rewritingInfo, subjectNameLiteral, group);
								}
							}
						} else if (annot.isMarkerAnnotation()) {
							throw new IllegalStateException("Encountered marker SubjectJ annotation, which doesn't make sense");
						} else {
							throw new IllegalStateException("Encountered annotation of unknown type");
						}
					}
				}
			}
			
			SingleMemberAnnotation newAnnot = ast.newSingleMemberAnnotation();
			String nameToUse = rewritingInfo.getImportRewrite().addImport((targetAnnotationType == SubjectAnnotationType.SUBJECT) 
					? SUBJECT_ANNOTATION_NAME : EXPORT_ANNOTATION_NAME);
			newAnnot.setTypeName(ast.newName(nameToUse));
			ArrayInitializer array = ast.newArrayInitializer();
			array.expressions().add(subjectNameLiteral);
			newAnnot.setValue(array);
			ListRewrite rewrite = rewritingInfo.getASTRewrite().getListRewrite(bodyDecl, bodyDecl.getModifiersProperty());
			rewrite.insertFirst(newAnnot, group);
			return group;
		}
		
		// FIXME falls through for now
		throw new IllegalStateException("Element not found or not a BodyDeclaration");			
	}
	
	@Override
	protected Class<BodyDeclaration> getMatchingNodesType () {
		return BodyDeclaration.class;
	}
	
	private TextEditGroup createSubjectNameInsertionRewrite (ArrayInitializer array, 
			RewritingInfo rewritingInfo, StringLiteral subjectNameLiteral,
			TextEditGroup group) {
		for (Object expObj : array.expressions()) {
			if (((Expression)expObj).resolveConstantExpressionValue().equals(subjectNameLiteral.getLiteralValue())) {
				// node is already in this subject
				return null;
			}
		}

		ListRewrite rewrite = rewritingInfo.getASTRewrite().getListRewrite(array, ArrayInitializer.EXPRESSIONS_PROPERTY);
		rewrite.insertLast(subjectNameLiteral, group);
		return group;
	}
}
