package js.tinyvm;

import java.io.IOException;

import js.tinyvm.io.IByteWriter;

import org.apache.bcel.classfile.Field;

public class StaticValue extends WritableDataWithOffset
{
   Field _field;
   byte iType;

   public StaticValue (Field aEntry) throws TinyVMException
   {
      _field = aEntry;
      iType = TinyVMConstants.tinyVMType(_field.getType().getType());
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
            case TinyVMConstants.T_BOOLEAN:
               writer.writeBoolean(false);
               break;
            case TinyVMConstants.T_BYTE:
               writer.writeByte(0);
               break;
            case TinyVMConstants.T_CHAR:
               writer.writeChar(0);
               break;
            case TinyVMConstants.T_SHORT:
               writer.writeShort(0);
               break;
            // case TinyVMConstants.T_ARRAY:
            // case TinyVMConstants.T_OBJECT:
            case TinyVMConstants.T_REFERENCE:
            case TinyVMConstants.T_INT:
               writer.writeInt(0);
               break;
            case TinyVMConstants.T_FLOAT:
               writer.writeFloat((float) 0.0);
               break;
            case TinyVMConstants.T_LONG:
               writer.writeLong(0L);
               break;
            case TinyVMConstants.T_DOUBLE:
               writer.writeInt(0);
               writer.writeFloat((float) 0.0);
               break;
            default:
               assert false: "Check: valid type: " + iType;
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

