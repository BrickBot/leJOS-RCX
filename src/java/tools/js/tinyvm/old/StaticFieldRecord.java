package js.tinyvm.old;

import java.io.IOException;

import js.tinyvm.io.IByteWriter;
import js.tinyvm.old.classfile.JClassName;
import js.tinyvm.old.classfile.JField;

public class StaticFieldRecord implements WritableData, Constants
{
   ClassRecord iClassRecord;
   JField iField;

   public StaticFieldRecord (JField aEntry, ClassRecord aRec)
   {
      iField = aEntry;
      iClassRecord = aRec;
   }

   public String getName ()
   {
      return iField.getName();
   }

   public int getLength ()
   {
      return 2;
   }

   public void dump (IByteWriter aOut) throws TinyVMException
   {
      int pType = JClassName
         .descriptorToType(iField.getDescriptor().toString());
      assert pType >= 0 && pType <= 0xF: "Check: valid type";
      int pOffset = iClassRecord.getStaticFieldOffset(iField.getName());
      assert pOffset >= 0 && pOffset <= 0x0FFF: "Check offset in range";

      try
      {
         aOut.writeU2((pType << 12) | pOffset);
      }
      catch (IOException e)
      {
         throw new TinyVMException(e.getMessage(), e);
      }
   }

   public boolean equals (Object aOther)
   {
      if (!(aOther instanceof StaticFieldRecord))
         return false;
      return ((StaticFieldRecord) aOther).iField.equals(iField)
         && ((StaticFieldRecord) aOther).iClassRecord.equals(iClassRecord);
   }

   public int hashCode ()
   {
      return iField.hashCode();
   }
}

