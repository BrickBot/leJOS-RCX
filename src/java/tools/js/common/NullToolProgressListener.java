package js.common;

/**
 * Dummy progress listener.
 */
public class NullToolProgressListener implements ToolProgressListener
{
  /* (non-Javadoc)
   * @see js.common.ToolProgressListener#operation(java.lang.String)
   */
  public void operation (String message)
  {
  }

  /* (non-Javadoc)
   * @see js.common.ToolProgressListener#log(java.lang.String)
   */
  public void log (String message)
  {
  }

  /* (non-Javadoc)
   * @see js.common.ToolProgressListener#progress(int)
   */
  public void progress (int progress)
  {
  }
}
