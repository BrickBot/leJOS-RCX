package josx.vision;

import java.io.IOException;

import josx.rcxcomm.RCXRemote;
import josx.rcxcomm.RemoteVisionConstants;

/**
 * Remote execution of commands for vision control.
 * 
 * @author Lawrie Griffiths
 */
public class RCX implements RemoteVisionConstants {

	private RCXRemote remote;
	
	public RCX(String port) throws IOException {
		remote = new RCXRemote(port);
	}
	
   /**
    * Move the robot forwards until stop or another command is executed.
    */
   public void forward ()
   {
      synchronized (remote.out)
      {
         try
         {
            remote.out.writeByte(METHOD_JOSX_VISION_RCX_FORWARD_V);
         }
         catch (IOException ioe)
         {
            remote.error();
         }
      }
   }

   /**
    * Move the robot forwards n units.
    * 
    * @param n the number of units to move
    */
   public void forward (int n)
   {
      if (n == 0)
         forward();
      else
         synchronized (remote.out)
         {
            try
            {
               remote.out.writeByte(METHOD_JOSX_VISION_RCX_FORWARD_I);
               remote.out.writeByte(n);
            }
            catch (IOException ioe)
            {
               remote.error();
            }
         }
   }

   /**
    * Move the robot backwards until stop or another command is executed.
    */
   public void backward ()
   {
      synchronized (remote.out)
      {
         try
         {
            remote.out.writeByte(METHOD_JOSX_VISION_RCX_BACKWARD_V);
         }
         catch (IOException ioe)
         {
            remote.error();
         }
      }
   }

   /**
    * Move the robot backwards n units.
    * 
    * @param n the number of units to move
    */
   public void backward (int n)
   {
      if (n == 0)
         backward();
      else
         synchronized (remote.out)
         {
            try
            {
               remote.out.writeByte(METHOD_JOSX_VISION_RCX_BACKWARD_I);
               remote.out.writeByte(n);
            }
            catch (IOException ioe)
            {
               remote.error();
            }
         }
   }

   /**
    * Spin left until stop or another command is executed.
    */
   public void spinLeft ()
   {
      synchronized (remote.out)
      {
         try
         {
            remote.out.writeByte(METHOD_JOSX_VISION_RCX_SPIN_LEFT_V);
         }
         catch (IOException ioe)
         {
            remote.error();
         }
      }
   }

   /**
    * Spin left n units.
    * 
    * @param n the number of units to move
    */
   public void spinLeft (int n)
   {
      if (n == 0)
         spinLeft();
      else
         synchronized (remote.out)
         {
            try
            {
               remote.out.writeByte(METHOD_JOSX_VISION_RCX_SPIN_LEFT_I);
               remote.out.writeByte(n);
            }
            catch (IOException ioe)
            {
               remote.error();
            }
         }
   }

   /**
    * Turn left until stop or another command is executed.
    */
   public void turnLeft ()
   {
      synchronized (remote.out)
      {
         try
         {
            remote.out.writeByte(METHOD_JOSX_VISION_RCX_TURN_LEFT_V);
         }
         catch (IOException ioe)
         {
            remote.error();
         }
      }
   }

   /**
    * Turn left n units.
    * 
    * @param n the number of units to move
    */
   public void turnLeft (int n)
   {
      if (n == 0)
         spinLeft();
      else
         synchronized (remote.out)
         {
            try
            {
               remote.out.writeByte(METHOD_JOSX_VISION_RCX_TURN_LEFT_I);
               remote.out.writeByte(n);
            }
            catch (IOException ioe)
            {
               remote.error();
            }
         }
   }

   /**
    * Scan left until stop or another command is executed.
    */
   public void scanLeft ()
   {
      synchronized (remote.out)
      {
         try
         {
            remote.out.writeByte(METHOD_JOSX_VISION_RCX_SCAN_LEFT_V);
         }
         catch (IOException ioe)
         {
            remote.error();
         }
      }
   }

   /**
    * Scan left n units.
    * 
    * @param n the number of units to move
    */
   public void scanLeft (int n)
   {
      if (n == 0)
         spinLeft();
      else
         synchronized (remote.out)
         {
            try
            {
               remote.out.writeByte(METHOD_JOSX_VISION_RCX_SCAN_LEFT_I);
               remote.out.writeByte(n);
            }
            catch (IOException ioe)
            {
               remote.error();
            }
         }
   }

   /**
    * Spin right until stop or another command is executed.
    * 
    */
   public void spinRight ()
   {
      synchronized (remote.out)
      {
         try
         {
            remote.out.writeByte(METHOD_JOSX_VISION_RCX_SPIN_RIGHT_V);
         }
         catch (IOException ioe)
         {
            remote.error();
         }
      }
   }

