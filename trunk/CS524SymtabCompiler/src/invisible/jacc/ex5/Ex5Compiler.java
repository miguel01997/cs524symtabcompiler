// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.ex5;

import java.util.Vector;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;

import invisible.jacc.parse.*;


/*->

Ex5Compiler is a Java class summarizer.  Its function is to read a Java source
file, and then print out a summary of the classes, interfaces, methods, and
fields.  These summaries are similar to the class summaries you can find in
the back of the Java Language Specification.

The grammar for this compiler is in file Ex5Grammar.jacc.  The grammar is a
complete LALR(1) grammar for the Java language, including Java 1.1 extensions.
Thus, we can produce class summaries that contain nested classes.

There are a few twists in generating consistent summaries.  For example, in
Java code the following three field declarations are equivalent:

	int[][] x;
	int[] x[];
	int x[][];

We accept any of these forms, but we convert them all into the first form, so
that in the summary, array brackets always appear immediately after the type.
Likewise, we convert any of the following three equivalent method declarations
into the first form:

	int[][] f(int y);
	int[] f(int y)[];
	int f(int y)[][];

Also, field declarations can define several fields in one statement.  For
example, the first declaration below is equivalent to the other three:

	int x, y, z[];

	int x;
	int y;
	int[] z;

When we encounter a statement that defines several fields, we split it up into
separate declarations.  So, in the summary each field is defined is a separate
statement.  In other words, we always output the second form shown above.


DESIGN OVERVIEW

This compiler follows the analysis-synthesis design.  Analysis-synthesis is
very commonly used in production compilers.

An analysis-synthesis compiler works in two stages.  First, it reads the input
file and builds up a data structure in memory that represents the entire
contents of the file.  This is called the "analysis" stage.  Then, it walks the
data structure to produce the compiler's output.  This is called the "synthesis"
stage.

In this compiler, we read the source code for each top-level class or interface
and produce a data structure that represents the complete signature of the
class or interface.  The signature of a class or interface includes the class
or interface header, plus the signatures of all its fields, methods,
constructors, nested classes, and nested interfaces.  Of course, if there are
nested classes or interfaces, their signatures include their own fields,
methods, constructors, and further nested classes and interfaces.

Because of the possiblity of nesting, our data structure is a tree.  Each leaf
in the tree contains the signature of a field, method, or constructor.  Each
internal node in the tree represents a class or interface.  The "children" of
a class or interface are all its fields, methods, constructors, and nested
classes or interfaces.  Since classes and interfaces can be nested to any
depth, our tree can have any depth.  The root node of the tree represents the
top-level class.

The following diagram shows the classes we used to build the tree:

    (PSig)
      |
      +----- (PSigContainer)
      |             |
      |             +---------- PSigClass
      |
      +----- PSigMethod
      |
      +----- PSigField

Parentheses indicate abstract classes, and lines indicate subclasses.

Abstract class PSig represents any node on the tree.  Abstract class
PSigContainer (a subclass of PSig) represents an internal node on the tree,
that is, a node that can have children.

Concrete class PSigClass (a subclass of PSigContainer) represents the signature
of a class or interface.

Concrete class PSigMethod (a subclass of PSig) represents the signature of a
method or constructor.  Concrete class PSigField (a subclass of PSig)
represents the signature of a field.

After building up this tree, we produce the summary by walking the tree and
printing out the signatures contained in the tree's nodes.

To construct the tree, we build it from the bottom up, using the parser's
stack to hold the various parts of the tree under construction.  To see how
this is done, consider a few productions:

  ClassDeclaration -> ModifiersOpt class identifier SuperOpt InterfacesOpt ClassBody;

  ClassBody -> { ClassBodyDeclarationsOpt };

  ClassBodyDeclarationsOpt -> ClassBodyDeclarations;
  ClassBodyDeclarationsOpt -> ;

  ClassBodyDeclarations -> ClassBodyDeclaration;
  ClassBodyDeclarations -> ClassBodyDeclarations ClassBodyDeclaration;

  ClassBodyDeclaration -> ClassMemberDeclaration;
  ClassBodyDeclaration -> StaticInitializer;
  ClassBodyDeclaration -> ConstructorDeclaration;

  ClassMemberDeclaration -> FieldDeclaration;
  ClassMemberDeclaration -> MemberDeclaration;
  ClassMemberDeclaration -> ClassDeclaration;
  ClassMemberDeclaration -> InterfaceDeclaration;

Because Invisible Jacc is an LR parser, the production for ClassDeclaration
won't be reduced until everything on its right hand side is reduced.  That
means the entire class body is parsed before ClassDeclaration is reduced.

As we parse the class body, we discover the signatures for all the class
members.  When we reduce FieldDeclaration, we put a PSigField object on the
parser's stack.  When we reduce MemberDeclaration or ConstructorDeclaration,
we put a PSigMethod object on the parser's stack.  When we reduce
ClassDeclaration or InterfaceDeclaration, we put a PSigClass object on the
parser's stack.

By the time we reduce ClassBodyDeclaration, we will have a PSig object on the
parser's stack;  it could be any of the concrete types that inherit from PSig.

When we reduce ClassBodyDeclarations, we put a Vector of PSig objects on the
parser's stack.  In the first production, we create a new Vector and add the
PSig object from ClassBodyDeclaration to it.  In the second production, we
take the existing Vector from ClassBodyDeclarations, and add the PSig object
from ClassBodyDeclaration.

By the time we reduce ClassBody, the parser's stack will contain a Vector of
PSig objects, which holds all the signatures in the class body.

Finally, when we reduce ClassDeclaration we can obtain the Vector of PSig
objects from the parser's stack.  We then use this Vector to construct a
PSigClass object, which we leave on the parser's stack.  The Vector contains
all of the class's child signatures.


CREATING THE SCANNER AND PARSER TABLES

There are two ways to create the scanner and parser tables:  you can use
the graphical interface, or you can use the command line.

To use the graphical interface, execute class invisible.jacc.gen.GenGUI.
When the window appears, enter invisible\jacc\ex5\Ex5Grammar.jacc as the
file name.  Check the boxes to "generate scanner table", "generate parser
table", "create Java source files", and "write output messages to file".
Then, click on "Generate".

To use the command line, execute class invisible.jacc.gen.GenMain with the
following command line:

        -o -j invisible\jacc\ex5\Ex5Grammar.jacc

In either case, Invisible Jacc creates Ex5GrammarScannerTable.java and
Ex5GrammarParserTable.java.

Note:  On Microsoft Windows, the filename is not case-sensitive.  On other
operating systems, the filename may or may not be case-sensitive depending
on the operating system.  (If you're using another operating system,
substitute the appropriate path separator character in place of backslash.)

Note:  If you omit the ".jacc" extension from the filename, Invisible Jacc
supplies the ".jacc" extension automatically.


RUNNING THE SAMPLE INPUT

To run the sample input, execute class invisible.jacc.ex5.Ex5Main with the
following command line:

        -4 invisible\jacc\ex5\Ex5Compiler.java

This produces a summary of the file Ex5Compiler.java.  The -4 option tells
the summarizer to include private members in the summary.  Refer to class
Ex5Main for information on available options.

File Ex5CompilerSummary.out contains the result of running the above command.

->*/


