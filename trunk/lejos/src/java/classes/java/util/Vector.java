package java.util;

/**
 * A dynamic array.
 */
public class Vector
{
  private Object[] iElements;
  private int iSize;
  
  public Vector()
  {
    iElements = new Object[7];
    iSize = 0;	  
  }
  
  public synchronized Object elementAt (int aIndex)
  {
    return iElements[aIndex];	  
  }

  public synchronized void addElement (Object aObj)
  {
    int pOldSize = iSize;
    setSize (pOldSize + 1);
    iElements[pOldSize] = aObj;
  }

  public synchronized void setSize (int aSize)
  {
    iSize = aSize;
    if (iElements.length < aSize)
    {
      int pNewCapacity = aSize * 2;
      Object[] pNewElements = new Object[pNewCapacity];
      // TBD: Replace this with arraycopy:
      for (int i = 0; i < iElements.length; i++)
          pNewElements[i] = iElements[i];
      iElements = pNewElements;
    }
  }
  
  public synchronized int size()
  {
    return iSize;
  }
  
  //private native void arraycopy (Object aSource, int aOffset1, Object aDest, int aOffset2, int aLength);
}	
