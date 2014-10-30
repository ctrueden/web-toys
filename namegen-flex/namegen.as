//
// namegen.as
//

// NameGen Flex style, coded in 2007 by Curtis Rueden
// This code is dedicated to the public domain.
// All copyrights (and warranties) are disclaimed.

// possible values for presets combo box
[Bindable]
public var presetValues:Array = [
  {label:"male", data:1}, 
  {label:"female", data:2},
  {label:"surnames", data:3}
];

public var names:Names;

include "male.as";
include "female.as";
include "last.as";

public function initNames():void {
  var source:Array;
  if (presets.selectedItem.label == "male") source = male;
  else if (presets.selectedItem.label == "female") source = female;
  else if (presets.selectedItem.label == "surnames") source = last;

  var width:int = 0;
  if (deviant.selected) width = 2;
  else if (balanced.selected) width = 3;
  else if (similar.selected) width = 4;

  names = new Names(source, width, 25);

  output.text += "[" + names.numberOfNames + " names]\n\n";
}

public function genNames():void {
  var num:int = number.value;
  var ns:Array = names.generate(num);
  for each (var n:String in ns) output.text += n + "\n";
  output.text += "\n";
  callLater(scrollToBottom); // ensure component is up-to-date
}

public function clearNames():void {
  output.text = "";
}

private function scrollToBottom():void {
  output.verticalScrollPosition = output.maxVerticalScrollPosition;
}
