package josx.vision;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.media.Control;

/**
 * Title: Lejos Vision System
 * Description: Color Detection Control
 * @author Lawrie Griffiths
 */
public class ColorDetectionControl implements Control, ActionListener, ChangeListener  {
  private Component component;
  private JButton button;
  private JSlider threshold;
  private JSlider proportionThreshold;
  private JLabel label;
  private JLabel proportionLabel;
  private ColorEffect effect;
  private boolean debug;
  private int thresholdValue;

  /**
   * Create the Motion Detection Control
   */  
  public ColorDetectionControl(ColorEffect effect) {
    this.effect = effect;
  }

  /**
   * Return the visual component
   * @return the component containing the GUI controls
   **/
  public Component getControlComponent () {
    if (component == null) {
      label = new JLabel("Set Pixel difference threshold:");
      proportionLabel = new JLabel("Set Proportion threshold:");
      
      button = new JButton("Color Debug");
      button.addActionListener(this);

      button.setToolTipText("Click to turn debugging mode on/off");

      threshold = new JSlider(JSlider.HORIZONTAL,
                               0,
                               effect.MAX_PIXEL_THRESHOLD / 4,
                               effect.pixelThreshold / 4);

      threshold.setMajorTickSpacing(1);
      threshold.setPaintLabels(true);
      threshold.addChangeListener(this);

      proportionThreshold = new JSlider(JSlider.HORIZONTAL,
                                        0,
                                        (int) (effect.MAX_PROPORTION / effect.PROPORTION_INC),
                                        (int) (effect.requiredProportion / effect.PROPORTION_INC));

      proportionThreshold.setMajorTickSpacing(1);
      proportionThreshold.setPaintLabels(true);
      proportionThreshold.addChangeListener(this);

      Box componentPanel = Box.createVerticalBox();
      componentPanel.add(label);
      componentPanel.add(threshold);
      componentPanel.add(proportionLabel);
      componentPanel.add(proportionThreshold);

      componentPanel.invalidate();
      component = componentPanel;
    }
    return component;
  }

  /**
   * Toggle debug
   * @param e the action event (ignored)
   **/
  public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    if (o == button) {
      debug = !debug;
    }
  }

  /**
   * Set the threshold value
   * @param e the Changeevent (ignored)
   **/
  public void stateChanged(ChangeEvent e) {
    Object o = e.getSource();
    if (o == threshold) {
      effect.pixelThreshold = threshold.getValue()*4;
    }
     if (o == proportionThreshold) {
      effect.requiredProportion = proportionThreshold.getValue()*effect.PROPORTION_INC;
    }
 }
}
