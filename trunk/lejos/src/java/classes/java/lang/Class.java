package java.lang;

public class Class
{
  /**
   * Throws NoSuchMethodError.
   */
  public static Class forName (String aName)
  throws ClassNotFoundException
  {
    throw new NoSuchMethodError();
  }
}
