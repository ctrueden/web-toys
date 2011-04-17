//
// ThumbTransferHandler.java
//

package restless.thumbmaker;

import java.awt.datatransfer.*;
import java.io.*;
import java.util.List;
import javax.swing.*;

public class ThumbTransferHandler extends TransferHandler {

  private static final String SLASH = System.getProperty("file.separator");

  // -- ThumbTransferHandler API methods --

  public boolean importFileData(JComponent comp, File [] files) {
    JList list = (JList) comp;
    DefaultListModel model = (DefaultListModel) list.getModel();

    // get all files and recurse if needed
    for (int i=0; i<files.length; i++) {
      File file = (File) files[i];
      if (file.isDirectory()) add(model, "", file);
      else add(model, file.getParent(), file);
    }
    return true;
  }

  // -- TransferHandler API methods --

  public boolean importData(JComponent c, Transferable t) {
    if (!canImport(c, t.getTransferDataFlavors())) return false;
    List files = null;
    try {
      files = (List) t.getTransferData(DataFlavor.javaFileListFlavor);
    }
    catch (UnsupportedFlavorException exc) { exc.printStackTrace(); }
    catch (IOException exc) { exc.printStackTrace(); }
    JList list = (JList) c;
    DefaultListModel model = (DefaultListModel) list.getModel();
    for (int i=0; i<files.size(); i++) {
      File file = (File) files.get(i);
      if (file.isDirectory()) add(model, "", file);
      else add(model, file.getParent(), file);
    }
    return true;
  }

  public boolean canImport(JComponent c, DataFlavor[] flavors) {
    if (!(c instanceof JList)) return false;
    JList list = (JList) c;
    ListModel model = list.getModel();
    if (!(model instanceof DefaultListModel)) return false;
    for (int i=0; i<flavors.length; i++) {
      if (DataFlavor.javaFileListFlavor.equals(flavors[i])) return true;
    }
    return false;
  }


  // -- Helper methods --

  protected void add(DefaultListModel model, String dir, File file) {
    if (file.isDirectory()) {
      if (dir.equals("")) dir = file.getParent();
      File[] files = file.listFiles();
      for (int i=0; i<files.length; i++) add(model, dir, files[i]);
    }
    else {
      String name = file.getPath().substring(dir.length());
      if (name.startsWith(SLASH)) name = name.substring(SLASH.length());
      int ndx = name.lastIndexOf(".");
      if (ndx < 0) return;
      String ext = name.substring(ndx + 1).toLowerCase();
      if (!ext.equals("gif") && !ext.equals("jpg") &&
        !ext.equals("jpeg") && !ext.equals("png"))
      {
        return;
      }
      model.addElement(new ThumbFile(dir, name));
    }
  }

}
