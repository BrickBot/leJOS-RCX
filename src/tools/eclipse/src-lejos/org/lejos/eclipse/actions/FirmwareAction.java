package org.lejos.eclipse.actions;
/*
* $Log$
*/

////////////////////////////////////////////////////////
/**
 *
 * This class is the firmware RCX download action for the Lejos plugin
 *
 * @author Christophe Ponsard
 * @version 1.1.0 
 *   
 */

import java.io.*;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.*;
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
public class FirmwareAction implements IEditorActionDelegate {
	
	////////////////////////////////////////////
	// constants
	////////////////////////////////////////////
    
	////////////////////////////////////////////
	// fields
	////////////////////////////////////////////
	private IEditorPart editor;
	
	/**
	 * The constructor.
	 */
	public FirmwareAction() {
	}

	/**
	 * Insert the method's description here.
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action)  {
		 try {
    		LejosFirmwareRunner op = new LejosFirmwareRunner();
		    new ProgressMonitorDialog(editor.getSite().getShell()).run(true, true, op);
		    
         	if (op.hasError()) {		 
         		String msg="RCX Firmware Download Error";
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


	// for now this will only handle slow download and rely on stderr output
	// possible download failures are returned through hasError() and getError()
	class LejosFirmwareRunner implements IRunnableWithProgress {
		
		private String error=null;
		
		boolean hasError(){
		  return error!=null;
		}
		
		String getError() {
		  return error;
		}
		
		public void run(IProgressMonitor monitor) {
 
	        try {	        	
	        	// create firmware download command
				String fileSep = System.getProperty("file.separator");
	        	String firmwareDownload = "bin" + fileSep + "firmdl";
	        	if(LejosPlugin.isWindows())
					firmwareDownload += ".bat";
	        	File file=new File(LejosPlugin.getDefault().getLejosPath(),
					firmwareDownload);
				int port=LejosPlugin.getDefault().getLejosPort();
	        	String sport=LejosPlugin.getPort(port);
				String cmd = LejosPlugin.getPath(file)+ " --tty="+sport;
//System.out.println("cmd: "+cmd);
    	        
    	        // execute download command
    	        Process p=Runtime.getRuntime().exec(cmd);
        	  
            	BufferedReader err=new BufferedReader(new InputStreamReader(p.getErrorStream()));
            	String s;
            	int pos;
            	int opg=0;
            	int npg=0;
            	boolean isDownloadSuccessfull = false;
            	StringBuffer msg=new StringBuffer();
                                        
		  	  	while(true) {
		  	  		if (monitor.isCanceled()) {
		  	  			p.destroy();
		  	  			try {
			  	  			p.waitFor();
		  	  			} catch(InterruptedException e) {
		  	  			}
   	  	  			  	break;
		  	  		}
		  	  		
		  	  		
			    	s=err.readLine();
				    if (s==null) break;
				    
				    //System.out.println(s);
				    
				    if(s.trim().startsWith("Firmware downloaded")) {
				    	isDownloadSuccessfull = true;
				    } // if
				    
				    
					if(s.trim().startsWith("Downloading firmware")) {
				      npg=0;
			    	  monitor.beginTask(s,100);
			    	  continue;
					} 
				    
				    pos=s.indexOf("%");
				    if (pos==-1) {
				      s=s.trim();
				      if (s.length()>0) msg.append(s+"\n");
				      continue;
				    }
				    
				    try {
				      	npg=Integer.parseInt(s.substring(0,pos).trim());
				      	monitor.worked(npg-opg);
				      	monitor.subTask(s+" completed");
				      	opg=npg;
				    } catch(NumberFormatException e) {
				    	e.printStackTrace();
				    }				    
			  	}
			  				  	
		  		monitor.done();
		  		
		  		// download successfull?
		  		//if (npg!=100) {
		  		if(!isDownloadSuccessfull)
	 				error=msg.toString();
		  		
		  		
	        } catch(IOException e) {
	        	e.printStackTrace();
	        	error=e.getMessage();
	        }
		}

	}	
	
}
