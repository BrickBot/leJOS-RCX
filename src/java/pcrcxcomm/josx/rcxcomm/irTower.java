package josx.rcxcomm;

// Native methods to access the Lego IR Tower

public class irTower {

  public native int open(String p);
  public native int close();
  public native int write(byte b[], int n);
  public native int send(byte b[], int n);
  public native int read(byte b[]);
  public native int receive(byte b[]);
  public native void hexdump(String prefix, byte b[], int n);
  public native int isAlive();

  private int err;
  long fh;

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

  public void irTower() {
    err = 0;
  }

  public int getError() {
    return err;
  }

  public void setError(int e) {
    err = e;
  }

  static {
    System.loadLibrary("irtower");
  }
}