package js.tools;

import josx.rcxcomm.Tower;
import josx.rcxcomm.TowerException;
import js.common.AbstractTool;
import js.common.ToolException;
import js.common.ToolProgressMonitor;

/**
 * RCX Downloading utilities
 * 
 * @author Lawrie Griffiths
 */
public class Download extends AbstractTool
{
   public static final int CHECKSUM_WAIT = 1000;
   private static final int WRITE_MAX = 200;
   private static final int MAX_ZEROS = 32;
   private static final int MAGIC = 0xCAF6;
   private Tower _tower;

   /**
    * Constructor.
    */
   public Download (ToolProgressMonitor monitor)
   {
      super(monitor);
   }

   /**
    * finalize. Closes tower if still open.
    */
   public void finalize ()
   {
      if (isOpen())
      {
         close();
      }
   }

   /**
    * Is tower open?
    */
   public boolean isOpen ()
   {
      return _tower != null;
   }

   /**
    * Open the tower.
    * 
    * @param port serial port
    * @param fastMode use fast mode transfer?
    */
   public void open (String port, boolean fastMode) throws ToolException
   {
      assert port != null: "Precondition: port != null";
      assert !isOpen(): "Precondition: !isOpen()";

      _tower = new Tower(port);

      try
      {
         _tower.openTower(fastMode);
      }
      catch (TowerException e)
      {
         throw new ToolException(e.getMessage(), e);
      }

      if (!_tower.isRCXAlive())
      {
         throw new ToolException("RCX not responding");
      }

      assert isOpen(): "Postcondition: isOpen()";
   }

   /**
    * Close the tower.
    */
   public void close ()
   {
      assert isOpen(): "Precondition: isOpen()";

      try
      {
         _tower.closeTower();
      }
      catch (TowerException e)
      {
         // ignore
      }
      finally
      {
         _tower = null;
      }

      assert !isOpen(): "Postcondition: !isOpen()";
   }

   /**
    * Download a java program.
    * 
    * @param buffer program
    * @param length length of program
    */
   public void downloadProgram (byte[] buffer, int length) throws ToolException
   {
      byte[] send = new byte[3];
      byte[] recv = new byte[3];

      getProgressMonitor().operation("download program");

      // Send program-download message
      send[0] = 0x12;
      send[1] = (byte) (MAGIC >> 8);
      send[2] = (byte) (MAGIC & 0xFF);

      try
      {
         int numRead = _tower.sendPacketReceivePacket(send, recv, 3);
         if (numRead < 3 || recv[1] != send[1] || recv[2] != send[2])
         {
            throw new ToolException("RCX communication: unexpected response."
               + "\nThe RCX either doesn't have valid leJOS firmware "
               + "or it is not in program-download mode.");
         }
      }
      catch (TowerException e)
      {
         throw new ToolException(e.getMessage()
            + "\nPlease make sure RCX has leJOS firmware and is in range."
            + "\nThe firmware must be in program download mode."
            + "\nTurn RCX off and on if necessary.");
      }

      transferData(buffer, length, true);
   }

   /**
    * Install the firmware.
    * 
    * @param image firmware
    * @param length length of firmware
    * @param baseAddress base address of firmware
    */
   public void installFirmware (byte[] image, int length, int baseAddress)
      throws ToolException
   {
      deleteFirmware();
      downloadFirmware(image, length, baseAddress);
      // ROM needs time to check the checksum of the firmware
      try
      {
         Thread.sleep(CHECKSUM_WAIT);
      }
      catch (InterruptedException e)
      {
         throw new ToolException("interrupted");
      }
      unlockFirmware();
   }

   /**
    * Download the firmware.
    * 
    * @param image firmware
    * @param length length of firmware
    * @param baseAddress base address of firmware
    */
   public void downloadFirmware (byte[] image, int len, int baseAddress)
      throws ToolException
   {
      byte[] send = new byte[6];
      byte[] recv = new byte[2];

      getProgressMonitor().operation("Downloading firmware");
      int kB = (len + 1023) / 1024;
      getProgressMonitor().log("Total image size = " + len + " (" + kB + "kB)");

      // Compute image checksum
      // TODO what is this?
      int checksumlength = (baseAddress + len < 0xcc00)
         ? len
         : 0xcc00 - baseAddress;
      short checksum = 0;
      for (int i = 0; i < checksumlength; i++)
      {
         checksum += (image[i] < 0? image[i] + 256 : image[i]);
      }

      // Start firmware download
      send[0] = 0x75;
      send[1] = (byte) ((baseAddress >> 0) & 0xff);
      send[2] = (byte) ((baseAddress >> 8) & 0xff);
      send[3] = (byte) ((checksum >> 0) & 0xff);
      send[4] = (byte) ((checksum >> 8) & 0xff);
      send[5] = 0;

      try
      {
         int numRead = _tower.sendPacketReceivePacket(send, recv, 3);
         if (numRead == 0)
         {
            throw new ToolException(
               "RCX communication: Start firmware download failed");
         }
      }
      catch (TowerException e)
      {
         throw new ToolException("Start firmware download failed: "
            + e.getMessage());
      }

      transferData(image, len, false);

      getProgressMonitor().operation("Firmware downloaded");
   }

