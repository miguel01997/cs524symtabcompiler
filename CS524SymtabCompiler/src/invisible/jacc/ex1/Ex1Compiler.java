// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.ex1;

import java.util.Hashtable;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

import invisible.jacc.parse.ErrorOutput;
import invisible.jacc.parse.InternalCompilerException;
import invisible.jacc.parse.NonterminalFactory;
import invisible.jacc.parse.Parser;
import invisible.jacc.parse.ParserClient;
import invisible.jacc.parse.ParserTable;
import invisible.jacc.parse.Preprocessor;
import invisible.jacc.parse.PreprocessorInclude;
import invisible.jacc.parse.Prescanner;
import invisible.jacc.parse.PrescannerByteStream;
import invisible.jacc.parse.PrescannerCharReader;
import invisible.jacc.parse.Scanner;
import invisible.jacc.parse.ScannerClient;
import invisible.jacc.parse.ScannerTable;
import invisible.jacc.parse.SyntaxException;
import invisible.jacc.parse.Token;
import invisible.jacc.parse.TokenFactory;
import invisible.jacc.parse.TokenStream;


/*->

  Ex1Compiler is an example compiler, using the Jacc parser and scanner.

  This compiler implements the language defined in Ex1Grammar.jacc.  It is a
  simple calculator language.  The source file can contain arithmetic
  expressions formed using addition, subtraction, multiplication, division,
  exponentiation, and square root.  There are two things you can do with an
  expression:  assign its value to a variable, or print it.

  All calculations are done using double-precision floating point.  The
  compiler uses the parser's value stack to hold Double objects, each of which
  contains a calculated value.  A null entry on the value stack indicates that
  there was an error insertion, so no value can be calculated.

  Once an Ex1Compiler object is constructed, it may be used to compile any
  number of source files.


  CREATING THE SCANNER AND PARSER TABLES
  
  There are two ways to create the scanner and parser tables:  you can use
  the graphical interface, or you can use the command line.
  
  To use the graphical interface, execute class invisible.jacc.gen.GenGUI.
  When the window appears, enter invisible\jacc\ex1\Ex1Grammar.jacc as the
  file name.  Check the boxes to "generate scanner table", "generate parser
  table", "create Java source files", "write output messages to file", and
  "write verbose output".  Then, click on "Generate".
  
  To use the command line, execute class invisible.jacc.gen.GenMain with the
  following command line:

        -v -o -j invisible\jacc\ex1\Ex1Grammar.jacc
  
  In either case, Invisible Jacc creates Ex1GrammarScannerTable.java and
  Ex1GrammarParserTable.java.
  
  Note:  On Microsoft Windows, the filename is not case-sensitive.  On other
  operating systems, the filename may or may not be case-sensitive depending
  on the operating system.  (If you're using another operating system,
  substitute the appropriate path separator character in place of backslash.)
  
  Note:  If you omit the ".jacc" extension from the filename, Invisible Jacc
  supplies the ".jacc" extension automatically.
  
  
  RUNNING THE SAMPLE INPUT
  
  To run the sample input, execute class invisible.jacc.ex1.Ex1Main with the
  following command line:
  
 		invisible\jacc\ex1\Ex1Input.txt
  
  File Ex1Input.out contains the result of running the above command.

->*/


