package js.tools;

import java.io.IOException;
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
 * Simple Lejos tool for sending bytes to rcx.
 */
public class Send extends SendTool
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
         Send send = new Send(new CLIToolProgressMonitor());
         send.start(args);
      }
      catch (SendException e)
      {
         System.err.println(e.getMessage());
         System.exit(1);
      }
   }

   /**
    * Constructor.
    */
   public Send (ToolProgressMonitor monitor)
   {
      super(monitor);
   }

   /**
    * Send bytes to rcx.
    * 
    * @param args command line
    * @throws SendException
    */
   public void start (String[] args) throws SendException
   {
      assert args != null: "Precondition: args != null";

      CommandLine commandLine = parse(args);

      // options
      boolean verbose = commandLine.hasOption("v");
      String tty = commandLine.getOptionValue("t");
      boolean fastMode = commandLine.hasOption("f");

      // files
      String[] bytes = commandLine.getArgs();

      ((CLIToolProgressMonitor) getProgressMonitor()).setVerbose(verbose);

      start(bytes, tty, fastMode);
   }

   /**
    * Send bytes to rcx.
    * 
    * @param bytes String representation of hex bytes
    * @param tty port
    * @param fastMode fast mode
    * @throws SendException
    */
   public void start (String[] bytes, String tty, boolean fastMode) throws SendException
   {
      byte[] send = new byte[bytes.length];
      for (int i = 0; i < bytes.length; i++)
      {
         try
         {
            send[i] = (byte) Integer.parseInt(bytes[i], 16);
         }
         catch (NumberFormatException e)
         {
            throw new SendException(e.getMessage(), e);
         }
      }
      start(send, tty, fastMode);
   }

   /**
    * Parse commandline.
    * 
    * @param args command line
    * @throws SendException
    */
   protected CommandLine parse (String[] args) throws SendException
   {
      assert args != null: "Precondition: args != null";

      Options options = new Options();
      options.addOption("v", "verbose", false, "be verbose");
      options.addOption("h", "help", false, "help");
      Option ttyOption = new Option("t", "tty", true, "tower port");
      ttyOption.setArgName("port");
      options.addOption(ttyOption);
      options.addOption("f", "fast", false, "fast mode");

      CommandLine result;
      try
      {
         try
         {
            result = new GnuParser().parse(options, args);
         }
         catch (ParseException e)
         {
            throw new SendException(e.getMessage());
         }

         if (result.hasOption("h"))
         {
            throw new SendException("Help:");
         }

         if (!result.hasOption("t"))
         {
            throw new SendException("No tower port specified");
         }

         if (result.getArgs().length == 0)
         {
            throw new SendException("More bytes to send specified");
         }
      }
      catch (SendException e)
      {
         StringWriter writer = new StringWriter();
         PrintWriter printWriter = new PrintWriter(writer);
         printWriter.println(e.getMessage());

         String usage = getClass().getName() + " [options] hexBytes";
         // TODO check format parameters
         new HelpFormatter().printHelp(printWriter, 80, usage, null, options,
            0, 2, null);

         throw new SendException(writer.toString());
      }

      assert result != null: "Postconditon: result != null";
      return result;
   }
}