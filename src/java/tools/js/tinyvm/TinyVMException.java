package js.tinyvm;

/**
 * Generic tiny vm exception.
 */
public class TinyVMException extends Exception
{
  /**
   * @param message
   */
  public TinyVMException(String message)
  {
    super(message);
  }

  /**
   * @param cause
   */
  public TinyVMException(Throwable cause)
  {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public TinyVMException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