public class Ex1Compiler implements ScannerClient, ParserClient 
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

	// The compiler's preprocessor.

	Preprocessor _preprocessor;

	// The compiler's parser table.

	ParserTable _parserTable;

	// The compiler's parser.

	Parser _parser;




	// ----- Per-Compilation Variables -----
	//
	// These variables are initialized at the start of each compilation.


	// This flag is true if an error occurred during the compilation

	boolean _error;

	// The scanner used for this compilation.

	Scanner _scanner;

	// This Hashtable holds the values of variables.  Each key is a String
	// giving the name of a variable.  Each element is a Double giving the
	// current value of that variable.

	Hashtable _variables;




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
		
			// Open the file and attach it to a Reader

			Reader reader = new FileReader (filename);

			// Make an input source for the scanner

			Prescanner scannerSource = new PrescannerCharReader (reader);
			
			// If you wanted to open the file using FileInputStream instead
			// of FileReader, then you could replace the previous two lines
			// of code with the following.  Note that if you use FileInputStream
			// then you can make the character set size equal to 256 because
			// FileInputStream only returns bytes.  If you use FileReader, then
			// the character set size must be 65536 because FileReader can
			// return any Unicode characters.
			//
			// InputStream stream = new FileInputStream (filename);
			// Prescanner scannerSource = new PrescannerByteStream (stream);

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




	// ----- Implementation of ParserClient Interface -----




	// The parser calls this routine when an I/O exception occurs.
	//
	// Implements the parserIOException() method of ParserClient.

	public void parserIOException (Parser parser, IOException e)
	{

		// Report the error

		reportError ("I/O Exception", e.toString() + ".");

		return;
	}




	// The parser calls this routine when a syntax exception occurs.
	//
	// Implements the parserSyntaxException() method of ParserClient.

	public void parserSyntaxException (Parser parser, SyntaxException e)
	{

		// Report the error

		reportError ("Syntax Exception", e.toString() + ".");

		return;
	}




	// The parser calls this routine when it repairs an error.
	//
	// Implements the parserErrorRepair() method of ParserClient.

	public void parserErrorRepair (Parser parser, Token errorToken,
		int[] insertions, int insertionLength, int[] deletions, int deletionLength)
	{

		// If it's a "simple" repair...

		if ((insertionLength + deletionLength) <= 6)
		{

			// For each insertion ...

			for (int i = 0; i < insertionLength; ++i)
			{

				// Report the insertion

				reportError (errorToken, null,
					"Expected '" + parser.symbolName(insertions[i]) + "'." );
			}

			// For each deletion ...

			for (int i = 0; i < deletionLength; ++i)
			{

				// Report the deletion

				reportError (errorToken, null,
					"Unexpected '" + parser.symbolName(deletions[i]) + "'." );
			}
		}

		// Otherwise, it's a "complicated" repair ...

		else
		{

			// Report a generic error

			reportError (errorToken, null, "Syntax error." );
		}

		return;
	}




	// The parser calls this routine when it is unable to repair an error.
	//
	// Implements the parserErrorFail() method of ParserClient.

	public void parserErrorFail (Parser parser, Token errorToken)
	{

		// Report a generic error

		reportError (errorToken, null, "Syntax error - unable to continue." );

		return;
	}




	// ----- Compiler Public Interface -----




	// This constructor creates the compiler object.

	public Ex1Compiler (PrintStream stdOut, ErrorOutput errOut)
	{
		super ();

		// Validate the arguments

		if ((stdOut == null) || (errOut == null))
		{
			throw new NullPointerException ("Ex1Compiler.Ex1Compiler");
		}

		// Save the output destinations

		_stdOut = stdOut;
		_errOut = errOut;

		// Get our scanner table

		_scannerTable = new Ex1GrammarScannerTable ();

		// Link the token factories to the scanner table

		_scannerTable.linkFactory ("identifier", "", new Ex1Identifier());

		_scannerTable.linkFactory ("number", "", new Ex1Number());
		_scannerTable.linkFactory ("number", "illegal", new Ex1NumberIllegal());

		_scannerTable.linkFactory ("string", "", new Ex1String());
		_scannerTable.linkFactory ("string", "runOn", new Ex1StringRunOn());

		_scannerTable.linkFactory ("include", "", new Ex1Include());

		_scannerTable.linkFactory ("illegalInclude", "", new Ex1IllegalInclude());

		_scannerTable.linkFactory ("lineEnd", "", new Ex1LineEnd());

		_scannerTable.linkFactory ("beginComment", "", new Ex1BeginComment());

		_scannerTable.linkFactory ("endComment", "", new Ex1EndComment());

		_scannerTable.linkFactory ("illegalChar", "", new Ex1IllegalChar());

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
				throw new InternalError ("Ex1Compiler: " + inv);
			}
		}

		// Link condition numbers

		_conditionNotInComment = _scannerTable.lookupCondition ("notInComment");
		_conditionInComment = _scannerTable.lookupCondition ("inComment");

		// Get our parser table

		_parserTable = new Ex1GrammarParserTable ();

		// Link the nonterminal factories to the parser table

		_parserTable.linkFactory ("Statement", "empty", new Ex1StatementEmpty());
		_parserTable.linkFactory ("Statement", "assign", new Ex1StatementAssign());
		_parserTable.linkFactory ("Statement", "print", new Ex1StatementPrint());

		_parserTable.linkFactory ("PrintItem", "", new Ex1PrintItem());

		_parserTable.linkFactory ("Expr", "", new Ex1Expr());

		// Enable tracing if debug mode

		if (_debug)
		{
			_parserTable.setTrace (_errOut);
		}

		// If debug mode, check the invariant.  This checks the parser table
		// for internal consistency.  It also checks to make sure that all the
		// factories we linked in with linkFactory have correct nonterminal
		// names and link names.

		if (_debug)
		{
			String inv = _parserTable.checkInvariant();

			if (inv != null)
			{
				throw new InternalError ("Ex1Compiler: " + inv);
			}
		}

		// Create our parser

		_parser = new Parser (this, _parserTable, null);

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

		// Create an empty hash table to hold program variables

		_variables = new Hashtable ();

		// Create a preprocessor that supplies input to the parser

		_preprocessor = new PreprocessorInclude (_scanner);

		// Parse the source

		_parser.parse (_preprocessor);

		// Dump the variable table

		_variables = null;

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

  In most cases, the token parameter is the numerical value of the terminal
  symbol that corresponds to the token.

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

