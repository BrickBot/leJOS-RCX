package js.common;

/**
 * Abstract tool.
 */
public class AbstractTool
{
  private ToolProgressListener _progress;

  /**
   * Constructor.
   * 
   * @param listener tool progress listener
   */
  public AbstractTool(ToolProgressListener listener)
  {
    _progress = listener;
  }

  //
  // protected interface
  //
  
  /**
   * Progress listener.
   */
  protected ToolProgressListener getProgressListener ()
  {
    assert _progress != null : "Postconditon: result != null";
    return _progress;
  }
}
