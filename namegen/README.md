This name generator outputs random names using an algorithm based on [Markov
chains](http://en.wikipedia.org/wiki/Markov_chains). It works by generating
tokens of a fixed length based on an input dictionary—in our case, a list of
names.

This Java Web Start application asks for full system privileges, but the only
reason it needs them is so that you can specify a custom dictionary text file
from your file system, rather than merely using the built-in male, female and
surname presets. If granting this sort of access bothers you, feel free to use
the [Flex version](name-generator-flex-style) instead.

I am not special. This program has been written many times in many languages
for many purposes. Here are a couple other random text generators that use
Markov chains:

* [Random Name Generator](http://www.fourteenminutes.com/fun/names/)
  with the same input data as mine.
* [Yet Another Fantasy NAme Generator](http://dicelog.com/yafnagen),
  accessible via SOAP and XML-RPC.

I didn't find any other versions in Java or Flex, so maybe my implementations
are not wholly redundant, but I didn't look very hard.

There are many other name generators out there that work on other principles,
as well:

* [These](http://www.xtra-rant.com/gennames/)
  [two](http://www.fakenamegenerator.com/)
  just pick a random name from a dictionary.
* [This one](http://www.rinkworks.com/namegen/)
  is template-driven.
* [Here](http://www.behindthename.com/random/)
  [are](http://arf.noemata.net/nbng/)
  [a few](http://www.seventhsanctum.com/index-name.php)
  whose inner workings are not immediately evident.

Here is a detailed description of the algorithm:

1. First, we choose the token size. A good length for words that tend toward
   uniqueness while still sounding like names is 3. A choice of 2 will result
   in more "deviant" names, while 4 will be more similar (often identical) to
   actual names in the dictionary.
2. For each name in the dictionary, we add all three-letter substrings to our
   tokens list. For example, for the name `Curtis` we would add tokens `cur`,
   `urt`, `rti` and `tis`. We also prepend a "beginning of name" character `^`
   and append an "end of name" character `$`, meaning that `^cu` and `is$` also
   go into our list.

Once we have processed the entire input dictionary, we have a nice list of
"name-like" substrings to use as the basis for generating our own names:

1. Pick a random token that begins with the "beginning of name" character `^`,
   saving the characters to our string buffer.
2. Pick a random token beginning with the last two characters in our string
   buffer, appending its last character to the string buffer.
3. Repeat step 2 until we pick a token ending in the "end of name" character
   `$`.

Note that the algorithm can generate names of arbitrary length, so if you are
reimplementing the algorithm yourself, you may wish to impose a maximum length
cut-off, or even discard results that get too long and start over.
