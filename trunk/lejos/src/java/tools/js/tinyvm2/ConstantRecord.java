package js.tinyvm2;

import java.io.IOException;

import js.tinyvm.io.ByteWriter;
import js.tinyvm.io.IOUtilities;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;

public class ConstantRecord implements WritableData, Constants
{
   Constant iEntry;
   ConstantValue iConstantValue;
   int iSize = -1;

   public ConstantRecord (ConstantPool pool, Constant aEntry)
      throws TinyVMException
   {
      iEntry = aEntry;
      if (aEntry instanceof ConstantString)
      {
         // TODO use ConstantUtf8?
         iSize = ((ConstantString) aEntry).getBytes(pool).getBytes().length;
         if (iSize > TinyVMConstants.MAX_STRING_CONSTANT_LENGTH)
         {
            throw new TinyVMException("String constant of length more than "
               + TinyVMConstants.MAX_STRING_CONSTANT_LENGTH + " not accepted: "
               + aEntry);
         }
      }
      else if (aEntry instanceof ConstantDouble
         || aEntry instanceof ConstantLong)
      {
         iSize = 8;
      }
      else if (aEntry instanceof ConstantInteger
         || aEntry instanceof ConstantFloat)
      {
         iSize = 4;
      }
      else
      {
         assert false: "Check: known entry type";
      }
   }

   public static int getType (Constant aEntry)
   {
      if (aEntry instanceof ConstantString)
         return Constants.T_REFERENCE;
      else if (aEntry instanceof ConstantDouble
         || aEntry instanceof ConstantLong)
         return T_LONG;
      else if (aEntry instanceof ConstantInteger)
         return T_INT;
      else if (aEntry instanceof ConstantFloat)
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

   public void dump (ByteWriter aOut) throws TinyVMException
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