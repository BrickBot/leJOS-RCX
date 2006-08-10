package josx.rcxcomm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Utility class to support remote execution. Creates an RCXPort for
 * communication. public data stream variables in anfd out are used to send add
 * receive data.
 */
public class RCXRemote
{
   private RCXPort rcxPort;
   public DataInputStream in;
   public DataOutputStream out;

   public RCXRemote(String port) throws IOException{
	   System.out.println("Starting remoting");
	   start(port);
   }
   
   public void start(String port) throws IOException
   {
      rcxPort = new RCXPort(port);
      in = new DataInputStream(rcxPort.getInputStream());
      out = new DataOutputStream(rcxPort.getOutputStream());
   }

   public void stop ()
   {
	   rcxPort.close();
   }

   public void error ()
   {
      System.out.println("Error in remote execution");
   }
}