final class Ex1Identifier extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Get the value as a string

		token.value = scanner.tokenToString ();

		// Assembled token

		return assemble;
	}
}




// Token factory class for number.
//
// The value of a number is a Double object, or null if there was a conversion
// error.

final class Ex1Number extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Get the value as a string

		String numString = scanner.tokenToString ();

		// We want to use Double.valueOf() to convert the string into a number.
		// Unfortunately, Double.valueOf() only accepts strings that contain a
		// decimal point.  Therefore, we need to check of our string contains a
		// decimal point, and add one if it doesn't.

		// If no decimal point ...

		if (numString.indexOf('.') < 0)
		{

			// Search for the index where we need to split the string

			int splitIndex;

			// Advance the split index until we reach a non-digit (which will
			// be the first character of the exponent) or the end of the string

			for (splitIndex = 1; splitIndex < numString.length(); ++splitIndex)
			{
				if (!Character.isDigit(numString.charAt(splitIndex)))
				{
					break;
				}
			}

			// Insert decimal point at the position of splitIndex

			numString = numString.substring (0, splitIndex) + "."
						+ numString.substring (splitIndex, numString.length());
		}

		// Try block lets us catch conversion errors

		try
		{

			// Convert to floating-point and store into value stack

			token.value = Double.valueOf (numString);
		}

		// If conversion error ...

		catch (NumberFormatException e)
		{

			// Use null value
			// (Technically, we don't need to code this explicitly, since
			// token.value is initialized to null by the scanner, and no
			// assignment to token.value will occur if Double.valueOf() throws
			// an exception.  But we consider it bad form to rely on the
			// precise timing of an exception, so we code as if it were
			// possible that token.value was modified.)

			token.value = null;

			// Report the error

			reportError (token, null,
				"Invalid number '" + scanner.tokenToString() + "'." );
		}

		// Assembled token

		return assemble;
	}
}




// Token factory class for illegal number.
//
// This token factory returns a number token with null value, which is the
// value of an error-insertion.

final class Ex1NumberIllegal extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Report the error

		reportError (token, null,
			"Invalid number '" + scanner.tokenToString() + "'." );

		// Assembled token

		return assemble;
	}
}



// Token factory class for string.
//
// The value of a string is a String object, or null if the string contains
// an invalid escape sequence.

