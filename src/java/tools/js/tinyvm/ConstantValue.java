package js.tinyvm;

import java.io.*;
import java.util.*;
import js.classfile.*;

public class ConstantValue extends WritableDataWithOffset
{
  JConstantPoolEntry iEntry;

  public ConstantValue (JConstantPoolEntry aEntry)
  {
    iEntry = aEntry;
  }

  public int getLength()
  {
    if (iEntry instanceof JCPE_String)
    {
      JCPE_Utf8 pValue = ((JCPE_String) iEntry).getValue();
      return pValue.getSize();
    }
    else if (iEntry instanceof JCPE_Integer)
    {
      return 4;
    }
    else if (iEntry instanceof JCPE_Long)
    {
      return 8;
    }
    else if (iEntry instanceof JCPE_Double)
    {
      return 8;
    }
    else if (iEntry instanceof JCPE_Float)
    {
      return 4;
    }
    else
    {
      Utilities.assert (false);
      return 0;
    }
  }

  public void dump (ByteWriter aOut) throws Exception
  {
    DataOutputStream pDataOut = (DataOutputStream) aOut;
    if (iEntry instanceof JCPE_String)
    {
      JCPE_Utf8 pValue = ((JCPE_String) iEntry).getValue();
      byte[] pBytes = pValue.getBytes();
      pDataOut.write (pBytes, 0, pBytes.length);
    }
    else if (iEntry instanceof JCPE_Integer)
    {
      int pValue = ((JCPE_Integer) iEntry).getValue();
      pDataOut.writeInt (pValue);
    }
    else if (iEntry instanceof JCPE_Long)
    {
      long pValue = ((JCPE_Long) iEntry).getValue();
      pDataOut.writeLong (pValue);      
    }
    else if (iEntry instanceof JCPE_Double)
    {
      float pValue = (float) ((JCPE_Double) iEntry).getValue();
      pDataOut.writeFloat (0);
      pDataOut.writeFloat (pValue);
    }
    else if (iEntry instanceof JCPE_Float)
    {
      float pValue = (float) ((JCPE_Float) iEntry).getValue();
      //System.out.println ("$$ " + pValue + ": " + Integer.toHexString ((Float.floatToIntBits (pValue))) + " $offset = " + getOffset());
      pDataOut.writeFloat (pValue);
    }
    else
    {
      Utilities.assert (false);
    }
  }

  public boolean equals (Object aOther)
  {
    return (aOther == this);
  }  

  public int hashCode()
  {
    return System.identityHashCode (this);
  }
}
  
