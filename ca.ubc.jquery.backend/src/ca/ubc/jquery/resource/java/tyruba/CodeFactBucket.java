/*
 * Created on Apr 28, 2003
 */
package ca.ubc.jquery.resource.java.tyruba;

import java.util.HashSet;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.CorrectionEngine;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.internal.core.SourceType;

import tyRuBa.engine.FrontEnd;
import tyRuBa.engine.RBTerm;
import tyRuBa.modes.TypeConstructor;
import tyRuBa.modes.TypeModeError;
import annotations.Export;
import annotations.Feature;
import ca.ubc.jquery.JQueryBackendPlugin;
import ca.ubc.jquery.api.JQueryException;
import ca.ubc.jquery.api.JQueryFactGenerator;
import ca.ubc.jquery.api.JQueryFileElement;
import ca.ubc.jquery.api.JQueryResourceManager;
import ca.ubc.jquery.api.tyruba.TyRuBaFactBaseResource;
import ca.ubc.jquery.tyruba.javadoc.IJavadocTagProcessor;

@Feature(names = { "./annotations", "./NONE" })
public abstract class CodeFactBucket extends ca.ubc.jquery.engine.tyruba.java.CodeFactBucket {

	//	public static final String READ = "reads";
	//
	//	public static final String WRITE = "writes";
	//
	//	/** String denoting a standard method call */
	//	public static final String METHOD_CALL = "methodCall";
	//
	//	/**
	//	 * String denoting a standard constructor call. Do not use for "this" or
	//	 * "super" constructor calls
	//	 */
	//	public static final String CONSTRUCTOR_CALL = "constructorCall";
	//
	//	/** String denoting a call to a method using the "this" keyword */
	//	public static final String THIS_CALL = "thisCall";
	//
	//	/** String denoting a call to a method using the "super" keyword */
	//	public static final String SUPER_CALL = "superCall";
	//
	//	/*
	//	 * Hashtable for storing representations of anonymous classes. key: an
	//	 * ITypeBinding returns: a representation (Object)
	//	 */
	//	protected Map anonClasses = new HashMap();
	//
	//	// counter for keeping track of/generating names for anonymous classes
	//	protected int anonClassCounter = 0;
	//
	//	protected int position;
	//
	//	protected IMarker[] markers;
	//
	//	private String cuSource = null;
	//
	//	private int markerIDCt = 0; // used to generate marker identifier
	//
	//	private static Hashtable javadocTagProcessors = new Hashtable();
	//
	//	protected Set addedMarkers = new HashSet();
	//
	//	static {
	//		javadocTagProcessors.put("jquery", new JQueryTagProcessor());
	//	}
	//
	//	/**
	//	 * THese TyRuBa userdefined types are declared in ... TYPE Package AS String
	//	 * TYPE CU AS String TYPE Field AS String
	//	 * 
	//	 * ///////////////////////////////////////////////////////////////// TYPE
	//	 * Class AS String TYPE Interface AS String TYPE RefType = Class | Interface
	//	 * TYPE Primitive AS String TYPE Type = RefType | Primitive
	//	 * 
	//	 * ///////////////////////////////////////////////////////////////// TYPE
	//	 * Method AS String TYPE Constructor AS String TYPE Callable = Method |
	//	 * Constructor TYPE Initializer AS String TYPE Block = Callable |
	//	 * Initializer
	//	 * 
	//	 * ///////////////////////////////////////////////////////////////// TYPE
	//	 * Element = Package | CU | Field | Type | Block
	//	 */
	//	public TypeConstructor type_Package;
	//
	//	public TypeConstructor type_CU;
	//
	//	public TypeConstructor type_Field;
	//
	//	public TypeConstructor type_RefType;
	//
	//	public TypeConstructor type_Primitive;
	//
	//	public TypeConstructor type_Type;
	//
	//	public TypeConstructor type_Method;
	//
	//	public TypeConstructor type_Constructor;
	//
	//	public TypeConstructor type_Callable;
	//
	//	public TypeConstructor type_Initializer;
	//
	//	public TypeConstructor type_Block;
	//
	//	public TypeConstructor type_Element;
	//
	//	public TypeConstructor type_Warning;
	//
	//	public TypeConstructor type_Error;
	//
	//	public TypeConstructor type_Problem;
	//
	//	public TypeConstructor type_Bookmark;
	//
	//	public TypeConstructor type_Task;
	//
	//	public TypeConstructor type_Marker;
	//
	//	public TypeConstructor type_Enum;
	//
	//	public @Feature(names = { "./annotations" })
	//	TypeConstructor type_Annotation;
	//
	//	/**
	//	 * A reference to tehe RuleBaseManager is needed to be able to add
	//	 * bucketDependencies into the factbase automatically.
	//	 */
	//	private JQueryResourceManager ruleBaseManager = null;
	//
	//	protected abstract IJavaElement getCompilationUnit();

