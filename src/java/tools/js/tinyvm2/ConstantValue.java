package js.tinyvm2;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import js.tinyvm.io.ByteWriter;

import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;

public class ConstantValue extends WritableDataWithOffset
{
   ConstantPool _pool;
   Constant iEntry;

   public ConstantValue (ConstantPool pool, Constant aEntry)
   {
      _pool = pool;
      iEntry = aEntry;
   }

   public int getLength ()
   {
      if (iEntry instanceof ConstantString)
      {
         // TODO use ConstantUtf8?
         return ((ConstantString) iEntry).getBytes(_pool).getBytes().length;
      }
      else if (iEntry instanceof ConstantInteger)
      {
         return 4;
      }
      else if (iEntry instanceof ConstantLong)
      {
         return 8;
      }
      else if (iEntry instanceof ConstantDouble)
      {
         return 8;
      }
      else if (iEntry instanceof ConstantFloat)
      {
         return 4;
      }
      else
      {
         assert false: "Check: known entry type";
         return 0;
      }
   }

   public void dump (ByteWriter aOut) throws TinyVMException
   {
      try
      {
         // Constant values must be dumped in Big Endian order.
         DataOutputStream pDataOut = (DataOutputStream) aOut;
         if (iEntry instanceof ConstantString)
         {
            // TODO use ConstantUtf8?
            byte[] pBytes = ((ConstantString) iEntry).getBytes(_pool)
               .getBytes();
            pDataOut.write(pBytes, 0, pBytes.length);
         }
         else if (iEntry instanceof ConstantInteger)
         {
            int pValue = ((ConstantInteger) iEntry).getBytes();
            pDataOut.writeInt(pValue);
         }
         else if (iEntry instanceof ConstantLong)
         {
            long pValue = ((ConstantLong) iEntry).getBytes();
            int pIntValue = (int) pValue;
            if (pIntValue != pValue)
            {
               _logger.log(Level.WARNING, "Long " + pValue + "L truncated to "
                  + pIntValue + ".");
            }
            pDataOut.writeInt(0);
            pDataOut.writeInt(pIntValue);
         }
         else if (iEntry instanceof ConstantDouble)
         {
            double pDoubleValue = ((ConstantDouble) iEntry).getBytes();
            float pValue = (float) pDoubleValue;
            if (pDoubleValue != 0.0
               && Math.abs((pDoubleValue - pValue) / pDoubleValue) > 0.1)
            {
               _logger.log(Level.WARNING, "Double " + pDoubleValue
                  + " truncated to " + pValue + "f.");
            }
            pDataOut.writeInt(0);
            pDataOut.writeInt(Float.floatToIntBits(pValue));
         }
         else if (iEntry instanceof ConstantFloat)
         {
            float pValue = (float) ((ConstantFloat) iEntry).getBytes();
            pDataOut.writeInt(Float.floatToIntBits(pValue));
         }
         else
         {
            assert false: "Check: known entry type";
         }
      }
      catch (IOException e)
      {
         throw new TinyVMException(e.getMessage(), e);
      }
   }

   public boolean equals (Object aOther)
   {
      return (aOther == this);
   }

   public int hashCode ()
   {
      return System.identityHashCode(this);
   }

   private static final Logger _logger = Logger.getLogger("TinyVM");
}

