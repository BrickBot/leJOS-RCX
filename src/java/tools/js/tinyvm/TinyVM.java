package js.tinyvm;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.Vector;

import js.tinyvm.io.BEDataOutputStream;
import js.tinyvm.io.ByteWriter;
import js.tinyvm.io.LEDataOutputStream;
import js.tinyvm.util.Assertion;
import js.tools.ToolProgressListener;
import js.tools.ToolProgressListenerImpl;

/**
 * Tiny VM
 * 
 * TODO use _progress instead of Assertion
 * TODO refactor commandline parsing
 * TODO use no system properties, use parameters instead
 */
public class TinyVM implements Constants
{
  static final String CP_PROPERTY = "tinyvm.class.path";
  static final String WO_PROPERTY = "tinyvm.write.order";
  static final String TINYVM_HOME = System.getProperty("tinyvm.home");
  static final String TINYVM_LOADER = System.getProperty("tinyvm.loader");
  static final String TEMP_DIR = System.getProperty("temp.dir");
  static final String TEMP_FILE = "__tinyvm__temp.tvm__";
  static final String TOOL_NAME = System.getProperty("tinyvm.linker");
  
  private String iClassPath = System.getProperty(CP_PROPERTY);
  private String iWriteOrder = System.getProperty(WO_PROPERTY);
  private String iOutputFile;
  
  private boolean iDoDownload = false;
  private boolean iDumpFile = false;
  private boolean iDumpGameboyRom = false;
  private boolean iAll = false;

  private ToolProgressListener _progress = null;

  private static class Option
  {
    String iOption;
    String iArgument;

    public String toString ()
    {
      return iOption + " " + iArgument;
    }
  }

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
  public void start (String[] arg) throws TinyVMException
  {
    Vector pRealArgs = new Vector();
    Vector pOptions = new Vector();
    boolean gotBin = false;

    for (int i = 0; i < arg.length; i++)
    {
      if (arg[i].startsWith("-"))
      {
        Option pOption = new Option();
        pOption.iOption = arg[i];
        if (arg[i].startsWith("-verbose="))
        {
          pOption.iOption = "-verbose";
          pOption.iArgument = arg[i].substring("-verbose=".length());
        }
        else if (arg[i].equals("-classpath"))
        {
          pOption.iArgument = arg[++i];
          Assertion.trace("Got -classpath option: " + pOption.iArgument);
        }
        else if (arg[i].equals("-o"))
        {
          pOption.iArgument = arg[++i];
          gotBin = true;
          Assertion.trace("Got -o option: " + pOption.iArgument);
        }
        pOptions.addElement(pOption);
      }
      else
      {
        pRealArgs.addElement(arg[i]);
      }
    }

    start(pRealArgs, pOptions, gotBin);
  }

  /**
   * Execute tiny vm.
   * 
   * @param aArgs
   * @param aOptions
   * @param gotBin
   * @throws Exception
   */
  public void start (Vector aArgs, Vector aOptions, boolean gotBin)
      throws TinyVMException
  {
    // if no download program is supplied, the -o parameter is mandatory
    // Make the usage statement reflect this

    if (aArgs.size() != 1 || (TINYVM_LOADER == null && !gotBin))
    {
      String firstLine = TOOL_NAME + " links";
      if (TINYVM_LOADER != null)
        firstLine += " and downloads";
      firstLine += " a program.";
      System.out.println(firstLine);
      String useLine = "Use: " + TOOL_NAME + " [options] class1[,class2,...]";
      if (TINYVM_LOADER == null)
        useLine += " -o <path>";
      System.out.println(useLine);
      if (TINYVM_LOADER == null)
        System.out.println("Dumps binary into <path>");
      System.out.println("Options:");
      if (TINYVM_LOADER != null)
        System.out
            .println("  -o <path>         Dump binary into path (no download)");
      System.out
          .println("  -verbose[=<n>]    Print class and signature information");
      System.out.println("  -all              Include all methods");
      System.exit(1);
    }
    processOptions(aOptions);
    if (iClassPath == null)
    {
      throw new TinyVMException("Internal error: Classpath not defined. "
          + "Use either -classpath or property " + CP_PROPERTY);
    }
    
    start((String) aArgs.elementAt(0));
  }

