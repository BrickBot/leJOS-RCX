package js.common;

/**
 * Simple implementation of ToolProgressMonitor woith output to System.out.
 */
public class CLIToolProgressMonitor implements ToolProgressMonitor
{
   private boolean _verbose = false;

   /*
    * (non-Javadoc)
    * 
    * @see js.tools.ToolProgressMonitor#operation(java.lang.String)
    */
   public void operation (String message)
   {
      assert message != null: "Precondition: message != null";

      System.out.println(message);
   }

   /*
    * (non-Javadoc)
    * 
    * @see js.tools.ToolProgressMonitor#log(java.lang.String)
    */
   public void log (String message)
   {
      assert message != null: "Precondition: message != null";

      System.out.println(message);
   }

   /*
    * (non-Javadoc)
    * 
    * @see js.tools.ToolProgressMonitor#progress(int)
    */
   public void progress (int progress)
   {
      assert progress >= 0 && progress <= 100: "Precondition: progress >= 0 && progress <= 100";

      System.out.print("\r  " + progress + "%\r");
      if (progress >= 100)
      {
         System.out.println();
      }
   }

   /*
    * (non-Javadoc)
    * 
    * @see js.common.ToolProgressMonitor#isCanceled()
    */
   public boolean isCanceled ()
   {
      return Thread.currentThread().isInterrupted();
   }

   /**
    * Be verbose?
    */
   public void setVerbose (boolean verbose)
   {
      _verbose = verbose;
   }
}