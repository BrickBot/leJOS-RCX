package josx.vision;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import javax.media.*;
import javax.media.control.TrackControl;
import javax.media.format.*;
import javax.media.protocol.*;
import javax.media.datasink.*;
import javax.media.control.*;
import javax.sound.sampled.*;
import com.sun.image.codec.jpeg.*;
import java.awt.image.*;

/**
 * Java version of Vision Command.
 */
public class Vision extends Frame implements ControllerListener {

  // package protected fields

  static int imageWidth, imageHeight;
  static String snapshotFilename;
  static Processor p;
  static DataSource cds;
  static String cameraDevice, soundDevice;
  static boolean isRecording = false;
  static Recorder recorder;
  static boolean captureColor = false;
  static Vision visionFrame;

  // private instance variables

  private Object waitSync = new Object();
  private boolean stateTransitionOK = true;
  private static Properties videoProperties;
  private final static String DEFAULT_VIDEO_DEV_NAME =
    "vfw:Logitech USB Video Camera:0";
  private final static String DEFAULT_SOUND_DEV_NAME =
    "DirectSoundCapture";
  private static Region [] regions = new Region[Region.MAX_REGIONS];
  private static boolean takeSnapshot = false;

  /**
   * Create the viewer frame with a title.
   */
  public Vision(String title) {
    super(title);
  }

