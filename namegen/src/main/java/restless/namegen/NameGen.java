//
// NameGen.java
//

// NameGen Java style, coded in 2006-2008 by Curtis Rueden
// This code is dedicated to the public domain.
// All copyrights (and warranties) are disclaimed.

package restless.namegen;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** GUI for name generation algorithm. */
public class NameGen extends JFrame
  implements ActionListener, ChangeListener, DocumentListener
{

  // -- Constants --

  private static final String MALE = "Male";
  private static final String FEMALE = "Female";
  private static final String LAST = "Surnames";

  private static final String MALE_FILE = "dist.male.first.txt";
  private static final String FEMALE_FILE = "dist.female.first.txt";
  private static final String LAST_FILE = "dist.all.last.txt";

  // -- Fields - GUI --

  private JRadioButton preset;
  private JComboBox presetBox;
  private JRadioButton custom;
  private JTextField customField;
  private JButton customButton;
  private JTextField excludeField;
  private JButton excludeButton;
  private JTextField regexField;
  private Color regexFieldColor;

  private JRadioButton deviant, balanced, similar;
  private SpinnerNumberModel countModel;
  private SpinnerNumberModel maxModel;
  private JSpinner maxSpinner;
  private SpinnerNumberModel iterModel;
  private JSpinner iterSpinner;
  private JCheckBox allowSource;
  private JButton generate;
  private JTextArea names;

  // -- Fields - data --

  private Names n;

  // -- Constructor --

  public NameGen() {
    super("Name Generator");

    ButtonGroup sourceGroup = new ButtonGroup();

    preset = new JRadioButton("Preset", true);
    preset.setActionCommand("names");
    preset.addActionListener(this);
    sourceGroup.add(preset);

    presetBox = new JComboBox(new String[] {MALE, FEMALE, LAST});
    presetBox.setActionCommand("preset");
    presetBox.addActionListener(this);

    custom = new JRadioButton("Custom");
    custom.setActionCommand("names");
    custom.addActionListener(this);
    sourceGroup.add(custom);

    customField = new JTextField(16);
    customField.setActionCommand("names");
    customField.addActionListener(this);

    customButton = new JButton("Choose file");
    customButton.setActionCommand("choose");
    customButton.addActionListener(this);

    excludeField = new JTextField(16);
    excludeField.setActionCommand("names");
    excludeField.addActionListener(this);

    excludeButton = new JButton("Choose file");
    excludeButton.setActionCommand("choose");
    excludeButton.addActionListener(this);

    regexField = new JTextField(24);
    regexField.getDocument().addDocumentListener(this);
    regexFieldColor = regexField.getBackground();

    names = new JTextArea(18, 16);
    names.setWrapStyleWord(true);
    names.setLineWrap(true);

    ButtonGroup widthGroup = new ButtonGroup();

    deviant = new JRadioButton("Deviant");
    deviant.setActionCommand("width");
    deviant.addActionListener(this);
    widthGroup.add(deviant);

    balanced = new JRadioButton("Balanced", true);
    balanced.setActionCommand("width");
    balanced.addActionListener(this);
    widthGroup.add(balanced);

    similar = new JRadioButton("Similar");
    similar.setActionCommand("width");
    similar.addActionListener(this);
    widthGroup.add(similar);

    JLabel countLabel = new JLabel("Number of names ");
    countModel = new SpinnerNumberModel(15, 1, 9999, 1);
    JSpinner countSpinner = new JSpinner(countModel);

    JLabel maxLabel = new JLabel("Maximum name length ");
    maxModel = new SpinnerNumberModel(Names.DEFAULT_MAXIMUM_LENGTH, 3, 9999, 1);
    maxSpinner = new JSpinner(maxModel);
    maxSpinner.addChangeListener(this);

    JLabel iterLabel = new JLabel("Maximum iterations ");
    iterModel = new SpinnerNumberModel(Names.DEFAULT_ITERATIONS,
      1, 999999, 100);
    iterSpinner = new JSpinner(iterModel);
    iterSpinner.addChangeListener(this);

    allowSource = new JCheckBox("Allow source names");
    allowSource.setActionCommand("allowSource");
    allowSource.addActionListener(this);

    generate = new JButton("Generate");
    generate.setActionCommand("generate");
    generate.addActionListener(this);

    // lay out components

    PanelBuilder builder = new PanelBuilder(new FormLayout(
      "pref:grow, 3dlu, pref, 3dlu, pref",
      "pref, " + // source
      "9dlu, pref, 3dlu, pref, 3dlu, pref, " +
      "3dlu, pref, 3dlu, pref, 3dlu, pref, " + // options
      "9dlu, fill:pref:grow")); // generate
    CellConstraints cc = new CellConstraints();

    // preset vs custom
    int row = 1;
    builder.add(makeSourceRow(), cc.xyw(1, row, 5));
    row += 2;

    builder.add(new JScrollPane(names), cc.xywh(1, row, 1, 13));

    builder.addSeparator("Options", cc.xyw(3, row, 3));
    row += 2;
    builder.add(makeWidthRow(), cc.xyw(3, row, 3));
    row += 2;
    builder.add(countLabel, cc.xy(3, row));
    builder.add(countSpinner, cc.xy(5, row));
    row += 2;
    builder.add(maxLabel, cc.xy(3, row));
    builder.add(maxSpinner, cc.xy(5, row));
    row += 2;
    builder.add(iterLabel, cc.xy(3, row));
    builder.add(iterSpinner, cc.xy(5, row));
    row += 2;
    builder.add(allowSource, cc.xyw(3, row, 3));

    row += 2;
    builder.add(generate, cc.xyw(3, row, 3));

    JPanel pane = builder.getPanel();
    //builder.setDialogBorder(true);
    pane.setBorder(new EmptyBorder(10, 10, 10, 10));
    setContentPane(pane);

    computeNames();
  }

  // -- ActionListener methods --

  public void actionPerformed(ActionEvent e) {
    String cmd = e.getActionCommand();
    if ("names".equals(cmd)) computeNames();
    else if ("width".equals(cmd)) {
      n.setWidth(getTupleWidth());
    }
    else if ("allowSource".equals(cmd)) {
      n.setAllowSourceNames(allowSource.isSelected());
    }
    else if ("preset".equals(cmd)) {
      if (!preset.isSelected()) preset.setSelected(true);
      else computeNames();
    }
    else if ("choose".equals(cmd)) {
      JFileChooser chooser = new JFileChooser();
      int rval = chooser.showOpenDialog(this);
      if (rval != JFileChooser.APPROVE_OPTION) return;
      String path = chooser.getSelectedFile().getAbsolutePath();
      if (e.getSource() == customButton) {
        customField.setText(path);
        if (!custom.isSelected()) custom.setSelected(true);
        else computeNames();
      }
      else { // e.getSource() == excludeButton
        excludeField.setText(path);
        computeNames();
      }
    }
    else if ("generate".equals(cmd)) {
      int num = countModel.getNumber().intValue();
      String[] ns = n.generate(num);
      for (int i=0; i<ns.length; i++) {
        String name = ns[i];
        if (name == null) {
          names.append("[no names found]\n");
          break;
        }
        name = name.substring(0, 1).toUpperCase() + name.substring(1);
        names.append(name + "\n");
      }
      names.append("\n");
      names.setCaretPosition(names.getText().length());
    }
  }

  // -- ChangeListener methods --

  public void stateChanged(ChangeEvent e) {
    Object src = e.getSource();
    if (src == maxSpinner) {
      n.setMaximumLength(getMaximumLength());
    }
    else if (src == iterSpinner) {
      n.setIterations(getIterations());
    }
  }

  // -- DocumentListener methods --

  public void changedUpdate(DocumentEvent e) { documentUpdate(e); }
  public void insertUpdate(DocumentEvent e) { documentUpdate(e); }
  public void removeUpdate(DocumentEvent e) { documentUpdate(e); }

  private void documentUpdate(DocumentEvent e) {
    n.setRegex(getRegex());
    Color c = n.getRegex() == null ? Color.red : regexFieldColor;
    regexField.setBackground(c);
  }

  // -- Helper methods - GUI --

  private JPanel makeSourceRow() {
    PanelBuilder builder = new PanelBuilder(new FormLayout(
      "pref, 3dlu, pref, fill:pref:grow, 3dlu, pref",
      "pref, 3dlu, pref, 3dlu, pref, 3dlu, pref"));
    CellConstraints cc = new CellConstraints();
    builder.add(preset, cc.xy(1, 1));
    builder.add(presetBox, cc.xy(3, 1));
    builder.add(custom, cc.xy(1, 3));
    builder.add(customField, cc.xyw(3, 3, 2));
    builder.add(customButton, cc.xy(6, 3));
    builder.add(new JLabel("Exclude"), cc.xy(1, 5));
    builder.add(excludeField, cc.xyw(3, 5, 2));
    builder.add(excludeButton, cc.xy(6, 5));
    builder.add(new JLabel("Regex"), cc.xy(1, 7));
    builder.add(regexField, cc.xyw(3, 7, 4));
    return builder.getPanel();
  }

  private JPanel makeWidthRow() {
    PanelBuilder builder = new PanelBuilder(new FormLayout(
      "pref, 3dlu, pref, 3dlu, pref", "pref"));
    CellConstraints cc = new CellConstraints();
    builder.add(deviant, cc.xy(1, 1));
    builder.add(balanced, cc.xy(3, 1));
    builder.add(similar, cc.xy(5, 1));
    return builder.getPanel();
  }

  private void printNameCounts() {
    StringBuffer sb = new StringBuffer();
    sb.append("[" + n.getSourceNameCount() + " names");
    int noCount = n.getExcludeNameCount();
    if (noCount > 0) sb.append(" (" + noCount + " excluded)");
    sb.append("]\n\n");
    names.append(sb.toString());
  }

  private int getTupleWidth() {
    if (deviant.isSelected()) return 2;
    if (balanced.isSelected()) return 3;
    if (similar.isSelected()) return 4;
    // NB: impossible
    return -1;
  }

  private String getRegex() {
    return regexField.getText();
  }

  private int getMaximumLength() {
    return maxModel.getNumber().intValue();
  }

  private int getIterations() {
    return iterModel.getNumber().intValue();
  }

  // -- Helper methods - computation --

  private void computeNames() {
    setCursor(new Cursor(Cursor.WAIT_CURSOR));

    URL source = null;
    if (custom.isSelected()) {
      // parse path to custom source file
      String path = customField.getText();
      if (path == null || path.equals("")) {
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        return;
      }
      File file = new File(path);
      try { source = file.toURI().toURL(); }
      catch (MalformedURLException exc) {
        names.append(exc.toString());
        names.append("\n");
        generate.setEnabled(false);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        return;
      }
    }

    if (source == null) {
      // obtain source names from a preset
      String s = (String) presetBox.getSelectedItem();
      if (s.equals(MALE)) source = getClass().getResource(MALE_FILE);
      else if (s.equals(FEMALE)) source = getClass().getResource(FEMALE_FILE);
      else if (s.equals(LAST)) source = getClass().getResource(LAST_FILE);
      else {
        // NB: impossible
        names.append("Invalid preset.\n\n");
        generate.setEnabled(false);
        setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        return;
      }
    }

    // parse path to exclusion file
    URL exclude = null;
    String path = excludeField.getText();
    if (path != null && !path.equals("")) {
      File file = new File(path);
      try { exclude = file.toURI().toURL(); }
      catch (MalformedURLException exc) { }
    }

    try {
      n = new Names(source, exclude, getTupleWidth());
      n.setRegex(getRegex());
      n.setMaximumLength(getMaximumLength());
      n.setIterations(getIterations());
    }
    catch (IOException exc) {
      names.append(exc.toString());
      names.append("\n\n");
      generate.setEnabled(false);
      setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
      return;
    }
    generate.setEnabled(true);
    printNameCounts();

    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
  }

  // -- Main method --

  public static void main(String[] args) {
    NameGen ng = new NameGen();
    ng.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    ng.pack();
    Dimension siz = ng.getSize();
    Dimension scr = Toolkit.getDefaultToolkit().getScreenSize();
    ng.setLocation((scr.width - siz.width) / 2, (scr.height - siz.height) / 2);
    ng.setVisible(true);
  }

}
