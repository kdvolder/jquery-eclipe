/*
 * Created on Jul 14, 2004
 */
package ca.ubc.jquery.resource.java.tyruba;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import tyRuBa.modes.TypeConstructor;

/**
 * Byte code visitor for bytecode in methods
 * @author riecken
 */
public class ClassFileCodeFactGenerator implements MethodVisitor {

	//opcodes that i'm interested in
	private static final int GETSTATIC = 178;

	private static final int PUTSTATIC = 179;

	private static final int GETFIELD = 180;

	private static final int PUTFIELD = 181;

	private static final int INSTANCEOF = 193;

	private static final int INVOKEVIRTUAL = 182;

	private static final int INVOKESPECIAL = 183;

	private static final int INVOKESTATIC = 184;

	private static final int INVOKEINTERFACE = 185;

	//some context
	private Object typeRepresentation;

	private Object methodRepresentation;

	private String prettyMethodName;

	private String prettyClassName;

	private String prettyPackageName;

	private CodeFactBucket bucket;

	public ClassFileCodeFactGenerator(Object typeRepresentation, Object methodRepresentation, String prettyMethodName, String prettyClassName, String prettyPackageName, CodeFactBucket bucket) {
		this.bucket = bucket;
		this.typeRepresentation = typeRepresentation;
		this.methodRepresentation = methodRepresentation;
		this.prettyMethodName = prettyMethodName;
		this.prettyPackageName = prettyPackageName;
		this.prettyClassName = prettyClassName;
	}

	@Override
	public void visitInsn(int arg0) {
		//Nothing here I want right now
	}

	@Override
	public void visitIntInsn(int arg0, int arg1) {
		//Nothing here i want right now
	}

	@Override
	public void visitVarInsn(int arg0, int arg1) {
		//Nothing here i want right now
	}

	/** TYPE INSTRUCTIONS (NEW, INSTANCEOF, ANEWARRAY) **/
	@Override
	public void visitTypeInsn(int opcode, String desc) {
		switch (opcode) {
		case INSTANCEOF:
			bucket.assertInstanceOf(methodRepresentation, prettyMethodName, getRepresentation(ClassFileFactGenerator.getEscapedClassName(desc), bucket.type_RefType), ClassFileFactGenerator.getQualifiedNameFromTypeDescriptor(desc), 0, 0);
			break;
		}
	}

	/** FIELD ACCESSES **/
	@Override
	public void visitFieldInsn(int opcode, String owner, String name, String desc) {
		Object fieldRep = getRepresentation(ClassFileFactGenerator.getEscapedClassName(owner) + "#" + name, bucket.type_Field);
		switch (opcode) {
		case GETSTATIC:
		case GETFIELD:
			bucket.assertAccess(CodeFactBucket.READ, methodRepresentation, prettyMethodName, fieldRep, name, 0, 0);
			break;
		case PUTSTATIC:
		case PUTFIELD:
			bucket.assertAccess(CodeFactBucket.WRITE, methodRepresentation, prettyMethodName, fieldRep, name, 0, 0);
			break;
		}
	}

