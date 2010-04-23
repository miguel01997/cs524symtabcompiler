package invisible.jacc.parse;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;


/*->

  CompilerModel is an abstract class that provides most of the implementation
  for a simple compiler.

  When you first start using Invisible Jacc, you can use CompilerModel to
  create a simple compiler with minimum programming effort.  As you become more
  experienced and write more sophisticated compilers, you will probably stop
  using CompilerModel.

  The way you use CompilerModel is to write a concrete subclass that extends
  CompilerModel.  At a minimum, the concrete subclass needs to provide a
  constructor that creates the scanner and parser tables.  The concrete
  subclass can also override methods of CompilerModel to customize its
  behavior.


  WHAT A CONCRETE SUBCLASS MUST DO

  At a minimum, a concrete subclass must include a constructor that creates
  scanner and parser tables.  The constructor must set the variables
  _scannerTable and _parserTable to point to these tables.


  WHAT A CONCRETE SUBCLASS MAY OPTIONALLY DO

  Any method in CompilerModel may be overridden to customize its behavior.
  However, there are three methods that are most likely to be overridden.

  The compile() method may be overridden to provide additional initialization
  at the start of a parse, and additional processing after the end of a parse.
  It may also be overridden to install a different preprocessor, or to
  implement a different method for finding the source file.

  The makeScanner() method may be overridden to install a different
  prescanner, or to implement a search path for source files.

  The scannerEOF() method may be overridden to perform processing at the end of
  each source file, for example to detect run-on comments.


  INVOKING THE COMPILER

  To invoke the compiler, first create an instance of the concrete subclass,
  and then call the compile() method.

->*/


public abstract class CompilerModel implements ScannerClient, ParserClient 
{


	// ----- Per-Compiler Variables -----
	//
	// These variables are initialized when the compiler object is constructed.


	// The ErrorOutput object that is used as the destination for error
	// messages.  The CompilerModel constructor sets this to send error
	// messages to System.out.  A concrete subclass can change this.

	protected ErrorOutput _errOut;

	// The compiler's scanner table.  The CompilerModel constructor sets this
	// to null.  The constructor of the concrete subclass must build the
	// scanner table and set this variable.

	protected ScannerTable _scannerTable;

	// The compiler's parser table.  The CompilerModel constructor sets this
	// to null.  The constructor of the concrete subclass must build the
	// parser table and set this variable.

	protected ParserTable _parserTable;




	// ----- Per-Compilation Variables -----
	//
	// These variables are initialized at the start of each compilation, in the
	// compile() method.  If the concrete subclass overrides compile(), then
	// the concrete subclass is responsible for these variables.


	// This flag is true if an error occurred during the compilation

	protected boolean _error;

	// The scanner used for the current compilation.

	protected Scanner _scanner;

	// The preprocessor used for the current compilation.

	protected Preprocessor _preprocessor;

	// The compiler's parser.

	protected Parser _parser;




	// ----- Internal Compiler Functions -----




	// Query the error flag.  This function returns true if an error has
	// occurred during the current compilation.

	public boolean error ()
	{
		return _error;
	}




	// Issue an error message at the specified position.  The Token object
	// provides the position information.  The first String is an error code,
	// and the second String is an error message.  Either String can be null.
	//
	// This function also sets the _error flag.

	public void reportError (Token token, String code, String message)
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

	public void reportError (String code, String message)
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

	public void reportWarning (Token token, String code, String message)
	{

		// Report the warning
		
		_errOut.reportError (ErrorOutput.typeWarning, null, token.file,
			token.line, token.column, code, message );

		return;
	}




	// Issue a warning message with no position information.  The first String
	// is a warning code, and the second String is a warning message.  Either
	// String can be null.

	public void reportWarning (String code, String message)
	{

		// Report the warning
		
		_errOut.reportError (ErrorOutput.typeWarning, null, null,
			ErrorOutput.noPosition, ErrorOutput.noPosition, code, message );

		return;
	}




	// Given a filename, this function creates a Scanner object for scanning
	// the file.  The return value is null if the file could not be opened.
	//
	// This implementation uses the prescanner PrescannerByteStream.  This
	// assumes that the file contains ASCII text, which is fed directly to the
	// scanner.
	//
	// A token factory that recognizes an 'include' statement can use this
	// function to create a scanner for the include file.  The scanner can then
	// be used as the value of an insert-stream escape token.
	//
	// A concrete subclass can optionally override this method.  Possible
	// reasons for overriding this method include (a) using a different
	// prescanner, or (b) implementing a search path.

