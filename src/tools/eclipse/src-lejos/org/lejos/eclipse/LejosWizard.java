package org.lejos.eclipse;
/*
* $Log$
* Revision 1.1  2003/10/05 16:34:06  mpscholz
* lejos plugin for Eclipse
*
*/

////////////////////////////////////////////////////////
/**
 *
 * This class is the project wizard for the Lejos Plugin
 *
 * @author Christophe Ponsard
 * @version 1.1.0 
 *   
 */

import java.io.File;

import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.ui.wizards.NewProjectCreationWizard;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;

/**
 * This is rather a hack for eclipse 2.0 (using internal class)
 * @see Wizard
 */
public class LejosWizard extends NewProjectCreationWizard {
// this code makes use of eclipse 2.0 internals

	
	/*
	 * @see Wizard#performFinish
	 */		
	public boolean performFinish() {
		boolean res=super.performFinish();
		
		// updating classspath
		customize();
		
		return res;
	}
		    
	// customize the classpath for compiling !
    private void customize() {

        try {
  			JavaCapabilityConfigurationPage page=(JavaCapabilityConfigurationPage)getPage("JavaCapabilityConfigurationPage");
			if (page!=null) {
				IJavaProject prj=page.getJavaProject();				
				IClasspathEntry[] ocp=prj.getRawClasspath();
				IClasspathEntry[] ncp=new IClasspathEntry[ocp.length+3];
				for(int i=0; i<ocp.length; i++) ncp[i]=ocp[i];
				if (ocp.length>0) ncp[ncp.length-1]=ocp[ocp.length-1];

				// JVM libs
				String home=LejosPlugin.getDefault().getLejosPath();
				File fp=new File(home,"lib/classes.jar");
				Path cp=new Path(fp.getCanonicalPath());
				IClasspathEntry icp=JavaCore.newLibraryEntry(cp,null,null);
				ncp[ncp.length-4]=icp;

				fp=new File(home,"lib/rcxcomm.jar");
				cp=new Path(LejosPlugin.getPath(fp));
				icp=JavaCore.newLibraryEntry(cp,null,null);
				ncp[ncp.length-3]=icp;

				fp=new File(home,"lib/pcrcxcomm.jar");
				cp=new Path(LejosPlugin.getPath(fp));
				icp=JavaCore.newLibraryEntry(cp,null,null);
				ncp[ncp.length-2]=icp;				

				prj.setRawClasspath(ncp,null);
			}	
        } catch(Exception e) {
        }
		
/*		
		// TODO conditionally enable RCX menu in the main toolbar (maybe as extension point)
		URL[] pp=BootLoader.getPluginPath(LejosPlugin.plugin.getDescriptor().getInstallURL());
		for(int i=0; i<pp.length; i++) System.out.println(pp[i]);
		
		System.out.println("IURL: "+BootLoader.getInstallURL()+LejosPlugin.plugin.getDescriptor().getInstallURL());
*/		
	}

}
