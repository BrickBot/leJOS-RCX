package js.tinyvm.old;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import js.tinyvm.io.IByteWriter;
import js.tinyvm.io.IOUtilities;

public abstract class RecordTable extends WritableDataWithOffset
   implements Constants
{
   int iLength = -1;
   private boolean iAlign;

   public abstract Enumeration elements ();

   public abstract int size ();

   public abstract Object elementAt (int aIndex);

   public abstract void add (WritableData aElement);

   public RecordTable ()
   {
      this(false);
   }

   public RecordTable (boolean aAlign)
   {
      super();
      iAlign = aAlign;
   }

   public void dump (IByteWriter aOut) throws TinyVMException
   {
      try
      {
         boolean pDoVerify = VERIFY_LEVEL > 0;
         Enumeration pEnum = elements();
         while (pEnum.hasMoreElements())
         {
            int pLength = 0;
            int pPrevSize = 0;
            WritableData pData = (WritableData) pEnum.nextElement();
            if (pDoVerify)
            {
               pLength = pData.getLength();
               pPrevSize = aOut.offset();
            }
            pData.dump(aOut);
            if (pDoVerify)
            {
               if (aOut.offset() != pPrevSize + pLength)
               {
                  if (pData instanceof RecordTable)
                  {
                     _logger.log(Level.SEVERE, "Aligned sequence: "
                        + ((RecordTable) pData).iAlign);
                  }
                  throw new TinyVMException("Bug RT-1: Written="
                     + (aOut.offset() - pPrevSize) + " Length=" + pLength
                     + " Class=" + pData.getClass().getName());
               }
            }
         }
         if (iAlign)
         {
            IOUtilities.writePadding(aOut, 2);
         }
      }
      catch (IOException e)
      {
         throw new TinyVMException(e.getMessage(), e);
      }
   }

   public int getLength () throws TinyVMException
   {
      if (iLength != -1)
         return iLength;
      iLength = 0;
      Enumeration pEnum = elements();
      while (pEnum.hasMoreElements())
      {
         iLength += ((WritableData) pEnum.nextElement()).getLength();
      }
      _logger.log(Level.INFO, "RT.getLength: " + iLength);
      if (iAlign)
         iLength = IOUtilities.adjustedSize(iLength, 2);
      return iLength;
   }

   public void initOffset (int aStart) throws TinyVMException
   {
      _logger.log(Level.INFO, "RT.initOffset: " + aStart);

      super.initOffset(aStart);
      Enumeration pEnum = elements();
      while (pEnum.hasMoreElements())
      {
         WritableData pElem = (WritableData) pEnum.nextElement();
         if (pElem instanceof WritableDataWithOffset)
         {
            ((WritableDataWithOffset) pElem).initOffset(aStart);
         }
         aStart += pElem.getLength();
      }
   }

   private static final Logger _logger = Logger.getLogger("TinyVM");
}

