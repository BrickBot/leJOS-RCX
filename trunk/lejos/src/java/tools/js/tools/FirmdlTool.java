package js.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import js.common.AbstractTool;
import js.common.NullToolProgressMonitor;
import js.common.ToolException;
import js.common.ToolProgressMonitor;

/**
 * Java RCX firmware downloader - replaces lejosfirmdl
 * 
 * @author Lawrie Griffiths, Markus Heiden
 */
public class FirmdlTool extends AbstractTool
{
   private static final int SEGMENT_BREAK = 1024;
   private static final int IMAGE_START = 0x8000;
   private static final int IMAGE_MAXLEN = 0x8000;
   private static final int MAX_SEGMENTS = 2;

   /**
    * Constructor.
    */
   public FirmdlTool (ToolProgressMonitor monitor)
   {
      super(monitor);
   }

   /**
    * Execute firmware download with default firmware.
    * 
    * @param tty port
    * @param download download read image?
    * @param fastMode use fast mode?
    * @throws FirmdlException
    */
   public void start (String tty, boolean download, boolean fastMode)
      throws FirmdlException
   {
      Image image = getImage("lejos");
      start(image, tty, download, fastMode);
   }

   /**
    * Execute firmware download.
    * 
    * @param reader reader to read firmware from
    * @param tty port
    * @param download download read image?
    * @param fastMode use fast mode?
    * @throws FirmdlException
    */
   public void start (Reader reader, String tty, boolean download,
      boolean fastMode) throws FirmdlException
   {
      Image image = srecLoad(reader, MAX_SEGMENTS, IMAGE_MAXLEN);
      start(image, tty, download, fastMode);
   }

   /**
    * Execute firmware download.
    * 
    * @param image firmware image
    * @param tty port
    * @param download download read image?
    * @param fastMode use fast mode?
    * @throws FirmdlException
    */
   public void start (Image image, String tty, boolean download,
      boolean fastMode) throws FirmdlException
   {
      log(image);

      if (download)
      {
         if (fastMode)
         {
            getProgressMonitor().operation("Installing fastmode firmware");
            Download d = new Download(new NullToolProgressMonitor());
            try
            {
               String fastdl = tty.toLowerCase().indexOf("usb") == -1? "fastdl4x" : "fastdl2x";
               Image fastImage = getImage(fastdl);
               log(fastImage);

               d.open(tty, false);
               d.installFirmware(fastImage.data, fastImage.length(),
                  IMAGE_START);
               d.close();
            }
            catch (Exception e)
            {
               throw new FirmdlException(e.getMessage(), e);
            }
            finally
            {
               if (d.isOpen())
               {
                  d.close();
               }
            }
         }
         getProgressMonitor().operation("Installing firmware");
         Download d = new Download(getProgressMonitor());
         try
         {
            d.open(tty, fastMode);
            d.installFirmware(image.data, image.length(), image.entry);
            d.close();
         }
         catch (ToolException e)
         {
            throw new FirmdlException(e.getMessage(), e);
         }
         finally
         {
            if (d.isOpen())
            {
               d.close();
            }
         }
      }
   }

   /**
    * Get builtin firmware image.
    * 
    * @param name base name of builtin image
    * @throws FirmdlException
    */
   protected Image getImage (String name) throws FirmdlException
   {
      assert name != null: "Precondition: name != null";

      InputStream stream = Firmdl.class.getResourceAsStream("/" + name
         + ".srec");
      if (stream == null)
      {
         throw new FirmdlException("Unable to find default firmware image "
            + name + ".srec");
      }
      Reader reader = new InputStreamReader(stream);

      Image result;
      try
      {
         result = srecLoad(reader, MAX_SEGMENTS, IMAGE_MAXLEN);
      }
      finally
      {
         try
         {
            reader.close();
         }
         catch (IOException e)
         {
            throw new FirmdlException(e.getMessage(), e);
         }
      }

      assert result != null: "Postcondition: result != null";
      return result;
   }

   /**
    * Log some infos about given image.
    * 
    * @param image firmware image
    */
   public void log (Image image)
   {
      assert image != null: "Preconditon: image != null";

      for (int i = 0; i < image.numSegments(); i++)
      {
         getProgressMonitor().log(
            "Segment " + i + ": length = " + image.segments[i].length);
      }
   }

   //
   // private interface
   //

