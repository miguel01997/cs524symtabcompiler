// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.parse;

import java.io.IOException;
import java.io.InputStream;


/*->

  PrescannerByteStream is a prescanner that reads from an InputStream, and
  delivers the raw byte stream to the scanner, with no modifications.

  PrescannerByteStream implements the PrescannerByte interface.

->*/



public class PrescannerByteStream implements PrescannerByte
{

	// The contained InputStream object.

	private InputStream _stream;


	// The constructor just saves the supplied InputStream.

	public PrescannerByteStream (InputStream stream)
	{
		super ();

		_stream = stream;

		return;
	}


	// Read bytes from the source.
	//
	// Implements the read() method of PrescannerByte.

	public int read (byte[] dstArray, int dstOffset, int dstLength, Token token)
		throws IOException, SyntaxException
	{
		return Math.max (0, _stream.read (dstArray, dstOffset, dstLength));
	}


	// Close the source.
	//
	// Implements the close() method of PrescannerByte

	public void close () throws IOException
	{
		_stream.close();
		return;
	}


}


