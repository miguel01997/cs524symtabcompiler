// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;

import java.io.IOException;


/*->

  PrescannerChar represents an input source for the scanner.

  A PrescannerChar is required to deliver chars into a buffer array provided
  by the scanner.

->*/


public interface PrescannerChar extends Prescanner
{

	// Read chars from the source.  The chars are read into dstArray, beginning
	// at element dstOffset (i.e., the first char read goes into array element
	// dstArray[dstOffset]).  A maximum of dstLength chars may be read.  The
	// value of dstLength must be a positive integer.
	//
	// If the return value is positive, it is the number of chars actually
	// read.  The function is not required to fill the buffer on each call,
	// even if there is sufficient source data to fill the buffer.
	//
	// If the return value is zero, it means that there is no more source data.
	//
	// If the return value is negative, it means that the source could not
	// deliver any data because the buffer is too small.  The negative of the
	// return value is the minimum buffer size required.  The scanner must
	// allocate a larger buffer and retry this function.  If the source cannot
	// determine how large a buffer is required, it may simply return -1, since
	// the scanner always increases the buffer size by at least a factor of 3/2
	// following the return of any negative value.
	//
	// It is recommended that a prescanner not produce error messages.  But if
	// it must produce an error message, it can use the token parameter to
	// identify the error's location.  The current file name is in token.file.
	// Estimates of the current line and column number (i.e., the line and
	// column of the first character to be read) are in token.line and
	// token.column.
	//
	// It is recommended that the line count should be maintained in the token
	// factories.  However, it is possible for the prescanner to maintain the
	// line count.  To do this, the prescanner should deliver one line of input
	// (or partial line) on each call.  Whenever a new line is delivered, the
	// prescanner should increment token.line and set token.column to 1.

	public int read (char[] dstArray, int dstOffset, int dstLength, Token token)
		throws IOException, SyntaxException;


	// This function is called by the scanner when it is finished using the
	// source.

	public void close () throws IOException;


}