  /**
   * Given a datasource, create a processor and use that processor
   * as a player to playback the media.
   *
   * During the processor's Configured state, the FlipEffect, MotionDetectionEffect
   * ColorEffect and RegionEffect are inserted into the video track.
   *
   */
  public boolean open(MediaLocator ml) {
    DataSource tds = null;

    try{
      tds = Manager.createDataSource(ml);
    } catch (Exception e) {
      System.err.println("Failed to create a datasource");
      return false;
    }

    cds = Manager.createCloneableDataSource(tds);

    try {
      p = Manager.createProcessor(cds);
    } catch (Exception e) {
      System.err.println("Failed to create a processor from the given datasource: " + e);
      return false;
    }

    p.addControllerListener(this);

    // Put the Processor into configured state.
    p.configure();
    if (!waitForState(p.Configured)) {
      System.err.println("Failed to configure the processor.");
      return false;
    }

    // So I can use it as a player.
    p.setContentDescriptor(null);

    // Obtain the track controls.
    TrackControl tc[] = p.getTrackControls();
    if (tc == null) {
      System.err.println("Failed to obtain track controls from the processor.");
      return false;
    }

    // Search for the track control for the video track.
    TrackControl videoTrack = null;

    for (int i = 0; i < tc.length; i++) {
      if (tc[i].getFormat() instanceof VideoFormat) {
	videoTrack = tc[i];
	break;
      }
    }

    if (videoTrack == null) {
      System.err.println("The input media does not contain a video track.");
      return false;
    }


    // Instantiate and set the frame access codec to the data flow path.

    try {
      Codec codec[] = {  new FlipEffect(),
                         new MotionDetectionEffect(), 
                         new RegionEffect(), 
                         new ColorEffect()};
      videoTrack.setCodecChain(codec);
    } catch (UnsupportedPlugInException e) {
      System.err.println("The processor does not support effects.");
    }

    // Realize the processor.
	
    p.prefetch();
    if (!waitForState(p.Prefetched)) {
      System.err.println("Failed to realize the processor.");
      return false;
    }

    // Layout the components

    setLayout(new BorderLayout());

    // Display the visual & control component if there's one.

    Component cc, vc;

    if ((vc = p.getVisualComponent()) != null) {
      add("Center", vc);
    }

    if ((cc = p.getControlPanelComponent()) != null) {
      add("South", cc);
    }

    // Start the processor.
    p.start();

    // Show the frame
    setVisible(true);

    // Detect the window close event
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent we) {
	p.close();
	System.exit(0);
      }
    });
 
    // Recorder recorder = new Recorder("c:/vision/start", 5000);
    // recorder.start();

    // Start the processor  
    p.start();

    return true;
  }

  /**
   * Close Video viewer
   */
  public static void stopViewer() {
    visionFrame.hide();
    p.close();
  }
    
  public void addNotify() {
    super.addNotify();
    pack();
  }

  /**
   * Block until the processor has transitioned to the given state.
   * Return false if the transition failed.
   */
  boolean waitForState(int state) {
    synchronized (waitSync) {
      try {
	while (p.getState() != state && stateTransitionOK)
          waitSync.wait();
      } catch (Exception e) {}
    }
    return stateTransitionOK;
  }

  /**
   * Controller Listener.
   */
  public void controllerUpdate(ControllerEvent evt) {

    // System.out.println(this.getClass().getName()+evt);

    if (evt instanceof ConfigureCompleteEvent ||
        evt instanceof RealizeCompleteEvent ||
        evt instanceof PrefetchCompleteEvent) {
      synchronized (waitSync) {
	stateTransitionOK = true;
	waitSync.notifyAll();
      }
    } else if (evt instanceof ResourceUnavailableEvent) {
      synchronized (waitSync) {
	stateTransitionOK = false;
	waitSync.notifyAll();
      }
    } else if (evt instanceof EndOfMediaEvent) {
      p.close();
      System.exit(0);
    }
  }

  /**
   * Start the video viewer frame
   */
  public static void startViewer(String title) {

    /*  We use a properties file to allow the user to define what kind of 
     *  camera and resolution they want to use for the vision input
     */
    String videoPropFile =
      System.getProperty("video.properties", "video.properties");

    try {
      FileInputStream fis = new FileInputStream(new File(videoPropFile));
      videoProperties = new Properties();
      videoProperties.load(fis);
    } catch (IOException ioe) {
      System.out.println("Failed to read property file");
      System.exit(1);
    }

    cameraDevice =
      videoProperties.getProperty("video-device-name", DEFAULT_VIDEO_DEV_NAME);

    soundDevice =
      videoProperties.getProperty("sound-device-name", DEFAULT_SOUND_DEV_NAME);

    // System.out.println("Searching for [" + cameraDevice + "]");

    /*  Try to get the CaptureDevice that matches the name supplied by the
     *  user
     */
    CaptureDeviceInfo device = CaptureDeviceManager.getDevice(cameraDevice);

    if (device == null) {
      System.out.println("No device found [ " + cameraDevice + "]");
      System.exit(1);
    }

    // Create a media locator from the device
    MediaLocator ml = device.getLocator();

    // Create the frame
    visionFrame = new Vision(title);

    // Start the video viewer
    if (!visionFrame.open(ml)) System.exit(1);
  }

  /**
   * Play an audio file
   */
  public static void playSound(String fileName) {

   // Create an Audio Stream from the file 
   try {
      AudioInputStream stream = AudioSystem.getAudioInputStream(new File(fileName));
    
      // Get the Audio format
      javax.sound.sampled.AudioFormat format = stream.getFormat();
 
      // Create the clip
    
      // Create a DataLine Info object

      DataLine.Info info = new DataLine.Info(
         Clip.class, stream.getFormat(), ((int)stream.getFrameLength()*format.getFrameSize()));
      

      // Create the audio clip
      Clip clip = (Clip) AudioSystem.getLine(info);
    
      // Load the audio clip -  does not return until the audio file is completely loaded 
      clip.open(stream);
    
      // Start playing
      clip.start();

    } catch (IOException e) {
    } catch (LineUnavailableException e) {
    } catch (UnsupportedAudioFileException e) {}
  }

  /**
   * Add a rectangular region
   * @param region the region number
   * @param x the x co-ordinate of the region bottom left corner
   * @param y the y co-ordinate of the region bottom left corner
   * @param width the width of the region
   * @param height the height of the region
   */
  public static void addRectRegion(int region, int x, int y, int width, int height) {
    regions[region-1] = new Region(x, y, width, height);
  }

  /**
   * Get the array of regions
   * @return the array of regions 
   */
  public static Region [] getRegions() {
    return regions;
  }

  /**
   * Set the size of the video viewer image
   */
  public static void setImageSize(int width, int height) {
    imageWidth = width;
    imageHeight = height;
  }

  /**
   * Add a Motion Listener for the region
   * @param region the region
   * @param ml the Motion Listener
   */
  public static void addMotionListener(int region, MotionListener ml) {
    regions[region-1].addMotionListener(ml);
  }

  /**
   * Add a Color Listener for the region
   * @param region the region
   * @param cl the Color Listener
   * @param color the color to listen for
   */
  public static void addColorListener(int region, ColorListener cl, int color) {
    regions[region-1].addColorListener(cl, color);
  }

  /**
   * Add a Light Listener for the region
   * @param region the region
   * @param ll the Light Listener
   */
  public static void addLightListener(int region, LightListener ll) {
    regions[region-1].addLightListener(ll);
  }

  /**
   * Return the state of the snapshot flag
   */
  public static boolean takeSnapshot() {
    return takeSnapshot;
  }

  /**
   * Take a snapshot
   * @param filename the JPG file to write the snapshop to
   */
  public static void snapshot(String filename) {
    snapshotFilename = filename;
    takeSnapshot = true;
  }

  /**
   * Set or unset the take snapshot flag
   */
  public static void setSnapshot(boolean snap) {
    takeSnapshot = snap;
  }

  /**
   * Write to <code>fn</code> file the <code>data</code> using the
   * <code>width, height</code> variables. Data is assumed to be 8bit RGB.
   * A JPEG format file is written.
   *
   * @param fn the filename
   * @param data the data to write 
   * @param width the width of the image
   * @param height the height of the image
   * @throws   FileNotFoundException   if the directory/image specified is wrong
   * @throws   IOException  if there are problems reading the file.
   */
  public static void writeImage(String fn, byte[] data, int width, int height)
  throws FileNotFoundException, IOException {

    FileOutputStream fOut = new FileOutputStream(fn);
    JPEGImageEncoder jpeg_encode = JPEGCodec.createJPEGEncoder(fOut);

    int ints[] = new int[data.length/3];
    int k = 0;
    for (int i = height-1; i > 0;i--) {
      for (int j=0;j<width;j++) {
        ints[k++] = 255 << 24 |
          (int) (data[i*width*3 + j*3 + 2] & 0xff) << 16 |
          (int) (data[i*width*3 + j*3 + 1] & 0xff) << 8 |
          (int) (data[i*width*3 + j*3] & 0xff);
      }
    }

    BufferedImage image = new BufferedImage (width, height,BufferedImage.TYPE_INT_RGB);
    image.setRGB(0,0,width,height,ints,0,width);

    jpeg_encode.encode(image);
    fOut.close();
  }

  /**
   * Start the video recorder
   */
  public static void startRecorder(String filename, int millis) {
    // Create the recorder
    recorder = new Recorder(filename, millis);

    // Start the video viewer
    recorder.start();
  }

  /**
   * Test is recording is in progress 
   */
  public static boolean isRecording() {
    return isRecording;
  }

  /*
   * Stop recording
   */
  public static void stopRecording() {
    recorder.stopRecording();
  }

  /**
   * Capture Color
   */
  public static void captureColor(boolean capture) {
    captureColor = capture;
  }
}

