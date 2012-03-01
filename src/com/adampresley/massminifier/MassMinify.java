package com.adampresley.massminifier;

import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;
import org.apache.commons.io.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;


/**
 * Class: MassMinify
 * Provides a custom ANT task that allows a user to scan a directory
 * and minify all the JavaScript and CSS found. The user may optionally
 * combine all each JS file or CSS file in a given directory into a single
 * consolidated file. The user may also choose to recurse down the directory
 * tree.
 *
 * Most likely your JavaScript files will have dependancies on other JavaScript
 * files, and thus needs to be included in a particular order. If you opt to
 * combine or consolidate your JavaScript this rule still applies.
 *
 * As such you may use the embedded <order> element to define the order of 
 * compressed JS files. Please note that you do *not* have to specify
 * the order of all your JS files. As an example assume we have 5 JS files.
 * If you specify the order for two of them as 1 and 2, then those two will be included
 * first, then the remaining will be placed at the end in the order they were
 * processed.
 *
 * As another example assume we have 10 JS files. If you specify the order for two
 * of them as 1 and 2, and another two as 4 and 5, then the remaining 6 will
 * be placed *in the first gap*. That means they will be placed at position 3,
 * after the first two, and before the last two.
 *
 * 
 * Author:
 * 	Adam Presley
 *
 * Extends:
 * 	org.apache.tools.ant.Task
 *
 * Properties:
 * 	dir - Source/starting directory to scan
 * 	recurse - True/false to recurse down the directory tree from the source
 * 	minifyjs - True/false to minify JavaScript files
 * 	combinejs - File name to combine all JS in a *single* directory into. Cannot be used with consolidatejs
 * 	consolidatejs - File name to combine all JS in *ALL* directories into (useful for recursing). Cannot be used with combinejs
 * 	minifycss - True/false to minify CSS files
 * 	combinecss - File name to combine all CSS in a *single* directory into. Cannot be used with consolidatecss
 * 	consolidatecss - File name to combine all CSS in *ALL* directories into (useful for recursing). Cannot be used with combinecss
 *
 * Example:
 * 	> <massminify dir="/some/path/here/1" minifyjs="true" minifycss="true" combinecss="all-css.min.css" />
 * 	> <massminify dir="/some/path/here/2" minifyjs="true" combinejs="all-js.min.js" recurse="true" />
 * 	> <massminify dir="/some/path/here/3" minifyjs="true" consolidatejs="all-js-everywhere.min.js" recurse="true" />
 * 	> <massminify dir="/some/path/here/4" minifyjs="true" consolidatejs="all-js-everywhere.min.js" recurse="true">
 * 	>    <order file="library.js" position="1" />
 * 	>    <order file="file2.js" position="2" />
 * 	>    <order file="last.js" position="4" />
 * 	> </massminify>
 */
public class MassMinify extends Task
{
	private String __dir = "";
	private boolean __recurse = false;
	
	private boolean __minifyJs = false;
	private String __combineJs = "";
	private String __consolidateJs = "";

	private boolean __minifyCss = false;
	private String __combineCss = "";
	private String __consolidateCss = "";

	private TreeSet<Order> __orders = new TreeSet<Order>(ORDER_ORDER);
	private TreeSet<SortableFile> __files = new TreeSet<SortableFile>(SORTABLEFILE_ORDER);
	private TreeSet<SortableFile> __unsortedFiles = new TreeSet<SortableFile>(SORTABLEFILE_ORDER);
	
	private Hashtable<String, List<File>> __combinedJsFiles;

