package josx.vision;

import java.io.IOException;

import josx.rcxcomm.RCXRemote;
import josx.rcxcomm.RemoteVisionConstants;

/**
 * Remote execution of commands for vision control.
 * @author Lawrie Griffiths
 **/
public class RCX implements RemoteVisionConstants {

  /**
   * Move the robot forwards until stop or another command is executed.
   **/
  public static void forward() {
    synchronized(RCXRemote.out) {
      try {
        RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_FORWARD_V);
      } catch (IOException ioe) {RCXRemote.error();}
    }
  }

  /**
   * Move the robot forwards n units.
   * @param n the number of units to move
   **/
  public static void forward(int n) {
    if (n == 0) forward();
    else
      synchronized(RCXRemote.out) {
        try {
          RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_FORWARD_I);
          RCXRemote.out.writeByte(n);
        } catch (IOException ioe) {RCXRemote.error();}
      }
  }

  /**
   * Move the robot backwards until stop or another command is executed.
   **/
  public static void backward() {
    synchronized(RCXRemote.out) {
      try {
        RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_BACKWARD_V);
      } catch (IOException ioe) {RCXRemote.error();}
    }
  }

  /**
   * Move the robot backwards n units.
   * @param n the number of units to move
   **/
  public static void backward(int n) {
    if (n == 0) backward();
    else
      synchronized(RCXRemote.out) {
        try {
          RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_BACKWARD_I);
          RCXRemote.out.writeByte(n);
        } catch (IOException ioe) {RCXRemote.error();}
      }
  }

  /**
   * Spin left until stop or another command is executed.
   **/
  public static void spinLeft() {
    synchronized(RCXRemote.out) {
      try {
        RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_SPIN_LEFT_V);
      } catch (IOException ioe) {RCXRemote.error();}
    }
  }

  /**
   * Spin left n units.
   * @param n the number of units to move
   **/
  public static void spinLeft(int n) {
    if (n == 0) spinLeft();
    else
      synchronized(RCXRemote.out) {
        try {
          RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_SPIN_LEFT_I);
          RCXRemote.out.writeByte(n);
        } catch (IOException ioe) {RCXRemote.error();}
      }
  }

  /**
   * Turn left until stop or another command is executed.
   **/
  public static void turnLeft() {
    synchronized(RCXRemote.out) {
      try {
        RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_TURN_LEFT_V);
      } catch (IOException ioe) {RCXRemote.error();}
    }
  }

  /**
   * Turn left n units.
   * @param n the number of units to move
   **/
  public static void turnLeft(int n) {
    if (n == 0) spinLeft();
    else
      synchronized(RCXRemote.out) {
        try {
          RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_TURN_LEFT_I);
          RCXRemote.out.writeByte(n);
        } catch (IOException ioe) {RCXRemote.error();}
      }
  }

  /**
   * Scan left until stop or another command is executed.
   **/
  public static void scanLeft() {
    synchronized(RCXRemote.out) {
      try {
        RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_SCAN_LEFT_V);
      } catch (IOException ioe) {RCXRemote.error();}
    }
  }

  /**
   * Scan left n units.
   * @param n the number of units to move
   **/
  public static void scanLeft(int n) {
    if (n == 0) spinLeft();
    else
      synchronized(RCXRemote.out) {
        try {
          RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_SCAN_LEFT_I);
          RCXRemote.out.writeByte(n);
        } catch (IOException ioe) {RCXRemote.error();}
      }
  }

  /**
   * Spin right until stop or another command is executed.
   * @param n the number of units to move
   **/
  public static void spinRight() {
    synchronized(RCXRemote.out) {
      try {
        RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_SPIN_RIGHT_V);
      } catch (IOException ioe) {RCXRemote.error();}
    }
  }

  /**
   * Spin right n units.
   * @param n the number of units to move
   **/
  public static void spinRight(int n) {
    if (n == 0) spinRight();
    else
      synchronized(RCXRemote.out) {
        try {
          RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_SPIN_RIGHT_I);
          RCXRemote.out.writeByte(n);
        } catch (IOException ioe) {RCXRemote.error();}
      }
  }

  /**
   * Turn right until stop or another command is executed.
   **/
  public static void turnRight() {
    synchronized(RCXRemote.out) {
      try {
        RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_TURN_RIGHT_V);
      } catch (IOException ioe) {RCXRemote.error();}
    }
  }

  /**
   * Turn right n units.
   * @param n the number of units to move
   **/
  public static void turnRight(int n) {
    if (n == 0) spinRight();
    else
      synchronized(RCXRemote.out) {
        try {
          RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_TURN_RIGHT_I);
          RCXRemote.out.writeByte(n);
        } catch (IOException ioe) {RCXRemote.error();}
      }
  }

  /**
   * Scan right until stop or another command is executed.
   **/
  public static void scanRight() {
    synchronized(RCXRemote.out) {
      try {
        RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_SCAN_RIGHT_V);
      } catch (IOException ioe) {RCXRemote.error();}
    }
  }

  /**
   * Scan right n units.
   * @param n the number of units to move
   **/
  public static void scanRight(int n) {
    if (n == 0) spinRight();
    else
      synchronized(RCXRemote.out) {
        try {
          RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_SCAN_RIGHT_I);
          RCXRemote.out.writeByte(n);
        } catch (IOException ioe) {RCXRemote.error();}
      }
  }

  /**
   * Stop all motors
   **/
  public static void stop() {
    synchronized(RCXRemote.out) {
      try {
        RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_STOP);
      } catch (IOException ioe) {RCXRemote.error();}
    }
  }

  /**
   * Set the power of selected motors
   * @param motors the selected motors
   * @param the power value 0-7
   **/
  public static void setPower(byte motors, byte power) {
    synchronized(RCXRemote.out) {
      try {
        RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_SET_POWER);
        RCXRemote.out.writeByte(motors);
        RCXRemote.out.writeByte(power);
      } catch (IOException ioe) {RCXRemote.error();}
    }
  }

  /**
   * Move the selected motors in the selected direction for n units
   * @param motors the selected motors
   * @param direction the direction to move in 0 - foreards, 1 backwards
   * @param n the number of units to move
   **/
  public static void controlMotors(byte motors, byte direction, byte n) {
    synchronized(RCXRemote.out) {
      try {
        RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_CONTROL_MOTORS);
        RCXRemote.out.writeByte(motors);
        RCXRemote.out.writeByte(direction);
        RCXRemote.out.writeByte(n);
      } catch (IOException ioe) {RCXRemote.error();}
    }
  }

  /**
   * Play a tone
   * @param frequency the tone frequency
   * @param duration the duration of the tone
   **/
  public static void playTone(short frequency, byte duration) {
    synchronized(RCXRemote.out) {
      try {
        RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_PLAY_TONE);
        RCXRemote.out.writeShort(frequency);
        RCXRemote.out.writeByte(duration);
      } catch (IOException ioe) {RCXRemote.error();}
    }
  }

  /**
   * Tilt the camera up n units
   * @param n the number of units to move
   **/
  public static void tiltUp(int n) {
    synchronized(RCXRemote.out) {
      try {
        RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_TILT_UP_I);
        RCXRemote.out.writeByte(n);
      } catch (IOException ioe) {RCXRemote.error();}
    }
  }

  /**
   * Tilt the camera down n units
   * @param n the number of units to move
   **/
  public static void tiltDown(int n) {
    synchronized(RCXRemote.out) {
      try {
        RCXRemote.out.writeByte(METHOD_JOSX_VISION_RCX_TILT_DOWN_I);
        RCXRemote.out.writeByte(n);
      } catch (IOException ioe) {RCXRemote.error();}
    }
  }
}
