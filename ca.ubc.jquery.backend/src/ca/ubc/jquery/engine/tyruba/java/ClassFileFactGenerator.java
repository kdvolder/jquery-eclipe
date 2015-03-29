///*
// * Created on Jul 14, 2004
// */
//package ca.ubc.jquery.engine.tyruba.java;
//
//import org.objectweb.asm.Attribute;
//import org.objectweb.asm.ClassVisitor;
//import org.objectweb.asm.CodeVisitor;
//import org.objectweb.asm.Constants;
//import org.objectweb.asm.Type;
//
//import tyRuBa.engine.RBRepAsJavaObjectCompoundTerm;
//import tyRuBa.engine.RBTerm;
//import tyRuBa.modes.TypeConstructor;
//
///**
// * Byte code visitor for class files
// * @category
// * @author riecken
// */
//public class ClassFileFactGenerator implements ClassVisitor {
//	
//	//Flag that controls whether facts are generated for method bodies (or just 
//	//for method signatures.
//	private boolean visitMethodBodies = false;
//	
//	//Some Class context
//	//    private String className;
//	//    private String escapedClassName;
//	private Object packageRepresentation;
//	private Object typeRepresentation;
//	private Object classFileRep;
//	private CodeFactBucket bucket;
//	private String unqualifiedClassName;
//	private String packageName;
//	
//	public ClassFileFactGenerator(CodeFactBucket bucket, Object classFileRep, boolean generateFactsForMethodBodies) {
//		this.bucket = bucket;
//		this.classFileRep = classFileRep;
//		this.visitMethodBodies = generateFactsForMethodBodies;
//	}
//	
//	/**
//	 * @see org.objectweb.asm.ClassVisitor#visit(int, java.lang.String, java.lang.String, java.lang.String[], java.lang.String)
//	 */
//	public void visit(int version, int access, String asmName, String asmSuperName, String[] asmInterfaces, String sourceFile) {
//		typeRepresentation = getTypeRepresentationFromAsmClassName(asmName);
//		packageRepresentation = getPackageRepresentationFromAsmClassName(asmName);
//		unqualifiedClassName = getUnqualifiedNameFromAsmClassName(asmName);
//		packageName = getPackageNameFromAsmClassName(asmName);
//		
//		Object superTypeRepresentation = null;
//		if (asmSuperName != null) {
//			superTypeRepresentation = getTypeRepresentationFromAsmClassName(asmSuperName);
//		}
//		
//		Object[] interfaceReps = new RBTerm[asmInterfaces.length];
//		for (int i = 0; i < asmInterfaces.length; i++) {
//			interfaceReps[i] = getTypeRepresentationFromAsmClassName(asmInterfaces[i]);
//		}
//		
//		String[] classModifiers = getClassModifiers(access);
//		
//		if ((access & Constants.ACC_INTERFACE) != 0) {
//			bucket.assertInterface(typeRepresentation, 0, 0);
//			if ((access & Constants.ACC_ANNOTATION) != 0) {
//				bucket.assertAnnotationDeclaration(typeRepresentation);
//			}
//		} else {
//			bucket.assertClass(typeRepresentation, 0, 0);
//		}
//		
//		bucket.assertChild(classFileRep, typeRepresentation);
//		
//		bucket.assertName(typeRepresentation, unqualifiedClassName);
//		bucket.assertFullyQualifiedName(typeRepresentation, getQualifiedNameFromAsmClassName(asmName));
//		
//		assertClassModifiers(typeRepresentation, access);
//		
//		if ((access & Constants.ACC_INTERFACE) != 0) {
//			for (int i = 0; i < interfaceReps.length; i++) {
//				bucket.assertExtends(typeRepresentation, interfaceReps[i]);
//			}
//		} else {
//			for (int i = 0; i < interfaceReps.length; i++) {
//				bucket.assertImplements(typeRepresentation, interfaceReps[i]);
//			}
//			if (asmSuperName != null) {
//				bucket.assertExtends(typeRepresentation, superTypeRepresentation);
//			}
//		}
//	}
//	
//	/**
//	 * @see org.objectweb.asm.ClassVisitor#visitInnerClass(java.lang.String, java.lang.String, java.lang.String, int)
//	 */
//	public void visitInnerClass(String name, String outerName, String innerName, int access) {
//		//        if (escapedClassName.indexOf('#') == -1) { // don't want inner inner classes or accessing an inner class that's not from the class we're looking at
//		//            if (outerName != null && !escapedClassName.equals(getEscapedClassName(outerName))) {
//		//                System.out.println(escapedClassName + " LLLLLL " + getEscapedClassName(outerName));
//		//                return;
//		//            }
//		//            
//		//            if (innerName == null) {
//		//                String newinnerName = String.valueOf(bucket.getAndIncrementAnonymousClassCounter()+ 1);
//		//                RBTerm innerClassRep = getRepresentation(escapedPackageName + "." + escapedClassName + "#" + newinnerName, bucket.type_RefType);
//		//                bucket.assertChild(typeRepresentation, innerClassRep);   
//		//            } else {
//		//                RBTerm innerClassRep = getRepresentation(escapedPackageName + "." + escapedClassName + "#" + innerName, bucket.type_RefType);
//		//                bucket.assertChild(typeRepresentation, innerClassRep);                    
//		//            }            
//		//        }               
//	}
//	
//	public void visitField(int access, String name, String desc, Object value, Attribute attrs) {
//		Object fieldTypeRep = getRepresentation(Type.getType(desc));
//		Object fieldRep = getFieldRepresentation(name);
//		bucket.assertChild(typeRepresentation, fieldRep);
//		assertModifiers(fieldRep, access);
//		bucket.assertName(fieldRep, name);
//		bucket.assertField(fieldRep, 0, 0);
//		bucket.assertTypeOf(fieldRep, fieldTypeRep);
//	}
//	
//	/**
//	 * @see org.objectweb.asm.ClassVisitor#visitMethod(int, java.lang.String, java.lang.String, java.lang.String[], org.objectweb.asm.Attribute)
//	 */
//	public CodeVisitor visitMethod(int access, String name, String desc, String[] exceptions, Attribute attrs) {
//		//TODO: Java 1.5 bytecode has "bridge methods" deal with it!
//		//  for now we just ignore them.
//		if (isBridge(access)) 
//			return null;
//		
//		Type[] argumentTypes = Type.getArgumentTypes(desc);
//		RBTerm[] argumentTypeReps = getRepresentation(argumentTypes);
//		Type returnType = Type.getReturnType(desc);
//		Object returnTypeRep = getRepresentation(returnType);
//		
//		Object methodRep;
//		String methodName;
//		if (name.equals("<clinit>")) {
//			// class initializer
//			methodRep = getInitializerRep(name);
//			methodName = "<clinit>";
//			bucket.assertInitializer(methodRep, 0, 0);
//		}
//		else if (name.equals("<init>")) {
//			// constructor
//			methodRep = getConstructorRepresentation(argumentTypeReps);
//			methodName = unqualifiedClassName;
//			bucket.assertConstructor(methodRep, 0, 0);
//			bucket.assertParams(methodRep,argumentTypeReps);
//			assertSignature(methodRep,methodName,argumentTypes,returnType);
//		}
//		else {
//			// ordinary method
//			methodRep = getMethodRepresentation(name,argumentTypeReps);
//			methodName = name;
//			bucket.assertMethod(methodRep, 0, 0);
//			bucket.assertReturnType(methodRep, returnTypeRep);
//			bucket.assertParams(methodRep,argumentTypeReps);
//			assertSignature(methodRep,methodName,argumentTypes,returnType);
//		}
//		bucket.assertName(methodRep, methodName);
//		bucket.assertChild(typeRepresentation, methodRep);
//		assertModifiers(methodRep, access);
//		if (exceptions!=null)
//			for (int i = 0; i < exceptions.length; i++) {
//				bucket.assertThrows(methodRep, getTypeRepresentationFromAsmClassName(exceptions[i]));
//			}
//		if (visitMethodBodies)
//			return new ClassFileCodeFactGenerator(typeRepresentation, methodRep, methodName, 
//					unqualifiedClassName, packageName, bucket); 
//		else 
//			return null;
//	}
//	
//	private void assertSignature(Object methodRep, String methodName, Type[] argumentTypes, Type returnType) {
//		StringBuffer sig = new StringBuffer(methodName);
//		sig.append("(");
//		for (int i = 0; i < argumentTypes.length; i++) {
//			if (i>0) sig.append(",");
//			sig.append(getUnqualifiedName(argumentTypes[i]));
//		}
//		sig.append(")");
//		bucket.assertSignature(methodRep,sig.toString());
//	}
//	
//	/**
//	 * @see org.objectweb.asm.ClassVisitor#visitAttribute(org.objectweb.asm.Attribute)
//	 */
//	public void visitAttribute(Attribute attr) {
//		//nothing for now, not using any 1.5 special features yet
//	}
//	
//	/**
//	 * @see org.objectweb.asm.ClassVisitor#visitEnd()
//	 */
//	public void visitEnd() {
//		
//	}
//	
//	private void assertModifiers(Object representation, int access) {
//		String[] modifiers = getModifiers(access);
//		for (int i = 0; i < modifiers.length; i++) {
//			//System.out.println("modifier(" + name  + "," + modifiers[i] + ")");
//			bucket.assertModifier(representation, modifiers[i]);
//		}
//	}
//	
//	private void assertClassModifiers(Object representation, int access) {
//		String[] classModifiers = getClassModifiers(access);
//		for (int i = 0; i < classModifiers.length; i++) {
//			//System.out.println("modifier(" + name + "," + classModifiers[i] + ")");
//			bucket.assertModifier(representation, classModifiers[i]);
//		}
//	}
//	
//	
//	private String[] getClassModifiers(int access) {
//		StringBuffer modifiers = new StringBuffer();
//		boolean isFirst = true;
//		
//		if ((access & Constants.ACC_PUBLIC) != 0) {
//			modifiers.append("public");
//			isFirst = false;
//		}
//		
//		if ((access & Constants.ACC_PRIVATE) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("private");
//			isFirst = false;
//		}
//		
//		if ((access & Constants.ACC_PROTECTED) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("protected");
//			isFirst = false;
//		} 
//		
//		if ((access & Constants.ACC_STATIC) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("static");
//			isFirst = false;
//		}
//		
//		if ((access & Constants.ACC_STRICT) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("strictfp");
//			isFirst = false;
//		}
//		
//		if ((access & Constants.ACC_ABSTRACT) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("abstract");
//			isFirst = false;
//		}
//		
//		if ((access & Constants.ACC_FINAL) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("final");
//			isFirst = false;
//		}
//		
//		return modifiers.toString().split(",");
//	}
//	
//	private boolean isBridge(int access) {
//		return (access & Constants.ACC_BRIDGE) != 0;
//	}
//	
//	private String[] getModifiers(int access) {
//		StringBuffer modifiers = new StringBuffer();
//		boolean isFirst = true;
//		
//		if ((access & Constants.ACC_PUBLIC) != 0) {
//			modifiers.append("public");
//			isFirst = false;
//		}
//		
//		if ((access & Constants.ACC_PRIVATE) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("private");
//			isFirst = false;
//		}
//		
//		if ((access & Constants.ACC_PROTECTED) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("protected");
//			isFirst = false;
//		} 
//		
//		if ((access & Constants.ACC_STATIC) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("static");
//			isFirst = false;
//		}
//		
//		if ((access & Constants.ACC_ABSTRACT) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("abstract");
//			isFirst = false;
//		}
//		
//		if ((access & Constants.ACC_FINAL) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("final");
//			isFirst = false;
//		}
//		
//		if ((access & Constants.ACC_NATIVE) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("native");
//			isFirst = false;
//		}
//		
//		if ((access & Constants.ACC_STRICT) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("strictfp");
//			isFirst = false;
//		}
//		
//		if ((access & Constants.ACC_SYNCHRONIZED) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("synchronized");
//			isFirst = false;
//		}
//		
//		if ((access & Constants.ACC_TRANSIENT) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("transient");
//			isFirst = false;
//		}
//		
//		if ((access & Constants.ACC_VOLATILE) != 0) {
//			if (!isFirst) {
//				modifiers.append(",");
//			}
//			modifiers.append("volatile");
//			isFirst = false;
//		}
//		
//		return modifiers.toString().split(",");
//	}
//	
//	/** 
//	 * @deprecated Replace with version without TypeConstructor
//	 */
//	private Object getRepresentation(String value, TypeConstructor type) {    
//		Object result = null;
//		result = bucket.makeTypeCast(type,value);
//		return result;
//	}
//	
////	public static String getPrettyTypeName(String typeName) {
////		return Type.getType(typeName).getClassName();
////	}
////	
////	public static String getPrettyClassName(String className) {
////		return className.replaceAll("/","\\.");
////	}
//	
//	/**
//	 * @deprecated Use getTypeRepresentation instead
//	 */
//	private static String getEscapedTypeName(String typeName) {
//		Type type = Type.getType(typeName);
//		
//		String result = type.getClassName().replace('$','#');
//		int lastIndexOfDot = result.lastIndexOf('.');
//		if (lastIndexOfDot != -1) {
//			String firstPart = result.substring(0,lastIndexOfDot);
//			String secondPart = result.substring(lastIndexOfDot +1);
//			result = firstPart + "%." + secondPart;
//		}
//		
//		return result;
//	}
//	
//	/**
//	 * @deprecated
//	 */
//	protected static String getEscapedClassName(String className) {
//		String result = className.replaceAll("/","\\.").replace('$','#');
//		int lastIndexOfDot = result.lastIndexOf('.');
//		if (lastIndexOfDot != -1) {
//			String firstPart = result.substring(0,lastIndexOfDot);
//			String secondPart = result.substring(lastIndexOfDot +1);
//			result = firstPart + "%." + secondPart;
//		}
//		return result;
//	}
//	
//	private Object getRepresentation(Type type) {
//		int sort = type.getSort();
//		switch (sort) {
//		// ASM JavaDoc: possible values are: 
//		// VOID, BOOLEAN, CHAR, BYTE, SHORT, INT, FLOAT, LONG, DOUBLE, ARRAY or OBJECT.
//		case Type.VOID:
//		case Type.BOOLEAN:
//		case Type.CHAR:
//		case Type.BYTE:
//		case Type.SHORT:
//		case Type.INT:
//		case Type.FLOAT:
//		case Type.LONG:
//		case Type.DOUBLE:
//			return bucket.makeTypeCast(bucket.type_Primitive,type.getClassName());
//		
//		case Type.OBJECT:
//		case Type.ARRAY:
//			String result = type.getClassName();
//			bucket.addDependencyOn(result);
//			result = result.replace('$','#');
//		int lastIndexOfDot = result.lastIndexOf('.');
//		if (lastIndexOfDot != -1) {
//			String firstPart = result.substring(0,lastIndexOfDot);
//			String secondPart = result.substring(lastIndexOfDot +1);
//			result = firstPart + "%." + secondPart;
//		} 
//		
//		return bucket.makeTypeCast(bucket.type_RefType,result);
//		
//		default:
//			throw new Error("Unexpected sort of type in .class file");
//		}
//	}
//	
//	private RBTerm[] getRepresentation(Type[] types) {
//		RBTerm[] result = new RBTerm[types.length];
//		for (int i = 0; i < types.length; i++) {
//			result[i] = (RBTerm)getRepresentation(types[i]);
//		}
//		return result;
//	}
//
//	private Object getTypeRepresentationFromAsmClassName(String asmName) {
//		String pkgName = getPackageNameFromAsmClassName(asmName);
//		String clsName = getUnqualifiedNameFromAsmClassName(asmName)
//							.replace('$','#');
//		bucket.addDependencyOn(getQualifiedNameFromAsmClassName(asmName));
//		if (pkgName.equals("")) {
//			return bucket.makeTypeCast(bucket.type_RefType,clsName);
//		}
//		else {
//			return bucket.makeTypeCast(bucket.type_RefType,pkgName + "%." + clsName);
//		}
//	}
//
//	static protected String getQualifiedNameFromAsmClassName(String asmName) {
//		return asmName.replaceAll("/",".");
//	}
//
//	static protected String getQualifiedNameFromTypeDescriptor(String typeDesc) {
//		return Type.getType(typeDesc).getClassName();
//	}
//	
//	private Object getPackageRepresentationFromAsmClassName(String name) {
//		String packageName = getPackageNameFromAsmClassName(name);
//		return bucket.makeTypeCast(bucket.type_Package,packageName);
//	}
//	
//	protected String getPackageNameFromAsmClassName(String name) {
//		int lastIndexOfDot = name.lastIndexOf('/');
//		if (lastIndexOfDot<0) { // dot not found
//			return "";
//		}
//		else {
//			return name.substring(0,lastIndexOfDot).replaceAll("/",".");
//		}
//	}
//	
//	private String getUnqualifiedNameFromAsmClassName(String name) {
//		int lastIndexOfSlash = name.lastIndexOf('/');
//		if (lastIndexOfSlash<0) { // dot not found
//			return name;
//		}
//		else {
//			return name.substring(lastIndexOfSlash+1,name.length());
//		}
//	}
//
//	private String getUnqualifiedName(Type type) {
//		String name = type.getClassName();
//		int lastIndexOfDot = name.lastIndexOf('.');
//		if (lastIndexOfDot<0) { // dot not found
//			return name;
//		}
//		else {
//			return name.substring(lastIndexOfDot+1,name.length());
//		}
//	}
//	
//	private Object getFieldRepresentation(String name) {
//		String typePrefix = getString(typeRepresentation);
//		return bucket.makeTypeCast(bucket.type_Field,typePrefix+"#"+name);
//	}
//	
//	private Object getArrayRep(RBTerm elementType) {
//		return bucket.makeTypeCast(bucket.type_RefType,getString(elementType)+"[]");
//	}
//	
//	private Object getMethodRepresentation(String name, Object[] argumentTypeReps) {
//		String typePrefix = getString(typeRepresentation);
//		StringBuffer methodRepString = new StringBuffer(getString(typeRepresentation));
//		methodRepString.append("#");
//		methodRepString.append(name);
//		methodRepString.append("(");
//		for (int i = 0; i < argumentTypeReps.length; i++) {
//			if (i>0) methodRepString.append(",");
//			methodRepString.append(getString(argumentTypeReps[i]));
//		}
//		methodRepString.append(")");
//		return bucket.makeTypeCast(bucket.type_Method,methodRepString.toString());
//	}
//	
//	private Object getConstructorRepresentation(Object[] argumentTypeReps) {
//		String typePrefix = getString(typeRepresentation);
//		StringBuffer methodRepString = new StringBuffer(getString(typeRepresentation));
//		methodRepString.append("#");
//		methodRepString.append("<init>");
//		methodRepString.append("(");
//		for (int i = 0; i < argumentTypeReps.length; i++) {
//			if (i>0) methodRepString.append(",");
//			methodRepString.append(getString(argumentTypeReps[i]));
//		}
//		methodRepString.append(")");
//		return bucket.makeTypeCast(bucket.type_Constructor,methodRepString.toString());
//	}
//	
//	private Object getInitializerRep(String methodName) {
//		String typePrefix = getString(typeRepresentation);
//		return bucket.makeTypeCast(bucket.type_Initializer,
//				typePrefix + "#" + methodName);
//	}
//	
//	private String getString(Object typeRepresentation) {
//		return (String)((RBRepAsJavaObjectCompoundTerm)typeRepresentation).getValue();
//	}
//	
//	/**
//	 * @deprecated
//	 */
//	protected static String[] getEscapedMethodArguments(String methodDescriptor) {
//		Type[] types = Type.getArgumentTypes(methodDescriptor);
//		String[] result = new String[types.length];
//		for (int i = 0; i < result.length; i++) {
//			result[i] = types[i].getClassName();
//			int lastIndexOfDot = result[i].lastIndexOf('.');
//			if (lastIndexOfDot != -1) {
//				String firstPart = result[i].substring(0,lastIndexOfDot);
//				String secondPart = result[i].substring(lastIndexOfDot +1);
//				result[i] = firstPart + "%." + secondPart;
//			}
//		}
//		return result;
//	}
//	
//	
//}
