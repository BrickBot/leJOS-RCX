package java.lang;

public class String
{
  private char[] characters;

  public String (char[] c, int off, int len)
  {
    characters = new char[len];
    System.arraycopy (c, off, characters, 0, len);
  }

  public char[] toCharArray()
  {
    int len = characters.length;
    char[] ca = new char[len];
    System.arraycopy (characters, 0, ca, 0, len);
    return ca;
  }

  public static String valueOf (Object aObj)
  {
    return aObj.toString();
  }

  public String toString()
  {
    return this;
  }
}

