package invisible.jacc.ex2;

import java.io.IOException;

import invisible.jacc.parse.CompilerModel;
import invisible.jacc.parse.NonterminalFactory;
import invisible.jacc.parse.Parser;
import invisible.jacc.parse.Scanner;
import invisible.jacc.parse.SyntaxException;
import invisible.jacc.parse.Token;
import invisible.jacc.parse.TokenFactory;


/*->

  Ex2Compiler is an example compiler, using the Jacc parser and scanner.

  This is just about the simplest possible compiler.  It inherits from class
  CompilerModel, so much of the compiler code is provided automatically.

  This compiler implements the language defined in Ex2Grammar.jacc.  It is a
  very simple calculator language.  Each statement in the source file is an
  expression followed by a semicolon.  An expression can be formed by decimal
  integers, addition, subtraction, and parentheses.  The compiler prints the
  value of each expression to the standard output.

  All calculations are done using integer arithmetic.  The compiler uses the
  parser's value stack to hold Integer objects, each of which contains a
  calculated value.  A null entry on the value stack indicates that there was
  a compiler error, so no value can be calculated.

  Once an Ex2Compiler object is constructed, it may be used to compile any
  number of source files.


  CREATING THE SCANNER AND PARSER TABLES
  
  There are two ways to create the scanner and parser tables:  you can use
  the graphical interface, or you can use the command line.
  
  To use the graphical interface, execute class invisible.jacc.gen.GenGUI.
  When the window appears, enter invisible\jacc\ex2\Ex2Grammar.jacc as the
  file name.  Check the boxes to "generate scanner table", "generate parser
  table", "create Java source files", and "write output messages to file".
  Then, click on "Generate".
  
  If you want to create verbose output, also check the box to "write verbose
  output".
  
  To use the command line, execute class invisible.jacc.gen.GenMain with the
  following command line:
  
        -o -j invisible\jacc\ex2\Ex2Grammar.jacc

  If you want to create verbose output, add the -v switch to the command line,
  like this:

        -v -o -j invisible\jacc\ex2\Ex2Grammar.jacc
  
  In either case, Invisible Jacc creates Ex2GrammarScannerTable.java and
  Ex2GrammarParserTable.java.
  
  Note:  On Microsoft Windows, the filename is not case-sensitive.  On other
  operating systems, the filename may or may not be case-sensitive depending
  on the operating system.  (If you're using another operating system,
  substitute the appropriate path separator character in place of backslash.)
  
  Note:  If you omit the ".jacc" extension from the filename, Invisible Jacc
  supplies the ".jacc" extension automatically.
  
  
  RUNNING THE SAMPLE INPUT

  This class includes a main() function with a simple command-line interface.
  To compile one or more files, execute class invisible.jacc.ex2.Ex2Compiler
  with the source file name(s) listed on the command line.  For example, to
  compile the sample file Ex2Input.txt, execute invisible.jacc.ex2.Ex2Compiler
  with the following command line:

        invisible\jacc\ex2\Ex2Input.txt
  
  File Ex2Input.out contains the result of running the above command.

->*/


public class Ex2Compiler extends CompilerModel 
{


	// This flag enables the use of debug token and nonterminal factories.

	static final boolean _debug = false;




	// Constructor must create the scanner and parser tables.

	public Ex2Compiler ()
	{
		super();

		// Get our scanner table

		_scannerTable = new Ex2GrammarScannerTable ();

		// Link the token factories to the scanner table

		_scannerTable.linkFactory ("number", "", new Ex2Number());

		_scannerTable.linkFactory ("lineEnd", "", new Ex2LineEnd());

		// Get our parser table

		_parserTable = new Ex2GrammarParserTable ();

		// Link the nonterminal factories to the parser table

		_parserTable.linkFactory ("Statement", "", new Ex2Statement());

		_parserTable.linkFactory ("Expression", "add", new Ex2ExpressionAdd());
		_parserTable.linkFactory ("Expression", "subtract", new Ex2ExpressionSubtract());

		_parserTable.linkFactory ("Primary", "paren", new Ex2PrimaryParen());
		
		// If debug mode, activate debugging features.  This checks the scanner
		// and parser tables for internal consistency, and checks all the strings
		// passed to the linkFactory functions to ensure they match names in the
		// grammar specification.  It also installs tracing code, which outputs
		// a message on every call to a token factory or nonterminal factory.

		if (_debug)
		{
			if (setDebugMode (true, true))
			{
				throw new InternalError ("Ex2Compiler: Consistency check failed.");
			}
		}

		// Done

		return;
	}