public class Ex5Compiler extends CompilerModel implements PrescannerJavaSourceClient
{


	// ----- Configuration Variables -----
	//
	// The client may alter these variables to configure the compiler.

	// The access level.  This determines which classes, interfaces, methods,
	// and fields are included in the summary.  Possible values are:
	//	1 = Include only public declarations.
	//	2 = Include public and protected declarations.
	//	3 = Include all non-private declarations.
	//	4 = Include all declarations.

	public int access = 2;

	// The indentation string.  This string is prepended to each line that
	// is indented.  If nested classes are used, this string is prepended
	// multiple times, as required, to show the nesting depth.

	public String indent = "    ";




	// ----- Per-Compiler Variables -----
	//
	// These variables are initialized when the compiler object is constructed.


	// This flag enables the use of debug token and nonterminal factories.

	static final boolean _debug = false;

	// The standard output object that is used as the destination for program
	// output.

	PrintStream _stdOut;

	// The condition number for "notInComment".

	int _conditionNotInComment;

	// The condition number for "inComment".

	int _conditionInComment;




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




	// ----- CompilerModel methods we are overriding -----




	// Given a filename, this function creates a Scanner object for scanning
	// the file.  The return value is null if the file could not be opened.

	public Scanner makeScanner (String filename)
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




	// ----- Compiler public interface -----




	// Constructor must create the scanner and parser tables.

