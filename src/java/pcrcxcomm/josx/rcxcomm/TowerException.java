package josx.rcxcomm;

/**
 * Exception of Tower.
 */
public class TowerException extends Exception
{
   /**
    * @param message
    */
   public TowerException (String message)
   {
      super("Tower error: " + message);
   }

   /**
    * @param status status code from tower
    */
   public TowerException (int status)
   {
      super("Tower error: " + errorMessage(status));
   }

   /**
    * Converts an status code to a human readable string.
    * 
    * @param status
    */
   public static String errorMessage (int status)
   {
      switch (Math.abs(status))
      {
         case 0:
            return "no error";
         case 1:
            return "tower not responding";
         case 2:
            return "bad ir link";
         case 3:
            return "bad ir echo";
         case 4:
            return "no response from rcx";
         case 5:
            return "bad response from rcx";
         case 6:
            return "write failure";
         case 7:
            return "read failure";
         case 8:
            return "open failure";
         case 9:
            return "internal error";
         case 10:
            return "already closed";
         case 11:
            return "already open";
         case 12:
            return "not open";
         default:
            return "unknown error (error code " + status + ")";
      }
   }

   /**
    * Comment for <code>serialVersionUID</code>
    */
   private static final long serialVersionUID = 3258410638383854137L;
}