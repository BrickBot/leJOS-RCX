package js.tinyvm.old;

import js.tinyvm.io.IByteWriter;

public interface WritableData
{
   public void dump (IByteWriter aOut) throws TinyVMException;

   /**
    * Returns the length of the record, in bytes.
    */
   public int getLength () throws TinyVMException;
}