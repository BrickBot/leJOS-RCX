package js.tinyvm;

import java.io.*;
import java.util.*;
import js.classfile.*;

public class StaticValue extends WritableDataWithOffset
implements Constants
{
  int iType;

  public StaticValue (JField aEntry)
  {
    iType = InstanceFieldRecord.descriptorToType (
            aEntry.getDescriptor().toString());
  }

  public int getLength()
  {
    return InstanceFieldRecord.getTypeSize (iType);
  }

  public void dump (ByteWriter aOut) throws Exception
  {
    switch (iType)
    {
      case T_BOOLEAN:
        aOut.writeU1 (0);
        break;
      case T_BYTE:
        aOut.writeU1 (0);
        break;
      case T_CHAR:
        aOut.writeU2 (0);
        break;
      case T_SHORT:
        aOut.writeU2 (0);
        break;
      case T_REFERENCE:
      case T_INT:
        aOut.writeU4 (0);
        break;
      case T_FLOAT:
        aOut.writeU4 (Float.floatToIntBits ((float) 0.0));
        break;
      case T_LONG:
        aOut.writeU8 (0L);
        break;
      case T_DOUBLE:
        aOut.writeU4 (0);
        aOut.writeU4 (Float.floatToIntBits ((float) 0.0));
        break;
      default:
        Utilities.assert (false);
    }
  }

  public boolean equals (Object aOther)
  {
    if (!(aOther instanceof StaticValue))
      return false;
    return ((StaticValue) aOther).iType == iType;    
  }  

  public int hashCode()
  {
    return iType;
  }
}
  
