import josx.vision.*;

public class Music implements MotionListener  {
  long lastPlay = 0;
  static String dir;
    
  public static void main(String [] args) {
    if (args.length < 1) {
      System.out.println("Usage: java Music <instrument>");
      System.exit(1);
    }
    dir = args[0];
    (new Music()).run();
  }

  private void run() {
    Vision.setImageSize(320, 240);
    Vision.flipHorizontal(true);
    for(int i=0;i<16;i++) {
      Vision.addRectRegion(i+1, i * 20, 0, 20, 160);
      Vision.addMotionListener(i+1, this);
    }
    Vision.addRectRegion(17,0,160,320,80);
    Vision.addMotionListener(17,this);
    Vision.startViewer("Music");
  }

  public void motionDetected(int region) {
    if ((System.currentTimeMillis() - lastPlay) > 1000) {
      lastPlay = System.currentTimeMillis();
      if (region == 17) Vision.playSound("../../Effects/Cymbal1.wav");
      else Vision.playSound("../../" + dir + "/E" + (region < 10 ? "0" + region : "" + region) + ".wav");
    }   
  }
}

