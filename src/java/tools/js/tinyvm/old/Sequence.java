package js.tinyvm.old;

import java.util.Enumeration;
import java.util.Vector;

public class Sequence extends RecordTable
{
   final Vector iVector = new Vector();

   public Sequence (boolean aAlign)
   {
      super(aAlign);
   }

   public Sequence ()
   {
      super();
   }

   public void add (WritableData aElement)
   {
      iVector.addElement(aElement);
   }

   public Enumeration elements ()
   {
      return iVector.elements();
   }

   public int size ()
   {
      return iVector.size();
   }

   public Object elementAt (int aIndex)
   {
      return iVector.elementAt(aIndex);
   }

   public int indexOf (Object aObj)
   {
      return iVector.indexOf(aObj);
   }
}

