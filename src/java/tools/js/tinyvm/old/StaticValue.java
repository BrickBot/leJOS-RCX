package js.tinyvm.old;

import java.io.IOException;

import js.tinyvm.io.IByteWriter;
import js.tinyvm.old.classfile.JField;

public class StaticValue extends WritableDataWithOffset implements Constants
{
   int iType;

   public StaticValue (JField aEntry) throws TinyVMException
   {
      iType = InstanceFieldRecord.descriptorToType(aEntry.getDescriptor()
         .toString());
   }

   public int getLength () throws TinyVMException
   {
      return InstanceFieldRecord.getTypeSize(iType);
   }

   public void dump (IByteWriter writer) throws TinyVMException
   {
      try
      {
         // Static values must be dumped in Big Endian order
         switch (iType)
         {
            case T_BOOLEAN:
               writer.writeBoolean(false);
               break;
            case T_BYTE:
               writer.writeByte(0);
               break;
            case T_CHAR:
               writer.writeChar(0);
               break;
            case T_SHORT:
               writer.writeShort(0);
               break;
            case T_REFERENCE:
            case T_INT:
               writer.writeInt(0);
               break;
            case T_FLOAT:
               writer.writeFloat((float) 0.0);
               break;
            case T_LONG:
               writer.writeLong(0L);
               break;
            case T_DOUBLE:
               writer.writeInt(0);
               writer.writeFloat((float) 0.0);
               break;
            default:
               assert false: "Check: valid type";
         }
      }
      catch (IOException e)
      {
         throw new TinyVMException(e.getMessage(), e);
      }
   }

   public boolean equals (Object aOther)
   {
      if (!(aOther instanceof StaticValue))
         return false;
      return ((StaticValue) aOther).iType == iType;
   }

   public int hashCode ()
   {
      return iType;
   }
}

