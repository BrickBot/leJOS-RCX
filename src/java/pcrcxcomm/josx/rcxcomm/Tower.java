package josx.rcxcomm;

/** Low-level interface to the Lego IR Tower
 * Used by RCXPort to send and receive messages to and from the RCX.
 * Can be used to send any packet or any sequence of bytes to the RCX.
 * The tower is opened with a call to open() or open(port) and
 * closed by a call to close().
 * <code>send</code> can be used to send a packet, and receive to receive one.
 * <code>write</code> can read a sequence of bytes, and read can read them.
 */
public class Tower {

  /** Set fast mode
   * @param fast - 0 = slow mode, 1 = fast mode
   */
  public native int setFast (int fast);

  /** Open the tower
   * @param port port to use, e.g. usb or COM1
   */
  public native int open(String p);

  /** Close the tower
   *@return error number or zero for success
   */
  public native int close();

  /** Write low-level bytes to the tower, e.g 0xff550010ef10ef for ping
   * @param b bytes to send
   * @param n number of bytes
   * @return error number
   */
  public native int write(byte b[], int n);

  /** send a packet to the RCX, e.g 0x10 for ping
   * @param b packet to send
   * @param n number of bytes
   * @return error number
   */
  public native int send(byte b[], int n);
  
  /** Low-level read
   * @param b buffer to receive bytes
   * @return number of bytes read
   */
  public native int read(byte b[]);

  /** Receive a packet
   * @param b buffer to receive packet
   * @return number of bytes read
   */
  public native int receive(byte b[]);

  /** dump hex to standard out
   * @param prefix identifies the dump
   * @param b bytes to dump
   * @param n numberof bytes
   */
  public native void hexdump(String prefix, byte b[], int n);
  
  /** Check if RCX is alive
   * @return 1 if alive, 0 if not
   */
  public native int isAlive();

  private int err;
  private long fh;
  private int usbFlag;

  /** Open the tower
   * @return an error number or zero for success
   */
  public int open() {
    return open("");
  }

  /** Converts an error number to a string
   * Note you should negate the error number before passing it to this method
   * as this method expects a positive value.
   * param errno the negation of the returned error
   * @return the message
   */
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

  /** Create the tower class
   *
   */
  public Tower() {
    err = 0;
  }

  /** Get the last OS error
   * @return the error number
   */
  public int getError() {
    return err;
  }

  /** Getter for USB Flag
   * @return USB Flag as an integer
   */
  public int getUsbFlag() {
    return usbFlag;
  }

  /** Setter for OS Error
   *
   */
  public void setError(int e) {
    err = e;
  }

  static {
    System.loadLibrary("jirtrcx");
  }
}