  /**
   * Process options.
   * 
   * @param aOptions
   * @throws TinyVMException
   */
  public void processOptions (Vector aOptions) throws TinyVMException
  {
    int pSize = aOptions.size();
    for (int i = 0; i < pSize; i++)
    {
      Option pOpt = (Option) aOptions.elementAt(i);
      Assertion.trace("Option " + i + ": " + pOpt);
      if (pOpt.iOption.equals("-classpath"))
      {
        iClassPath = pOpt.iArgument;
      }
      if (pOpt.iOption.equals("-o"))
      {
        if (iDoDownload)
        {
          throw new TinyVMException("You cannot specify both -d and -o options.");
        }
        iDumpFile = true;
        iOutputFile = pOpt.iArgument;
      }
      else if (pOpt.iOption.equals("-gb"))
      {
        iDumpGameboyRom = true;
      }
      else if (pOpt.iOption.equals("-all"))
      {
        iAll = true;
      }
      else if (pOpt.iOption.equals("-verbose"))
      {
        int pLevel = 1;
        try
        {
          pLevel = Integer.parseInt(pOpt.iArgument);
        }
        catch (Exception e)
        {
          if (Assertion.iTrace)
          {
            e.printStackTrace();
          }
        }
        Assertion.setVerboseLevel(pLevel);
      }
    }
    if (!iDumpFile && !iDumpGameboyRom)
    {
      iDoDownload = true;
      iOutputFile = TEMP_FILE;
    }
  }

  /**
   * Execute tiny vm.
   * 
   * @param aClassList
   * @throws Exception
   */
  public void start (String aClassList) throws TinyVMException
  {
    Vector pVec = new Vector();
    StringTokenizer pTok = new StringTokenizer(aClassList, ",");
    while (pTok.hasMoreTokens())
    {
      String pClassName = pTok.nextToken();
      pVec.addElement(pClassName.replace('.', '/').trim());
    }

    start(pVec);
  }

  /**
   * Execute tiny vm.
   * 
   * @param aEntryClasses
   * @throws Exception
   */
  public void start (Vector aEntryClasses) throws TinyVMException
  {
    try
    {
      if (aEntryClasses.size() >= 256)
      {
        throw new TinyVMException("Too many entry classes (max is 255!)");
      }
      ClassPath pCP = new ClassPath(iClassPath);
      Binary pBin = Binary.createFromClosureOf(aEntryClasses, pCP, iAll);
      int pNum = aEntryClasses.size();
      for (int i = 0; i < pNum; i++)
      {
        String pName = (String) aEntryClasses.elementAt(i);
        if (!pBin.hasMain(pName))
        {
          throw new TinyVMException("Class " + pName + " doesn't have a "
              + "static void main(String[]) method");
        }
      }
      ByteArrayOutputStream pGbStream = null;
      OutputStream pBaseStream;
      if (iDumpGameboyRom)
      {
        pGbStream = new ByteArrayOutputStream(1024);
        pBaseStream = pGbStream;
      }
      else
      {
        if (iOutputFile == null)
        {
          throw new TinyVMException("No ouput file specified. Use -d, -o or -gb");
        }
        pBaseStream = new FileOutputStream(iOutputFile);
      }
      OutputStream pOut = new BufferedOutputStream(pBaseStream, 4096);
      ByteWriter pBW = null;
      if ("BE".equals(iWriteOrder))
        pBW = new BEDataOutputStream(pOut);
      else if ("LE".equals(iWriteOrder))
        pBW = new LEDataOutputStream(pOut);
      else
      {
        throw new TinyVMException(WO_PROPERTY + " not BE or LE.");
      }
      pBin.dump(pBW);
      pOut.close();
      if (iDoDownload)
      {
        invokeTvm(TEMP_FILE);
        new File(TEMP_FILE).delete();
      }
      if (iDumpGameboyRom)
      {
        GbMerger merger = new GbMerger(TEMP_DIR);
        merger.dumpRom(pGbStream.toByteArray(), (String) aEntryClasses
            .elementAt(0));
      }
    }
    catch (FileNotFoundException e)
    {
      throw new TinyVMException(e);
    }
    catch (IOException e)
    {
      throw new TinyVMException(e);
    }
    catch (Exception e)
    {
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
  public static void invokeTvm (String aFileName) throws TinyVMException
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
      Assertion.verbose(1, "Executing " + pTvmExec + " (downloading) ...");
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
      throw new TinyVMException("Execution of " + pTvmExec + " was interrupted.");
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