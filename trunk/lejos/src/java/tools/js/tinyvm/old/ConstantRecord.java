package js.tinyvm.old;

import java.io.IOException;

import js.tinyvm.io.IByteWriter;
import js.tinyvm.io.IOUtilities;
import js.tinyvm.old.classfile.JCPE_Double;
import js.tinyvm.old.classfile.JCPE_Float;
import js.tinyvm.old.classfile.JCPE_Integer;
import js.tinyvm.old.classfile.JCPE_Long;
import js.tinyvm.old.classfile.JCPE_String;
import js.tinyvm.old.classfile.JConstantPoolEntry;

public class ConstantRecord implements WritableData, Constants
{
   JConstantPoolEntry iEntry;
   ConstantValue iConstantValue;
   int iSize = -1;

   public ConstantRecord (JConstantPoolEntry aEntry) throws TinyVMException
   {
      iEntry = aEntry;
      if (aEntry instanceof JCPE_String)
      {
         iSize = ((JCPE_String) aEntry).getSize();
         if (iSize > MAX_STRING_CONSTANT_LENGTH)
         {
            throw new TinyVMException("String constant of length more than "
               + MAX_STRING_CONSTANT_LENGTH + " not accepted: " + aEntry);
         }
      }
      else if (aEntry instanceof JCPE_Double || aEntry instanceof JCPE_Long)
      {
         iSize = 8;
      }
      else if (aEntry instanceof JCPE_Integer || aEntry instanceof JCPE_Float)
      {
         iSize = 4;
      }
      else
      {
         assert false: "Check: known entry type";
      }
   }

   public static int getType (JConstantPoolEntry aEntry)
   {
      if (aEntry instanceof JCPE_String)
         return T_REFERENCE;
      else if (aEntry instanceof JCPE_Double || aEntry instanceof JCPE_Long)
         return T_LONG;
      else if (aEntry instanceof JCPE_Integer)
         return T_INT;
      else if (aEntry instanceof JCPE_Float)
         return T_FLOAT;
      else
      {
         assert false: "Check: known type";
         return -1;
      }
   }

   public void setConstantValue (ConstantValue aValue)
   {
      iConstantValue = aValue;
   }

   public int getLength ()
   {
      return IOUtilities.adjustedSize(2 + // offset
         1 + // type
         1, // size
         2);
   }

   public int getOffset () throws TinyVMException
   {
      return iConstantValue.getOffset();
   }

   public int getConstantSize ()
   {
      return iSize;
   }

   public void dump (IByteWriter aOut) throws TinyVMException
   {
      assert iSize != -1: "Check: iSize != -1";
      assert iConstantValue != null: "Check: iConstantValue != null";

      try
      {
         aOut.writeU2(iConstantValue.getOffset());
         aOut.writeU1(getType(iEntry));
         aOut.writeU1(iSize);
         IOUtilities.writePadding(aOut, 2);
      }
      catch (IOException e)
      {
         throw new TinyVMException(e.getMessage(), e);
      }
   }

   public boolean equals (Object aOther)
   {
      if (!(aOther instanceof ConstantRecord))
         return false;
      return ((ConstantRecord) aOther).iEntry.equals(iEntry);
   }

   public int hashCode ()
   {
      return iEntry.hashCode();
   }
}