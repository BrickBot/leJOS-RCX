package js.tinyvm;

import java.io.*;
import java.util.*;

public class GbMerger
{
  String tempDir;
  String compiler;

  public GbMerger (String tmpDir)
  {
    tempDir = tmpDir == null ? "." : tmpDir;
    compiler = "lcc";
  }
 
  String getByteCodeFile (String className)
  {
     File dirFile = new File (tempDir);
     return new File (dirFile, "gb$" + className + ".c").getPath();
  }

  String getRomFile (String className)
  {
     return className + ".gb";
  }

  public void dumpRom (byte[] bytes, String className)  
  throws IOException
  {
    String cfile = getByteCodeFile (className);
    dumpByteCodeFile (bytes, cfile);
    String outfile = getRomFile (className);
    //invokeLinker (cfile, outfile);
  }

  void dumpByteCodeFile (byte[] bytes, String outputFile)
  throws IOException
  {
    PrintWriter out = new PrintWriter (new BufferedWriter (new FileWriter (outputFile), 4096));
    out.println ("/* Machine generated temp file */");
    out.println();
    out.println ("#include <sys/types.h>");
    out.println();
    // The name "lejos_code" also appears in main.c
    out.println ("UINT8 lejos_code[] = {");
    dumpByteCodeList (bytes, out);
    out.println ("};");
    out.close();
  }
  
  void dumpByteCodeList (byte[] bytes, PrintWriter out)
  {
    int i = 0;
    for (;;)
    {
       out.print ("0x" + Integer.toHexString (bytes[i]));
       i++;
       if (i >= bytes.length)
           break;
       out.print (", ");
       if (i % 10 == 0)
           out.println();
    }
  }
}





