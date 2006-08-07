package org.lejos.eclipse.actions;
/*
* $Log$
* Revision 1.1  2003/10/05 16:34:06  mpscholz
* lejos plugin for Eclipse
*
*/

////////////////////////////////////////////////////////
/**
 *
 * This class is the bytecode RCX download action for the Lejos Plugin
 *
 * @author Christophe Ponsard
 * @version 1.1.0 
 *   
 */

import java.io.*;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.launching.IVMInstall;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorActionDelegate;
import org.eclipse.ui.IEditorPart;
import org.lejos.eclipse.LejosPlugin;


/**
 * Insert the type's description here.
 * @see IWorkbenchWindowActionDelegate
 */
public class DownloadAction implements IEditorActionDelegate {  //, IEditorActionDelegate, IViewActionDelegate {

	////////////////////////////////////////////
	// constants
	////////////////////////////////////////////
	private static final String BYTECODE_DOWNLOAD_UTILITY = "bin/lejosdl";
    
	////////////////////////////////////////////
	// fields
	////////////////////////////////////////////
	private IEditorPart editor;
	
	/**
	 * The constructor.
	 */
	public DownloadAction() {
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action)  {
		 try {	 	
    		LejosDownloadRunner op = new LejosDownloadRunner();
		    new ProgressMonitorDialog(editor.getSite().getShell()).run(true, true, op);
		    
         	if (op.hasError()) {		 
         		String msg="RCX Byte Code Download Error";
  	 	 		ErrorDialog.openError(editor.getSite().getShell(), msg, msg,
									  new Status(IStatus.ERROR, "org.RCX", 0, op.getError(), null));
         	}		    
		 } catch (InvocationTargetException e) {
		 } catch (InterruptedException e) {
		 }	
         	
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection)  {
	}

	public void setActiveEditor(IAction action, IEditorPart targetEditor)  {
		editor=targetEditor;
	}

	
//	public void init(IViewPart part) {
//	}
	
//	public void setActiveEditor(IAction action, IEditorPart part) {
//	}
	
	
	// for now this will only handle slow download and rely on stderr output
	// possible download failures are returned through hasError() and getError()
	class LejosDownloadRunner implements IRunnableWithProgress {
		
		private String error;
		
		boolean hasError(){
		  return error!=null;
		}
		
		String getError() {
		  return error;
		}
			
