package java.util;

/**
 * Represents a long set of bits.
 */
public class BitSet
{
  byte[] iBytes;
  static final int[] MASK = { 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01 }; 

  public BitSet (int nbits)
  {
    iBytes = new byte[(nbits - 1) / 8 + 1];
  }
  
  public void clear (int n)
  {
    int idx = n / 8;
    iBytes[idx] = (byte) ((iBytes[idx] & 0xFF) & ~MASK[n%8]);
  }
  
  public void set (int n)
  {
    int idx = n / 8;
    iBytes[idx] = (byte) ((iBytes[idx] & 0xFF) | MASK[n%8]);
  }
  
  public boolean get (int n)
  {
    return ((iBytes[n/8] & 0xFF) & MASK[n%8]) != 0;
  }
}
