package ca.ubc.jquery.refactoring;

import java.util.HashMap;

import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.text.edits.TextEditGroup;

import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.refactoring.RefactoringTargetSet.RefactoringTarget;

/**
 * @author awjb
 */
public class AddAnnotationsRefactoring extends QueryBasedRefactoring {
	private String currentAnnotFQName;
	private HashMap<Object, Annotation> annotCache = new HashMap<Object, Annotation>();
	
	public AddAnnotationsRefactoring(RefactoringTargetSet targets) {
		super(targets);
	}

	@Override
	public String getName() {
		return "Add Annotations";
	}

	@Override
	protected TextEditGroup handleTargetNode(ASTNode foundNode, RefactoringTarget target, RewritingInfo rewritingInfo) throws JavaModelException {
		Object annotationType = target.get(1);
		if (foundNode != null && foundNode instanceof BodyDeclaration) {
			BodyDeclaration bodyDecl = (BodyDeclaration)foundNode;
			ListRewrite typeDeclRewrite = rewritingInfo.getASTRewrite().getListRewrite(bodyDecl, bodyDecl.getModifiersProperty());
			Annotation annot = getAnnotation(annotationType, rewritingInfo);
			TextEditGroup group = new TextEditGroup("Add @" + annot.getTypeName() + " to " + JQueryAPI.getElementLabel(target.get(0)));
			typeDeclRewrite.insertFirst(annot, group);
			return group;
		} else {
			throw new IllegalStateException("Element not found or not a BodyDeclaration");			
		}
	}
	
	@Override
	protected Class<BodyDeclaration> getMatchingNodesType () {
		return BodyDeclaration.class;
	}
	
	private Annotation getAnnotation (Object obj, RewritingInfo rewritingInfo) {
		if (annotCache.containsKey(obj)) {
			return annotCache.get(obj);
		} else {
			Annotation annot = rewritingInfo.getASTCUNode().getAST().newMarkerAnnotation();
			currentAnnotFQName = JQueryAPI.getStringProperty(obj, "qname");
			String nameToUse = rewritingInfo.getImportRewrite().addImport(currentAnnotFQName);
			Name simpleName = rewritingInfo.getASTCUNode().getAST().newName(nameToUse);
			annot.setTypeName(simpleName);
			annotCache.put(obj, annot);
			return annot;
		}
	}
}
