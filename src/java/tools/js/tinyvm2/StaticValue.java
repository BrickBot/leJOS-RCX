package js.tinyvm2;

import java.io.DataOutputStream;
import java.io.IOException;

import js.tinyvm.io.ByteWriter;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Field;

public class StaticValue extends WritableDataWithOffset implements Constants
{
   byte iType;

   public StaticValue (Field aEntry) throws TinyVMException
   {
      iType = aEntry.getType().getType();
   }

   public int getLength () throws TinyVMException
   {
      return InstanceFieldRecord.getTypeSize(iType);
   }

   public void dump (ByteWriter aOut) throws TinyVMException
   {
      try
      {
         // Static values must be dumped in Big Endian order
         DataOutputStream pDataOut = (DataOutputStream) aOut;
         switch (iType)
         {
            case Constants.T_BOOLEAN:
               pDataOut.writeBoolean(false);
               break;
            case Constants.T_BYTE:
               pDataOut.writeByte(0);
               break;
            case Constants.T_CHAR:
               pDataOut.writeChar(0);
               break;
            case Constants.T_SHORT:
               pDataOut.writeShort(0);
               break;
            case Constants.T_REFERENCE:
            case Constants.T_INT:
               pDataOut.writeInt(0);
               break;
            case Constants.T_FLOAT:
               pDataOut.writeFloat((float) 0.0);
               break;
            case Constants.T_LONG:
               pDataOut.writeLong(0L);
               break;
            case Constants.T_DOUBLE:
               pDataOut.writeInt(0);
               pDataOut.writeFloat((float) 0.0);
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

