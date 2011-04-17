// Codebreaker.java

// Codebreaker - a mini-game from Revelations: Persona's arcade.
// Permission is granted to use this code for whatever you want.

// Applet written Sunday, 15 June 2003 by Curtis Rueden.
// Applet revised Friday, 25 July 2003.
// Scaling support added Sunday, 08 February 2004.
// Quote scrolling support added Sunday, 22 February 2004.

package restless.codebreaker;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

/** Main Codebreaker applet class. */
public class Codebreaker extends Applet
  implements KeyListener, MouseListener, Runnable
{

  // -- Constants --

  /** Flag indicating applet is still loading. */
  private static final int STATUS_LOADING = 1;

  /** Flag indicating game has not yet been started. */
  private static final int STATUS_NO_GAME = 2;

  /** Flag indicating game is in progress. */
  private static final int STATUS_IN_GAME = 3;

  /** Flag indicating game is finished. */
  private static final int STATUS_GAME_OVER = 4;

  /** Character names. */
  private static final String[] CHAR_NAMES = {
    "chris", "main", "mark", "mary", "nate"
  };

  /** Character quotes. */
  private static final String[][] CHAR_QUOTES = {
    {"Turn 1",
     "Turn 2",
     "Turn 3",
     "Turn 4",
     "Failure"},
    {"@#&%$@$%!!!",
     "...!!!",
     "...!",
     "...",
     "............"},
    {"Turn 1",
     "Turn 2",
     "Turn 3",
     "Turn 4",
     "Failure"},
    {"Turn 1",
     "Turn 2",
     "Turn 3",
     "Turn 4",
     "Failure"},
    {"Turn 1",
     "Turn 2",
     "Stupid people can be happy when ruled by smart people.",
     "If they waste precious time, they can never become No. 1!",
     "Shut up Mark, you stupid pest!"}
  };

  /** Speed at which quotes scroll. */
  private static final int QUOTE_SPEED = 1;

  /** Delay in milliseconds between each scroll iteration. */
  private static final int QUOTE_DELAY = 48;


  // -- Scaling fields --

  /** Applet size. */
  private Dimension dim;

  /** X scale factor. */
  private double scaleX;

  /** Y scale factor. */
  private double scaleY;


  // -- Double buffering fields --

  /** Offscreen image for double buffering. */
  private Image offscreen;

  /** Offscreen buffer for double buffering. */
  private Graphics buffer;


  // -- Images --

  /** "Now loading" image. */
  private Image loading;

  /** Background image. */
  private Image bg;

  /** "Check" button. */
  private Image buttonCheck;

  /** "Exit" button. */
  private Image buttonExit;

  /** "Start" button. */
  private Image buttonStart;

  /** "Bingo!" message. */
  private Image msgBingo;

  /** "Insert card" message. */
  private Image msgInsertCard;

  /** "Make the code" message. */
  private Image msgMakeTheCode;

  /** Selection cursor. */
  private Image selector;

  /** Character quote background section. */
  private Image quoteBg;

  /** Tiny blue numbers for result grids. */
  private Image[] tbn;

  /** "Chosen" numbers for 1-9 listing above main grid. */
  private Image[] chosen;

  /** Blue (locked in) numbers for main grid. */
  private Image[] gridB;

  /** Red (normal) numbers for main grid. */
  private Image[] gridR;

  /** Tiny yellow (highlighted) numbers for result grids. */
  private Image[] tyn;

  /** Small numbers indicating results on main grid. */
  private Image[] res;

  /** Tiny green numbers for result grids (signifying "close"). */
  private Image[] tgn;

  /** Tiny red numbers for result grids (signifying "correct"). */
  private Image[] trn;

  /** Lit yellow turn numbers for result grids. */
  private Image[] lit;

  /** Turn listings (1st, 2nd, 3rd and 4th). */
  private Image[] turn;

  /** Character images. */
  private Image[] chars;


  // -- Game state fields

  /** Solution to the current game. */
  private int[][] solution;

  /** Current grid values. */
  private int[][] grid;

  /** Whether each number on the grid is blue (versus red). */
  private boolean[][] blue;

  /** How many numbers in each column are "correct." */
  private int[] colCorrect;

  /** How many numbers in each row are "correct." */
  private int[] rowCorrect;

  /** How many numbers in each column are "close." */
  private int[] colClose;

  /** How many numbers in each row are "close." */
  private int[] rowClose;

  /** Archived grid values from previous turns. */
  private int[][][] oldGrids;

  /** Archived "correct" column values from previous turns. */
  private int[][] oldColCorrect;

  /** Archived "correct" row values from previous turns. */
  private int[][] oldRowCorrect;

  /** Archived "close" column values from previous turns. */
  private int[][] oldColClose;

  /** Archived "close" row values from previous turns. */
  private int[][] oldRowClose;

  /** Selected grid box column. */
  private int selCol;

  /** Selected grid box row. */
  private int selRow;

  /** Current turn number. */
  private int turnNo;


  // -- Character quote fields --

  /** Character number of character being displayed (it's random). */
  private int charNo;

  /** Character quote currently being displayed. */
  private String quote;

  /** Font used for character quote. */
  private Font quoteFont;

  /** Width in pixels of character quote. */
  private int quoteWidth;

  /** Character quote display offset. */
  private int quoteOffset;

  /** Character quote scrolling direction. */
  private boolean quoteDir;


  // -- Other fields --

  /** Media tracker for loading images. */
  private MediaTracker tracker;

  /** Base URL for applet's images. */
  private URL base;

  /** Id counter for MediaTracker images. */
  private int id = 0;

  /** Applet status flag. */
  private int status;

  /** Flag indicating applet still needs to load images. */
  private boolean needsLoading;

  /** Synchronization object for ensuring multiple threads behave properly. */
  private Object threadLock;


  // -- Applet API methods --

  /** Initializes the applet. */
  public void init() {
    // initialize game state variables
    solution = new int[3][3];
    grid = new int[3][3];
    blue = new boolean[3][3];
    colCorrect = new int[3];
    colClose = new int[3];
    rowCorrect = new int[3];
    rowClose = new int[3];
    oldGrids = new int[4][3][3];
    oldGrids = new int[4][3][3];
    oldColCorrect = new int[4][3];
    oldColClose = new int[4][3];
    oldRowCorrect = new int[4][3];
    oldRowClose = new int[4][3];
    quote = "";
    clearState();

    // determine scaling
    dim = getSize();
    scaleX = (double) dim.width / 320;
    scaleY = (double) dim.height / 240;
    quoteFont = new Font("SansSerif", Font.PLAIN, (int) (scaleX * 12));

    // set up double buffering
    setBackground(Color.black);
    offscreen = createImage(dim.width, dim.height);
    buffer = offscreen.getGraphics();

    // create media tracker
    tracker = new MediaTracker(this);
    base = null;
    try { base = getDocumentBase(); }
    catch (Exception exc) { exc.printStackTrace(); }

    // fully load "loading" image
    loading = doImage("loading.jpg");
    status = STATUS_LOADING;

    // start new thread to finish loading the images
    threadLock = new Object();
    synchronized (threadLock) {
      needsLoading = true;
      new Thread(this).start();
      try { threadLock.wait(); }
      catch (InterruptedException exc) { }
    }

    // start new thread to handle character quote scrolling
    new Thread(this).start();
  }

  /** Draws the applet. */
  public void paint(Graphics g) {
    buffer.clearRect(0, 0, dim.width, dim.height);

    // draw into offscreen buffer
    if (status == STATUS_LOADING) drawLoading(buffer);
    else drawBoard(buffer);

    // flush buffer to screen
    draw(g, offscreen, 0, 0);
  }

  /** Required for double buffering to work properly. */
  public void update(Graphics g) { paint(g); }


  // -- KeyListener API methods --

  /** Key was pressed. */
  public void keyPressed(KeyEvent e) {
    int code = e.getKeyCode();
    if (code == KeyEvent.VK_ENTER) doStartCheck();
    else if (code == KeyEvent.VK_ESCAPE) exitGame();
    if (status != STATUS_IN_GAME) return;
    if (code == KeyEvent.VK_UP) {
      // move selector up
      selRow--;
      if (selRow < 0) selRow = 2;
    }
    else if (code == KeyEvent.VK_DOWN) {
      // move selector down
      selRow++;
      if (selRow > 2) selRow = 0;
    }
    else if (code == KeyEvent.VK_LEFT) {
      // move selector to the left
      selCol--;
      if (selCol < 0) selCol = 2;
    }
    else if (code == KeyEvent.VK_RIGHT) {
      // move selector to the right
      selCol++;
      if (selCol > 2) selCol = 0;
    }
    else if (code == KeyEvent.VK_SPACE) {
      // toggle grid number color between red and blue
      blue[selCol][selRow] = !blue[selCol][selRow];
    }
    else {
      int num = -1;
      if (code == KeyEvent.VK_NUMPAD1 || code == KeyEvent.VK_1) num = 0;
      else if (code == KeyEvent.VK_NUMPAD2 || code == KeyEvent.VK_2) num = 1;
      else if (code == KeyEvent.VK_NUMPAD3 || code == KeyEvent.VK_3) num = 2;
      else if (code == KeyEvent.VK_NUMPAD4 || code == KeyEvent.VK_4) num = 3;
      else if (code == KeyEvent.VK_NUMPAD5 || code == KeyEvent.VK_5) num = 4;
      else if (code == KeyEvent.VK_NUMPAD6 || code == KeyEvent.VK_6) num = 5;
      else if (code == KeyEvent.VK_NUMPAD7 || code == KeyEvent.VK_7) num = 6;
      else if (code == KeyEvent.VK_NUMPAD8 || code == KeyEvent.VK_8) num = 7;
      else if (code == KeyEvent.VK_NUMPAD9 || code == KeyEvent.VK_9) num = 8;
      if (num >= 0 && !blue[selCol][selRow]) grid[selCol][selRow] = num;
      else return;
    }
    repaint();
  }

  /** Key was released. */
  public void keyReleased(KeyEvent e) { }

  /** Key was typed. */
  public void keyTyped(KeyEvent e) { }


  // -- MouseListener API methods --

  /** Mouse button was pressed. */
  public void mousePressed(MouseEvent e) {
    int mod = e.getModifiers();
    boolean left = (mod & InputEvent.BUTTON1_MASK) != 0;
    boolean right = (mod & InputEvent.BUTTON3_MASK) != 0;
    if (!left && !right) return;
    Point p = e.getPoint();

    if (status == STATUS_IN_GAME) {
      // detect mouse press on main grid numbers
      for (int col=0; col<3; col++) for (int row=0; row<3; row++) {
        if (inside(32 * col + 17, 32 * row + 97, 30, 30, p)) {
          if (left) {
            // select given grid box
            selCol = col;
            selRow = row;
            repaint();
          }
          else if (right) {
            // toggle grid number color between red and blue
            blue[col][row] = !blue[col][row];
            repaint();
          }
        }
      }
    }

    // detect mouse press on start/check button
    if (left && inside(224, 16, 48, 16, p)) doStartCheck();

    // detect mouse press on exit button
    if (left && inside(48, 16, 48, 16, p)) exitGame();
  }

  /** Mouse button was released. */
  public void mouseReleased(MouseEvent e) { }

  /** Mouse button was clicked. */
  public void mouseClicked(MouseEvent e) { }

  /** Mouse pointer entered the area. */
  public void mouseEntered(MouseEvent e) { }

  /** Mouse pointer exited the area. */
  public void mouseExited(MouseEvent e) { }


  // -- Runnable API methods --

  /** Loads images in a separate thread. */
  public void run() {
    if (needsLoading) {
      synchronized (threadLock) {
        needsLoading = false;
        threadLock.notifyAll();
      }
      loadImages();
    }
    else scrollQuote();
  }


  // -- Helper methods --

  /** Loads necessary images. */
  private void loadImages() {
    bg = doImage("bg.jpg");

    buttonCheck = doImage("button_check.gif");
    buttonExit = doImage("button_exit.gif");
    buttonStart = doImage("button_start.gif");

    msgBingo = doImage("msg_bingo.gif");
    msgInsertCard = doImage("msg_insert_card.gif");
    msgMakeTheCode = doImage("msg_make_the_code.gif");

    selector = doImage("selector.gif");
    quoteBg = doImage("quote_bg.gif");

    tbn = new Image[9];
    chosen = new Image[9];
    gridB = new Image[9];
    gridR = new Image[9];
    tyn = new Image[9];
    for (int i=0; i<9; i++) {
      String ext = "" + (i + 1) + ".gif";
      tbn[i] = doImage("b" + ext);
      chosen[i] = doImage("chosen" + ext);
      gridB[i] = doImage("grid_b" + ext);
      gridR[i] = doImage("grid_r" + ext);
      tyn[i] = doImage("y" + ext);
    }
    res = new Image[4];
    tgn = new Image[4];
    trn = new Image[4];
    lit = new Image[4];
    turn = new Image[4];
    for (int i=0; i<4; i++) {
      String ext = "" + i + ".gif";
      res[i] = doImage("res" + ext);
      tgn[i] = doImage("g" + ext);
      trn[i] = doImage("r" + ext);
      ext = "" + (i + 1) + ".gif";
      lit[i] = doImage("lit" + ext);
      turn[i] = doImage("turn" + ext);
    }

    chars = new Image[CHAR_NAMES.length];
    for (int i=0; i<CHAR_NAMES.length; i++) {
      chars[i] = doImage("char_" + CHAR_NAMES[i] + ".gif");
    }

    addKeyListener(this);
    addMouseListener(this);
    status = STATUS_NO_GAME;
    repaint();
  }

  /** Scrolls character quote. */
  private void scrollQuote() {
    while (true) {
      if (status == STATUS_GAME_OVER) doQuote();
      try { Thread.sleep(QUOTE_DELAY); }
      catch (InterruptedException exc) { }
    }
  }

  /** Displays the game board. */
  private void drawBoard(Graphics g) {
    // background
    draw(g, bg, 0, 0);

    if (status == STATUS_IN_GAME || status == STATUS_GAME_OVER) {
      // exit button
      draw(g, buttonExit, 48, 16);
    }

    if (status == STATUS_NO_GAME || status == STATUS_GAME_OVER) {
      // start button
      draw(g, buttonStart, 224, 16);
    }
    else if (status == STATUS_IN_GAME) {
      // check button
      draw(g, buttonCheck, 224, 16);
    }

    // message
    Image msg = null;
    if (status == STATUS_NO_GAME) msg = msgInsertCard;
    else if (status == STATUS_IN_GAME) msg = msgMakeTheCode;
    else if (status == STATUS_GAME_OVER) {
      msg = turnNo < 3 ? msgBingo : msgInsertCard;
    }
    int w = (int) (scaleX * 80 - msg.getWidth(this) / 2);
    g.drawImage(msg, w, (int) (scaleY * 41), this);

    if (status == STATUS_IN_GAME) {
      // chosen numbers
      boolean[] chose = new boolean[9];
      for (int col=0; col<3; col++) for (int row=0; row<3; row++) {
        int ndx = grid[col][row];
        chose[ndx] = true;
      }
      for (int num=0; num<9; num++) {
        if (chose[num]) draw(g, chosen[num], 16 * num + 9, 73);
      }
    }

    // main grid numbers
    for (int col=0; col<3; col++) for (int row=0; row<3; row++) {
      int ndx = grid[col][row];
      int x = 32 * col + 17;
      int y = 32 * row + 97;
      draw(g, blue[col][row] ? gridB[ndx] : gridR[ndx], x, y);
    }

    // main result numbers - "correct"
    for (int col=0; col<3; col++) {
      int ndx = colCorrect[col];
      if (ndx >= 0) draw(g, res[ndx], 32 * col + 28, 194);
    }
    for (int row=0; row<3; row++) {
      int ndx = rowCorrect[row];
      if (ndx >= 0) draw(g, res[ndx], 115, 32 * row + 107);
    }

    // main result numbers - "close"
    for (int col=0; col<3; col++) {
      int ndx = colClose[col];
      if (ndx >= 0) draw(g, res[ndx], 32 * col + 28, 210);
    }
    for (int row=0; row<3; row++) {
      int ndx = rowClose[row];
      if (ndx >= 0) draw(g, res[ndx], 131, 32 * row + 107);
    }

    // selector
    if (status == STATUS_IN_GAME) {
      int x = 32 * selCol + 11;
      int y = 32 * selRow + 90;
      draw(g, selector, x, y);
    }

    // result grids
    final int[] offsetX = {169, 217, 265, 169};
    final int[] offsetY = {57, 57, 57, 105};
    for (int t=0; t<4; t++) {
      if (t > turnNo) break;

      // result grid numbers
      for (int col=0; col<3; col++) for (int row=0; row<3; row++) {
        int ndx = oldGrids[t][col][row];
        if (ndx >= 0) {
          int x = 8 * col + offsetX[t];
          int y = 8 * row + offsetY[t];
          draw(g, tbn[ndx], x, y);
        }
      }

      // result grid - "correct"
      for (int col=0; col<3; col++) {
        int ndx = oldColCorrect[t][col];
        if (ndx >= 0) {
          int x = 8 * col + offsetX[t] + 2;
          int y = 24 + offsetY[t] + 1;
          draw(g, trn[ndx], x, y);
        }
      }
      for (int row=0; row<3; row++) {
        int ndx = oldRowCorrect[t][row];
        if (ndx >= 0) {
          int x = 24 + offsetX[t] + 2;
          int y = 8 * row + offsetY[t] + 1;
          draw(g, trn[ndx], x, y);
        }
      }

      // result grid - "close"
      for (int col=0; col<3; col++) {
        int ndx = oldColClose[t][col];
        if (ndx >= 0) {
          int x = 8 * col + offsetX[t] + 2;
          int y = 32 + offsetY[t] + 1;
          draw(g, tgn[ndx], x, y);
        }
      }
      for (int row=0; row<3; row++) {
        int ndx = oldRowClose[t][row];
        if (ndx >= 0) {
          int x = 32 + offsetX[t] + 2;
          int y = 8 * row + offsetY[t] + 1;
          draw(g, tgn[ndx], x, y);
        }
      }

      // result grid - turn
      draw(g, lit[t], 25 + offsetX[t], 25 + offsetY[t]);
    }

    // turn
    int ndx = turnNo >= turn.length ? turn.length - 1 : turnNo;
    draw(g, turn[ndx], 129, 210);

    if (status == STATUS_GAME_OVER) {
      // character
      draw(g, chars[charNo], 216, 103);

      // character quote
      String name = CHAR_NAMES[charNo].substring(0, 1).toUpperCase() +
        CHAR_NAMES[charNo].substring(1);
      quote = name + ": " + CHAR_QUOTES[charNo][turnNo];
      FontMetrics fm = getFontMetrics(new Font("SansSerif", Font.PLAIN, 12));
      quoteWidth = fm.stringWidth(quote);
    }
  }

  /** Displays the "Now loading" message. */
  private void drawLoading(Graphics g) {
    int x = (dim.width - loading.getWidth(this)) / 2;
    int y = (dim.height - loading.getHeight(this)) / 2;
    g.drawImage(loading, x, y, this);
  }

  /** Clears the game state. */
  private void clearState() {
    // randomly generate a new solution
    boolean[] chose = new boolean[9];
    for (int col=0; col<3; col++) for (int row=0; row<3; row++) {
      int q;
      do { q = (int) (9 * Math.random()); }
      while (chose[q]);
      solution[col][row] = q;
      chose[q] = true;
    }

    // clear game state
    for (int col=0; col<3; col++) for (int row=0; row<3; row++) {
      grid[col][row] = 0;
      blue[col][row] = false;
      for (int t=0; t<4; t++) oldGrids[t][col][row] = -1;
    }
    for (int col=0; col<3; col++) {
      colCorrect[col] = -1;
      colClose[col] = -1;
    }
    for (int row=0; row<3; row++) {
      rowCorrect[row] = -1;
      rowClose[row] = -1;
    }
    for (int t=0; t<4; t++) {
      for (int col=0; col<3; col++) {
        oldColCorrect[t][col] = -1;
        oldColClose[t][col] = -1;
      }
      for (int row=0; row<3; row++) {
        oldRowCorrect[t][row] = -1;
        oldRowClose[t][row] = -1;
      }
    }
    turnNo = 0;
  }

  /** Begins loading the given image. */
  private Image doImage(String name) {
    URL resource = getClass().getResource(name);
    Image img = load(getImage(resource));
    int w = (int) (scaleX * img.getWidth(this));
    int h = (int) (scaleY * img.getHeight(this));
    img = load(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
    return img;
  }

  /** Computes accuracy of user's guess. */
  private void doStartCheck() {
    if (status == STATUS_NO_GAME || status == STATUS_GAME_OVER) {
      // start new game
      status = STATUS_IN_GAME;
      clearState();
      repaint();
    }
    else if (status == STATUS_IN_GAME) {
      // check current code
      boolean winner = true;
      for (int col=0; col<3; col++) {
        colCorrect[col] = 0;
        colClose[col] = 0;
        for (int row=0; row<3; row++) {
          if (solution[col][row] == grid[col][row]) colCorrect[col]++;
          else for (int r2=0; r2<3; r2++) {
            if (solution[col][row] == grid[col][r2]) {
              colClose[col]++;
              break;
            }
          }
        }
        if (colCorrect[col] < 3) winner = false;
      }
      for (int row=0; row<3; row++) {
        rowCorrect[row] = 0;
        rowClose[row] = 0;
        for (int col=0; col<3; col++) {
          if (solution[col][row] == grid[col][row]) rowCorrect[row]++;
          else for (int c2=0; c2<3; c2++) {
            if (solution[col][row] == grid[c2][row]) {
              rowClose[row]++;
              break;
            }
          }
        }
        if (rowCorrect[row] < 3) winner = false;
      }

      // copy turn result into old grid
      for (int col=0; col<3; col++) for (int row=0; row<3; row++) {
        oldGrids[turnNo][col][row] = grid[col][row];
      }
      for (int col=0; col<3; col++) {
        oldColCorrect[turnNo][col] = colCorrect[col];
        oldColClose[turnNo][col] = colClose[col];
      }
      for (int row=0; row<3; row++) {
        oldRowCorrect[turnNo][row] = rowCorrect[row];
        oldRowClose[turnNo][row] = rowClose[row];
      }

      if (!winner) turnNo++;
      if (winner || turnNo == 4) {
        status = STATUS_GAME_OVER;
        charNo = (int) (CHAR_NAMES.length * Math.random());

        // set grid values to correct answer
        for (int col=0; col<3; col++) for (int row=0; row<3; row++) {
          grid[col][row] = solution[col][row];
          blue[col][row] = true;
        }
      }
      repaint();
    }
  }

  /** Draws the scrolling character quote. */
  private void doQuote() {
    Image image = createImage((int) (scaleX * 152), (int) (scaleY * 14));
    if (image == null) return;
    Graphics g = image.getGraphics();
    g.drawImage(quoteBg, 0, 0, this);
    g.setColor(Color.white);
    g.setFont(quoteFont);
    draw(g, quote, quoteOffset, 11);
    if (quoteDir) { // scroll left
      if (quoteOffset <= 148 - quoteWidth) quoteDir = false;
      else quoteOffset -= QUOTE_SPEED;
    }
    else { // scroll right
      if (quoteOffset >= 0) quoteDir = true;
      else quoteOffset += QUOTE_SPEED;
    }
    g.dispose();
    g = getGraphics();
    draw(g, image, 160, 209);
    g.dispose();
  }

  /** Quits the current game. */
  private void exitGame() {
    if (status != STATUS_IN_GAME && status != STATUS_GAME_OVER) return;
    status = STATUS_NO_GAME;
    clearState();
    repaint();
  }

  /** Tests whether the given point is inside the specified rectangle. */
  private boolean inside(int x, int y, int w, int h, Point p) {
    x *= scaleX;
    y *= scaleY;
    w *= scaleX;
    h *= scaleY;
    return new Rectangle(x, y, w, h).contains(p);
  }

  /** Draws the given image at the specified location. */
  private void draw(Graphics g, Image img, int x, int y) {
    g.drawImage(img, (int) (scaleX * x), (int) (scaleY * y), this);
  }

  /** Draws the given string at the specified location. */
  private void draw(Graphics g, String str, int x, int y) {
    g.drawString(str, (int) (scaleX * x), (int) (scaleY * y));
  }

  /** Loads an image completely before proceeding. */
  private Image load(Image img) {
    if (img == null) return null;
    tracker.addImage(img, ++id);
    try { tracker.waitForID(id); }
    catch (InterruptedException exc) { exc.printStackTrace(); }
    return img;
  }

}
