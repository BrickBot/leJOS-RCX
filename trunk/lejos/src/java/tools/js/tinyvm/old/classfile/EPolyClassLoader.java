package js.tinyvm.old.classfile;

public class EPolyClassLoader extends ClassNotFoundException
{
   Exception iWrapped;

   public EPolyClassLoader (Exception aE)
   {
      iWrapped = aE;
   }

   public String toString ()
   {
      return iWrapped.toString();
   }
}

