package js.tools;

/**
 * Simple implementation of ToolProgressListener woith output to System.out.
 */
public class ToolProgressListenerImpl implements ToolProgressListener
{
  /* (non-Javadoc)
   * @see js.tools.ToolProgressListener#operation(java.lang.String)
   */
  public void operation (String message)
  {
    assert message != null : "Precondition: message != null";

    System.out.println(message);
  }

  /* (non-Javadoc)
   * @see js.tools.ToolProgressListener#log(java.lang.String)
   */
  public void log (String message)
  {
    assert message != null : "Precondition: message != null";

    System.out.println(message);
  }

  /* (non-Javadoc)
   * @see js.tools.ToolProgressListener#progress(int)
   */
  public void progress (int progress)
  {
    assert progress >= 0 && progress <= 100 : "Precondition: progress >= 0 && progress <= 100";
    
    System.out.print("\r  " + progress + "%\r");
    if (progress >= 100)
    {
      System.out.println();
    }
  }
}
