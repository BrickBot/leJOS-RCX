package js.tinyvm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import js.tinyvm.io.ByteWriter;
import js.tinyvm.io.IOUtilities;

public class RecordTable extends WritableDataWithOffset
{
   private boolean _align;
   private boolean _duplicates;
   private ArrayList _list;
   int _length;

   /**
    * Constructor.
    * 
    * @param allowDuplicates allow duplicates?
    * @param align align output?
    */
   public RecordTable (boolean allowDuplicates, boolean align)
   {
      _duplicates = allowDuplicates;
      _align = align;
      _list = new ArrayList();
      _length = -1;
   }

   //
   // public interface
   //
   
   public Iterator iterator ()
   {
      return _list.iterator();
   }

   public int size ()
   {
      return _list.size();
   }

   public boolean contains (WritableData element)
   {
      assert element != null: "Precondition: element != null";
      
      return _list.contains(element);
   }

   public WritableData get (int index)
   {
      return (WritableData) _list.get(index);
   }

   public int indexOf (WritableData element)
   {
      assert element != null: "Precondition: element != null";
      
      return _list.indexOf(element);
   }

   public void add (WritableData element)
   {
      assert element != null: "Precondition: element != null";
      
      if (_duplicates || !_list.contains(element))
      {
         _list.add(element);
      }
   }

   //
   // Writable interface
   //

   public void dump (ByteWriter writer) throws TinyVMException
   {
      try
      {
         boolean pDoVerify = TinyVMConstants.VERIFY_LEVEL > 0;
         for (Iterator iter = _list.iterator(); iter.hasNext();)
         {
            WritableData pData = (WritableData) iter.next();

            int pLength = pData.getLength();
            int pPrevSize = writer.size();

            pData.dump(writer);

            if (pDoVerify)
            {
               if (writer.size() != pPrevSize + pLength)
               {
                  if (pData instanceof RecordTable)
                  {
                     _logger.log(Level.SEVERE, "Aligned sequence: "
                        + ((RecordTable) pData)._align);
                  }
                  throw new TinyVMException("Bug RT-1: Written="
                     + (writer.size() - pPrevSize) + " Length=" + pLength
                     + " Class=" + pData.getClass().getName());
               }
            }
         }
         if (_align)
         {
            IOUtilities.writePadding(writer, 2);
         }
      }
      catch (IOException e)
      {
         throw new TinyVMException(e.getMessage(), e);
      }
   }

   public int getLength () throws TinyVMException
   {
      if (_length != -1)
      {
         return _length;
      }

      _length = 0;
      for (Iterator iter = _list.iterator(); iter.hasNext();)
      {
         _length += ((WritableData) iter.next()).getLength();
      }

      _logger.log(Level.INFO, "RT.getLength: " + _length);

      if (_align)
      {
         _length = IOUtilities.adjustedSize(_length, 2);
      }

      return _length;
   }

   public void initOffset (int start) throws TinyVMException
   {
      _logger.log(Level.INFO, "RT.initOffset: " + start);

      super.initOffset(start);

      for (Iterator iter = _list.iterator(); iter.hasNext();)
      {
         WritableData element = (WritableData) iter.next();
         if (element instanceof WritableDataWithOffset)
         {
            ((WritableDataWithOffset) element).initOffset(start);
         }
         start += element.getLength();
      }
   }

   private static final Logger _logger = Logger.getLogger("TinyVM");
}

