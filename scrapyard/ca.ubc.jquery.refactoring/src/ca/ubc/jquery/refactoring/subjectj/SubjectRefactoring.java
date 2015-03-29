package ca.ubc.jquery.refactoring.subjectj;

import org.eclipse.jdt.core.dom.Annotation;

import ca.ubc.jquery.refactoring.QueryBasedRefactoring;
import ca.ubc.jquery.refactoring.RefactoringTargetSet;

public abstract class SubjectRefactoring extends QueryBasedRefactoring {
	protected static final String SUBJECT_ANNOTATION_NAME = "annotations.Subject";
	protected static final String EXPORT_ANNOTATION_NAME = "annotations.Export";
	
	public SubjectRefactoring (RefactoringTargetSet targets) {
		super(targets);
	}
	
//	@Override
//	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
//		if (pm != null) {
//			pm.beginTask("", 1); //$NON-NLS-1$
//			//JQueryAPI.createQuery("annotationDeclaration(?D),qname(?D," + SUBJECT_ANNOTATION_NAME + ")");			
//			pm.worked(1);
//			pm.done();
//		}
//		return new RefactoringStatus();
//	}

	@Override
	protected boolean requireBindings () {
		return true;
	}

	protected boolean isSubjectAnnotation (Annotation annot) {
		return annot.resolveAnnotationBinding().getAnnotationType().getQualifiedName().equals(SUBJECT_ANNOTATION_NAME);
	}
	
	protected boolean isExportAnnotation (Annotation annot) {
		return annot.resolveAnnotationBinding().getAnnotationType().getQualifiedName().equals(EXPORT_ANNOTATION_NAME);
	}
	
	protected enum SubjectAnnotationType { SUBJECT, EXPORT };
	
	protected SubjectAnnotationType getTargetAnnotationType (String name) {
		if (name.toLowerCase().equals("subject")) {
			return SubjectAnnotationType.SUBJECT;
		} else if (name.toLowerCase().equals("export")) {
			return SubjectAnnotationType.EXPORT;
		}
		throw new IllegalArgumentException("Invalid target subject annotation type: " + name);
	}
}
