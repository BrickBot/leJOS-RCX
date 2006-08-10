import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.io.*;
import java.util.*;
import javax.swing.event.*;
import josx.rcxcomm.*;
import josx.vision.*;

public class RemoteControl implements ColorListener {
  static private JPanel arrowPanel = new JPanel();
  static private JPanel tiltPanel = new JPanel();
  static private JPanel colorPanel1;  
  static private JPanel colorPanel2;
  static private JPanel colorPanel3;
  private RCX _rcx;

  public RemoteControl(String port) throws IOException {
	  _rcx = new RCX(port);

    // Create the Icon buttons
    
    JIconButton upButton = new JIconButton("up.gif");
    JIconButton downButton = new JIconButton("down.gif");
    JIconButton stopButton = new JIconButton("stop.gif");
    JIconButton leftButton = new JIconButton("left.gif");
    JIconButton rightButton = new JIconButton("right.gif");
    JIconButton tiltUpButton = new JIconButton("up.gif");
    JIconButton tiltDownButton = new JIconButton("down.gif");

    // Set up the arrow panel including all the button action listeners

    arrowPanel.setBackground(Color.blue);
    arrowPanel.setLayout(new BorderLayout());

    upButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _rcx.forward();
        _rcx.playTone((short) 500, (byte) 100);
      }
    });
    arrowPanel.add(labelledComponent("Forwards",upButton, true), BorderLayout.NORTH);

    downButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _rcx.backward();
        _rcx.playTone((short) 1000,(byte) 100);
      }
    });
    arrowPanel.add(labelledComponent("Backwards",downButton, true), BorderLayout.SOUTH);   

    stopButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _rcx.stop();
      }
    });
    arrowPanel.add(labelledComponent("Stop",stopButton, true), BorderLayout.CENTER);

    leftButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _rcx.spinLeft();
      }
    });
    arrowPanel.add(labelledComponent("Left",leftButton, true), BorderLayout.WEST);

    rightButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _rcx.spinRight();
      }
    });
    arrowPanel.add(labelledComponent("Right",rightButton, true), BorderLayout.EAST);

    // Set up the tilt and colors panel, including the tilt button action listeners

    tiltPanel.setBackground(Color.blue);
    tiltPanel.setLayout(new BorderLayout());

    tiltUpButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _rcx.tiltUp(1);
      }
    });
    tiltPanel.add(labelledComponent("Tilt Camera up",tiltUpButton, true), BorderLayout.NORTH);

    tiltDownButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        _rcx.tiltDown(1);
      }
    });
    tiltPanel.add(labelledComponent("Tilt Camera down",tiltDownButton, true), BorderLayout.SOUTH);

    Box currentColors = Box.createHorizontalBox();
    colorPanel1 = colorPanel();
    colorPanel2 = colorPanel();
    colorPanel3 = colorPanel();

    currentColors.add(colorPanel1);
    currentColors.add(colorPanel2);
    currentColors.add(colorPanel3);

    JPanel colorPanel4 = colorPanel();
    colorPanel4.setBackground(new Color(red2));
    JPanel colorPanel5 = colorPanel();
    colorPanel5.setBackground(new Color(red1));
    JPanel colorPanel6 = colorPanel();
    colorPanel6.setBackground(new Color(red2));

    Box targetColors = Box.createHorizontalBox();    

    targetColors.add(colorPanel4);
    targetColors.add(colorPanel5);
    targetColors.add(colorPanel6);

    tiltPanel.add(labelledComponent("Target Colors", targetColors, true), BorderLayout.WEST);
    tiltPanel.add(labelledComponent("Average Colors", currentColors, true), BorderLayout.EAST);
  }

  // Create a Box containing a labelled component.
  // Apply the glue thickly.

  private Box labelledComponent(String label, Component comp, boolean horizontal) {
    Box labelBox = (horizontal ? Box.createHorizontalBox() : Box.createVerticalBox());
    labelBox.add(Box.createGlue());
    JLabel lab = new JLabel(label);
    lab.setForeground(Color.yellow);
    labelBox.add(lab);
    labelBox.add(Box.createGlue());
    Box spaceBox = (horizontal ? Box.createHorizontalBox() : Box.createVerticalBox());
    spaceBox.add(Box.createGlue());
    spaceBox.add(comp);
    spaceBox.add(Box.createGlue());
    Box compBox = (horizontal ? Box.createVerticalBox() : Box.createHorizontalBox());
    compBox.add(Box.createGlue());
    compBox.add(spaceBox);
    compBox.add(labelBox);
    return compBox;
  }

  // Create a panel that displays a color

  private JPanel colorPanel() {
    JPanel colorPanel = new JPanel();
    Border border = BorderFactory.createLineBorder(Color.red,2);
    colorPanel.setBorder(border);

    // Set a minimum width 

    colorPanel.add(new JLabel("        "));
    return colorPanel;
  }

  static private long lastPlay = 0;

  // Follow the red object and play a sound when it is straight ahead 

  public void colorDetected(int region, int color) {
    if ((System.currentTimeMillis() - lastPlay) > 2000) {
      lastPlay = System.currentTimeMillis();
      if (region == 2) {
        _rcx.forward(1);
        Vision.playSound("../../../Effects/CarHorn.wav");
      } else if (region == 1) {
        System.out.println("Region 1");
        _rcx.spinLeft(1);
      } else if (region == 3) {
        System.out.println("Region 3");
        _rcx.spinRight(1);
      }
    }
  }

  private static final int red1 = 0xfe9878;
  private static final int red2 = 0xd88868;

  public static void main(String [] args) { 

	  try {
		  	if(args.length!=1)
				throw new Exception("first argument must be tower port (USB,COM1 etc)");
	  
    // Create an instance of this class for listening
    RemoteControl listener = new RemoteControl(args[0]); 

    // Create 3 regions and set them to look for a red object
    // Looking for darker red at the edges as color is not as bright there

    Vision.addRectRegion(1, 0, 60, 106, 60);
    Vision.addRectRegion(2, 106, 60, 106, 60);
    Vision.addRectRegion(3, 212, 60, 108, 60);
    Vision.addColorListener(1, listener, red2);
    Vision.addColorListener(2, listener, red1);
    Vision.addColorListener(3, listener, red2);

    // Create the viewer and set its title

    Vision.startViewer("Robot Remote Control");

    // Get the frame and add the remote control and color components

    Frame visionFrame = Vision.getFrame();
    visionFrame.add("West", arrowPanel);
    visionFrame.add("East", tiltPanel);
    visionFrame.pack();
    visionFrame.setVisible(true);

    // Continually update the current color panels
    // Quits when the window is shut down

    for(;;) {
      colorPanel1.setBackground(new Color(Vision.getAvgRGB(1)));
      colorPanel1.revalidate();
      colorPanel2.setBackground(new Color(Vision.getAvgRGB(2)));
      colorPanel2.revalidate();
      colorPanel3.setBackground(new Color(Vision.getAvgRGB(3)));
      colorPanel3.revalidate();

      // System.out.println("Color1 is " + Integer.toHexString(Vision.getAvgRGB(1)));
      // System.out.println("Color2 is " + Integer.toHexString(Vision.getAvgRGB(2)));
      // System.out.println("Color3 is " + Integer.toHexString(Vision.getAvgRGB(3)));

      try {
        Thread.sleep(500);
      } catch (InterruptedException ie) {}
    }
	  } catch(Exception e) {
		  e.printStackTrace();
	  }

  }
}
