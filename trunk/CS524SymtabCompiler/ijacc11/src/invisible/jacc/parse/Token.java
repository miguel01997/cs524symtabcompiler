// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;


/*->

  Token objects are used to return tokens from a Preprocessor or TokenStream.

  Data fields within the Token object are declared public to allow efficient
  access.

->*/


public class Token 
{

	// The token number.  This indicates the type of token.
	//
	// A positive integer denotes an ordinary token, which is intended for
	// the ultimate consumer of the token stream (such as a parser).
	//
	// Zero denotes an end-of-file token.
	//
	// A negative integer denotes an escape token, which is intended to be
	// removed before the token reaches the ultimate consumer.

	public int number;


	// The token value.  This can be an arbitrary object, and it can be null.

	public Object value;


	// The source file where the token originated.  This can be null.

	public String file;


	// The line number within the source file where the token originated.  This
	// can be noPosition to indicate that the line number is unknown or
	// unspecified.
	//
	// It is expected that this field may be an approximation of the token's
	// actual position.

	public int line;


	// The column number within the line where the token originated.  This can
	// be noPosition to indicate that the column number is unknown or
	// unspecified.
	//
	// It is expected that this field may be an approximation of the token's
	// actual position.

	public int column;


	// Constant that defines an unknown or unspecified line or column.

	public static final int noPosition = ErrorOutput.noPosition;	// = 0x80000000


	// Constant that defines the end-of-file token.

	public static final int EOF = 0;


	// Constant that defines the insert-stream escape token.  For this token,
	// value contains a TokenStream object.  PreprocessorInclude recognizes
	// this escape token and replaces it with all the tokens of the stream.

	public static final int escapeInsertStream = -1;


	// Constructor to create an empty Token object.

	public Token ()
	{
		super();

		number = 0;
		value = null;
		file = null;
		line = noPosition;
		column = noPosition;

		return;
	}


	// Constructor to create a Token object with specified contents.

	public Token (int number, Object value, String file, int line, int column)
	{
		super();

		this.number = number;
		this.value = value;
		this.file = file;
		this.line = line;
		this.column = column;

		return;
	}


	// Constructor to create a Token object with the same contents as another.

	public Token (Token other)
	{
		super();

		this.number = other.number;
		this.value = other.value;
		this.file = other.file;
		this.line = other.line;
		this.column = other.column;

		return;
	}


	// Copies all the fields from the specified Token object to this Token object.

	public void copyFrom (Token other)
	{
		this.number = other.number;
		this.value = other.value;
		this.file = other.file;
		this.line = other.line;
		this.column = other.column;

		return;
	}


}