		public void run(IProgressMonitor monitor) {
			// no error
			error=null;
			

	        try {
	        	// get paths
				IVMInstall vmInstall= JavaRuntime.getDefaultVMInstall();
				File jvm=new File(vmInstall.getInstallLocation(),"bin/java");
				String home=LejosPlugin.getDefault().getLejosPath();
				File cflib=new File(home,"lib/jtools.jar");
				File rtlib=new File(home,"lib/classes.jar");				
				File rclib=new File(home,"lib/rcxcomm.jar");			
				
				// get file to link	
				ICompilationUnit cu=LejosPlugin.getDefault().getCurrentCompilationUnit();
				IPackageDeclaration[] pk=cu.getPackageDeclarations();
				String code;
				if (pk.length==0)
					code=cu.getElementName();
				else
					code=pk[0].getElementName()+"."+cu.getElementName();
				int pcode=code.lastIndexOf(".java");
				code=code.substring(0,pcode);
				// get path of file without packages
				// may be done in a more sophisticated way, though :-)
				String pathSep=System.getProperty("path.separator");
				String fileSep=System.getProperty("file.separator");
				IFile fmain=LejosPlugin.getDefault().getCurrentJavaFile();
				String main=LejosPlugin.getPath(fmain);
				int packageCounter = 0;
				int index = 0;
				while((index=code.indexOf('.',index+1))>0)
					packageCounter++;
				String codeDir = main;
				for(int i=0;i<=packageCounter;i++) {
					int indexOfLastFileSep = codeDir.lastIndexOf(fileSep);
					if(indexOfLastFileSep>=0)
						codeDir = codeDir.substring(0,indexOfLastFileSep);
				} // for
	        	
	        	//==== STEP 1: build binary file
	        	 
				File tmp=File.createTempFile("bcode",".bin");
				tmp.deleteOnExit();	
				
				// create download command
				String linkTool = "bin" + fileSep + "lejoslink";
				if(LejosPlugin.isWindows())
					linkTool += ".bat";
				File linker = new File(LejosPlugin.getDefault().getLejosPath(),
					linkTool);
				String cmd = LejosPlugin.getPath(jvm) 
						+ " -classpath " + LejosPlugin.getPath(cflib) 
						+ " -Dtinyvm.linker=lejoslink" 
						+  " -Djava.library.path=" + LejosPlugin.getDefault().getLejosPath() + "/bin"
						+ " -Dtinyvm.home=" + LejosPlugin.getDefault().getLejosPath()
						+ " -Dtinyvm.write.order=BE js.tinyvm.TinyVM"
						+ " -classpath " + LejosPlugin.getPath(rtlib) 
						+ pathSep + LejosPlugin.getPath(rclib)
						+ pathSep + "."
						+ pathSep + codeDir
						+ " -o " + LejosPlugin.getPath(tmp)
						+ "  " +  code;		   					
	 //System.out.println("linking: " + cmd);
	
				// execute download command
				monitor.beginTask("Linking",100);
    	        Process p=Runtime.getRuntime().exec(cmd);

				// inspect linker's output    	            	      
            	BufferedReader err=new BufferedReader(new InputStreamReader(p.getErrorStream()));
            	String s;
            	StringBuffer msg=new StringBuffer();

		  	  	while(true) {
		  	  		if (monitor.isCanceled()) {
		  	  			p.destroy();
		  	  			try {
			  	  			p.waitFor();
			  	  			return;
		  	  			} catch(InterruptedException e) {
		  	  			}
   	  	  			  	break;
		  	  		}
		  	  		
			    	s=err.readLine();
				    if (s==null) break;
//System.out.println("--> "+s);
	
					s=s.trim();
					if (s.length()>0) msg.append(s+"\n");
		  	  	}
		  	  	monitor.done();
		  	  			  	  	
		  	 	// check result
		  	  	if (msg.length()>0) {
		  	  		error=msg.toString();
		  	  	 	return;
		  	  	}

//			System.out.println("linking successfull");
				
				//===== STEP 2: download 

				// create download command
				String downloadTool = "bin" + fileSep + "lejosdl";
				if(LejosPlugin.isWindows())
					downloadTool += ".bat";
	        	File lejosdl = new File(LejosPlugin.getDefault().getLejosPath(),
					downloadTool);
	        	int port = LejosPlugin.getDefault().getLejosPort();
	        	String sport = LejosPlugin.getPort(port);
				cmd = LejosPlugin.getPath(lejosdl) + " --tty=" + sport 
					+ " " + LejosPlugin.getPath(tmp);

//System.out.println("download: " + cmd);
	
				// execute download command
    	        p=Runtime.getRuntime().exec(cmd);

				// inspect download's output    	            	      
            	err=new BufferedReader(new InputStreamReader(p.getErrorStream()));
            	int pos;
            	int opg=100;
            	int npg=0;
            	boolean fast=false;
            	msg=new StringBuffer();
                                        
		  	  	while(true) {
		  	  		if (monitor.isCanceled()) {
		  	  			p.destroy();
		  	  			try {
			  	  			p.waitFor();
			  	  			error="User interrupted - please check RCX state before downloading again";
			  	  			return;
		  	  			} catch(InterruptedException e) {
		  	  			}
   	  	  			  	break;
		  	  		}
		  	  				  	  		
			    	s=err.readLine();
				    if (s==null) break;
				    
//				    System.out.println(s);				    			    
				    				    
				    pos=s.indexOf("%");
				    if (pos==-1) {
				      s=s.trim();
				      if (s.length()>0) msg.append(s+"\n");
				      continue;
				    }
				    
				    try {
				    	npg=Integer.parseInt(s.substring(0,pos).trim());
				    	if (opg>npg) {
				      		npg=0;
			    	  		monitor.beginTask("Downloading byte-code",100);			    	  		
				    	} else {				    					      	
				      		monitor.worked(npg-opg);
				      		monitor.subTask(npg+"% completed");
				    	}
				    	opg=npg;		
				    } catch(NumberFormatException e) {
				    }				    
			  	}
			  				  	
		  		monitor.done();
		  		
		  		// check result
		  		if (npg!=100) {
	 				error=msg.toString();
		  		}
		  		
	        } catch(Exception e) {
	        	error=e.getMessage().trim();
//	        	e.printStackTrace();
	        }
       		if (error.length()==0) error="Unknown - probably interrupted or firmware not present";
		}		

	}		
	
}
