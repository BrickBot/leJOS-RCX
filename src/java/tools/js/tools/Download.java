package js.tools;

import josx.rcxcomm.Tower;

/**
 * RCX Downloading utilities
 * @author Lawrie Griffiths
 */
public class Download {

  private static final int TOWRITEMAX = 200;
  private static final int MAGIC = 0xCAF6;
  private static Tower tower;

  /**
   * Open the tower
   **/
  public static void open(String port, boolean fastMode) {
    tower = new Tower();
    int r;
    tower.setFast((fastMode ? 1 : 0));
    if ((r=tower.open(port)) < 0) {
      System.err.println("Open failed: " + tower.strerror(-r) +
                 ", errno = " + tower.getError());
      System.exit(1);
    }
    if (tower.isAlive() == 0) {
      System.err.println("Tower not responding");
      System.exit(1);
    }
  }

  /**
   * Close the tower
   */
  public static void close() {
    tower.close();
  }

  /**
   * Transfer a block of data
   **/
  public static int transfer_data (
                      byte opcode, 
                      int index,
                      byte [] buffer,
                      int offset, 
                      int length)
  {
    byte checkSum;
    byte [] response = new byte[2];
    int  i, r = 0;

    byte [] actualBuffer = new byte[length + 6];

    actualBuffer[0] = opcode;
    actualBuffer[1] = (byte) ((index >> 0) & 0xFF);
    actualBuffer[2] = (byte) ((index >> 8) & 0xFF);
    actualBuffer[3] = (byte) ((length >> 0) & 0xFF);
    actualBuffer[4] = (byte) ((length >> 8) & 0xFF);

    checkSum = 0;

    // Don't include opcode in this checksum!

    for (i = 0; i < length; i++)
    {
      checkSum += buffer[offset+i];
      actualBuffer[5 + i] = buffer[offset+i];
    }

    actualBuffer[5 + i] = checkSum;

    // Try 10 times

    for(i=0;i<10 && r <=0;i++) {
      tower.send(actualBuffer, length + 6);
      r = tower.receive(response);
    }

    if (r >=0 && response[1] == 3)
    {
      System.err.println("Checksum failed");
      r = -3;
    }
    if (r < 0) {
      System.err.println("Response = " + r);
      System.exit(1);
    }
    return r;
  }

  /**
   * Download a java program
   **/
  public static void downloadProgram(byte [] buffer, int len) {
    byte [] send = new byte[3];
    int numRead = 0, numToWrite, index, status;
    byte [] recv = new byte[3];
    byte opcode = 0x45;
    int offset = 0;
   
    // Send program-download message

    send[0] = 0x12;
    send[1] = (byte) (MAGIC >> 8);
    send[2] = (byte) (MAGIC & 0xFF);

    // Try 3 times
    
    for(int i=0;i<3 && numRead <= 0;i++) {
      tower.send(send, 3);
      numRead = tower.receive(recv);
    }

    if (numRead != 3)
    {
      System.err.println (numRead == -4 ? "No response from RCX. " : "Bad response from RCX. ");
      System.err.println("Status = " + numRead + " : " + tower.strerror(-numRead));
      System.err.println("Please make sure RCX has leJOS firmware " +
              "and is in range. The firmware must be in program download mode. " +
	      "Turn RCX off and on if necessary.\n");
      System.exit (1);
    }
        
    if (recv[1] != send[1] || recv[2] != send[2])
    {
      System.err.println("Unexpected response from RCX. The RCX either doesn't have valid leJOS firmware or " +
              "it is not in program-download mode. (lejosfirmdl downloads firmware).");
      System.exit (1);
    }

    // Transfer data  
  
    numToWrite = TOWRITEMAX;
    for(index = 1; numToWrite == TOWRITEMAX; index++) {
      numToWrite = (len - offset > TOWRITEMAX ? TOWRITEMAX : len - offset);
      System.err.print("\r  " + (int) (((float) offset/(float) len)*100f) + "%\r");
      if ((status = Download.transfer_data (opcode, len - offset <= TOWRITEMAX ? 0 : index, 
                                   buffer, offset, numToWrite)) < 0)
      {
          System.err.println("Unexpected response from RCX whilst downloading: " + status);
          System.exit(1);
      }
      opcode ^= 0x08;
      offset += numToWrite;
    };
    
    System.err.print("\r  100%\r");
  }

