package java.lang;

/**
 * Utilities for non-standard String maniplulation.
 * Use of these utilities will make your programs non-standard.
 */
public final class StringUtils
{
  /**
   * Return the character array correesponding to the String without copying it.
   * This method, unlike the toCharArray method of String, uses no memory.
   * This method should be used with care as it is non-standard and allows
   * the contents of a String to me overwritten.
   * Use in user programs will make the programs non-standard and non-portable.
   * @param s the String whose characters are required
   * @return the character array (uncopied)
   */
  public static char [] getCharacters(String s) {
    return s.characters;
  }
}

