package js.tinyvm.old;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import js.common.CLIToolProgressMonitor;
import js.common.ToolProgressMonitor;
import js.tools.LejosdlException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Tiny VM.
 */
public class TinyVM extends TinyVMTool
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
         TinyVM tinyVM = new TinyVM(new CLIToolProgressMonitor());
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
   public TinyVM (ToolProgressMonitor monitor)
   {
      super(monitor);
   }

   /**
    * Execute tiny vm.
    * 
    * @param args command line
    * @throws TinyVMException
    */
   public void start (String[] args) throws TinyVMException
   {
      assert args != null: "Precondition: args != null";

      CommandLine commandLine = parse(args);

      // options
      boolean verbose = commandLine.hasOption("v");
      String classpath = commandLine.getOptionValue("cp");
      String output = commandLine.getOptionValue("o");
      boolean all = commandLine.hasOption("a");
      boolean bigEndian = "be".equalsIgnoreCase(commandLine
         .getOptionValue("wo"));

      // files
      String[] classes = commandLine.getArgs();

      ((CLIToolProgressMonitor) getProgressMonitor()).setVerbose(verbose);

      OutputStream stream = null;
      try
      {
         stream = output == null
            ? (OutputStream) System.out
            : (OutputStream) new FileOutputStream(output);
         link(classpath, classes, all, stream, bigEndian);
      }
      catch (FileNotFoundException e)
      {
         throw new TinyVMException(e.getMessage(), e);
      }
      finally
      {
         if (stream instanceof OutputStream)
         {
            try
            {
               stream.close();
            }
            catch (IOException e)
            {
               throw new TinyVMException(e);
            }
         }
      }
   }

   /**
    * Parse commandline.
    * 
    * @param args command line
    * @throws LejosdlException
    */
   protected CommandLine parse (String[] args) throws TinyVMException
   {
      assert args != null: "Precondition: args != null";

      Options options = new Options();
      options.addOption("v", "verbose", false,
         "print class and signature information");
      options.addOption("h", "help", false, "help");
      Option classpathOption = new Option("cp", "classpath", true, "classpath");
      classpathOption.setArgName("classpath");
      options.addOption(classpathOption);
      Option outputOption = new Option("o", "output", true,
         "dump binary to file");
      outputOption.setArgName("binary");
      options.addOption(outputOption);
      options.addOption("a", "all", false, "do not filter classes");
      Option writerOrderOption = new Option("wo", "writeorder", true,
         "write order (BE or LE)");
      writerOrderOption.setArgName("write order");
      options.addOption(writerOrderOption);

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

         if (result.hasOption("h"))
         {
            throw new TinyVMException("Help:");
         }

         if (!result.hasOption("cp"))
         {
            throw new TinyVMException("No classpath defined");
         }

         if (!result.hasOption("wo"))
         {
            throw new TinyVMException("No write order specified");
         }
         String writeOrder = result.getOptionValue("wo").toLowerCase();
         if (!"be".equals(writeOrder) && !"le".equals(writeOrder))
         {
            throw new TinyVMException("Wrong write order: " + writeOrder);
         }

         if (result.getArgs().length == 0)
         {
            throw new TinyVMException("No classes specified");
         }
      }
      catch (TinyVMException e)
      {
         StringWriter writer = new StringWriter();
         PrintWriter printWriter = new PrintWriter(writer);
         printWriter.println(e.getMessage());

         String usage = getClass().getName() + " [options] class1[,class2,...]";
         // TODO check format parameters
         new HelpFormatter().printHelp(printWriter, 80, usage.toString(), null,
            options, 0, 2, null);

         throw new TinyVMException(writer.toString());
      }

      assert result != null: "Postconditon: result != null";
      return result;
   }
}