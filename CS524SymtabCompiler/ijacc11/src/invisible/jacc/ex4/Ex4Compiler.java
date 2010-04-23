// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.ex4;

import java.util.Enumeration;
import java.util.Hashtable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;

import invisible.jacc.parse.ErrorOutput;
import invisible.jacc.parse.InternalCompilerException;
import invisible.jacc.parse.Prescanner;
import invisible.jacc.parse.PrescannerJavaSource;
import invisible.jacc.parse.PrescannerJavaSourceClient;
import invisible.jacc.parse.Scanner;
import invisible.jacc.parse.ScannerClient;
import invisible.jacc.parse.ScannerTable;
import invisible.jacc.parse.SyntaxException;
import invisible.jacc.parse.Token;
import invisible.jacc.parse.TokenFactory;
import invisible.jacc.parse.TokenStream;


/*->

  Ex4Compiler is an example compiler, using the Jacc scanner.

  This compiler implements the language defined in Ex4Grammar.jacc.  It scans
  a Java source file and extracts all the identifiers.  Then it prints a list
  of the identifiers, together with the number of times each identifier appears
  in the source file.

  This compiler only uses the scanner, not the parser.  That's because the
  scanner by itself is powerful enough to do this job.  So, this example
  demonstrates how to use the scanner by itself.

  This example also demonstrates the use of PrescannerJavaSource, the
  prescanner for Java source files.

  Once an Ex4Compiler object is constructed, it may be used to compile any
  number of source files.


  CREATING THE SCANNER TABLE
  
  There are two ways to create the scanner table:  you can use the graphical
  interface, or you can use the command line.
  
  To use the graphical interface, execute class invisible.jacc.gen.GenGUI.
  When the window appears, enter invisible\jacc\ex4\Ex4Grammar.jacc as the
  file name.  Check the boxes to "generate scanner table", "create Java source
  files", and "write output messages to file".  Then, click on "Generate".
  
  To use the command line, execute class invisible.jacc.gen.GenMain with the
  following command line:
  
        -o -s -j invisible\jacc\ex4\Ex4Grammar.jacc
  
  In either case, Invisible Jacc creates Ex4GrammarScannerTable.java.
  
  Note:  On Microsoft Windows, the filename is not case-sensitive.  On other
  operating systems, the filename may or may not be case-sensitive depending
  on the operating system.  (If you're using another operating system,
  substitute the appropriate path separator character in place of backslash.)
  
  Note:  If you omit the ".jacc" extension from the filename, Invisible Jacc
  supplies the ".jacc" extension automatically.
  
  
  RUNNING THE SAMPLE INPUT
  
  To run the sample input, execute class invisible.jacc.ex4.Ex4Main with the
  following command line:
  
 		invisible\jacc\ex4\Ex4Input.txt
  
  File Ex4Input.out contains the result of running the above command.

->*/


public class Ex4Compiler implements ScannerClient, PrescannerJavaSourceClient 
{


	// ----- Per-Compiler Variables -----
	//
	// These variables are initialized when the compiler object is constructed.


	// This flag enables the use of debug token and nonterminal factories.

	static final boolean _debug = false;

	// The ErrorOutput object that is used as the destination for error
	// messages.

	ErrorOutput _errOut;

	// The standard output object that is used as the destination for program
	// output.

	PrintStream _stdOut;

	// The compiler's scanner table.

	ScannerTable _scannerTable;

	// The condition number for "notInComment".

	int _conditionNotInComment;

	// The condition number for "inComment".

	int _conditionInComment;




	// ----- Per-Compilation Variables -----
	//
	// These variables are initialized at the start of each compilation.


	// This flag is true if an error occurred during the compilation

	boolean _error;

	// The scanner used for this compilation.

	Scanner _scanner;

	// This Hashtable holds the identifiers.  Each key is a String giving the
	// name of an identifier.  Each element is an Integer giving the number of
	// times the identifier appeared.

	Hashtable _identifiers;




	// ----- Internal Compiler Functions -----




	// Issue an error message at the specified position.  The Token object
	// provides the position information.  The first String is an error code,
	// and the second String is an error message.  Either String can be null.
	//
	// This function also sets the _error flag.

	void reportError (Token token, String code, String message)
	{

		// Set error flag

		_error = true;

		// Report the error
		
		_errOut.reportError (ErrorOutput.typeError, null, token.file,
			token.line, token.column, code, message );

		return;
	}




	// Issue an error message with no position information.  The first String
	// is an error code, and the second String is an error message.  Either
	// String can be null.
	//
	// This function also sets the _error flag.

	void reportError (String code, String message)
	{

		// Set error flag

		_error = true;

		// Report the error
		
		_errOut.reportError (ErrorOutput.typeError, null, null,
			ErrorOutput.noPosition, ErrorOutput.noPosition, code, message );

		return;
	}




	// Issue a warning message at the specified position.  The Token object
	// provides the position information.  The first String is a warning code,
	// and the second String is a warning message.  Either String can be null.

