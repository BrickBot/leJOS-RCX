package js.tinyvm;

import java.io.*;
import java.util.*;

public class MasterRecord implements WritableData, Constants
{
  Binary iBinary;

  public MasterRecord (Binary aBinary)
  {
    iBinary = aBinary;
  }

  public void dump (ByteWriter aOut) throws Exception
  {
    int pMagicNumber = MAGIC_MASK;
    int pConstantTableOffset = iBinary.iConstantTable.getOffset();
    if (pConstantTableOffset <= 0 || pConstantTableOffset > 0xFFFF)
    {
      Utilities.fatal ("Bug MR-1: Offset=" + pConstantTableOffset + 
                       " CTSize=" + iBinary.iConstantTable.size());
    }
    int pStaticFieldsOffset = iBinary.iStaticFields.getOffset();
    Utilities.assert (pStaticFieldsOffset > 0 && pStaticFieldsOffset <= 0xFFFF);
    int pStaticStateOffset = iBinary.iStaticState.getOffset();
    Utilities.assert (pStaticStateOffset > 0 && pStaticStateOffset <= 0xFFFF);
    
    aOut.writeU2 (pMagicNumber);    
    aOut.writeU2 (pConstantTableOffset);
    aOut.writeU2 (pStaticFieldsOffset);
    aOut.writeU2 (pStaticStateOffset);
    IOUtilities.writePadding (aOut, 2);
  }

  public int getLength()
  {
    return IOUtilities.adjustedSize ( 2 + // magic number
                                      2 + // constant table offset
                                      2 + // static fields offset
                                      2,  // static state offset
           2);
  }
}


