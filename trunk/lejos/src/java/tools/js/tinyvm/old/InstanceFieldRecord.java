package js.tinyvm.old;

import java.io.IOException;

import js.tinyvm.io.IByteWriter;
import js.tinyvm.old.classfile.JField;

public class InstanceFieldRecord implements WritableData, Constants
{
   JField iField;
   int iType;

   public InstanceFieldRecord (JField aEntry) throws TinyVMException
   {
      iField = aEntry;
      iType = descriptorToType(iField.getDescriptor().toString());
   }

   public String getName ()
   {
      return iField.getName();
   }

   public static int descriptorToType (String aDesc) throws TinyVMException
   {
      switch (aDesc.charAt(0))
      {
         case 'B':
            return T_BYTE;
         case 'C':
            return T_CHAR;
         case 'D':
            return T_DOUBLE;
         case 'F':
            return T_FLOAT;
         case 'I':
            return T_INT;
         case 'J':
            return T_LONG;
         case 'S':
            return T_SHORT;
         case 'Z':
            return T_BOOLEAN;
         case 'L':
         case '[':
            return T_REFERENCE;
         default:
         {
            throw new TinyVMException("Bug IFR-2: " + aDesc);
         }
      }
   }

   public int getLength ()
   {
      return 1;
   }

   public void dump (IByteWriter aOut) throws TinyVMException
   {
      try
      {
         aOut.writeU1((int) iType);
      }
      catch (IOException e)
      {
         throw new TinyVMException(e.getMessage(), e);
      }
   }

   public static int getTypeSize (int aType) throws TinyVMException
   {
      switch (aType)
      {
         case T_BYTE:
         case T_BOOLEAN:
            return 1;
         case T_SHORT:
         case T_CHAR:
            return 2;
         case T_INT:
         case T_REFERENCE:
         case T_FLOAT:
            return 4;
         case T_LONG:
         case T_DOUBLE:
            return 8;
         default:
         {
            throw new TinyVMException("Bug SV-1: " + aType);
         }
      }
   }

   public int getFieldSize () throws TinyVMException
   {
      return getTypeSize(iType);
   }

   public boolean equals (Object aOther)
   {
      if (!(aOther instanceof InstanceFieldRecord))
         return false;
      return ((InstanceFieldRecord) aOther).iField.equals(iField);
   }

   public int hashCode ()
   {
      return iField.hashCode();
   }
}