	// This is a simple front end that lets you invoke the compiler from the
	// command line.  The command line can contain the name of one or more
	// source files.  The compiler is invoked on each source file.

	public static void main (String[] args) throws Exception
	{

		// Create the compiler object

		Ex2Compiler compiler = new Ex2Compiler ();

		// For each filename listed on the command line ...

		for (int i = 0; i < args.length; ++i)
		{

			// Print the filename

			System.out.println ();
			System.out.println ("Compiling " + args[i] + " ...");

			// Compile the file

			compiler.compile (args[i]);
		}

		return;
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




// Token factory class for number.
//
// The value of a number is an Integer object, or null if there was a
// conversion error.

final class Ex2Number extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Get the value as a string

		String numString = scanner.tokenToString ();

		// Convert it to an Integer object

		try
		{
			token.value = new Integer (Integer.parseInt (numString, 10));
		}

		// If conversion error, print error message and return null

		catch (NumberFormatException e)
		{
			reportError (token, null,
				"Invalid number '" + numString + "'." );

			token.value = null;
		}

		// Assembled token

		return assemble;
	}
}




// Token factory class for lineEnd.
//
// A lineEnd is discarded after counting the line.

final class Ex2LineEnd extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Bump the line number

		scanner.countLine ();

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
  is being reduced.  (None of the factories in this example use the parameter.)

  Alternatively, it is possible to use link names to give a different
  nonterminal factory to each production.  For example, the productions
  with Expression on the left hand side are handled by three different
  nonterminal factory classes: Ex2ExpressionAdd, Ex2ExpressionSubtract, and
  the default nonterminal factory class.

  As you see, there are two possible methods for distinguishing between
  different productions with the same left hand side.  The choice of which
  method to use is a matter of taste and convenience.

  When a nonterminal factory reads a terminal symbol T from the production's
  right hand side, the nonterminal factory must be prepared to handle the
  possiblity that the value of T could be null.  This is true even if the token
  factory for T never returns a null value.  The reason is that during error
  repair, the parser may insert terminal symbols that are not present in the
  source file, and these error insertions always have null value.

  On the other hand, when a nonterminal factory reads a nonterminal symbol S
  from the production's right hand side, the value of S is always the result of
  calling the nonterminal factory for S.  If the nonterminal factory for S
  never returns null, then the value of S is never null.

  You will notice that not every production listed in the grammar specification
  has a nonterminal factory.  Productions without a nonterminal factory are
  handled by the default nonterminal factory.  The default nonterminal factory
  does the following:  (a) if the production has a non-empty right hand side,
  it returns the value of the first item on the right hand side;  (b) if the
  production has an empty right hand side, it returns null.

  Each nonterminal factory is an inner class.  This allows the nonterminal
  factory to access variables and functions in the compiler object.

->*/




// Nonterminal factory class for Statement.
//
// A Statement has null value.
//
// When this nonterminal factory is called, it prints the value of the
// expression on the standard output.

final class Ex2Statement extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		Integer value = (Integer) parser.rhsValue (0);

		// If value is an error insertion, do nothing

		if (value == null)
		{
			return null;
		}

		// Print the value

		System.out.println (value.intValue());

		// Return null value

		return null;
	}
}




// Nonterminal factory class for Expression{add}.
//
// The value of an Expression is an Integer object.  It is null if there is
// an error.

final class Ex2ExpressionAdd extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		Integer leftOperand = (Integer) parser.rhsValue (0);

		Integer rightOperand = (Integer) parser.rhsValue (2);

		// If either operand is an error insertion, return null to indicate error

		if ((leftOperand == null) || (rightOperand == null))
		{
			return null;
		}

		// Return the sum of the two operands

		return new Integer (leftOperand.intValue() + rightOperand.intValue());
	}
}




// Nonterminal factory class for Expression{subtract}.
//
// The value of an Expression is an Integer object.  It is null if there is
// an error.

final class Ex2ExpressionSubtract extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		Integer leftOperand = (Integer) parser.rhsValue (0);

		Integer rightOperand = (Integer) parser.rhsValue (2);

		// If either operand is an error insertion, return null to indicate error

		if ((leftOperand == null) || (rightOperand == null))
		{
			return null;
		}

		// Return the difference of the two operands

		return new Integer (leftOperand.intValue() - rightOperand.intValue());
	}
}




// Nonterminal factory class for Primary{paren}.
//
// The value of a Primary is an Integer object.  It is null if there is
// an error.

final class Ex2PrimaryParen extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Return the operand in parentheses

		return parser.rhsValue (1);
	}
}




}

