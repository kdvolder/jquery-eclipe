///*
// * Created on Jul 14, 2004
// */
//package ca.ubc.jquery.engine.tyruba.java;
//
//import org.objectweb.asm.Attribute;
//import org.objectweb.asm.CodeVisitor;
//import org.objectweb.asm.Label;
//
//import tyRuBa.modes.TypeConstructor;
//
///**
// * Byte code visitor for bytecode in methods
// * @author riecken
// */
//public class ClassFileCodeFactGenerator implements CodeVisitor {
//
//    //opcodes that i'm interested in
//    private static final int GETSTATIC = 178;
//    private static final int PUTSTATIC = 179;
//    private static final int GETFIELD = 180;
//    private static final int PUTFIELD = 181;
//    private static final int INSTANCEOF = 193;
//    private static final int INVOKEVIRTUAL = 182;
//    private static final int INVOKESPECIAL = 183;
//    private static final int INVOKESTATIC = 184;
//    private static final int INVOKEINTERFACE = 185;
//    
//    //some context
//    private Object typeRepresentation;
//    private Object methodRepresentation;
//    private String prettyMethodName;
//    private String prettyClassName;
//    private String prettyPackageName;
//    private CodeFactBucket bucket;
//    
//    public ClassFileCodeFactGenerator(
//    		Object typeRepresentation, 
//    		Object methodRepresentation, 
//			String prettyMethodName, 
//			String prettyClassName, 
//			String prettyPackageName, 
//			CodeFactBucket bucket) 
//    {
//        this.bucket = bucket;
//        this.typeRepresentation = typeRepresentation;
//        this.methodRepresentation = methodRepresentation;   
//        this.prettyMethodName = prettyMethodName;
//        this.prettyPackageName = prettyPackageName;
//        this.prettyClassName = prettyClassName;
//    }
//
//    public void visitInsn(int arg0) {
//        //Nothing here I want right now
//    }
//
//    public void visitIntInsn(int arg0, int arg1) {
//        //Nothing here i want right now
//    }
//
//    public void visitVarInsn(int arg0, int arg1) {
//        //Nothing here i want right now
//    }
//
//    /** TYPE INSTRUCTIONS (NEW, INSTANCEOF, ANEWARRAY) **/
//    public void visitTypeInsn(int opcode, String desc) {
//        switch (opcode) {
//            case INSTANCEOF:
//                bucket.assertInstanceOf(
//                		methodRepresentation, prettyMethodName, 
//                		getRepresentation(ClassFileFactGenerator.getEscapedClassName(desc), 
//                				          bucket.type_RefType), 
//                		ClassFileFactGenerator.getQualifiedNameFromTypeDescriptor(desc), 0, 0);
//                break;
//        }
//    }
//
//    /** FIELD ACCESSES **/
//    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
//    	Object fieldRep = getRepresentation(ClassFileFactGenerator.getEscapedClassName(owner) + "#" + name, bucket.type_Field);
//        switch (opcode) {
//            case GETSTATIC:             
//            case GETFIELD:
//                bucket.assertAccess(CodeFactBucket.READ, methodRepresentation, prettyMethodName, fieldRep, name, 0, 0);
//                break;
//            case PUTSTATIC:  
//            case PUTFIELD:
//                bucket.assertAccess(CodeFactBucket.WRITE, methodRepresentation, prettyMethodName, fieldRep, name, 0, 0);
//                break;
//        }
//    }
//
//    /** METHOD CALLS **/
//    public void visitMethodInsn(int opcode, String owner, String name, String desc) {       
//        String[] argumentTypes = ClassFileFactGenerator.getEscapedMethodArguments(desc);
//
//        StringBuffer fullyQualifiedSignatureBuff = new StringBuffer();
//        StringBuffer signatureBuff = new StringBuffer();
//        fullyQualifiedSignatureBuff.append("(");
//        signatureBuff.append("(");
//        
//        for (int i = 0; i < argumentTypes.length; i++) {
//            if (i != 0) {
//                fullyQualifiedSignatureBuff.append(", ");
//                signatureBuff.append(", ");
//            }
//            fullyQualifiedSignatureBuff.append(argumentTypes[i]);
//            signatureBuff.append(argumentTypes[i].substring(argumentTypes[i].lastIndexOf('.') +1));
//        }
//        
//        fullyQualifiedSignatureBuff.append(")");
//        signatureBuff.append(")");
//        
//        String signature = signatureBuff.toString();
//        String fullyQualifiedSignature = fullyQualifiedSignatureBuff.toString();
//        
//        
//        String ownerCheckString = ClassFileFactGenerator.getQualifiedNameFromAsmClassName(owner);
//        String ownerClass = ClassFileFactGenerator.getEscapedClassName(owner);
//        String ownerClassName = ownerClass.substring(ownerClass.lastIndexOf('.') + 1);
//        String prettyOwnerClassName = ownerCheckString.substring(ownerCheckString.lastIndexOf('.') + 1);
//        
//        Object ownerRep = getRepresentation(ownerClass + "#" + name + fullyQualifiedSignature, bucket.type_Method);
//        
//        switch (opcode) {
//            case INVOKESPECIAL:
//                if (name.equals("<init>")) {
//                    ownerRep = getRepresentation(ownerClass + "#" + ownerClassName + fullyQualifiedSignature, bucket.type_Constructor);
//                    if (ownerCheckString.equals(prettyPackageName + "." + prettyClassName) && prettyMethodName.substring(0, prettyMethodName.indexOf("(")).equals(prettyClassName)) {
//                        //This Call
//                        bucket.assertCall(CodeFactBucket.THIS_CALL, methodRepresentation, prettyMethodName, ownerRep, prettyClassName + signature, 0, 0);
//                    } else {
//                        //Constructor Call
//                        bucket.assertCall(CodeFactBucket.CONSTRUCTOR_CALL, methodRepresentation, prettyMethodName, ownerRep, prettyOwnerClassName + signature, 0, 0);
//                    }
//                } else {
//                    //Super call
//                    bucket.assertCall(CodeFactBucket.SUPER_CALL, methodRepresentation, prettyMethodName, ownerRep, name + signature, 0, 0);
//                }
//                break;
//            case INVOKESTATIC:
//            case INVOKEINTERFACE:
//            case INVOKEVIRTUAL:
//                bucket.assertCall(CodeFactBucket.METHOD_CALL, methodRepresentation, prettyMethodName, ownerRep, name + signature, 0, 0);
//                break;
//        }
//    }
//
//    public void visitJumpInsn(int arg0, Label arg1) {
//        //Nothing here i want right now
//    }
//
//    public void visitLabel(Label arg0) {
//        //Nothing here i want right now
//    }
//    
//    public void visitLdcInsn(Object arg0) {
//        //Nothing here I want right now
//    }
//
//    public void visitIincInsn(int arg0, int arg1) {
//        //Nothing here i want right now
//    }
//
//    public void visitTableSwitchInsn(int arg0, int arg1, Label arg2,
//            Label[] arg3) {
//        //Nothing here i want right now
//    }
//
//    public void visitLookupSwitchInsn(Label arg0, int[] arg1, Label[] arg2) {
//        //Nothing here I want right now
//    }
//    
//    public void visitMultiANewArrayInsn(String arg0, int arg1) {
//        //Nothing here i want right now
//    }
//
//    public void visitTryCatchBlock(Label arg0, Label arg1, Label arg2,
//            String arg3) {
//        //Nothing here I want right now
//    }
//
//    public void visitMaxs(int arg0, int arg1) {
//        //Nothing here i want right now
//    }
//    
//    public void visitLocalVariable(String arg0, String arg1, Label arg2,
//            Label arg3, int arg4) {
//        //Nothing here i want right now
//    }
//
//    public void visitLineNumber(int arg0, Label arg1) {
//        //Nothing here I want right now
//    }
//
//    public void visitAttribute(Attribute arg0) {
//        //Nothing here I want right now
//    }
//    
//    private Object getRepresentation(String value, TypeConstructor type) {    
//    	Object result = null;
//        result = bucket.makeTypeCast(type,value);
//        return result;
//    }
//
//
//}
