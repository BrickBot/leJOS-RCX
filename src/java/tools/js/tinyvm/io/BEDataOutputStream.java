package js.tinyvm.io;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BEDataOutputStream extends DataOutputStream implements ByteWriter
{
   public BEDataOutputStream (OutputStream aOut)
   {
      super(aOut);
   }

   public void writeU1 (int aByte) throws IOException
   {
      // System.out.println("B:" + Integer.toHexString(aByte));
      write(aByte);
   }

   public void writeU2 (int aShort) throws IOException
   {
      // System.out.println("S:" + Integer.toHexString(aShort));
      writeShort(aShort);
   }

   public void writeU4 (int aInt) throws IOException
   {
      // System.out.println("I:" + Integer.toHexString(aInt));
      writeInt(aInt);
   }

   public void writeU8 (long aLong) throws IOException
   {
      // System.out.println("L:" + Long.toHexString(aLong));
      writeLong(aLong);
   }
}