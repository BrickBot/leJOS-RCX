package js.tinyvm;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import js.tinyvm.io.ByteWriter;

import org.apache.bcel.Constants;
import org.apache.bcel.classfile.Constant;
import org.apache.bcel.classfile.ConstantDouble;
import org.apache.bcel.classfile.ConstantFloat;
import org.apache.bcel.classfile.ConstantInteger;
import org.apache.bcel.classfile.ConstantLong;
import org.apache.bcel.classfile.ConstantPool;
import org.apache.bcel.classfile.ConstantString;

/**
 * This class represents a constant value of a basic type.
 */
public class ConstantValue extends WritableDataWithOffset
{
   /**
    * The dereferenced value.
    */
   Object _value;

   /**
    * Constructor.
    * 
    * @param pool constant pool
    * @param constant constant
    */
   public ConstantValue (ConstantPool pool, Constant constant)
   {
      _value = value(pool, constant);

      assert _value != null: "Postconditon: result != null";
   }

   // use Object.equals() for equality
   // use Object.hashCode() for hash code

   /**
    * Dereferenced value.
    */
   public Object value ()
   {
      assert _value != null: "Postconditon: result != null";
      return _value;
   }

   /**
    * Get type of this value.
    */
   public byte getType ()
   {
      if (_value instanceof Double)
      {
         // TODO map long to double correct?
         return Constants.T_LONG;
      }
      else if (_value instanceof Float)
      {
         return Constants.T_FLOAT;
      }
      else if (_value instanceof Integer)
      {
         return Constants.T_INT;
      }
      else if (_value instanceof Long)
      {
         return Constants.T_LONG;
      }
      else if (_value instanceof String)
      {
         return TinyVMConstants.tinyVMType(Constants.T_OBJECT);
      }
      else
      {
         assert false: "Check: known type";
         return -1;
      }
   }

   /**
    * Get length in bytes of value.
    */
   public int getLength ()
   {
      if (_value instanceof Double)
      {
         return 8;
      }
      else if (_value instanceof Float)
      {
         return 4;
      }
      else if (_value instanceof Integer)
      {
         return 4;
      }
      else if (_value instanceof Long)
      {
         return 8;
      }
      else if (_value instanceof String)
      {
         return ((String) _value).getBytes().length;
      }
      else
      {
         assert false: "Check: known type";
         return -1;
      }
   }

   /**
    * Dump.
    * 
    * @param writer byte writer
    */
   public void dump (ByteWriter writer) throws TinyVMException
   {
      assert writer != null: "Precondition: writer != null";

      try
      {
         // Constant values must be dumped in Big Endian order.
         DataOutputStream pDataOut = (DataOutputStream) writer;
         if (_value instanceof Double)
         {
            double doubleValue = ((Double) _value).doubleValue();
            float floatValue = (float) doubleValue;
            if (doubleValue != 0.0
               && Math.abs((doubleValue - floatValue) / doubleValue) > 0.1)
            {
               _logger.log(Level.WARNING, "Double " + doubleValue
                  + " truncated to " + floatValue + "f.");
            }
            pDataOut.writeInt(0);
            pDataOut.writeInt(Float.floatToIntBits(floatValue));
         }
         else if (_value instanceof Float)
         {
            pDataOut.writeInt(Float.floatToIntBits(((Float) _value)
               .floatValue()));
         }
         else if (_value instanceof Integer)
         {
            pDataOut.writeInt(((Integer) _value).intValue());
         }
         else if (_value instanceof Long)
         {
            long longValue = ((Long) _value).longValue();
            int intValue = (int) longValue;
            if (intValue != longValue)
            {
               _logger.log(Level.WARNING, "Long " + longValue
                  + "L truncated to " + intValue + ".");
            }
            pDataOut.writeInt(0);
            pDataOut.writeInt(intValue);
         }
         else if (_value instanceof String)
         {
            byte[] bytes = ((String) _value).getBytes();
            pDataOut.write(bytes, 0, bytes.length);
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

   //
   // protected interface
   //

   /**
    * Get value from constant.
    * 
    * @param pool constant pool
    * @param constant constant to get value from
    * @return Double, Float, Integer, Long or String
    */
   private Object value (ConstantPool pool, Constant constant)
   {
      assert pool != null: "Precondition: pool != null";
      assert constant != null: "Precondition: constant != null";

      Object result = null;
      if (constant instanceof ConstantDouble)
      {
         result = new Double(((ConstantDouble) constant).getBytes());
      }
      else if (constant instanceof ConstantFloat)
      {
         result = new Float(((ConstantFloat) constant).getBytes());
      }
      else if (constant instanceof ConstantInteger)
      {
         result = new Integer(((ConstantInteger) constant).getBytes());
      }
      else if (constant instanceof ConstantLong)
      {
         result = new Long(((ConstantLong) constant).getBytes());
      }
      else if (constant instanceof ConstantString)
      {
         result = new String(((ConstantString) constant).getBytes(pool));
      }
      else
      {
         assert false: "Check: known type";
      }

      assert result != null: "Postconditon: result != null";
      return result;
   }

   private static final Logger _logger = Logger.getLogger("TinyVM");
}

