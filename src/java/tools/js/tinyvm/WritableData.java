package js.tinyvm;

import js.tinyvm.io.ByteWriter;

public interface WritableData
{
  public void dump (ByteWriter aOut) throws TinyVMException;

  /**
   * Returns the length of the record, in bytes.
   */
  public int getLength () throws TinyVMException;
}