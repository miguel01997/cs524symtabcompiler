// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;


/*->

  PrescannerJavaSourceClient is an interface that represents a client of a
  PrescannerJavaSource object.

->*/


public interface PrescannerJavaSourceClient 
{

	// The prescanner calls this routine when it encounters an invalid unicode
	// escape code.
	//
	// The fields token.file, token.line, and token.column contain the file
	// name, line number, and column number where the invalid escape sequence
	// is located.

	public void javaSourceInvalidEscape (Token token);


}

