package org.lejos.eclipse;
/*
* $Log$
*/

////////////////////////////////////////////////////////
/**
 *
 * This class is the preference page for the Lejos Plugin
 *
 * @author Christophe Ponsard
 * @version 1.1.0 
 *   
 */

import java.io.File;

import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Insert the type's description here.
 * @see PreferencePage
 */
public class LejosPreference extends PreferencePage implements IWorkbenchPreferencePage {
	
	private LejosDirEditor fInstallDir;
	private Combo fPort;
	private Combo fSpeed;
	private Text fUSP;
		
	/**
	 * The constructor.
	 */
	public LejosPreference() {
	}

	/**
	 * Insert the method's description here.
	 * @see PreferencePage#init
	 */
	public void init(IWorkbench workbench)  {
		//Initialize the preference store we wish to use
		setPreferenceStore(LejosPlugin.getDefault().getPreferenceStore());		
	}

	protected void performDefaults() {
		//fInstallDir.setStringValue(LejosPlugin.DEFAULT_LEJOS_PATH);
		fInstallDir.setStringValue(LejosPlugin.getDefault().getPlatformLejosPath());
	}
	
	/** 
	 * Method declared on IPreferencePage. Save the
	 * author name to the preference store.
	 */
	public boolean performOk() {
		// perform checks
		String path=fInstallDir.getStringValue();

    	int port=fPort.getSelectionIndex();
	   	boolean isFast=(fSpeed.getSelectionIndex()==1);
		
		LejosPlugin.getDefault().setLejosPath(path);
		LejosPlugin.getDefault().setLejosPort(port);
		LejosPlugin.getDefault().setLejosIsFast(isFast);
		return super.performOk();
	}	
		
	// to get around protected visibility of createContents
	public Control getContents(Composite parent) {
		 return createContents(parent);
	}

	/**
	 * Insert the method's description here.
	 * @see PreferencePage#createContents
	 */
	protected Control createContents(Composite parent)  {
		Composite composite= new Composite(parent, SWT.NONE);
		
		GridLayout gl= new GridLayout(3,false);
		gl.marginHeight=20;
		gl.marginWidth=20;
		gl.verticalSpacing=20;
		composite.setLayout(gl);

		// controls
		
		fInstallDir=new LejosDirEditor("LejosDir", "leJOS installation directory", composite);
        fInstallDir.setStringValue(LejosPlugin.getDefault().getPlatformLejosPath());
				
		Label labelPort = new Label(composite, SWT.LEFT);
		labelPort.setText("RCX communication port");
		
		fPort=new Combo(composite,SWT.DROP_DOWN | SWT.READ_ONLY);
		fPort.setItems(LejosPlugin.getPorts());
		fPort.select(LejosPlugin.getDefault().getLejosPort());

		Label labelSpeed = new Label(composite, SWT.LEFT);
		labelSpeed.setText("Data transfer rate");
		
		fSpeed=new Combo(composite,SWT.DROP_DOWN | SWT.READ_ONLY);
		fSpeed.add("slow");
		
		// fast download not supported yet
/*		fSpeed.add("fast");
		if (LejosPlugin.getDefault().getLejosIsFast())
			fSpeed.select(1);
		else
		*/
			fSpeed.select(0);
			
		// user specific port (only for Mac)
		if(LejosPlugin.isMacOSX()) {
			fUSP = new Text(composite, SWT.SINGLE | SWT.BORDER);
			String usp = LejosPlugin.getDefault().getUSP();
			fUSP.setText(usp);
			fUSP.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent ev) {
					String usp = fUSP.getText();
					LejosPlugin.getDefault().setUSP(usp);
					fPort.setItem(fPort.getItemCount() - 1, usp);
				}
			});
		} // if
		
		// layout
		GridData data = new GridData();
		labelPort.setLayoutData(data);
		
		data = new GridData();
		data.horizontalSpan = 2;
		fPort.setLayoutData(data);

		data = new GridData();
		labelSpeed.setLayoutData(data);

		data = new GridData();
		data.horizontalSpan = 2;
		fSpeed.setLayoutData(data);

		if(LejosPlugin.isMacOSX()) {
			Label labelUSP = null;
			String usp = LejosPlugin.getDefault().getUSP();
			fPort.add(usp);
			labelUSP = new Label(composite, SWT.LEFT);
			labelUSP.setText("User Specific Port");
			data = new GridData();
			labelUSP.setLayoutData(data);
			data = new GridData();
			data.horizontalSpan = 2;
			data.horizontalAlignment = GridData.FILL;
			fUSP.setLayoutData(data);
		} // if

		return composite;
	}
	
	class LejosDirEditor extends DirectoryFieldEditor {
		
		Label warning;
		
		LejosDirEditor(String name, String label, Composite parent) {
			super(name,label,parent);
			setValidateStrategy(VALIDATE_ON_KEY_STROKE);
			warning=new Label(parent,SWT.LEFT);
	        GridData data = new GridData(GridData.FILL_HORIZONTAL);
			data.horizontalSpan = 3;
			warning.setLayoutData(data);		
		}
		
		protected boolean doCheckState() {
			String fileSep=System.getProperty("file.separator");
			File file=new File(getStringValue(),
				"lib" + fileSep + "classes.jar");
			boolean res = file.exists();
			//boolean res = true;

			if (res) 
				warning.setText("");
			else
				warning.setText("      >>>> Warning: invalid leJOS path <<<<");
			
			return res;
		}	
	}

}
