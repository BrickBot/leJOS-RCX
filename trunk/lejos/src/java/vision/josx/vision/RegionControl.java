package josx.vision;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.media.Control;

/**
 * Title: Lejos Vision System
 * Description: REgion Control
 * @author Lawrie Griffiths
 */
public class RegionControl implements Control, ActionListener {
  private Component component;
  private JButton button;
  private RegionEffect effect;
  private boolean debug;

  /**
   * Create the Region Control
   */  
  public RegionControl(RegionEffect effect) {
    this.effect = effect;
  }

  /**
   * Return the visual component
   * @return the component containing the GUI controls
   **/
  public Component getControlComponent () {
    if (component == null) {
      button = new JButton("Toggle Show Regions");
      button.addActionListener(this);

      button.setToolTipText("Click to toggle show regions on and off");

      Panel componentPanel = new Panel();
      componentPanel.setLayout(new BorderLayout());
      componentPanel.add("Center", button);
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
      effect.show = !effect.show;
    }
  }

}
