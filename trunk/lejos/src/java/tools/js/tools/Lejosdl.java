package js.tools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import js.common.CLIToolProgressMonitor;
import js.common.ToolProgressMonitor;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Simple Lejos program downloader.
 */
public class Lejosdl extends LejosdlTool
{
   /**
    * Main entry point for command line usage.
    * 
    * @param args command line
    */
   public static void main (String[] args) throws IOException
   {
      assert args != null: "Precondition: args != null";

      try
      {
         Lejosdl lejosdl = new Lejosdl(new CLIToolProgressMonitor());
         lejosdl.start(args);
      }
      catch (LejosdlException e)
      {
         System.err.println(e.getMessage());
         System.exit(1);
      }
   }

   /**
    * Constructor.
    */
   public Lejosdl (ToolProgressMonitor monitor)
   {
      super(monitor);
   }

   /**
    * Execute program download.
    * 
    * @param args command line
    * @throws LejosdlException
    */
   public void start (String[] args) throws LejosdlException
   {
      assert args != null: "Precondition: args != null";

      CommandLine commandLine = parse(args);

      // options
      boolean verbose = commandLine.hasOption("v");
      String tty = commandLine.getOptionValue("t");
      boolean download = !commandLine.hasOption("n");
      boolean fastMode = commandLine.hasOption("f");

      // files
      String[] binaries = commandLine.getArgs();

      ((CLIToolProgressMonitor) getProgressMonitor()).setVerbose(verbose);

      try
      {
         InputStream stream = binaries.length == 0
            ? System.in
            : new FileInputStream(binaries[0]);
         start(stream, tty, download, fastMode);
      }
      catch (FileNotFoundException e)
      {
         throw new LejosdlException(e.getMessage(), e);
      }
   }

   /**
    * Parse commandline.
    * 
    * @param args command line
    * @throws LejosdlException
    */
   protected CommandLine parse (String[] args) throws LejosdlException
   {
      assert args != null: "Precondition: args != null";

      Options options = new Options();
      options.addOption("v", "verbose", false, "be verbose");
      options.addOption("h", "help", false, "help");
      Option ttyOption = new Option("t", "tty", true, "tower port");
      ttyOption.setArgName("port");
      options.addOption(ttyOption);
      options.addOption("f", "fast", false, "fast mode");
      options.addOption("n", "nodownload", false, "do not download");

      CommandLine result;
      try
      {
         try
         {
            result = new GnuParser().parse(options, args);
         }
         catch (ParseException e)
         {
            throw new LejosdlException(e.getMessage());
         }

         if (result.hasOption("h"))
         {
            throw new LejosdlException("Help:");
         }

         if (!result.hasOption("t"))
         {
            throw new LejosdlException("No tower port specified");
         }

         if (result.getArgs().length > 1)
         {
            throw new LejosdlException("More than one binary specified");
         }
      }
      catch (LejosdlException e)
      {
         StringWriter writer = new StringWriter();
         PrintWriter printWriter = new PrintWriter(writer);
         printWriter.println(e.getMessage());

         String usage = getClass().getName() + " [options] binary";
         // TODO check format parameters
         new HelpFormatter().printHelp(printWriter, 80, usage, null, options,
            0, 2, null);

         throw new LejosdlException(writer.toString());
      }

      assert result != null: "Postconditon: result != null";
      return result;
   }
}