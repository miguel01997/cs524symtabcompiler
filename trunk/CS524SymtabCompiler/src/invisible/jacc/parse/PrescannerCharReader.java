// Copyright 1997-1998 Invisible Software, Inc.

package invisible.jacc.parse;

import java.io.IOException;
import java.io.Reader;


/*->

  PrescannerCharReader is a prescanner that reads from a Reader, and
  delivers the raw character stream to the scanner, with no modifications.

  PrescannerCharReader implements the PrescannerChar interface.

  Caution:  This class uses code specific to Java 1.1.  If you use this class,
  then your program will not run on Java 1.0.

->*/



public class PrescannerCharReader implements PrescannerChar
{

	// The contained Reader object.

	private Reader _reader;


	// The constructor just saves the supplied Reader.

	public PrescannerCharReader (Reader reader)
	{
		super ();

		_reader = reader;

		return;
	}


	// Read bytes from the source.
	//
	// Implements the read() method of PrescannerChar.

	public int read (char[] dstArray, int dstOffset, int dstLength, Token token)
		throws IOException, SyntaxException
	{
		return Math.max (0, _reader.read (dstArray, dstOffset, dstLength));
	}


	// Close the source.
	//
	// Implements the close() method of PrescannerChar

	public void close () throws IOException
	{
		_reader.close();
		return;
	}


}


