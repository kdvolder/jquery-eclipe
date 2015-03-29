package ca.ubc.jquery.refactoring;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import ca.ubc.jquery.api.JQueryFileElement;

/**
 * AST visitor that traverses an AST looking for the node corresponding to a file location
 * from a JQuery query result node.
 * @author awjb
 */
class LocationFinderVisitor extends ASTVisitor {
	private JQueryFileElement fileElem;

	private ASTNode foundNode = null;
	private Class<? extends ASTNode> matchType;

	public LocationFinderVisitor(JQueryFileElement member, Class<? extends ASTNode> matchType) {
		this.fileElem = member;
		this.matchType = matchType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MarkerAnnotation)
	 */
	@Override
	public boolean visit(MarkerAnnotation node) {
		return !nodeMatches(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.NormalAnnotation)
	 */
	@Override
	public boolean visit(NormalAnnotation node) {
		return !nodeMatches(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.SingleMemberAnnotation)
	 */
	@Override
	public boolean visit(SingleMemberAnnotation node) {
		return !nodeMatches(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.MethodDeclaration)
	 */
	@Override
	public boolean visit(MethodDeclaration node) {
		return !nodeMatches(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.AnnotationTypeDeclaration)
	 */
	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		return !nodeMatches(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration)
	 */
	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		return !nodeMatches(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.EnumConstantDeclaration)
	 */
	@Override
	public boolean visit(EnumConstantDeclaration node) {
		return !nodeMatches(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.EnumDeclaration)
	 */
	@Override
	public boolean visit(EnumDeclaration node) {
		return !nodeMatches(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldDeclaration)
	 */
	@Override
	public boolean visit(FieldDeclaration node) {
		return !nodeMatches(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Initializer)
	 */
	@Override
	public boolean visit(Initializer node) {
		return !nodeMatches(node);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.TypeDeclaration)
	 */
	@Override
	public boolean visit(TypeDeclaration node) {
		return !nodeMatches(node);
	}
	
	public ASTNode getFoundNode() {
		return foundNode;
	}
	
	private boolean nodeMatches(ASTNode node) {
		if (matchType.isInstance(node)) {
			if ( isMatchingMethod(node) ||
			     isMatchingField(node) ||
			     isMatchingClass(node) ||
			     isMatchingAnnotation(node) ||
			     // XXX This feels kludgy.
			     (node.getStartPosition() == fileElem.getStart()) ) {
				foundNode = node;
				return true;
			}
		}
		return false;
	}
	
	private boolean isMatchingMethod (ASTNode node) {
		return (node instanceof MethodDeclaration && 
				((MethodDeclaration)node).getName().getStartPosition() == fileElem.getStart());
	}
	
	private boolean isMatchingField (ASTNode node) {
		if (node instanceof FieldDeclaration) {
			List varDecls = ((FieldDeclaration)node).fragments();
			
			// We do not support applying an annotation to a multi-variable field declaration
			// (like int x, y) because that one annotation would apply to all the variables
			// declared, which might not be what the user wants.
			if (varDecls.size() == 1) {
				VariableDeclarationFragment var = (VariableDeclarationFragment)varDecls.get(0);
				return var.getStartPosition() == fileElem.getStart();
			}
		}
		return false;
	}
	
	private boolean isMatchingClass (ASTNode node) {
		return (node instanceof TypeDeclaration && 
				((TypeDeclaration)node).getName().getStartPosition() == fileElem.getStart());
	}
	
	private boolean isMatchingAnnotation (ASTNode node) {
		return (node instanceof AnnotationTypeDeclaration && 
				((AnnotationTypeDeclaration)node).getName().getStartPosition() == fileElem.getStart());
	}

}

