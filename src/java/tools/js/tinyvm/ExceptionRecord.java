package js.tinyvm;

import java.io.*;
import java.util.*;
import js.classfile.*;

public class ExceptionRecord implements WritableData, Constants
{
  JExcep iExcep;
  int iClassIndex;

  public ExceptionRecord (JExcep aExcep, Binary aBinary, JClassFile aCF)
  throws Exception
  {
    iExcep = aExcep;
    int pCPIndex = aExcep.getClassIndex();
    JCPE_Class pCls = (JCPE_Class) aCF.getConstantPool().getEntry (pCPIndex);
    String pName = pCls.getName();
    iClassIndex = aBinary.getClassIndex (pName);    
    if (iClassIndex == -1)
    {
      Utilities.fatal ("Error: Exception " + pName + " not found.");
    }
  }

  public int getLength()
  {
    return IOUtilities.adjustedSize (
				       2 + // start
                                       2 + // end
                                       2 + // handler
				       1,  // class index
           2);
  }

  public void dump (ByteWriter aOut) throws Exception
  {
    int pStart = iExcep.getStartPc();
    int pEnd = iExcep.getEndPc();
    int pHandler = iExcep.getHandlerPc();
    if (pStart > MAX_CODE || pEnd > MAX_CODE || pHandler > MAX_CODE)
    {
      Utilities.fatal ("Error: Exception handler with huge PCs.");
    }
    aOut.writeU2 (pStart);
    aOut.writeU2 (pEnd);
    aOut.writeU2 (pHandler);
    aOut.writeU1 (iClassIndex);
    IOUtilities.writePadding (aOut, 2);
  }

  public boolean equals (Object aOther)
  {
    if (!(aOther instanceof ExceptionRecord))
      return false;
    return ((ExceptionRecord) aOther).iExcep.equals (iExcep);    
  }  

  public int hashCode()
  {
    return iExcep.hashCode();
  }
}
  