final class Ex1String extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// The token text is a string enclosed in double quotes.  Create
		// a char array and copy the part of the text between quotes into
		// the array.

		char[] stringChars = new char[scanner.tokenLength() - 2];

		scanner.tokenToChars (1, scanner.tokenLength() - 2, stringChars, 0);

		// This flag is true if there is a bad escape sequence

		boolean badEscape = false;

		// We need to scan the string and convert each escape sequence into the
		// corresponding character.  We recognize the same escape sequences as
		// the Java compiler.  Begin by initializing source and destination
		// indexes.

		int srcIndex = 0;
		int dstIndex = 0;

		// Loop until all characters are processed

		while (srcIndex < stringChars.length)
		{

			// Get the next character

			char c = stringChars[srcIndex++];

			// If it's the start of an escape sequence ...

			if (c == '\\')
			{

				// Get the next character.  Notice that we don't have to check
				// if srcIndex is past the end of the array, since the regular
				// expression won't match a string that ends in the middle of
				// an escape sequence.

				c = stringChars[srcIndex++];

				// Switch on the escape code ...

				switch (c)
				{

				case 'b':

					// Backspace

					c = 0x0008;
					break;

				case 't':

					// Tab

					c = 0x0009;
					break;

				case 'n':

					// Line feed

					c = 0x000A;
					break;

				case 'f':

					// Form feed

					c = 0x000C;
					break;

				case 'r':

					// Carriage return

					c = 0x000D;
					break;

				case '\"':
				case '\'':
				case '\\':

					// Double quote, single quote, and backslash

					break;

				case '0':
				case '1':
				case '2':
				case '3':
				case '4':
				case '5':
				case '6':
				case '7':

					// Octal escape

					c = (char)(c - '0');

					// If there are two octal digits ...

					if ((srcIndex < stringChars.length)
						&& (stringChars[srcIndex] >= '0')
						&& (stringChars[srcIndex] <= '7') )
					{

						// Shift in the second octal digit

						c = (char)((c << 3) + (stringChars[srcIndex++] - '0'));

						// If there are three octal digits ...

						if ((c <= 0x001F)
							&& (srcIndex < stringChars.length)
							&& (stringChars[srcIndex] >= '0')
							&& (stringChars[srcIndex] <= '7') )
						{

							// Shift in the third octal digit

							c = (char)((c << 3) + (stringChars[srcIndex++] - '0'));
						}
					}

					break;

				default:

					// Invalid escape sequence

					// Adjust column in token to point right at the bad escape

					token.column += (srcIndex - 1);

					// Report the error

					reportError (token, null, "Invalid escape sequence '"
						+ new String (stringChars, srcIndex - 2, 2) + "'." );

					// Restore the column

					token.column -= (srcIndex - 1);

					// Set bad escape sequence flag

					badEscape = true;

					break;

				}	// end switch on escape code

			}	// end if escape sequence

			// Write the character at the destination index

			stringChars[dstIndex++] = c;

		}	// end loop over source characters

		// If not a bad escape sequence ...

		if (!badEscape)
		{

			// Get the value as a string

			token.value = new String (stringChars, 0, dstIndex);
		}

		// Assembled token

		return assemble;
	}
}




// Token factory class for run-on string.
//
// This token factory returns a string token with null value, which is the
// value of an error-insertion.

final class Ex1StringRunOn extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Report the error

		reportError (token, null, "Unterminated string.");

		// Assembled token

		return assemble;
	}
}




// Token factory class for include.
//
// This token factory creates an include file escape token.  The class
// PreprocessorInclude recognizes the escape token before the parser sees
// it, and inserts the entire contents of the included file into the token
// stream.  The value of the escape token is the Scanner object for the include
// file.
//
// In case of error, this token factory produces an error message and then
// throws FileNotFoundException, since it makes no sense to continue scanning
// when an include file is missing.

final class Ex1Include extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Get the token text as a string

		String includeText = scanner.tokenToString ();

		// The filename is at the end of the token text, enclosed in < and >,
		// and possibly surrounded by leading and trailing white space.

		// Extract the filename by taking the substring that lies between <
		// and >, and trimming it.  Notice that filename characters are defined
		// (in Ex1Grammar.jacc) to be characters greater than 0x0020 other than
		// '<' and '>'.  Therefore, trim() does exactly the right thing in
		// removing leading and trailing white space from the filename.  Notice
		// we must use lastIndexOf() rather than indexOf(), in case there is a
		// comment between the keyword 'include' and the filename which contains
		// a '<' or '>' character.

		String filename = includeText.substring (
			includeText.lastIndexOf('<') + 1, includeText.lastIndexOf('>') ).trim();

		// Make a scanner for this filename

		Scanner includeScanner = makeScanner (filename);

		// If we couldn't open the file ...

		if (includeScanner == null)
		{

			// Report the error

			reportError (token, null,
				"Unable to open include file '" + filename + "'." );

			// Throw an exception to abort compilation

			throw new FileNotFoundException (filename);
		}

		// Construct an include-file escape token

		token.number = Token.escapeInsertStream;
		token.value = includeScanner;

		// Assembled token

		return assemble;
	}
}




