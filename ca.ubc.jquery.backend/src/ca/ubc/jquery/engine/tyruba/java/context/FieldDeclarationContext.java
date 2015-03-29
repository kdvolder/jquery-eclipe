/*
 * Created on Jan 30, 2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package ca.ubc.jquery.engine.tyruba.java.context;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import tyRuBa.engine.RBTerm;
import tyRuBa.modes.TypeConstructor;
import annotations.Export;
import annotations.Feature;
import ca.ubc.jquery.tyruba.javadoc.JavadocComment;
import ca.ubc.jquery.tyruba.javadoc.JavadocTag;

/**
 * @author dsjanzen
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public @Feature(names={"./annotations","./NONE"}) class FieldDeclarationContext extends ContextTracker {

	public @Feature(names={"./annotations","./NONE"}) interface FieldAction {
		public void doIt(FieldContext field);
	}

	private List tags = new Vector();
	private JavadocComment comment = null;
	private int start;
	private int length;
	private @Export(to={"./annotations"}) List delayedFieldActions = new ArrayList();
	
	/**
	 * @param containingContext
	 */
	public FieldDeclarationContext(ContextTracker containingContext) {
		super(containingContext);
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.engine.context.ContextTracker#assertJavadocTag(ca.ubc.jquery.ast.JavadocTag)
	 */
	public void assertJavadocTag(JavadocTag tag) {
		tags.add(tag);
	}

	public void assertJavadocComment(JavadocComment comment) {
		this.comment = comment;
	}

	public void assertTags(RBTerm rep) {
		
		bucket.assertElementLocation(rep, start, length);
	
		if (comment != null) {
			bucket.assertJavadocLocation(rep, comment.getStart(), comment.getLength());
		}
		
		for (Iterator iter = tags.iterator(); iter.hasNext();) {
			JavadocTag tag = (JavadocTag) iter.next();
			bucket.assertJavadocTag(rep, tag.getName(), tag.getValue());
		}
	}
	
	/* (non-Javadoc)
	 * @see ca.ubc.jquery.engine.context.ContextTracker#getName()
	 */
	protected String getName() {
		return containing.getName();
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.engine.context.ContextTracker#getRepresentation()
	 */
	protected Object getRepresentation() {
		return containing.getRepresentation();
	}

	/* (non-Javadoc)
	 * @see ca.ubc.jquery.engine.context.ContextTracker#getContextType()
	 */
	protected TypeConstructor getContextType() {
		return containing.getContextType();
	}

	public void assertElementLocation(int start, int length) {							 	
		this.start = start;
		this.length = length;
	}

	@Feature(names="./annotations")
	public void assertAnnotation(final AnnotationContext annot) {
		delayedFieldActions.add(new FieldAction() {
			@Feature(names={"./annotations"})
			public void doIt(FieldContext field) {
				field.assertAnnotation(annot);
			}
		});
	}

	@Override
	public void assertChild(ContextTracker childContext) {
		super.assertChild(childContext);
		for (Object action : delayedFieldActions) {
			((FieldAction)action).doIt((FieldContext) childContext);
		}
	}

}
