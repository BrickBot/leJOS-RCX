package js.tinyvm.old;

import js.common.ToolException;

/**
 * Generic tiny vm exception.
 */
public class TinyVMException extends ToolException
{
   /**
    * @param message
    */
   public TinyVMException (String message)
   {
      super(message);
   }

   /**
    * @param cause
    */
   public TinyVMException (Throwable cause)
   {
      super(cause);
   }

   /**
    * @param message
    * @param cause
    */
   public TinyVMException (String message, Throwable cause)
   {
      super(message, cause);
   }

   /**
    * Comment for <code>serialVersionUID</code>
    */
   private static final long serialVersionUID = 3762256348515153713L;
}