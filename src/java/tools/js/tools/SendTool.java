package js.tools;

import josx.rcxcomm.Tower;
import josx.rcxcomm.TowerException;
import js.common.AbstractTool;
import js.common.ToolProgressMonitor;

/**
 * Simple Lejos tool for sending bytes to rcx.
 */
public class SendTool extends AbstractTool
{
   /**
    * Constructor.
    */
   public SendTool (ToolProgressMonitor monitor)
   {
      super(monitor);
   }

   /**
    * Send bytes to rcx.
    * 
    * @param bytes String representation of hex bytes
    * @param tty port
    * @param fastMode fast mode
    * @throws SendException
    */
   public void start (byte[] bytes, String tty, boolean fastMode) 
     throws SendException
   {
      assert bytes != null: "Precondition: bytes != null";
      assert tty != null: "Precondition: tty != null";
      
      byte[] result = new byte[4096];
      
      Tower tower = new Tower(tty);
      try
      {
         tower.openTower(fastMode);
         getProgressMonitor().log("write");
         dump (bytes, bytes.length);
         tower.writeBytes(bytes);
         getProgressMonitor().log("read");
         int read = tower.readBytes(result);
         dump (result, read);
      }
      catch (TowerException e)
      {
         throw new SendException(e.getMessage(), e);
      }
      finally 
      {
         try
         {
            tower.closeTower();
         }
         catch (TowerException e)
         {
            throw new SendException(e.getMessage(), e);
         }
      }
   }
   
   protected void dump (byte[] bytes, int length)
   {
      StringBuffer result = new StringBuffer(length * 3);
      for (int i = 0; i < bytes.length; i++)
      {
         String string = "00" + Integer.toHexString(bytes[i]);
         result.append(string.substring(string.length() - 2));
         result.append(" ");
      }
      getProgressMonitor().log(result.toString());
   }
}