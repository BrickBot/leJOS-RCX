package js.tinyvm;

import java.io.*;
import java.util.*;
import js.classfile.*;

public class StaticFieldRecord implements WritableData, Constants
{
  ClassRecord iClassRecord;
  JField iField;

  public StaticFieldRecord (JField aEntry, ClassRecord aRec)
  {
    iField = aEntry;
    iClassRecord = aRec;
  }

  public String getName()
  {
    return iField.getName();
  }

  public int getLength()
  {
    return 2;
  }

  public void dump (ByteWriter aOut) throws Exception
  {
    int pType = JClassName.descriptorToType (iField.getDescriptor().toString());
    Assertion.test (pType <= 0xF);
    Assertion.test (pType >= 0);
    int pOffset = iClassRecord.getStaticFieldOffset (iField.getName());
    Assertion.test (pOffset >= 0);
    Assertion.test (pOffset <= 0x0FFF);
    aOut.writeU2 ((pType << 12) | pOffset);
  }

  public boolean equals (Object aOther)
  {
    if (!(aOther instanceof StaticFieldRecord))
      return false;
    return ((StaticFieldRecord) aOther).iField.equals (iField)  &&
           ((StaticFieldRecord) aOther).iClassRecord.equals (iClassRecord);
  }  

  public int hashCode()
  {
    return iField.hashCode();
  }
}
  
