package js.tools;

/**
 * Firmdl exception.
 */
public class FirmdlException extends Exception
{
  /**
   * @param message
   */
  public FirmdlException(String message)
  {
    super(message);
  }

  /**
   * @param cause
   */
  public FirmdlException(Throwable cause)
  {
    super(cause);
  }

  /**
   * @param message
   * @param cause
   */
  public FirmdlException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
