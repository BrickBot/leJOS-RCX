package js.tinyvm.gameboy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import js.common.CLIToolProgressMonitor;
import js.common.ToolProgressMonitor;
import js.tinyvm.TinyVMException;
import js.tools.FirmdlException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Gameboy downloader.
 */
public class Gameboydl
{
   private ToolProgressMonitor _progress = null;

   private boolean _verbose = false;

   /**
    * Main entry point for command line usage.
    * 
    * @param args command line
    */
   public static void main (String[] args)
   {
      try
      {
         Gameboydl tinyVM = new Gameboydl(new CLIToolProgressMonitor());
         tinyVM.start(args);
      }
      catch (TinyVMException e)
      {
         System.err.println(e.getMessage());
         System.exit(1);
      }
   }

   /**
    * Constructor.
    */
   public Gameboydl (ToolProgressMonitor monitor)
   {
      assert monitor != null: "Precondition: monitor != null";

      _progress = monitor;
   }

   /**
    * Execute tiny vm.
    * 
    * @param args command line
    * @throws TinyVMException
    */
   public void start (String[] args) throws TinyVMException
   {
      CommandLine commandLine = parse(args);

      // options
      _verbose = commandLine.hasOption("v");
      String input = commandLine.getOptionValue("i");

      // files
      String[] classes = commandLine.getArgs();

      try
      {
         InputStream stream = input == null
            ? (InputStream) System.in
            : (InputStream) new FileInputStream(input);
         download(stream, classes[0]);
      }
      catch (FileNotFoundException e)
      {
         throw new TinyVMException(e.getMessage(), e);
      }
   }

   /**
    * Parse commandline.
    * 
    * @param args command line
    * @throws FirmdlException
    */
   protected CommandLine parse (String[] args) throws TinyVMException
   {
      assert args != null: "Precondition: args != null";

      Options options = new Options();
      options.addOption("v", "verbose", false,
         "print class and signature information");
      Option inputOption = new Option("i", "input", true, "binary to download");
      inputOption.setArgName("binary");
      options.addOption(inputOption);

      CommandLine result;
      try
      {
         try
         {
            result = new GnuParser().parse(options, args);
         }
         catch (ParseException e)
         {
            throw new TinyVMException(e.getMessage(), e);
         }

         if (result.getArgs().length == 0)
         {
            throw new TinyVMException("No class specified");
         }

         if (result.getArgs().length > 1)
         {
            throw new TinyVMException("More than one class specified");
         }
      }
      catch (TinyVMException e)
      {
         StringWriter writer = new StringWriter();
         PrintWriter printWriter = new PrintWriter(writer);
         printWriter.println(e.getMessage());

         String usage = getClass().getName() + " [options] class";
         // TODO check format parameters
         new HelpFormatter().printHelp(printWriter, 80, usage, null, options,
            0, 2, null);

         throw new TinyVMException(writer.toString());
      }

      assert result != null: "Postconditon: result != null";
      return result;
   }

   /**
    * Download to gameboy.
    * 
    * @param aEntryClasses
    * @throws Exception
    */
   public void download (InputStream stream, String mainClass)
      throws TinyVMException
   {
      try
      {
         GameboyMerger merger = new GameboyMerger(System
            .getProperty("temp.dir"));
         merger.dumpRom(stream, mainClass);
      }
      catch (Exception e)
      {
         // TODO make other classes throw TinyVMExceptions too
         throw new TinyVMException(e.getMessage(), e);
      }
   }
}