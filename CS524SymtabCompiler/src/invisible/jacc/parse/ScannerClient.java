// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;


/*->

  ScannerClient is an interface that represents a client of a Scanner object.

->*/


public interface ScannerClient 
{

	// The scanner calls this routine when it reaches end-of-file.

	public void scannerEOF (Scanner scanner, Token token);

	// The scanner calls this routine when it cannot match a token.

	public void scannerUnmatchedToken (Scanner scanner, Token token);


}

