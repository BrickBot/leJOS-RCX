package js.tinyvm.old.classfile;

import java.io.InputStream;
import java.io.OutputStream;

public interface IDumpable
{
   public void dump (OutputStream aOut) throws Exception;

   public void read (InputStream aIn) throws Exception;
}