package js.tinyvm.old;

import java.io.IOException;

import js.tinyvm.io.IByteWriter;
import js.tinyvm.io.IOUtilities;
import js.tinyvm.old.classfile.JCPE_Class;
import js.tinyvm.old.classfile.JClassFile;
import js.tinyvm.old.classfile.JExcep;

public class ExceptionRecord implements WritableData, Constants
{
   JExcep iExcep;
   int iClassIndex;

   public ExceptionRecord (JExcep aExcep, Binary aBinary, JClassFile aCF)
      throws Exception
   {
      iExcep = aExcep;
      int pCPIndex = aExcep.getClassIndex();
      if (pCPIndex == 0)
      {
         // An index of 0 means ANY.
         iClassIndex = aBinary.getClassIndex("java/lang/Throwable");
      }
      else
      {
         JCPE_Class pCls = (JCPE_Class) aCF.getConstantPool()
            .getEntry(pCPIndex);
         String pName = pCls.getName();
         iClassIndex = aBinary.getClassIndex(pName);
      }
      if (iClassIndex == -1)
      {
         throw new TinyVMException("Exception not found: " + iExcep);
      }
   }

   public int getLength ()
   {
      return IOUtilities.adjustedSize(2 + // start
         2 + // end
         2 + // handler
         1, // class index
         2);
   }

   public void dump (IByteWriter aOut) throws TinyVMException
   {
      int pStart = iExcep.getStartPc();
      int pEnd = iExcep.getEndPc();
      int pHandler = iExcep.getHandlerPc();
      if (pStart > MAX_CODE || pEnd > MAX_CODE || pHandler > MAX_CODE)
      {
         throw new TinyVMException("Exception handler with huge PCs");
      }

      try
      {
         aOut.writeU2(pStart);
         aOut.writeU2(pEnd);
         aOut.writeU2(pHandler);
         aOut.writeU1(iClassIndex);
         IOUtilities.writePadding(aOut, 2);
      }
      catch (IOException e)
      {
         throw new TinyVMException(e.getMessage(), e);
      }
   }

   public boolean equals (Object aOther)
   {
      if (!(aOther instanceof ExceptionRecord))
         return false;
      return ((ExceptionRecord) aOther).iExcep.equals(iExcep);
   }

   public int hashCode ()
   {
      return iExcep.hashCode();
   }
}