	public CodeFactBucket(JQueryResourceManager rbm) {
		super(rbm);
		//		this.ruleBaseManager = rbm;
	}

	@Override
	public void initialize(JQueryFactGenerator generator) {
		setGenerator(generator);

		try {
			type_Package = this.findTypeConst("Package");
			type_CU = this.findTypeConst("CU");
			type_Field = this.findTypeConst("Field");
			// type_Class = this.findType("Class");
			// type_Interface = this.findType("Interface");
			type_RefType = this.findTypeConst("RefType");
			type_Primitive = this.findTypeConst("Primitive");
			type_Type = this.findTypeConst("Type");
			type_Method = this.findTypeConst("Method");
			type_Constructor = this.findTypeConst("Constructor");
			type_Callable = this.findTypeConst("Callable");
			type_Initializer = this.findTypeConst("Initializer");
			type_Block = this.findTypeConst("Block");
			type_Element = this.findTypeConst("Element");
			type_Enum = this.findTypeConst("Enum");

			type_Warning = this.findTypeConst("Warning");
			type_Error = this.findTypeConst("Error");
			type_Problem = this.findTypeConst("Problem");
			type_Bookmark = this.findTypeConst("Bookmark");
			type_Task = this.findTypeConst("Task");
			type_Marker = this.findTypeConst("Marker");
			type_Annotation = this.findTypeConst("Annotation");
		} catch (Exception e) {
			System.err.println("BAD BAD PARSER: " + e);
		}
	}

	public String getName() {
		return getCUHandle();
	}

	protected TyRuBaFactBaseResource getResource() {
		return (TyRuBaFactBaseResource) getGenerator();
	}

	public TypeConstructor findTypeConst(String name) throws TypeModeError {
		return getResource().findTypeConst(name);
	}

	public Object makeTypeCast(TypeConstructor toType, Object value) {
		return getResource().makeTypeCast(toType, value);
	}

	public Object makeJavaObject(Object value) {
		return getResource().makeJavaObject(value);
	}

	private IJavaProject getJavaProject() {
		return getCompilationUnit().getJavaProject();
	}

	private void setMarkerAdded(IMarker marker) {
		addedMarkers.add(marker);
	}

	private boolean isMarkerAdded(IMarker marker) {
		return addedMarkers.contains(marker);
	}

	private int getMarkerID() {
		return ++markerIDCt;
	}

	// public abstract CompilationUnit getAST();

	/**
	 * Returns the eclipse JDT's handle identifier for the compilation unit
	 * represented by this bucket.
	 */
	protected String getCUHandle() {
		return getCompilationUnit().getHandleIdentifier();
	}

	private int getAndIncrementAnonymousClassCounter() {
		int result = anonClassCounter;
		anonClassCounter++;
		return result;
	}

	// TODO Do I need this method?
	//	protected void clear() {
	//		getResource().clear();
	//		anonClasses.clear();
	//		anonClassCounter = 0;
	//		markers = null;
	//		addedMarkers.clear();
	//	}

//	/**
//	 * Creates a qualified name from the given binding.
//	 */
//	private String getQNameFromBinding(IMethodBinding binding) {
//		return getQNameFromBinding(binding.getDeclaringClass()) + "#" + getSignatureFromBinding(binding, true);
//	}

