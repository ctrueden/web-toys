//
// ThumbFile.java
//

package restless.thumbmaker;

public class ThumbFile {

  // -- Constants --

  private static final String SLASH = System.getProperty("file.separator");


  // -- Fields --

  private String dir;
  private String name;


  // -- Constructor --

  public ThumbFile(String dir, String name) {
    this.dir = dir;
    this.name = name;
  }


  // -- ThumbFile API methods --

  public String getBaseDir() { return dir; }
  public String getName() { return name; }
  public String getPath() { return dir + SLASH + name; }


  // -- Object API methods --

  public String toString() { return getName(); }

}
