package js.tools;

import java.io.FileNotFoundException;
import java.io.FileReader;
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
 * Java RCX firmware downloader - replaces lejosfirmdl
 * 
 * @author Lawrie Griffiths, Markus Heiden
 */
public class Firmdl extends FirmdlTool
{
   /**
    * Main entry point for command line usage.
    * 
    * @param args command line
    */
   public static void main (String[] args)
   {
      try
      {
         Firmdl firmdl = new Firmdl(new CLIToolProgressMonitor());
         firmdl.start(args);
      }
      catch (FirmdlException e)
      {
         System.err.println(e.getMessage());
         System.exit(1);
      }
   }

   /**
    * Constructor.
    */
   public Firmdl (ToolProgressMonitor monitor)
   {
      super(monitor);
   }

   /**
    * Execute firmware download.
    * 
    * @param args command line
    * @throws FirmdlException
    */
   public void start (String[] args) throws FirmdlException
   {
      assert args != null: "Precondition: args != null";

      CommandLine commandLine = parse(args);

      // options
      boolean verbose = commandLine.hasOption("v");
      String tty = commandLine.getOptionValue("t");
      boolean download = !commandLine.hasOption("n");
      boolean fastMode = commandLine.hasOption("f");

      // files
      String[] srecs = commandLine.getArgs();

      if (srecs.length == 0)
      {
         start(tty, download, fastMode);
      }
      else
      {
         try
         {
            start(new FileReader(srecs[0]), tty, download, fastMode);
         }
         catch (FileNotFoundException e)
         {
            throw new FirmdlException(e.getMessage(), e);
         }
      }
   }

   /**
    * Parse commandline.
    * 
    * @param args command line
    * @throws FirmdlException
    */
   protected CommandLine parse (String[] args) throws FirmdlException
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
            throw new FirmdlException(e.getMessage());
         }

         if (result.hasOption("h"))
         {
            throw new FirmdlException("Help:");
         }

         if (!result.hasOption("t"))
         {
            throw new FirmdlException("No tower port specified");
         }
      }
      catch (FirmdlException e)
      {
         StringWriter writer = new StringWriter();
         PrintWriter printWriter = new PrintWriter(writer);
         printWriter.println(e.getMessage());

         String usage = getClass().getName() + " [options] [firmware.srec]";
         // TODO check format parameters
         new HelpFormatter().printHelp(printWriter, 80, usage, null, options,
            0, 2, null);

         throw new FirmdlException(writer.toString());
      }

      assert result != null: "Postconditon: result != null";
      return result;
   }
}