import josx.vision.*;

public class Alarm implements MotionListener, ColorListener {
  long lastPlay = 0;
  private final int blue = 0xa0b0c0;

  public static void main(String [] args) {
    (new Alarm()).run();
  }

  private void run() {
    Vision.setImageSize(320, 240);
    Vision.flipHorizontal(true);
    Vision.addRectRegion(1, 30, 100, 50, 100);
    Vision.addMotionListener(1, this);
    Vision.addRectRegion(2, 130, 100, 50, 100);
    Vision.addMotionListener(2, this);
    Vision.addRectRegion(3, 230, 100, 50, 100);
    Vision.addColorListener(3, this, blue);
    Vision.startViewer("Alarm");
  }

  public void motionDetected(int region) {
    if ((System.currentTimeMillis() - lastPlay) > 1000) {
      lastPlay = System.currentTimeMillis();
      Vision.playSound("../../Effects/Alarm.wav");
    }   
  }
  public void colorDetected(int region, int color) {
    if ((System.currentTimeMillis() - lastPlay) > 1000) {
      lastPlay = System.currentTimeMillis();
      Vision.playSound("../../Effects/Cavalry.wav");
      Vision.stopViewer();
      System.exit(0);
    }   
  }
}