	public Ex5Compiler (PrintStream stdOut, ErrorOutput errOut)
	{
		super ();

		// Validate the arguments

		if ((stdOut == null) || (errOut == null))
		{
			throw new NullPointerException ("Ex5Compiler.Ex5Compiler");
		}

		// Save the output destinations

		_stdOut = stdOut;
		_errOut = errOut;

		// Get our scanner and parser tables
                // NOTE: It is not possible to implement the parser table as a
                //   Java class, because the table is so large it creates a method
                //   exceeding 64K bytes in size. Sun's Java runtime 1.3 will not
                //   execute methods over 64K bytes (although earlier versions
                //   of the Java runtime did execute them without complaint).
                // NOTE: We have hardcoded the filename invisible\jacc\ex5\Ex5Grammar.gen.
                //   You may need to change the directory path for your system.

		if (readGenFile ("invisible\\jacc\\ex5\\Ex5Grammar.gen"))
		{
			throw new InternalError ("Ex5Compiler: Error reading generated file invisible\\jacc\\ex5\\Ex5Grammar.gen");
		}

		// Link the token factories to the scanner table

		_scannerTable.linkFactory ("identifier", "", new Ex5Identifier());

		_scannerTable.linkFactory ("integerLiteral", "illegal", new Ex5NumberIllegal());
		_scannerTable.linkFactory ("characterLiteral", "illegal", new Ex5CharacterIllegal());
		_scannerTable.linkFactory ("stringLiteral", "illegal", new Ex5StringIllegal());
		_scannerTable.linkFactory ("stringLiteral", "runOn", new Ex5StringRunOn());

		_scannerTable.linkFactory ("lineEnd", "", new Ex5LineEnd());

		_scannerTable.linkFactory ("beginComment", "", new Ex5BeginComment());

		_scannerTable.linkFactory ("endComment", "", new Ex5EndComment());

		_scannerTable.linkFactory ("illegalChar", "", new Ex5IllegalChar());

		TokenFactory reservedKeyword = new Ex5ReservedKeyword();
		_scannerTable.linkFactory ("const", "", reservedKeyword);
		_scannerTable.linkFactory ("goto", "", reservedKeyword);

		// Link condition numbers

		_conditionNotInComment = _scannerTable.lookupCondition ("notInComment");
		_conditionInComment = _scannerTable.lookupCondition ("inComment");

		// Link the nonterminal factories to the parser table

		NonterminalFactory typeDeclaration = new Ex5TypeDeclaration();
		_parserTable.linkFactory ("TypeDeclaration", "class", typeDeclaration);
		_parserTable.linkFactory ("TypeDeclaration", "interface", typeDeclaration);

		_parserTable.linkFactory ("SimpleName", "", new Ex5SimpleName());
		_parserTable.linkFactory ("QualifiedName", "", new Ex5QualifiedName());

		_parserTable.linkFactory ("DimsOpt", "none", new Ex5DimsOptNone());
		_parserTable.linkFactory ("Dims", "first", new Ex5DimsFirst());
		_parserTable.linkFactory ("Dims", "next", new Ex5DimsNext());

		NonterminalFactory primitiveOrVoid = new Ex5PrimitiveOrVoid();
		_parserTable.linkFactory ("PrimitiveType", "boolean", primitiveOrVoid);
		_parserTable.linkFactory ("IntegralType", "byte", primitiveOrVoid);
		_parserTable.linkFactory ("IntegralType", "short",primitiveOrVoid);
		_parserTable.linkFactory ("IntegralType", "int", primitiveOrVoid);
		_parserTable.linkFactory ("IntegralType", "long", primitiveOrVoid);
		_parserTable.linkFactory ("IntegralType", "char", primitiveOrVoid);
		_parserTable.linkFactory ("FloatingPointType", "float", primitiveOrVoid);
		_parserTable.linkFactory ("FloatingPointType", "double", primitiveOrVoid);
		_parserTable.linkFactory ("VoidType", "", primitiveOrVoid);

		_parserTable.linkFactory ("ClassOrInterfaceType", "", new Ex5ClassOrInterfaceType());
		_parserTable.linkFactory ("ArrayType", "primitive", new Ex5ArrayTypePrimitive());
		_parserTable.linkFactory ("ArrayType", "reference", new Ex5ArrayTypeReference());

		_parserTable.linkFactory ("VariableDeclaratorId", "", new Ex5VariableDeclaratorId());

		_parserTable.linkFactory ("VariableDeclarators", "first", new Ex5VariableDeclaratorsFirst());
		_parserTable.linkFactory ("VariableDeclarators", "next", new Ex5VariableDeclaratorsNext());

		_parserTable.linkFactory ("MethodDeclarator", "", new Ex5MethodDeclarator());
		_parserTable.linkFactory ("ConstructorDeclarator", "", new Ex5ConstructorDeclarator());

		_parserTable.linkFactory ("FormalParameter", "", new Ex5FormalParameter());
		_parserTable.linkFactory ("FormalParameter", "modified", new Ex5FormalParameterModified());
		_parserTable.linkFactory ("FormalParameterList", "next", new Ex5FormalParameterListNext());

		NonterminalFactory modifierOrNone = new Ex5ModifierOrNone();
		_parserTable.linkFactory ("Modifier", "public", modifierOrNone);
		_parserTable.linkFactory ("Modifier", "protected", modifierOrNone);
		_parserTable.linkFactory ("Modifier", "private", modifierOrNone);
		_parserTable.linkFactory ("Modifier", "static", modifierOrNone);
		_parserTable.linkFactory ("Modifier", "abstract", modifierOrNone);
		_parserTable.linkFactory ("Modifier", "final", modifierOrNone);
		_parserTable.linkFactory ("Modifier", "native", modifierOrNone);
		_parserTable.linkFactory ("Modifier", "synchronized", modifierOrNone);
		_parserTable.linkFactory ("Modifier", "transient", modifierOrNone);
		_parserTable.linkFactory ("Modifier", "volatile", modifierOrNone);
		_parserTable.linkFactory ("ModifiersOpt", "none", modifierOrNone);

		_parserTable.linkFactory ("Modifiers", "next", new Ex5ModifiersNext());

		NonterminalFactory coiTypeListFirst = new Ex5ClassOrInterfaceTypeListFirst();
		NonterminalFactory coiTypeListNext = new Ex5ClassOrInterfaceTypeListNext();
		NonterminalFactory soeiFirst = new Ex5SuperOrExtendsInterfacesFirst();
		NonterminalFactory interfacesOrThrows = new Ex5InterfacesOrThrows();
		_parserTable.linkFactory ("Super", "", soeiFirst);
		_parserTable.linkFactory ("Interfaces", "", interfacesOrThrows);
		_parserTable.linkFactory ("Throws", "", interfacesOrThrows);
		_parserTable.linkFactory ("InterfaceTypeList", "first", coiTypeListFirst);
		_parserTable.linkFactory ("InterfaceTypeList", "next", coiTypeListNext);
		_parserTable.linkFactory ("ClassTypeList", "first", coiTypeListFirst);
		_parserTable.linkFactory ("ClassTypeList", "next", coiTypeListNext);
		_parserTable.linkFactory ("ExtendsInterfaces", "first", soeiFirst);
		_parserTable.linkFactory ("ExtendsInterfaces", "next", coiTypeListNext);

		NonterminalFactory methodHeader = new Ex5MethodHeader();
		_parserTable.linkFactory ("MethodHeader", "notVoid", methodHeader);
		_parserTable.linkFactory ("MethodHeader", "void", methodHeader);
		_parserTable.linkFactory ("ConstructorDeclaration", "", new Ex5ConstructorDeclaration());

		_parserTable.linkFactory ("FieldDeclaration", "", new Ex5FieldDeclaration());

		NonterminalFactory bodyDeclarationsFirst = new Ex5BodyDeclarationsFirst();
		NonterminalFactory bodyDeclarationsNext = new Ex5BodyDeclarationsNext();
		NonterminalFactory classOrInterfaceBody = new Ex5ClassOrInterfaceBody();
		_parserTable.linkFactory ("ClassBodyDeclarations", "first", bodyDeclarationsFirst);
		_parserTable.linkFactory ("ClassBodyDeclarations", "next", bodyDeclarationsNext);
		_parserTable.linkFactory ("ClassBody", "", classOrInterfaceBody);
		_parserTable.linkFactory ("InterfaceMemberDeclarations", "first", bodyDeclarationsFirst);
		_parserTable.linkFactory ("InterfaceMemberDeclarations", "next", bodyDeclarationsNext);
		_parserTable.linkFactory ("InterfaceBody", "", classOrInterfaceBody);

		_parserTable.linkFactory ("ClassDeclaration", "", new Ex5ClassDeclaration());
		_parserTable.linkFactory ("InterfaceDeclaration", "", new Ex5InterfaceDeclaration());

		// If debug mode, activate debugging features.  This checks the scanner
		// and parser tables for internal consistency, and checks all the strings
		// passed to the linkFactory functions to ensure they match names in the
		// grammar specification.  It also installs tracing code, which outputs
		// a message on every call to a token factory or nonterminal factory.

		if (_debug)
		{
			if (setDebugMode (true, true))
			{
				throw new InternalError ("Ex5Compiler: Consistency check failed.");
			}
		}

		// Done

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

final class Ex5Identifier extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Get the value as a string

		token.value = scanner.tokenToString ();

		// Assembled token

		return assemble;
	}
}