   /**
    * Spin right n units.
    * 
    * @param n the number of units to move
    */
   public void spinRight (int n)
   {
      if (n == 0)
         spinRight();
      else
         synchronized (remote.out)
         {
            try
            {
               remote.out.writeByte(METHOD_JOSX_VISION_RCX_SPIN_RIGHT_I);
               remote.out.writeByte(n);
            }
            catch (IOException ioe)
            {
               remote.error();
            }
         }
   }

   /**
    * Turn right until stop or another command is executed.
    */
   public void turnRight ()
   {
      synchronized (remote.out)
      {
         try
         {
            remote.out.writeByte(METHOD_JOSX_VISION_RCX_TURN_RIGHT_V);
         }
         catch (IOException ioe)
         {
            remote.error();
         }
      }
   }

   /**
    * Turn right n units.
    * 
    * @param n the number of units to move
    */
   public void turnRight (int n)
   {
      if (n == 0)
         spinRight();
      else
         synchronized (remote.out)
         {
            try
            {
               remote.out.writeByte(METHOD_JOSX_VISION_RCX_TURN_RIGHT_I);
               remote.out.writeByte(n);
            }
            catch (IOException ioe)
            {
               remote.error();
            }
         }
   }

   /**
    * Scan right until stop or another command is executed.
    */
   public void scanRight ()
   {
      synchronized (remote.out)
      {
         try
         {
            remote.out.writeByte(METHOD_JOSX_VISION_RCX_SCAN_RIGHT_V);
         }
         catch (IOException ioe)
         {
            remote.error();
         }
      }
   }

   /**
    * Scan right n units.
    * 
    * @param n the number of units to move
    */
   public void scanRight (int n)
   {
      if (n == 0)
         spinRight();
      else
         synchronized (remote.out)
         {
            try
            {
               remote.out.writeByte(METHOD_JOSX_VISION_RCX_SCAN_RIGHT_I);
               remote.out.writeByte(n);
            }
            catch (IOException ioe)
            {
               remote.error();
            }
         }
   }

   /**
    * Stop all motors
    */
   public void stop ()
   {
      synchronized (remote.out)
      {
         try
         {
            remote.out.writeByte(METHOD_JOSX_VISION_RCX_STOP);
         }
         catch (IOException ioe)
         {
            remote.error();
         }
      }
   }

   /**
    * Set the power of selected motors
    * 
    * @param motors the selected motors
    * @param power value 0-7
    */
   public void setPower (byte motors, byte power)
   {
      synchronized (remote.out)
      {
         try
         {
            remote.out.writeByte(METHOD_JOSX_VISION_RCX_SET_POWER);
            remote.out.writeByte(motors);
            remote.out.writeByte(power);
         }
         catch (IOException ioe)
         {
            remote.error();
         }
      }
   }

   /**
    * Move the selected motors in the selected direction for n units
    * 
    * @param motors the selected motors
    * @param direction the direction to move in 0 - foreards, 1 backwards
    * @param n the number of units to move
    */
   public void controlMotors (byte motors, byte direction, byte n)
   {
      synchronized (remote.out)
      {
         try
         {
            remote.out.writeByte(METHOD_JOSX_VISION_RCX_CONTROL_MOTORS);
            remote.out.writeByte(motors);
            remote.out.writeByte(direction);
            remote.out.writeByte(n);
         }
         catch (IOException ioe)
         {
            remote.error();
         }
      }
   }

   /**
    * Play a tone
    * 
    * @param frequency the tone frequency
    * @param duration the duration of the tone
    */
   public void playTone (short frequency, byte duration)
   {
      synchronized (remote.out)
      {
         try
         {
            remote.out.writeByte(METHOD_JOSX_VISION_RCX_PLAY_TONE);
            remote.out.writeShort(frequency);
            remote.out.writeByte(duration);
         }
         catch (IOException ioe)
         {
            remote.error();
         }
      }
   }

   /**
    * Tilt the camera up n units
    * 
    * @param n the number of units to move
    */
   public void tiltUp (int n)
   {
      synchronized (remote.out)
      {
         try
         {
            remote.out.writeByte(METHOD_JOSX_VISION_RCX_TILT_UP_I);
            remote.out.writeByte(n);
         }
         catch (IOException ioe)
         {
            remote.error();
         }
      }
   }

   /**
    * Tilt the camera down n units
    * 
    * @param n the number of units to move
    */
   public void tiltDown (int n)
   {
      synchronized (remote.out)
      {
         try
         {
            remote.out.writeByte(METHOD_JOSX_VISION_RCX_TILT_DOWN_I);
            remote.out.writeByte(n);
         }
         catch (IOException ioe)
         {
            remote.error();
         }
      }
   }
}