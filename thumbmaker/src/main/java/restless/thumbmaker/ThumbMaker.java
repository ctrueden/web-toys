//
// ThumbMaker.java
//

package restless.thumbmaker;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.*;

/** An application for creating thumbnails from a list of images. */
public class ThumbMaker extends JFrame
  implements ActionListener, ChangeListener, WindowListener
{

  // -- Constants --

  private static final String SLASH = System.getProperty("file.separator");

  // -- Fields --

  private JList list;
  private ThumbTransferHandler changeFilesInList;
  private JProgressBar progress;
  private JButton addFiles, process, remove;
  private JTextField xres, yres;
  private JCheckBox aspect;
  private JLabel redLabel, greenLabel, blueLabel;
  private JSlider red, green, blue;
  private JLabel redValue, greenValue, blueValue;
  private JLabel colorLabel;
  private JPanel colorBox;
  private JComboBox format;
  private JComboBox algorithm;
  private JTextField prepend, append;
  private JTextField output;
  private JButton dotDotDot;

  // -- Constructor --

  public ThumbMaker() {
    super("ThumbMaker");

    // grab the preferences so that they can be used to fill out the layout
    ThumbMakerPreferences myPrefs = ThumbMakerPreferences.getInstance();

    // content pane
    JPanel pane = new JPanel();
    pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
    setContentPane(pane);

    // top panel
    JPanel top = new JPanel();
    top.setLayout(new BoxLayout(top, BoxLayout.X_AXIS));
    pane.add(top);

    // left-hand panel
    JPanel left = new JPanel();
    left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
    top.add(left);

    // horizontal padding
    top.add(Box.createHorizontalStrut(5));

    // label for file list
    JLabel listLabel = GUIUtil.makeLabel("Files to process:");
    listLabel.setDisplayedMnemonic('f');
    String listTip = "List of files from which to create thumbnails";
    listLabel.setToolTipText(listTip);
    left.add(GUIUtil.pad(listLabel));

    // list of files to convert
    list = new JList();
    listLabel.setLabelFor(list);
    list.setToolTipText(listTip);
    list.setModel(new DefaultListModel());
    list.setDragEnabled(true);
    changeFilesInList = new ThumbTransferHandler();
    list.setTransferHandler(changeFilesInList);
    left.add(new JScrollPane(list));

    // progress bar
    progress = new JProgressBar(0, 1);
    progress.setString("[Drag and drop files onto list to begin]");
    progress.setStringPainted(true);
    progress.setToolTipText("Status of thumbnail processing operation");
    left.add(progress);

    // panel for process and remove buttons
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

    // add files button
    addFiles = new JButton("Add Files");
    addFiles.setMnemonic('d');
    addFiles.setToolTipText("Add files to be processed.");
    addFiles.addActionListener(this);
    p.add(addFiles);

    p.add(Box.createHorizontalStrut(5));

    // process button
    process = new JButton("Process");
    process.setMnemonic('p');
    process.setToolTipText("Begin creating thumbnails");
    process.addActionListener(this);
    p.add(process);

    p.add(Box.createHorizontalStrut(5));

    // remove button
    remove = new JButton("Remove");
    remove.setMnemonic('v');
    remove.setToolTipText("Remove selected files from the list");
    remove.addActionListener(this);
    p.add(remove);

    left.add(GUIUtil.pad(p));

    // right-hand panel
    JPanel right = new JPanel();
    right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
    top.add(right);

    // panel for resolution settings
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

    // resolution label
    JLabel resLabel = GUIUtil.makeLabel("Resolution: ");
    resLabel.setDisplayedMnemonic('s');
    resLabel.setToolTipText("Resolution of the thumbnails");
    p.add(resLabel);

    // x resolution text box
    xres = GUIUtil.makeTextField(myPrefs.getStringPref
            (ThumbMakerPreferences.RES_WIDTH_PREF_NAME), 2);
    resLabel.setLabelFor(xres);
    xres.setToolTipText("Thumbnail width");
    p.add(xres);

    // "by" label
    JLabel byLabel = GUIUtil.makeLabel(" by ");
    byLabel.setDisplayedMnemonic('y');
    p.add(byLabel);

    // y resolution text box
    yres = GUIUtil.makeTextField(myPrefs.getStringPref
            (ThumbMakerPreferences.RES_HEIGHT_PREF_NAME), 2);
    byLabel.setLabelFor(yres);
    yres.setToolTipText("Thumbnail height");
    p.add(yres);

    right.add(GUIUtil.pad(p));
    right.add(Box.createVerticalStrut(8));

    // aspect ratio checkbox
    aspect = new JCheckBox("Maintain aspect ratio", true);
    aspect.setMnemonic('m');
    aspect.setToolTipText(
      "When checked, thumbnails are not stretched, " +
      "but rather padded with the background color.");
    aspect.addActionListener(this);
    right.add(GUIUtil.pad(aspect));
    // make sure that the check box is initialized correctly
    aspect.setSelected(myPrefs.getStringPref
        (ThumbMakerPreferences.DO_MAINTAIN_ASPECT_PREF_NAME)
        .equalsIgnoreCase(ThumbMakerPreferences.BOOLEAN_TRUE_STRING));

    // panel for background color
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

    // load the color values from the preferences
    int redValueNumber = myPrefs.getIntegerPref
            (ThumbMakerPreferences.RED_VALUE_PREF_NAME);
    int greenValueNumber = myPrefs.getIntegerPref
            (ThumbMakerPreferences.GREEN_VALUE_PREF_NAME);
    int blueValueNumber = myPrefs.getIntegerPref
            (ThumbMakerPreferences.BLUE_VALUE_PREF_NAME);

    // background color label
    colorLabel = GUIUtil.makeLabel("Background color: ");
    String colorTip = "Thumbnail background color";
    colorLabel.setToolTipText(colorTip);
    p.add(colorLabel);

    // background color
    colorBox = new JPanel();
    colorBox.setToolTipText(colorTip);
    colorBox.setBorder(new LineBorder(Color.black, 1));
    Dimension colorBoxSize = new Dimension(45, 15);
    colorBox.setMaximumSize(colorBoxSize);
    colorBox.setMinimumSize(colorBoxSize);
    colorBox.setPreferredSize(colorBoxSize);
    colorBox.setBackground(
      new Color(redValueNumber, greenValueNumber, blueValueNumber));
    p.add(colorBox);

    right.add(GUIUtil.pad(p));
    right.add(Box.createVerticalStrut(2));

    // red slider
    redLabel = GUIUtil.makeLabel("R");
    red = new JSlider(0, 255, redValueNumber);
    redValue = GUIUtil.makeLabel("" + redValueNumber);
    redValue.setToolTipText("Red color component slider");
    right.add(makeSlider(redLabel, red, redValue, "Red"));

    // green slider
    greenLabel = GUIUtil.makeLabel("G");
    green = new JSlider(0, 255, greenValueNumber);
    greenValue = GUIUtil.makeLabel("" + greenValueNumber);
    greenValue.setToolTipText("Green color component slider");
    right.add(makeSlider(greenLabel, green, greenValue, "Green"));

    // blue slider
    blueLabel = GUIUtil.makeLabel("B");
    blue = new JSlider(0, 255, blueValueNumber);
    blueValue = GUIUtil.makeLabel("" + blueValueNumber);
    right.add(makeSlider(blueLabel, blue, blueValue, "Blue"));

    right.add(Box.createVerticalStrut(8));

    // panel for algorithm
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

    // algorithm label
    JLabel algorithmLabel = GUIUtil.makeLabel("Algorithm: ");
    algorithmLabel.setDisplayedMnemonic('l');
    String algorithmTip = "Resizing algorithm to use";
    algorithmLabel.setToolTipText(algorithmTip);
    p.add(algorithmLabel);

    // algorithm combo box
    algorithm = GUIUtil.makeComboBox(new String[] {
      "Smooth", "Standard", "Fast", "Replicate", "Area averaging"
    });
    algorithmLabel.setLabelFor(algorithm);
    algorithm.setToolTipText(algorithmTip);
    p.add(algorithm);
    // set the algorithm value from the preferences
    algorithm.setSelectedIndex(myPrefs.getIntegerPref
            (ThumbMakerPreferences.RESIZE_ALG_PREF_NAME));

    right.add(GUIUtil.pad(p));

    // panel for output format
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

    // format label
    JLabel formatLabel = GUIUtil.makeLabel("Format: ");
    formatLabel.setDisplayedMnemonic('f');
    String formatTip = "Thumbnail output format";
    formatLabel.setToolTipText(formatTip);
    p.add(formatLabel);

    // format combo box
    format = GUIUtil.makeComboBox(new String[] {"PNG", "JPG"});
    formatLabel.setLabelFor(format);
    format.setToolTipText(formatTip);
    p.add(format);
    // set the format value from the preferences
    format.setSelectedIndex(myPrefs.getIntegerPref
            (ThumbMakerPreferences.THUMB_FORMAT_PREF_NAME));

    right.add(GUIUtil.pad(p));
    right.add(Box.createVerticalStrut(5));

    // panel for prepend string
    p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));

    // prepend label
    JLabel prependLabel = GUIUtil.makeLabel("Prepend: ");
    prependLabel.setDisplayedMnemonic('e');
    String prependTip = "Starting string for each thumbnail filename";
    prependLabel.setToolTipText(prependTip);
    p.add(prependLabel);

    // prepend field
    prepend = GUIUtil.makeTextField(myPrefs.getStringPref
            (ThumbMakerPreferences.STRING_TO_PREPEND_PREF_NAME), 4);
    prependLabel.setLabelFor(prepend);
    prepend.setToolTipText(prependTip);
    p.add(prepend);

    p.add(Box.createHorizontalStrut(5));

    // append label
    JLabel appendLabel = GUIUtil.makeLabel("Append: ");
    appendLabel.setDisplayedMnemonic('a');
    String appendTip = "Ending string for each thumbnail filename";
    appendLabel.setToolTipText(appendTip);
    p.add(appendLabel);

    // append field
    append = GUIUtil.makeTextField(myPrefs.getStringPref
            (ThumbMakerPreferences.STRING_TO_APPEND_PREF_NAME), 4);
    appendLabel.setLabelFor(append);
    append.setToolTipText(appendTip);
    p.add(append);

    right.add(GUIUtil.pad(p));

    // vertical padding
    right.add(Box.createVerticalGlue());

    // bottom panel
    JPanel bottom = new JPanel();
    bottom.setLayout(new BoxLayout(bottom, BoxLayout.X_AXIS));
    pane.add(bottom);

    // output folder label
    JLabel outputLabel = GUIUtil.makeLabel("Output folder: ");
    outputLabel.setDisplayedMnemonic('o');
    String outputTip = "Thumbnail output folder";
    outputLabel.setToolTipText(outputTip);
    bottom.add(outputLabel);

    // output folder field
    String filePath = new File(myPrefs.getStringPref(
      ThumbMakerPreferences.FILE_PATH_STRING_PREF_NAME)).getAbsolutePath();
    output = GUIUtil.makeTextField(filePath, 8);
    outputLabel.setLabelFor(output);
    output.setToolTipText(outputTip);
    // start this in default and then lock down so "..." is used
    output.setEditable(false);
    output.setBackground(Color.LIGHT_GRAY);
    bottom.add(output);

    // add a file chooser button "..."
    dotDotDot = new JButton("...");
    dotDotDot.setMnemonic('.');
    dotDotDot.setToolTipText("Select destination directory.");
    dotDotDot.addActionListener(this);
    bottom.add(dotDotDot);

    right.add(GUIUtil.pad(p));

    setFromPreferences();
    addWindowListener(this);
  }

  // Methods that will handle saving and loading the preferences

  private void loadPreferences() {
    // grab the preferences so that they can be used to fill out the layout
    ThumbMakerPreferences myPreferences = ThumbMakerPreferences.getInstance();

    // x resolution text box
    xres.setText(myPreferences.getStringPref(
      ThumbMakerPreferences.RES_WIDTH_PREF_NAME));

    // y resolution text box
    yres.setText(myPreferences.getStringPref(
      ThumbMakerPreferences.RES_HEIGHT_PREF_NAME));

    // aspect ratio checkbox
    aspect.setSelected(myPreferences.getStringPref(
      ThumbMakerPreferences.DO_MAINTAIN_ASPECT_PREF_NAME).equalsIgnoreCase(
      ThumbMakerPreferences.BOOLEAN_TRUE_STRING));

    // load the color values from the preferences
    int redValueNumber = myPreferences.getIntegerPref(
      ThumbMakerPreferences.RED_VALUE_PREF_NAME);
    int greenValueNumber = myPreferences.getIntegerPref(
      ThumbMakerPreferences.GREEN_VALUE_PREF_NAME);
    int blueValueNumber = myPreferences.getIntegerPref(
      ThumbMakerPreferences.BLUE_VALUE_PREF_NAME);

    // set the background color image
    colorBox.setBackground(
      new Color(redValueNumber, greenValueNumber, blueValueNumber));

    // red slider
    red.setValue(redValueNumber);
    redValue.setText("" + redValueNumber);

    // green slider
    green.setValue(greenValueNumber);
    greenValue.setText("" + greenValueNumber);

    // blue slider
    blue.setValue(blueValueNumber);
    blueValue.setText("" + blueValueNumber);

    // algorithm combo box
    algorithm.setSelectedIndex(myPreferences.getIntegerPref(
      ThumbMakerPreferences.RESIZE_ALG_PREF_NAME));

    // format combo box
    format.setSelectedIndex(myPreferences.getIntegerPref(
      ThumbMakerPreferences.THUMB_FORMAT_PREF_NAME));

    // prepend field
    prepend.setText(myPreferences.getStringPref(
      ThumbMakerPreferences.STRING_TO_PREPEND_PREF_NAME));

    // append field
    append.setText(myPreferences.getStringPref(
      ThumbMakerPreferences.STRING_TO_APPEND_PREF_NAME));

    // output folder field
    output.setText((new File(myPreferences.getStringPref(
      ThumbMakerPreferences.FILE_PATH_STRING_PREF_NAME))).getAbsolutePath());

  }

  private void savePreferences() {
      // grab the preferences so that they can be filled in from the
      // user's selections
    ThumbMakerPreferences myPreferences = ThumbMakerPreferences.getInstance();

    // x resolution text box
    myPreferences.setStringPref(ThumbMakerPreferences.RES_WIDTH_PREF_NAME,
      xres.getText());

    // y resolution text box
    myPreferences.setStringPref(ThumbMakerPreferences.RES_HEIGHT_PREF_NAME,
      yres.getText());

    // aspect ratio checkbox
    String aspectText;
    if (aspect.isSelected()) {
      aspectText = ThumbMakerPreferences.BOOLEAN_TRUE_STRING;
    }
    else aspectText = ThumbMakerPreferences.BOOLEAN_FALSE_STRING;
    myPreferences.setStringPref(
      ThumbMakerPreferences.DO_MAINTAIN_ASPECT_PREF_NAME, aspectText);

    // red slider
    myPreferences.setIntegerPref(ThumbMakerPreferences.RED_VALUE_PREF_NAME,
      red.getValue());

    // green slider
    myPreferences.setIntegerPref(ThumbMakerPreferences.GREEN_VALUE_PREF_NAME,
      green.getValue());

    // blue slider
    myPreferences.setIntegerPref(ThumbMakerPreferences.BLUE_VALUE_PREF_NAME,
      blue.getValue());

    // algorithm combo box
    myPreferences.setIntegerPref(ThumbMakerPreferences.RESIZE_ALG_PREF_NAME,
      algorithm.getSelectedIndex());

    // format combo box
    myPreferences.setIntegerPref(ThumbMakerPreferences.THUMB_FORMAT_PREF_NAME,
      format.getSelectedIndex());

    // prepend field
    myPreferences.setStringPref(
      ThumbMakerPreferences.STRING_TO_PREPEND_PREF_NAME, prepend.getText());

    // append field
    myPreferences.setStringPref(
      ThumbMakerPreferences.STRING_TO_APPEND_PREF_NAME, append.getText());

    // output folder field
    myPreferences.setStringPref(
      ThumbMakerPreferences.FILE_PATH_STRING_PREF_NAME, output.getText());
  }

  // -- ActionListener API methods --

  /** Called for button presses and checkbox toggles. */
  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();

    // the source of the event is the add files button
    // so we need to let the user add some files!
    if (src == addFiles) {
      // create an appropriate file chooser
      JFileChooser myTempFileChooser = new JFileChooser
              ((new File("")).getAbsolutePath());
      myTempFileChooser.setMultiSelectionEnabled(true);
      myTempFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      myTempFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
      myTempFileChooser.setFileHidingEnabled(true);

      // open the file chooser and get the user's selctions
      int returnCode = myTempFileChooser.showOpenDialog(this);

      // use the return info to add files if the
      // user selected any in the file chooser
      if (returnCode == JFileChooser.APPROVE_OPTION) {
        // grab the files the user selected
        File [] selectedFiles = myTempFileChooser.getSelectedFiles();

        // pull the files into the list
        changeFilesInList.importFileData(list, selectedFiles);
      }
    }

    // the source of the event was the process button
    else if (src == process) {
      if (((DefaultListModel) list.getModel()).size() == 0) return;

      setComponentsEnabled(false);
      final ThumbMaker tm = this;
      Thread t = new Thread(new Runnable() {
        public void run() {
          tm.process();
          setComponentsEnabled(true);
        }
      });
      t.start();

      // because we can't do it on quit, we are going to save
      // all the preference values when the user processes a set
      // of images
      savePreferences();
    }

    // the source of the event was the remove button
    else if (src == remove) {
      DefaultListModel model = (DefaultListModel) list.getModel();
      while (true) {
        int ndx = list.getSelectedIndex();
        if (ndx < 0) break;
        model.removeElementAt(ndx);
      }
    }

    // the source of the event was the preserve aspect check box
    else if (src == aspect) {
      boolean b = aspect.isSelected();
      colorLabel.setEnabled(b);
      colorBox.setEnabled(b);
      redLabel.setEnabled(b);
      red.setEnabled(b);
      redValue.setEnabled(b);
      greenLabel.setEnabled(b);
      green.setEnabled(b);
      greenValue.setEnabled(b);
      blueLabel.setEnabled(b);
      blue.setEnabled(b);
      blueValue.setEnabled(b);
    }

    // the source of the event is the "..." button,
    // we need to let the user select a destination file
    else if (src == dotDotDot) {
      // create an appropriate file chooser
      JFileChooser myTempFileChooser = new JFileChooser(
        new File(output.getText()).getAbsolutePath());
      myTempFileChooser.setMultiSelectionEnabled(false);
      myTempFileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      myTempFileChooser.setDialogType(JFileChooser.OPEN_DIALOG);
      myTempFileChooser.setFileHidingEnabled(true);

      // open the file chooser and get the user's selctions
      int returnCode = myTempFileChooser.showOpenDialog(this);

      // use the return info to set the directory if the
      // user selected one in the file chooser
      if (returnCode == JFileChooser.APPROVE_OPTION) {
        // grab the file the user selected
        File selectedFile = myTempFileChooser.getSelectedFile();

        // stuff the file path into the text field
        output.setText(selectedFile.getAbsolutePath());
      }
    }
  }

  // -- ChangeListener API methods --

  /** Called when color slider values change. */
  public void stateChanged(ChangeEvent e) {
    JSlider slider = (JSlider) e.getSource();
    int val = slider.getValue();
    String s = "" + val;
    if (val < 100) s = "0" + s;
    if (val < 10) s = "0" + s;
    if (slider == red) redValue.setText(s);
    else if (slider == green) greenValue.setText(s);
    else if (slider == blue) blueValue.setText(s);
    Color c = new Color(red.getValue(), green.getValue(), blue.getValue());
    colorBox.setBackground(c);
  }

  // -- WindowListener API methods --

  /** Save preferences when window closes. */
  public void windowClosing(WindowEvent event) {
    saveToPreferences();
  }

  public void windowClosed(WindowEvent event) { }
  public void windowDeactivated(WindowEvent event) { }
  public void windowActivated(WindowEvent event) { }
  public void windowDeiconified(WindowEvent event) { }
  public void windowIconified(WindowEvent event) { }
  public void windowOpened(WindowEvent event) { }

  // -- Helper methods --

  private JPanel makeSlider(JLabel label,
    JSlider slider, JLabel value, String color)
  {
    JPanel p = new JPanel();
    p.setLayout(new BoxLayout(p, BoxLayout.X_AXIS));
    label.setDisplayedMnemonic(label.getText().charAt(0));
    label.setLabelFor(slider);
    String tip = color + " color component slider";
    label.setToolTipText(tip);
    slider.setToolTipText(tip);
    value.setToolTipText(tip);
    p.add(label);
    slider.setMaximumSize(new Dimension(128, slider.getMaximumSize().height));
    slider.addChangeListener(this);
    p.add(slider);
    p.add(value);
    return p;
  }

  private void setComponentsEnabled(boolean enabled) {
    list.setEnabled(enabled);
    process.setEnabled(enabled);
    remove.setEnabled(enabled);
    xres.setEnabled(enabled);
    yres.setEnabled(enabled);
    aspect.setEnabled(enabled);

    boolean b = aspect.isSelected() && enabled;
    colorLabel.setEnabled(b);
    colorBox.setEnabled(b);
    redLabel.setEnabled(b);
    red.setEnabled(b);
    redValue.setEnabled(b);
    greenLabel.setEnabled(b);
    green.setEnabled(b);
    greenValue.setEnabled(b);
    blueLabel.setEnabled(b);
    blue.setEnabled(b);
    blueValue.setEnabled(b);

    format.setEnabled(enabled);
    algorithm.setEnabled(enabled);
    prepend.setEnabled(enabled);
    append.setEnabled(enabled);
    output.setEnabled(enabled);
  }

  private void process() {
    int width = Integer.parseInt(xres.getText());
    int height = Integer.parseInt(yres.getText());
    boolean preserveAspect = aspect.isSelected();
    Color bg = new Color(red.getValue(), green.getValue(), blue.getValue());

    String outDir = output.getText();
    String suffix = ((String) format.getSelectedItem()).toLowerCase();
    String preText = prepend.getText();
    String appText = append.getText();

    int scaleType = -1;
    String alg = (String) algorithm.getSelectedItem();
    if (alg.equals("Smooth")) scaleType = Image.SCALE_SMOOTH;
    else if (alg.equals("Standard")) scaleType = Image.SCALE_DEFAULT;
    else if (alg.equals("Fast")) scaleType = Image.SCALE_FAST;
    else if (alg.equals("Replicate")) scaleType = Image.SCALE_REPLICATE;
    else if (alg.equals("Area averaging")) {
      scaleType = Image.SCALE_AREA_AVERAGING;
    }

    DefaultListModel model = (DefaultListModel) list.getModel();
    int size = model.size();
    progress.setValue(0);
    progress.setMaximum(4 * size);

    for (int i=0; i<size; i++) {
      ThumbFile tf = (ThumbFile) model.elementAt(i);
      list.setSelectedValue(tf, true);
      String tail = " (" + (i + 1) + " of " + size + ")";

      progress.setValue(4 * i);
      progress.setString("Reading" + tail);

      // construct input and output filenames
      String inFile = tf.getPath();
      String outFile = outDir + SLASH + tf.getName();
      int ndx = outFile.lastIndexOf(SLASH);
      String s1 = outFile.substring(0, ndx + SLASH.length());
      String s2 = outFile.substring(ndx + SLASH.length());
      int dot_ndx = s2.lastIndexOf(".");
      if (dot_ndx >= 0) s2 = s2.substring(0, dot_ndx);

      // make the thumbnail file name
      outFile = s1 + preText + s2 + appText + "." + suffix;

      // read in the file to an image
      BufferedImage image = null;
      try { image = ImageIO.read(new File(inFile)); }
      catch (IOException exc) {
        exc.printStackTrace();
      }

      progress.setValue(4 * i + 1);
      progress.setString("Resizing" + tail);

      // resize image
      int w, h;
      if (preserveAspect) {
        int ow = image.getWidth();
        int oh = image.getHeight();
        double oasp = (double) ow / oh;
        double tasp = (double) width / height;
        if (oasp > tasp) {
          w = width;
          h = (int) (w / oasp);
        }
        else {
          h = height;
          w = (int) (oasp * h);
        }
      }
      else {
        w = width;
        h = height;
      }
      Image resized = image.getScaledInstance(w, h, scaleType);

      progress.setValue(4 * i + 2);
      progress.setString("Painting" + tail);

      // create thumbnail
      BufferedImage thumb = new BufferedImage(
        width, height, BufferedImage.TYPE_INT_RGB);
      Graphics2D g2d = thumb.createGraphics();
      g2d.setColor(bg);
      g2d.fillRect(0, 0, width, height);
      g2d.drawImage(resized, (width - w) / 2, (height - h) / 2, this);
      g2d.dispose();

      progress.setValue(4 * i + 3);
      progress.setString("Writing" + tail);

      // save thumbnail to disk
      File out = new File(outFile);
      File parent = out.getParentFile();
      if (parent != null && !parent.exists()) parent.mkdirs();
      try { ImageIO.write(thumb, suffix, out); }
      catch (IOException exc) {
        exc.printStackTrace();
      }
    }

    list.setSelectedIndices(new int[0]);
    progress.setValue(4 * size);
    progress.setString("Complete");
  }

  // some preference related methods

  // set the visible values from the preferences file
  private void setFromPreferences() {
    System.out.println("setting stuff from preferences");
  }

  // save the current values to the preferences file
  private void saveToPreferences() {
    System.out.println("saving stuff to preferences");
  }

  // -- Main method --

  public static void main(String[] args) throws Exception {
    //JFrame.setDefaultLookAndFeelDecorated(true);
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    ThumbMaker tm = new ThumbMaker();
    tm.pack();

    // center window onscreen
    Dimension window = tm.getSize();
    Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
    int w = (screen.width - window.width) / 2;
    int h = (screen.height - window.height) / 2;
    tm.setLocation(w, h);

    tm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    tm.setVisible(true);
  }

}
