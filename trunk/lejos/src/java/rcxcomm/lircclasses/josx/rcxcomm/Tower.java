package josx.rcxcomm;
/***************************************************************
*                                                              *
* Tower.java                                                *
*                                                              *
* Description:                                                 *
* This class defines native RCX calls and loads the native     *
* library liblircwrapper.so.                                   *
*							       *
* Author:    Lawrie Griffiths                                  *
*                                                              *
***************************************************************/
public class Tower
{
	/**
	 * Private members
	 */
	private int usbFlag		= 1;
	private int err 		= 0;
	/**
	 * -----------------------------
	 */

	/**
	 * Load library
	 */
	static
	{
		try
		{
			System.loadLibrary("josx_rcxcomm_Tower");
		}
		catch (UnsatisfiedLinkError e)
		{
			System.out.println("Library not loaded."+ e);
		}
	}

	/**
	 * Native functions
	 */

	/** Open the tower
   	 * @param port port to use, e.g. usb or COM1
   	 */
	public native int open(String p);

	/** Close the tower
   	 *@return error number or zero for success
   	 */
    	public native int close();

    	/** send a packet to the RCX, e.g 0x10 for ping
   	 * @param b packet to send
   	 * @param n number of bytes
   	 * @return error number
   	 */
    	public native int send(byte b[], int n);

        /** receive a packet to the RCX
   	 * @param b buffer to receive packet into
   	 * @return error number
   	 */
    	public native int receive(byte b[]);

    	/** Write low-level bytes to the tower, e.g 0x55ff0010ef10ef for ping
   	 * @param b bytes to send
   	 * @param n number of bytes
   	 * @return error number
   	 */
    	public native int write(byte b[], int n);

    	/** Low-level read
   	 * @param b buffer to receive bytes
   	 * @return number of bytes read
   	 */
 	public native int read(byte b[]);

 	public native int message(String msg);
	/**
	 * -----------------------------
	 */

	/**
	 * Constructor
	 */
	public Tower()
	{
		err = 0;
	}
	/**
	 * Member functions
	 */
	public int open()
	{
 	   return open("");
 	}

	public int getError()
	{
  	  return err;
  	}

	public void setError(int e) {
		err = 0;
  	}

	public int getUsbFlag()
	{
 	   return usbFlag;
  	}

	public String strerror(int errno) {
    		switch (errno) {
    			case 0: return "no error";
    			case 1: return "tower not responding";
    			case 2: return "bad ir link";
    			case 3: return "bad ir echo";
    			case 4: return "no response from rcx";
    			case 5: return "bad response from rcx";
    			case 6: return "write failure";
    			case 7: return "read failure";
    			case 8: return "open failure";
    			case 9: return "internal error";
    			case 10: return "already closed";
    			case 11: return "already open";
    			case 12: return "not open";
    			default: return "unknown error";
    		}
  	}


	public int isAlive() {
		byte [] ping = {0x10};
		byte [] buf = new byte[10];

		send(ping,1);
		int r = receive(buf);
		if (r != 1) {
		        System.out.println("Tower: returned " + r + " from receive");
			return 0;
		}
		if (buf[0] != (byte) 0xef) {
			System.out.println("Tower: buf[0] = " + (buf[0] & 0xff));
			return 0;
		}
		return 1;
	}
}