	void reportWarning (Token token, String code, String message)
	{

		// Report the warning
		
		_errOut.reportError (ErrorOutput.typeWarning, null, token.file,
			token.line, token.column, code, message );

		return;
	}




	// Issue a warning message with no position information.  The first String
	// is a warning code, and the second String is a warning message.  Either
	// String can be null.

	void reportWarning (String code, String message)
	{

		// Report the warning
		
		_errOut.reportError (ErrorOutput.typeWarning, null, null,
			ErrorOutput.noPosition, ErrorOutput.noPosition, code, message );

		return;
	}




	// Given a filename, this function creates a Scanner object for scanning
	// the file.  The return value is null if the file could not be opened.

	Scanner makeScanner (String filename)
	{

		// We need to catch I/O exceptions

		try
		{
		
			// Open the file and attach it to an InputStream

			InputStream stream = new FileInputStream (filename);

			// Make an input source for the scanner

			Prescanner scannerSource = new PrescannerJavaSource (this, stream, 2000);

			// Create our scanner

			Scanner scanner = Scanner.makeScanner (
				this, scannerSource, _scannerTable, filename, 1, 1, 4000, null );

			// Return the scanner we created

			return scanner;
		}

		// If an I/O exception occurs, return null to indicate failure

		catch (IOException e)
		{
			return null;
		}
	}




	// ----- Implementation of ScannerClient Interface -----




	// The scanner calls this routine when it reaches end-of-file.
	//
	// Implements the scannerEOF() method of ScannerClient.

	public void scannerEOF (Scanner scanner, Token token)
	{

		// If we are in the middle of a comment ...

		if (scanner.condition() == _conditionInComment)
		{

			// Report a run-on comment error

			reportError (token, null, "Run-on comment." );
		}

		return;
	}




	// The scanner calls this routine when it cannot match a token.
	//
	// Implements the scannerUnmatchedToken() method of ScannerClient.

	public void scannerUnmatchedToken (Scanner scanner, Token token)
	{

		// Report the error

		reportError (token, null, "Illegal character or unrecognized token in input." );

		return;
	}




	// ----- Implementation of PrescannerJavaSourceClient Interface -----




	// The prescanner calls this routine when an invalid unicode escape
	// character is encountered.
	//
	// Implements the parserIOException() method of ParserClient.

	public void javaSourceInvalidEscape (Token token)
	{

		// Report the error

		reportError (token, null, "Invalid escape character." );

		return;
	}




	// ----- Compiler Public Interface -----




	// This constructor creates the compiler object.

	public Ex4Compiler (PrintStream stdOut, ErrorOutput errOut)
	{
		super ();

		// Validate the arguments

		if ((stdOut == null) || (errOut == null))
		{
			throw new NullPointerException ("Ex4Compiler.Ex4Compiler");
		}

		// Save the output destinations

		_stdOut = stdOut;
		_errOut = errOut;

		// Get our scanner table

		_scannerTable = new Ex4GrammarScannerTable ();

		// Link the token factories to the scanner table

		_scannerTable.linkFactory ("identifier", "", new Ex4Identifier());

		_scannerTable.linkFactory ("illegalNumberLiteral", "", new Ex4NumberIllegal());
		_scannerTable.linkFactory ("illegalCharacterLiteral", "", new Ex4CharacterIllegal());
		_scannerTable.linkFactory ("illegalStringLiteral", "", new Ex4StringIllegal());
		_scannerTable.linkFactory ("runOnStringLiteral", "", new Ex4StringRunOn());

		_scannerTable.linkFactory ("lineEnd", "", new Ex4LineEnd());

		_scannerTable.linkFactory ("beginComment", "", new Ex4BeginComment());

		_scannerTable.linkFactory ("endComment", "", new Ex4EndComment());

		_scannerTable.linkFactory ("illegalChar", "", new Ex4IllegalChar());

		// Enable tracing if debug mode

		if (_debug)
		{
			_scannerTable.setTrace (_errOut);
		}

		// If debug mode, check the invariant.  This checks the scanner table
		// for internal consistency.  It also checks to make sure that all the
		// factories we linked in with linkFactory have correct token names
		// and link names.

		if (_debug)
		{
			String inv = _scannerTable.checkInvariant();

			if (inv != null)
			{
				throw new InternalError ("Ex4Compiler: " + inv);
			}
		}

		// Link condition numbers

		_conditionNotInComment = _scannerTable.lookupCondition ("notInComment");
		_conditionInComment = _scannerTable.lookupCondition ("inComment");


		// Done

		return;
	}




	// Compile a source file.
	//
	// The return value is true if there was an error.

