package js.tools;

import java.io.*;
import josx.rcxcomm.*;

/**
 * Simple Lejos program downloader - replaces lejosrun
 */
public class Lejosdl {

  private static final int MAGIC = 0xCAF6;

  public static void main(String [] args) throws IOException {
    String fileName = args[0];
    FileInputStream fis = null;
    long pLength = 0;
    byte [] buffer;
    boolean usage = false;
    String tty = "";
 
   // Process args

    for(int i=0; i < args.length;i++) {
      if (args[i].equals("--tty") && i < args.length - 1) {
        tty = args[++i];
        System.out.println("Setting tty = " + tty);
      } else if (args[i].equals("--help") || args[i].equals("-h")) {
        usage = true;
      } else if (args[i].equals("--debug")) {
        System.out.println("For debug output set RCXCOMM_DEBUG=Y");
        System.exit(1);
      }
    }

    if (usage) {
      System.out.println("usage: firmdl [options]");
      System.out.println("--tty=<tty>   assume tower connected to <tty>");
      System.out.println("--tty=usb     assume tower connected to usb");
      System.out.println("-h, --help    display this message and exit");
      System.out.println("For debug output set RCXCOMM_DEBUG=Y");
      System.exit(1);
    }
   
    Download.open("");

    // Open the file

    try {
      File f = new File(fileName);
      pLength = f.length();
      fis = new FileInputStream(f);
    } catch (FileNotFoundException fe) {
      System.out.println("File " + fileName + " not found");
      System.exit(1);
    }

    if (pLength > 0xFFFF) {
      System.out.println("Huge file: " + pLength + " bytes");
      System.exit (1);
    }

    buffer = new byte[(int) pLength];
    fis.read(buffer);

    if (buffer[0] != (byte) ((MAGIC >> 8) & 0xFF) ||
      buffer[1] != (byte) ((MAGIC >> 0) & 0xFF)) {
      System.out.println("Magic number is not right. Linker used was for emulation only?");
      System.exit (1);
    }

    Download.downloadProgram(buffer, (int) pLength);

    fis.close();
    Download.close();
  }
}

