package java.lang;

/**
 * System utilities.
 */
public class System
{
  private System() {}
  
  /**
   * Native copy of one array to another.
   */
  public static native void arraycopy (Object src, int srcoffset, Object dest, int destoffset, int length);
  
  /**
   * Current time expressed in milliseconds. In Java, this would
   * be since January 1st, 1970. In the RCX, this is the number
   * of milliseconds since the RCX has been on.
   */
  public static native long currentTimeMillis();
}