	public Scanner makeScanner (String filename)
	{

		// We need to catch I/O exceptions

		try
		{
		
			// Open the file and attach it to an InputStream

			InputStream stream = new FileInputStream (filename);

			// Make an input source for the scanner

			Prescanner scannerSource = new PrescannerByteStream (stream);

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




	// Given a filename, this function reads a generated file and creates the
	// _scannerTable and _parserTable objects.
	//
	// The file should be a ".gen" file as produced by GenMain, GenGUI, or GenFrontEnd.
	//
	// The return value is true if there was an error.

	protected boolean readGenFile (String filename)
	{

		// We need to catch I/O exceptions

		try
		{
		
			// Open the file and attach it to a DataInputStream

			DataInputStream stream = new DataInputStream (new FileInputStream (filename));

			// Read the _scannerTable object

			_scannerTable = new ScannerTable ();

			_scannerTable.readFromStream (stream);

			// Read the _parserTable object

			_parserTable = new ParserTable ();

			_parserTable.readFromStream (stream);

			// Close the stream

			stream.close ();

			// Return success

			return false;
		}

		// If an I/O exception occurs, return true to indicate failure

		catch (IOException e)
		{
			return true;
		}
	}
	
	
	
	
	// Activate debugging features.
	//
	// This checks the scanner and parser tables for internal consistency.  It
	// also checks all the token names, nonterminal names, and link names passed
	// to the linkFactory functions, to ensure that they match names that appear
	// in the grammar specification.  If any errors are found, messages are sent
	// to the _errOut object.
	//
	// If traceScanner is true, then scanner tracing is enabled.  This sends a
	// message to the _errOut object on every call to a token factory.
	//
	// If traceParser is true, then parser tracing is enabled.  This sends a
	// message to the _errOut object on every call to a nonterminal factory.
	//
	// This function should be used only while you are debugging your code.  The
	// call to setDebugMode should be placed in the constructor of your concrete
	// subclass, after you set up the scanner and parser tables, make all your
	// calls to linkFactory, and set up _errOut to the destination for debug
	// output.
	//
	// The release version of your code should not call setDebugMode at all.
	//
	// The return value is true if an error was detected in the scanner and/or
	// parser tables.
	
	protected boolean setDebugMode (boolean traceScanner, boolean traceParser)
	{

		// Enable scanner tracing if desired

		if (traceScanner)
		{
			_scannerTable.setTrace (_errOut);
		}

		// Enable parser tracing if desired

		if (traceParser)
		{
			_parserTable.setTrace (_errOut);
		}

		// Check the scanner table invariant.  This checks the scanner table
		// for internal consistency.  It also checks to make sure that all the
		// factories we linked in with linkFactory have correct token names
		// and link names.

		String sinv = _scannerTable.checkInvariant();

		if (sinv != null)
		{
			_errOut.reportError (ErrorOutput.typeError, null, null,
				ErrorOutput.noPosition, ErrorOutput.noPosition, null,
				"Invalid scanner table: " + sinv );
			
			_errOut.flush();
		}

		// Check the parser table invariant.  This checks the parser table
		// for internal consistency.  It also checks to make sure that all the
		// factories we linked in with linkFactory have correct nonterminal
		// names and link names.

		String pinv = _parserTable.checkInvariant();

		if (pinv != null)
		{
			_errOut.reportError (ErrorOutput.typeError, null, null,
				ErrorOutput.noPosition, ErrorOutput.noPosition, null,
				"Invalid parser table: " + pinv );
			
			_errOut.flush();
		}
		
		// Return true if there was an error
		
		return (sinv != null) || (pinv != null);
	}




	// ----- Implementation of ScannerClient Interface -----




	// The scanner calls this routine when it reaches end-of-file.
	//
	// Implements the scannerEOF() method of ScannerClient.
	//
	// This implementation does nothing.  A concrete subclass can override
	// this method.  A common reason for overriding this method is to check
	// for run-on comments.

	public void scannerEOF (Scanner scanner, Token token)
	{
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
		
		// If it was an interrupt ...
		
		if (e instanceof InterruptedCompilerException)
		{
			
			// Report the interrupt

			reportError (null, "Interrupted.");
		}
		
		// Otherwise, we detected some other error ...
		
		else
		{

			// Report the error

			reportError ("Syntax Exception", e.toString() + ".");
		}

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
	//
	// This constructor sets _errOut to point at the standard output, and sets
	// _scannerTable and _parserTable to null.
	//
	// The concrete subclass must construct scanner and parser tables, and must
	// set _scannerTable and _parserTable to point at the constructed tables.
	//
	// Optionally, the concrete subclass can change _errOut to point at a
	// different ErrorOutput object.

	public CompilerModel ()
	{
		super ();

		// Set _errOut to point at the standard output

		_errOut = new ErrorOutputStream (System.out, null);

		// No scanner or parser tables

		_scannerTable = null;
		_parserTable = null;

		// Done

		return;
	}




	// Compile a source file.
	//
	// The return value is true if there was an error.
	//
	// This implementation first calls makeScanner() to create a scanner for
	// the given filename.  Then it creates a parser object and a preprocessor
	// object.  (It uses PreprocessorInclude to create a simple preprocessor.)
	// Finally, it calls the parser to parse the source file.
	//
	// Many concrete subclasses need to override this method.  A common reason
	// for overriding this method is to perform additional initialization
	// before the parse begins, and additional calculations after the parse is
	// over.

	public boolean compile (String filename)
	{

		// No error so far

		_error = false;

		// Create a scanner for the source file

		_scanner = makeScanner (filename);

		// If the scanner couldn't be created, report the error

		if (_scanner == null)
		{
			reportError (null, "Unable to open source file '" + filename + "'.");
			return _error;
		}

		// Create our parser

		_parser = new Parser (this, _parserTable, null);

		// Create a preprocessor that supplies input to the parser

		_preprocessor = new PreprocessorInclude (_scanner);

		// Parse the source

		_parser.parse (_preprocessor);

		// Get rid of objects we don't need any more

		_scanner = null;
		_preprocessor = null;
		_parser = null;

		// Return the error flag

		return _error;
	}


}

