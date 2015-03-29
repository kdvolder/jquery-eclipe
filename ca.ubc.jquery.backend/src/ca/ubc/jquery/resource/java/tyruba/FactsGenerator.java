package ca.ubc.jquery.resource.java.tyruba;

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import annotations.Export;
import annotations.Feature;
import ca.ubc.jquery.JQueryBackendPlugin;
import ca.ubc.jquery.engine.tyruba.java.context.AnnotationAttributeContext;
import ca.ubc.jquery.engine.tyruba.java.context.AnnotationContext;
import ca.ubc.jquery.engine.tyruba.java.context.ContextTracker;
import ca.ubc.jquery.engine.tyruba.java.context.EnumerationContext;
import ca.ubc.jquery.engine.tyruba.java.context.FieldContext;
import ca.ubc.jquery.engine.tyruba.java.context.FieldDeclarationContext;
import ca.ubc.jquery.engine.tyruba.java.context.InitializerContext;
import ca.ubc.jquery.engine.tyruba.java.context.MethodContext;
import ca.ubc.jquery.engine.tyruba.java.context.TypeContext;

public @Feature(names = { "./annotations", "./NONE" })
class FactsGenerator extends ASTVisitor {

	//string used to set context property on ast nodes
	private @Export(to = { "./annotations" })
	String CONTEXT = "jquery.context";

	private @Export(to = { "./annotations" })
	ContextTracker context;

	public FactsGenerator(ContextTracker cuContext) {
		context = cuContext;
	}

	@Override
	@Feature(names = "./annotations")
	public boolean visit(MarkerAnnotation node) {
		context = new AnnotationContext(node, node.getStartPosition(), node.getLength(), context);
		//context.assertAccess(CodeFactBucket.READ, node.resolveTypeBinding(), node.getStartPosition(), node.getLength());
		node.setProperty(CONTEXT, context);
		return false; // No visit children
	}

	@Override
	@Feature(names = "./annotations")
	public void endVisit(MarkerAnnotation node) {
		ContextTracker nodeContext = (ContextTracker) node.getProperty(CONTEXT);
		if (nodeContext != null)
			context = nodeContext.exitContext();
	}

	@Override
	@Feature(names = "./annotations")
	public boolean visit(NormalAnnotation node) {
		context = new AnnotationContext(node, node.getStartPosition(), node.getLength(), context);
		node.setProperty(CONTEXT, context);
		List values = node.values();
		for (Object object : values) {
			MemberValuePair val = (MemberValuePair) object;
			val.accept(this);
		}
		return false; // No visit children
	}

	@Override
	@Feature(names = "./annotations")
	public void endVisit(NormalAnnotation node) {
		ContextTracker nodeContext = (ContextTracker) node.getProperty(CONTEXT);
		if (nodeContext != null)
			context = nodeContext.exitContext();
	}

	@Override
	@Feature(names = "./annotations")
	public boolean visit(SingleMemberAnnotation node) {
		ContextTracker annContext = new AnnotationContext(node, node.getStartPosition(), node.getLength(), context);
		// We set up an attribute context inside our annotation context to handle the single
		// contained attribute, which will always have the name "value" according to the Java spec.
		context = new AnnotationAttributeContext("value", annContext);
		node.setProperty(CONTEXT, context);
		node.getValue().accept(this);
		return false; // No visit children
	}

	@Override
	@Feature(names = "./annotations")
	public void endVisit(SingleMemberAnnotation node) {
		ContextTracker nodeContext = (ContextTracker) node.getProperty(CONTEXT);
		if (nodeContext != null)
			// Now we have to back out of two contexts at once because of the nesting described
			// above.
			context = nodeContext.exitContext().exitContext();
	}

	@Override
	@Feature(names = "./annotations")
	public boolean visit(MemberValuePair node) {
		context = new AnnotationAttributeContext(node.getName(), context);
		node.getValue().accept(this);
		return false;
	}

	@Override
	@Feature(names = "./annotations")
	public void endVisit(MemberValuePair node) {
		ContextTracker nodeContext = (ContextTracker) node.getProperty(CONTEXT);
		if (nodeContext != null)
			context = nodeContext.exitContext();
	}

