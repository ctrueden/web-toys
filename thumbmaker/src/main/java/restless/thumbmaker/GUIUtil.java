//
// GUIUtil.java
//

package restless.thumbmaker;

import java.awt.*;

import javax.swing.*;

/** A collection of user interface utilities. */
public final class GUIUtil {

  private GUIUtil() { }

  /** Centers the given window onscreen. */
  public static void centerWindow(Window w) {
    Dimension win = w.getSize();
    Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
    w.setLocation((scr.width - win.width) / 2, (scr.height - win.height) / 2);
  }

  /** Pads a component horizontally, so that it stays centered. */
  public static JPanel pad(Component c) {
    JPanel p;
    if (c instanceof JPanel) p = (JPanel) c;
    else {
      p = new JPanel();
      p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
      p.add(c);
    }
    p.add(Box.createHorizontalGlue(), 0);
    p.add(Box.createHorizontalGlue());
    return p;
  }

  /** Creates a label with the proper text color. */
  public static JLabel makeLabel(String text) {
    JLabel label = new JLabel(text);
    label.setForeground(Color.black);
    return label;
  }

  /** Creates a text field that remains one line in height. */
  public static JTextField makeTextField(String text, int columns) {
    return new JTextField(text, columns) {
      public Dimension getMaximumSize() {
        Dimension pref = getPreferredSize();
        Dimension max = super.getMaximumSize();
        return new Dimension(max.width, pref.height);
      }
    };
  }
  
  /** Creates a combo box that remains one line in height. */
  public static JComboBox makeComboBox(Object[] items) {
    return new JComboBox(items) {
      public Dimension getMaximumSize() {
        Dimension pref = getPreferredSize();
        Dimension max = super.getMaximumSize();
        return new Dimension(max.width, pref.height);
      }
    };
  }

}