// Token factory class for reserved keywords.

final class Ex5ReservedKeyword extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Report the error

		reportError (token, null,
							   "Reserved keyword '" + scanner.tokenToString () + "'." );

		// Discard token

		return discard;
	}
}




// Token factory class for illegal numeric literal.

final class Ex5NumberIllegal extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Report the error

		reportError (token, null, "Illegal number.");

		// Assemble token

		return assemble;
	}
}




// Token factory class for illegal character literal.

final class Ex5CharacterIllegal extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Report the error

		reportError (token, null, "Illegal character literal.");

		// Assemble token

		return assemble;
	}
}




// Token factory class for illegal string literal.

final class Ex5StringIllegal extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Report the error

		reportError (token, null, "Illegal string (invalid escape sequence).");

		// Assemble token

		return assemble;
	}
}




// Token factory class for run-on string literal.

final class Ex5StringRunOn extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Report the error

		reportError (token, null, "Run-on string.");

		// Assemble token

		return assemble;
	}
}




// Token factory class for lineEnd.
//
// A lineEnd is discarded after counting the line.

final class Ex5LineEnd extends TokenFactory
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

final class Ex5BeginComment extends TokenFactory
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

final class Ex5EndComment extends TokenFactory
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

final class Ex5IllegalChar extends TokenFactory
{
	public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
	{

		// Tell the client

		scanner.client().scannerUnmatchedToken (scanner, token);

		// Discard token

		return discard;
	}
}




// ----- Parser Stack Classes -----


/*->

The following classes define objects that we use on the parser's stack.

Refer to the comments at the start of this file for a detailed description of
how we use PSig and its subclasses to build up a tree containing the complete
signature of a top-level class.

The other classes defined here are used to build up the signatures of
individual fields, methods, and constructors.

->*/




// PDim - A dimension.
//
// A PDim object holds an integer which represents a number of array dimensions.
//
// We use PDim objects to represent the values of these nonterminals:
//		Dims  DimsOpt

static class PDim
{

	// The number of array dimensions

	public int dim;

	// The constructor just initializes the object

	public PDim (int dim)
	{
		this.dim = dim;
		return;
	}

}




// PType - A Java type.
//
// A PType object holds two pieces of information:  a String which represents
// the base type, and an integer which represents the number of array dimensions.
//
// For example, the type int[][] would be represented as a PType with base equal
// to "int" and dim equal to 2.
//
// We use PType objects to represent the values of these nonterminals:
//		Type  PrimitiveType  NumericType  IntegralType  FloatingPointType
//		ReferenceType  ClassOrInterfaceType  ClassType  InterfaceType
//      ArrayType  VoidType

static class PType
{

	// The base type

	public String base;

	// The number of array dimensions

	public int dim;

	// The constructor just initializes the object

	public PType (String base, int dim)
	{
		this.base = base;
		this.dim = dim;
		return;
	}

	// Given a StringBuffer, this function appends the type, plus the
	// specified number of extra array dimensions, plus a trailing blank.

	public void append (StringBuffer buf, int extraDim)
	{
		buf.append (base);
		for (int i = 0; i < dim + extraDim; ++i)
		{
			buf.append ("[]");
		}
		buf.append (" ");
		return;
	}

}




// PVarDec - A Java variable declaration.
//
// A PVarDec object holds two pieces of information:  a String which represents
// the variable name, and an integer which represents the number of array dimensions.
//
// We use PVarDec objects to represent the values of these nonterminals:
//		VariableDeclarator  VariableDeclaratorId

static class PVarDec
{

	// The variable name

	public String var;

	// The number of array dimensions

	public int dim;

	// The constructor just initializes the object

	public PVarDec (String var, int dim)
	{
		this.var = var;
		this.dim = dim;
		return;
	}

}




// PMethDec - A Java method declaration.
//
// A PMethDec object holds two pieces of information:  a String which represents
// the method name and formal parameter list, and an integer which represents
// the number of array dimensions appearing after the formal parameters.
//
// We use PMethDec objects to represent the values of these nonterminals:
//		MethodDeclarator  ConstructorDeclarator

static class PMethDec
{

	// The method name and argument list

	public String meth;

	// The number of array dimensions

	public int dim;

	// The constructor just initializes the object

	public PMethDec (String meth, int dim)
	{
		this.meth = meth;
		this.dim = dim;
		return;
	}

}




// PMod - A set of Java modifiers.
//
// A PMod object holds a set of bit flags which represent a set of modifiers.
// We use the standard bit flags from the Java VM Spec.
//
// We use PMod objects to represent the values of these nonterminals:
//		Modifier  Modifiers  ModifiersOpt

static class PMod
{

	// The standard bit flags

	public static final int ACC_PUBLIC = 0x0001;
	public static final int ACC_PRIVATE = 0x0002;
	public static final int ACC_PROTECTED = 0x0004;
	public static final int ACC_STATIC = 0x0008;
	public static final int ACC_FINAL = 0x0010;
	public static final int ACC_SYNCHRONIZED = 0x0020;
	public static final int ACC_VOLATILE = 0x0040;
	public static final int ACC_TRANSIENT = 0x0080;
	public static final int ACC_NATIVE = 0x0100;
	public static final int ACC_INTERFACE = 0x0200;	// Not used
	public static final int ACC_ABSTRACT = 0x0400;

