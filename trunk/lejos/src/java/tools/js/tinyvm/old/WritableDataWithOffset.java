package js.tinyvm.old;

public abstract class WritableDataWithOffset implements WritableData
{
   int iOffset = -1;

   public int getOffset () throws TinyVMException
   {
      if (iOffset == -1)
      {
         throw new TinyVMException(
            "Bug WDWO-1: Premature getOffset call: Class="
               + getClass().getName());
      }
      return iOffset;
   }

   public void initOffset (int aStart) throws TinyVMException
   {
      assert aStart != -1: "Precondition: aStart != -1";
      iOffset = aStart;
   }
}