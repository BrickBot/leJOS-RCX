import josx.vision.*;

public class Motion implements MotionListener {
  private static int image = 1;
  long lastPlay = 0;

  public static void main(String [] args) {
    (new Motion()).run();
  }

  private void run() {
    Vision.setImageSize(320, 240);
    Vision.flipHorizontal(true);
    Vision.addRectRegion(1, 0, 0, 320, 240);
    Vision.addMotionListener(1, this);
    Vision.startViewer("Test Vision");
  }

  public void motionDetected(int region) {
    if ((System.currentTimeMillis() - lastPlay) > 1000) {
      lastPlay = System.currentTimeMillis();
      Vision.snapshot("Intruder" + image++ + ".jpg");
      Vision.playSound("../../Effects/Alarm.wav");
    }   
  }
}