	// The modifier bit flags

	public int mod;

	// The constructor just initializes the object

	public PMod (int mod)
	{
		this.mod = mod;
		return;
	}

	// Given a StringBuffer, this function appends the text version of the modifier.

	public void append (StringBuffer buf)
	{
		if ((mod & ACC_PUBLIC) != 0)
		{
			buf.append ("public ");
		}
		if ((mod & ACC_PROTECTED) != 0)
		{
			buf.append ("protected ");
		}
		if ((mod & ACC_PRIVATE) != 0)
		{
			buf.append ("private ");
		}
		if ((mod & ACC_STATIC) != 0)
		{
			buf.append ("static ");
		}
		if ((mod & ACC_ABSTRACT) != 0)
		{
			buf.append ("abstract ");
		}
		if ((mod & ACC_FINAL) != 0)
		{
			buf.append ("final ");
		}
		if ((mod & ACC_NATIVE) != 0)
		{
			buf.append ("native ");
		}
		if ((mod & ACC_SYNCHRONIZED) != 0)
		{
			buf.append ("synchronized ");
		}
		if ((mod & ACC_TRANSIENT) != 0)
		{
			buf.append ("transient ");
		}
		if ((mod & ACC_VOLATILE) != 0)
		{
			buf.append ("volatile ");
		}
		return;
	}

	// This function returns true if this modifier is visible at the specified
	// access level.  Access levels are:
	//	1 = public
	//	2 = public or protected
	//	3 = non-private
	//	4 = all

	public boolean isVisible (int access)
	{
		switch (access)
		{
		case 1:
			return (mod & ACC_PUBLIC) != 0;
		case 2:
			return (mod & (ACC_PUBLIC | ACC_PROTECTED)) != 0;
		case 3:
			return (mod & ACC_PRIVATE) == 0;
		}
		return true;
	}

}




// PSig - Signature.
//
// PSig is an abstract class that represents the signature of a class,
// interface, field, method, or constructor.
//
// A PSig object holds a set of modifiers.
//
// We use PSig objects to represent the values of these nonterminals:
//		ClassBodyDeclaration  ClassMemberDeclaration  InterfaceMemberDeclaration
//
// In addition, we use a Vector of PSig objects to represent the values
// of these nonterminals:
//		ClassBodyDeclarations  ClassBodyDeclarationsOpt  ClassBody
//		InterfaceMemberDeclarations  InterfaceMemberDeclarationsOpt
//		InterfaceBody

static abstract class PSig
{

	// The modifiers for this signature

	private PMod modifiers;

	// The constructor just saves the modifiers

	public PSig (PMod modifiers)
	{
		this.modifiers = modifiers;
		return;
	}

	// This function returns true if the signature is visible at the
	// specified access level.

	public boolean isVisible (int access)
	{
		return modifiers.isVisible (access);
	}

	// Allocate a StringBuffer and write the specified number of copies of
	// the indentation string into it.

	protected StringBuffer startIndent (String indent, int level)
	{

		// Allocate the buffer

		StringBuffer buf = new StringBuffer();

		// Indent the specified number of levels

		for (int i = 0; i < level; ++i)
		{
			buf.append (indent);
		}

		// Return the buffer

		return buf;

	}

	// Append our modifiers to the StringBuffer.

	public void appendModifiers (StringBuffer buf)
	{
		modifiers.append (buf);
		return;
	}

	// This function prints the signature, if it's visible at the specified
	// access.  The level parameter is the number of copies of the indent
	// string to prepend to each line.

	public abstract void print (PrintStream out, int access, String indent, int level);

}




// PSigContainer - Container signature.
//
// PSigContainer is an abstract class that represents a signature that can
// contain child signatures.
//
// A PSigContainer object holds a Vector, each of whose elements is a child
// signature.

static abstract class PSigContainer extends PSig
{

	// The children signatures

	private Vector children;

	// The constructor saves the modifiers and children.

	public PSigContainer (PMod modifiers, Vector children)
	{
		super (modifiers);
		this.children = children;
		return;
	}

	// This function prints all the child signatures.

	protected void printChildren (PrintStream out, int access, String indent, int level)
	{
		if (children != null)
		{
			for (int i = 0; i < children.size(); ++i)
			{
				((PSig)children.elementAt(i)).print (out, access, indent, level);
			}
		}
		return;
	}

}




// PSigClass - Signature for a class or interface.
//
// A PSigClass object represents the signature of a class or interface.  It
// contains three items:  a set of modifiers, a String which contains the
// header (beginning with the keyword "class" or "interface"), and a Vector
// which contains the signatures of all the members of the class or interface.
// (Members can include fields, methods, constructors, nested classes, and
// nested interfaces.)
//
// We use PSigClass objects to represent the values of these nonterminals:
//		ClassDeclaration  InterfaceDeclaration

static class PSigClass extends PSigContainer
{

	// Our signature, not including modifiers

	private String sig;

	// The constructor saves the modifiers, signature, and children.

	public PSigClass (PMod modifiers, String sig, Vector children)
	{
		super (modifiers, children);
		this.sig = sig;
		return;
	}

	// This function prints the signature, if it's visible at the specified
	// access.  The level parameter is the number of copies of the indent
	// string to prepend to each line.

	public void print (PrintStream out, int access, String indent, int level)
	{

		// If we're visible ...

		if (isVisible (access))
		{

			// Print the class header

			StringBuffer buf = startIndent (indent, level);
			appendModifiers (buf);
			buf.append (sig);
			out.println (buf.toString());

			// Print the open brace

			buf = startIndent (indent, level);
			buf.append ("{");
			out.println (buf.toString());

			// Print the child signatures, indenting one more level

			printChildren (out, access, indent, level + 1);

			// Print the close brace

			buf = startIndent (indent, level);
			buf.append ("};");
			out.println (buf.toString());
		}

		return;
	}

}