   /**
    * Delete the firmware.
    */
   public void deleteFirmware () throws ToolException
   {
      byte[] send = new byte[6];
      byte[] recv = new byte[1];

      getProgressMonitor().operation("Deleting firmware");

      // Delete firmware
      send[0] = 0x65;
      send[1] = 1;
      send[2] = 3;
      send[3] = 5;
      send[4] = 7;
      send[5] = 11;

      try
      {
         int numRead = _tower.sendPacketReceivePacket(send, recv, 3);
         if (numRead != 1)
         {
            throw new ToolException("Delete firmware failed");
         }
      }
      catch (TowerException e)
      {
         throw new ToolException("Delete firmware failed: " + e.getMessage());
      }

      getProgressMonitor().operation("Firmware deleted");
   }

   /**
    * Unlock the firmware.
    */
   public void unlockFirmware () throws ToolException
   {
      byte[] send = new byte[6];
      byte[] recv = new byte[27]; // TODO was 26, is 27 OK???

      getProgressMonitor().operation("Unlocking firmware");

      // Unlock firmware
      send[0] = (byte) 0xa5;
      send[1] = 76; // 'L'
      send[2] = 69; // 'E'
      send[3] = 71; // 'G'
      send[4] = 79; // 'O'
      send[5] = (byte) 174; // trademark symbol (R) in ISO-8859-1

      // Use longer timeout so ROM has time to checksum firmware
      try
      {
         int numRead = _tower.sendPacketReceivePacket(send, recv, 10);
         if (numRead == 0)
         {
            throw new ToolException("Unlock firmware failed");
         }
      }
      catch (TowerException e)
      {
         throw new ToolException("Unlock firmware failed: " + e.getMessage());
      }

      getProgressMonitor().operation("Firmware unlocked");
   }

   /**
    * Transfer data.
    * 
    * @param opcode opcode
    * @param data data array
    * @param length number of bytes to transfer
    * @param terminate0 is last block to transfer number 0?
    */
   public void transferData (byte[] data, int length, boolean terminate0)
      throws ToolException
   {
      byte opcode = 0x45;
      int addr = 0;
      for (int block = 1; addr < length; block++)
      {
         int numToWrite = maxLength(data, length, addr);

         if (getProgressMonitor().isCanceled())
         {
            throw new ToolException("canceled");
         }
         getProgressMonitor().progress(addr * 1000 / length);

         block = terminate0 && length - addr <= WRITE_MAX? 0 : block;
         transferData(opcode, block, data, addr, numToWrite);

         opcode ^= 0x08;
         addr += numToWrite;
      };

      getProgressMonitor().progress(1000);
   }

   /**
    * Get max size of data segment with less than MAX_ZEROS zeros.
    * 
    * @param data data buffer
    * @param length max length
    * @param addr start index
    */
   public int maxLength (byte[] data, int length, int addr)
   {
      int result = Math.min(length - addr, WRITE_MAX);
      for (int i = 0, count = 0; i < result; i++)
      {
         if (data[addr + i] == 0)
         {
            count++;
            if (count >= MAX_ZEROS)
            {
               return i;
            }
         }
         else
         {
            count = 0;
         }
      }
      
      return result;
   }

   //
   // private interface
   //

   /**
    * Transfer a single block of data.
    * 
    * @param opcode opcode
    * @param index index of block to transfer
    * @param data data array
    * @param offset offset in data array
    * @param length number of bytes to transfer
    */
   private void transferData (byte opcode, int index, byte[] data, int offset,
      int length) throws ToolException
   {
      byte[] send = new byte[length + 6];
      byte[] response = new byte[2];

      send[0] = opcode;
      send[1] = (byte) ((index >> 0) & 0xFF);
      send[2] = (byte) ((index >> 8) & 0xFF);
      send[3] = (byte) ((length >> 0) & 0xFF);
      send[4] = (byte) ((length >> 8) & 0xFF);

      // Don't include opcode in this checksum!
      byte checkSum = 0;
      for (int i = 0; i < length; i++)
      {
         checkSum += data[offset + i];
         send[5 + i] = data[offset + i];
      }
      send[5 + length] = checkSum;

      try
      {
         int numRead = _tower.sendPacketReceivePacket(send, response, 10);
         if (numRead >= 2)
         {
            if (response[0] != ~opcode)
            {
               throw new ToolException("Error while downloading: wrong response");
            }
            switch (response[1])
            {
               case 0: break;
               case 3: throw new ToolException("Error while downloading: checksum error");
               case 4: throw new ToolException("Error while downloading: firmware checksum error");
               case 6: throw new ToolException("Error while downloading: download not properly started");
               default: throw new ToolException("Error while downloading: unknown error");
            }
         }
         else
         {
            throw new ToolException("Error while downloading: response too short");
         }
      }
      catch (TowerException e)
      {
         throw new ToolException("Error while downloading: " + e.getMessage());
      }
   }
}