	/**
	 * Creates a qualified name from the given binding. Cannot be static as it
	 * must have access to the anonymous class hashtable
	 * 
	 */
	private String getQNameFromBinding(ITypeBinding binding) {
		if (binding == null) {
			return "java.lang%.Object";
		}
		String declaringQName;
		
		// To be consistent with class file facts, we do not use generics
		// information
		ITypeBinding erasureOfBinding = binding.getErasure();
		String typeQName = erasureOfBinding.getName();
		
		boolean useDot = true;

		if (binding.isArray()) {
			binding = binding.getElementType();
		}

		if (binding.isPrimitive() || binding.isNullType()) {
			return typeQName;
		}

		ITypeBinding declaring = binding.getDeclaringClass();

		if (declaring == null) {
			IPackageBinding pkg = binding.getPackage();
			if (pkg != null)
				declaringQName = pkg.getName();
			else
				declaringQName = typeQName;
		} else {
			declaringQName = getQNameFromBinding(declaring);
			useDot = false; // it's an inner class
		}

		if (binding.isAnonymous()) {
			if (anonClasses.containsKey(binding)) {
				return declaringQName + anonClasses.get(binding);
			} else { // create and store a new name for this anonymous type
				typeQName = "#" + (++anonClassCounter);
				anonClasses.put(binding, typeQName);
				return declaringQName + typeQName;
			}
		} else {
			return declaringQName + (useDot ? "%." : "#") + typeQName;

		}
	}

	/**
	 * Determines and returns a representation for given compilation unit, to be
	 * used when inserting facts into the rulebase.
	 */
	public Object getRepresentation(IJavaElement je) {
		String rep = null; // = je.getHandleIdentifier();
		TypeConstructor type;

		String projectName = je.getJavaProject().getElementName();

		switch (je.getElementType()) {
		case IJavaElement.CLASS_FILE:
		case IJavaElement.COMPILATION_UNIT:
			type = type_CU;
			IJavaElement parent = je.getParent();
			if (parent != null) {
				rep = parent.getElementName() + "#" + je.getElementName();
			}
			break;
		case IJavaElement.PACKAGE_FRAGMENT:
		case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			type = type_Package;
			rep = je.getElementName();
			break;
		default:
			throw new Error("CodeFactBucket.getRepresentation: encountered unsupported type of IJavaElement: " + rep);
		}

		try {
			if (je.getCorrespondingResource() != null) {
				return makeTypeCast(type, getIntRep(projectName + "/" + rep));
			} else {
				return makeTypeCast(type, getIntRep(rep));
			}
		} catch (JavaModelException ex) {
			return makeTypeCast(type, getIntRep(projectName + "/" + rep));
		}
	}

	public RBTerm[] getRepresentation(ITypeBinding[] paramTypes) {
		RBTerm[] result = new RBTerm[paramTypes.length];
		for (int i = 0; i < paramTypes.length; i++) {
			result[i] = (RBTerm) getRepresentation(paramTypes[i]);
		}
		return result;
	}

	public String getSignatureRepresentationFromBinding(IMethodBinding binding) {
		String sig;
		if (binding.isConstructor()) 
			sig = "<init>";
		else
			sig = binding.getName();
		sig += "(";
		ITypeBinding paramBindings[] = binding.getParameterTypes();
		for (int i = 0; i < paramBindings.length; i++) {
			sig += getIntRep(getQNameFromBinding(paramBindings[i]));
			if (i < paramBindings.length - 1) {
				sig += ",";
			}
		}
		sig += ")";
		return sig;
	}

	private String getMethodRepresentation(IMethodBinding binding) {
		return getIntRep(getQNameFromBinding(binding.getDeclaringClass())) + 
				"#" + getSignatureRepresentationFromBinding(binding);
	}
	