// PSigMethod - Signature for a method or constructor.
//
// A PSigMethod object represents the signature of a method or constructor.
// It contains two items:  a set of modifiers, and a String which gives the
// signature.
//
// We use PSigMethod objects to represent the values of these nonterminals:
//		MethodHeader  MethodDeclaration  AbstractMethodDeclaration

static class PSigMethod extends PSig
{

	// Our signature, not including modifiers

	private String sig;

	// The constructor saves the modifiers and signature.

	public PSigMethod (PMod modifiers, String sig)
	{
		super (modifiers);
		this.sig = sig;
		return;
	}

	// This function prints the signature, if it's visible at the specified
	// access.  The level parameter is the number of copies of the indent
	// string to prepend to each line.

	public void print (PrintStream out, int access, String indent, int level)
	{

		// If we're visible ...

		if (isVisible (access))
		{

			// Print the signature

			StringBuffer buf = startIndent (indent, level);
			appendModifiers (buf);
			buf.append (sig);
			buf.append (";");
			out.println (buf.toString());
		}

		return;
	}

}




// PSigField - Signature for a field.
//
// A PSigField object represents the signatures of all the fields declared
// in a single statement.  It contains two items:  a set of modifiers, and
// a Vector.  The Vector contains one or more Strings, each of which contains
// the signature of one field.
//
// We use PSigField objects to represent the values of these nonterminals:
//		FieldDeclaration  ConstantDeclaration

static class PSigField extends PSig
{

	// Our signatures, not including modifiers.
	// Each element of the vector is a String giving one signature.

	private Vector sigs;

	// The constructor saves the modifiers and signatures.

	public PSigField (PMod modifiers, Vector sigs)
	{
		super (modifiers);
		this.sigs = sigs;
		return;
	}

	// This function prints the signature, if it's visible at the specified
	// access.  The level parameter is the number of copies of the indent
	// string to prepend to each line.

	public void print (PrintStream out, int access, String indent, int level)
	{

		// If we're visible ...

		if (isVisible (access))
		{

			// Print the signatures

			for (int i = 0; i < sigs.size(); ++i)
			{
				StringBuffer buf = startIndent (indent, level);
				appendModifiers (buf);
				buf.append ((String) sigs.elementAt(i));
				buf.append (";");
				out.println (buf.toString());
			}
		}

		return;
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
  is being reduced.

  Alternatively, it is possible to use link names to give a different
  nonterminal factory to each production.

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




// Nonterminal factory class for TypeDeclaration.
//
// When this nonterminal factory is called, it prints the signature
// on the standard output.

final class Ex5TypeDeclaration extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		PSig signature = (PSig) parser.rhsValue (0);

		// Print it

		signature.print (_stdOut, access, indent, 0);

		// Return null value

		return null;
	}
}




// Nonterminal factory classes for Name, SimpleName, QualifiedName.
//
// The value of any of these nonterminals is a String object.  It cannot
// be null.

final class Ex5SimpleName extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		String identifier = (String) parser.rhsValue (0);

		// If the identifier is an error insertion, substitute "???"

		if (identifier == null)
		{
			identifier = "???";
		}

		// Return a String object

		return identifier;
	}
}


final class Ex5QualifiedName extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		String name = (String) parser.rhsValue (0);

		String identifier = (String) parser.rhsValue (2);

		// If the identifier is an error insertion, substitute "???"

		if (identifier == null)
		{
			identifier = "???";
		}

		// Return a String containing the qualified name

		return name + "." + identifier;
	}
}




// Nonterminal factory classes for DimsOpt, Dims.
//
// The value of any of these nonterminals is a PType object.  It cannot
// be null.
//
// In the returned PType object, the name field is null, and the dim field
// is the number of array dimensions.  (Although it may seem that the name
// field is wasted, in fact it is eventually used to hold the name of the
// entity that the array brackets are attached to.)

final class Ex5DimsOptNone extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Return a PDim object with zero dimensions

		return new PDim (0);
	}
}


final class Ex5DimsFirst extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Return a PDim object with one dimension

		return new PDim (1);
	}
}


final class Ex5DimsNext extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		PDim dims = (PDim) parser.rhsValue (0);

		// Increment the dimension

		dims.dim++;

		// Return the resulting PDim

		return dims;
	}
}




// Nonterminal factory classes for Type, PrimitiveType, NumericType,
// IntegralType, FloatingPointType, ReferenceType, ClassOrInterfaceType,
// ClassType, InterfaceType, ArrayType, VoidType.
//
// The value of any of these nonterminals is a PType object.  It cannot
// be null.
//
// In the PType object, the base field is the base name of the type, and
// the dim field is the number of array dimensions.

final class Ex5PrimitiveOrVoid extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Return a PType object.
		// Note that the parameter is the Java type code.

		switch (param)
		{
		case 'B':
			return new PType ("byte", 0);
		case 'C':
			return new PType ("char", 0);
		case 'D':
			return new PType ("double", 0);
		case 'F':
			return new PType ("float", 0);
		case 'I':
			return new PType ("int", 0);
		case 'J':
			return new PType ("long", 0);
		case 'S':
			return new PType ("short", 0);
		case 'V':
			return new PType ("void", 0);
		case 'Z':
			return new PType ("boolean", 0);
		}

		throw new InternalCompilerException();
	}
}


final class Ex5ClassOrInterfaceType extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		String name = (String) parser.rhsValue (0);

		// Return a PType object

		return new PType (name, 0);
	}
}


final class Ex5ArrayTypePrimitive extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		PType type = (PType) parser.rhsValue (0);

		PDim dims = (PDim) parser.rhsValue (1);

		// Adjust the number of dimensions

		type.dim += dims.dim;

		// Return the PType

		return type;
	}
}


