package js.tools;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;

import js.common.ToolException;
import js.common.ToolProgressListener;
import js.common.ToolProgressListenerImpl;

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
      Firmdl firmdl = new Firmdl(new ToolProgressListenerImpl());
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
  public Firmdl(ToolProgressListener listener)
  {
    super(listener);
  }

  /**
   * Execute firmware download.
   * 
   * @param args command line
   * @throws ToolException
   */
  public void start (String[] args) throws FirmdlException
  {
    assert args != null : "Precondition: args != null";

    CommandLine commandLine = parse(args);

    // options
    boolean verbose = commandLine.hasOption("v");
    String tty = commandLine.getOptionValue("t");
    boolean download = !commandLine.hasOption("n");
    boolean fastMode = commandLine.hasOption("f");

    // files
    String[] srecs = commandLine.getArgs();

    // get firmware
    // Find the lejos bin directory
    // String dir = which("js.tools.Firmdl");
    // fileName = dir + "bin/lejos.srec";
    Reader reader;
    if (srecs.length == 0)
    {
      InputStream stream = Firmdl.class.getResourceAsStream("/lejos.srec");
      if (stream == null)
      {
        throw new FirmdlException("Unable to find default lejos.srec");
      }
      reader = new InputStreamReader(stream);
    }
    else
    {
      try
      {
        reader = new FileReader(srecs[0]);
      }
      catch (FileNotFoundException e)
      {
        throw new FirmdlException(e);
      }
    }

    start(reader, tty, download, fastMode);
  }

  /**
   * Parse commandline.
   * 
   * @param args command line
   * @throws FirmdlException
   */
  protected CommandLine parse (String[] args) throws FirmdlException
  {
    assert args != null : "Precondition: args != null";

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
      new HelpFormatter().printHelp(printWriter, 80, usage, null, options, 0,
          2, null);

      throw new FirmdlException(writer.toString());
    }

    assert result != null : "Postconditon: result != null";
    return result;
  }
}