	public Object getRepresentation(IBinding binding) {
		addDependencies(binding);
		if (binding == null) {
			throw new IllegalArgumentException("null binding passed as parameter");
		}
		TypeConstructor type = getElementType(binding);
		String strRep;
		if (binding instanceof ITypeBinding) {
			ITypeBinding typeBinding = (ITypeBinding) binding;
			strRep = getQNameFromBinding(typeBinding);
			if (typeBinding.isPrimitive()) {
				return makeTypeCast(type, strRep); // no int rep for primitive types (no facts declaring them)
			}
		} else if (binding instanceof IMethodBinding) {
			strRep = getMethodRepresentation((IMethodBinding)binding);
		} else if (binding instanceof IVariableBinding) {
			IVariableBinding varBinding = (IVariableBinding) binding;
			if (varBinding.isField()) {
				strRep = getIntRep(getQNameFromBinding(varBinding.getDeclaringClass())) + 
							"#" + 
							varBinding.getName();
			} else {
				throw new IllegalArgumentException("CodeFactBucket.getRepresentation:encountered non-field variable binding");
			}
		} else {
			JQueryBackendPlugin.traceQueries("CodeFactBucket.getRepresentation: encountered unsupported type of IBinding");
			throw new IllegalArgumentException("CodeFactBucket.getRepresentation: encountered unsupported type of IBinding:" + binding.getClass().getName());
		}

		try {
			if (binding.getJavaElement() != null && binding.getJavaElement().getCorrespondingResource() != null) {
				String projectName = getCompilationUnit().getJavaProject().getElementName();
				return makeTypeCast(type, getIntRep(projectName + "/" + strRep));
			} else {
				return makeTypeCast(type, getIntRep(strRep));
			}
		} catch (JavaModelException ex) {
			String projectName = getCompilationUnit().getJavaProject().getElementName();
			return makeTypeCast(type, getIntRep(projectName + "/" + strRep));
		}
	}

	protected String getIntRep(String strRep) {
		return "" + getGenerator().getUniqueID(strRep);
	}

	private void addDependencies(IBinding binding) {
		if (binding instanceof ITypeBinding) {
			ITypeBinding typeBinding = (ITypeBinding) binding;
			if (!typeBinding.isAnonymous()) {
				if (typeBinding.isClass() || typeBinding.isInterface()) {
					addDependencyOn(typeBinding.getQualifiedName());
					return;
				}
			}
			//			 else {
			//			 JQueryPlugin.traceQueries("CodeFactBucket.addDependencies:
			//			 encountered unsupported type of IBinding");
			//			 }
		} else if (binding instanceof IMethodBinding) {
			IMethodBinding methodBinding = (IMethodBinding) binding;
			addDependencies(methodBinding.getDeclaringClass());
		} else if (binding instanceof IVariableBinding) {
			IVariableBinding varBinding = (IVariableBinding) binding;
			addDependencies(varBinding.getDeclaringClass());
		}
		// JQueryPlugin.traceQueries("CodeFactBucket.addDependencies:
		// encountered unsupported type of IBinding");
	}

	protected void addDependencyOn(String qualifiedClassName) {
		try {
			while (qualifiedClassName.endsWith("[]")) {
				qualifiedClassName = qualifiedClassName.substring(0, qualifiedClassName.length() - 2);
			}
			IType type = getJavaProject().findType(qualifiedClassName);
			if (type == null) {
				JQueryBackendPlugin.traceQueries("CodeFactBucket.addDependencies: could not findType " + qualifiedClassName);
				return;
			}
			ICompilationUnit cu = type.getCompilationUnit();
			if (cu != null) {
				ruleBaseManager.dependencyFound(cu);
			} else {
				IClassFile cf = type.getClassFile();
				if (cf != null) {
					ruleBaseManager.dependencyFound(cf);
				} else
					JQueryBackendPlugin.traceQueries("CodeFactBucket.addDependencies: could not find CompilationUnit or class file for " + type.getFullyQualifiedName());
			}
			return;
		} catch (JavaModelException e) {
			e.printStackTrace();
			JQueryBackendPlugin.traceQueries("CodeFactBucket.addDependencies: " + e);
		}
	}

	/**
	 * Determines and returns the appropriate TypeAtom for given binding.
	 */

	private TypeConstructor getElementType(final IBinding binding) {
		if (binding == null) {
			throw new IllegalArgumentException("null binding passed as parameter");
		} else {
			if (binding instanceof ITypeBinding) {
				ITypeBinding typeBinding = (ITypeBinding) binding;
				if (typeBinding.isClass())
					// return type_Class;
					return type_RefType;
				else if (typeBinding.isInterface())
					// return type_Interface;
					return type_RefType;
				else if (typeBinding.isPrimitive())
					return type_Primitive;
				else if (typeBinding.isArray())
					return getElementType(typeBinding.getElementType());
				else if (typeBinding.isTypeVariable()) {
					return type_RefType;
				} else if (typeBinding.isEnum()) {
					return type_Enum;
				}
			} else if (binding instanceof IMethodBinding) {
				IMethodBinding methodBinding = (IMethodBinding) binding;
				if (methodBinding.isConstructor())
					return type_Constructor;
				else
					return type_Method;
			} else if (binding instanceof IVariableBinding) {
				IVariableBinding varBinding = (IVariableBinding) binding;
				if (varBinding.isField()) {
					return type_Field;
				} else {
					Error e = new Error("encountered non-field variable binding");
					JQueryBackendPlugin.error("CodeFactBucket.getRepresentation:", e);
					throw e;
				}
			} else {
				Error e = new Error("encountered unsupported type of IBinding");
				JQueryBackendPlugin.error("CodeFactBucket.getRepresentation:", e);
				throw e;
			}
		}
		return null;
	}