	/************************************************************************
		Public Methods
	************************************************************************/
	@Override
	public void execute() throws BuildException {
		System.out.println("Initializing mass minify...");

		File dirHandle = new File(this.__dir);
		this.__validate();

		System.out.println("Beginning execution. Prepare for minification");
		
		System.out.println("dir = " + this.__dir);
		System.out.println("recurse = " + this.__recurse);
		System.out.println("minifyjs = " + this.__minifyJs);
		System.out.println("combinejs = " + this.__combineJs);
		System.out.println("consolidatejs = " + this.__consolidateJs);
		System.out.println("minifycss = " + this.__minifyCss);
		System.out.println("combinecss = " + this.__combineCss);
		System.out.println("consolidatecss = " + this.__consolidateCss);

		try {
			this.__getFileListing(dirHandle);
		}
		catch (FileNotFoundException fnfe) {
			log("Uh oh, something bad happened while getting the files: " + fnfe.getMessage());
			return;
		}

		__adjustFilePosition();

		for (SortableFile sf : this.__files) {
			log("sorted: " + sf.toString());
		}


		/*
		 * Loop and create the minified JS files
		 */
		if (this.__consolidateJs.length() > 0 || this.__combineJs.length() > 0) {
			if (this.__consolidateJs.length() > 0) {
				
			}

			if (this.__combineJs.length() > 0) {
				for (SortableFile sf : this.__files) {
					
				}
			}
		}
		else {
			for (SortableFile sf : this.__files) {
				String oldFilename = sf.getFile().toString();

				if (FilenameUtils.getExtension(oldFilename).compareTo("js") == 0) {
					String newFilename = FilenameUtils.getFullPath(oldFilename) + FilenameUtils.getBaseName(oldFilename) + ".min.js";
					File newFile = new File(newFilename);

					BufferedReader in = null;
					Writer out = null;

					try {
						in = new BufferedReader(new FileReader(sf.getFile()));
						out = new BufferedWriter(new FileWriter(newFile));

						JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {
							public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
								if (line < 0) {
									System.err.println("\n[WARNING] " + message);
								} 
								else {
									System.err.println("\n[WARNING] " + line + ':' + lineOffset + ':' + message);
								}
							}

							public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
								if (line < 0) {
									System.err.println("\n[ERROR] " + message);
								} 
								else {
									System.err.println("\n[ERROR] " + line + ':' + lineOffset + ':' + message);
								}
							}

							public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
								error(message, sourceName, line, lineSource, lineOffset);
								return new EvaluatorException(message);
							}
						});

						in.close();
						in = null;

						compressor.compress(out, 3000, true, true, true, false);
					}
					catch (Exception e) {
						log (e.getMessage());
					}
					finally {
						if (in != null) {
							try {
								in.close();
								in = null;
							}
							catch (IOException ioe) {
								log(ioe.getMessage());
							}
						}

						if (out != null) {
							try {
								out.close();
								out = null;
							}
							catch (IOException ioe) {
								log(ioe.getMessage());
							}
						}
					}
				}
			} // for
		}
	}


	/************************************************************************
		Setters
	************************************************************************/

	/**
	 * Function: setDir
	 * Setter for the *dir* attribute
	 * 
	 * Author:
	 * 	Adam Presley
	 *
	 * Visibility:
	 * 	public
	 *
	 * Parameters:
	 * 	value - Source/starting directory to scan
	 */
	public void setDir(String value) {
		this.__dir = value;
	}


	/**
	 * Function: setRecurse
	 * Setter for the *recurse* attribute
	 * 
	 * Author:
	 * 	Adam Presley
	 *
	 * Visibility:
	 * 	public
	 *
	 * Parameters:
	 * 	value - True/false to recurse down the directory tree from the source
	 */
	public void setRecurse(boolean value) {
		this.__recurse = value;
	}


	/**
	 * Function: setMinifyjs
	 * Setter for the *minifyjs* attribute
	 * 
	 * Author:
	 * 	Adam Presley
	 *
	 * Visibility:
	 * 	public
	 *
	 * Parameters:
	 * 	value - True/false to minify JavaScript files
	 */
	public void setMinifyjs(boolean value) {
		this.__minifyJs = value;
	}


	/**
	 * Function: setCombinejs
	 * Setter for the *combinejs* attribute
	 * 
	 * Author:
	 * 	Adam Presley
	 *
	 * Visibility:
	 * 	public
	 *
	 * Parameters:
	 * 	value - File name to combine all JS in a *single* directory into. Cannot be used with consolidatejs
	 */
	public void setCombinejs(String value) {
		this.__combineJs = value;
	}


	/**
	 * Function: setConsolidatejs
	 * Setter for the *consolidatejs* attribute
	 * 
	 * Author:
	 * 	Adam Presley
	 *
	 * Visibility:
	 * 	public
	 *
	 * Parameters:
	 * 	value - File name to combine all JS in *ALL* directories into (useful for recursing). Cannot be used with combinejs
	 */
	public void setConsolidatejs(String value) {
		this.__consolidateJs = value;
	}


	/**
	 * Function: setMinifycss
	 * Setter for the *minifycss* attribute
	 * 
	 * Author:
	 * 	Adam Presley
	 *
	 * Visibility:
	 * 	public
	 *
	 * Parameters:
	 * 	value - True/false to minify CSS files
	 */
	public void setMinifycss(boolean value) {
		this.__minifyCss = value;
	}


	/**
	 * Function: setCombinecss
	 * Setter for the *combinecss* attribute
	 * 
	 * Author:
	 * 	Adam Presley
	 *
	 * Visibility:
	 * 	public
	 *
	 * Parameters:
	 * 	value - File name to combine all CSS in a *single* directory into. Cannot be used with consolidatecss
	 */
	public void setCombinecss(String value) {
		this.__combineCss = value;
	}


	/**
	 * Function: setConsolidatecss
	 * Setter for the *consolidatecss* attribute
	 * 
	 * Author:
	 * 	Adam Presley
	 *
	 * Visibility:
	 * 	public
	 *
	 * Parameters:
	 * 	value - File name to combine all CSS in *ALL* directories into (useful for recursing). Cannot be used with combinecss
	 */
	public void setConsolidatecss(String value) {
		this.__consolidateCss = value;
	}


	/************************************************************************
		Private Methods
	************************************************************************/

	/**
	 * Function: __validate
	 * Validates all properties sent to this task to ensure they make sense. Certain
	 * properties cannot be set together, for example.
	 * 
	 * Author:
	 * 	Adam Presley
	 *
	 * Visibility:
	 * 	private
	 */
	private void __validate() {
		if (this.__dir.trim() == "") throw new BuildException("Please provide a valid source directory to scan");
		if (!this.__minifyJs && !this.__minifyCss) throw new BuildException("You must choose to minify either JavaScript or CSS. Pick one please");
		if (this.__combineJs.length() > 0 && this.__consolidateJs.length() > 0) throw new BuildException("You cannot choose to both combine and consolidate JavaScript. Pick one or the other please");
		if (this.__combineCss.length() > 0 && this.__consolidateCss.length() > 0) throw new BuildException("You cannot choose to both combine and consolidate CSS. Pick one or the other please");
	}

	private int __getFileOrder(File file) {
		for (Order o : this.__orders) {
			if (o.fileMatches(file)) {
				return o.getPosition();
			}
		}

		return 0;
	}

	private void __adjustFilePosition() {
		if (this.__files.size() > 0) {
			SortableFile first = this.__files.first();
			int nextGap = first.getPosition();

			/*
			 * Find a place to inject the unordered items
			 */
			for (SortableFile sf : this.__files) {
				if (nextGap == sf.getPosition()) {
					nextGap++;
				}
			}

			/*
			 * Now inject them
			 */
			for (SortableFile sf : this.__unsortedFiles) {
				sf.setPosition(nextGap);
			}

			if (this.__unsortedFiles.size() > 0) {
				this.__files.addAll(this.__unsortedFiles);
			}
		}
	}

	private void __getFileListing(File startingDirectory) throws FileNotFoundException {
		this.__validateDirectory(startingDirectory);
		this.__getFileListingRecurse(startingDirectory);
	}

	private void __getFileListingRecurse(File startingDirectory) throws FileNotFoundException {
		List<File> files = Arrays.asList(startingDirectory.listFiles());
		
		for (File f : files) {
			if (!f.isDirectory()) {
				int position = this.__getFileOrder(f);
				String extension = FilenameUtils.getExtension(f.toString());
				SortableFile sf = new SortableFile(f, position);

				if ((extension.compareTo("js") == 0 && this.__minifyJs) || (extension.compareTo("css") == 0 && this.__minifyCss)) {
					if (position > 0)
						this.__files.add(sf);
					else
						this.__unsortedFiles.add(sf);
				}
			}

			if (!f.isFile()) {
				this.__getFileListingRecurse(f);
			}
		}
	}	

	private void __validateDirectory(File directory) throws FileNotFoundException {
		if (directory == null) {
			throw new IllegalArgumentException("Directory should never be null");
		}

		if (!directory.exists()) {
			throw new FileNotFoundException("Directory does not exist: " + directory);
		}

		if (!directory.isDirectory()) {
			throw new IllegalArgumentException(directory + " is not a directory");
		}

		if (!directory.canRead()) {
			throw new IllegalArgumentException(directory + " cannot be read");
		}
	}


	/************************************************************************
		Nested Elements
	************************************************************************/
	public Order createOrder() {
		Order order = new Order();

		this.__orders.add(order);
		return order;
	}

	static final Comparator<Order> ORDER_ORDER = new Comparator<Order>() {
		public int compare(Order a, Order b) {
			return b.compareTo(a);
		}
	};

	public class Order implements Comparable<Order> {
		private String __file = "";
		private int __position = 0;
		private Pattern __pattern = null;

		public Order() {}

		public void setFile(String value) {
			this.__file = value;
			this.__pattern = Pattern.compile(value);
		}

		public String getFile() {
			return this.__file;
		}

		public void setPosition(int value) {
			this.__position = value;
		}

		public int getPosition() {
			return this.__position;
		}

		public boolean fileMatches(File file) {
			Matcher m = this.__pattern.matcher(file.toString());
			return m.find();
		}

		@Override
		public int compareTo(Order a) {
			String comparisonA = a.getPosition() + "-" + a.getFile();
			String comparisonB = this.__position + "-" + this.__file;

			return comparisonA.compareTo(comparisonB);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Order))
				return false;

			Order o = (Order) obj;
			return (o.getFile().equals(this.__file) && o.getPosition() == this.__position);
		}
	}


	static final Comparator<SortableFile> SORTABLEFILE_ORDER = new Comparator<SortableFile>() {
		public int compare(SortableFile a, SortableFile b) {
			return b.compareTo(a);
		}
	};


	public class SortableFile implements Comparable<SortableFile> {
		private File __file = null;
		private int __position = 0;

		public SortableFile() {}
		public SortableFile(File file, int position) {
			this.__file = file;
			this.__position = position;
		}

		public void setFile(File value) {
			this.__file = value;
		}

		public File getFile() {
			return this.__file;
		}

		public void setPosition(int value) {
			this.__position = value;
		}

		public int getPosition() {
			return this.__position;
		}


		@Override
		public int compareTo(SortableFile a) {
			int positionCompare = new Integer(a.getPosition()).compareTo(new Integer(this.__position));

			if (positionCompare != 0) {
				return positionCompare;
			}
			else {
				return a.getFile().compareTo(this.__file);
			}
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof SortableFile))
				return false;

			SortableFile f = (SortableFile) obj;
			return (f.getFile().equals(this.__file) && f.getPosition() == this.__position);
		}

		@Override
		public String toString() {
			return this.__file + " at position " + this.__position;
		}
	}
}