package js.common;

/**
 * Generic tool exception.
 */
public class ToolException extends Exception
{
  /**
   * @param message
   */
  public ToolException(String message)
  {
    super(message);
  }

  /**
   * @param cause
   */
  public ToolException(Throwable cause)
  {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public ToolException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
