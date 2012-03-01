= ANT Mass Minify =

Provides a custom ANT task that allows a user to scan a directory
and minify all the JavaScript and CSS found. The user may optionally
combine all each JS file or CSS file in a given directory into a single
consolidated file. The user may also choose to recurse down the directory
tree.

Most likely your JavaScript files will have dependancies on other JavaScript
files, and thus needs to be included in a particular order. If you opt to
combine or consolidate your JavaScript this rule still applies.
As such you may use the embedded <order> element to define the order of 
compressed JS files. Please note that you do *not* have to specify
the order of all your JS files. As an example assume we have 5 JS files.
If you specify the order for two of them as 1 and 2, then those two will be included
first, then the remaining will be placed at the end in the order they were
processed.

As another example assume we have 10 JS files. If you specify the order for two
of them as 1 and 2, and another two as 4 and 5, then the remaining 6 will
be placed *in the first gap*. That means they will be placed at position 3,
after the first two, and before the last two.

Example:
	> <massminify dir="/some/path/here/1" minifyjs="true" minifycss="true" combinecss="all-css.min.css" />
	> <massminify dir="/some/path/here/2" minifyjs="true" combinejs="all-js.min.js" recurse="true" />
	> <massminify dir="/some/path/here/3" minifyjs="true" consolidatejs="all-js-everywhere.min.js" recurse="true" />
	> <massminify dir="/some/path/here/4" minifyjs="true" consolidatejs="all-js-everywhere.min.js" recurse="true">
	>    <order file="library.js" position="1" />
	>    <order file="file2.js" position="2" />
	>    <order file="last.js" position="4" />
	> </massminify>
