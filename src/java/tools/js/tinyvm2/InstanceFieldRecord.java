package js.tinyvm2;

import java.io.IOException;

import js.tinyvm.io.ByteWriter;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Field;

public class InstanceFieldRecord implements WritableData
{
   Field iField;
   byte iType;

   public InstanceFieldRecord (Field aEntry) throws TinyVMException
   {
      iField = aEntry;
      iType = iField.getType().getType();
   }

   public String getName ()
   {
      return iField.getName();
   }

   public int getLength ()
   {
      return 1;
   }

   public void dump (ByteWriter aOut) throws TinyVMException
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
         case Constants.T_BYTE:
         case Constants.T_BOOLEAN:
            return 1;
         case Constants.T_SHORT:
         case Constants.T_CHAR:
            return 2;
         case Constants.T_INT:
         case Constants.T_REFERENCE:
         case Constants.T_FLOAT:
            return 4;
         case Constants.T_LONG:
         case Constants.T_DOUBLE:
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

