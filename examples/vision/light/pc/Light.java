import josx.vision.*;
import java.io.*;

public class Light implements LightListener {
  private long lastPlay = 0;
  private RCX _rcx;
  
  public Light(String port) throws IOException {
	  _rcx = new RCX(port);
  }
  
  public static void main(String [] args) {
	  try {
	  	if(args.length!=1)
			throw new Exception("first argument must be tower port (USB,COM1 etc)");
		(new Light(args[0])).run();
	  } catch(Exception e) {
		  e.printStackTrace();
	  }
  }

  private void run() {
    Vision.setImageSize(320, 240);
    for(int i=0;i<3;i++) {
      Vision.addRectRegion(i+1, 106 * i, 0, 106, 240);
      Vision.addLightListener(i+1, this);
    }
    Vision.flipHorizontal(true);
    Vision.startViewer("Light Follower");
  }

  // Move towards the light, barking 

  public void lightDetected(int region) {
    if ((System.currentTimeMillis() - lastPlay) > 1000) {
      lastPlay = System.currentTimeMillis();
      Vision.playSound("../../../Effects/Dogbark.wav");
      if (region == 1) {
        _rcx.spinRight(1);
      }
      else if (region == 3) {
        _rcx.spinLeft(1);
      }
      else if (region == 2) {
        _rcx.forward(2);
      }
    }   
  }
}

