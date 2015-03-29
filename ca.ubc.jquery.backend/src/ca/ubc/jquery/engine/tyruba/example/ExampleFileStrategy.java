///*
// * Created on Aug 24, 2004
// */
//package ca.ubc.jquery.engine.tyruba.example;
//
//import java.util.Collection;
//
//import org.eclipse.core.resources.IFile;
//import org.eclipse.core.resources.IResource;
//import org.eclipse.core.resources.IResourceDelta;
//import org.eclipse.core.resources.ResourcesPlugin;
//import org.eclipse.core.runtime.IAdaptable;
//import org.eclipse.core.runtime.Path;
//import org.eclipse.ui.IEditorPart;
//import org.eclipse.ui.PartInitException;
//import org.eclipse.ui.PlatformUI;
//import org.eclipse.ui.ide.IDE;
//import org.eclipse.ui.texteditor.ITextEditor;
//
//import tyRuBa.engine.FrontEnd;
//import ca.ubc.jquery.JQueryBackendPlugin;
//import ca.ubc.jquery.api.JQueryFactBaseResource;
//import ca.ubc.jquery.api.JQueryFileElement;
//import ca.ubc.jquery.api.JQueryResourceManager;
//import ca.ubc.jquery.api.tyruba.IQueryableResourceStrategy;
//
///**
// * An example file strategy for .jquery files.  It just adds facts about
// * documents, lines in the document and words in those lines. Very simple, 
// * maybe not so useful for querying text files, but it's an example for extending JQuery.
// * 
// * -There are custom icons in the example folder of the icons directory
// * -There are custom label/icon, type, predicate, and menu rub files in the 
// *  rules/example directory.  They are all included by their respective JQuery rub file
// *  (i.e. rules/example/label.rub is included by LabelProvider.rub)
// * 
// * @author riecken
// */
//public class ExampleFileStrategy implements IQueryableResourceStrategy {
//
//	public void parse() {
//		// does nothing (for now...) only here to conform to API
//	}
//
//    public boolean rightType(IAdaptable adaptable) {
//        if (adaptable instanceof IFile) {
//            IFile file = (IFile) adaptable;
//            if (file.getFileExtension().equalsIgnoreCase("jquery")) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public void addApplicableElementToCollection(IAdaptable element, Collection c) {
//        if (element instanceof IFile) {
//            IFile file = (IFile) element;
//            if (file.getFileExtension().equalsIgnoreCase("jquery")) {
//                c.add(element);
//            }
//        }
//    }
//
//    public JQueryFactBaseResource makeBucket(IAdaptable adaptable, FrontEnd frontend, JQueryResourceManager rbm) {
//    		//Note: this example doesn't require access to the RuleBaseManager. 
//    		// The reason it is there is for Buckets that want to register file dependencies
//    	    // and add dependent files to the bucket collection automatically.
//    		return new ExampleFileBucket(frontend, (IFile) adaptable);
//    }
//
//    public IAdaptable resourceDelta(IResourceDelta delta) {
//        IResource res = delta.getResource();
//        if (res.getType() == IResource.FILE) {
//            IFile file = (IFile) res;
//            if (file.getFileExtension().equalsIgnoreCase("jquery")) {
//                return file;
//            }
//        }
//        return null;
//    }
//
//    public void openInEditor(JQueryFileElement loc) {
//        try {
//            IResource file = ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(loc.locationID));
//            if (file != null && file.getType() == IResource.FILE) {
//                IEditorPart editor = IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow()
//                        .getActivePage(), (IFile) file);
//                if (editor != null && editor instanceof ITextEditor) {
//                    ((ITextEditor) editor).selectAndReveal(loc.start, loc.length);
//                }
//            }
//        } catch (PartInitException e) {
//            JQueryBackendPlugin.error(e);
//        }
//    }
//
//}