package org.lejos.eclipse;
/*
* $Log$
*/

////////////////////////////////////////////////////////
/**
 *
 * This class is the entry point for the Lejos Plugin
 *
 * @author Christophe Ponsard
 * @version 1.1.0 
 *   
 * TODO  don't call scripts for download but js.tools download classes directly
 */

import java.io.File;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.ui.IWorkingCopyManager;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.*;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class LejosPlugin extends AbstractUIPlugin {
	
	//The shared instance.
	static LejosPlugin plugin;
	
	//Resource bundle.
	private ResourceBundle resourceBundle;
	
	// LejosPath 
	public static final String LEJOS_PATH = "lejos.path";
	//static final String DEFAULT_LEJOS_PATH = "";
	
	static final String[] WIN_PORTS={"usb","com1","com2"};
	static final String[] LINUX_PORTS={"usb","/dev/ttyS0","/dev/ttyS1"};
	static final String[] MACOSX_PORTS={};
	public static final String LEJOS_PORT ="lejos.port";
	static final int DEFAULT_LEJOS_PORT = 0;

	public static final String LEJOS_ISFAST = "lejos.isFast";
	// fast download mode not supported yet
	static final boolean DEFAULT_LEJOS_ISFAST = false;
	public static final String LEJOS_USP = "lejos.usp";

	// static OS stuff
	private static final int UNKNOWN=0;
	private static final int WIN=1;
	private static final int LINUX=2;
	private static final int MACOSX=3;
	private static int type;
	
	static {
		String os=System.getProperty("os.name").toLowerCase();
		if (os.startsWith("win")) type=WIN;
		else if (os.startsWith("linux")) type=LINUX;
		else if (os.startsWith("mac os x")) type=MACOSX;
		else type=UNKNOWN;		
	}
	
	/**
	 * The constructor.
	 */
	public LejosPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		try {
			resourceBundle= ResourceBundle.getBundle("lejos.lejosPluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		
	}

	/**
	 * Returns the shared instance.
	 */
	public static LejosPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}	

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle= LejosPlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	/** 
	 * Initializes a preference store with default preference values 
	 * for this plug-in.
	 * @param store the preference store to fill
	 */
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		//store.setDefault(LEJOS_PATH, DEFAULT_LEJOS_PATH);
		// eclipse must have been started with "-DLEJOS_HOME" option for this
		// else lejosHome will be null
		String lejosHome = System.getProperty("LEJOS_HOME");
		if(lejosHome==null)
			lejosHome = "<LEJOS_HOME not set>"; 
		store.setDefault(LEJOS_PATH, lejosHome);
		store.setDefault(LEJOS_PORT, DEFAULT_LEJOS_PORT);
		store.setDefault(LEJOS_ISFAST, DEFAULT_LEJOS_ISFAST);
		if (isWindows()) store.setDefault(LEJOS_USP, "usb");
		if (isLinux()) store.setDefault(LEJOS_USP, "/dev/lego0");
		if (isMacOSX()) store.setDefault(LEJOS_USP, "/dev/cu.USA19QI11P1.1");	
	}
	
	void setLejosPath(String path) {
		getPreferenceStore().setValue(LEJOS_PATH, path);
	}
	
	public String getPlatformLejosPath() {
		return getPreferenceStore().getString(LEJOS_PATH);
	}
	
	public String getLejosPath() {
		return getPath(getPreferenceStore().getString(LEJOS_PATH));
	}	
	
	void setLejosPort(int port) {
		getPreferenceStore().setValue(LEJOS_PORT, port);
	}
	
	public int getLejosPort() {
		return getPreferenceStore().getInt(LEJOS_PORT);
	}

	void setLejosIsFast(boolean isFast) {
		getPreferenceStore().setValue(LEJOS_ISFAST, isFast);
	}
	
	public boolean getLejosIsFast() {
		return getPreferenceStore().getBoolean(LEJOS_ISFAST);
	}

	void setUSP(String usp) {
		getPreferenceStore().setValue(LEJOS_USP, usp);	
	}
	
	public String getUSP() {
		return getPreferenceStore().getString(LEJOS_USP);
	}
	
	private IEditorInput getEditorInput() throws IOException {
		IWorkbench wb=PlatformUI.getWorkbench();
		IWorkbenchWindow[] wws=wb.getWorkbenchWindows();
		if (wws.length!=1) throw new IOException("Failed to find workbench window");
		IWorkbenchWindow ww=wws[0];
		
		IWorkbenchPage[] wps=ww.getPages();
		if (wws.length!=1) throw new IOException("Failed to find workbench page");
		IWorkbenchPage wp=wps[0];
		
		IEditorPart ep=wp.getActiveEditor();
		if (ep==null) throw new IOException("Failed to find active editor");
		return ep.getEditorInput();		
	}
	
	public IFile getCurrentJavaFile() throws IOException {
		IEditorInput ei=getEditorInput();
		if (!(ei instanceof IFileEditorInput)) throw new IOException("IFileEditorInput expected");
		IFileEditorInput fei=(IFileEditorInput)ei;
		return fei.getFile();
	}

	public ICompilationUnit getCurrentCompilationUnit() throws IOException {
		IEditorInput ei=getEditorInput();
		IWorkingCopyManager wcm=JavaUI.getWorkingCopyManager();
		return wcm.getWorkingCopy(ei);
	}

	// static helpers
	// for string only uses forward slashed (works on windows & linux)
		
	public static String getPath(String file) {
/*		file.replace('\\','/');
		
		// quoting for window (for now assuming Linux dont use name with spaces...)
		int p=file.indexOf(" ");
		if (p!=-1) return "\""+file+"\"";
		return file;
		*/
		return file;		
	}
	
	public static String getPath(File file) {
//		String s=file.getAbsolutePath();
//		return getPath(s);
		return file.getAbsolutePath();
	}
	
	public static String getPath(IFile file) {
		IPath p = file.getLocation();
//		String s=p.toString();
//		return getPath(s);
		return getPath(p);
	}

	public static String getPath(IPath path) {
//		String s=path.toString();
//		return getPath(s);
		return path.toOSString();
	}
		
	public boolean isValid() {
		return type!=0;
	}
	
	public static boolean isWindows() {
		return type==WIN;
	}
	
	public static boolean isLinux() {
		return type==LINUX;
	}

	public static boolean isMacOSX() {
		return type==MACOSX;
	}

	public static String[] getPorts() {
		if (isWindows()) return WIN_PORTS;
		if (isLinux()) return LINUX_PORTS;
		if (isMacOSX()) return MACOSX_PORTS;
		return new String[0];
	}	

	public static String getPort(int port) {
		try {
			if (isWindows()) return WIN_PORTS[port];
			if (isLinux()) return LINUX_PORTS[port];
			if (isMacOSX()) return MACOSX_PORTS[port];
		} catch(Exception e) {  // ie. out-of-bound
		}
		return getDefault().getUSP();
	}	
		
}