	public boolean compile (String filename)
	{

		// Create a scanner for the source file

		_scanner = makeScanner (filename);

		// If the scanner couldn't be created, return error

		if (_scanner == null)
		{
			return true;
		}

		// No error so far

		_error = false;

		// Create an empty hash table to hold identifiers

		_identifiers = new Hashtable ();

		// Use a try block to catch exceptions

		try
		{

			// Loop to read all the tokens

		tokenLoop:
			for ( ; ; )
			{

				// Get the next token

				Token token = _scanner.nextToken();

				// Switch on token type

				switch (token.number)
				{

				case Token.EOF:

					// An end-of-file token has number Token.EOF, which
					// is defined to be 0.

					// Stop reading tokens

					break tokenLoop;


				case 1:

					// An identifier token has number 1.  This is the token
					// parameter from the grammar specification file.

					{

						// Get the identifier name

						String idName = (String) token.value;

						// Get its entry in the identifier table

						Integer idCount = (Integer) _identifiers.get (idName);

						// Increment the number, or initialize it to 1

						if (idCount == null)
						{
							idCount = new Integer (1);
						}
						else
						{
							idCount = new Integer (idCount.intValue() + 1);
						}

						// Store new value in table

						_identifiers.put (idName, idCount);
					}

					break;


				default:

					// Any other token is an internal error

					throw new InternalCompilerException ("Bad token number " + token.number);
				}
			}
		}

		catch (IOException e)
		{

			// Report the error

			reportError ("I/O Exception", e.toString() + ".");
		}

		catch (SyntaxException e)
		{

			// Report the error

			reportError ("Syntax Exception", e.toString() + ".");
		}

		// If there are no errors ...

		if (!_error)
		{

			// Enumerate the identifier table

			for (Enumeration e = _identifiers.keys(); e.hasMoreElements(); )
			{

				// Get identifier name

				String idName = (String) e.nextElement();

				// Get identifier count

				Integer idCount = (Integer) _identifiers.get (idName);

				// Print it

				_stdOut.println (idName + "  " + idCount);
			}
		}

		// Dump the identifier table

		_identifiers = null;

		// Return the error flag

		return _error;
	}




// ----- Token Factories -----


/*->

  The following classes define the token factories for this compiler.

  When the scanner recognizes a token, it calls the makeToken() entry point in
  the corresponding token factory.  The token factory can do one of three
  things:  (a) assemble the token by filling in the fields of the Token object;
  (b) discard the token;  or (c) reject the token.  (Rejecting is only used in
  special cases;  none of the token factories in this example ever reject a
  token.)

  The scanner supplies a Token object initialized as follows:

	token.number = token parameter (from the grammar specification)

	token.value = null

	token.file = current filename

	token.line = current line number

	token.column = current column number

  You will notice that not every token listed in the grammar specification has
  a token factory.  Tokens without a token factory are handled by the default
  token factory.  The default token factory does the following:  (a) if the
  parameter is zero, it discards the token;  and (b) if the parameter is
  nonzero, it assembles a token whose number equals the parameter, and whose
  value is null (that is, it leaves the Token object unchanged).

  Each token factory is an inner class.  This allows the token factory to
  access variables and functions in the compiler object.

->*/




// Token factory class for identifier.
//
// The value of an identifier is a String object.

final class Ex4Identifier extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Get the value as a string

		token.value = scanner.tokenToString ();

		// Assembled token

		return assemble;
	}
}




// Token factory class for illegal numeric literal.

final class Ex4NumberIllegal extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Report the error

		reportError (token, null, "Illegal number.");

		// Discard token

		return discard;
	}
}




// Token factory class for illegal character literal.

final class Ex4CharacterIllegal extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Report the error

		reportError (token, null, "Illegal character literal.");

		// Discard token

		return discard;
	}
}




// Token factory class for illegal string literal.

final class Ex4StringIllegal extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Report the error

		reportError (token, null, "Illegal string (invalid escape sequence).");

		// Discard token

		return discard;
	}
}




// Token factory class for run-on string literal.

final class Ex4StringRunOn extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Report the error

		reportError (token, null, "Run-on string.");

		// Discard token

		return discard;
	}
}




// Token factory class for lineEnd.
//
// A lineEnd is discarded after counting the line.

final class Ex4LineEnd extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Bump the line number

		scanner.countLine ();

		// Discard token

		return discard;
	}
}




// Token factory class for beginComment.
//
// A beginComment is discarded after setting the start condition.

final class Ex4BeginComment extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Set start condition to "inComment"

		scanner.setCondition (_conditionInComment);

		// Discard token

		return discard;
	}
}




// Token factory class for endComment.
//
// An endComment is discarded after setting the start condition.

final class Ex4EndComment extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Set start condition to "notInComment"

		scanner.setCondition (_conditionNotInComment);

		// Discard token

		return discard;
	}
}




// Token factory class for illegalChar.
//
// An illegalChar is discarded, after informing the scanner client.

final class Ex4IllegalChar extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Tell the client

		scanner.client().scannerUnmatchedToken (scanner, token);

		// Discard token

		return discard;
	}
}




}


