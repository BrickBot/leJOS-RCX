package js.tinyvm.io;

import java.io.IOException;

public class IOUtilities
{
   public static void writePadding (IByteWriter aOut, int aMinRecSize)
      throws IOException
   {
      int pRegSize = aOut.offset();
      int pPad = adjustedSize(pRegSize, aMinRecSize) - pRegSize;
      aOut.write(new byte[pPad]);
   }

   public static int adjustedSize (int aSize, int aMinRecSize)
   {
      int pMod = aSize % aMinRecSize;
      if (pMod != 0)
      {
         return aSize + aMinRecSize - pMod;
      }
      return aSize;
   }
}

