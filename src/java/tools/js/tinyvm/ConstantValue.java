package js.tinyvm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import js.classfile.JCPE_Double;
import js.classfile.JCPE_Float;
import js.classfile.JCPE_Integer;
import js.classfile.JCPE_Long;
import js.classfile.JCPE_String;
import js.classfile.JCPE_Utf8;
import js.classfile.JConstantPoolEntry;
import js.tinyvm.io.ByteWriter;

public class ConstantValue extends WritableDataWithOffset
{
   JConstantPoolEntry iEntry;

   public ConstantValue (JConstantPoolEntry aEntry)
   {
      iEntry = aEntry;
   }

   public int getLength ()
   {
      if (iEntry instanceof JCPE_String)
      {
         JCPE_Utf8 pValue = ((JCPE_String) iEntry).getValue();
         return pValue.getSize();
      }
      else if (iEntry instanceof JCPE_Integer)
      {
         return 4;
      }
      else if (iEntry instanceof JCPE_Long)
      {
         return 8;
      }
      else if (iEntry instanceof JCPE_Double)
      {
         return 8;
      }
      else if (iEntry instanceof JCPE_Float)
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
         if (iEntry instanceof JCPE_String)
         {
            JCPE_Utf8 pValue = ((JCPE_String) iEntry).getValue();
            byte[] pBytes = pValue.getBytes();
            pDataOut.write(pBytes, 0, pBytes.length);
         }
         else if (iEntry instanceof JCPE_Integer)
         {
            int pValue = ((JCPE_Integer) iEntry).getValue();
            pDataOut.writeInt(pValue);
         }
         else if (iEntry instanceof JCPE_Long)
         {
            long pValue = ((JCPE_Long) iEntry).getValue();
            int pIntValue = (int) pValue;
            if (pIntValue != pValue)
            {
               _logger.log(Level.WARNING, "Long " + pValue + "L truncated to "
                  + pIntValue + ".");
            }
            pDataOut.writeInt(0);
            pDataOut.writeInt(pIntValue);
         }
         else if (iEntry instanceof JCPE_Double)
         {
            double pDoubleValue = ((JCPE_Double) iEntry).getValue();
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
         else if (iEntry instanceof JCPE_Float)
         {
            float pValue = (float) ((JCPE_Float) iEntry).getValue();
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