  /**
   * Download the firmware 
   */
  public static void downloadFirmware(byte [] image, int len, int start, boolean verbose)
  {
    short cksum = 0;
    byte [] send = new byte[6];
    byte [] recv = new byte[2];
    int addr = 0, index, size, i;
    byte opcode = 0x45;
    int numToWrite, status;

    if (verbose) System.err.println("Downloading firmware");
    if (verbose) System.err.println("Total image size = " + len + "(" + (len+1023)/1024 + "k)");
    // System.err.println("start = " + start);

    /* Compute image checksum */
    int cksumlen = (start + len < 0xcc00) ? len : 0xcc00 - start;
    int r = 0;

    for (i = 0; i < cksumlen; i++)
	cksum += (image[i] < 0 ? image[i] + 256 : image[i]);

    // System.err.println("Checksum = " + cksum);

    /* Start firmware download */
    send[0] = 0x75;
    send[1] = (byte) ((start >> 0) & 0xff);
    send[2] = (byte) ((start >> 8) & 0xff);
    send[3] = (byte) ((cksum >> 0) & 0xff);
    send[4] = (byte) ((cksum >> 8) & 0xff);
    send[5] = 0;

    // try 3 times
    
    for(i=0;i<3 && r <=0;i++) {
      tower.send(send,6);
      r = tower.receive(recv);
    }

    if (r <= 0) {
      System.err.println("Start firmware download failed");
      System.exit(1);
    }

    // Transfer the data

    // System.err.println("Length = " + len);
    // System.err.println("TOWRITEMAX " + TOWRITEMAX);
 
    numToWrite = TOWRITEMAX;
    for(index = 1; numToWrite == TOWRITEMAX; index++) {
      numToWrite = (addr + TOWRITEMAX > len ? len - addr : TOWRITEMAX);
      if (verbose) System.err.print("\r  " + (int) (((float) addr/(float) len)*100f) + "%\r");
      String s = "Transfer = ";
      if ((status = transfer_data (opcode, numToWrite < TOWRITEMAX ? index : index, 
                                   image, addr, numToWrite)) < 0)
      {
          System.err.println("Unexpected response from RCX whilst downloading: " + status);
          System.exit(1);
      }
      for(i=0;i<numToWrite;i++) s += " " + image[addr + i];
      // System.err.println(s);
      opcode ^= 0x08;
      addr += numToWrite;
    }
    if (verbose) System.err.println("Firmware downloaded");
  }

  /**
   * Delete the firmware
   **/
  public static void deleteFirmware(boolean verbose)
  {
    byte [] send = new byte[6];
    byte [] recv = new byte[1];
    int r=0;

    if (verbose) System.err.println("Deleting firmware");

    /* Delete firmware */
    send[0] = 0x65;
    send[1] = 1;
    send[2] = 3;
    send[3] = 5;
    send[4] = 7;
    send[5] = 11;

    // Needs at least 2 goes to delete firmware 

    for(int i=0;i<3 && r <= 0;i++) {
      tower.send(send,6);
      r = tower.receive(recv);
    }

    if (r != 1) {
      System.err.println("Delete firmware failed, response = " + r);
      System.exit(1);
    }
    if (verbose) System.err.println("Firmware deleted");
  }

  /**
   * Unlock the firmware
   **/
  public static void unlockFirmware(boolean verbose)
  {
    byte [] send = new byte[6];
    byte [] recv = new byte[26];
    int r = 0;

    if (verbose) System.err.println("Unlocking firmware");

    /* Unlock firmware */
    send[0] = (byte) 0xa5;
    send[1] = 76;		// 'L'
    send[2] = 69;		// 'E'
    send[3] = 71;		// 'G'
    send[4] = 79;		// 'O'
    send[5] = (byte) 174;	// '®'

    /* Use longer timeout so ROM has time to checksum firmware */

    // Try 10 times
    
    for(int i=0;i<10 && r <= 0;i++) {
      tower.send(send,6);
      r = tower.receive(recv);
    }

    if (r <= 0) {
      System.err.println("Unlock firmware failed, response = " + r);
      System.exit(1);
    }

    if (verbose) System.err.println("Firmware unlocked");
  }

  /**
   * Install the firmware
   **/
  public static void installFirmware(byte [] image, int length,
	               int entry, boolean verbose)
  {
    deleteFirmware(verbose);
    downloadFirmware(image, length, entry, verbose);
    unlockFirmware(verbose);
  }
}

