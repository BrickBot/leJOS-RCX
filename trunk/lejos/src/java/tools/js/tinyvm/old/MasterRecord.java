package js.tinyvm.old;

import java.io.IOException;

import js.tinyvm.io.IByteWriter;
import js.tinyvm.io.IOUtilities;

public class MasterRecord implements WritableData, Constants
{
   Binary iBinary;

   public MasterRecord (Binary aBinary)
   {
      iBinary = aBinary;
   }

   public void dump (IByteWriter aOut) throws TinyVMException
   {
      int pMagicNumber = MAGIC_MASK;
      int pConstantTableOffset = iBinary.iConstantTable.getOffset();
      if (pConstantTableOffset <= 0 || pConstantTableOffset > 0xFFFF)
      {
         throw new TinyVMException("Bug MR-1: Offset=" + pConstantTableOffset
            + " CTSize=" + iBinary.iConstantTable.size());
      }
      int pStaticFieldsOffset = iBinary.iStaticFields.getOffset();
      assert pStaticFieldsOffset >= 0 && pStaticFieldsOffset <= 0xFFFF: "Check: static field offset in range";
      int pStaticStateOffset = iBinary.iStaticState.getOffset();
      assert pStaticStateOffset >= 0 && pStaticStateOffset <= 0xFFFF: "Check: static state offset in range";
      int pStaticStateLength = (iBinary.iStaticState.getLength() + 1) / 2;
      assert pStaticStateLength >= 0 && pStaticStateLength <= 0xFFFF: "Check: state length in range";
      int pNumStaticFields = iBinary.iStaticFields.size();
      int pEntryClassesOffset = iBinary.iEntryClassIndices.getOffset();
      int pNumEntryClasses = iBinary.iEntryClassIndices.size();
      assert pNumEntryClasses < MAX_CLASSES: "Check: not too much classes";
      int pLastClass = iBinary.iClassTable.size() - 1;
      assert pLastClass >= 0 && pLastClass < MAX_CLASSES: "Check: class index in range";

      try
      {
         aOut.writeU2(pMagicNumber);
         aOut.writeU2(pConstantTableOffset);
         aOut.writeU2(pStaticFieldsOffset);
         aOut.writeU2(pStaticStateOffset);
         aOut.writeU2(pStaticStateLength);
         aOut.writeU2(pNumStaticFields);
         aOut.writeU2(pEntryClassesOffset);
         aOut.writeU1(pNumEntryClasses);
         aOut.writeU1(pLastClass);
         IOUtilities.writePadding(aOut, 2);
      }
      catch (IOException e)
      {
         throw new TinyVMException(e.getMessage(), e);
      }
   }

   public int getLength ()
   {
      return IOUtilities.adjustedSize(16, 2);
   }
}