   /**
    * Load image from srec.
    */
   private Image srecLoad (Reader reader, int numimage_def, int maxlen)
      throws FirmdlException
   {
      BufferedReader bufferedReader = new BufferedReader(reader);
      Image image = new Image();

      try
      {
         getProgressMonitor().operation("read firmware srec");

         String record;
         SRec srec = null;
         int segStartAddr = 0;
         int prevAddr = -SEGMENT_BREAK;
         int prevCount = SEGMENT_BREAK;
         boolean strip = false;
         int imageIndex = -SEGMENT_BREAK;
         int length = 0, i;
         int segIndex = -1;

         /* Read image file */
         int line = 0;
         while ((record = bufferedReader.readLine()) != null)
         {
            // Skip blank lines
            record = record.trim();
            if (record.length() == 0)
            {
               continue;
            }
            line++;

            /* Decode line */
            try
            {
               char[] buf = record.toCharArray();
               srec = new SRec(buf, buf.length);
            }
            catch (IOException e)
            {
               throw new FirmdlException("Error on line " + line + " : "
                  + e.getMessage(), e);
            }

            if (srec.type == 0)
            {
               /* Detect Firm0309.lgo header, set strip if found */
               if ((new String(srec.data)).equals("?LIB_VERSION_L00"))
               {
                  getProgressMonitor().log("Setting strip");
                  strip = true;
               }
            }
            else if (srec.type == 1)
            {
               /* Process s-record data */

               /* Start of a new segment? */
               if (srec.addr - prevAddr >= SEGMENT_BREAK)
               {
                  segIndex++;
                  image.segments[segIndex] = new Segment();
                  if (segIndex >= numimage_def)
                  {
                     throw new FirmdlException(
                        "Expected number of image_def exceeded");
                  }
                  image.segments[segIndex].length = 0;
                  segStartAddr = srec.addr;
                  prevAddr = srec.addr - prevCount;
                  image.segments[segIndex].offset = imageIndex + prevCount;
               }

               if (srec.addr < IMAGE_START
                  || srec.addr + srec.count > IMAGE_START + maxlen)
               {
                  throw new FirmdlException("Address (" + srec.addr
                     + ") out of bounds (srec) on line " + line + "\nCount = "
                     + srec.count + "\nmaxlen = " + maxlen);
               }

               // Data is not necessarily contiguous so can't just accumulate
               // srec.counts.
               image.segments[segIndex].length = srec.addr - segStartAddr
                  + srec.count;

               imageIndex += srec.addr - prevAddr;
               for (i = 0; i < srec.count; i++)
               {
                  image.data[imageIndex + i] = (byte) srec.data[i];
               }
               prevAddr = srec.addr;
               prevCount = srec.count;
            }
            else if (srec.type == 9)
            {
               // Process image entry point
               if (srec.addr < IMAGE_START || srec.addr > IMAGE_START + maxlen)
               {
                  throw new FirmdlException(
                     "Address out of bounds (image) on line" + line);
               }

               image.entry = srec.addr;
            }
         }

         if (strip)
         {
            int pos;
            getProgressMonitor().log("Stripping");
            for (pos = IMAGE_MAXLEN - 1; pos >= 0 && image.data[pos] == 0; pos--)
            {
               image.segments[segIndex].length--;
            }
         }

         for (i = 0; i <= segIndex; i++)
         {
            length += image.segments[segIndex].length;
         }

         if (length == 0)
         {
            throw new FirmdlException("Image contains no data");
         }

         getProgressMonitor().progress(1000);
      }
      catch (IOException e)
      {
         throw new FirmdlException("Unable to read srec: " + e.getMessage(), e);
      }
      finally
      {
         try
         {
            bufferedReader.close();
         }
         catch (IOException e)
         {
            // ignore
         }
      }

      assert image != null: "Postcondition: result != null";
      return image;
   }

   private static class Segment
   {
      public int length;
      public int offset;
   }

   private static class Image
   {
      /**
       * Entry point.
       */
      public int entry;

      /**
       * Segments.
       */
      public Segment[] segments = new Segment[MAX_SEGMENTS];

      /**
       * Data.
       */
      public byte[] data = new byte[IMAGE_MAXLEN];

      /**
       * Number of segments.
       */
      public int numSegments ()
      {
         int result = 0;
         for (int i = 0; i < segments.length; i++)
         {
            if (segments[i] == null)
            {
               break;
            }
            result++;
         }

         return result;
      }

      /**
       * Number of data bytes.
       */
      public int length ()
      {
         int result = 0;
         for (int i = 0; i < numSegments(); i++)
         {
            result += segments[i].length;
         }
         return result;
      }
   }
}