	public String getSignatureFromBinding(IMethodBinding binding, boolean qualifyParameterTypes) {
		String sig;
		sig = binding.getName();

		sig += "(";

		ITypeBinding paramBindings[] = binding.getParameterTypes();

		if (!qualifyParameterTypes) {
			for (int i = 0; i < paramBindings.length; i++) {
				// this name includes braces for array types
				sig += paramBindings[i].getName();
				if (i < paramBindings.length - 1) {
					sig += ",";
				}
			}
		} else {
			for (int i = 0; i < paramBindings.length; i++) {
				// this name includes braces for array types
				sig += getQNameFromBinding(paramBindings[i]);
				if (i < paramBindings.length - 1) {
					sig += ",";
				}
			}
		}
		sig += ")";
		return sig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see tyRuBa.engine.RuleBaseBucket#update()
	 */
	private String getQuotedString(IJavaElement je) {
		String rep = "";

		switch (je.getElementType()) {
		case IJavaElement.TYPE:
			rep = getQuotedStringFromSourceType((SourceType) je);
			break;
		case IJavaElement.FIELD:
			rep = getQuotedStringFromIField((IField) je);
			break;
		case IJavaElement.METHOD:
			rep = getQuotedStringFromIMethod((IMethod) je);
			break;
		}
		return rep;
	}

	/*
	 * return a RBquotedString
	 */
	private String getQuotedStringFromSourceType(SourceType theType) {

		String fullName = theType.getFullyQualifiedName();
		String packageName = "";
		String className = "";

		int index = fullName.lastIndexOf(".");
		if (index != -1) {
			packageName = fullName.substring(0, index);
			className = fullName.substring(index + 1);
		} else {
			packageName = "UNNAMED";
			className = fullName;
		}
		className = className.replace('$', '#');
		return packageName + "%." + className + "::RefType";
	}

	/*
	 * return a RBquotedString
	 */
	private String getQuotedStringFromIMethod(IMethod theMethod) {

		String type = "Method";
		String methodName = theMethod.getElementName();

		SourceType parent = (SourceType) theMethod.getParent();
		String parentName = getQuotedStringFromSourceType(parent);
		int index1 = parentName.lastIndexOf("::");
		String quotedString = parentName.substring(0, index1);

		try {
			if (theMethod.isConstructor())
				type = "Constructor";
		} catch (JavaModelException e) {
			System.out.println(e.getMessage());
		}
		quotedString = quotedString + "#" + methodName + "()::" + type;
		return quotedString;
	}

	/*
	 * return a RBquotedString
	 */
	private String getQuotedStringFromIField(IField theField) {

		String fieldName = theField.getElementName();
		SourceType parent = (SourceType) theField.getParent();
		String parentName = getQuotedStringFromSourceType(parent);

		int index1 = parentName.lastIndexOf("::");
		String quotedString = parentName.substring(0, index1);
		quotedString += "#" + fieldName + "::Field";

		return quotedString;
	}

	@Feature(names = { "./annotations" })
	public void assertAnnotation(Object annotRep) {
		insertFact("annotation", annotRep);
	}

	@Feature(names = { "./annotations" })
	public void assertHasAnnotation(Object representation, Object annotRep) {
		insertFact("hasAnnotation", representation, annotRep);
	}

	@Feature(names = { "./annotations" })
	public void assertAnnotationAttribute(Object representation, String name, Object value) {
		insertFact("attributeSpecified", representation, name, value);
	}

	public void assertCall(String callType, // one of the call strings declared above
			Object callerRep, String callerName, Object calledRep, String calledName, int start, int length) {

		String srString = callerName + " calls " + calledName;
		Object sr = makeSourceLocation(start, length);

		insertFact(callType, callerRep, calledRep, sr);
		insertFact("primLabel", sr, srString);
	}

	public void assertInstanceOf(Object callerRep, String callerName, Object typeRep, String typeName, int start, int length) {

		String srString = callerName + "calls instanceOf " + typeName;
		Object sr = makeSourceLocation(start, length);

		insertFact("instanceOf", callerRep, typeRep, sr);
		insertFact("primLabel", sr, srString);
	}

	public void assertAccess(String accessType, // "reads" or "writes"
			Object accessor, String accessorName, Object accessedField, String accessedName, int start, int length) {

		String srString = accessorName + " " + accessType + " " + accessedName;

		Object sr = makeSourceLocation(start, length);

		insertFact(accessType, accessor, accessedField, sr);
		insertFact("primLabel", sr, srString);
	}

	/**
	 * creates and inserts for the given object a new SourceLocation created from the given startPos and length, along with the IFile resource corresponding to this compilation unit bucket.
	 */
	public @Export(to = { "./annotations" })
	void assertSourceLocation(Object elementRep, int startPos, int length) {
		insertFact("sourceLocation", elementRep, makeJavaObject(makeSourceLocation(startPos, length)));
	}

	abstract protected JQueryFileElement makeSourceLocation(int startPos, int length);

	public void assertField(Object field, int startLoc, int length) {
		insertFact("field", field);
		assertSourceLocation(field, startLoc, length);
	}

	public void assertMethod(Object method, int startLoc, int length) {
		insertFact("method", method);
		assertSourceLocation(method, startLoc, length);
	}

	public void assertHasDefault(Object annotAttrib) {
		insertFact("hasDefault", annotAttrib);
	}

	public void assertDefaultValue(Object annotAttrib, String value) {
		insertFact("defaultValue", annotAttrib, value);
	}

	public void assertConstructor(Object constructorRep, int startLoc, int length) {
		insertFact("constructor", constructorRep);
		assertSourceLocation(constructorRep, startLoc, length);
	}

	public void assertInterface(Object iface, int startLoc, int length) {
		insertFact("interface", iface);
		assertSourceLocation(iface, startLoc, length);
	}

	public void assertClass(Object type, int startLoc, int length) {
		insertFact("class", type);
		assertSourceLocation(type, startLoc, length);
	}

	public void assertAnnotationDeclaration(Object annotDecl) {
		insertFact("annotationDeclaration", annotDecl);
		// Don't assertSourceLocation as that will be done by assertInterface (all annotation
		// declarations are interface declarations)
	}

	public void assertTypeParameter(Object type, Object paramType, int sourceStart, int sourceLen) {
		insertFact("typevar", paramType);
		assertSourceLocation(paramType, sourceStart, sourceLen);
		assertChild(type, paramType);
	}

	public void assertInitializer(Object initRep, int sourceStart, int sourcelength) {
		insertFact("initializer", initRep);
		assertSourceLocation(initRep, sourceStart, sourcelength);
	}

	public void assertParams(Object method, RBTerm[] argTypes) {
		insertFact("params", method, FrontEnd.makeList(argTypes));
		HashSet seenTypes = new HashSet();
		for (int i = 0; i < argTypes.length; i++) {
			if (!seenTypes.contains(argTypes[i])) {
				seenTypes.add(argTypes[i]);
				insertFact("arg", method, argTypes[i]);
			}
		}
	}

	public void assertChild(Object parent, Object child) {
		insertFact("child", parent, child);
	}

	/**
	 * Method assertCompilationUnit.
	 * 
	 * @param iCompilationUnit
	 */
	public void assertCompilationUnit(Object CU) {
		//		logger.debug("AssertCU: " + CU);
		insertFact("cu", CU);
		assertSourceLocation(CU, 0, 0);
	}

	public void assertExtends(Object subT, Object superT) {
		insertFact("extends", subT, superT);
	}

	public void assertImplements(Object type, Object implemented) {

		insertFact("implements", type, implemented);

	}

	public void assertModifier(Object obj, String mod) {
		insertFact("modifier", obj, mod);
	}

	public void assertModifiers(Object rep, int modifiers) {
		if (Modifier.isAbstract(modifiers)) {
			assertModifier(rep, "abstract");
		}

		if (Modifier.isNative(modifiers)) {
			assertModifier(rep, "native");
		}

		if (Modifier.isFinal(modifiers)) {
			assertModifier(rep, "final");
		}

		if (Modifier.isPrivate(modifiers)) {
			assertModifier(rep, "private");
		}

		if (Modifier.isProtected(modifiers)) {
			assertModifier(rep, "protected");
		}

		if (Modifier.isPublic(modifiers)) {
			assertModifier(rep, "public");
		}

		if (Modifier.isStatic(modifiers)) {
			assertModifier(rep, "static");
		}

		if (Modifier.isStrictfp(modifiers)) {
			assertModifier(rep, "strictfp");
		}

		if (Modifier.isSynchronized(modifiers)) {
			assertModifier(rep, "synchronized");
		}

		if (Modifier.isTransient(modifiers)) {
			assertModifier(rep, "transient");
		}

		if (Modifier.isVolatile(modifiers)) {
			assertModifier(rep, "volatile");
		}
	}

	public void assertName(Object obj, String objName) {
		insertFact("name", obj, objName);
	}

	public void assertFullyQualifiedName(Object obj, String objName) {
		insertFact("qname", obj, objName);
	}

	public void assertMarkers(Object rep, int sourceStart, int sourceLength) {
		try {
			for (int i = 0; i < markers.length; i++) {
				if (!isMarkerAdded(markers[i])) {
					int start = markers[i].getAttribute(IMarker.CHAR_START, 0);
					int end = markers[i].getAttribute(IMarker.CHAR_END, 0);
					String message = (String) markers[i].getAttribute(IMarker.MESSAGE, "No message");
					if (start >= sourceStart && end <= (sourceStart + sourceLength)) {
						// Compiler Errors and Warnings
						if (markers[i].isSubtypeOf(IMarker.PROBLEM)) {
							int severity = markers[i].getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
							if (severity == IMarker.SEVERITY_WARNING) {
								String warningToken = CorrectionEngine.getWarningToken(markers[i].getAttribute("id", -1));
								assertWarning(rep, start, (end - start), message, warningToken);
								setMarkerAdded(markers[i]);
							} else if (severity == IMarker.SEVERITY_ERROR) {
								assertError(rep, start, (end - start), message);
								setMarkerAdded(markers[i]);
							}
							// Bookmarks
						} else if (markers[i].isSubtypeOf(IMarker.BOOKMARK)) {
							assertBookmark(rep, start, (end - start), message);
							setMarkerAdded(markers[i]);
							// Tasks
						} else if (markers[i].isSubtypeOf(IMarker.TASK)) {
							int priority = markers[i].getAttribute(IMarker.PRIORITY, 999);
							String stPriority = "Unknown";
							switch (priority) {
							case IMarker.PRIORITY_HIGH:
								stPriority = "High";
								break;
							case IMarker.PRIORITY_NORMAL:
								stPriority = "Normal";
								break;
							case IMarker.PRIORITY_LOW:
								stPriority = "Low";
								break;
							}
							assertTask(rep, stPriority, start, (end - start), message);
							setMarkerAdded(markers[i]);
						}
					}
				}
			}
		} catch (CoreException e) {
			throw new Error("If this has happened, eclipse is not happy (CoreException): " + e.getMessage());
		}
	}

	public void assertError(Object obj, int start, int length, String srString) {
		Object errorRep = makeTypeCast(type_Error, getIntRep(srString + getCUHandle() + "$" + getMarkerID()));
		insertFact("error", errorRep);
		assertName(errorRep, srString);
		assertChild(obj, errorRep);
		assertSourceLocation(errorRep, start, length);
	}

	public void assertWarning(Object obj, int start, int length, String srString, String warningToken) {
		Object warningRep = makeTypeCast(type_Warning, getIntRep(srString + getCUHandle() + "$" + getMarkerID()));
		insertFact("warning", warningRep);
		if (warningToken != null) {
			insertFact("warningToken", warningRep, warningToken);
		}
		assertName(warningRep, srString);
		assertChild(obj, warningRep);
		assertSourceLocation(warningRep, start, length);
	}

	public void assertBookmark(Object obj, int start, int length, String srString) {
		Object bookmarkRep = makeTypeCast(type_Bookmark, srString + getCUHandle() + "$" + getMarkerID());
		insertFact("bookmark", bookmarkRep);
		assertName(bookmarkRep, srString);
		assertChild(obj, bookmarkRep);
		assertSourceLocation(bookmarkRep, start, length);
	}

	public void assertTask(Object obj, String priority, int start, int length, String srString) {
		Object taskRep = makeTypeCast(type_Task, srString + getCUHandle() + "$" + getMarkerID());
		insertFact("task", taskRep);
		insertFact("priority", taskRep, makeJavaObject(priority));
		assertName(taskRep, srString);
		assertChild(obj, taskRep);
		assertSourceLocation(taskRep, start, length);
	}

	public void assertPackage(Object pkg) {
		insertFact("package", pkg);
	}

	public void assertReturnType(Object currMethod, Object returnType) {
		insertFact("returns", currMethod, returnType);
	}

	public void assertSignature(Object methodRep, String signature) {
		insertFact("signature", methodRep, signature);
	}

	public void assertThrows(Object currMethod, Object exceptionType) {
		insertFact("throws", currMethod, exceptionType);
	}

	public void assertTypeOf(Object field, Object typeOfField) {
		insertFact("type", field, typeOfField);
	}

	public void assertTypeOfAnnotation(Object annot, Object typeOfAnnot) {
		insertFact("annotType", annot, typeOfAnnot);
	}

	public void assertJavadocTag(Object element, String tag, String value) {
		IJavadocTagProcessor tagProcessor = (IJavadocTagProcessor) javadocTagProcessors.get(tag);
		if (tagProcessor != null) {
			tagProcessor.processTag(getResource(), element, tag, value);
		}
		insertFact("tag", element, tag, value);
	}

	public void assertJavadocLocation(Object elementRep, int start, int length) {

		insertFact("javadocLocation", elementRep, makeJavaObject(makeSourceLocation(start, length)));
	}

	public void assertElementLocation(Object elementRep, int start, int length) {

		if (cuSource == null) {
			ICompilationUnit cu = (ICompilationUnit) getCompilationUnit();
			try {
				cuSource = cu.getSource();
			} catch (JavaModelException e) {
				e.printStackTrace();
				return;
			}
		}

		// TODO: Figure out what the code below is really for and why it sometimes
		// Throws an outOfBoundsExcpetion for the call to charAt.

		// Calculate start of javadoc so that it includes all
		// white space before the comment up to start of line.
		// int idx = start-1;
		// char c = cuSource.charAt(idx);
		// while (idx>0 && (c == ' ' || c == '\t')) {
		// idx--;
		// length++;
		// c = cuSource.charAt(idx);
		// }
		// start = idx + 1;

		insertFact("elementLocation", elementRep, makeJavaObject(makeSourceLocation(start, length)));
	}

	private void insertFact(String predName, Object[] args) {
		//		JQueryAPI.getFactBase().insert(this, predName, args);
		try {
			getGenerator().insert(predName, args);
		} catch (JQueryException e) {
			JQueryBackendPlugin.error("Inserting fact:", e);
		}
	}

	/**
	 * Inserts a fact "?predName(?arg1)" into the database, first converting Object arg1 into RBJavaObjects if necessary..
	 */
	private void insertFact(String predName, Object arg1) {
		insertFact(predName, new Object[] { arg1 });
	}

	/**
	 * Inserts a fact "?predName(?arg1, ?arg2)" into the database, first converting Objects arg1 and arg2 into a RBJavaObject if necessary.
	 */
	private void insertFact(String predName, Object arg1, Object arg2) {
		insertFact(predName, new Object[] { arg1, arg2 });
	}

	/**
	 * Inserts a fact "?predName(?arg1, ?arg2, ?arg3)" into the database, first converting Objects arg1, arg2 and arg3 into RBJavaObjects if necessary..
	 */
	private void insertFact(String predName, Object arg1, Object arg2, Object arg3) {
		insertFact(predName, new Object[] { arg1, arg2, arg3 });
	}
}
