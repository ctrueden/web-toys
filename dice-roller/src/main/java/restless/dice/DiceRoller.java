//
// DiceRoller.java
//

// DiceRoller, coded by Curtis Rueden.
// This code is dedicated to the public domain.
// All copyrights (and warranties) are disclaimed.

package restless.dice;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DiceRoller extends JFrame
  implements ActionListener, MouseListener
{

  // -- Constants --

  private static final int HIST_WIDTH = 300;
  private static final int HIST_HEIGHT = 124;

  private static final int PRESET_ROWS = 2;

  private static final int PAD = 5;


  // -- Fields --

  private int[] presetNum = {1, 1, 1, 1, 1, 1, 1, 3, 5, 10};
  private int[] presetIndex = {3, 5, 7, 8, 9, 11, 14, 5, 5, 5};

  private SpinnerNumberModel model;
  private JComboBox box;
  private JButton roll, ace;
  private JTextField total, best, most;
  private JLabel histLabel;
  private ImageIcon histogram;
  private JPanel[] presets;
  private JButton[] presetButtons;
  private JTextField presetText;
  private int numAces;
  private int aceTotal;
  private int editIndex = -1;


  // -- Constructor --

  public DiceRoller() {
    super("Dice Roller");
    setResizable(false);
    JPanel pane = new JPanel();
    pane.setBorder(new EmptyBorder(PAD, PAD, PAD, PAD));
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    setContentPane(pane);

    JPanel top = new JPanel();
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));

    JPanel left = new JPanel();
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

    JPanel left1 = new JPanel();
    left1.setLayout(new BoxLayout(left1, BoxLayout.X_AXIS));
    model = new SpinnerNumberModel(1, 1, 999, 1);
    JSpinner spinner = new JSpinner(model);
    spinner.setMaximumSize(spinner.getPreferredSize());
    left1.add(spinner);
    box = new JComboBox(new String[] {
      "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8",
      "d10", "d12", "d16", "d20", "d24", "d30", "d100"
    });
    box.setSelectedIndex(11); // default to d20
    box.addActionListener(this);
    box.setMaximumSize(box.getPreferredSize());
    left1.add(Box.createHorizontalStrut(PAD));
    left1.add(box);
    left.add(left1);

    JPanel left2 = new JPanel();
    left2.setLayout(new BoxLayout(left2, BoxLayout.X_AXIS));
    roll = new JButton("Roll");
    roll.setMnemonic('r');
    roll.addActionListener(this);
    left2.add(roll);
    ace = new JButton("Ace");
    ace.setMnemonic('a');
    ace.addActionListener(this);
    ace.setEnabled(false);
    left2.add(Box.createHorizontalStrut(PAD));
    left2.add(ace);
    left.add(Box.createVerticalStrut(PAD));
    left.add(left2);

    JPanel left3 = new JPanel();
    left3.setLayout(new BoxLayout(left3, BoxLayout.X_AXIS));
    JLabel totalLabel = new JLabel("Total: ");
    totalLabel.setDisplayedMnemonic('t');
    left3.add(Box.createHorizontalGlue());
    left3.add(new JLabel("Total: "));
    total = new JTextField(8);
    total.setEditable(false);
    total.setMaximumSize(total.getPreferredSize());
    totalLabel.setLabelFor(total);
    left3.add(Box.createHorizontalStrut(PAD));
    left3.add(total);
    left3.add(Box.createHorizontalStrut(PAD));
    left.add(Box.createVerticalStrut(PAD));
    left.add(left3);

    JPanel left4 = new JPanel();
    left4.setLayout(new BoxLayout(left4, BoxLayout.X_AXIS));
    JLabel bestLabel = new JLabel("Best: ");
    bestLabel.setDisplayedMnemonic('b');
    left4.add(Box.createHorizontalGlue());
    left4.add(bestLabel);
    best = new JTextField(8);
    best.setEditable(false);
    best.setMaximumSize(best.getPreferredSize());
    bestLabel.setLabelFor(best);
    left4.add(Box.createHorizontalStrut(PAD));
    left4.add(best);
    left4.add(Box.createHorizontalStrut(PAD));
    left.add(Box.createVerticalStrut(PAD));
    left.add(left4);

    JPanel left5 = new JPanel();
    left5.setLayout(new BoxLayout(left5, BoxLayout.X_AXIS));
    JLabel mostLabel = new JLabel("Most: ");
    mostLabel.setDisplayedMnemonic('m');
    left5.add(Box.createHorizontalGlue());
    left5.add(mostLabel);
    most = new JTextField(8);
    most.setEditable(false);
    most.setMaximumSize(most.getPreferredSize());
    mostLabel.setLabelFor(most);
    left5.add(Box.createHorizontalStrut(PAD));
    left5.add(most);
    left5.add(Box.createHorizontalStrut(PAD));
    left.add(Box.createVerticalStrut(PAD));
    left.add(left5);
    top.add(left);

    // create starting histogram image, with some documentation
    BufferedImage startImage = new BufferedImage(
      HIST_WIDTH + 2, HIST_HEIGHT + 2, BufferedImage.TYPE_INT_RGB);
    Graphics g = startImage.getGraphics();
    g.setColor(Color.green);
    g.setFont(new Font("Serif", Font.BOLD, 20));
    g.drawString("Dice Roller by Curtis Rueden", 5, 22);
    g.setColor(Color.yellow);
    g.setFont(new Font("SansSerif", Font.PLAIN, 12));
    g.drawString("\"Best\" is highest value with # of times rolled.", 14, 42);
    g.drawString("\"Botch\" reported if more than 50% are 1s.", 14, 56);
    g.drawString("\"Ace\" rerolls maximal values from previous roll.", 14, 70);
    g.drawString("\"Ace\" keeps track of total maximum in brackets.", 14, 84);
    g.drawString("Right-click preset buttons to change dice rolled.", 14, 98);
    g.dispose();

    histogram = new ImageIcon(startImage);
    histLabel = new JLabel(histogram);
    top.add(histLabel);
    pane.add(top);
    pane.add(Box.createVerticalStrut(PAD));

    // create preset roll combination buttons along the bottom rows
    int len = presetNum.length;
    int cols = len / PRESET_ROWS;
    if (len % PRESET_ROWS > 0) cols++;
    JPanel p = null;
    presets = new JPanel[PRESET_ROWS];
    presetButtons = new JButton[presetNum.length];
    for (int i=0; i<presetNum.length; i++) {
      if (i % cols == 0) {
        p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
        pane.add(p);
        presets[i / cols] = p;
      }
      JButton b = new JButton();
      setPresetText(b, i + 1, presetNum[i], presetIndex[i]);
      if (i == 9) b.setMnemonic('0');
      else if (i < 9) b.setMnemonic('1' + i);
      b.setActionCommand("" + i);
      b.addActionListener(this);
      b.addMouseListener(this);
      p.add(b);
      presetButtons[i] = b;
    }

    // create preset text field for changing preset buttons
    presetText = new JTextField(4);
    presetText.addActionListener(this);
  }


  // -- API methods --

  public void setPresetText(JButton b, int index, int num, int die) {
    int digits = ("" + presetNum.length).length();
    String s = "" + index;
    while (s.length() < digits) s = "0" + s;
    b.setText(s + ". " + num + ((String) box.getItemAt(die)));
  }

  /**
   * Replaces the given preset button with the preset editor text field.
   * Called when a preset button is right clicked.
   */
  public void setPresetEditField(int index) {
    int len = presetNum.length;
    int cols = len / PRESET_ROWS;

    if (editIndex >= 0) {
      // restore previously hidden preset button
      int pi = editIndex / cols; // panel index
      int ci = editIndex % cols; // component index
      presets[pi].remove(ci);
      presets[pi].add(presetButtons[editIndex], ci);
    }
    editIndex = index;
    if (editIndex >= 0) {
      String dice = presetButtons[editIndex].getText();
      int ndx = dice.indexOf(".");
      dice = dice.substring(ndx + 2);
      presetText.setText(dice);
      Dimension prefSize = presetButtons[editIndex].getPreferredSize();
      presetText.setPreferredSize(prefSize);
      presetText.setMaximumSize(prefSize);

      // replace preset button with text field for editing
      int pi = editIndex / cols; // panel index
      int ci = editIndex % cols; // component index
      presets[pi].remove(ci);
      presets[pi].add(presetText, ci);
    }
    JPanel pane = (JPanel) getContentPane();
    pane.validate();
    pane.repaint();
  }


  public static int[] roll(int num, int die) {
    int[] rolls = new int[num];
    for (int i=0; i<num; i++) rolls[i] = (int) (die * Math.random()) + 1;
    return rolls;
  }

  public void doTotal(int[] rolls) {
    int sum = 0;
    for (int i=0; i<rolls.length; i++) sum += rolls[i];
    total.setText("" + sum);
  }

  public void doBest(int[] rolls, int die) {
    int high = 0;
    int count = 0;
    int ones = 0;
    for (int i=0; i<rolls.length; i++) {
      if (rolls[i] == 1) ones++;
      if (rolls[i] > high) {
        count = 1;
        high = rolls[i];
      }
      else if (rolls[i] == high) count++;
    }
    boolean botch = false;
    String s = "" + high + "x" + count;
    if (aceTotal > 0) s += " [" + (aceTotal + high) + "]";
    else if (ones > rolls.length / 2) {
      s += " [botch]";
      botch = true;
    }

    numAces = high == die ? count : 0;
    if (numAces > 0 && !botch) ace.setEnabled(true);
    else {
      ace.setEnabled(false);
      aceTotal = 0;
    }

    best.setText(s);
  }

  public void doHistogram(int[] rolls, int xlen) {
    int[] hist = new int[xlen];
    for (int i=0; i<rolls.length; i++) hist[rolls[i] - 1]++;

    // determine most value
    int ylen = 0;
    int ycount = 0;
    int[] yvals = new int[xlen];
    for (int i=0; i<xlen; i++) {
      if (hist[i] > ylen) {
        ylen = hist[i];
        ycount = 1;
        yvals[0] = i + 1;
      }
      else if (hist[i] == ylen) yvals[ycount++] = i + 1;
    }
    String s = ycount > 1 ? "(" : "";
    for (int i=0; i<ycount; i++) {
      s += yvals[i];
      if (i < ycount - 1) s += ",";
    }
    if (ycount > 1) s += ")";
    most.setText(s + "x" + ylen);

    // draw histogram
    int xinc = HIST_WIDTH / xlen;
    int yinc = HIST_HEIGHT / ylen;
    BufferedImage image = new BufferedImage(
      HIST_WIDTH + 2, HIST_HEIGHT + 2, BufferedImage.TYPE_INT_RGB);
    if (xinc >= 1 && yinc >= 1) {
      int ymax = HIST_HEIGHT / yinc;
      Graphics2D g = image.createGraphics();
      for (int i=0; i<xlen; i++) {
        for (int j=0; j<ymax; j++) {
          int x = xinc * i + 1;
          int y = HIST_HEIGHT - yinc * (j + 1) + 1;
          Color c = j < hist[i] ?
            (hist[i] == ylen ? Color.red : Color.yellow) : Color.gray;
          g.setColor(c);
          g.fill3DRect(x, y, xinc, yinc, true);
        }
      }
      g.dispose();
    }
    histogram.setImage(image);
    histLabel.repaint();
  }

  public void doRoll() {
    int num = model.getNumber().intValue();
    int die = getDie();
    int[] rolls = roll(num, die);
    doTotal(rolls);
    doBest(rolls, die);
    doHistogram(rolls, die);
  }

  public void doAce() {
    model.setValue(new Integer(numAces));
    aceTotal += getDie();
    doRoll();
  }

  public void doPreset(int i) {
    model.setValue(new Integer(presetNum[i]));
    box.setSelectedIndex(presetIndex[i]);
    doRoll();
  }

  public int getDie() {
    String s = ((String) box.getSelectedItem()).substring(1);
    return Integer.parseInt(s);
  }


  // -- ActionListener methods --

  public void actionPerformed(ActionEvent e) {
    Object o = e.getSource();
    if (o == roll) doRoll();
    else if (o == ace) doAce();
    else if (o == box) {
      ace.setEnabled(false);
      aceTotal = 0;
    }
    else if (o == presetText) {
      String dice = presetText.getText().trim().toLowerCase();
      int ndx = dice.indexOf("d");
      if (ndx < 0 || dice.length() < ndx + 2) return;
      int num;
      try { num = Integer.parseInt(dice.substring(0, ndx)); }
      catch (NumberFormatException exc) { return; }
      dice = dice.substring(ndx);
      int die = -1;
      for (int i=0; i<box.getItemCount(); i++) {
        String s = (String) box.getItemAt(i);
        if (s.equals(dice)) {
          die = i;
          break;
        }
      }
      if (die < 0) return;
      setPresetText(presetButtons[editIndex], editIndex + 1, num, die);
      presetNum[editIndex] = num;
      presetIndex[editIndex] = die;
      setPresetEditField(-1);
    }
    else {
      String cmd = e.getActionCommand();
      int i = -1;
      try { i = Integer.parseInt(cmd); }
      catch (NumberFormatException exc) { }
      if (i >= 0) doPreset(i);
    }
  }


  // -- MouseListener methods --

  public void mousePressed(MouseEvent e) {
    if (!SwingUtilities.isRightMouseButton(e)) return;
    JButton b = (JButton) e.getSource();
    int index = -1;
    for (int i=0; i<presetButtons.length; i++) {
      if (b == presetButtons[i]) {
        index = i;
        break;
      }
    }
    setPresetEditField(index);
  }

  public void mouseClicked(MouseEvent e) { }
  public void mouseEntered(MouseEvent e) { }
  public void mouseExited(MouseEvent e) { }
  public void mouseReleased(MouseEvent e) { }


  // -- Main method --

  public static void main(String[] args) {
    DiceRoller dr = new DiceRoller();
    dr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    dr.pack();
    Dimension siz = dr.getSize();
    Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
    dr.setLocation((scr.width - siz.width) / 2, (scr.height - siz.height) / 2);
    dr.setVisible(true);
  }

}