// Token factory class for illegalInclude.
//
// An illegalInclude is discarded, after printing an error message.

final class Ex1IllegalInclude extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Report the error

		reportError (token, null, "Invalid 'include' statement." );

		// Discard token

		return discard;
	}
}




// Token factory class for lineEnd.
//
// A lineEnd is discarded after counting the line.

final class Ex1LineEnd extends TokenFactory
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

final class Ex1BeginComment extends TokenFactory
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

final class Ex1EndComment extends TokenFactory
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

final class Ex1IllegalChar extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Tell the client

		scanner.client().scannerUnmatchedToken (scanner, token);

		// Discard token

		return discard;
	}
}




// ----- Nonterminal Factories -----


/*->

  The following classes define the nonterminal factories for this compiler.

  When the parser reduces a production, it calls the makeNonterminal() entry
  point for the production's nonterminal factory.  The nonterminal factory
  must return the value of the nonterminal symbol on the production's left hand
  side.  The parser saves the returned value on the value stack.

  The parser supplies a parameter which is the production parameter from the
  grammar specification.  If the same nonterminal factory is used for more than
  one production, then the parameter can be used to identify which production
  is being reduced.  For example, the class Ex1Expr is the nonterminal factory
  for all productions with Expr on the left hand side, and a switch statement
  is used to select code for each specific production.

  Alternatively, it is possible to use link names to give a different
  nonterminal factory to each production.  For example, the productions
  with Statement on the left hand side are handled by three different
  nonterminal factory classes: Ex1StatementEmpty, Ex1StatementAssign, and
  Ex1StatementPrint.

  As you see, there are two possible methods for distinguishing between
  different productions with the same left hand side.  The choice of which
  method to use is a matter of taste and convenience.

  When a nonterminal factory reads a terminal symbol T from the production's
  right hand side, the nonterminal factory must be prepared to handle the
  possiblity that the value of T could be null.  This is true even if the token
  factory for T never returns a null value.  The reason is that during error
  repair, the parser may insert terminal symbols that are not present in the
  source file, and these error insertions always have null value.  For example,
  in the class Ex1Expr, cases 0 and 1 read terminal symbols from the right hand
  side, and so the code carefully checks each terminal symbol to see if it is
  null.

  On the other hand, when a nonterminal factory reads a nonterminal symbol S
  from the production's right hand side, the value of S is always the result of
  calling the nonterminal factory for S.  If the nonterminal factory for S
  never returns null, then the value of S is never null.  For example, in the
  class Ex1Expr, cases 2 through 10 read nonterminal symbols from the right
  hand side, and the code doesn't need to check for a null value because the
  nonterminal factory never returns null.

  You will notice that not every production listed in the grammar specification
  has a nonterminal factory.  Productions without a nonterminal factory are
  handled by the default nonterminal factory.  The default nonterminal factory
  does the following:  (a) if the production has a non-empty right hand side,
  it returns the value of the first item on the right hand side;  (b) if the
  production has an empty right hand side, it returns null.

  Each nonterminal factory is an inner class.  This allows the nonterminal
  factory to access variables and functions in the compiler object.

->*/




// Nonterminal factory class for Statement{empty}.
//
// A Statement has null value.
//
// This is an empty statement.  It just does nothing and returns.

final class Ex1StatementEmpty extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Return null value

		return null;
	}
}




// Nonterminal factory class for Statement{assign}.
//
// A Statement has null value.
//
// This is an assignment statement.  It stores the the value of an expression
// into a variable.

final class Ex1StatementAssign extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		String variable = (String) parser.rhsValue (0);

		Double value = (Double) parser.rhsValue (2);

		// If variable is an error insertion, do nothing

		if (variable == null)
		{
			return null;
		}

		// Set the variable to have the specified value

		_variables.put (variable, value);

		// Return null value

		return null;
	}
}




// Nonterminal factory class for Statement{print}.
//
// A Statement has null value.
//
// This is a print statement.  When this nonterminal factory is called, all
// the items in the print list will already have been printed (by class
// Ex1PrintItem).  Therefore, all we have to do is print the line feed.

final class Ex1StatementPrint extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// End the line

		_stdOut.println();

		// Return null value

		return null;
	}
}




