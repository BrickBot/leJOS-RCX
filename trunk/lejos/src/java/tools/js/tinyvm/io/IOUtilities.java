package js.tinyvm.io;

import java.io.IOException;

public class IOUtilities
{
   public static void writePadding (ByteWriter aOut, int aMinRecSize)
      throws IOException
   {
      int pRegSize = aOut.size();
      int pPad = adjustedSize(pRegSize, aMinRecSize) - pRegSize;
      aOut.write(new byte[pPad], 0, pPad);
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

