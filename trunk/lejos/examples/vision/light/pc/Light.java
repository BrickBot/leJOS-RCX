import josx.vision.*;

public class Light implements LightListener {
  private long lastPlay = 0;

  public static void main(String [] args) {
    (new Light()).run();
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
        RCX.spinRight(1);
      }
      else if (region == 3) {
        RCX.spinLeft(1);
      }
      else if (region == 2) {
        RCX.forward(2);
      }
    }   
  }
}

