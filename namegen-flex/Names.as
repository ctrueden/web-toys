//
// Names.as
//

// NameGen Flex style, coded in 2007 by Curtis Rueden
// This code is dedicated to the public domain.
// All copyrights (and warranties) are disclaimed.

// A class for generating random names based on existing ones.
package {

  public class Names {

    // -- Fields --

    // Counters for valid N-tuples.
    protected var counts:Array;

    // Sums of valid N-tuple counts, for computing probability.
    protected var sums:Array;

    // Tuple width for generation algorithm.
    protected var width:int;

    // Maximum length of generated names.
    protected var max:int;

    // Number of names used as input.
    protected var num:int;

    private var first:int = "a".charCodeAt(0);
    private var brace:int = "{".charCodeAt(0);

    // -- Constructor --

    // Constructs a names object capable of generating random names.
    // The algorithm constructs a name based on names from the given source
    // file. The width parameter controls how similar to the existing names
    // the generated names are. The length parameter controls the maximum
    // length of each generated name.
    public function Names(names:Array, width:int, length:int) {
      this.width = width;
      this.max = length;
      var size:int = 1;
      for (var i:int = 0; i < width; i++) size *= 27;
      counts = new Array(size);
      for (i = 0; i < counts.length; i++) counts[i] = 0;
      sums = new Array(size / 27);
      for (i = 0; i < sums.length; i++) sums[i] = 0;
      num = 0;

      var prefix:String = "";
      for (i = 0; i < width - 1; i++) prefix += "{";
      for each (var name:String in names) {
        name = prefix + name.toLowerCase() + "{";
        num++;

        // compile tuples
        for (i = width; i <= name.length; i++) {
          var s:String = name.substring(i - width, i);
          counts[index(s)]++;
          sums[index(s.substring(0, s.length - 1))]++;
        }
      }
    }

    // -- Names API methods --

    // Generates the given number of random names.
    public function generate(num:int):Array {
      var names:Array = new Array(num);
      for (var n:int = 0; n < num; n++) names[n] = generateName();
      return names;
    }

    // Generates a random name.
    public function generateName():String {
      var s:String = "";
      for (var i:int = 0; i < width - 1; i++) s += "{";
      while (s.length < max + width + 1) {
        var tuple:String = s.substring(s.length - width + 1);
        var sum:int = sums[index(tuple)];
        var ci:int = sum * Math.random();
        var c:int = first - 1;
        do {
          c++;
          var ndx:int = index(tuple + String.fromCharCode(c));
          var q:int = counts[ndx];
          ci -= q;
        }
        while (ci >= 0 && c < brace);
        if (c == brace) break;
        s += String.fromCharCode(c);
      }
      return s.substring(width - 1);
    }

    // Gets the number of names used for input.
    public function get numberOfNames():int { return num; }

    // -- Helper methods --

    // Converts an N-tuple to an index.
    protected function index(s:String):int {
      var ndx:int = 0;
      for (var j:int = 0; j < s.length; j++) {
        ndx *= 27;
        ndx += s.charCodeAt(j) - first;
      }
      return ndx;
    }

  }

}
