//
// ThumbMakerPreferences.java
//

package restless.thumbmaker;

import java.util.Properties;
import java.util.prefs.Preferences;

/**
 * This class handles the preferences of the thumbmaker so that
 * changes are remembered and the user does not need to re-set
 * the values when reloading the program.
 *
 * Note: This class sort of uses the Singleton pattern.
 *
 * @author Eva Schiffer
 */
public class ThumbMakerPreferences {

  // The following set of strings specify the names that will be used
  // as keys to store and retrieve the program preferences.
  //
  // Note: These strings all have names ending in _NAME.

  /** resolution height */
  public static final String RES_HEIGHT_PREF_NAME = "resolution height";

  /** resolution width */
  public static final String RES_WIDTH_PREF_NAME = "resolution width";

  /** maintain aspect? */
  public static final String DO_MAINTAIN_ASPECT_PREF_NAME =
    "do maintain aspect ratio";

  /** red color value */
  public static final String RED_VALUE_PREF_NAME = "red color value";

  /** green color value */
  public static final String GREEN_VALUE_PREF_NAME = "green color value";

  /** blue color value */
  public static final String BLUE_VALUE_PREF_NAME = "blue color value";

  /** resize algorithm */
  public static final String RESIZE_ALG_PREF_NAME = "resize algorithm";

  /** thumb format */
  public static final String THUMB_FORMAT_PREF_NAME = "thumb format type";

  /** prepend string */
  public static final String STRING_TO_PREPEND_PREF_NAME = "prepend string";

  /** append string */
  public static final String STRING_TO_APPEND_PREF_NAME = "append string";

  /**
   * A constant which describes the file path string;
   * i.e., the key for the file path.
   */
  public static final String FILE_PATH_STRING_PREF_NAME = "file path string";

  /** the string value of true for our boolean values. */
  public static final String BOOLEAN_TRUE_STRING = "true";

  /** the string value of false for our boolean values. */
  public static final String BOOLEAN_FALSE_STRING = "false";

  /** Our singleton instance. **/
  private static ThumbMakerPreferences myInstance = null;

  /** The singleton's preferences object. **/
  private Preferences myPrefsObject = null;

  /** A set of defaults we will keep just in case. **/
  private Properties myDefaultValues = null;

  /** The method used to get the only instance of this class. */
  public static synchronized ThumbMakerPreferences getInstance() {
    if (myInstance == null) myInstance = new ThumbMakerPreferences();
    return myInstance;
  }

  /** Load preferences and initialize our default values. */
  private ThumbMakerPreferences() {
    // initialize our internal objects
    myPrefsObject = Preferences.userNodeForPackage(ThumbMakerPreferences.class);
    myDefaultValues = getDefaults();
  }

  /**
   * Get a set of property values which represents the default
   * preference values.
   */
  private static Properties getDefaults() {
    Properties toReturn = new Properties();

    // fill in the various default properties

    // resolution height
                toReturn.setProperty(RES_HEIGHT_PREF_NAME, "64");
    //toReturn.setProperty(RES_HEIGHT_PREF_NAME, "100");

    // resolution width
    toReturn.setProperty(RES_WIDTH_PREF_NAME, "64");
    //toReturn.setProperty(RES_WIDTH_PREF_NAME, "100");

    // maintain aspect?
                toReturn.setProperty(DO_MAINTAIN_ASPECT_PREF_NAME, BOOLEAN_TRUE_STRING);
    //toReturn.setProperty(DO_MAINTAIN_ASPECT_PREF_NAME, BOOLEAN_TRUE_STRING);

    // red color value
                toReturn.setProperty(RED_VALUE_PREF_NAME, "255");
    //toReturn.setProperty(RED_VALUE_PREF_NAME, "0");

    // green color value
                toReturn.setProperty(GREEN_VALUE_PREF_NAME, "255");
    //toReturn.setProperty(GREEN_VALUE_PREF_NAME, "0");

    // blue color value
                toReturn.setProperty(BLUE_VALUE_PREF_NAME, "255");
    //toReturn.setProperty(BLUE_VALUE_PREF_NAME, "0");

    // resize algorithm
                toReturn.setProperty(RESIZE_ALG_PREF_NAME, "0");
    //toReturn.setProperty(RESIZE_ALG_PREF_NAME, "0");

    // thumb format
                toReturn.setProperty(THUMB_FORMAT_PREF_NAME, "0");
    //toReturn.setProperty(THUMB_FORMAT_PREF_NAME, "1");

    // prepend string
                toReturn.setProperty(STRING_TO_PREPEND_PREF_NAME, "");
    //toReturn.setProperty(STRING_TO_PREPEND_PREF_NAME, "thumb_");

    // append string
                toReturn.setProperty(STRING_TO_APPEND_PREF_NAME, "_tn");
    //toReturn.setProperty(STRING_TO_APPEND_PREF_NAME, "");

    // output folder path
                // Note: If this property is set to "" the current
                // directory will be used.
                toReturn.setProperty(FILE_PATH_STRING_PREF_NAME, "");
                //toReturn.setProperty(FILE_PATH_STRING_PREF_NAME, "/thumbs/");

    return toReturn;
  }

  /** This method will return an integer preference. */
  public int getIntegerPref(String prefKey) {
    return myPrefsObject.getInt(prefKey,
      Integer.parseInt(myDefaultValues.getProperty(prefKey)));
  }

  /** This method will set an integer preference. */
  public void setIntegerPref(String prefKey, int prefValue) {
    myPrefsObject.putInt(prefKey, prefValue);
  }

  /** This method will return the value of a String preference. */
  public String getStringPref(String prefKey) {
    return myPrefsObject.get(prefKey,
      myDefaultValues.getProperty(prefKey));
  }

  /** This method will set a String preference. */
  public void setStringPref (String prefKey, String prefValue) {
      myPrefsObject.put(prefKey, prefValue);
  }

}
