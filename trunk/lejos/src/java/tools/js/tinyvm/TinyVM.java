package js.tinyvm;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Vector;

import js.tinyvm.io.BEDataOutputStream;
import js.tinyvm.io.ByteWriter;
import js.tinyvm.io.LEDataOutputStream;
import js.tools.ToolProgressListener;
import js.tools.ToolProgressListenerImpl;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Tiny VM
 * 
 * TODO use _progress instead of Assertion TODO refactor commandline parsing
 * TODO use no system properties, use parameters instead
 */
public class TinyVM implements Constants
{
  static final String CP_PROPERTY = "tinyvm.class.path";
  static final String TINYVM_HOME = System.getProperty("tinyvm.home");
  static final String TINYVM_LOADER = System.getProperty("tinyvm.loader");
  static final String TEMP_DIR = System.getProperty("temp.dir");
  static final String TEMP_FILE = "__tinyvm__temp.tvm__";
  static final String TOOL_NAME = System.getProperty("tinyvm.linker");

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
      TinyVM tinyVM = new TinyVM(new ToolProgressListenerImpl());
      tinyVM.start(args);
    }
    catch (TinyVMException e)
    {
      e.printStackTrace();
      System.err.println(e.getMessage());
      System.exit(1);
    }
  }

  /**
   * Constructor.
   */
  public TinyVM(ToolProgressListener listener)
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
    options.addOption("cp", "classpath", true, "classpath");
    options.addOption("d", "download", false, "download binary");
    options.addOption("o", "output", true, "dump binary into path");
    options.addOption("a", "all", false, "???");
    options.addOption("wo", "writeorder", true, "write order (BE or LE)");
    options.addOption("gb", "gameboy", false, "dump gameboy rom");

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
    String systemClasspath = System.getProperty(CP_PROPERTY);
    String classpath = commandLine.getOptionValue("cp", systemClasspath);
    boolean download = commandLine.hasOption("d");
    String output = commandLine.getOptionValue("o");
    boolean all = commandLine.hasOption("a");
    boolean bigEndian = "be".equalsIgnoreCase(commandLine.getOptionValue("wo"));
    boolean dumpGameboy = commandLine.hasOption("gb");

    // files
    String[] classes = commandLine.getArgs();

    try
    {
      if (dumpGameboy)
      {
        ByteArrayOutputStream stream = new ByteArrayOutputStream(1024);
        link(classpath, classes, all, stream, bigEndian);
        downloadGameboyRom(stream, classes[0]);
      }
      else if (download)
      {
        File file = new File(new File(TEMP_DIR), TEMP_FILE);
        OutputStream stream = new FileOutputStream(file);
        link(classpath, classes, all, stream, bigEndian);
        invokeTvm(output);
        file.delete();
      }
      else if (output != null)
      {
        OutputStream stream = new FileOutputStream(output);
        link(classpath, classes, all, stream, bigEndian);
      }
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
      if (!commandLine.hasOption("cp")
          && System.getProperty(CP_PROPERTY) == null)
      {
        throw new TinyVMException("No classpath defined");
      }

      int dumpGameboyRom = commandLine.hasOption("gb") ? 1 : 0;
      int download = commandLine.hasOption("d") ? 1 : 0;
      int output = commandLine.hasOption("o") ? 1 : 0;
      if (dumpGameboyRom + download + output > 1)
      {
        throw new TinyVMException("-gb -d and -o are exclusive");
      }
      else if (dumpGameboyRom + download + output == 0)
      {
        throw new TinyVMException("No output location specified");
      }

      String writeOrder = commandLine.getOptionValue("wo").toLowerCase();
      if (!"be".equals(writeOrder) && !"le".equals(writeOrder))
      {
        throw new TinyVMException("Wrong write order: " + writeOrder);
      }

      if (commandLine.getArgs().length == 0)
      {
        throw new TinyVMException("No classes specified");
      }
    }
    catch (TinyVMException e)
    {
      StringWriter writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      printWriter.println(e.getMessage());

      String usage = getClass().getName() + " [options] class1[,class2,...]\n";
      new HelpFormatter().printHelp(printWriter, 80, usage.toString(), null,
          options, 0, 2, null);

      throw new TinyVMException(writer.toString());
    }
  }

  /**
   * Execute tiny vm.
   * 
   * @param classpath classpath
   * @param classes main classes to compile
   * @param all
   * @param stream output stream to write binary to
   * @param bigEndian write big endian output?
   * @throws TinyVMException
   */
  public void link (String classpath, String[] classes, boolean all,
      OutputStream stream, boolean bigEndian) throws TinyVMException
  {
    Binary binary = link(classpath, classes, all);
    dump(binary, stream, bigEndian);
  }

  /**
   * Execute tiny vm.
   * 
   * @param aEntryClasses
   * @throws Exception
   */
  public Binary link (String classpath, String[] classes, boolean all)
      throws TinyVMException
  {
    Binary result;
    try
    {
      if (classes.length >= 256)
      {
        throw new TinyVMException("Too many entry classes (max is 255!)");
      }

      ClassPath computedClasspath = new ClassPath(classpath);
      // TODO refactor
      Vector classVector = new Vector();
      for (int i = 0; i < classes.length; i++)
      {
        classVector.addElement(classes[i].replace('.', '/').trim());
      }
      result = Binary.createFromClosureOf(classVector, computedClasspath, all);
      for (int i = 0; i < classes.length; i++)
      {
        String clazz = classes[i].replace('.', '/').trim();
        if (!result.hasMain(clazz))
        {
          throw new TinyVMException("Class " + clazz
              + " doesn't have a static void main(String[]) method");
        }
      }
    }
    catch (Exception e)
    {
      // TODO make other classes throw TinyVMExceptions too
      throw new TinyVMException(e);
    }

    return result;
  }

  /**
   * Execute tiny vm.
   * 
   * @param aEntryClasses
   * @throws Exception
   */
  public void dump (Binary binary, OutputStream stream, boolean bigEndian)
      throws TinyVMException
  {
    try
    {
      OutputStream bufferedStream = new BufferedOutputStream(stream, 4096);
      ByteWriter byteWriter = bigEndian ? (ByteWriter) new BEDataOutputStream(
          bufferedStream) : (ByteWriter) new LEDataOutputStream(bufferedStream);
      binary.dump(byteWriter);
      bufferedStream.close();
    }
    catch (Exception e)
    {
      // TODO make other classes throw TinyVMExceptions too
      throw new TinyVMException(e);
    }
  }

  /**
   * Execute tiny vm.
   * 
   * @param aEntryClasses
   * @throws Exception
   */
  public void downloadGameboyRom (ByteArrayOutputStream stream, String mainClass)
      throws TinyVMException
  {
    try
    {
      GbMerger merger = new GbMerger(TEMP_DIR);
      merger.dumpRom(stream.toByteArray(), mainClass);
    }
    catch (Exception e)
    {
      // TODO make other classes throw TinyVMExceptions too
      throw new TinyVMException(e);
    }
  }

  /**
   * Execute tiny vm.
   * 
   * @param aFileName ???
   * @throws TinyVMException
   * @throws TinyVMException
   */
  public void invokeTvm (String aFileName) throws TinyVMException
  {
    if (TINYVM_HOME == null)
    {
      throw new TinyVMException("tinyvm.home not set");
    }

    // Allow download tool to be null

    if (TINYVM_LOADER == null)
      return;

    String pTvmExec = TINYVM_HOME + File.separator + "bin" + File.separator
        + TINYVM_LOADER;
    String[] pParams = new String[]
    {pTvmExec, aFileName};
    try
    {
      if (_verbose)
      {
        _progress.log("Executing " + pTvmExec + " (downloading) ...");
      }
      Process p = Runtime.getRuntime().exec(pParams);
      pipeStream(p.getInputStream(), System.out);
      pipeStream(p.getErrorStream(), System.err);
      int pStatus;
      if ((pStatus = p.waitFor()) != 0)
      {
        System.err
            .println(TINYVM_LOADER + ": returned status " + pStatus + ".");
      }
      // Hack: Small wait to get all the output flushed.
      Thread.sleep(100);
      System.out.flush();
      System.err.flush();
    }
    catch (InterruptedException e)
    {
      throw new TinyVMException("Execution of " + pTvmExec
          + " was interrupted.");
    }
    catch (IOException e)
    {
      throw new TinyVMException("Problem executing " + pTvmExec + ". "
          + "Apparently, the program was not found. ");
    }
  }

  static void pipeStream (final InputStream aIn, final OutputStream aOut)
  {
    Thread pThread = new Thread("output-pipe")
    {
      public void run ()
      {
        try
        {
          int c;
          for (;;)
          {
            c = aIn.read();
            if (c == -1)
              Thread.sleep(1);
            else
              aOut.write(c);
          }
        }
        catch (Exception e)
        {
          e.printStackTrace();
        }
      }
    };

    pThread.setDaemon(true);
    pThread.start();
  }
}