package java.lang;

// EXPOSE WHEN native arraycopy IS AVAILABLE

class System
{
  //public static native void arraycopy (Object src, int srcoffset, Object dest, int destoffset, int length);

  static void arraycopy (char[] src, int srcoffset, char[] dest, int destoffset, int length)
  {
    for (int i = 0; i < length; i++)
      dest[srcoffset+i] = src[destoffset+i];
  }
}

