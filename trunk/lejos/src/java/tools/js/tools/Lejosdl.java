package js.tools;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import js.common.ToolException;
import js.common.ToolProgressListener;
import js.common.ToolProgressListenerImpl;

/**
 * Simple Lejos program downloader.
 */
public class Lejosdl
{
  private static final int MAGIC = 0xCAF6;

  private ToolProgressListener _progress = null;

  /**
   * Main entry point for command line usage.
   * 
   * @param args command line
   */
  public static void main (String[] args) throws IOException
  {
    assert args != null : "Precondition: args != null";

    try
    {
      Lejosdl lejosdl = new Lejosdl(new ToolProgressListenerImpl());
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
  public Lejosdl(ToolProgressListener listener)
  {
    assert listener != null : "Precondition: listener != null";

    _progress = listener;
  }

  /**
   * Execute program download.
   * 
   * @param args command line
   * @throws ToolException
   */
  public void start (String[] args) throws LejosdlException
  {
    assert args != null : "Precondition: args != null";

    String fileName = "";
    String tty = "";
    boolean download = true;
    boolean fastMode = false;

    // Process args
    try
    {
      int i;
      for (i = 0; i < args.length && args[i].substring(0, 1).equals("-"); i++)
      {
        if (args[i].equals("--tty") && i < args.length - 1)
        {
          tty = args[++i];
          _progress.log("Setting tty = " + tty);
        }
        else if (args[i].length() > 6
            && args[i].substring(0, 6).equals("--tty="))
        {
          tty = args[i].substring(6);
          _progress.log("Setting tty = " + tty);
        }
        else if (args[i].equals("--help") || args[i].equals("-h"))
        {
          throw new LejosdlException("Help:");
        }
        else if (args[i].equals("--nodl") || args[i].equals("-n"))
        {
          download = false;
        }
        else if (args[i].equals("--debug"))
        {
          throw new LejosdlException("For debug output set RCXCOMM_DEBUG=Y");
        }
        else if (args[i].equals("--fast") || args[i].equals("-f"))
        {
          fastMode = true;
        }
        else
        {
          throw new LejosdlException("Unrecognized option " + args[i]);
        }
      }

      if (i < args.length)
      {
        fileName = args[i];
      }
      else
      {
        throw new LejosdlException("No file specified");
      }
    }
    catch (LejosdlException e)
    {
      throw new LejosdlException(e.getMessage()
          + "\n\nusage: lejosdl [options] filename"
          + "\n--tty=<tty>   assume tower connected to <tty>"
          + "\n--tty=usb     assume tower connected to usb"
          + "\n-n, --nodl    do not download image"
          + "\n-h, --help    display this message and exit"
          + "\n-f, --fast    use fast transfer mode"
          + "\nFor debug output set RCXCOMM_DEBUG=Y");
    }

    start(fileName, tty, download, fastMode);
  }

  /**
   * Execute program download.
   * 
   * @param fileName file name of program
   * @param tty serial port
   * @param download download program?
   * @param fastMode use fast mode?
   * @throws ToolException
   */
  protected void start (String fileName, String tty, boolean download,
      boolean fastMode) throws LejosdlException
  {
    assert fileName != null : "Precondition: fileName != null";
    assert tty != null : "Precondition: tty != null";

    // Open the file
    InputStream stream;
    try
    {
      stream = new FileInputStream(fileName);
    }
    catch (FileNotFoundException e)
    {
      throw new LejosdlException("Program " + fileName + " not found");
    }

    start(stream, tty, download, fastMode);

    try
    {
      stream.close();
    }
    catch (IOException e)
    {
      throw new LejosdlException(e);
    }
  }

  /**
   * Execute program download.
   * 
   * @param program reader with program to download
   * @param tty serial port
   * @param fastMode use fast mode?
   * @throws ToolException
   */
  public void start (InputStream program, String tty, boolean fastMode)
      throws LejosdlException
  {
    start(program, tty, true, fastMode);
  }

  /**
   * Execute program download.
   * 
   * @param program reader with program to download
   * @param tty serial port
   * @param download download program?
   * @param fastMode use fast mode?
   * @throws ToolException
   */
  public void start (InputStream program, String tty, boolean download,
      boolean fastMode) throws LejosdlException
  {
    assert program != null : "Precondition: program != null";
    assert tty != null : "Precondition: tty != null";

    byte[] buffer = new byte[0x10000];
    int index = 0;

    try
    {
      int read;
      while ((read = program.read(buffer, index, 0x10000 - index)) != -1
          && index < 0x10000)
      {
        index += read;
      }

      if (program.read() != -1)
      {
        // read remaining bytes
        while ((read = program.read(buffer, 0, 0x1000)) != -1)
        {
          index += read;
        }
        throw new LejosdlException("Huge file: " + index + " bytes");
      }
    }
    catch (IOException e)
    {
      throw new LejosdlException("Unable to read program: " + e.getMessage(), e);
    }
    finally
    {
      try
      {
        program.close();
      }
      catch (IOException e)
      {
        // ignore
      }
    }

    if (buffer[0] != (byte) ((MAGIC >> 8) & 0xFF)
        || buffer[1] != (byte) ((MAGIC >> 0) & 0xFF))
    {
      throw new LejosdlException("Magic number is not right."
          + "\nLinker used was for emulation only?");
    }

    if (download)
    {
      Download d = new Download(_progress);
      try
      {
        d.open(tty, fastMode);
        d.downloadProgram(buffer, index);
        d.close();
      }
      catch (ToolException e)
      {
        throw new LejosdlException(e);
      }
      finally
      {
        if (d.isOpen())
        {
          d.close();
        }
      }
    }
  }
}