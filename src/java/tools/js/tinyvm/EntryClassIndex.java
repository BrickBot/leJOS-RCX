package js.tinyvm;


public class EntryClassIndex implements WritableData, Constants
{
  String iClassName;
  Binary iBinary;
  
  public EntryClassIndex (Binary aBinary, String aClassName)
  {
    iBinary = aBinary;
    iClassName = aClassName;
  }

  public int getLength()
  {
    return 1;
  }

  public void dump (ByteWriter aOut) throws Exception
  {
    int pIndex = iBinary.getClassIndex (iClassName);
    Assertion.test (pIndex >= 0 && pIndex < 256);
    aOut.writeU1 (pIndex);
  }
}
  
