package js.tinyvm;

import java.io.*;
import java.util.*;
import js.classfile.*;

public class TinyVM
implements Constants
{
  static final String CP_PROPERTY = "tinyvm.class.path";
  static final String WO_PROPERTY = "tinyvm.write.order";
  static final String TINYVM_HOME = System.getProperty ("tinyvm.home");
  static final String TINYVM_LOADER = System.getProperty ("tinyvm.loader");
  static final String TEMP_DIR = System.getProperty ("temp.dir");
  static final String TEMP_FILE = "__tinyvm__temp.tvm__";
  static String iClassPath = System.getProperty (CP_PROPERTY);
  static String iWriteOrder = System.getProperty (WO_PROPERTY);
  static String iOutputFile;
  static boolean iDoDownload = false;
  static boolean iDumpFile = false;
  static boolean iDumpGameboyRom = false;
  static boolean iAll = false;

  private static class Option
  {
    String iOption;
    String iArgument;

    public String toString()
    {
      return iOption + " " + iArgument;
    }
  }

  public static void invokeTvm (String aFileName)
  {
    Assertion.test (TINYVM_HOME != null);
    Assertion.test (TINYVM_LOADER != null);
    String pTvmExec = TINYVM_HOME + File.separator + "bin" +
                      File.separator + TINYVM_LOADER; 
    String[] pParams = new String[] { pTvmExec, aFileName };
    try {
      Assertion.verbose (1, "Executing " + pTvmExec + " (downloading) ...");
      Process p = Runtime.getRuntime().exec (pParams);
      pipeStream (p.getInputStream(), System.out);
      pipeStream (p.getErrorStream(), System.err);
      int pStatus;
      if ((pStatus = p.waitFor()) != 0)
      {
        System.err.println (TINYVM_LOADER + ": returned status " + pStatus + ".");
      }
      // Hack: Small wait to get all the output flushed.
      Thread.sleep (100);
      System.out.flush();
      System.err.flush();      
    } catch (InterruptedException e) {
      Assertion.fatal ("Execution of " + pTvmExec + " was interrupted.");
    } catch (IOException e) {
      Assertion.fatal ("Problem executing " + pTvmExec + ". " +
                       "Apparently, the program was not found. ");
    }
  }

  static void pipeStream (final InputStream aIn, final OutputStream aOut)
  {
    Thread pThread = new Thread ("output-pipe")
    {
      public void run()
      {
        try {
          int c;
          for (;;)
	  {
            c = aIn.read();
            if (c == -1)
              Thread.sleep (1);
            else
              aOut.write (c);
	  }
	} catch (Exception e) {
          e.printStackTrace();
	}
      }
    };

    pThread.setDaemon (true);
    pThread.start();
  }

  static void main (String aClassList)
  throws Exception
  {
    Vector pVec = new Vector();
    StringTokenizer pTok = new StringTokenizer (aClassList, ",");
    while (pTok.hasMoreTokens())
    {
      String pClassName = pTok.nextToken();
      pVec.addElement (pClassName.replace ('.', '/').trim());
    }
    main (pVec);
  }
  
  static void main (Vector aEntryClasses)
  throws Exception
  {
    if (aEntryClasses.size() >= 256)
      Assertion.fatal ("Too many entry classes (max is 255!)");
    ClassPath pCP = new ClassPath (iClassPath);
    Binary pBin = Binary.createFromClosureOf (aEntryClasses, pCP, iAll);
    int pNum = aEntryClasses.size();
    for (int i = 0; i < pNum; i++)
    {
      String pName = (String) aEntryClasses.elementAt (i);
      if (!pBin.hasMain (pName))
        Assertion.fatal ("Class " + pName + " doesn't have a " +
                         "static void main(String[]) method");
    }
    ByteArrayOutputStream pGbStream = null;
    OutputStream pBaseStream;
    if (iDumpGameboyRom)
    {
        pGbStream = new ByteArrayOutputStream (1024);
        pBaseStream = pGbStream;
    }
    else
    {
        if (iOutputFile == null)
            Assertion.fatal ("No ouput file specified. Use -d, -o or -gb");
        pBaseStream = new FileOutputStream (iOutputFile);
    }
    OutputStream pOut =
      new BufferedOutputStream (pBaseStream, 4096);
    ByteWriter pBW = null;
    if ("BE".equals (iWriteOrder))
      pBW = new BEDataOutputStream (pOut);
    else if ("LE".equals (iWriteOrder))
      pBW = new LEDataOutputStream (pOut);
    else
      Assertion.fatal (WO_PROPERTY + " not BE or LE.");
    pBin.dump (pBW);
    pOut.close();
    if (iDoDownload)
    {
      invokeTvm (TEMP_FILE);
      new File (TEMP_FILE).delete();
    }
    if (iDumpGameboyRom)
    {
      GbMerger merger = new GbMerger (TEMP_DIR);
      merger.dumpRom (pGbStream.toByteArray(), (String) aEntryClasses.elementAt (0));
    }
  }

  public static void processOptions (Vector aOptions)
  {
    int pSize = aOptions.size();
    for (int i = 0; i < pSize; i++)
    {
      Option pOpt = (Option) aOptions.elementAt(i);
      Assertion.trace ("Option " + i + ": " + pOpt);
      if (pOpt.iOption.equals ("-classpath"))
      {
        iClassPath =  pOpt.iArgument;
      }
      if (pOpt.iOption.equals ("-o"))
      {
        if (iDoDownload)
          Assertion.fatal ("You cannot specify both -d and -o options.");
        iDumpFile = true;
        iOutputFile = pOpt.iArgument;
      }
      else if (pOpt.iOption.equals ("-gb"))
      {
        iDumpGameboyRom = true;
      }
      else if (pOpt.iOption.equals ("-all")) {
        iAll = true;
      }
      else if (pOpt.iOption.equals ("-verbose"))
      {
        int pLevel = 1;
        try {
          pLevel = Integer.parseInt (pOpt.iArgument);
	} catch (Exception e) {
          if (Assertion.iTrace)
            e.printStackTrace();
	}
        Assertion.setVerboseLevel (pLevel);
      }     
    }
    if (!iDumpFile && !iDumpGameboyRom)
    {
      iDoDownload = true;
      iOutputFile = TEMP_FILE;
    }
  }

  public static void main (Vector aArgs, Vector aOptions)
  throws Exception
  {
    if (aArgs.size() != 1)
    {
      System.out.println (TOOL_NAME + " links and downloads a program.");
      System.out.println ("Use: " + TOOL_NAME + " [options] class1[,class2,...] [arg1 arg2 ...]");
      System.out.println ("Options:");
      System.out.println ("  -o <path>         Dump binary into path (no download)");
      System.out.println ("  -verbose[=<n>]    Print class and signature information");
      System.exit (1);
    }
    processOptions (aOptions);
    if (iClassPath == null)
    {
      Assertion.fatal ("Internal error: Classpath not defined. " +
        "Use either -classpath or property " + CP_PROPERTY);
    }
    main ((String) aArgs.elementAt (0)); 
  }

  public static void main (String[] arg)
  throws Exception
  {
    Vector pRealArgs = new Vector();
    Vector pOptions = new Vector();
    for (int i = 0; i < arg.length; i++)
    {
      if (arg[i].startsWith ("-"))
      {
        Option pOption = new Option();
        pOption.iOption = arg[i];
        if (arg[i].startsWith ("-verbose="))
	{
		  pOption.iOption = "-verbose";
          pOption.iArgument = arg[i].substring ("-verbose=".length());
	}
        else if (arg[i].equals ("-classpath"))
	{
          pOption.iArgument = arg[++i];
          Assertion.trace ("Got -classpath option: " + pOption.iArgument);
	}
        else if (arg[i].equals ("-o"))
	{
          pOption.iArgument = arg[++i];
          Assertion.trace ("Got -o option: " + pOption.iArgument);
	}
        pOptions.addElement (pOption);
      }
      else
        pRealArgs.addElement (arg[i]);
    }
    main (pRealArgs, pOptions);
  }
}
