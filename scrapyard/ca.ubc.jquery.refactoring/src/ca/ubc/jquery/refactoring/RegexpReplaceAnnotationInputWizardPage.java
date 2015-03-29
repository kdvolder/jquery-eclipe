package ca.ubc.jquery.refactoring;

import org.eclipse.ltk.ui.refactoring.UserInputWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class RegexpReplaceAnnotationInputWizardPage extends UserInputWizardPage {
	private static final String PAGE_TITLE = "Regular Expression Annotation Rewriting";

	private static final String FROM_LABEL = "Enter regular expression to search for in annotation";
	private static final String TO_LABEL = "Enter replacement string for matches";
	
	private Text fromTextbox;
	private Text toTextbox;	

	private RegexpReplaceAnnotationRefactoring regexpReplaceAnnotationRefactoring;

	public RegexpReplaceAnnotationInputWizardPage(RegexpReplaceAnnotationRefactoring regexpReplaceAnnotationRefactoring) {
		super(PAGE_TITLE);
		setTitle(PAGE_TITLE);

		this.regexpReplaceAnnotationRefactoring = regexpReplaceAnnotationRefactoring;
	}

	public void createControl(Composite parent) {
		// based on org.eclipse.ui.dialogs.WizardNewFileCreationPage
		initializeDialogUnits(parent);
		// top level group
		Composite topLevel = new Composite(parent, SWT.NONE);
		topLevel.setLayout(new GridLayout());
		topLevel.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		topLevel.setFont(parent.getFont());

		Label fromLabel = new Label(topLevel, SWT.LEFT);
		fromLabel.setText(FROM_LABEL + ":");
		
		fromTextbox = new Text(topLevel, SWT.SINGLE | SWT.BORDER);
		fromTextbox.setToolTipText(FROM_LABEL);
		
		fromTextbox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (fromTextbox.getText().length() != 0) {
					regexpReplaceAnnotationRefactoring.setFrom(fromTextbox.getText());
					setErrorMessage(null);
					RegexpReplaceAnnotationInputWizardPage.this.setPageComplete(true);
				} else {
					setErrorMessage("Name of feature to rename not provided");
					RegexpReplaceAnnotationInputWizardPage.this.setPageComplete(false);
				}
			}
		});

		Label toLabel = new Label(topLevel, SWT.LEFT);
		toLabel.setText(TO_LABEL + ":");

		toTextbox = new Text(topLevel, SWT.SINGLE | SWT.BORDER);
		toTextbox.setToolTipText(TO_LABEL);

		toTextbox.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (toTextbox.getText().length() != 0) {
					regexpReplaceAnnotationRefactoring.setTo(toTextbox.getText());
					setErrorMessage(null);
					RegexpReplaceAnnotationInputWizardPage.this.setPageComplete(true);
				} else {
					setErrorMessage("No new name for feature provided");
					RegexpReplaceAnnotationInputWizardPage.this.setPageComplete(false);
				}
			}
		});

		// Show description on opening
		setErrorMessage(null);
		setMessage(null);
		setControl(topLevel);

		// cf. JavaTypeCompletionProcessor, NewClassWizardPage
	}
}
