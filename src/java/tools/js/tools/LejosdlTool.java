package js.tools;

import java.io.IOException;
import java.io.InputStream;

import js.common.AbstractTool;
import js.common.ToolException;
import js.common.ToolProgressMonitor;
import js.tinyvm.TinyVMConstants;

/**
 * Simple Lejos program downloader.
 */
public class LejosdlTool extends AbstractTool
{
   /**
    * Constructor.
    */
   public LejosdlTool (ToolProgressMonitor monitor)
   {
      super(monitor);
   }

   /**
    * Execute program download.
    * 
    * @param program reader with program to download
    * @param tty serial port
    * @param fastMode use fast mode?
    * @throws LejosdlException
    */
   public void start (InputStream program, String tty, boolean fastMode)
      throws LejosdlException
   {
      start(program, tty, true, fastMode);
   }

   /**
    * Execute program download.
    * 
    * @param program reader with program to download
    * @param tty serial port
    * @param download download program?
    * @param fastMode use fast mode?
    * @throws LejosdlException
    */
   public void start (InputStream program, String tty, boolean download,
      boolean fastMode) throws LejosdlException
   {
      assert program != null: "Precondition: program != null";
      assert tty != null: "Precondition: tty != null";

      byte[] buffer = new byte[0x10000];
      int index = 0;

      try
      {
         getProgressMonitor().operation("read binary");
         int read;
         while ((read = program.read(buffer, index, 0x10000 - index)) != -1
            && index < 0x10000)
         {
            index += read;
         }

         if (program.read() != -1)
         {
            // read remaining bytes
            while ((read = program.read(buffer, 0, 0x1000)) != -1)
            {
               index += read;
            }
            throw new LejosdlException("Huge file: " + index + " bytes");
         }
         getProgressMonitor().progress(1000);
      }
      catch (IOException e)
      {
         throw new LejosdlException(
            "Unable to read program: " + e.getMessage(), e);
      }
      finally
      {
         try
         {
            program.close();
         }
         catch (IOException e)
         {
            // ignore
         }
      }

      // TODO reference to TinyVMConstants should be avoided here...
      if (buffer[0] != (byte) ((TinyVMConstants.MAGIC_MASK >> 8) & 0xFF)
         || buffer[1] != (byte) ((TinyVMConstants.MAGIC_MASK >> 0) & 0xFF))
      {
         throw new LejosdlException("Magic number is not right."
            + "\nLinker used was for emulation only?");
      }

      if (download)
      {
         getProgressMonitor().operation("download binary");
         Download d = new Download(getProgressMonitor());
         try
         {
            d.open(tty, fastMode);
            d.downloadProgram(buffer, index);
            d.close();
         }
         catch (ToolException e)
         {
            throw new LejosdlException(e.getMessage(), e);
         }
         finally
         {
            if (d.isOpen())
            {
               d.close();
            }
         }
         getProgressMonitor().progress(1000);
      }
   }
}