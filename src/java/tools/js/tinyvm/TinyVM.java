package js.tinyvm;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
 * Tiny VM.
 */
public class TinyVM implements Constants
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
      TinyVM tinyVM = new TinyVM(new ToolProgressListenerImpl());
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
    options.addOption("o", "output", true, "dump binary into path");
    options.addOption("a", "all", false, "do not filter classes");
    options.addOption("wo", "writeorder", true, "write order (BE or LE)");

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
    String classpath = commandLine.getOptionValue("cp");
    String output = commandLine.getOptionValue("o");
    boolean all = commandLine.hasOption("a");
    boolean bigEndian = "be".equalsIgnoreCase(commandLine.getOptionValue("wo"));

    // files
    String[] classes = commandLine.getArgs();

    try
    {
      OutputStream stream = output == null
          ? (OutputStream) System.out
          : (OutputStream) new FileOutputStream(output);
      link(classpath, classes, all, stream, bigEndian);
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
      if (!commandLine.hasOption("cp"))
      {
        throw new TinyVMException("No classpath defined");
      }

      if (!commandLine.hasOption("wo"))
      {
        throw new TinyVMException("No write order specified");
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
      // TODO check format parameters
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
}