final class Ex5ArrayTypeReference extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		String name = (String) parser.rhsValue (0);

		PDim dims = (PDim) parser.rhsValue (1);

		// Return a PType object

		return new PType (name, dims.dim);
	}
}




// Nonterminal factory classes for VariableDeclarator, VariableDeclaratorId
//
// The value of any of these nonterminals is a PVarDec object.  It cannot
// be null.
//
// Notice that we throw away variable initializers, because we don't need
// them for the class summary.

final class Ex5VariableDeclaratorId extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		String identifier = (String) parser.rhsValue (0);

		PDim dimsOpt = (PDim) parser.rhsValue (1);

		// If the identifier is an error insertion, substitute "???"

		if (identifier == null)
		{
			identifier = "???";
		}

		// Return a PVarDec object

		return new PVarDec (identifier, dimsOpt.dim);
	}
}




// Nonterminal factory classes for VariableDeclarators.
//
// The value of any of these nonterminals is a Vector object.  Each member
// of the Vector object is a PVarDec object that contains a declaration of
// a single variable.

final class Ex5VariableDeclaratorsFirst extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		PVarDec declarator = (PVarDec) parser.rhsValue (0);

		// Return a one-element vector

		Vector declarators = new Vector();
		declarators.addElement (declarator);
		return declarators;
	}
}


final class Ex5VariableDeclaratorsNext extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		Vector declarators = (Vector) parser.rhsValue (0);

		PVarDec declarator = (PVarDec) parser.rhsValue (2);

		// Add a new declarator

		declarators.addElement (declarator);
		return declarators;
	}
}




// Nonterminal factory classes for MethodDeclarator, ConstructorDeclarator.
//
// The value of any of these nonterminals is a PMethDec object.  It cannot
// be null.

final class Ex5MethodDeclarator extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		String identifier = (String) parser.rhsValue (0);

		String formalList = (String) parser.rhsValue (2);

		PDim dimsOpt = (PDim) parser.rhsValue (4);

		// If the identifier is an error insertion, substitute "???"

		if (identifier == null)
		{
			identifier = "???";
		}

		// If there's no formal list, use an empty string

		if (formalList == null)
		{
			formalList = "";
		}

		// Return a PMethDec object

		return new PMethDec (identifier + " (" + formalList + ")", dimsOpt.dim);
	}
}


final class Ex5ConstructorDeclarator extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		String name = (String) parser.rhsValue (0);

		String formalList = (String) parser.rhsValue (2);

		// If there's no formal list, use an empty string

		if (formalList == null)
		{
			formalList = "";
		}

		// Return a PMethDec object

		return new PMethDec (name + " (" + formalList + ")", 0);
	}
}




// Nonterminal factory classes for FormalParameterListOpt, FormalParameterList,
// FormalParameter.
//
// The value of any of these nonterminals is a String object.  It cannot
// be null, except that the "Opt" nonterminal can be null if the list is
// empty.  The string contains a comma-delimited list of formal parameters.
//
// Notice that we throw away modifiers on formal parameters, since they're not
// part of the function signature.

final class Ex5FormalParameter extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		PType type = (PType) parser.rhsValue (0);

		PVarDec varDecId = (PVarDec) parser.rhsValue (1);

		// Build the string in a StringBuffer

		StringBuffer buf = new StringBuffer ();
		type.append (buf, varDecId.dim);
		buf.append (varDecId.var);

		// Return a String object

		return buf.toString();
	}
}


final class Ex5FormalParameterModified extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		PType type = (PType) parser.rhsValue (1);

		PVarDec varDecId = (PVarDec) parser.rhsValue (2);

		// Build the string in a StringBuffer

		StringBuffer buf = new StringBuffer ();
		buf.append (type.base);
		for (int i = 0; i < type.dim + varDecId.dim; ++i)
		{
			buf.append ("[]");
		}
		buf.append (" ");
		buf.append (varDecId.var);

		// Return a String object

		return buf.toString();
	}
}


final class Ex5FormalParameterListNext extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		String formalList = (String) parser.rhsValue (0);

		String formalParam = (String) parser.rhsValue (2);

		// Return a String object

		return formalList + ", " + formalParam;
	}
}




// Nonterminal factory classes for Modifier, Modifiers, ModifiersOpt.
//
// The value of any of these nonterminals is a PMod object.  It cannot
// be null.
//
// In the PMod object, the mod field is the set of modifier bit flags.

final class Ex5ModifierOrNone extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Return a PMod object.
		// Note that the parameter is the bit flag.

		return new PMod (param);
	}
}


final class Ex5ModifiersNext extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		PMod modifiers = (PMod) parser.rhsValue (0);

		PMod modifier = (PMod) parser.rhsValue (1);

		// Combine the bit flags

		modifiers.mod |= modifier.mod;

		// Return the PMod

		return modifiers;
	}
}




// Nonterminal factory classes for SuperOpt, Super, InterfacesOpt, Interfaces,
// InterfaceTypeList, ThrowsOpt, Throws, ClassTypeList, ExtendsInterfacesOpt,
// ExtendsInterfaces.
//
// The value of any of these nonterminals is a String object.  It contains a
// comma-delimited list of class or interface names.  The value cannot be
// null, except that the "Opt" nonterminals are null if the list is absent.

final class Ex5ClassOrInterfaceTypeListFirst extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		PType type = (PType) parser.rhsValue (0);

		// Return a String object containing the class or interface name

		return type.base;
	}
}


final class Ex5ClassOrInterfaceTypeListNext extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		String list = (String) parser.rhsValue (0);

		PType type = (PType) parser.rhsValue (2);

		// Return a String object for a comma-delimited list

		return list + ", " + type.base;
	}
}


final class Ex5SuperOrExtendsInterfacesFirst extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		PType type = (PType) parser.rhsValue (1);

		// Return a String object containing the class or interface name

		return type.base;
	}
}