// Nonterminal factory class for PrintItem.
//
// A PrintItem has null value.

final class Ex1PrintItem extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		switch (param)
		{

		case 0:

			// Print expression

			{

				// Get items from value stack

				Double value = (Double) parser.rhsValue (0);

				// Print value

				if (value.isNaN() || value.isInfinite())
				{
					_stdOut.print ("--error--");
				}

				else
				{
					_stdOut.print (value.toString());
				}

				// Return null value

				return null;
			}

		case 1:

			// Print string

			{

				// Get items from value stack

				String value = (String) parser.rhsValue (0);

				// If value is an error insertion, do nothing

				if (value == null)
				{
					return null;
				}

				// Print value

				_stdOut.print (value);

				// Return null value

				return null;
			}

		}	// end switch

		// Come here if we get an invalid parameter, which should never happen

		throw new InternalCompilerException ("PrintItem #" + param);
	}
}




// Nonterminal factory class for Expr.
//
// The value of an Expr is a Double object.  If there was some error in
// creating the Expr, then the value is Not-a-Number.

final class Ex1Expr extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		switch (param)
		{

		case 0:

			// Number

			{

				// Get items from value stack

				Double value = (Double) parser.rhsValue (0);

				// If value is an error insertion, return Not-a-Number

				if (value == null)
				{
					return new Double (Double.NaN);
				}

				// Return the value

				return value;
			}

		case 1:

			// Identifier

			{

				// Get items from value stack

				String variable = (String) parser.rhsValue (0);

				// If variable is an error insertion, return Not-a-Number

				if (variable == null)
				{
					return new Double (Double.NaN);
				}

				// Get the value of this variable

				Double value = (Double) _variables.get (variable);

				// If variable is undefined ...

				if (value == null)
				{

					// Report the error

					reportError (parser.token(), null,
						"Undefined variable '" + variable + "'.");

					// Return Not-a-Number

					return new Double (Double.NaN);
				}

				// Return the value

				return value;
			}

		case 2:

			// Expression in parentheses

			{

				// Get items from value stack

				Double arg1 = (Double) parser.rhsValue (1);

				// Return the value

				return arg1;
			}

		case 3:

			// Square root

			{

				// Get items from value stack

				Double arg1 = (Double) parser.rhsValue (1);

				// Return the value

				return new Double (Math.sqrt(arg1.doubleValue()));
			}

		case 4:

			// Exponentiation

			{

				// Get items from value stack

				Double arg1 = (Double) parser.rhsValue (0);

				Double arg2 = (Double) parser.rhsValue (2);

				// Return the value

				return new Double (Math.pow (arg1.doubleValue(), arg2.doubleValue()));
			}

		case 5:

			// Multiplication

			{

				// Get items from value stack

				Double arg1 = (Double) parser.rhsValue (0);

				Double arg2 = (Double) parser.rhsValue (2);

				// Return the value

				return new Double (arg1.doubleValue() * arg2.doubleValue());
			}

		case 6:

			// Division

			{

				// Get items from value stack

				Double arg1 = (Double) parser.rhsValue (0);

				Double arg2 = (Double) parser.rhsValue (2);

				// Return the value

				return new Double (arg1.doubleValue() / arg2.doubleValue());
			}

		case 7:

			// Unary plus

			{

				// Get items from value stack

				Double arg1 = (Double) parser.rhsValue (1);

				// Return the value

				return arg1;
			}

		case 8:

			// Unary minus

			{

				// Get items from value stack

				Double arg1 = (Double) parser.rhsValue (1);

				// Return the value

				return new Double (- arg1.doubleValue());
			}


		case 9:

			// Addition

			{

				// Get items from value stack

				Double arg1 = (Double) parser.rhsValue (0);

				Double arg2 = (Double) parser.rhsValue (2);

				// Return the value

				return new Double (arg1.doubleValue() + arg2.doubleValue());
			}

		case 10:

			// Subtraction

			{

				// Get items from value stack

				Double arg1 = (Double) parser.rhsValue (0);

				Double arg2 = (Double) parser.rhsValue (2);

				// Return the value

				return new Double (arg1.doubleValue() - arg2.doubleValue());
			}

		}	// end switch

		// Come here if we get an invalid parameter, which should never happen

		throw new InternalCompilerException ("Expr #" + param);
	}
}




}


