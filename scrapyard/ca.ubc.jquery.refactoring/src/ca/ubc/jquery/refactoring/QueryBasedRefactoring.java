package ca.ubc.jquery.refactoring;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ImportRewrite;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.TextFileChange;
import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.text.edits.TextEditGroup;

import ca.ubc.jquery.JQueryTreeBrowserPlugin;
import ca.ubc.jquery.api.JQueryAPI;
import ca.ubc.jquery.api.JQueryFileElement;
import ca.ubc.jquery.refactoring.RefactoringTargetSet.RefactoringTarget;

/**
 * Abstract base class for Eclipse LTK refactorings based on the results of a JQuery query.
 * @author awjb
 */
public abstract class QueryBasedRefactoring extends Refactoring {
	protected final RefactoringTargetSet targets;
	
	private Change calculatedChange = null;
	
	/**
	 * AST-rewriting information associated with a particular compilation unit
	 * @author awjb
	 */
	public class RewritingInfo {
		private final ASTRewrite astRewrite;
		private final ImportRewrite importRewrite;
		private final CompilationUnit astCUNode;

		private RewritingInfo (ASTRewrite astRewrite, ImportRewrite importRewrite,
				CompilationUnit astCUNode) {
			this.astRewrite = astRewrite;
			this.importRewrite = importRewrite;
			this.astCUNode = astCUNode;
		}

		/**
		 * @return An {@link ASTRewrite} that keeps track of changes to the compilation unit's AST
		 * (except for import statements)
		 */
		public ASTRewrite getASTRewrite() {
			return astRewrite;
		}

		/**
		 * @return An {@link ImportRewrite} that keeps track of changes to import statements
		 */
		public ImportRewrite getImportRewrite() {
			return importRewrite;
		}

		/**
		 * @return AST {@link CompilationUnit} associated with this rewriting information
		 */
		public CompilationUnit getASTCUNode() {
			return astCUNode;
		}
	}
	
	public QueryBasedRefactoring (RefactoringTargetSet targets) {
		super();
		this.targets = targets;
	}
	
