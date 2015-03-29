package ca.ubc.jquery.resource.java.tyruba;
//package ca.ubc.jquery.engine.tyruba.java;
//
//import java.util.HashSet;
//import java.util.Hashtable;
//import java.util.Set;
//
//import org.apache.log4j.Logger;
//import org.eclipse.core.resources.IMarker;
//import org.eclipse.core.runtime.CoreException;
//import org.eclipse.jdt.core.ICompilationUnit;
//import org.eclipse.jdt.core.IJavaElement;
//import org.eclipse.jdt.core.JavaModelException;
//import org.eclipse.jdt.core.dom.Modifier;
//
//import tyRuBa.engine.FrontEnd;
//import tyRuBa.engine.RBTerm;
//import tyRuBa.engine.RuleBaseBucket;
//import tyRuBa.modes.TypeConstructor;
//import annotations.Export;
//import annotations.Feature;
//import ca.ubc.jquery.api.JQueryAPI;
//import ca.ubc.jquery.api.JQueryFileElement;
//import ca.ubc.jquery.javadoc.IJavadocTagProcessor;
//import ca.ubc.jquery.javadoc.JQueryTagProcessor;
//
//public abstract class CodeFactAssertion extends RuleBaseBucket {
//
//	private static Logger logger = Logger.getLogger(CodeFactBucket.class);
//
//	protected IMarker[] markers;
//
//	public TypeConstructor type_Task;
//
//	public TypeConstructor type_Warning;
//
//	public TypeConstructor type_Error;
//
//	public TypeConstructor type_Bookmark;
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
//	protected CodeFactAssertion(FrontEnd p, String s) {
//		// FIXME This needs help!!
//		super(p, s);
//	}
//
//	private void setMarkerAdded(IMarker marker) {
//		addedMarkers.add(marker);
//	}
//
//	private boolean isMarkerAdded(IMarker marker) {
//		return addedMarkers.contains(marker);
//	}
//
//	public abstract IJavaElement getCompilationUnit();
//
//	/**
//	 * Returns the eclipse JDT's handle identifier for the compilation unit represented by this bucket.
//	 */
//	public String getCUHandle() {
//		return getCompilationUnit().getHandleIdentifier();
//	}
//
//	private int getMarkerID() {
//		return ++markerIDCt;
//	}
//
//	@Feature(names = { "./annotations" })
//	public void assertAnnotation(Object representation, Object annotRep) {
//		insertFact("annotation", representation, annotRep);
//	}
//
//	@Feature(names = { "./annotations" })
//	public void assertAnnotationAttribute(Object representation, String name, String literalValue) {
//		insertFact("attribute", representation, name, literalValue);
//	}
//
//	public void assertCall(String callType, // one of the call strings declared above
//			Object callerRep, String callerName, Object calledRep, String calledName, int start, int length) {
//
//		String srString = callerName + " calls " + calledName;
//		Object sr = makeSourceLocation(start, length);
//
//		insertFact(callType, callerRep, calledRep, sr);
//		insertFact("primLabel", sr, srString);
//	}
//
//	public void assertInstanceOf(Object callerRep, String callerName, Object typeRep, String typeName, int start, int length) {
//
//		String srString = callerName + "calls instanceOf " + typeName;
//		Object sr = makeSourceLocation(start, length);
//
//		insertFact("instanceOf", callerRep, typeRep, sr);
//		insertFact("primLabel", sr, srString);
//	}
//
//	public void assertAccess(String accessType, // "reads" or "writes"
//			Object accessor, String accessorName, Object accessedField, String accessedName, int start, int length) {
//
//		String srString = accessorName + " " + accessType + " " + accessedName;
//
//		Object sr = makeSourceLocation(start, length);
//
//		insertFact(accessType, accessor, accessedField, sr);
//		insertFact("primLabel", sr, srString);
//	}
//
//	/**
//	 * creates and inserts for the given object a new SourceLocation created from the given startPos and length, along with the IFile resource corresponding to this compilation unit bucket.
//	 */
//	public @Export(to = { "./annotations" })
//	void assertSourceLocation(Object elementRep, int startPos, int length) {
//		insertFact("sourceLocation", elementRep, makeJavaObject(makeSourceLocation(startPos, length)));
//	}
//
//	abstract protected JQueryFileElement makeSourceLocation(int startPos, int length);
//
//	public void assertField(Object field, int startLoc, int length) {
//		insertFact("field", field);
//		assertSourceLocation(field, startLoc, length);
//	}
//
//	public void assertMethod(Object method, int startLoc, int length) {
//		insertFact("method", method);
//		assertSourceLocation(method, startLoc, length);
//	}
//
//	public void assertConstructor(Object constructorRep, int startLoc, int length) {
//		insertFact("constructor", constructorRep);
//		assertSourceLocation(constructorRep, startLoc, length);
//	}
//
//	public void assertInterface(Object iface, int startLoc, int length) {
//		insertFact("interface", iface);
//		assertSourceLocation(iface, startLoc, length);
//	}
//
//	public void assertClass(Object type, int startLoc, int length) {
//		insertFact("class", type);
//		assertSourceLocation(type, startLoc, length);
//	}
//
//	public void assertTypeParameter(Object type, Object paramType, int sourceStart, int sourceLen) {
//		insertFact("typevar", paramType);
//		assertSourceLocation(paramType, sourceStart, sourceLen);
//		assertChild(type, paramType);
//	}
//
//	public void assertInitializer(Object initRep, int sourceStart, int sourcelength) {
//		insertFact("initializer", initRep);
//		assertSourceLocation(initRep, sourceStart, sourcelength);
//	}
//
//	public void assertParams(Object method, RBTerm[] argTypes) {
//		insertFact("params", method, FrontEnd.makeList(argTypes));
//		HashSet seenTypes = new HashSet();
//		for (int i = 0; i < argTypes.length; i++) {
//			if (!seenTypes.contains(argTypes[i])) {
//				seenTypes.add(argTypes[i]);
//				insertFact("arg", method, argTypes[i]);
//			}
//		}
//	}
//
//	public void assertChild(Object parent, Object child) {
//		insertFact("child", parent, child);
//	}
//
//	/**
//	 * Method assertCompilationUnit.
//	 * 
//	 * @param iCompilationUnit
//	 */
//	public void assertCompilationUnit(Object CU) {
//		logger.debug("AssertCU: " + CU);
//		insertFact("cu", CU);
//		assertSourceLocation(CU, 0, 0);
//	}
//
//	public void assertExtends(Object subT, Object superT) {
//		insertFact("extends", subT, superT);
//	}
//
//	public void assertImplements(Object type, Object implemented) {
//
//		insertFact("implements", type, implemented);
//
//	}
//
//	public void assertModifier(Object obj, String mod) {
//		insertFact("modifier", obj, mod);
//	}
//
//	public void assertModifiers(Object rep, int modifiers) {
//		if (Modifier.isAbstract(modifiers)) {
//			assertModifier(rep, "abstract");
//		}
//
//		if (Modifier.isNative(modifiers)) {
//			assertModifier(rep, "native");
//		}
//
//		if (Modifier.isFinal(modifiers)) {
//			assertModifier(rep, "final");
//		}
//
//		if (Modifier.isPrivate(modifiers)) {
//			assertModifier(rep, "private");
//		}
//
//		if (Modifier.isProtected(modifiers)) {
//			assertModifier(rep, "protected");
//		}
//
//		if (Modifier.isPublic(modifiers)) {
//			assertModifier(rep, "public");
//		}
//
//		if (Modifier.isStatic(modifiers)) {
//			assertModifier(rep, "static");
//		}
//
//		if (Modifier.isStrictfp(modifiers)) {
//			assertModifier(rep, "strictfp");
//		}
//
//		if (Modifier.isSynchronized(modifiers)) {
//			assertModifier(rep, "synchronized");
//		}
//
//		if (Modifier.isTransient(modifiers)) {
//			assertModifier(rep, "transient");
//		}
//
//		if (Modifier.isVolatile(modifiers)) {
//			assertModifier(rep, "volatile");
//		}
//	}
//
//	public void assertName(Object obj, String objName) {
//		insertFact("name", obj, objName);
//	}
//
//	public void assertMarkers(Object rep, int sourceStart, int sourceLength) {
//		try {
//			for (int i = 0; i < markers.length; i++) {
//				if (!isMarkerAdded(markers[i])) {
//					int start = markers[i].getAttribute(IMarker.CHAR_START, 0);
//					int end = markers[i].getAttribute(IMarker.CHAR_END, 0);
//					String message = (String) markers[i].getAttribute(IMarker.MESSAGE, "No message");
//					if (start >= sourceStart && end <= (sourceStart + sourceLength)) {
//						// Compiler Errors and Warnings
//						if (markers[i].isSubtypeOf(IMarker.PROBLEM)) {
//							int severity = markers[i].getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
//							if (severity == IMarker.SEVERITY_WARNING) {
//								assertWarning(rep, start, (end - start), message);
//								setMarkerAdded(markers[i]);
//							} else if (severity == IMarker.SEVERITY_ERROR) {
//								assertError(rep, start, (end - start), message);
//								setMarkerAdded(markers[i]);
//							}
//							// Bookmarks
//						} else if (markers[i].isSubtypeOf(IMarker.BOOKMARK)) {
//							assertBookmark(rep, start, (end - start), message);
//							setMarkerAdded(markers[i]);
//							// Tasks
//						} else if (markers[i].isSubtypeOf(IMarker.TASK)) {
//							int priority = markers[i].getAttribute(IMarker.PRIORITY, 999);
//							String stPriority = "Unknown";
//							switch (priority) {
//							case IMarker.PRIORITY_HIGH:
//								stPriority = "High";
//								break;
//							case IMarker.PRIORITY_NORMAL:
//								stPriority = "Normal";
//								break;
//							case IMarker.PRIORITY_LOW:
//								stPriority = "Low";
//								break;
//							}
//							assertTask(rep, stPriority, start, (end - start), message);
//							setMarkerAdded(markers[i]);
//						}
//					}
//				}
//			}
//		} catch (CoreException e) {
//			throw new Error("If this has happened, eclipse is not happy (CoreException): " + e.getMessage());
//		}
//	}
//
//	public void assertError(Object obj, int start, int length, String srString) {
//		Object errorRep = makeTypeCast(type_Error, srString + getCUHandle() + "$" + getMarkerID());
//		insertFact("error", errorRep);
//		assertName(errorRep, srString);
//		assertChild(obj, errorRep);
//		assertSourceLocation(errorRep, start, length);
//	}
//
//	public void assertWarning(Object obj, int start, int length, String srString) {
//		Object warningRep = makeTypeCast(type_Warning, srString + getCUHandle() + "$" + getMarkerID());
//		insertFact("warning", warningRep);
//		assertName(warningRep, srString);
//		assertChild(obj, warningRep);
//		assertSourceLocation(warningRep, start, length);
//	}
//
//	public void assertBookmark(Object obj, int start, int length, String srString) {
//		Object bookmarkRep = makeTypeCast(type_Bookmark, srString + getCUHandle() + "$" + getMarkerID());
//		insertFact("bookmark", bookmarkRep);
//		assertName(bookmarkRep, srString);
//		assertChild(obj, bookmarkRep);
//		assertSourceLocation(bookmarkRep, start, length);
//	}
//
//	public void assertTask(Object obj, String priority, int start, int length, String srString) {
//		Object taskRep = makeTypeCast(type_Task, srString + getCUHandle() + "$" + getMarkerID());
//		insertFact("task", taskRep);
//		insertFact("priority", taskRep, makeJavaObject(priority));
//		assertName(taskRep, srString);
//		assertChild(obj, taskRep);
//		assertSourceLocation(taskRep, start, length);
//	}
//
//	public void assertPackage(Object pkg) {
//		insertFact("package", pkg);
//	}
//
//	public void assertReturnType(Object currMethod, Object returnType) {
//		insertFact("returns", currMethod, returnType);
//	}
//
//	public void assertSignature(Object methodRep, String signature) {
//		insertFact("signature", methodRep, signature);
//	}
//
//	public void assertThrows(Object currMethod, Object exceptionType) {
//		insertFact("throws", currMethod, exceptionType);
//	}
//
//	public void assertTypeOf(Object field, Object typeOfField) {
//		insertFact("type", field, typeOfField);
//	}
//
//	public void assertJavadocTag(Object element, String tag, String value) {
//		IJavadocTagProcessor tagProcessor = (IJavadocTagProcessor) javadocTagProcessors.get(tag);
//		if (tagProcessor != null) {
//			tagProcessor.processTag(this, element, tag, value);
//		}
//		insertFact("tag", element, tag, value);
//	}
//
//	public void assertJavadocLocation(Object elementRep, int start, int length) {
//
//		insertFact("javadocLocation", elementRep, makeJavaObject(makeSourceLocation(start, length)));
//	}
//
//	public void assertElementLocation(Object elementRep, int start, int length) {
//
//		if (cuSource == null) {
//			ICompilationUnit cu = (ICompilationUnit) getCompilationUnit();
//			try {
//				cuSource = cu.getSource();
//			} catch (JavaModelException e) {
//				e.printStackTrace();
//				return;
//			}
//		}
//
//		// TODO: Figure out what the code below is really for and why it sometimes
//		// Throws an outOfBoundsExcpetion for the call to charAt.
//
//		// Calculate start of javadoc so that it includes all
//		// white space before the comment up to start of line.
//		// int idx = start-1;
//		// char c = cuSource.charAt(idx);
//		// while (idx>0 && (c == ' ' || c == '\t')) {
//		// idx--;
//		// length++;
//		// c = cuSource.charAt(idx);
//		// }
//		// start = idx + 1;
//
//		insertFact("elementLocation", elementRep, makeJavaObject(makeSourceLocation(start, length)));
//	}
//
//	public void insertFact(String predName, Object[] args) {
//		JQueryAPI.getFactBase().insert(this, predName, args);
//	}
//
//	/**
//	 * Inserts a fact "?predName(?arg1)" into the database, first converting Object arg1 into RBJavaObjects if necessary..
//	 */
//	public void insertFact(String predName, Object arg1) {
//		insertFact(predName, new Object[] { arg1 });
//	}
//
//	/**
//	 * Inserts a fact "?predName(?arg1, ?arg2)" into the database, first converting Objects arg1 and arg2 into a RBJavaObject if necessary.
//	 */
//	public void insertFact(String predName, Object arg1, Object arg2) {
//		insertFact(predName, new Object[] { arg1, arg2 });
//	}
//
//	/**
//	 * Inserts a fact "?predName(?arg1, ?arg2, ?arg3)" into the database, first converting Objects arg1, arg2 and arg3 into RBJavaObjects if necessary..
//	 */
//	public void insertFact(String predName, Object arg1, Object arg2, Object arg3) {
//		insertFact(predName, new Object[] { arg1, arg2, arg3 });
//	}
//}
