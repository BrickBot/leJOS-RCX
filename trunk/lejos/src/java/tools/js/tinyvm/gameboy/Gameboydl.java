package js.tinyvm.gameboy;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import js.tinyvm.Constants;
import js.tinyvm.TinyVMException;
import js.tools.ToolProgressListener;
import js.tools.ToolProgressListenerImpl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Gameboy downloader.
 */
public class Gameboydl implements Constants
{
  private ToolProgressListener _progress = null;

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
      Gameboydl tinyVM = new Gameboydl(new ToolProgressListenerImpl());
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
  public Gameboydl(ToolProgressListener listener)
  {
    assert listener != null : "Precondition: listener != null";

    _progress = listener;
  }

  /**
   * Execute tiny vm.
   * 
   * @param args command line
   * @throws TinyVMException
   */
  public void start (String[] args) throws TinyVMException
  {
    Options options = new Options();
    options.addOption("v", "verbose", false,
        "print class and signature information");
    options.addOption("i", "input", true, "binary to download");

    CommandLine commandLine;
    try
    {
      commandLine = new GnuParser().parse(options, args);
      checkParameters(commandLine, options);
    }
    catch (ParseException e)
    {
      throw new TinyVMException(e);
    }

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
      throw new TinyVMException(e);
    }
  }

  /**
   * Check parameters.
   * 
   * @param classpath
   * @param classes
   * @param all
   * @return @throws TinyVMException
   * @throws TinyVMException
   */
  protected void checkParameters (CommandLine commandLine, Options options)
      throws TinyVMException
  {
    try
    {
      if (commandLine.getArgs().length == 0)
      {
        throw new TinyVMException("No class specified");
      }

      if (commandLine.getArgs().length > 1)
      {
        throw new TinyVMException("More than one class specified");
      }
    }
    catch (TinyVMException e)
    {
      StringWriter writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      printWriter.println(e.getMessage());

      String usage = getClass().getName() + " [options] class\n";
      // TODO check format parameters
      new HelpFormatter().printHelp(printWriter, 80, usage.toString(), null,
          options, 0, 2, null);

      throw new TinyVMException(writer.toString());
    }
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
      GameboyMerger merger = new GameboyMerger(System.getProperty("temp.dir"));
      merger.dumpRom(stream, mainClass);
    }
    catch (Exception e)
    {
      // TODO make other classes throw TinyVMExceptions too
      throw new TinyVMException(e);
    }
  }
}