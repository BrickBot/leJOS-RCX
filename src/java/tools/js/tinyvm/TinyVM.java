package js.tinyvm;

import java.io.*;
import java.util.*;
import js.classfile.*;

public class TinyVM
implements Constants
{
  static final String CP_PROPERTY = "tinyvm.class.path";
  static final String WO_PROPERTY = "tinyvm.write.order";
  static String iClassPath = System.getProperty (CP_PROPERTY);
  static String iWriteOrder = System.getProperty (WO_PROPERTY);
  static String iOutputFile;

  private static class Option
  {
    String iOption;
    String iArgument;

    public String toString()
    {
      return iOption + " " + iArgument;
    }
  }
  
  public static void main (String aClassName)
  throws Exception
  {
    ClassPath pCP = new ClassPath (iClassPath);
    String pNormName = aClassName.replace ('.', '/');
    Binary pBin = Binary.createFromClosureOf (pNormName, pCP);
    OutputStream pOut =
      new BufferedOutputStream (new FileOutputStream (iOutputFile), 4096);
    ByteWriter pBW = null;
    if ("BE".equals (iWriteOrder))
      pBW = new BEDataOutputStream (pOut);
    else if ("LE".equals (iWriteOrder))
    {
      pBW = new LEDataOutputStream (pOut);
      System.out.println ("Warning: Output for emulation only.");
    }
    else
      Utilities.fatal (WO_PROPERTY + " not BE or LE.");
    pBin.dump (pBW);
    pOut.close();
  }

  public static void processOptions (Vector aOptions)
  {
    int pSize = aOptions.size();
    for (int i = 0; i < pSize; i++)
    {
      Option pOpt = (Option) aOptions.elementAt(i);
      Utilities.trace ("Option " + i + ": " + pOpt);
      if (pOpt.iOption.equals ("-classpath"))
      {
        iClassPath =  pOpt.iArgument;
      }
      if (pOpt.iOption.equals ("-o"))
      {
        iOutputFile = pOpt.iArgument;
      }
      else if (pOpt.iOption.equals ("-verbose"))
      {
        int pLevel = 1;
        try {
          pLevel = Integer.parseInt (pOpt.iArgument);
	} catch (Exception e) {
          if (Utilities.iTrace)
            e.printStackTrace();
	}
        Utilities.setVerboseLevel (pLevel);
      }     
    }
  }

  public static void main (Vector aArgs, Vector aOptions)
  throws Exception
  {
    if (aArgs.size() != 1)
    {
      System.out.println (TOOL_NAME + " " + VERSION + 
                          ". Copyright (c) 1999-2000 Jose Solorzano.");
      System.out.println ("Use: " + TOOL_NAME + " [options] className");
      System.out.println ("Options:");
      System.out.println ("  -o <path>         Dump binary into path");
      System.out.println ("  -verbose[=<n>]    Print additional messages");
      System.exit (1);
    }
    processOptions (aOptions);
    if (iClassPath == null)
    {
      Utilities.fatal ("Error: Classpath not defined. " +
        "Use either -classpath or property " + CP_PROPERTY);
    }
    if (iOutputFile == null)
    {
      Utilities.fatal ("Error: No output file specified.");
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
          Utilities.trace ("Got -classpath option: " + pOption.iArgument);
	}
        else if (arg[i].equals ("-o"))
	{
          pOption.iArgument = arg[++i];
          Utilities.trace ("Got -o option: " + pOption.iArgument);
	}
        pOptions.addElement (pOption);
      }
      else
        pRealArgs.addElement (arg[i]);
    }
    main (pRealArgs, pOptions);
  }
}