final class Ex5InterfacesOrThrows extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		String list = (String) parser.rhsValue (1);

		// Return a String object containing the list

		return list;
	}
}




// Nonterminal factory class for MethodHeader, MethodDeclaration,
// ConstructorDeclaration, AbstractMethodDeclaration.
//
// The value of any of these nonterminals is a PSigMethod object that contains
// the method signature.  It cannot be null.

final class Ex5MethodHeader extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		PMod modifiers = (PMod) parser.rhsValue (0);

		PType type = (PType) parser.rhsValue (1);

		PMethDec methodDec = (PMethDec) parser.rhsValue (2);

		String throwsOpt = (String) parser.rhsValue (3);

		// Construct the signature

		StringBuffer buf = new StringBuffer();

		type.append (buf, methodDec.dim);
		buf.append (methodDec.meth);

		if (throwsOpt != null)
		{
			buf.append (" throws ");
			buf.append (throwsOpt);
		}

		// Return the PSigMethod object

		return new PSigMethod (modifiers, buf.toString());
	}
}


final class Ex5ConstructorDeclaration extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		PMod modifiers = (PMod) parser.rhsValue (0);

		PMethDec constructorDec = (PMethDec) parser.rhsValue (1);

		String throwsOpt = (String) parser.rhsValue (2);

		// Construct the signature

		StringBuffer buf = new StringBuffer();

		buf.append (constructorDec.meth);

		if (throwsOpt != null)
		{
			buf.append (" throws ");
			buf.append (throwsOpt);
		}

		// Return the PSigMethod object

		return new PSigMethod (modifiers, buf.toString());
	}
}




// Nonterminal factory class for FieldDeclaration, ConstantDeclaration.
//
// The value of any of these nonterminals is a PSigField object that contains
// the field signatures.  It cannot be null.

final class Ex5FieldDeclaration extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		PMod modifiers = (PMod) parser.rhsValue (0);

		PType type = (PType) parser.rhsValue (1);

		Vector declarators = (Vector) parser.rhsValue (2);

		// Construct the signatures

		Vector sigs = new Vector();

		for (int i = 0; i < declarators.size(); ++i)
		{
			PVarDec varDec = (PVarDec) declarators.elementAt(i);

			StringBuffer buf = new StringBuffer();

			type.append (buf, varDec.dim);
			buf.append (varDec.var);

			sigs.addElement (buf.toString());
		}

		// Return the PSigField object

		return new PSigField (modifiers, sigs);
	}
}




// Nonterminal factory classes for ClassBody, ClassBodyDeclarations,
// InterfaceBody, InterfaceMemberDeclarations.
//
// The value of any of these nonterminals is a Vector object.  Each member
// of the Vector object is a PSig object that contains the signature of
// method, field, constructor, nested class, or nested interface.  The value
// can be null if there are no child signatures.

final class Ex5BodyDeclarationsFirst extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		PSig signature = (PSig) parser.rhsValue (0);

		// Note that the nonterminal ClassBodyDeclarations can be null if we
		// encounter a static initializer or instance initializer.  Therefore,
		// we must check for null signature.

		if (signature == null)
		{
			return null;
		}

		// Return a one-element vector

		Vector children = new Vector();
		children.addElement (signature);
		return children;
	}
}


final class Ex5BodyDeclarationsNext extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		Vector children = (Vector) parser.rhsValue (0);

		PSig signature = (PSig) parser.rhsValue (1);

		// Note that the nonterminal ClassBodyDeclarations can be null if we
		// encounter a static initializer or instance initializer.  Therefore,
		// we must check for null signature.

		if (signature == null)
		{
			return children;
		}

		// If there's no vector, create one

		if (children == null)
		{
			children = new Vector();
		}

		// Add a new child signature

		children.addElement (signature);
		return children;
	}
}


final class Ex5ClassOrInterfaceBody extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		Vector children = (Vector) parser.rhsValue (1);

		// Return the vector of child signatures, or null if none

		return children;
	}
}




// Nonterminal factory class for ClassDeclaration, InterfaceDeclaration.
//
// The value of any of these nonterminals is a PSigClass object that contains
// the class or interface signature.  It cannot be null.

final class Ex5ClassDeclaration extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		PMod modifiers = (PMod) parser.rhsValue (0);

		String identifier = (String) parser.rhsValue (2);

		String superOpt = (String) parser.rhsValue (3);

		String interfacesOpt = (String) parser.rhsValue (4);

		Vector classBody = (Vector) parser.rhsValue (5);

		// If the identifier is an error insertion, substitute "???"

		if (identifier == null)
		{
			identifier = "???";
		}

		// Construct the signature

		StringBuffer buf = new StringBuffer();

		buf.append ("class ");
		buf.append (identifier);

		if (superOpt != null)
		{
			buf.append (" extends ");
			buf.append (superOpt);
		}

		if (interfacesOpt != null)
		{
			buf.append (" implements ");
			buf.append (interfacesOpt);
		}

		// Return the PSigClass object

		return new PSigClass (modifiers, buf.toString(), classBody);
	}
}


final class Ex5InterfaceDeclaration extends NonterminalFactory
{
	public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
	{

		// Get items from value stack

		PMod modifiers = (PMod) parser.rhsValue (0);

		String identifier = (String) parser.rhsValue (2);

		String extendsInterfacesOpt = (String) parser.rhsValue (3);

		Vector classBody = (Vector) parser.rhsValue (4);

		// If the identifier is an error insertion, substitute "???"

		if (identifier == null)
		{
			identifier = "???";
		}

		// Construct the signature

		StringBuffer buf = new StringBuffer();

		buf.append ("interface ");
		buf.append (identifier);

		if (extendsInterfacesOpt != null)
		{
			buf.append (" extends ");
			buf.append (extendsInterfacesOpt);
		}

		// Return the PSigClass object

		return new PSigClass (modifiers, buf.toString(), classBody);
	}
}




}

