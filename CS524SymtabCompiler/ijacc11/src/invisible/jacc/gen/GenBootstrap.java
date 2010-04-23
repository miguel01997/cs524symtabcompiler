// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.gen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;

import invisible.jacc.parse.ErrorOutput;
import invisible.jacc.parse.ErrorOutputStream;
import invisible.jacc.parse.SyntaxException;
import invisible.jacc.parse.ParserTable;
import invisible.jacc.parse.ScannerTable;


/*->

  GenBootstrap contains the code to generate the scanner and parser tables
  that are used by ParserGenerator.

  For phase 1 bootstrap, execute class invisible.jacc.gen.GenBootstrap with
  the following command line:

		-v -d invisible\jacc\gen -1

  For phase 2 bootstrap, execute class invisible.jacc.gen.GenBootstrap with
  the following command line:

		-v -d invisible\jacc\gen -2

  Each phase generates Java source files JaccGrammarScannerTable.java and
  JaccGrammarParserTable.java.  Therefore, code must be recompiled after each
  phase.

  The directory on the command line is the directory where invisible.jacc.gen
  is located.

->*/


public class GenBootstrap 
{

	// Generate scanner and parser tables.
	//
	// A command-line parameter of -v can be used to select verbose mode.
	//
	// A command-line parameter of -d, followed by a space, followed by a
	// directory name, specifies the directory where input files are located
	// and output files are written.  (Otherwise, the current directory is
	// used.)
	//
	// A command-line parameter of -1 selects bootstrap phase 1, where the
	// grammar specification is generated internally by ParserGenerator.
	//
	// A command-line parameter of -2 selects bootstrap phase 2, where the
	// grammar specification is read from JaccGrammar.jacc.
	//
	// Exactly one of -1 and -2 must be specified.  You must run -1 and then
	// recompile the source before running -2.  Each phase generates Java source
	// code for JaccGrammarScannerTable and JaccGrammarParserTable.
	//
	// Each phase only needs to be run once, unless the grammar specification
	// is changed.

	public static void main (String[] args) throws IOException, SyntaxException
	{

		// Assume not verbose mode.

		boolean verbose = false;

		// Bootstrap phase

		int phase = 0;

		// Directory name

		String dirname = null;

		// Scan for command-line parameters

		for (int i = 0; i < args.length; ++i)
		{

			// If verbose mode ...

			if (args[i].equalsIgnoreCase ("-v"))
			{
				verbose = true;
			}

			// If working directory ...

			else if (args[i].equalsIgnoreCase ("-d") && ((i+1) < args.length))
			{
				dirname = args[i+1];

				// Skip the directory name argument

				++i;
			}

			// If phase 1 ...

			else if (args[i].equalsIgnoreCase ("-1"))
			{
				phase = 1;
			}

			// If phase 2 ...

			else if (args[i].equalsIgnoreCase ("-2"))
			{
				phase = 2;
			}

			// Otherwise, an invalid argument ...

			else
			{
				System.out.println ("Invalid command-line argument '" + args[i] + "'.");
				return;
			}
		}

		// The ErrorOutput object is the destination of error messages.

		ErrorOutput errOut;

		// The ParserGenerator object to hold the grammar specification.

		ParserGenerator PG;

		// Read the grammar specification

		switch (phase)
		{

		case 1:

			// Phase 1 bootstrap

			System.out.println ("Reading grammar specification for phase 1 ...");

			// Write error messages to JaccBoot.out

			errOut = new ErrorOutputStream (new PrintStream (
				new FileOutputStream (new File (dirname, "JaccBoot.out"))), null );

			// Generate bootstrap grammar specification

			PG = ParserGeneratorBootstrap.bootstrap (errOut, verbose);

			break;


		case 2:

			// Phase 2 bootstrap

			System.out.println ("Reading grammar specification for phase 2 ...");

			// Write error messages to JaccGrammar.out

			errOut = new ErrorOutputStream (new PrintStream (
				new FileOutputStream (new File (dirname, "JaccGrammar.out"))), null );

			// Create an InputStream connected to our grammar specification.

			InputStream stream = new FileInputStream (
				new File (dirname, "JaccGrammar.jacc") );

			// Read the grammar specification.  The first argument is the
			// destination for error messages;  the second argument is a boolean
			// that selects verbose mode;  the third argument is the input;  and
			// the fourth argument is a string used in error messages.  The return
			// value is true if an error occurred.

			PG = new ParserGenerator ();

			boolean error = PG.generate (errOut, verbose, stream, "JaccGrammar.jacc", null);

			// If there was an error, print a final message and return.

			if (error)
			{
				System.out.println ("Error reading grammar specification.");
				return;
			}

			break;


		default:

			// No phase specified

			System.out.println ("Must specify either -1 or -2 on command line.");
			return;
		}
		
		// Get the Java name and split it into package and class
		
		String javaName = PG.getJavaName();
		
		if (javaName == null)
		{
			System.out.println ("No Java name in grammar specification.");
			return;
		}
		
		int javaQual = javaName.lastIndexOf ('.');
		
		String javaClass = javaName.substring (javaQual + 1);
		
		String javaPackage = ((javaQual < 0) ? "" : javaName.substring (0, javaQual));

		// Generate our scanner table.

		System.out.println ("Generating scanner table ...");

		ScannerTable ST = PG.makeScannerTable ();

		// If it was successful, write the Java source file.  In the call to
		// writeToJavaSource, the first argument is a PrintStream to which the
		// source is written, the second argument is the package name, the
		// third argument is the class name, and the fourth argument is a
		// boolean that is true to enable run-length-encoding of the tables.

		if (ST != null)
		{
			ST.writeToJavaSource (
				new PrintStream (new FileOutputStream (
					new File (dirname, javaClass + "ScannerTable.java") )),
				javaPackage,
				javaClass + "ScannerTable",
				true );
		}

		// Otherwise, print an error message.

		else
		{
			System.out.println ("Error generating scanner table.");
			return;
		}

		// Generate our parser table.

		System.out.println ("Generating parser table ...");

		ParserTable PT = PG.makeParserTable ();

		// If it was successful, write the Java source file.  In the call to
		// writeToJavaSource, the first argument is a PrintStream to which the
		// source is written, the second argument is the package name, the
		// third argument is the class name, and the fourth argument is a
		// boolean that is true to enable run-length-encoding of the tables.

		if (PT != null)
		{
			PT.writeToJavaSource (
				new PrintStream (new FileOutputStream (
					new File (dirname, javaClass + "ParserTable.java") )),
				javaPackage,
				javaClass + "ParserTable",
				true );
		}

		// Otherwise, print an error message.

		else
		{
			System.out.println ("Error generating parser table.");
			return;
		}

		// All done.

		System.out.println ("All tables generated successfully.");

		return;
	}


}

