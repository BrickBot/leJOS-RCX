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
   /**
    * Align output?.
    */
   private boolean _align;
   
   /**
    * Content.
    */
   private ArrayList _list;
   
   /**
    * Cache for length.
    */
   private int _length;

   /**
    * Constructor.
    * 
    * @param align align when dumping?
    */
   public RecordTable (boolean align)
   {
      _align = align;
      _list = new ArrayList();
      _length = -1;
   }

   /**
    * Iterator.
    */
   public Iterator iterator ()
   {
      return _list.iterator();
   }

   /**
    * Size of record table.
    */
   public int size ()
   {
      return _list.size();
   }

   /**
    * Add element
    * @param element element
    */
   public void add (WritableData element)
   {
      _list.add(element);
   }

   /**
    * Element at index.
    * 
    * @param index index
    */
   public WritableData get (int index)
   {
      return (WritableData) _list.get(index);
   }
   
   /**
    * Get index of element.
    * 
    * @param element element
    * @return index of element or -1 if not found
    */
   public int indexOf (WritableData element)
   {
      return _list.indexOf(element);
   }
   
   //
   // writable interface
   //

   /**
    * Dump.
    */
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

   /**
    * Length.
    */
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

   /**
    * Init offset.
    */
   public void initOffset (int aStart) throws TinyVMException
   {
      _logger.log(Level.INFO, "RT.initOffset: " + aStart);

      super.initOffset(aStart);

      for (Iterator iter = _list.iterator(); iter.hasNext();)
      {
         WritableData element = (WritableData) iter.next();
         if (element instanceof WritableDataWithOffset)
         {
            ((WritableDataWithOffset) element).initOffset(aStart);
         }
         aStart += element.getLength();
      }
   }

   private static final Logger _logger = Logger.getLogger("TinyVM");
}

