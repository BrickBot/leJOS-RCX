package js.tinyvm;

import java.io.IOException;

import js.tinyvm.io.IByteWriter;

import org.apache.bcel.classfile.Field;

public class InstanceFieldRecord implements WritableData
{
   Field iField;
   byte iType;

   public InstanceFieldRecord (Field aEntry) throws TinyVMException
   {
      iField = aEntry;
      iType = TinyVMConstants.tinyVMType(iField.getType().getType());
   }

   public String getName ()
   {
      return iField.getName();
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

   /**
    * Get type size in bytes.
    * 
    * @param type tiny vm type to get size for
    */
   public static int getTypeSize (byte type) throws TinyVMException
   {
      switch (type)
      {
         case TinyVMConstants.T_BYTE:
         case TinyVMConstants.T_BOOLEAN:
            return 1;
         case TinyVMConstants.T_SHORT:
         case TinyVMConstants.T_CHAR:
            return 2;
         // case TinyVMConstants.T_ARRAY:
         // case TinyVMConstants.T_OBJECT:
         case TinyVMConstants.T_REFERENCE:
         case TinyVMConstants.T_INT:
         case TinyVMConstants.T_FLOAT:
            return 4;
         case TinyVMConstants.T_LONG:
         case TinyVMConstants.T_DOUBLE:
            return 8;
         default:
         {
            throw new TinyVMException("Undefined type: " + type);
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

