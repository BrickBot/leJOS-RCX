package js.tinyvm.gameboy;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

public class GameboyMerger
{
   String tempDir;
   String compiler;

   public GameboyMerger (String tmpDir)
   {
      tempDir = tmpDir == null? "." : tmpDir;
      compiler = "lcc";
   }

   String getByteCodeFile (String className)
   {
      File dirFile = new File(tempDir);
      return new File(dirFile, "gb$" + className + ".c").getPath();
   }

   String getRomFile (String className)
   {
      return className + ".gb";
   }

   public void dumpRom (InputStream stream, String className)
      throws IOException
   {
      String cfile = getByteCodeFile(className);
      dumpByteCodeFile(stream, cfile);
      String outfile = getRomFile(className);
      //invokeLinker (cfile, outfile);
   }

   void dumpByteCodeFile (InputStream stream, String outputFile)
      throws IOException
   {
      PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(
         outputFile), 4096));
      out.println("/* Machine generated temp file */");
      out.println();
      out.println("#include <asm/types.h>");
      out.println();
      // The name "lejos_code" also appears in main.c
      out.println("UINT8 lejos_code[] = {");
      dumpByteCodeList(stream, out);
      out.println("};");
      out.close();
   }

   void dumpByteCodeList (InputStream stream, PrintWriter out)
      throws IOException
   {
      int b;
      for (int i = 0; (b = stream.read()) != -1; i++)
      {
         out.print("0x" + Integer.toHexString(((byte) b) & 0xFF));
         out.print(", ");
         if (i % 10 == 0)
         {
            out.println();
         }
      }
   }
}

