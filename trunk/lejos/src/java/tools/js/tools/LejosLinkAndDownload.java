/*
 * Created on 16.03.2006
 * $Log$
 */
package js.tools;

import org.apache.commons.cli.CommandLine;

import js.common.CLIToolProgressMonitor;
import js.tinyvm.TinyVM;
import js.tinyvm.util.TinyVMCommandLineParser;

/**
 * links and downloads leJOS programs in one run
 * @author Matthias Paul Scholz
 *
 */

public class LejosLinkAndDownload {
	
	private TinyVMCommandLineParser fParser;

	public LejosLinkAndDownload() {
		super();
		fParser = new TinyVMCommandLineParser();
	}

	/**
	 * Main entry point for command line usage
	 * @param args
	 */
	public static void main(String[] args) {
		// check arguments
		assert args != null: "error: no argument given";
		try {
			LejosLinkAndDownload instance = new LejosLinkAndDownload();
			instance.run(args);
		} catch(js.tinyvm.TinyVMException tvexc) {
	         System.err.println("error while linking: " + tvexc.getMessage());
		} catch(LejosdlException ldlexc) {
	         System.err.println("error while downloading: " + ldlexc.getMessage());
		}

	}
	
	private void run(String[] args) throws js.tinyvm.TinyVMException,LejosdlException {
		// process arguments
		CommandLine commandLine = fParser.parse(args);
		String binName = commandLine.getOptionValue("o");
		boolean isVerbose = commandLine.hasOption("v");
		String port = commandLine.getOptionValue("tty");
		String tinyVMArgs[];
		String lejosdlArgs[];
		if(binName != null) {
			tinyVMArgs = args;
		} else {
			binName = "tmp.bin";
			tinyVMArgs = new String[args.length+2];
			for(int i=0;i<args.length;i++)
				tinyVMArgs[i] = args[i];
			tinyVMArgs[args.length] = "-o";
			tinyVMArgs[args.length+1] = binName;
		} // else	
		if(isVerbose) {
			lejosdlArgs = new String[4];
			lejosdlArgs[2] = "-v";
			lejosdlArgs[3] = binName;
		} else {
			lejosdlArgs = new String[3];
			lejosdlArgs[2] = binName;
		}
		lejosdlArgs[0] = "-tty";
		lejosdlArgs[1] = port;
		// create progress monitor
		CLIToolProgressMonitor monitor = new CLIToolProgressMonitor();
		// link
		if(isVerbose)
			System.out.println("linking..."); 
		TinyVM tinyVM = new TinyVM(monitor);
		tinyVM.start(tinyVMArgs);
		// download         
		if(isVerbose)
			System.out.println("downloading..."); 
		Lejosdl lejosdl = new Lejosdl(monitor);
		System.out.println("calling lejosdl with ");
		for(int i=0;i<lejosdlArgs.length;i++)
			System.out.println(lejosdlArgs[i]);
        lejosdl.start(lejosdlArgs);
	}
}