	/** METHOD CALLS **/
	@Override
	public void visitMethodInsn(int opcode, String owner, String name, String desc) {
		String[] argumentTypes = ClassFileFactGenerator.getEscapedMethodArguments(desc);

		StringBuffer fullyQualifiedSignatureBuff = new StringBuffer();
		StringBuffer signatureBuff = new StringBuffer();
		fullyQualifiedSignatureBuff.append("(");
		signatureBuff.append("(");

		for (int i = 0; i < argumentTypes.length; i++) {
			if (i != 0) {
				fullyQualifiedSignatureBuff.append(", ");
				signatureBuff.append(", ");
			}
			fullyQualifiedSignatureBuff.append(argumentTypes[i]);
			signatureBuff.append(argumentTypes[i].substring(argumentTypes[i].lastIndexOf('.') + 1));
		}

		fullyQualifiedSignatureBuff.append(")");
		signatureBuff.append(")");

		String signature = signatureBuff.toString();
		String fullyQualifiedSignature = fullyQualifiedSignatureBuff.toString();

		String ownerCheckString = ClassFileFactGenerator.getQualifiedNameFromAsmClassName(owner);
		String ownerClass = ClassFileFactGenerator.getEscapedClassName(owner);
		String ownerClassName = ownerClass.substring(ownerClass.lastIndexOf('.') + 1);
		String prettyOwnerClassName = ownerCheckString.substring(ownerCheckString.lastIndexOf('.') + 1);

		Object ownerRep = getRepresentation(ownerClass + "#" + name + fullyQualifiedSignature, bucket.type_Method);

		switch (opcode) {
		case INVOKESPECIAL:
			if (name.equals("<init>")) {
				ownerRep = getRepresentation(ownerClass + "#" + ownerClassName + fullyQualifiedSignature, bucket.type_Constructor);
				if (ownerCheckString.equals(prettyPackageName + "." + prettyClassName) && prettyMethodName.substring(0, prettyMethodName.indexOf("(")).equals(prettyClassName)) {
					//This Call
					bucket.assertCall(CodeFactBucket.THIS_CALL, methodRepresentation, prettyMethodName, ownerRep, prettyClassName + signature, 0, 0);
				} else {
					//Constructor Call
					bucket.assertCall(CodeFactBucket.CONSTRUCTOR_CALL, methodRepresentation, prettyMethodName, ownerRep, prettyOwnerClassName + signature, 0, 0);
				}
			} else {
				//Super call
				bucket.assertCall(CodeFactBucket.SUPER_CALL, methodRepresentation, prettyMethodName, ownerRep, name + signature, 0, 0);
			}
			break;
		case INVOKESTATIC:
		case INVOKEINTERFACE:
		case INVOKEVIRTUAL:
			bucket.assertCall(CodeFactBucket.METHOD_CALL, methodRepresentation, prettyMethodName, ownerRep, name + signature, 0, 0);
			break;
		}
	}

	@Override
	public void visitJumpInsn(int arg0, Label arg1) {
		//Nothing here i want right now
	}

	@Override
	public void visitLabel(Label arg0) {
		//Nothing here i want right now
	}

	@Override
	public void visitLdcInsn(Object arg0) {
		//Nothing here I want right now
	}

	@Override
	public void visitIincInsn(int arg0, int arg1) {
		//Nothing here i want right now
	}

	@Override
	public void visitTableSwitchInsn(int arg0, int arg1, Label arg2, Label[] arg3) {
		//Nothing here i want right now
	}

	@Override
	public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
		//Nothing here I want right now
	}

	@Override
	public void visitMultiANewArrayInsn(String arg0, int arg1) {
		//Nothing here i want right now
	}

	@Override
	public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2, String arg3) {
		//Nothing here I want right now
	}

	@Override
	public void visitMaxs(int arg0, int arg1) {
		//Nothing here i want right now
	}

	@Override
	public void visitLineNumber(int arg0, Label arg1) {
		//Nothing here I want right now
	}

	@Override
	public void visitAttribute(Attribute arg0) {
		//Nothing here I want right now
	}

	private Object getRepresentation(String value, TypeConstructor type) {
		Object result = null;
		result = bucket.makeTypeCast(type, bucket.getIntRep(value));
		return result;
	}

	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		//TODO: annotations in .class files parsing?
		return null;
	}

	@Override
	public AnnotationVisitor visitAnnotationDefault() {
		//TODO: annotations in .class files parsing?
		return null;
	}

	@Override
	public void visitCode() {
	}

	@Override
	public void visitEnd() {
	}

	@Override
	public void visitFrame(int type, int local, Object[] local2, int stack,
			Object[] stack2) {
	}

	@Override
	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index) {
	}

	@Override
	public AnnotationVisitor visitParameterAnnotation(int parameter,
			String desc, boolean visible) {
		//TODO: annotations in .class files parsing?
		return null;
	}

}
