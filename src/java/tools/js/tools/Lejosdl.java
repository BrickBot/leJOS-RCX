package js.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Simple Lejos program downloader - replaces lejosrun
 */
public class Lejosdl {

  private static final int MAGIC = 0xCAF6;

  public static void main(String [] args) throws IOException {
    String fileName = "";
    FileInputStream fis = null;
    long pLength = 0;
    byte [] buffer;
    boolean usage = false;
    String tty = "";
    int i;
 
    // Process args

   for(i=0; i < args.length && args[i].substring(0,1).equals("-");i++) {
      if (args[i].equals("--tty") && i < args.length - 1) {
        tty = args[++i];
        System.err.println("Setting tty = " + tty);
      } else if(args[i].length() > 6 && args[i].substring(0,6).equals("--tty=")) {
        tty = args[i].substring(6);
        System.err.println("Setting tty = " + tty);
      } else if (args[i].equals("--help") || args[i].equals("-h")) {
        usage = true;
      } else if (args[i].equals("--debug")) {
        System.err.println("For debug output set RCXCOMM_DEBUG=Y");
        System.exit(1);
      } else {
        System.err.println("Unrecognized option " + args[i]);
        System.exit(1);
      }
    }

    if (i <args.length) fileName = args[i];
    else usage = true;

    if (usage) {
      System.err.println("usage: lejosdl [options] filename");
      System.err.println("--tty=<tty>   assume tower connected to <tty>");
      System.err.println("--tty=usb     assume tower connected to usb");
      System.err.println("-h, --help    display this message and exit");
      System.err.println("For debug output set RCXCOMM_DEBUG=Y");
      System.exit(1);
    }
   
    // Open the file

    try {
      File f = new File(fileName);
      pLength = f.length();
      fis = new FileInputStream(f);
    } catch (FileNotFoundException fe) {
      System.err.println("File " + fileName + " not found");
      System.exit(1);
    }

    if (pLength > 0xFFFF) {
      System.err.println("Huge file: " + pLength + " bytes");
      System.exit (1);
    }

    Download.open(tty, false);

    buffer = new byte[(int) pLength];
    fis.read(buffer);

    if (buffer[0] != (byte) ((MAGIC >> 8) & 0xFF) ||
      buffer[1] != (byte) ((MAGIC >> 0) & 0xFF)) {
      System.err.println("Magic number is not right. Linker used was for emulation only?");
      System.exit (1);
    }

    Download.downloadProgram(buffer, (int) pLength);

    fis.close();
    Download.close();
  }
}

