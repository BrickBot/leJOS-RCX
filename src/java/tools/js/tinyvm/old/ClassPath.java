package js.tinyvm.old;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ClassPath
{
   private Object[] iEntries;

   public ClassPath (String aEntries) throws TinyVMException
   {
      Vector pTokens = new Vector();
      StringTokenizer pTok = new StringTokenizer(aEntries, File.pathSeparator);
      while (pTok.hasMoreTokens())
      {
         pTokens.addElement(pTok.nextToken());
      }
      iEntries = new Object[pTokens.size()];
      for (int i = 0; i < iEntries.length; i++)
      {
         String pEntry = (String) pTokens.elementAt(i);
         if (pEntry.endsWith(".zip") || pEntry.endsWith(".jar"))
         {
            try
            {
               iEntries[i] = new ZipFile(pEntry);
            }
            catch (IOException e)
            {
               throw new TinyVMException("Can't open zip/jar file " + pEntry);
            }
         }
         else
         {
            iEntries[i] = new File(pEntry);
            if (!((File) iEntries[i]).isDirectory())
            {
               throw new TinyVMException(pEntry + " is not a directory");
            }
         }
      }
   }

   /**
    * @param aName Fully qualified class name with '/' characters.
    * @return <code>null</code> iff not found.
    */
   public InputStream getInputStream (String aName) throws TinyVMException
   {
      try
      {
         String pRelName = aName + ".class";
         for (int i = 0; i < iEntries.length; i++)
         {
            if (iEntries[i] instanceof File)
            {
               File pClassFile = new File((File) iEntries[i], pRelName);
               if (pClassFile.exists())
                  return new FileInputStream(pClassFile);
            }
            else if (iEntries[i] instanceof ZipFile)
            {
               ZipFile pZipFile = (ZipFile) iEntries[i];
               ZipEntry pEntry = pZipFile.getEntry(pRelName);
               if (pEntry != null)
                  return pZipFile.getInputStream(pEntry);
            }
         }
      }
      catch (FileNotFoundException e)
      {
         throw new TinyVMException(e.getMessage(), e);
      }
      catch (IOException e)
      {
         throw new TinyVMException(e.getMessage(), e);
      }

      return null;
   }

   public String toString ()
   {
      StringBuffer pBuf = new StringBuffer();
      for (int i = 0; i < iEntries.length; i++)
      {
         if (iEntries[i] instanceof ZipFile)
            pBuf.append(((ZipFile) iEntries[i]).getName());
         else
            pBuf.append(iEntries[i]);
         if (i < iEntries.length - 1)
            pBuf.append(File.pathSeparator);
      }
      return pBuf.toString();
   }
}