package js.tinyvm;


public abstract class WritableDataWithOffset implements WritableData
{
  int iOffset = -1;

  public int getOffset()
  {
    if (iOffset == -1)
    {
      new Error().printStackTrace();
      System.out.println ("--------------------------");
      Assertion.fatal ("Bug WDWO-1: Premature getOffset call: Class=" + 
                       getClass().getName());
    }
    return iOffset;
  }

  public void initOffset (int aStart)
  {
    Assertion.test (aStart != -1);
    iOffset = aStart;
  }
}
