package js.tools;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

/**
 * Java RCX firmware downloader - replaces lejosfirmdl
 * @author Lawrie Griffiths
 */
public class Firmdl implements FastImage {

  private static final int SEGMENT_BREAK = 1024;
  private static final int IMAGE_START = 0x8000;
  private static final int IMAGE_MAXLEN = 0x8000;
  private static final int MAX_SEGMENTS = 2;

  private static FileReader fr = null;
  private static BufferedReader br = null;
  private static boolean fastMode = false;

  private static char [] readRecord() throws IOException {
    String s = br.readLine();
    // System.err.println("Line = " + s);
    if (s == null) return null;
    else return s.toCharArray();
  }

  /*
   * Returns number of images found.
   */
  private static int srecLoad (Image image, int numimage_def, int maxlen) throws IOException {
    char [] buf;
    int line = 0;
    SRec srec = null;
    int segStartAddr = 0;
    int prevAddr = -SEGMENT_BREAK;
    int prevCount = SEGMENT_BREAK;
    int segIndex = -1;
    boolean strip = false;
    int imageIndex = -SEGMENT_BREAK;
    int length = 0, i;

    /* Read image file */

    while ((buf = readRecord()) != null) {
      int error;
      line++;

      /* Skip blank lines */
      for (i = 0; i < buf.length; i++) if (buf[i] != ' ') break;
      if (i == buf.length) continue;

      /* Decode line */

      try {
        srec = new SRec(buf, buf.length);
      } catch (IOException ioe) {
        System.err.println("Error on line " + line + " : " + ioe.getMessage());
        System.exit(1);
      }
        
      /* Detect Firm0309.lgo header, set strip if found */
      if (srec.type == 0) {
        if (srec.count == 16)
          if ((new String(srec.data)).equals("?LIB_VERSION_L00")) {
            System.err.println("Setting strip");
            strip = true;
          }
      }

      /* Process s-record data */

      else if (srec.type == 1) {
          
        /* Start of a new segment? */
	  
        if (srec.addr - prevAddr >= SEGMENT_BREAK) {
          segIndex++;
          image.segments[segIndex] = new Segment();
          if (segIndex >= numimage_def)
          {
            System.err.println("Expected number of image_def exceeded"); 
            System.exit(1);
          }
          image.segments[segIndex].length = 0;
          segStartAddr = srec.addr;
          prevAddr = srec.addr - prevCount;
          // System.err.println("Setting offset to " + (imageIndex + prevCount));
          image.segments[segIndex].offset = imageIndex + prevCount;
        }
		
        if (srec.addr < IMAGE_START || srec.addr + srec.count > IMAGE_START + maxlen) {
          System.err.println("Address (" +  srec.addr + ")out of bounds (srec) on line " + line);
          System.err.println("Count = " + srec.count);
          System.err.println("maxlen = " + maxlen);
          System.exit(1);
        }

        // Data is not necessarily contiguous so can't just accumulate srec.counts.
	  
        image.segments[segIndex].length = srec.addr - segStartAddr + srec.count;
	    
        imageIndex += srec.addr - prevAddr;	
        // System.err.println("Image index = " + imageIndex);
        // System.err.println("Count = " + srec.count);
        for(i=0;i<srec.count;i++) image.data[imageIndex+i] = (byte) srec.data[i];
        String s = "Line = ";
        for(i=0;i<srec.count;i++) s += " " + (byte) srec.data[i] ;
        // System.err.println(s);
        prevAddr = srec.addr;
        prevCount = srec.count;
      }

      /* Process image entry point */

      else if (srec.type == 9) {
        if (srec.addr < IMAGE_START || srec.addr > IMAGE_START + maxlen) {
          System.err.println("Address out of bounds (image) on line" + line);
          System.exit(1);
        }
        // System.err.println("Setting image entry point to " + srec.addr);
        image.entry = srec.addr;
      }
    }

    if (strip) {
      int pos;
      System.err.println("Stripping");
      for (pos = IMAGE_MAXLEN - 1; pos >= 0 && image.data[pos] == 0; pos--)
        image.segments[segIndex].length--;
    }

    for (i = 0; i <= segIndex; i++)
      length += image.segments[segIndex].length;
		
    if (length == 0) {
      System.err.println("Image contains no data");
      System.exit(1);
    }

    return segIndex+1;
  }

  public static String which(String className) {
    if (!className.startsWith("/")) {
       className = "/" + className;
    }
    className = className.replace('.', '/');
    className = className + ".class";

    java.net.URL classUrl = new Object().getClass().getResource(className);
    if (classUrl != null) {
      String s = classUrl.getFile();
      int i = s.indexOf("lib/jtools.jar");
      return s.substring("file:".length(), i);
    } else {
      System.err.println("Cannot locate lejos.srec");
      System.exit(1);
      return null;
    }
  }

  public static void main(String [] args) throws IOException {
    String fileName;
    String tty = "";
    Image image = new Image();
    boolean usage = false, download = true;;
    
    // Find the lejos bin directory
   
    String dir = which("js.tools.Firmdl");
    fileName = dir + "bin/lejos.srec";
    // System.err.println(fileName);

    // Process args

    for(int i=0; i < args.length;i++) {
      if (args[i].equals("--tty") && i < args.length - 1) {
        tty = args[++i];
        System.err.println("Setting tty = " + tty);
      } else if(args[i].length() > 6 && args[i].substring(0,6).equals("--tty=")) {
        tty = args[i].substring(6);
        System.err.println("Setting tty = " + tty);
      } else if (args[i].equals("--help") || args[i].equals("-h")) {
        usage = true;
      } else if (args[i].equals("--nodl") || args[i].equals("-n")) {
        download = false;
      } else if (args[i].equals("--debug")) {
        System.err.println("For debug output set RCXCOMM_DEBUG=Y");
        System.exit(1);
      } else if (args[i].equals("--fast") || args[i].equals("-f")) {
        fastMode = true;
      } else usage = true;
    }

    if (usage) {
      System.err.println("usage: firmdl [options]");
      System.err.println("--tty=<tty>   assume tower connected to <tty>");
      System.err.println("--tty=usb     assume tower connected to usb");
      System.err.println("-n, --nodl    do not download image");
      System.err.println("-h, --help    display this message and exit");
      System.err.println("For debug output set RCXCOMM_DEBUG=Y");
      System.exit(1);
    }

    if (download) Download.open(tty, false);

    // Open the file

    try {
      fr = new FileReader(fileName);
      br = new BufferedReader(fr);
    } catch (FileNotFoundException fe) {
      System.err.println("File " + fileName + " not found");
      System.exit(1);
    }

    // Load the s-record file

    int numImageDef = srecLoad(image, MAX_SEGMENTS, IMAGE_MAXLEN);
    int length = 0;

    for (int i=0; i < numImageDef; i++) {
      System.err.println("Segment " + i + " length = " + image.segments[i].length);
      length += image.segments[i].length; 
    }

    br.close();
    fr.close();

    if (download) {
      if (fastMode) {
        System.out.println("Installing fastmode image");
        Download.installFirmware(fastdlImage, fastdlImage.length, IMAGE_START, false);
        Download.close();
        Download.open(tty, fastMode);
      }
      Download.installFirmware(image.data, length, image.entry, true);
      Download.close();
    }
  }

  private static class Segment {
    public  int length;
    public int offset;
  }

  private static class Image {
    public int entry;
    public Segment [] segments = new Segment[MAX_SEGMENTS];
    public byte [] data = new byte[IMAGE_MAXLEN];
  }
}

