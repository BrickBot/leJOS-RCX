package js.classfile;
import java.io.*;

public class JCPE_Float extends JConstantPoolEntry
{
  private float iValue;

  public JCPE_Float (IConstantPool aConstantPool,
                     float aValue) 
  {
    super (aConstantPool);
    iValue = aValue;
  }

  public JCPE_Float (IConstantPool aConstantPool)
  {
    super (aConstantPool);
  }

  public void dump (OutputStream aOut)
  throws Exception
  {
    aOut.write (JConstantPoolEntry.CONSTANT_Float); // 1 byte
    JIO.writeU4 (aOut, Float.floatToIntBits (iValue));
  }
  
  public void read (InputStream aIn)
  throws Exception
  {
    // Assume tag already read
    iValue = Float.intBitsToFloat(JIO.readU4 (aIn));
  }

  public final void update()
  {
  }

  public int hashCode()
  {
    return Float.floatToIntBits(iValue);
  }

  public float getValue()
  {
    return iValue;
  }

  public boolean equals (Object aObj)
  {
    if (aObj instanceof  JCPE_Float)
    {
      float pOther = ((JCPE_Float) aObj).iValue;
      return (iValue == pOther ||
              (Float.isNaN(iValue) && Float.isNaN(pOther)));
    }
    return false;
  }

  public String toString()
  {
    return String.valueOf (iValue);
  }
}
