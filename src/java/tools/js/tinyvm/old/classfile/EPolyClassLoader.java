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

   /**
    * Comment for <code>serialVersionUID</code>
    */
   private static final long serialVersionUID = 3257847684051251252L;
}