	@Override
	@Feature(names = "./annotations")
	public boolean visit(StringLiteral node) {
		context.assertStringLiteral(node.getLiteralValue());
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.BooleanLiteral)
	 */
	@Override
	@Feature(names = "./annotations")
	public boolean visit(BooleanLiteral node) {
		// TODO Auto-generated method stub
		context.assertBooleanLiteral(node.booleanValue());
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.CharacterLiteral)
	 */
	@Override
	@Feature(names = "./annotations")
	public boolean visit(CharacterLiteral node) {
		// TODO Auto-generated method stub
		context.assertCharacterLiteral(node.charValue());
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.NumberLiteral)
	 */
	@Override
	@Feature(names = "./annotations")
	public boolean visit(NumberLiteral node) {
		context.assertNumericLiteral(node.getToken());
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(TypeDeclaration)
	 */
	@Override
	public boolean visit(TypeDeclaration node) {
		return visitType(node);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(AnnotationTypeDeclaration)
	 */
	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		return visitType(node);
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		EnumerationContext x = (EnumerationContext) context;
		x.assertField(node);

		return false;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		ITypeBinding binding = node.resolveBinding();
		if ((node.getFlags() & ASTNode.MALFORMED) != 0) {
			System.out.println("Type " + node.getName().toString() + " is malformed");
		}
		if (binding == null) {
			return false;
		}
		SimpleName typeName = node.getName();
		context = new EnumerationContext(binding, typeName.getStartPosition(), typeName.getLength(), node.getStartPosition(), node.getLength(), context);
		//		context = new TypeContext(binding,typeName.getStartPosition(),typeName.getLength(),node.getStartPosition(),node.getLength(),context);
		// mark node with context
		node.setProperty(CONTEXT, context);

		// assert element location
		context.assertElementLocation(node.getStartPosition(), node.getLength());

		for (Object o : node.modifiers()) {
			if (o instanceof Annotation) {
				((Annotation) o).accept(this);
			}
		}

		Javadoc jd = node.getJavadoc();
		if (jd != null) {
			jd.accept(this);
		}

		List bodyDecs = node.bodyDeclarations();
		for (Iterator iter = bodyDecs.iterator(); iter.hasNext();) {
			((BodyDeclaration) iter.next()).accept(this);
		}

		bodyDecs = node.enumConstants();
		for (Iterator iter = bodyDecs.iterator(); iter.hasNext();) {
			((EnumConstantDeclaration) iter.next()).accept(this);
		}
		return false;
	}

	public void endVisit(EnumDeclaration node) {
		endTypeVisit(node);
	}

	public boolean visitType(AbstractTypeDeclaration node) {
		ITypeBinding binding = node.resolveBinding();
		if ((node.getFlags() & ASTNode.MALFORMED) != 0) {
			System.out.println("Type " + node.getName().toString() + " is malformed");
		}
		if (binding == null) {
			return false;
		}
		SimpleName typeName = node.getName();
		context = new TypeContext(binding, typeName.getStartPosition(), typeName.getLength(), node.getStartPosition(), node.getLength(), context);
		// mark node with context
		node.setProperty(CONTEXT, context);

		// assert elementLocation
		context.assertElementLocation(node.getStartPosition(), node.getLength());

		for (Object o : node.modifiers()) {
			if (o instanceof Annotation) {
				((Annotation) o).accept(this);
			}
		}

		Javadoc jd = node.getJavadoc();
		if (jd != null) {
			jd.accept(this);
		}

		List bodyDecs = node.bodyDeclarations();
		for (Iterator iter = bodyDecs.iterator(); iter.hasNext();) {
			((BodyDeclaration) iter.next()).accept(this);
		}
		return false;
	}

	@Override
	public void endVisit(TypeDeclaration node) {
		endTypeVisit(node);
	}

	@Override
	public void endVisit(AnnotationTypeDeclaration node) {
		endTypeVisit(node);
	}

	public void endTypeVisit(AbstractTypeDeclaration node) {
		ContextTracker nodeContext = (ContextTracker) node.getProperty(CONTEXT);
		if (nodeContext != null)
			context = nodeContext.exitContext();
	}

	public boolean visit(MethodDeclaration node) {

		IMethodBinding binding = node.resolveBinding();
		if (binding == null)
			return false;

		SimpleName name = node.getName();
		context = new MethodContext(binding, name.getStartPosition(), name.getLength(), node.getStartPosition(), node.getLength(), context);

		for (Object o : node.modifiers()) {
			if (o instanceof Annotation)
				((Annotation) o).accept(this);
		}

		context.assertElementLocation(node.getStartPosition(), node.getLength());

		//mark node with context
		node.setProperty(CONTEXT, context);

		Javadoc jd = node.getJavadoc();
		if (jd != null) {
			jd.accept(this);
		}

		Block body = node.getBody();
		if (body != null)
			body.accept(this);

		return false;
	}

	public void endVisit(MethodDeclaration node) {
		ContextTracker nodeContext = (ContextTracker) node.getProperty(CONTEXT);
		if (nodeContext != null)
			context = nodeContext.exitContext();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration)
	 */
	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		IMethodBinding binding = node.resolveBinding();
		if (binding == null)
			return false;

		SimpleName name = node.getName();
		context = new MethodContext(binding, name.getStartPosition(), name.getLength(), node.getStartPosition(), node.getLength(), context);

		for (Object o : node.modifiers()) {
			if (o instanceof Annotation)
				((Annotation) o).accept(this);
		}

		// Record the default value; at the moment, we just convert it to a string.
		Expression defaultValue = node.getDefault();
		if (defaultValue != null) {
			// Get simplified constant expressions where possible.
			Object constVal = defaultValue.resolveConstantExpressionValue();
			if (constVal != null) {
				context.assertDefaultValue(constVal.toString());
			} else {
				context.assertDefaultValue(defaultValue.toString());
			}
			defaultValue.accept(this);
		}
		
		context.assertElementLocation(node.getStartPosition(), node.getLength());

		//mark node with context
		node.setProperty(CONTEXT, context);

		Javadoc jd = node.getJavadoc();
		if (jd != null) {
			jd.accept(this);
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration)
	 */
	@Override
	public void endVisit(AnnotationTypeMemberDeclaration node) {
		ContextTracker nodeContext = (ContextTracker) node.getProperty(CONTEXT);
		if (nodeContext != null)
			context = nodeContext.exitContext();
	}


	//	public boolean visit(ThrowStatement node) {
	//		Expression exp = node.getExpression();
	//		ITypeBinding thrown = null;
	//		switch (exp.getNodeType()) {
	//			case ASTNode.CLASS_INSTANCE_CREATION:
	//				thrown = ((ClassInstanceCreation) exp).resolveTypeBinding();
	//				break;
	//			default: JQueryPlugin.traceQueries("FactsGenerator.visit(ThrowStatement): encountered throw expression of type " + exp.getClass().getName());
	//		}
	//		if (thrown != null) {
	//			context.assertThrow(thrown, node.getStartPosition(), node.getLength());
	//		}
	//		return true;
	//	}
	//	

	public boolean visit(Assignment node) {
		//find writeAccess facts for lhs
		boolean visitChildren = false;
		Expression lhs = node.getLeftHandSide();

		SimpleName varName = null;
		switch (lhs.getNodeType()) {

		case ASTNode.FIELD_ACCESS:

			varName = ((FieldAccess) lhs).getName();
			Expression exp = ((FieldAccess) lhs).getExpression();
			exp.accept(this);
			break;

		case ASTNode.SUPER_FIELD_ACCESS:
			varName = ((SuperFieldAccess) lhs).getName();
			break;

		case ASTNode.SIMPLE_NAME:
			varName = (SimpleName) lhs;
			break;

		case ASTNode.QUALIFIED_NAME:
			varName = ((QualifiedName) lhs).getName();
			Name qualif = ((QualifiedName) lhs).getQualifier();
			if (qualif != null)
				qualif.accept(this);
			break;

		case ASTNode.ARRAY_ACCESS:
			//varName = (Array ((ArrayAccess) lhs).getArray()).	
			visitChildren = true;
			break;
		default:
			JQueryBackendPlugin.traceQueries("FactsGenerator visit(Assignment): encountered lhs node other than expected");
			visitChildren = true;
		}
		if (varName != null) {
			makeAccessFact(node, varName, CodeFactBucket.WRITE);
		}

		Expression rhs = node.getRightHandSide();
		if (rhs != null && !visitChildren)
			rhs.accept(this);

		return visitChildren;
	}

	/**@author wannop **/
	public boolean visit(PostfixExpression node) {
		boolean visitChildren = false;
		Expression operand = node.getOperand();
		SimpleName varName = null;
		switch (operand.getNodeType()) {

		case ASTNode.FIELD_ACCESS:
			varName = ((FieldAccess) operand).getName();
			Expression fAExp = ((FieldAccess) operand).getExpression();
			fAExp.accept(this);
			break;

		case ASTNode.SUPER_FIELD_ACCESS:
			varName = ((SuperFieldAccess) operand).getName();
			break;

		case ASTNode.SIMPLE_NAME:
			varName = (SimpleName) operand;
			break;

		case ASTNode.QUALIFIED_NAME:
			varName = ((QualifiedName) operand).getName();
			Name qNameQualif = ((QualifiedName) operand).getQualifier();
			if (qNameQualif != null) {
				qNameQualif.accept(this);
			}
			break;

		case ASTNode.ARRAY_ACCESS:
			//varName = (Array ((ArrayAccess) operand).getArray()).	
			visitChildren = true;
			break;
		default:
			JQueryBackendPlugin.traceQueries("FactsGenerator visit(PostfixExpression): encountered operand node other than expected");
			visitChildren = true;
		}
		if (varName != null) {
			//create write fact...
			makeAccessFact(node, varName, CodeFactBucket.WRITE);
			//and read fact
			if (node.getParent().getNodeType() != ASTNode.BLOCK)
				makeAccessFact(node, varName, CodeFactBucket.READ);
		}
		return visitChildren;

	}

	/**@author wannop **/
	public boolean visit(PrefixExpression node) {
		boolean visitChildren = false;
		Expression operand = node.getOperand();
		SimpleName varName = null;

		//only prefix operators "++" and "--" are writes
		String op = ((PrefixExpression.Operator) node.getOperator()).toString();
		if (op.equals("++") || op.equals("--")) {
			switch (operand.getNodeType()) {

			case ASTNode.FIELD_ACCESS:
				varName = ((FieldAccess) operand).getName();
				Expression fAExp = ((FieldAccess) operand).getExpression();
				fAExp.accept(this);
				break;

			case ASTNode.SUPER_FIELD_ACCESS:
				varName = ((SuperFieldAccess) operand).getName();
				break;

			case ASTNode.SIMPLE_NAME:
				varName = (SimpleName) operand;
				break;

			case ASTNode.QUALIFIED_NAME:
				varName = ((QualifiedName) operand).getName();
				Name qNameQualif = ((QualifiedName) operand).getQualifier();
				if (qNameQualif != null) {
					qNameQualif.accept(this);
				}
				break;

			case ASTNode.ARRAY_ACCESS:
				//varName = (Array ((ArrayAccess) operand).getArray()).	
				visitChildren = true;
				break;

			default:
				JQueryBackendPlugin.traceQueries("FactsGenerator visit(PrefixExpression): encountered operand node other than expected");
				visitChildren = true;
			}
			if (varName != null) {
				makeAccessFact(node, varName, CodeFactBucket.WRITE);
				if (node.getParent().getNodeType() != ASTNode.BLOCK) {
					makeAccessFact(node, varName, CodeFactBucket.READ);
				}
			}
		} else
			visitChildren = true;

		return visitChildren;
	}

	private void makeAccessFact(ASTNode accessNode, SimpleName accessedVarName, String accessType) {
		IBinding maybeAccessedVariable = accessedVarName.resolveBinding();

		if (maybeAccessedVariable == null) {
			JQueryBackendPlugin.traceQueries("FactsGenerator.makeAccessFact: encountered null variable binding");
		} else if (maybeAccessedVariable.getKind() != IBinding.VARIABLE) {
			//			JQueryBackendPlugin.traceQueries("FactsGenerator.makeAccessFact: encountered SIMPLE_NAME other than variable : " + accessedVarName);
			return;
		} else {
			IVariableBinding accessedVariable = (IVariableBinding) maybeAccessedVariable;
			//String varIdent = accessedVar.getIdentifier();
			//String srString = " " + accessType + " ";

			if (accessedVariable.isField()) { //don't care about local variables
				ITypeBinding declaringType = accessedVariable.getDeclaringClass();
				//occurs with length field of array type
				if (declaringType != null) {
					context.assertAccess(accessType, accessedVariable, accessNode.getStartPosition(), accessNode.getLength());
				}
			}
		}
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(MethodInvocation)
	 */
	public boolean visit(MethodInvocation node) {
		SimpleName invokedMethodName = node.getName();
		IMethodBinding invokedMethodBinding = (IMethodBinding) invokedMethodName.resolveBinding();
		makeCallFact(CodeFactBucket.METHOD_CALL, node, invokedMethodBinding);
		Expression exp = node.getExpression();
		if (exp != null)
			exp.accept(this);
		List args = node.arguments();
		for (Iterator iter = args.iterator(); iter.hasNext();) {
			ASTNode element = (ASTNode) iter.next();
			element.accept(this);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(ClassInstanceCreation)
	 */
	public boolean visit(ClassInstanceCreation node) {
		//		if ((node.getFlags() & ASTNode.MALFORMED) > 0) {
		//could insert malformed facts in this case.  If so, we should do for each node visited.	
		//		}

		IMethodBinding constructorBinding = node.resolveConstructorBinding();
		makeCallFact(CodeFactBucket.CONSTRUCTOR_CALL, node, constructorBinding);
		if (constructorBinding != null && constructorBinding.isSynthetic()) {
			//I've never seen one of these... I think they're a myth ;)
			JQueryBackendPlugin.traceQueries("SYNTHETIC CONSTRUCTOR ENCOUNTERED: " + constructorBinding.getName());
		}
		Expression exp = node.getExpression();
		if (exp != null)
			exp.accept(this);

		List args = node.arguments();
		for (Iterator iter = args.iterator(); iter.hasNext();) {
			ASTNode element = (ASTNode) iter.next();
			element.accept(this);
		}

		AnonymousClassDeclaration anonClass = node.getAnonymousClassDeclaration();
		if (anonClass != null) {
			anonClass.accept(this);
		}
		return false;
	}

	public boolean visit(InstanceofExpression node) {
		node.getLeftOperand().accept(this);
		ITypeBinding instanceOfWhat = node.getRightOperand().resolveBinding();
		if (instanceOfWhat != null) {
			context.assertInstanceOf(instanceOfWhat, node.getStartPosition(), node.getLength());
		} else {
			JQueryBackendPlugin.traceQueries("FactsGenerator.visit(InstanceofExpression: encountered null Type binding");
		}
		return false;
	}

	@Override
	public boolean visit(CastExpression node) {
		node.getExpression().accept(this);
		ITypeBinding castToWhat = node.getType().resolveBinding();
		if (castToWhat != null) {
			context.assertTypeCast(castToWhat, node.getStartPosition(), node.getLength());
		} else {
			JQueryBackendPlugin.traceQueries("FactsGenerator.visit(CastExpression: encountered null Type binding");
		}
		return false;
	}

	public boolean visit(AnonymousClassDeclaration node) {
		ITypeBinding aClassBinding = node.resolveBinding();
		if (aClassBinding == null) {
			return false;
		}
		context = new TypeContext(aClassBinding, node.getStartPosition(), node.getLength(), node.getStartPosition(), node.getLength(), context);
		//mark node with context
		node.setProperty(CONTEXT, context);

		//		Object methodRep = factMaker.getRepresentation(constructorBinding);
		//		context.assertMethod(methodRep, anonClass.getStartPosition(), 0);
		//		factMaker.assertChild(currTypeRep, methodRep);
		//		factMaker.assertConstructor(methodRep);

		// assert elementLocation
		context.assertElementLocation(node.getStartPosition(), node.getLength());

		//visit body declarations	
		List anonClassBodyDecs = node.bodyDeclarations();
		for (Iterator iter = anonClassBodyDecs.iterator(); iter.hasNext();) {
			BodyDeclaration bodyDec = (BodyDeclaration) iter.next();

			bodyDec.accept(this);
		}
		return false;
	}

	public void endVisit(AnonymousClassDeclaration node) {
		ContextTracker nodeContext = (ContextTracker) node.getProperty(CONTEXT);
		if (nodeContext != null)
			context = nodeContext.exitContext();
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(ConstructorInvocation)
	 */
	public boolean visit(ConstructorInvocation node) {
		makeCallFact(CodeFactBucket.THIS_CALL, node, node.resolveConstructorBinding());
		return true;
	}

	//Don't need this visit,classInstanceCreation visit takes care of anon types
	//	public boolean visit(AnonymousClassDeclaration node) {
	//	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(SuperMethodInvocation)
	 */
	public boolean visit(SuperMethodInvocation node) {
		makeCallFact(CodeFactBucket.SUPER_CALL, node, (IMethodBinding) node.getName().resolveBinding());
		List args = node.arguments();
		for (Iterator iter = args.iterator(); iter.hasNext();) {
			Expression argExp = (Expression) iter.next();
			argExp.accept(this);
		}
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(SuperConstructorInvocation)
	 */
	public boolean visit(SuperConstructorInvocation node) {
		makeCallFact(CodeFactBucket.SUPER_CALL, node, node.resolveConstructorBinding());
		return true;
	}

	/**
	 * Utility method for creating call facts.
	 */
	private void makeCallFact(String callType, ASTNode callingNode, IMethodBinding calledMethodBinding) {
		if (calledMethodBinding != null) {
			context.assertCall(callType, calledMethodBinding, callingNode.getStartPosition(), callingNode.getLength());

		} else {
			JQueryBackendPlugin.traceQueries("FactsGenerator.makeCallFact: encountered null method binding");
		}
	}

	/**
	 * Method generate.
	 * @param iCompilationUnit
	 */
	public void generate(CompilationUnit cu) {
		cu.accept(this);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(ImportDeclaration)
	 */
	public boolean visit(ImportDeclaration node) {
		//		IBinding binding = node.resresolveBinding();
		////		if (binding != null) {
		////			switch(binding.getKind()) {
		////				case binding.PACKAGE:
		//		String qName = node.getName();
		//		Object type = null;
		//		try {
		//			type = factMaker.getElement(qName);
		//			if (type == null) {
		//				type = project.(qName);
		//				if (type == null) {
		//					throw new NullPointerException(); 
		//				}
		//					IPackageFragment ipf = (IPackageBinding)binding.getName()
		//					factMaker.insertFact("imports", currType,  )
		//				
		//			factMaker.insertFact("imports", currType, )
		return false;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(PackageDeclaration)
	 */
	//	public boolean visit(PackageDeclaration node) {
	//		factMaker.assertChild(currTypeRep, node.getN);
	//		return false;
	//	}
	public boolean visit(FieldAccess node) {
		Expression exp = node.getExpression();
		if (exp != null)
			exp.accept(this);

		SimpleName varName = node.getName();
		makeAccessFact(node, varName, CodeFactBucket.READ);
		return false;
	}

	public boolean visit(SuperFieldAccess node) {
		SimpleName varName = node.getName();
		makeAccessFact(node, varName, CodeFactBucket.READ);
		return false;
	}

	public boolean visit(SimpleName node) {
		makeAccessFact(node, node, CodeFactBucket.READ);
		return false;
	}

	public boolean visit(QualifiedName node) {
		Name qualif = node.getQualifier();
		if (qualif != null)
			qualif.accept(this);

		SimpleName varName = node.getName();
		makeAccessFact(node, varName, CodeFactBucket.READ);
		return false;
	}

	public boolean visit(VariableDeclarationStatement node) {
		List varDeclFrags = node.fragments();
		for (Iterator iter = varDeclFrags.iterator(); iter.hasNext();) {
			VariableDeclarationFragment frag = (VariableDeclarationFragment) iter.next();
			frag.accept(this);
		}
		return false;
	}

	//the initializer of a for statement
	public boolean visit(VariableDeclarationExpression node) {
		List varDeclFrags = node.fragments();
		for (Iterator iter = varDeclFrags.iterator(); iter.hasNext();) {
			VariableDeclarationFragment frag = (VariableDeclarationFragment) iter.next();
			frag.accept(this);
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.FieldDeclaration)
	 */
	public boolean visit(FieldDeclaration node) {
		context = new FieldDeclarationContext(context);
		node.setProperty(CONTEXT, context);
		context.assertElementLocation(node.getStartPosition(), node.getLength());
		return super.visit(node);
	}

	public void endVisit(FieldDeclaration node) {
		ContextTracker nodeContext = (ContextTracker) node.getProperty(CONTEXT);
		if (nodeContext != null)
			context = nodeContext.exitContext();
		super.endVisit(node);
	}

	public boolean visit(VariableDeclarationFragment node) {
		IVariableBinding varBinding = node.resolveBinding();

		if (varBinding == null) {
			return false;
			//			JQueryPlugin.traceQueries("FactsGenerator.visit(VariableDeclarationFragment): "+" null varBinding.  Node="+node);
		} else if (varBinding.isField()) {
			SimpleName fieldName = node.getName();
			context = new FieldContext(varBinding, fieldName.getStartPosition(), fieldName.getLength(), node.getStartPosition(), node.getLength(), context);
			//mark node with context
			node.setProperty(CONTEXT, context);
			((FieldContext) context).assertTags();

		} else {
			//this is a local variable.  Could add code to handle them here
		}

		Expression initializer = node.getInitializer();
		if (initializer != null) {
			if (varBinding.isField()) {
				context = new InitializerContext(initializer.getStartPosition(), initializer.getLength(), 0, context);
				makeAccessFact(initializer, node.getName(), CodeFactBucket.WRITE);
			}

			initializer.accept(this);
			if (varBinding.isField()) {
				context = context.exitContext();
			}

		}
		return false;
	}

	public void endVisit(VariableDeclarationFragment node) {
		ContextTracker nodeContext = (ContextTracker) node.getProperty(CONTEXT);
		if (nodeContext != null)
			context = nodeContext.exitContext();
	}

	//	public boolean visit(SingleVariableDeclaration node) {
	//		return true;
	//	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Initializer)
	 */
	public boolean visit(Initializer node) {

		int modifiers = node.getModifiers();
		int initStart = node.getStartPosition();
		int initLength = node.getLength();

		context = new InitializerContext(initStart, initLength, modifiers, context);
		//mark node with context
		node.setProperty(CONTEXT, context);
		context.assertElementLocation(node.getStartPosition(), node.getLength());

		return super.visit(node);
	}

	public void endVisit(Initializer node) {
		ContextTracker nodeContext = (ContextTracker) node.getProperty(CONTEXT);
		if (nodeContext != null)
			context = nodeContext.exitContext();
	}

	public boolean visit(TryStatement node) {
		//		int tryStart = node.getStartPosition();
		//		int tryLength = node.getLength();
		//		
		//		context = new TryContext(tryStart, tryLength, context);
		//		
		//		node.getBody();
		//		node.catchClauses();
		//		node.getFinally();
		//		
		return super.visit(node);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#endVisit(org.eclipse.jdt.core.dom.Javadoc)
	 */
	public void endVisit(Javadoc node) {
		super.endVisit(node);
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ASTVisitor#visit(org.eclipse.jdt.core.dom.Javadoc)
	 */
	public boolean visit(Javadoc node) {
		// TODO Get this Javadoc tags thing working!
		// try{
		// List tags = node.tags();
		// for (Iterator it = tags.iterator(); it.hasNext();) {
		// TagElement tag = (TagElement) it.next();
		// context.assertJavadocTag(tag);
		// }
		// }catch(Exception e ){e.printStackTrace();}
		return super.visit(node);
	}

}