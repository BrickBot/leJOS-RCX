package josx.vision;

/**
 * Representation of a region in the camera's field of view
 */
public class Region {

  public static int MAX_REGIONS = 32;

  private int x, y;
  private int w, h;

  private static final int MAX_MOTION_LISTENERS = 32;
  private static final int MAX_COLOR_LISTENERS = 32;
  private static final int MAX_LIGHT_LISTENERS = 32;

  private MotionListener [] motionListeners= new MotionListener[MAX_MOTION_LISTENERS];
  private int numMotionListeners = 0;

  private ColorListener [] colorListeners= new ColorListener[MAX_COLOR_LISTENERS];
  private int numColorListeners = 0;
  private int [] colors = new int[MAX_COLOR_LISTENERS];

  private LightListener [] lightListeners= new LightListener[MAX_LIGHT_LISTENERS];
  private int numLightListeners = 0;

  /**
   * Create a region
   *
   * @param x the x coordinate of the botton left corner
   * @param y the y coordinate of the botton left corner
   * @param w the width of the region
   * @param h the height of the region
   */
  public Region(int x, int y, int w, int h) {
    this.x = x;
    this.y = y;
    this.w = w;
    this.h = h;
  }

  /**
   * Get the X coordinate of the bottom left corner
   */
  public int getX() {
    return x;
  }

  /**
   * Get the Y coordinate of the bottom left corner
   */
  public int getY() {
    return y;
  }

  /**
   * Get the width of the region
   */
  public int getWidth() {
    return w;
  }

  /**
   * Get the height of the region
   */
  public int getHeight() {
    return h;
  }

  /**
   * Test if point is in the region
   * @param tx test x coordinate
   * @param ty test y coordinate
   */
  public boolean inRegion(int tx, int ty) {
    return (tx >=x && ty >= y && tx <=x+w && ty <= y+h);
  }

  /**
   * Add a motion listener
   */
  public void addMotionListener(MotionListener ml) {
    motionListeners[numMotionListeners++] = ml;
  }

  /**
   * Add a color listener
   */
  public void addColorListener(ColorListener cl, int color) {
    colors[numColorListeners] = color;
    colorListeners[numColorListeners++] = cl;
  }

  /**
   * Add a light listener
   */
  public void addLightListener(LightListener ll) {
    lightListeners[numLightListeners++] = ll;
  }

  /**
   * Return the array of motion listeners 
   */
  public MotionListener [] getMotionListeners() {
    MotionListener [] ml = new MotionListener[numMotionListeners];
    for(int i=0;i<numMotionListeners;i++) ml[i] = motionListeners[i];
    return ml;
  } 

  /**
   * Return the array of color listeners 
   */
  public ColorListener [] getColorListeners() {
    ColorListener [] cl = new ColorListener[numColorListeners];
    for(int i=0;i<numColorListeners;i++) cl[i] = colorListeners[i];
    return cl;
  } 

  /**
   * Return the array of colors corresponding to the color listeners 
   */
  public int [] getColors() {
    return colors;
  }

  /**
   * Return the array of light listeners 
   */
  public LightListener [] getLightListeners() {
    LightListener [] ll = new LightListener[numLightListeners];
    for(int i=0;i<numLightListeners;i++) ll[i] = lightListeners[i];
    return ll;
  } 
}