	@Override
	public RefactoringStatus checkFinalConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (pm != null) {
			pm.beginTask("", 1); //$NON-NLS-1$
			pm.worked(1);
			pm.done();
		}
		try {
			calculatedChange = calculateChange();
		} catch (Exception e) {
			return RefactoringStatus.createFatalErrorStatus(e.getMessage());
		}
		return new RefactoringStatus();
	}

	@Override
	public RefactoringStatus checkInitialConditions(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (pm != null) {
			pm.beginTask("", 1); //$NON-NLS-1$
			pm.worked(1);
			pm.done();
		}

		if (targets.isEmpty()) {
			return RefactoringStatus.createFatalErrorStatus("No targets selected for refactoring");
		} else {
			return new RefactoringStatus();
		}
	}

	@Override
	public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
		if (calculatedChange != null) {
			return calculatedChange;
		} else {
			JQueryTreeBrowserPlugin.traceUI("Warning: createChange called when change not calculated through checkFinalConditions. This should not happen.");
			return calculateChange();
		}
	}
	
	@SuppressWarnings("unchecked")
	protected Change calculateChange () throws CoreException {
		HashMap<ICompilationUnit, List<TextEditGroup>>changes = new HashMap<ICompilationUnit, List<TextEditGroup>>();
		HashMap<ICompilationUnit, RewritingInfo> rewritingInfos = new HashMap<ICompilationUnit, RewritingInfo>();
		
		CompositeChange rootChange = new CompositeChange(getName());
		for (Object obj : targets) {
			RefactoringTarget target = (RefactoringTarget)obj;
			
			JQueryFileElement fe = JQueryAPI.getFileElement(target.get(0));
			IFile file = fe.getSourceFile();
			IAdaptable a = file;
			IJavaElement element = ((IJavaElement) a.getAdapter(IJavaElement.class));
			if (element instanceof ICompilationUnit) {
				ICompilationUnit cu = (ICompilationUnit) element;
				if (cu.hasUnsavedChanges()) {
					throw new IllegalStateException("Not safe to execute JQuery-based refactoring because " + file.getName() + " has unsaved changes. Save this file and try again.");
				}
				
				RewritingInfo rewritingInfo;
				if (rewritingInfos.containsKey(cu)) {
					rewritingInfo = rewritingInfos.get(cu);
				} else {
					rewritingInfo = makeRewritingInfo(cu);
					rewritingInfos.put(cu, rewritingInfo);
				}

				// Rather inefficient!
				LocationFinderVisitor visitor = new LocationFinderVisitor(fe, getMatchingNodesType());
				rewritingInfo.getASTCUNode().accept(visitor);
				TextEditGroup group = handleTargetNode(visitor.getFoundNode(), target, rewritingInfo);

				if (group != null) {
					List<TextEditGroup> list = changes.get(cu);
					if (list == null) {
						list = new LinkedList<TextEditGroup>();
					}
					list.add(group);

					changes.put(cu, list);
				}
			}
		}

		for (Object entry : changes.entrySet()) {
			Map.Entry pair = (Map.Entry)entry;
			ICompilationUnit cu = (ICompilationUnit)pair.getKey();
			IFile file = (IFile) cu.getUnderlyingResource();
			List list = (List)pair.getValue();
			
			RewritingInfo rewritingInfo = rewritingInfos.get(cu);
			
			TextFileChange change = new TextFileChange(file.getName(), file);
			TextEdit mainEdit = rewritingInfo.getASTRewrite().rewriteAST();
			TextEdit importsEdit = rewritingInfo.getImportRewrite().rewriteImports(null);
			
			MultiTextEdit bothEdits = new MultiTextEdit();
			bothEdits.addChild(importsEdit);
			bothEdits.addChild(mainEdit);
			change.setEdit(bothEdits);
			change.setTextType("java");
			
			for ( Object groupObj : list ) {
				change.addTextEditGroup((TextEditGroup)groupObj);
			}
			
			rootChange.add((Change) change);
		}

		return rootChange;
	}

	/**
	 * @return The wizard page necessary to get input for this refactoring, or null if there
	 * is no such page
	 */
	public UserInputWizardPage getWizardPage () {
		return null;
	}
	
	/**
	 * @return A {@link Class} giving the type of node to look for in an AST when searching for
	 * target nodes for this refactoring.
	 */
	abstract protected Class<? extends ASTNode> getMatchingNodesType ();
	
	/**
	 * @param foundNode The {@link ASTNode} corresponding to the code element to process
	 * @param target A {@link RefactoringTarget} giving information (from a JQuery query result) about the
	 * code element to process 
	 * @param rewritingInfo A {@link RewritingInfo} containing objects to use for rewriting the AST for
	 * this node
	 * @return A {@link TextEditGroup} giving the changes to make, or null if no changes are made
	 * @throws JavaModelException
	 */
	abstract protected TextEditGroup handleTargetNode (ASTNode foundNode, RefactoringTarget target, 
			RewritingInfo rewritingInfo) throws JavaModelException;

	/**
	 * @return Whether to resolve bindings when creating ASTs for compilation units.
	 */
	protected boolean requireBindings () {
		return false;
	}
	
	private RewritingInfo makeRewritingInfo (ITypeRoot cu) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(cu); // set source
		parser.setResolveBindings(requireBindings());
		CompilationUnit astCUNode = (CompilationUnit) parser.createAST(null /* IProgressMonitor */); // parse

		AST ast = astCUNode.getAST();
		ASTRewrite astRewrite = ASTRewrite.create(ast);

		ImportRewrite importRewrite = ImportRewrite.create(astCUNode, true);
		return new RewritingInfo(astRewrite, importRewrite, astCUNode);
	}
}
