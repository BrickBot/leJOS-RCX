package js.tools;

import js.common.ToolException;

/**
 * Lejosdl exception.
 */
public class LejosdlException extends ToolException
{
   /**
    * @param message
    */
   public LejosdlException (String message)
   {
      super(message);
   }

   /**
    * @param cause
    */
   public LejosdlException (Throwable cause)
   {
      super(cause);
   }

   /**
    * @param message
    * @param cause
    */
   public LejosdlException (String message, Throwable cause)
   {
      super(message, cause);
   }
}