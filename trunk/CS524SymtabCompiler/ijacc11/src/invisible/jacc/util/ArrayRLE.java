// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;


/*->

  ArrayRLE is a class that defines a set of run length encoding functions for
  arrays.

  This class supports conversion to and from three different RLE formats:

  1. Boolean RLE.  This format is used to store boolean (1-bit) data.  The
     encoded data consists of a sequence of bytes, each of which contains a
	 1-bit data field (in the high order bit) and a 7-bit repetition count (in
	 the low order 7 bits).

  2. Byte RLE.  This format is used to store byte (8-bit) data.  The encoded
     data consists of a sequence of 2-byte records.  The first byte of each
	 record contains a repetition count, and the second byte contains a data
	 value.

  3. Short RLE.  This format is used to store short (16-bit) data.  The encoded
     data consists of a series of records, each either 2 or 4 bytes long.  A
	 2-byte record contains a 16-bit data value, stored high byte first.  A
	 4-byte record contains an 8-bit escape code, followed by an 8-bit
	 repetition count, followed by a 16-bit data value stored high byte first.

	 2-byte records are used to store unrepeated data values, while 4-byte
	 records are used to store repeated data values.

	 (Since the escape code can "collide" with a data value, it may happen
	 occasionally that an unrepeated data value needs to be stored in a 4-byte
	 record.  When this happens, the escape code is changed in hopes of
	 avoiding further collisions.)

  This class provides three different styles of conversion functions.

  With the first style of function, the caller provides both a one-dimensional
  source array and a one-dimensional destination array.  These functions all
  accept an offset and length for both source and destination.  Therefore, the
  caller can use subarrays for both source and destination.

  The following categories of functions are available in the first style:

  1. Conversion from an array of boolean to boolean RLE format.  The RLE data
     is stored in an array of byte.

  2. Conversion from boolean RLE format to an array of boolean.  The RLE data
     is supplied in an array of byte.

  3. Conversion from an array of byte, char, short, or int to byte RLE format.
     In the case of char, short, or int, only the low 8 bits of each data value
	 are used.  The RLE data can be stored in either an array of byte or an
	 array of short.

  4. Conversion from byte RLE format to an array of byte, char, short, or int.
     The RLE data can be supplied in either an array of byte or an array of
	 short.  In the case of char, short, or int, the resulting 8-bit data can
	 be either sign-extended or zero-extended.

  5. Conversion from an array of char, short, or int to short RLE format.  In
     the case of int, only the low 16 bits of each data value are used.  The
	 RLE data can be stored in either an array of byte or an array of short.

  6. Conversion from short RLE format to an array of char, short, or int.  The
     RLE data can be supplied in either an array of byte or an array of short.
	 In the case of int, the resulting 16-bit data can be either sign-extended
	 or zero-extended.

  With the second style of function, the caller provides a one-dimensional
  source array, plus an offset and length (which allows the caller to use a
  subarray).  These functions perform a conversion and discard the result,
  returning to the caller the amount of destination space that would be 
  required to hold the result of the conversion.

  The following categories of functions are available in the second style:

  1. Conversion from an array of boolean to boolean RLE format.  The function
     returns the length of the boolean RLE data, in bytes.

  2. Conversion from boolean RLE format to an array of boolean values.  The RLE
     data is supplied in an array of byte.  The function returns the number of
	 resulting booleans.

  3. Conversion from an array of byte, char, short, or int to byte RLE format.
     In the case of char, short, or int, only the low 8 bits of each data value
	 are used.  The function returns the length of the byte RLE data, in bytes.

  4. Conversion from byte RLE format to an array of 8-bit values.  The RLE
     data can be supplied in either an array of byte or an array of short.
	 The function returns the number of resulting 8-bit values.

  5. Conversion from an array of char, short, or int to short RLE format.  In
     the case of int, only the low 16 bits of each data value are used.  The
	 function returns the length of the short RLE data, in bytes.

  6. Conversion from short RLE format to an array of 16-bit values.  The RLE
     data can be supplied in either an array of byte or an array of short.  The
	 function returns the number of resulting 16-bit values.

  With the third style of function, the caller provides just a source array,
  and the function allocates the destination array.  Thus, each function
  converts an entire array.

  The third style supports both one-dimensional and two-dimensional arrays.
  With this style, the RLE data includes length information which allows the
  original array to be automatically reconstructed.  A one-dimensional array
  is encoded as a length field followed by the RLE contents.  A two-dimensional
  array is encoded as a length field which gives the length of the top-level
  array, followed by the encodings of the one-dimensional second-level arrays.

  An array length is encoded in either 2 or 4 bytes as follows:  (1) If the 
  array is null, the length field is 2 bytes containing 0xFFFF.  (2) Otherwise,
  if the length is less than or equal to 0x7FFF, the length field is 2 bytes
  containing the length, high order byte first.  (3) Otherwise, the length
  field is 4 bytes containing the length plus 0x80000000, high order byte first.

  The following categories of functions are available in the third style:

  1. Conversion from a one- or two-dimensional array of boolean to boolean RLE
     format.  The RLE data is stored in a one-dimensional array of byte.

  2. Conversion from boolean RLE format to a one- or two-dimensional array of
     boolean.  The RLE data is supplied in a one-dimensional array of byte.

  3. Conversion from a one- or two-dimensional array of byte, char, short, or
     int to byte RLE format.  In the case of char, short, or int, only the low
	 8 bits of each data value are used.  The RLE data is stored in a
	 one-dimensional array of short.

  4. Conversion from byte RLE format to a one- or two-dimensional array of
     byte, char, short, or int.  The RLE data is supplied in a one-dimensional
	 array of short.  In the case of char, short, or int, the resulting 8-bit
	 data can be either sign-extended or zero-extended.

  5. Conversion from a one- or two-dimensional array of char, short, or int to
     short RLE format.  In the case of int, only the low 16 bits of each data
	 value are used.  The RLE data is stored in a one-dimensional array of
	 short.

  6. Conversion from short RLE format to a one- or two-dimensional array of
     char, short, or int.  The RLE data is supplied in a one-dimensional array
	 of short.  In the case of int, the resulting 16-bit data can be either
	 sign-extended or zero-extended.
  
  Since all functions are static, there is no need to ever create an instance
  of this class.

->*/


public final class ArrayRLE 
{




	// ----- Boolean run-length-encoding routines -----


	// Boolean run-length-encoded data is stored as a sequence of bytes.  Each
	// byte has the following format:
	//
	//		High bit = Data value (0 = false, 1 = true).
	//		Low 7 bits = Number of repetitions, minus 1.


	// Repetition field mask.

	private static final int booleanRLERepMask = 0x7F;

	// Data field mask.

	private static final int booleanRLEDataMask = 0x80;




	// Convert boolean data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is stored beginning at dst[dstOff].  The number of source array elements
	// is srcLen.  The number of destination array elements is dstLen.
	//
	// Conversion stops when either the source or the destination is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.
	//
	// The maximum possible number of destination array elements consumed is
	// srcLen.

	public static long booleanToRLE (boolean[] src, int srcOff, int srcLen,
		byte[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.booleanToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		boolean prevElement = src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			boolean nextElement = src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == booleanRLERepMask)
				{

					// If the destination is full, return

					if (dstIndex >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the repeat code for previous element

					dst[dstIndex++] =
						(byte)(repCount + (prevElement ? booleanRLEDataMask : 0));

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the destination is full, return

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the repeat code for previous element

				dst[dstIndex++] =
					(byte)(repCount + (prevElement ? booleanRLEDataMask : 0));

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the destination is full, return

		if (dstIndex >= dstLimit)
		{
			return ((long)(srcIndex - (srcOff + repCount + 1)) << 32L)
				+ (long)(dstIndex - dstOff);
		}

		// Write the repeat code for the last element

		dst[dstIndex++] =
			(byte)(repCount + (prevElement ? booleanRLEDataMask : 0));

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert boolean data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is discarded.  The number of source array elements is srcLen.
	//
	// Conversion stops when the source is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// bytes.
	//
	// The maximum possible number of destination bytes is srcLen.

	public static long booleanToRLE (boolean[] src, int srcOff, int srcLen)
	{
		if ((srcLen < 0)
			|| (srcOff < 0)
			|| ((srcOff + srcLen) > src.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.booleanToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Index into destination array

		int dstIndex = 0;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		boolean prevElement = src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			boolean nextElement = src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == booleanRLERepMask)
				{

					// Count destination bytes

					dstIndex++;

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// Count destination bytes

				dstIndex++;

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// Count destination bytes

		dstIndex++;

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex);
	}




	// Convert RLE format to boolean data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to boolean,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToBoolean (byte[] src, int srcOff, int srcLen,
		boolean[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToBoolean");
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get next data element

			int nextElement = src[srcIndex++];

			// Get the next boolean

			boolean nextBoolean = ((nextElement & booleanRLEDataMask) != 0);

			// Get the repetition limit

			int repLimit = dstIndex + (nextElement & booleanRLERepMask) + 1;

			// Range check the repetition

			if (repLimit > dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the repetitions

			do
			{
				dst[dstIndex++] = nextBoolean;

			} while (dstIndex < repLimit);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to boolean data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to boolean,
	// and the result is discarded.  The number of source array elements is
	// srcLen.
	//
	// Conversion stops when the source is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// booleans.

	public static long RLEToBoolean (byte[] src, int srcOff, int srcLen)
	{
		if ((srcLen < 0)
			|| (srcOff < 0)
			|| ((srcOff + srcLen) > src.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToBoolean");
		}

		// Index into destination array

		int dstIndex = 0;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Count destination bytes

			dstIndex += ((src[srcIndex++] & booleanRLERepMask) + 1);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex);
	}




	// ----- Byte run-length-encoding routines -----


	// Byte run-length-encoded data is stored as a sequence of records.  Each
	// record is two bytes long and has the following format:
	//
	//		Byte 0 = Number of repetitions, minus 1.
	//		Byte 1 = Data value.
	//
	// Some routines use an array of short to hold the byte RLE records.  When
	// this is done, each short holds one record;  byte 0 in the high order
	// position, and byte 1 in the low order position.




	// Convert byte data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is stored beginning at dst[dstOff].  The number of source array elements
	// is srcLen.  The number of destination array elements is dstLen.
	//
	// Conversion stops when either the source or the destination is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.
	//
	// The maximum possible number of destination array elements consumed is
	// srcLen*2.

	public static long byteToRLE (byte[] src, int srcOff, int srcLen,
		byte[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.byteToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit (forcing length to be even)

		int dstLimit = dstOff + (dstLen & 0xFFFFFFFE);

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		byte prevElement = src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			byte nextElement = src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// If the destination is full, return

					if (dstIndex >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the repeat code for previous element

					dst[dstIndex++] = (byte)(repCount);

					// Write the previous element

					dst[dstIndex++] = prevElement;

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the destination is full, return

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the repeat code for previous element

				dst[dstIndex++] = (byte)(repCount);

				// Write the previous element

				dst[dstIndex++] = prevElement;

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the destination is full, return

		if (dstIndex >= dstLimit)
		{
			return ((long)(srcIndex - (srcOff + repCount + 1)) << 32L)
				+ (long)(dstIndex - dstOff);
		}

		// Write the repeat code for previous element

		dst[dstIndex++] = (byte)(repCount);

		// Write the previous element

		dst[dstIndex++] = prevElement;

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert byte data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is stored beginning at dst[dstOff].  The number of source array elements
	// is srcLen.  The number of destination array elements is dstLen.
	//
	// This function uses the low 8 bits of each source element.  The high
	// order bits of the source are discarded.
	//
	// Conversion stops when either the source or the destination is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.
	//
	// The maximum possible number of destination array elements consumed is
	// srcLen*2.

	public static long byteToRLE (char[] src, int srcOff, int srcLen,
		byte[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.byteToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit (forcing length to be even)

		int dstLimit = dstOff + (dstLen & 0xFFFFFFFE);

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		byte prevElement = (byte) src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			byte nextElement = (byte) src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// If the destination is full, return

					if (dstIndex >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the repeat code for previous element

					dst[dstIndex++] = (byte)(repCount);

					// Write the previous element

					dst[dstIndex++] = prevElement;

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the destination is full, return

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the repeat code for previous element

				dst[dstIndex++] = (byte)(repCount);

				// Write the previous element

				dst[dstIndex++] = prevElement;

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the destination is full, return

		if (dstIndex >= dstLimit)
		{
			return ((long)(srcIndex - (srcOff + repCount + 1)) << 32L)
				+ (long)(dstIndex - dstOff);
		}

		// Write the repeat code for previous element

		dst[dstIndex++] = (byte)(repCount);

		// Write the previous element

		dst[dstIndex++] = prevElement;

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert byte data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is stored beginning at dst[dstOff].  The number of source array elements
	// is srcLen.  The number of destination array elements is dstLen.
	//
	// This function uses the low 8 bits of each source element.  The high
	// order bits of the source are discarded.
	//
	// Conversion stops when either the source or the destination is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.
	//
	// The maximum possible number of destination array elements consumed is
	// srcLen*2.

	public static long byteToRLE (short[] src, int srcOff, int srcLen,
		byte[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.byteToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit (forcing length to be even)

		int dstLimit = dstOff + (dstLen & 0xFFFFFFFE);

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		byte prevElement = (byte) src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			byte nextElement = (byte) src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// If the destination is full, return

					if (dstIndex >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the repeat code for previous element

					dst[dstIndex++] = (byte)(repCount);

					// Write the previous element

					dst[dstIndex++] = prevElement;

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the destination is full, return

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the repeat code for previous element

				dst[dstIndex++] = (byte)(repCount);

				// Write the previous element

				dst[dstIndex++] = prevElement;

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the destination is full, return

		if (dstIndex >= dstLimit)
		{
			return ((long)(srcIndex - (srcOff + repCount + 1)) << 32L)
				+ (long)(dstIndex - dstOff);
		}

		// Write the repeat code for previous element

		dst[dstIndex++] = (byte)(repCount);

		// Write the previous element

		dst[dstIndex++] = prevElement;

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert byte data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is stored beginning at dst[dstOff].  The number of source array elements
	// is srcLen.  The number of destination array elements is dstLen.
	//
	// This function uses the low 8 bits of each source element.  The high
	// order bits of the source are discarded.
	//
	// Conversion stops when either the source or the destination is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.
	//
	// The maximum possible number of destination array elements consumed is
	// srcLen*2.

	public static long byteToRLE (int[] src, int srcOff, int srcLen,
		byte[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.byteToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit (forcing length to be even)

		int dstLimit = dstOff + (dstLen & 0xFFFFFFFE);

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		byte prevElement = (byte) src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			byte nextElement = (byte) src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// If the destination is full, return

					if (dstIndex >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the repeat code for previous element

					dst[dstIndex++] = (byte)(repCount);

					// Write the previous element

					dst[dstIndex++] = prevElement;

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the destination is full, return

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the repeat code for previous element

				dst[dstIndex++] = (byte)(repCount);

				// Write the previous element

				dst[dstIndex++] = prevElement;

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the destination is full, return

		if (dstIndex >= dstLimit)
		{
			return ((long)(srcIndex - (srcOff + repCount + 1)) << 32L)
				+ (long)(dstIndex - dstOff);
		}

		// Write the repeat code for previous element

		dst[dstIndex++] = (byte)(repCount);

		// Write the previous element

		dst[dstIndex++] = prevElement;

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert byte data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is stored beginning at dst[dstOff].  The number of source array elements
	// is srcLen.  The number of destination array elements is dstLen.
	//
	// Each pair of destination bytes is packed into one destination array
	// element, high order byte first.
	//
	// Conversion stops when either the source or the destination is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.
	//
	// The maximum possible number of destination array elements consumed is
	// srcLen.

	public static long byteToRLE (byte[] src, int srcOff, int srcLen,
		short[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.byteToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		byte prevElement = src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			byte nextElement = src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// If the destination is full, return

					if (dstIndex >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the repeat code for previous element

					dst[dstIndex++] = (short)((repCount << 8) + (prevElement & 0xFF));

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the destination is full, return

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the repeat code for previous element

				dst[dstIndex++] = (short)((repCount << 8) + (prevElement & 0xFF));

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the destination is full, return

		if (dstIndex >= dstLimit)
		{
			return ((long)(srcIndex - (srcOff + repCount + 1)) << 32L)
				+ (long)(dstIndex - dstOff);
		}

		// Write the repeat code for previous element

		dst[dstIndex++] = (short)((repCount << 8) + (prevElement & 0xFF));

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert byte data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is stored beginning at dst[dstOff].  The number of source array elements
	// is srcLen.  The number of destination array elements is dstLen.
	//
	// This function uses the low 8 bits of each source element.  The high
	// order bits of the source are discarded.
	//
	// Each pair of destination bytes is packed into one destination array
	// element, high order byte first.
	//
	// Conversion stops when either the source or the destination is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.
	//
	// The maximum possible number of destination array elements consumed is
	// srcLen.

	public static long byteToRLE (char[] src, int srcOff, int srcLen,
		short[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.byteToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		byte prevElement = (byte) src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			byte nextElement = (byte) src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// If the destination is full, return

					if (dstIndex >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the repeat code for previous element

					dst[dstIndex++] = (short)((repCount << 8) + (prevElement & 0xFF));

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the destination is full, return

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the repeat code for previous element

				dst[dstIndex++] = (short)((repCount << 8) + (prevElement & 0xFF));

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the destination is full, return

		if (dstIndex >= dstLimit)
		{
			return ((long)(srcIndex - (srcOff + repCount + 1)) << 32L)
				+ (long)(dstIndex - dstOff);
		}

		// Write the repeat code for previous element

		dst[dstIndex++] = (short)((repCount << 8) + (prevElement & 0xFF));

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert byte data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is stored beginning at dst[dstOff].  The number of source array elements
	// is srcLen.  The number of destination array elements is dstLen.
	//
	// This function uses the low 8 bits of each source element.  The high
	// order bits of the source are discarded.
	//
	// Each pair of destination bytes is packed into one destination array
	// element, high order byte first.
	//
	// Conversion stops when either the source or the destination is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.
	//
	// The maximum possible number of destination array elements consumed is
	// srcLen.

	public static long byteToRLE (short[] src, int srcOff, int srcLen,
		short[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.byteToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		byte prevElement = (byte) src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			byte nextElement = (byte) src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// If the destination is full, return

					if (dstIndex >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the repeat code for previous element

					dst[dstIndex++] = (short)((repCount << 8) + (prevElement & 0xFF));

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the destination is full, return

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the repeat code for previous element

				dst[dstIndex++] = (short)((repCount << 8) + (prevElement & 0xFF));

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the destination is full, return

		if (dstIndex >= dstLimit)
		{
			return ((long)(srcIndex - (srcOff + repCount + 1)) << 32L)
				+ (long)(dstIndex - dstOff);
		}

		// Write the repeat code for previous element

		dst[dstIndex++] = (short)((repCount << 8) + (prevElement & 0xFF));

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert byte data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is stored beginning at dst[dstOff].  The number of source array elements
	// is srcLen.  The number of destination array elements is dstLen.
	//
	// This function uses the low 8 bits of each source element.  The high
	// order bits of the source are discarded.
	//
	// Each pair of destination bytes is packed into one destination array
	// element, high order byte first.
	//
	// Conversion stops when either the source or the destination is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.
	//
	// The maximum possible number of destination array elements consumed is
	// srcLen.

	public static long byteToRLE (int[] src, int srcOff, int srcLen,
		short[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.byteToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		byte prevElement = (byte) src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			byte nextElement = (byte) src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// If the destination is full, return

					if (dstIndex >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the repeat code for previous element

					dst[dstIndex++] = (short)((repCount << 8) + (prevElement & 0xFF));

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the destination is full, return

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the repeat code for previous element

				dst[dstIndex++] = (short)((repCount << 8) + (prevElement & 0xFF));

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the destination is full, return

		if (dstIndex >= dstLimit)
		{
			return ((long)(srcIndex - (srcOff + repCount + 1)) << 32L)
				+ (long)(dstIndex - dstOff);
		}

		// Write the repeat code for previous element

		dst[dstIndex++] = (short)((repCount << 8) + (prevElement & 0xFF));

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert byte data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is discarded.  The number of source array elements is srcLen.
	//
	// Conversion stops when the source is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// bytes.
	//
	// The maximum possible number of destination bytes is srcLen*2.

	public static long byteToRLE (byte[] src, int srcOff, int srcLen)
	{
		if ((srcLen < 0)
			|| (srcOff < 0)
			|| ((srcOff + srcLen) > src.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.byteToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Index into destination array

		int dstIndex = 0;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		byte prevElement = src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			byte nextElement = src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// Count destination bytes

					dstIndex += 2;

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// Count destination bytes

				dstIndex += 2;

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// Count destination bytes

		dstIndex += 2;

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex);
	}




	// Convert byte data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is discarded.  The number of source array elements is srcLen.
	//
	// This function uses the low 8 bits of each source element.  The high
	// order bits of the source are discarded.
	//
	// Conversion stops when the source is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// bytes.
	//
	// The maximum possible number of destination bytes is srcLen*2.

	public static long byteToRLE (char[] src, int srcOff, int srcLen)
	{
		if ((srcLen < 0)
			|| (srcOff < 0)
			|| ((srcOff + srcLen) > src.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.byteToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Index into destination array

		int dstIndex = 0;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		byte prevElement = (byte) src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			byte nextElement = (byte) src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// Count destination bytes

					dstIndex += 2;

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// Count destination bytes

				dstIndex += 2;

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// Count destination bytes

		dstIndex += 2;

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex);
	}




	// Convert byte data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is discarded.  The number of source array elements is srcLen.
	//
	// This function uses the low 8 bits of each source element.  The high
	// order bits of the source are discarded.
	//
	// Conversion stops when the source is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// bytes.
	//
	// The maximum possible number of destination bytes is srcLen*2.

	public static long byteToRLE (short[] src, int srcOff, int srcLen)
	{
		if ((srcLen < 0)
			|| (srcOff < 0)
			|| ((srcOff + srcLen) > src.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.byteToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Index into destination array

		int dstIndex = 0;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		byte prevElement = (byte) src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			byte nextElement = (byte) src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// Count destination bytes

					dstIndex += 2;

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// Count destination bytes

				dstIndex += 2;

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// Count destination bytes

		dstIndex += 2;

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex);
	}




	// Convert byte data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is discarded.  The number of source array elements is srcLen.
	//
	// This function uses the low 8 bits of each source element.  The high
	// order bits of the source are discarded.
	//
	// Conversion stops when the source is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// bytes.
	//
	// The maximum possible number of destination bytes is srcLen*2.

	public static long byteToRLE (int[] src, int srcOff, int srcLen)
	{
		if ((srcLen < 0)
			|| (srcOff < 0)
			|| ((srcOff + srcLen) > src.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.byteToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Index into destination array

		int dstIndex = 0;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		byte prevElement = (byte) src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			byte nextElement = (byte) src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// Count destination bytes

					dstIndex += 2;

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// Count destination bytes

				dstIndex += 2;

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// Count destination bytes

		dstIndex += 2;

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToByte (byte[] src, int srcOff, int srcLen,
		byte[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByte");
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit (forcing length to be even)

		int srcLimit = srcOff + (srcLen & 0xFFFFFFFE);

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get the repetition limit

			int repLimit = dstIndex + (src[srcIndex++] & 0xFF) + 1;

			// Get next data element

			byte nextElement = src[srcIndex++];

			// Range check the repetition

			if (repLimit > dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 2)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the repetitions

			do
			{
				dst[dstIndex++] = nextElement;

			} while (dstIndex < repLimit);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Destination data is sign-extended.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToByte (byte[] src, int srcOff, int srcLen,
		char[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByte");
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit (forcing length to be even)

		int srcLimit = srcOff + (srcLen & 0xFFFFFFFE);

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get the repetition limit

			int repLimit = dstIndex + (src[srcIndex++] & 0xFF) + 1;

			// Get next data element

			char nextElement = (char) src[srcIndex++];

			// Range check the repetition

			if (repLimit > dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 2)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the repetitions

			do
			{
				dst[dstIndex++] = nextElement;

			} while (dstIndex < repLimit);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Destination data is sign-extended.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToByte (byte[] src, int srcOff, int srcLen,
		short[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByte");
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit (forcing length to be even)

		int srcLimit = srcOff + (srcLen & 0xFFFFFFFE);

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get the repetition limit

			int repLimit = dstIndex + (src[srcIndex++] & 0xFF) + 1;

			// Get next data element

			short nextElement = (short) src[srcIndex++];

			// Range check the repetition

			if (repLimit > dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 2)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the repetitions

			do
			{
				dst[dstIndex++] = nextElement;

			} while (dstIndex < repLimit);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Destination data is sign-extended.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToByte (byte[] src, int srcOff, int srcLen,
		int[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByte");
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit (forcing length to be even)

		int srcLimit = srcOff + (srcLen & 0xFFFFFFFE);

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get the repetition limit

			int repLimit = dstIndex + (src[srcIndex++] & 0xFF) + 1;

			// Get next data element

			int nextElement = (int) src[srcIndex++];

			// Range check the repetition

			if (repLimit > dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 2)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the repetitions

			do
			{
				dst[dstIndex++] = nextElement;

			} while (dstIndex < repLimit);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Destination data is zero-extended.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToByteUnsigned (byte[] src, int srcOff, int srcLen,
		char[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByteUnsigned");
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit (forcing length to be even)

		int srcLimit = srcOff + (srcLen & 0xFFFFFFFE);

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get the repetition limit

			int repLimit = dstIndex + (src[srcIndex++] & 0xFF) + 1;

			// Get next data element

			char nextElement = (char) (src[srcIndex++] & 0xFF);

			// Range check the repetition

			if (repLimit > dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 2)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the repetitions

			do
			{
				dst[dstIndex++] = nextElement;

			} while (dstIndex < repLimit);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Destination data is zero-extended.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToByteUnsigned (byte[] src, int srcOff, int srcLen,
		short[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByteUnsigned");
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit (forcing length to be even)

		int srcLimit = srcOff + (srcLen & 0xFFFFFFFE);

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get the repetition limit

			int repLimit = dstIndex + (src[srcIndex++] & 0xFF) + 1;

			// Get next data element

			short nextElement = (short) (src[srcIndex++] & 0xFF);

			// Range check the repetition

			if (repLimit > dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 2)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the repetitions

			do
			{
				dst[dstIndex++] = nextElement;

			} while (dstIndex < repLimit);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Destination data is zero-extended.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToByteUnsigned (byte[] src, int srcOff, int srcLen,
		int[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByteUnsigned");
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit (forcing length to be even)

		int srcLimit = srcOff + (srcLen & 0xFFFFFFFE);

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get the repetition limit

			int repLimit = dstIndex + (src[srcIndex++] & 0xFF) + 1;

			// Get next data element

			int nextElement = src[srcIndex++] & 0xFF;

			// Range check the repetition

			if (repLimit > dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 2)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the repetitions

			do
			{
				dst[dstIndex++] = nextElement;

			} while (dstIndex < repLimit);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is discarded.  The number of source array elements is
	// srcLen.
	//
	// Conversion stops when the source is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// bytes.

	public static long RLEToByte (byte[] src, int srcOff, int srcLen)
	{
		if ((srcLen < 0)
			|| (srcOff < 0)
			|| ((srcOff + srcLen) > src.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByte");
		}

		// Index into destination array

		int dstIndex = 0;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit (forcing length to be even)

		int srcLimit = srcOff + (srcLen & 0xFFFFFFFE);

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Count destination bytes

			dstIndex += (src[srcIndex++] & 0xFF) + 1;

			// Skip the data element

			srcIndex++;

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Each source array element contains two packed bytes, most significant
	// byte first.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToByte (short[] src, int srcOff, int srcLen,
		byte[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByte");
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get the packed data

			int packedData = src[srcIndex++];

			// Get the repetition limit

			int repLimit = dstIndex + ((packedData >> 8) & 0xFF) + 1;

			// Get next data element

			byte nextElement = (byte) packedData;

			// Range check the repetition

			if (repLimit > dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the repetitions

			do
			{
				dst[dstIndex++] = nextElement;

			} while (dstIndex < repLimit);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Each source array element contains two packed bytes, most significant
	// byte first.
	//
	// Destination data is sign-extended.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToByte (short[] src, int srcOff, int srcLen,
		char[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByte");
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get the packed data

			int packedData = src[srcIndex++];

			// Get the repetition limit

			int repLimit = dstIndex + ((packedData >> 8) & 0xFF) + 1;

			// Get next data element

			char nextElement = (char)(byte) packedData;

			// Range check the repetition

			if (repLimit > dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the repetitions

			do
			{
				dst[dstIndex++] = nextElement;

			} while (dstIndex < repLimit);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Each source array element contains two packed bytes, most significant
	// byte first.
	//
	// Destination data is sign-extended.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToByte (short[] src, int srcOff, int srcLen,
		short[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByte");
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get the packed data

			int packedData = src[srcIndex++];

			// Get the repetition limit

			int repLimit = dstIndex + ((packedData >> 8) & 0xFF) + 1;

			// Get next data element

			short nextElement = (short)(byte) packedData;

			// Range check the repetition

			if (repLimit > dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the repetitions

			do
			{
				dst[dstIndex++] = nextElement;

			} while (dstIndex < repLimit);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Each source array element contains two packed bytes, most significant
	// byte first.
	//
	// Destination data is sign-extended.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToByte (short[] src, int srcOff, int srcLen,
		int[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByte");
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get the packed data

			int packedData = src[srcIndex++];

			// Get the repetition limit

			int repLimit = dstIndex + ((packedData >> 8) & 0xFF) + 1;

			// Get next data element

			int nextElement = (int)(byte) packedData;

			// Range check the repetition

			if (repLimit > dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the repetitions

			do
			{
				dst[dstIndex++] = nextElement;

			} while (dstIndex < repLimit);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Each source array element contains two packed bytes, most significant
	// byte first.
	//
	// Destination data is zero-extended.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToByteUnsigned (short[] src, int srcOff, int srcLen,
		char[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByteUnsigned");
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get the packed data

			int packedData = src[srcIndex++];

			// Get the repetition limit

			int repLimit = dstIndex + ((packedData >> 8) & 0xFF) + 1;

			// Get next data element

			char nextElement = (char)(packedData & 0xFF);

			// Range check the repetition

			if (repLimit > dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the repetitions

			do
			{
				dst[dstIndex++] = nextElement;

			} while (dstIndex < repLimit);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Each source array element contains two packed bytes, most significant
	// byte first.
	//
	// Destination data is zero-extended.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToByteUnsigned (short[] src, int srcOff, int srcLen,
		short[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByteUnsigned");
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get the packed data

			int packedData = src[srcIndex++];

			// Get the repetition limit

			int repLimit = dstIndex + ((packedData >> 8) & 0xFF) + 1;

			// Get next data element

			short nextElement = (short)(packedData & 0xFF);

			// Range check the repetition

			if (repLimit > dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the repetitions

			do
			{
				dst[dstIndex++] = nextElement;

			} while (dstIndex < repLimit);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Each source array element contains two packed bytes, most significant
	// byte first.
	//
	// Destination data is zero-extended.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToByteUnsigned (short[] src, int srcOff, int srcLen,
		int[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByteUnsigned");
		}

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get the packed data

			int packedData = src[srcIndex++];

			// Get the repetition limit

			int repLimit = dstIndex + ((packedData >> 8) & 0xFF) + 1;

			// Get next data element

			int nextElement = packedData & 0xFF;

			// Range check the repetition

			if (repLimit > dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the repetitions

			do
			{
				dst[dstIndex++] = nextElement;

			} while (dstIndex < repLimit);

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to byte data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is discarded.  The number of source array elements is
	// srcLen.
	//
	// Each source array element contains two packed bytes, most significant
	// byte first.
	//
	// Conversion stops when the source is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// bytes.

	public static long RLEToByte (short[] src, int srcOff, int srcLen)
	{
		if ((srcLen < 0)
			|| (srcOff < 0)
			|| ((srcOff + srcLen) > src.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToByte");
		}

		// Index into destination array

		int dstIndex = 0;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Count destination bytes

			dstIndex += ((src[srcIndex++] >> 8) & 0xFF) + 1;

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex);
	}




	// ----- Short run-length-encoding routines -----


	// Short run-length-encoded data is stored as a sequence of records.  There
	// are two record formats.
	//
	// A literal record represents a single data value.  It has the following
	// format:
	//
	//		Byte 0 = Data value, most significant byte.
	//		Byte 1 = Data value, least significant byte.
	//
	// A repetition record represents a data value that is repeated one or more
	// times.  It has the following format:
	//
	//		Byte 0 = Escape code.
	//		Byte 1 = Number of repetitions, minus 1.
	//		Byte 2 = Data value, most significant byte.
	//		Byte 3 = Data value, least significant byte.
	//
	// The two record formats are distinguished by the value of byte 0.  If
	// byte 0 equals the escape code, it is a repetition record;  otherwise,
	// it is a literal record.
	//
	// The escape code is initially 0x81.  Whenever a data value is encountered
	// whose most significant byte equals the escape code, it is encoded in a
	// repetition record and then the escape code is modified by adding 0x53
	// (modulo 0x100).
	//
	// Some routines use an array of short to hold the byte RLE records.  When
	// this is done, each short holds two consecutive bytes, packed most
	// significant byte first.

	// Initial escape code, shifted 8 bits.

	private static final int shortRLEEscInitial = 0x8100;

	// Escape code increment, shifted 8 bits.

	private static final int shortRLEEscIncrement = 0x5300;




	// Convert short data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is stored beginning at dst[dstOff].  The number of source array elements
	// is srcLen.  The number of destination array elements is dstLen.
	//
	// Conversion stops when either the source or the destination is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.
	//
	// The maximum possible number of destination array elements consumed is
	// srcLen*4.

	public static long shortToRLE (char[] src, int srcOff, int srcLen,
		byte[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.shortToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit (forcing length to be even)

		int dstLimit = dstOff + (dstLen & 0xFFFFFFFE);

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		char prevElement = src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			char nextElement = src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// If the destination is full, return

					if ((dstIndex + 2) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code

					dst[dstIndex++] = (byte)(escape >> 8);

					// Write the repeat code for previous element

					dst[dstIndex++] = (byte)(repCount);

					// Write the previous element

					dst[dstIndex++] = (byte)(prevElement >> 8);
					dst[dstIndex++] = (byte)(prevElement);

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the previous element is repeated more than once ...

				if (repCount > 0)
				{

					// If the destination is full, return

					if ((dstIndex + 2) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code

					dst[dstIndex++] = (byte)(escape >> 8);

					// Write the repeat code for previous element

					dst[dstIndex++] = (byte)(repCount);

					// Write the previous element

					dst[dstIndex++] = (byte)(prevElement >> 8);
					dst[dstIndex++] = (byte)(prevElement);

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}
				}

				// Otherwise, if the previous element had escape code as high byte ...

				else if ((prevElement & 0xFF00) == escape)
				{

					// If the destination is full, return

					if ((dstIndex + 2) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code

					dst[dstIndex++] = (byte)(escape >> 8);

					// Write the repeat code for previous element

					dst[dstIndex++] = 0;

					// Write the previous element

					dst[dstIndex++] = (byte)(prevElement >> 8);
					dst[dstIndex++] = (byte)(prevElement);

					// Change escape code

					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}

				// Otherwise, we can write a single element ...

				else
				{

					// If the destination is full, return

					if (dstIndex >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the previous element

					dst[dstIndex++] = (byte)(prevElement >> 8);
					dst[dstIndex++] = (byte)(prevElement);
				}

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the last element is repeated more than once, or had escape code as high byte ...

		if ((repCount > 0) || ((prevElement & 0xFF00) == escape))
		{

			// If the destination is full, return

			if ((dstIndex + 2) >= dstLimit)
			{
				return ((long)(srcIndex - (srcOff + repCount + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the escape code

			dst[dstIndex++] = (byte)(escape >> 8);

			// Write the repeat code for previous element

			dst[dstIndex++] = (byte)(repCount);

			// Write the last element

			dst[dstIndex++] = (byte)(prevElement >> 8);
			dst[dstIndex++] = (byte)(prevElement);
		}

		// Otherwise, we can write a single element ...

		else
		{

			// If the destination is full, return

			if (dstIndex >= dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the last element

			dst[dstIndex++] = (byte)(prevElement >> 8);
			dst[dstIndex++] = (byte)(prevElement);
		}

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert short data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is stored beginning at dst[dstOff].  The number of source array elements
	// is srcLen.  The number of destination array elements is dstLen.
	//
	// Conversion stops when either the source or the destination is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.
	//
	// The maximum possible number of destination array elements consumed is
	// srcLen*4.

	public static long shortToRLE (short[] src, int srcOff, int srcLen,
		byte[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.shortToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit (forcing length to be even)

		int dstLimit = dstOff + (dstLen & 0xFFFFFFFE);

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		short prevElement = src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			short nextElement = src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// If the destination is full, return

					if ((dstIndex + 2) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code

					dst[dstIndex++] = (byte)(escape >> 8);

					// Write the repeat code for previous element

					dst[dstIndex++] = (byte)(repCount);

					// Write the previous element

					dst[dstIndex++] = (byte)(prevElement >> 8);
					dst[dstIndex++] = (byte)(prevElement);

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the previous element is repeated more than once ...

				if (repCount > 0)
				{

					// If the destination is full, return

					if ((dstIndex + 2) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code

					dst[dstIndex++] = (byte)(escape >> 8);

					// Write the repeat code for previous element

					dst[dstIndex++] = (byte)(repCount);

					// Write the previous element

					dst[dstIndex++] = (byte)(prevElement >> 8);
					dst[dstIndex++] = (byte)(prevElement);

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}
				}

				// Otherwise, if the previous element had escape code as high byte ...

				else if ((prevElement & 0xFF00) == escape)
				{

					// If the destination is full, return

					if ((dstIndex + 2) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code

					dst[dstIndex++] = (byte)(escape >> 8);

					// Write the repeat code for previous element

					dst[dstIndex++] = 0;

					// Write the previous element

					dst[dstIndex++] = (byte)(prevElement >> 8);
					dst[dstIndex++] = (byte)(prevElement);

					// Change escape code

					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}

				// Otherwise, we can write a single element ...

				else
				{

					// If the destination is full, return

					if (dstIndex >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the previous element

					dst[dstIndex++] = (byte)(prevElement >> 8);
					dst[dstIndex++] = (byte)(prevElement);
				}

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the last element is repeated more than once, or had escape code as high byte ...

		if ((repCount > 0) || ((prevElement & 0xFF00) == escape))
		{

			// If the destination is full, return

			if ((dstIndex + 2) >= dstLimit)
			{
				return ((long)(srcIndex - (srcOff + repCount + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the escape code

			dst[dstIndex++] = (byte)(escape >> 8);

			// Write the repeat code for previous element

			dst[dstIndex++] = (byte)(repCount);

			// Write the last element

			dst[dstIndex++] = (byte)(prevElement >> 8);
			dst[dstIndex++] = (byte)(prevElement);
		}

		// Otherwise, we can write a single element ...

		else
		{

			// If the destination is full, return

			if (dstIndex >= dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the last element

			dst[dstIndex++] = (byte)(prevElement >> 8);
			dst[dstIndex++] = (byte)(prevElement);
		}

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert short data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is stored beginning at dst[dstOff].  The number of source array elements
	// is srcLen.  The number of destination array elements is dstLen.
	//
	// Only the low 16 bits of each source element is used.  The high order
	// bits are discarded.
	//
	// Conversion stops when either the source or the destination is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.
	//
	// The maximum possible number of destination array elements consumed is
	// srcLen*4.

	public static long shortToRLE (int[] src, int srcOff, int srcLen,
		byte[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.shortToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit (forcing length to be even)

		int dstLimit = dstOff + (dstLen & 0xFFFFFFFE);

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		short prevElement = (short) src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			short nextElement = (short) src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// If the destination is full, return

					if ((dstIndex + 2) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code

					dst[dstIndex++] = (byte)(escape >> 8);

					// Write the repeat code for previous element

					dst[dstIndex++] = (byte)(repCount);

					// Write the previous element

					dst[dstIndex++] = (byte)(prevElement >> 8);
					dst[dstIndex++] = (byte)(prevElement);

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the previous element is repeated more than once ...

				if (repCount > 0)
				{

					// If the destination is full, return

					if ((dstIndex + 2) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code

					dst[dstIndex++] = (byte)(escape >> 8);

					// Write the repeat code for previous element

					dst[dstIndex++] = (byte)(repCount);

					// Write the previous element

					dst[dstIndex++] = (byte)(prevElement >> 8);
					dst[dstIndex++] = (byte)(prevElement);

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}
				}

				// Otherwise, if the previous element had escape code as high byte ...

				else if ((prevElement & 0xFF00) == escape)
				{

					// If the destination is full, return

					if ((dstIndex + 2) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code

					dst[dstIndex++] = (byte)(escape >> 8);

					// Write the repeat code for previous element

					dst[dstIndex++] = 0;

					// Write the previous element

					dst[dstIndex++] = (byte)(prevElement >> 8);
					dst[dstIndex++] = (byte)(prevElement);

					// Change escape code

					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}

				// Otherwise, we can write a single element ...

				else
				{

					// If the destination is full, return

					if (dstIndex >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the previous element

					dst[dstIndex++] = (byte)(prevElement >> 8);
					dst[dstIndex++] = (byte)(prevElement);
				}

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the last element is repeated more than once, or had escape code as high byte ...

		if ((repCount > 0) || ((prevElement & 0xFF00) == escape))
		{

			// If the destination is full, return

			if ((dstIndex + 2) >= dstLimit)
			{
				return ((long)(srcIndex - (srcOff + repCount + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the escape code

			dst[dstIndex++] = (byte)(escape >> 8);

			// Write the repeat code for previous element

			dst[dstIndex++] = (byte)(repCount);

			// Write the last element

			dst[dstIndex++] = (byte)(prevElement >> 8);
			dst[dstIndex++] = (byte)(prevElement);
		}

		// Otherwise, we can write a single element ...

		else
		{

			// If the destination is full, return

			if (dstIndex >= dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the last element

			dst[dstIndex++] = (byte)(prevElement >> 8);
			dst[dstIndex++] = (byte)(prevElement);
		}

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert short data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is stored beginning at dst[dstOff].  The number of source array elements
	// is srcLen.  The number of destination array elements is dstLen.
	//
	// Each destination element contains two bytes, high order byte first.
	//
	// Conversion stops when either the source or the destination is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.
	//
	// The maximum possible number of destination array elements consumed is
	// srcLen*4.

	public static long shortToRLE (char[] src, int srcOff, int srcLen,
		short[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.shortToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		char prevElement = src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			char nextElement = src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// If the destination is full, return

					if ((dstIndex + 1) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code and repeat code for previous element

					dst[dstIndex++] = (short)(escape + repCount);

					// Write the previous element

					dst[dstIndex++] = (short)(prevElement);

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the previous element is repeated more than once ...

				if (repCount > 0)
				{

					// If the destination is full, return

					if ((dstIndex + 1) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code and repeat code for previous element

					dst[dstIndex++] = (short)(escape + repCount);

					// Write the previous element

					dst[dstIndex++] = (short)(prevElement);

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}
				}

				// Otherwise, if the previous element had escape code as high byte ...

				else if ((prevElement & 0xFF00) == escape)
				{

					// If the destination is full, return

					if ((dstIndex + 1) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code and repeat code for previous element

					dst[dstIndex++] = (short)(escape + repCount);

					// Write the previous element

					dst[dstIndex++] = (short)(prevElement);

					// Change escape code

					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}

				// Otherwise, we can write a single element ...

				else
				{

					// If the destination is full, return

					if (dstIndex >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the previous element

					dst[dstIndex++] = (short)(prevElement);
				}

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the last element is repeated more than once, or had escape code as high byte ...

		if ((repCount > 0) || ((prevElement & 0xFF00) == escape))
		{

			// If the destination is full, return

			if ((dstIndex + 1) >= dstLimit)
			{
				return ((long)(srcIndex - (srcOff + repCount + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the escape code and repeat code for previous element

			dst[dstIndex++] = (short)(escape + repCount);

			// Write the last element

			dst[dstIndex++] = (short)(prevElement);
		}

		// Otherwise, we can write a single element ...

		else
		{

			// If the destination is full, return

			if (dstIndex >= dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the last element

			dst[dstIndex++] = (short)(prevElement);
		}

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert short data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is stored beginning at dst[dstOff].  The number of source array elements
	// is srcLen.  The number of destination array elements is dstLen.
	//
	// Each destination element contains two bytes, high order byte first.
	//
	// Conversion stops when either the source or the destination is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.
	//
	// The maximum possible number of destination array elements consumed is
	// srcLen*4.

	public static long shortToRLE (short[] src, int srcOff, int srcLen,
		short[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.shortToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		short prevElement = src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			short nextElement = src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// If the destination is full, return

					if ((dstIndex + 1) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code and repeat code for previous element

					dst[dstIndex++] = (short)(escape + repCount);

					// Write the previous element

					dst[dstIndex++] = prevElement;

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the previous element is repeated more than once ...

				if (repCount > 0)
				{

					// If the destination is full, return

					if ((dstIndex + 1) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code and repeat code for previous element

					dst[dstIndex++] = (short)(escape + repCount);

					// Write the previous element

					dst[dstIndex++] = prevElement;

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}
				}

				// Otherwise, if the previous element had escape code as high byte ...

				else if ((prevElement & 0xFF00) == escape)
				{

					// If the destination is full, return

					if ((dstIndex + 1) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code and repeat code for previous element

					dst[dstIndex++] = (short)(escape + repCount);

					// Write the previous element

					dst[dstIndex++] = prevElement;

					// Change escape code

					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}

				// Otherwise, we can write a single element ...

				else
				{

					// If the destination is full, return

					if (dstIndex >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the previous element

					dst[dstIndex++] = prevElement;
				}

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the last element is repeated more than once, or had escape code as high byte ...

		if ((repCount > 0) || ((prevElement & 0xFF00) == escape))
		{

			// If the destination is full, return

			if ((dstIndex + 1) >= dstLimit)
			{
				return ((long)(srcIndex - (srcOff + repCount + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the escape code and repeat code for previous element

			dst[dstIndex++] = (short)(escape + repCount);

			// Write the last element

			dst[dstIndex++] = prevElement;
		}

		// Otherwise, we can write a single element ...

		else
		{

			// If the destination is full, return

			if (dstIndex >= dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the last element

			dst[dstIndex++] = prevElement;
		}

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert short data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is stored beginning at dst[dstOff].  The number of source array elements
	// is srcLen.  The number of destination array elements is dstLen.
	//
	// Only the low 16 bits of each source element is used.  The high order
	// bits are discarded.
	//
	// Each destination element contains two bytes, high order byte first.
	//
	// Conversion stops when either the source or the destination is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.
	//
	// The maximum possible number of destination array elements consumed is
	// srcLen*4.

	public static long shortToRLE (int[] src, int srcOff, int srcLen,
		short[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.shortToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		short prevElement = (short) src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			short nextElement = (short) src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// If the destination is full, return

					if ((dstIndex + 1) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code and repeat code for previous element

					dst[dstIndex++] = (short)(escape + repCount);

					// Write the previous element

					dst[dstIndex++] = prevElement;

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the previous element is repeated more than once ...

				if (repCount > 0)
				{

					// If the destination is full, return

					if ((dstIndex + 1) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + repCount + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code and repeat code for previous element

					dst[dstIndex++] = (short)(escape + repCount);

					// Write the previous element

					dst[dstIndex++] = prevElement;

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}
				}

				// Otherwise, if the previous element had escape code as high byte ...

				else if ((prevElement & 0xFF00) == escape)
				{

					// If the destination is full, return

					if ((dstIndex + 1) >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the escape code and repeat code for previous element

					dst[dstIndex++] = (short)(escape + repCount);

					// Write the previous element

					dst[dstIndex++] = prevElement;

					// Change escape code

					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}

				// Otherwise, we can write a single element ...

				else
				{

					// If the destination is full, return

					if (dstIndex >= dstLimit)
					{
						return ((long)(srcIndex - (srcOff + 2)) << 32L)
							+ (long)(dstIndex - dstOff);
					}

					// Write the previous element

					dst[dstIndex++] = prevElement;
				}

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the last element is repeated more than once, or had escape code as high byte ...

		if ((repCount > 0) || ((prevElement & 0xFF00) == escape))
		{

			// If the destination is full, return

			if ((dstIndex + 1) >= dstLimit)
			{
				return ((long)(srcIndex - (srcOff + repCount + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the escape code and repeat code for previous element

			dst[dstIndex++] = (short)(escape + repCount);

			// Write the last element

			dst[dstIndex++] = prevElement;
		}

		// Otherwise, we can write a single element ...

		else
		{

			// If the destination is full, return

			if (dstIndex >= dstLimit)
			{
				return ((long)(srcIndex - (srcOff + 1)) << 32L)
					+ (long)(dstIndex - dstOff);
			}

			// Write the last element

			dst[dstIndex++] = prevElement;
		}

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert short data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is discarded.  The number of source array elements is srcLen.
	//
	// Conversion stops when the source is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// bytes.
	//
	// The maximum possible number of destination bytes is srcLen*4.

	public static long shortToRLE (char[] src, int srcOff, int srcLen)
	{
		if ((srcLen < 0)
			|| (srcOff < 0)
			|| ((srcOff + srcLen) > src.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.shortToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = 0;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		char prevElement = src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			char nextElement = src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// Count destination bytes

					dstIndex += 4;

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the previous element is repeated more than once ...

				if (repCount > 0)
				{

					// Count destination bytes

					dstIndex += 4;

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}
				}

				// Otherwise, if the previous element had escape code as high byte ...

				else if ((prevElement & 0xFF00) == escape)
				{

					// Count destination bytes

					dstIndex += 4;

					// Change escape code

					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}

				// Otherwise, we can write a single element ...

				else
				{

					// Count destination bytes

					dstIndex += 2;
				}

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the last element is repeated more than once, or had escape code as high byte ...

		if ((repCount > 0) || ((prevElement & 0xFF00) == escape))
		{

			// Count destination bytes

			dstIndex += 4;
		}

		// Otherwise, we can write a single element ...

		else
		{

			// Count destination bytes

			dstIndex += 2;
		}

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex);
	}




	// Convert short data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is discarded.  The number of source array elements is srcLen.
	//
	// Conversion stops when the source is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// bytes.
	//
	// The maximum possible number of destination bytes is srcLen*4.

	public static long shortToRLE (short[] src, int srcOff, int srcLen)
	{
		if ((srcLen < 0)
			|| (srcOff < 0)
			|| ((srcOff + srcLen) > src.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.shortToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = 0;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		short prevElement = src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			short nextElement = src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// Count destination bytes

					dstIndex += 4;

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the previous element is repeated more than once ...

				if (repCount > 0)
				{

					// Count destination bytes

					dstIndex += 4;

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}
				}

				// Otherwise, if the previous element had escape code as high byte ...

				else if ((prevElement & 0xFF00) == escape)
				{

					// Count destination bytes

					dstIndex += 4;

					// Change escape code

					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}

				// Otherwise, we can write a single element ...

				else
				{

					// Count destination bytes

					dstIndex += 2;
				}

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the last element is repeated more than once, or had escape code as high byte ...

		if ((repCount > 0) || ((prevElement & 0xFF00) == escape))
		{

			// Count destination bytes

			dstIndex += 4;
		}

		// Otherwise, we can write a single element ...

		else
		{

			// Count destination bytes

			dstIndex += 2;
		}

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex);
	}




	// Convert short data to RLE format.
	//
	// Data beginning at src[srcOff] is converted to RLE format, and the result
	// is discarded.  The number of source array elements is srcLen.
	//
	// Only the low 16 bits of each source element is used.  The high order
	// bits are discarded.
	//
	// Conversion stops when the source is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// bytes.
	//
	// The maximum possible number of destination bytes is srcLen*4.

	public static long shortToRLE (int[] src, int srcOff, int srcLen)
	{
		if ((srcLen < 0)
			|| (srcOff < 0)
			|| ((srcOff + srcLen) > src.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.shortToRLE");
		}

		// If the source length is zero, just return zero

		if (srcLen == 0)
		{
			return 0L;
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = 0;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Get first data element

		short prevElement = (short) src[srcIndex++];

		// Repetition count for prevElement

		int repCount = 0;

		// Loop over data array ...

		while (srcIndex < srcLimit)
		{

			// Get the next element

			short nextElement = (short) src[srcIndex++];

			// If it repeats the previous element ...

			if (nextElement == prevElement)
			{

				// If the repetition count is already maxed out ...

				if (repCount == 0xFF)
				{

					// Count destination bytes

					dstIndex += 4;

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}

					// Restart the repetition count

					repCount = 0;
				}

				// Otherwise, increment the repetition count

				else
				{
					++repCount;
				}
			}

			// Otherwise, it's a new element ...

			else
			{

				// If the previous element is repeated more than once ...

				if (repCount > 0)
				{

					// Count destination bytes

					dstIndex += 4;

					// If previous element had escape code as high byte, change escape code

					if ((prevElement & 0xFF00) == escape)
					{
						escape = (escape + shortRLEEscIncrement) & 0xFF00;
					}
				}

				// Otherwise, if the previous element had escape code as high byte ...

				else if ((prevElement & 0xFF00) == escape)
				{

					// Count destination bytes

					dstIndex += 4;

					// Change escape code

					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}

				// Otherwise, we can write a single element ...

				else
				{

					// Count destination bytes

					dstIndex += 2;
				}

				// Restart the repetition count

				repCount = 0;

				// The next element becomes the previous element

				prevElement = nextElement;
			}

		}	// end loop over data array

		// If the last element is repeated more than once, or had escape code as high byte ...

		if ((repCount > 0) || ((prevElement & 0xFF00) == escape))
		{

			// Count destination bytes

			dstIndex += 4;
		}

		// Otherwise, we can write a single element ...

		else
		{

			// Count destination bytes

			dstIndex += 2;
		}

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex);
	}




	// Convert RLE format to short data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToShort (byte[] src, int srcOff, int srcLen,
		char[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToShort");
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit (forcing length to be even)

		int srcLimit = srcOff + (srcLen & 0xFFFFFFFE);

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get next data element

			int nextElement = src[srcIndex++];
			nextElement = (nextElement << 8) + (src[srcIndex++] & 0xFF);

			// If high byte is escape code ...

			if ((nextElement & 0xFF00) == escape)
			{

				// Get the repetition limit

				int repLimit = dstIndex + (nextElement & 0xFF) + 1;

				// Range check the repetition, and check for source exhausted

				if ((repLimit > dstLimit) || (srcIndex >= srcLimit))
				{
					return ((long)(srcIndex - (srcOff + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Get next data element

				nextElement = src[srcIndex++];
				nextElement = (nextElement << 8) + (src[srcIndex++] & 0xFF);

				// Write the repetitions

				do
				{
					dst[dstIndex++] = (char) nextElement;

				} while (dstIndex < repLimit);

				// If high byte is escape code, change the escape code

				if ((nextElement & 0xFF00) == escape)
				{
					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}
			}

			// Otherwise, just write a single short

			else
			{

				// Check for destination full

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the element

				dst[dstIndex++] = (char) nextElement;
			}

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to short data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToShort (byte[] src, int srcOff, int srcLen,
		short[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToShort");
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit (forcing length to be even)

		int srcLimit = srcOff + (srcLen & 0xFFFFFFFE);

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get next data element

			int nextElement = src[srcIndex++];
			nextElement = (nextElement << 8) + (src[srcIndex++] & 0xFF);

			// If high byte is escape code ...

			if ((nextElement & 0xFF00) == escape)
			{

				// Get the repetition limit

				int repLimit = dstIndex + (nextElement & 0xFF) + 1;

				// Range check the repetition, and check for source exhausted

				if ((repLimit > dstLimit) || (srcIndex >= srcLimit))
				{
					return ((long)(srcIndex - (srcOff + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Get next data element

				nextElement = src[srcIndex++];
				nextElement = (nextElement << 8) + (src[srcIndex++] & 0xFF);

				// Write the repetitions

				do
				{
					dst[dstIndex++] = (short) nextElement;

				} while (dstIndex < repLimit);

				// If high byte is escape code, change the escape code

				if ((nextElement & 0xFF00) == escape)
				{
					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}
			}

			// Otherwise, just write a single short

			else
			{

				// Check for destination full

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the element

				dst[dstIndex++] = (short) nextElement;
			}

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to short data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Each destination short is sign-extended to int.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToShort (byte[] src, int srcOff, int srcLen,
		int[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToShort");
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit (forcing length to be even)

		int srcLimit = srcOff + (srcLen & 0xFFFFFFFE);

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get next data element

			int nextElement = src[srcIndex++];
			nextElement = (nextElement << 8) + (src[srcIndex++] & 0xFF);

			// If high byte is escape code ...

			if ((nextElement & 0xFF00) == escape)
			{

				// Get the repetition limit

				int repLimit = dstIndex + (nextElement & 0xFF) + 1;

				// Range check the repetition, and check for source exhausted

				if ((repLimit > dstLimit) || (srcIndex >= srcLimit))
				{
					return ((long)(srcIndex - (srcOff + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Get next data element

				nextElement = src[srcIndex++];
				nextElement = (nextElement << 8) + (src[srcIndex++] & 0xFF);

				// Write the repetitions

				do
				{
					dst[dstIndex++] = nextElement;

				} while (dstIndex < repLimit);

				// If high byte is escape code, change the escape code

				if ((nextElement & 0xFF00) == escape)
				{
					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}
			}

			// Otherwise, just write a single short

			else
			{

				// Check for destination full

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the element

				dst[dstIndex++] = nextElement;
			}

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to short data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Each destination short is zero-extended to int.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToShortUnsigned (byte[] src, int srcOff, int srcLen,
		int[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToShortUnsigned");
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit (forcing length to be even)

		int srcLimit = srcOff + (srcLen & 0xFFFFFFFE);

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get next data element

			int nextElement = src[srcIndex++] & 0xFF;
			nextElement = (nextElement << 8) + (src[srcIndex++] & 0xFF);

			// If high byte is escape code ...

			if ((nextElement & 0xFF00) == escape)
			{

				// Get the repetition limit

				int repLimit = dstIndex + (nextElement & 0xFF) + 1;

				// Range check the repetition, and check for source exhausted

				if ((repLimit > dstLimit) || (srcIndex >= srcLimit))
				{
					return ((long)(srcIndex - (srcOff + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Get next data element

				nextElement = src[srcIndex++] & 0xFF;
				nextElement = (nextElement << 8) + (src[srcIndex++] & 0xFF);

				// Write the repetitions

				do
				{
					dst[dstIndex++] = nextElement;

				} while (dstIndex < repLimit);

				// If high byte is escape code, change the escape code

				if ((nextElement & 0xFF00) == escape)
				{
					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}
			}

			// Otherwise, just write a single short

			else
			{

				// Check for destination full

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + 2)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the element

				dst[dstIndex++] = nextElement;
			}

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to short data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Each source element contains two bytes, most significant byte first.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToShort (short[] src, int srcOff, int srcLen,
		char[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToShort");
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get next data element

			int nextElement = src[srcIndex++];

			// If high byte is escape code ...

			if ((nextElement & 0xFF00) == escape)
			{

				// Get the repetition limit

				int repLimit = dstIndex + (nextElement & 0xFF) + 1;

				// Range check the repetition, and check for source exhausted

				if ((repLimit > dstLimit) || (srcIndex >= srcLimit))
				{
					return ((long)(srcIndex - (srcOff + 1)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Get next data element

				nextElement = src[srcIndex++];

				// Write the repetitions

				do
				{
					dst[dstIndex++] = (char) nextElement;

				} while (dstIndex < repLimit);

				// If high byte is escape code, change the escape code

				if ((nextElement & 0xFF00) == escape)
				{
					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}
			}

			// Otherwise, just write a single short

			else
			{

				// Check for destination full

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + 1)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the element

				dst[dstIndex++] = (char) nextElement;
			}

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to short data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Each source element contains two bytes, most significant byte first.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToShort (short[] src, int srcOff, int srcLen,
		short[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToShort");
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get next data element

			int nextElement = src[srcIndex++];

			// If high byte is escape code ...

			if ((nextElement & 0xFF00) == escape)
			{

				// Get the repetition limit

				int repLimit = dstIndex + (nextElement & 0xFF) + 1;

				// Range check the repetition, and check for source exhausted

				if ((repLimit > dstLimit) || (srcIndex >= srcLimit))
				{
					return ((long)(srcIndex - (srcOff + 1)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Get next data element

				nextElement = src[srcIndex++];

				// Write the repetitions

				do
				{
					dst[dstIndex++] = (short) nextElement;

				} while (dstIndex < repLimit);

				// If high byte is escape code, change the escape code

				if ((nextElement & 0xFF00) == escape)
				{
					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}
			}

			// Otherwise, just write a single short

			else
			{

				// Check for destination full

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + 1)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the element

				dst[dstIndex++] = (short) nextElement;
			}

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to short data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Each source element contains two bytes, most significant byte first.
	//
	// Each destination short is sign-extended to int.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToShort (short[] src, int srcOff, int srcLen,
		int[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToShort");
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get next data element

			int nextElement = src[srcIndex++];

			// If high byte is escape code ...

			if ((nextElement & 0xFF00) == escape)
			{

				// Get the repetition limit

				int repLimit = dstIndex + (nextElement & 0xFF) + 1;

				// Range check the repetition, and check for source exhausted

				if ((repLimit > dstLimit) || (srcIndex >= srcLimit))
				{
					return ((long)(srcIndex - (srcOff + 1)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Get next data element

				nextElement = src[srcIndex++];

				// Write the repetitions

				do
				{
					dst[dstIndex++] = nextElement;

				} while (dstIndex < repLimit);

				// If high byte is escape code, change the escape code

				if ((nextElement & 0xFF00) == escape)
				{
					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}
			}

			// Otherwise, just write a single short

			else
			{

				// Check for destination full

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + 1)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the element

				dst[dstIndex++] = nextElement;
			}

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to short data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is stored beginning at dst[dstOff].  The number of source
	// array elements is srcLen.  The number of destination array elements is
	// dstLen.
	//
	// Each source element contains two bytes, most significant byte first.
	//
	// Each destination short is zero-extended to int.
	//
	// Conversion stops when either the source or the destination is exhausted.
	// Note in particular that each repetition block is either completely 
	// written into the destination, or not written at all.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// array elements consumed.

	public static long RLEToShortUnsigned (short[] src, int srcOff, int srcLen,
		int[] dst, int dstOff, int dstLen)
	{
		if ((srcLen < 0)
			|| (dstLen < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + srcLen) > src.length)
			|| ((dstOff + dstLen) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToShortUnsigned");
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = dstOff;

		// Destination limit

		int dstLimit = dstOff + dstLen;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get next data element

			int nextElement = src[srcIndex++] & 0xFFFF;

			// If high byte is escape code ...

			if ((nextElement & 0xFF00) == escape)
			{

				// Get the repetition limit

				int repLimit = dstIndex + (nextElement & 0xFF) + 1;

				// Range check the repetition, and check for source exhausted

				if ((repLimit > dstLimit) || (srcIndex >= srcLimit))
				{
					return ((long)(srcIndex - (srcOff + 1)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Get next data element

				nextElement = src[srcIndex++] & 0xFFFF;

				// Write the repetitions

				do
				{
					dst[dstIndex++] = nextElement;

				} while (dstIndex < repLimit);

				// If high byte is escape code, change the escape code

				if ((nextElement & 0xFF00) == escape)
				{
					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}
			}

			// Otherwise, just write a single short

			else
			{

				// Check for destination full

				if (dstIndex >= dstLimit)
				{
					return ((long)(srcIndex - (srcOff + 1)) << 32L)
						+ (long)(dstIndex - dstOff);
				}

				// Write the element

				dst[dstIndex++] = nextElement;
			}

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex - dstOff);
	}




	// Convert RLE format to short data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is discarded.  The number of source array elements is
	// srcLen.
	//
	// Conversion stops when the source is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// shorts.

	public static long RLEToShort (byte[] src, int srcOff, int srcLen)
	{
		if ((srcLen < 0)
			|| (srcOff < 0)
			|| ((srcOff + srcLen) > src.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToShort");
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = 0;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit (forcing length to be even)

		int srcLimit = srcOff + (srcLen & 0xFFFFFFFE);

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get next data element

			int nextElement = src[srcIndex++];
			nextElement = (nextElement << 8) + (src[srcIndex++] & 0xFF);

			// If high byte is escape code ...

			if ((nextElement & 0xFF00) == escape)
			{

				// Check for source exhausted

				if (srcIndex >= srcLimit)
				{
					return ((long)(srcIndex - (srcOff + 2)) << 32L)
						+ (long)(dstIndex);
				}

				// Count destination shorts

				dstIndex += ((nextElement & 0xFF) + 1);

				// Get next data element

				nextElement = src[srcIndex++];
				nextElement = (nextElement << 8) + (src[srcIndex++] & 0xFF);

				// If high byte is escape code, change the escape code

				if ((nextElement & 0xFF00) == escape)
				{
					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}
			}

			// Otherwise, just write a single short

			else
			{

				// Count destination shorts

				dstIndex++;
			}

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex);
	}




	// Convert RLE format to short data.
	//
	// Data beginning at src[srcOff] is converted from RLE format to byte,
	// and the result is discarded.  The number of source array elements is
	// srcLen.
	//
	// Each source element contains two bytes, most significant byte first.
	//
	// Conversion stops when the source is exhausted.
	//
	// The high half of the return value is the number of source array elements
	// consumed.  The low half of the return value is the number of destination
	// shorts.

	public static long RLEToShort (short[] src, int srcOff, int srcLen)
	{
		if ((srcLen < 0)
			|| (srcOff < 0)
			|| ((srcOff + srcLen) > src.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayRLE.RLEToShort");
		}

		// Initial escape code, shifted 8 bits

		int escape = shortRLEEscInitial;

		// Index into destination array

		int dstIndex = 0;

		// Index into source array

		int srcIndex = srcOff;

		// Source limit

		int srcLimit = srcOff + srcLen;

		// Loop over data

		while (srcIndex < srcLimit)
		{

			// Get next data element

			int nextElement = src[srcIndex++];

			// If high byte is escape code ...

			if ((nextElement & 0xFF00) == escape)
			{

				// Check for source exhausted

				if (srcIndex >= srcLimit)
				{
					return ((long)(srcIndex - (srcOff + 1)) << 32L)
						+ (long)(dstIndex);
				}

				// Count destination shorts

				dstIndex += ((nextElement & 0xFF) + 1);

				// Get next data element

				nextElement = src[srcIndex++];

				// If high byte is escape code, change the escape code

				if ((nextElement & 0xFF00) == escape)
				{
					escape = (escape + shortRLEEscIncrement) & 0xFF00;
				}
			}

			// Otherwise, just write a single short

			else
			{

				// Count destination shorts

				dstIndex++;
			}

		}	// end loop over data

		// Return the source and destination lengths consumed

		return ((long)(srcIndex - srcOff) << 32L)
			+ (long)(dstIndex);
	}




	// ----- Boolean RLE Array Conversion -----


	// The following routines convert entire arrays to or from boolean RLE
	// format.
	//
	// A one-dimensional array is encoded as the array length, followed by
	// the boolean RLE data.
	//
	// A two-dimensional array is encoded as the array length, followed by
	// the encoding for each one-dimensional subarray.
	//
	// The array length is encoded in either 2 or 4 bytes, as follows:
	//
	//	- If the array is null, the length is encoded in two bytes which
	//	  contain 0xFFFF.
	//
	//	- Otherwise, if the length of the array is less than or equal to
	//	  0x7FFF, the length is encoded in two bytes which contain the length.
	//
	//	- Otherwise, the length is encoded in four bytes which contain the
	//	  length plus 0x80000000.
	//
	// In all cases, the RLE data is stored in a one-dimensional array of
	// byte.




	// Convert one-dimensional array of boolean to boolean RLE.

	public static byte[] boolean1DToBooleanRLE (boolean[] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			byte[] nullResult = new byte[2];
			nullResult[0] = (byte) 0xFF;
			nullResult[1] = (byte) 0xFF;
			return nullResult;
		}

		// Calculate length required

		int dstIndex = ((data.length <= 0x7FFF) ? 2 : 4)
			+ ((int) booleanToRLE (data, 0, data.length));

		// Allocate the array

		byte[] result = new byte[dstIndex];

		// Store the length

		if (data.length <= 0x7FFF)
		{
			result[0] = (byte)(data.length >> 8);
			result[1] = (byte) data.length;
			dstIndex = 2;
		}
		else
		{
			result[0] = (byte)((data.length >> 24) + 0x80);
			result[1] = (byte)(data.length >> 16);
			result[2] = (byte)(data.length >> 8);
			result[3] = (byte) data.length;
			dstIndex = 4;
		}

		// Convert to RLE

		booleanToRLE (data, 0, data.length, result, dstIndex, result.length - dstIndex);

		// Return result

		return result;
	}




	// Convert two-dimensional array of boolean to boolean RLE.

	public static byte[] boolean2DToBooleanRLE (boolean[][] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			byte[] nullResult = new byte[2];
			nullResult[0] = (byte) 0xFF;
			nullResult[1] = (byte) 0xFF;
			return nullResult;
		}

		// Length of top level array

		int dstIndex = ((data.length <= 0x7FFF) ? 2 : 4);

		// Add lengths of subarrays

		for (int i = 0; i < data.length; ++i)
		{
			dstIndex += ( ((data[i].length <= 0x7FFF) ? 2 : 4)
				+ ((int) booleanToRLE (data[i], 0, data[i].length)) );
		}

		// Allocate the array

		byte[] result = new byte[dstIndex];

		// Store the length of the top level array

		if (data.length <= 0x7FFF)
		{
			result[0] = (byte)(data.length >> 8);
			result[1] = (byte) data.length;
			dstIndex = 2;
		}
		else
		{
			result[0] = (byte)((data.length >> 24) + 0x80);
			result[1] = (byte)(data.length >> 16);
			result[2] = (byte)(data.length >> 8);
			result[3] = (byte) data.length;
			dstIndex = 4;
		}

		// For each subarray ...

		for (int i = 0; i < data.length; ++i)
		{

			// If null subarray, return null encoding

			if (data[i] == null)
			{
				result[dstIndex++] = (byte) 0xFF;
				result[dstIndex++] = (byte) 0xFF;
				continue;
			}

			// Store the length of the subarray

			if (data[i].length <= 0x7FFF)
			{
				result[dstIndex++] = (byte)(data[i].length >> 8);
				result[dstIndex++] = (byte) data[i].length;
			}
			else
			{
				result[dstIndex++] = (byte)((data[i].length >> 24) + 0x80);
				result[dstIndex++] = (byte)(data[i].length >> 16);
				result[dstIndex++] = (byte)(data[i].length >> 8);
				result[dstIndex++] = (byte) data[i].length;
			}

			// Convert to RLE

			dstIndex += (int) booleanToRLE (
				data[i], 0, data[i].length, result, dstIndex, result.length - dstIndex );
		}

		// Return result

		return result;
	}




	// Convert boolean RLE to one-dimensional array of boolean.

	public static boolean[] booleanRLEToBoolean1D (byte[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];
		resultLength = (resultLength << 8) + (data[srcIndex++] & 0xFF);

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 8) + (data[srcIndex++] & 0xFF);
			resultLength = (resultLength << 8) + (data[srcIndex++] & 0xFF);
		}

		// Allocate the result array

		boolean[] result = new boolean[resultLength];

		// Convert from RLE

		RLEToBoolean (data, srcIndex, data.length - srcIndex, result, 0, resultLength);

		// Return result

		return result;
	}




	// Convert boolean RLE to two-dimensional array of boolean.

	public static boolean[][] booleanRLEToBoolean2D (byte[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];
		resultLength = (resultLength << 8) + (data[srcIndex++] & 0xFF);

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 8) + (data[srcIndex++] & 0xFF);
			resultLength = (resultLength << 8) + (data[srcIndex++] & 0xFF);
		}

		// Allocate the result array

		boolean[][] result = new boolean[resultLength][];

		// For each subarray ...

		for (int i = 0; i < result.length; ++i)
		{

			// Get array length

			resultLength = data[srcIndex++];
			resultLength = (resultLength << 8) + (data[srcIndex++] & 0xFF);

			// If it's the null flag, return null

			if (resultLength == -1)
			{
				result[i] = null;
				continue;
			}

			// If the large length flag is set, get the second word of length

			if ((resultLength & 0x8000) != 0)
			{
				resultLength = ((resultLength & 0x7FFF) << 8) + (data[srcIndex++] & 0xFF);
				resultLength = (resultLength << 8) + (data[srcIndex++] & 0xFF);
			}

			// Allocate the result array

			result[i] = new boolean[resultLength];

			// Convert from RLE

			srcIndex += (int)(RLEToBoolean (
				data, srcIndex, data.length - srcIndex, result[i], 0, resultLength ) >> 32L);
		}

		// Return result

		return result;
	}




	// ----- Byte RLE Array Conversion -----


	// The following routines convert entire arrays to or from byte RLE
	// format.
	//
	// A one-dimensional array is encoded as the array length, followed by
	// the byte RLE data.
	//
	// A two-dimensional array is encoded as the array length, followed by
	// the encoding for each one-dimensional subarray.
	//
	// The array length is encoded in either 2 or 4 bytes, as follows:
	//
	//	- If the array is null, the length is encoded in two bytes which
	//	  contain 0xFFFF.
	//
	//	- Otherwise, if the length of the array is less than or equal to
	//	  0x7FFF, the length is encoded in two bytes which contain the length.
	//
	//	- Otherwise, the length is encoded in four bytes which contain the
	//	  length plus 0x80000000.
	//
	// In all cases, the RLE data is stored in a one-dimensional array of
	// short.




	// Convert one-dimensional array of byte to byte RLE.

	public static short[] byte1DToByteRLE (byte[] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			short[] nullResult = new short[1];
			nullResult[0] = (short) 0xFFFF;
			return nullResult;
		}

		// Calculate length required

		int dstIndex = ((data.length <= 0x7FFF) ? 1 : 2)
			+ (((int) byteToRLE (data, 0, data.length)) / 2);

		// Allocate the array

		short[] result = new short[dstIndex];

		// Store the length

		if (data.length <= 0x7FFF)
		{
			result[0] = (short) data.length;
			dstIndex = 1;
		}
		else
		{
			result[0] = (short)((data.length >> 16) + 0x8000);
			result[1] = (short) data.length;
			dstIndex = 2;
		}

		// Convert to RLE

		byteToRLE (data, 0, data.length, result, dstIndex, result.length - dstIndex);

		// Return result

		return result;
	}




	// Convert one-dimensional array of char to byte RLE.
	//
	// Only the low 8 bits of each char are used.

	public static short[] char1DToByteRLE (char[] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			short[] nullResult = new short[1];
			nullResult[0] = (short) 0xFFFF;
			return nullResult;
		}

		// Calculate length required

		int dstIndex = ((data.length <= 0x7FFF) ? 1 : 2)
			+ (((int) byteToRLE (data, 0, data.length)) / 2);

		// Allocate the array

		short[] result = new short[dstIndex];

		// Store the length

		if (data.length <= 0x7FFF)
		{
			result[0] = (short) data.length;
			dstIndex = 1;
		}
		else
		{
			result[0] = (short)((data.length >> 16) + 0x8000);
			result[1] = (short) data.length;
			dstIndex = 2;
		}

		// Convert to RLE

		byteToRLE (data, 0, data.length, result, dstIndex, result.length - dstIndex);

		// Return result

		return result;
	}




	// Convert one-dimensional array of short to byte RLE.
	//
	// Only the low 8 bits of each short are used.

	public static short[] short1DToByteRLE (short[] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			short[] nullResult = new short[1];
			nullResult[0] = (short) 0xFFFF;
			return nullResult;
		}

		// Calculate length required

		int dstIndex = ((data.length <= 0x7FFF) ? 1 : 2)
			+ (((int) byteToRLE (data, 0, data.length)) / 2);

		// Allocate the array

		short[] result = new short[dstIndex];

		// Store the length

		if (data.length <= 0x7FFF)
		{
			result[0] = (short) data.length;
			dstIndex = 1;
		}
		else
		{
			result[0] = (short)((data.length >> 16) + 0x8000);
			result[1] = (short) data.length;
			dstIndex = 2;
		}

		// Convert to RLE

		byteToRLE (data, 0, data.length, result, dstIndex, result.length - dstIndex);

		// Return result

		return result;
	}




	// Convert one-dimensional array of int to byte RLE.
	//
	// Only the low 8 bits of each int are used.

	public static short[] int1DToByteRLE (int[] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			short[] nullResult = new short[1];
			nullResult[0] = (short) 0xFFFF;
			return nullResult;
		}

		// Calculate length required

		int dstIndex = ((data.length <= 0x7FFF) ? 1 : 2)
			+ (((int) byteToRLE (data, 0, data.length)) / 2);

		// Allocate the array

		short[] result = new short[dstIndex];

		// Store the length

		if (data.length <= 0x7FFF)
		{
			result[0] = (short) data.length;
			dstIndex = 1;
		}
		else
		{
			result[0] = (short)((data.length >> 16) + 0x8000);
			result[1] = (short) data.length;
			dstIndex = 2;
		}

		// Convert to RLE

		byteToRLE (data, 0, data.length, result, dstIndex, result.length - dstIndex);

		// Return result

		return result;
	}




	// Convert two-dimensional array of byte to byte RLE.

	public static short[] byte2DToByteRLE (byte[][] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			short[] nullResult = new short[1];
			nullResult[0] = (short) 0xFFFF;
			return nullResult;
		}

		// Length of top level array

		int dstIndex = ((data.length <= 0x7FFF) ? 1 : 2);

		// Add lengths of subarrays

		for (int i = 0; i < data.length; ++i)
		{
			dstIndex += ( ((data[i].length <= 0x7FFF) ? 1 : 2)
				+ (((int) byteToRLE (data[i], 0, data[i].length)) / 2) );
		}

		// Allocate the array

		short[] result = new short[dstIndex];

		// Store the length of the top level array

		if (data.length <= 0x7FFF)
		{
			result[0] = (short) data.length;
			dstIndex = 1;
		}
		else
		{
			result[0] = (short)((data.length >> 16) + 0x8000);
			result[1] = (short) data.length;
			dstIndex = 2;
		}

		// For each subarray ...

		for (int i = 0; i < data.length; ++i)
		{

			// If null subarray, return null encoding

			if (data[i] == null)
			{
				result[dstIndex++] = (short) 0xFFFF;
				continue;
			}

			// Store the length of the subarray

			if (data[i].length <= 0x7FFF)
			{
				result[dstIndex++] = (short) data[i].length;
			}
			else
			{
				result[dstIndex++] = (short)((data[i].length >> 16) + 0x8000);
				result[dstIndex++] = (short) data[i].length;
			}

			// Convert to RLE

			dstIndex += (int) byteToRLE (
				data[i], 0, data[i].length, result, dstIndex, result.length - dstIndex );
		}

		// Return result

		return result;
	}




	// Convert two-dimensional array of char to byte RLE.
	//
	// Only the low 8 bits of each char are used.

	public static short[] char2DToByteRLE (char[][] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			short[] nullResult = new short[1];
			nullResult[0] = (short) 0xFFFF;
			return nullResult;
		}

		// Length of top level array

		int dstIndex = ((data.length <= 0x7FFF) ? 1 : 2);

		// Add lengths of subarrays

		for (int i = 0; i < data.length; ++i)
		{
			dstIndex += ( ((data[i].length <= 0x7FFF) ? 1 : 2)
				+ (((int) byteToRLE (data[i], 0, data[i].length)) / 2) );
		}

		// Allocate the array

		short[] result = new short[dstIndex];

		// Store the length of the top level array

		if (data.length <= 0x7FFF)
		{
			result[0] = (short) data.length;
			dstIndex = 1;
		}
		else
		{
			result[0] = (short)((data.length >> 16) + 0x8000);
			result[1] = (short) data.length;
			dstIndex = 2;
		}

		// For each subarray ...

		for (int i = 0; i < data.length; ++i)
		{

			// If null subarray, return null encoding

			if (data[i] == null)
			{
				result[dstIndex++] = (short) 0xFFFF;
				continue;
			}

			// Store the length of the subarray

			if (data[i].length <= 0x7FFF)
			{
				result[dstIndex++] = (short) data[i].length;
			}
			else
			{
				result[dstIndex++] = (short)((data[i].length >> 16) + 0x8000);
				result[dstIndex++] = (short) data[i].length;
			}

			// Convert to RLE

			dstIndex += (int) byteToRLE (
				data[i], 0, data[i].length, result, dstIndex, result.length - dstIndex );
		}

		// Return result

		return result;
	}




	// Convert two-dimensional array of short to byte RLE.
	//
	// Only the low 8 bits of each short are used.

	public static short[] short2DToByteRLE (short[][] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			short[] nullResult = new short[1];
			nullResult[0] = (short) 0xFFFF;
			return nullResult;
		}

		// Length of top level array

		int dstIndex = ((data.length <= 0x7FFF) ? 1 : 2);

		// Add lengths of subarrays

		for (int i = 0; i < data.length; ++i)
		{
			dstIndex += ( ((data[i].length <= 0x7FFF) ? 1 : 2)
				+ (((int) byteToRLE (data[i], 0, data[i].length)) / 2) );
		}

		// Allocate the array

		short[] result = new short[dstIndex];

		// Store the length of the top level array

		if (data.length <= 0x7FFF)
		{
			result[0] = (short) data.length;
			dstIndex = 1;
		}
		else
		{
			result[0] = (short)((data.length >> 16) + 0x8000);
			result[1] = (short) data.length;
			dstIndex = 2;
		}

		// For each subarray ...

		for (int i = 0; i < data.length; ++i)
		{

			// If null subarray, return null encoding

			if (data[i] == null)
			{
				result[dstIndex++] = (short) 0xFFFF;
				continue;
			}

			// Store the length of the subarray

			if (data[i].length <= 0x7FFF)
			{
				result[dstIndex++] = (short) data[i].length;
			}
			else
			{
				result[dstIndex++] = (short)((data[i].length >> 16) + 0x8000);
				result[dstIndex++] = (short) data[i].length;
			}

			// Convert to RLE

			dstIndex += (int) byteToRLE (
				data[i], 0, data[i].length, result, dstIndex, result.length - dstIndex );
		}

		// Return result

		return result;
	}




	// Convert two-dimensional array of int to byte RLE.
	//
	// Only the low 8 bits of each int are used.

	public static short[] int2DToByteRLE (int[][] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			short[] nullResult = new short[1];
			nullResult[0] = (short) 0xFFFF;
			return nullResult;
		}

		// Length of top level array

		int dstIndex = ((data.length <= 0x7FFF) ? 1 : 2);

		// Add lengths of subarrays

		for (int i = 0; i < data.length; ++i)
		{
			dstIndex += ( ((data[i].length <= 0x7FFF) ? 1 : 2)
				+ (((int) byteToRLE (data[i], 0, data[i].length)) / 2) );
		}

		// Allocate the array

		short[] result = new short[dstIndex];

		// Store the length of the top level array

		if (data.length <= 0x7FFF)
		{
			result[0] = (short) data.length;
			dstIndex = 1;
		}
		else
		{
			result[0] = (short)((data.length >> 16) + 0x8000);
			result[1] = (short) data.length;
			dstIndex = 2;
		}

		// For each subarray ...

		for (int i = 0; i < data.length; ++i)
		{

			// If null subarray, return null encoding

			if (data[i] == null)
			{
				result[dstIndex++] = (short) 0xFFFF;
				continue;
			}

			// Store the length of the subarray

			if (data[i].length <= 0x7FFF)
			{
				result[dstIndex++] = (short) data[i].length;
			}
			else
			{
				result[dstIndex++] = (short)((data[i].length >> 16) + 0x8000);
				result[dstIndex++] = (short) data[i].length;
			}

			// Convert to RLE

			dstIndex += (int) byteToRLE (
				data[i], 0, data[i].length, result, dstIndex, result.length - dstIndex );
		}

		// Return result

		return result;
	}




	// Convert byte RLE to one-dimensional array of byte.

	public static byte[] byteRLEToByte1D (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		byte[] result = new byte[resultLength];

		// Convert from RLE

		RLEToByte (data, srcIndex, data.length - srcIndex, result, 0, resultLength);

		// Return result

		return result;
	}




	// Convert byte RLE to one-dimensional array of char.
	//
	// Each resulting byte is sign-extended to char.

	public static char[] byteRLEToChar1D (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		char[] result = new char[resultLength];

		// Convert from RLE

		RLEToByte (data, srcIndex, data.length - srcIndex, result, 0, resultLength);

		// Return result

		return result;
	}




	// Convert byte RLE to one-dimensional array of short.
	//
	// Each resulting byte is sign-extended to short.

	public static short[] byteRLEToShort1D (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		short[] result = new short[resultLength];

		// Convert from RLE

		RLEToByte (data, srcIndex, data.length - srcIndex, result, 0, resultLength);

		// Return result

		return result;
	}




	// Convert byte RLE to one-dimensional array of int.
	//
	// Each resulting byte is sign-extended to int.

	public static int[] byteRLEToInt1D (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		int[] result = new int[resultLength];

		// Convert from RLE

		RLEToByte (data, srcIndex, data.length - srcIndex, result, 0, resultLength);

		// Return result

		return result;
	}




	// Convert byte RLE to one-dimensional array of char.
	//
	// Each resulting byte is zero-extended to char.

	public static char[] byteRLEToChar1DUnsigned (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		char[] result = new char[resultLength];

		// Convert from RLE

		RLEToByteUnsigned (data, srcIndex, data.length - srcIndex, result, 0, resultLength);

		// Return result

		return result;
	}




	// Convert byte RLE to one-dimensional array of short.
	//
	// Each resulting byte is zero-extended to short.

	public static short[] byteRLEToShort1DUnsigned (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		short[] result = new short[resultLength];

		// Convert from RLE

		RLEToByteUnsigned (data, srcIndex, data.length - srcIndex, result, 0, resultLength);

		// Return result

		return result;
	}




	// Convert byte RLE to one-dimensional array of int.
	//
	// Each resulting byte is zero-extended to int.

	public static int[] byteRLEToInt1DUnsigned (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		int[] result = new int[resultLength];

		// Convert from RLE

		RLEToByteUnsigned (data, srcIndex, data.length - srcIndex, result, 0, resultLength);

		// Return result

		return result;
	}




	// Convert byte RLE to two-dimensional array of byte.

	public static byte[][] byteRLEToByte2D (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		byte[][] result = new byte[resultLength][];

		// For each subarray ...

		for (int i = 0; i < result.length; ++i)
		{

			// Get array length

			resultLength = data[srcIndex++];

			// If it's the null flag, return null

			if (resultLength == -1)
			{
				result[i] = null;
				continue;
			}

			// If the large length flag is set, get the second word of length

			if ((resultLength & 0x8000) != 0)
			{
				resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
			}

			// Allocate the result array

			result[i] = new byte[resultLength];

			// Convert from RLE

			srcIndex += (int)(RLEToByte (
				data, srcIndex, data.length - srcIndex, result[i], 0, resultLength ) >> 32L);
		}

		// Return result

		return result;
	}




	// Convert byte RLE to two-dimensional array of char.
	//
	// Each resulting byte is sign-extended to char.

	public static char[][] byteRLEToChar2D (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		char[][] result = new char[resultLength][];

		// For each subarray ...

		for (int i = 0; i < result.length; ++i)
		{

			// Get array length

			resultLength = data[srcIndex++];

			// If it's the null flag, return null

			if (resultLength == -1)
			{
				result[i] = null;
				continue;
			}

			// If the large length flag is set, get the second word of length

			if ((resultLength & 0x8000) != 0)
			{
				resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
			}

			// Allocate the result array

			result[i] = new char[resultLength];

			// Convert from RLE

			srcIndex += (int)(RLEToByte (
				data, srcIndex, data.length - srcIndex, result[i], 0, resultLength ) >> 32L);
		}

		// Return result

		return result;
	}




	// Convert byte RLE to two-dimensional array of short.
	//
	// Each resulting byte is sign-extended to short.

	public static short[][] byteRLEToShort2D (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		short[][] result = new short[resultLength][];

		// For each subarray ...

		for (int i = 0; i < result.length; ++i)
		{

			// Get array length

			resultLength = data[srcIndex++];

			// If it's the null flag, return null

			if (resultLength == -1)
			{
				result[i] = null;
				continue;
			}

			// If the large length flag is set, get the second word of length

			if ((resultLength & 0x8000) != 0)
			{
				resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
			}

			// Allocate the result array

			result[i] = new short[resultLength];

			// Convert from RLE

			srcIndex += (int)(RLEToByte (
				data, srcIndex, data.length - srcIndex, result[i], 0, resultLength ) >> 32L);
		}

		// Return result

		return result;
	}




	// Convert byte RLE to two-dimensional array of int.
	//
	// Each resulting byte is sign-extended to int.

	public static int[][] byteRLEToInt2D (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		int[][] result = new int[resultLength][];

		// For each subarray ...

		for (int i = 0; i < result.length; ++i)
		{

			// Get array length

			resultLength = data[srcIndex++];

			// If it's the null flag, return null

			if (resultLength == -1)
			{
				result[i] = null;
				continue;
			}

			// If the large length flag is set, get the second word of length

			if ((resultLength & 0x8000) != 0)
			{
				resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
			}

			// Allocate the result array

			result[i] = new int[resultLength];

			// Convert from RLE

			srcIndex += (int)(RLEToByte (
				data, srcIndex, data.length - srcIndex, result[i], 0, resultLength ) >> 32L);
		}

		// Return result

		return result;
	}




	// Convert byte RLE to two-dimensional array of char.
	//
	// Each resulting byte is zero-extended to char.

	public static char[][] byteRLEToChar2DUnsigned (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		char[][] result = new char[resultLength][];

		// For each subarray ...

		for (int i = 0; i < result.length; ++i)
		{

			// Get array length

			resultLength = data[srcIndex++];

			// If it's the null flag, return null

			if (resultLength == -1)
			{
				result[i] = null;
				continue;
			}

			// If the large length flag is set, get the second word of length

			if ((resultLength & 0x8000) != 0)
			{
				resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
			}

			// Allocate the result array

			result[i] = new char[resultLength];

			// Convert from RLE

			srcIndex += (int)(RLEToByteUnsigned (
				data, srcIndex, data.length - srcIndex, result[i], 0, resultLength ) >> 32L);
		}

		// Return result

		return result;
	}




	// Convert byte RLE to two-dimensional array of short.
	//
	// Each resulting byte is zero-extended to short.

	public static short[][] byteRLEToShort2DUnsigned (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		short[][] result = new short[resultLength][];

		// For each subarray ...

		for (int i = 0; i < result.length; ++i)
		{

			// Get array length

			resultLength = data[srcIndex++];

			// If it's the null flag, return null

			if (resultLength == -1)
			{
				result[i] = null;
				continue;
			}

			// If the large length flag is set, get the second word of length

			if ((resultLength & 0x8000) != 0)
			{
				resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
			}

			// Allocate the result array

			result[i] = new short[resultLength];

			// Convert from RLE

			srcIndex += (int)(RLEToByteUnsigned (
				data, srcIndex, data.length - srcIndex, result[i], 0, resultLength ) >> 32L);
		}

		// Return result

		return result;
	}




	// Convert byte RLE to two-dimensional array of int.
	//
	// Each resulting byte is zero-extended to int.

	public static int[][] byteRLEToInt2DUnsigned (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		int[][] result = new int[resultLength][];

		// For each subarray ...

		for (int i = 0; i < result.length; ++i)
		{

			// Get array length

			resultLength = data[srcIndex++];

			// If it's the null flag, return null

			if (resultLength == -1)
			{
				result[i] = null;
				continue;
			}

			// If the large length flag is set, get the second word of length

			if ((resultLength & 0x8000) != 0)
			{
				resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
			}

			// Allocate the result array

			result[i] = new int[resultLength];

			// Convert from RLE

			srcIndex += (int)(RLEToByteUnsigned (
				data, srcIndex, data.length - srcIndex, result[i], 0, resultLength ) >> 32L);
		}

		// Return result

		return result;
	}




	// ----- Short RLE Array Conversion -----


	// The following routines convert entire arrays to or from short RLE
	// format.
	//
	// A one-dimensional array is encoded as the array length, followed by
	// the short RLE data.
	//
	// A two-dimensional array is encoded as the array length, followed by
	// the encoding for each one-dimensional subarray.
	//
	// The array length is encoded in either 2 or 4 bytes, as follows:
	//
	//	- If the array is null, the length is encoded in two bytes which
	//	  contain 0xFFFF.
	//
	//	- Otherwise, if the length of the array is less than or equal to
	//	  0x7FFF, the length is encoded in two bytes which contain the length.
	//
	//	- Otherwise, the length is encoded in four bytes which contain the
	//	  length plus 0x80000000.
	//
	// In all cases, the RLE data is stored in a one-dimensional array of
	// short.




	// Convert one-dimensional array of char to short RLE.

	public static short[] char1DToShortRLE (char[] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			short[] nullResult = new short[1];
			nullResult[0] = (short) 0xFFFF;
			return nullResult;
		}

		// Calculate length required

		int dstIndex = ((data.length <= 0x7FFF) ? 1 : 2)
			+ (((int) shortToRLE (data, 0, data.length)) / 2);

		// Allocate the array

		short[] result = new short[dstIndex];

		// Store the length

		if (data.length <= 0x7FFF)
		{
			result[0] = (short) data.length;
			dstIndex = 1;
		}
		else
		{
			result[0] = (short)((data.length >> 16) + 0x8000);
			result[1] = (short) data.length;
			dstIndex = 2;
		}

		// Convert to RLE

		shortToRLE (data, 0, data.length, result, dstIndex, result.length - dstIndex);

		// Return result

		return result;
	}




	// Convert one-dimensional array of short to short RLE.

	public static short[] short1DToShortRLE (short[] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			short[] nullResult = new short[1];
			nullResult[0] = (short) 0xFFFF;
			return nullResult;
		}

		// Calculate length required

		int dstIndex = ((data.length <= 0x7FFF) ? 1 : 2)
			+ (((int) shortToRLE (data, 0, data.length)) / 2);

		// Allocate the array

		short[] result = new short[dstIndex];

		// Store the length

		if (data.length <= 0x7FFF)
		{
			result[0] = (short) data.length;
			dstIndex = 1;
		}
		else
		{
			result[0] = (short)((data.length >> 16) + 0x8000);
			result[1] = (short) data.length;
			dstIndex = 2;
		}

		// Convert to RLE

		shortToRLE (data, 0, data.length, result, dstIndex, result.length - dstIndex);

		// Return result

		return result;
	}




	// Convert one-dimensional array of int to short RLE.
	//
	// Only the low 16 bits of each int are used.

	public static short[] int1DToShortRLE (int[] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			short[] nullResult = new short[1];
			nullResult[0] = (short) 0xFFFF;
			return nullResult;
		}

		// Calculate length required

		int dstIndex = ((data.length <= 0x7FFF) ? 1 : 2)
			+ (((int) shortToRLE (data, 0, data.length)) / 2);

		// Allocate the array

		short[] result = new short[dstIndex];

		// Store the length

		if (data.length <= 0x7FFF)
		{
			result[0] = (short) data.length;
			dstIndex = 1;
		}
		else
		{
			result[0] = (short)((data.length >> 16) + 0x8000);
			result[1] = (short) data.length;
			dstIndex = 2;
		}

		// Convert to RLE

		shortToRLE (data, 0, data.length, result, dstIndex, result.length - dstIndex);

		// Return result

		return result;
	}




	// Convert two-dimensional array of char to short RLE.

	public static short[] char2DToShortRLE (char[][] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			short[] nullResult = new short[1];
			nullResult[0] = (short) 0xFFFF;
			return nullResult;
		}

		// Length of top level array

		int dstIndex = ((data.length <= 0x7FFF) ? 1 : 2);

		// Add lengths of subarrays

		for (int i = 0; i < data.length; ++i)
		{
			dstIndex += ( ((data[i].length <= 0x7FFF) ? 1 : 2)
				+ (((int) shortToRLE (data[i], 0, data[i].length)) / 2) );
		}

		// Allocate the array

		short[] result = new short[dstIndex];

		// Store the length of the top level array

		if (data.length <= 0x7FFF)
		{
			result[0] = (short) data.length;
			dstIndex = 1;
		}
		else
		{
			result[0] = (short)((data.length >> 16) + 0x8000);
			result[1] = (short) data.length;
			dstIndex = 2;
		}

		// For each subarray ...

		for (int i = 0; i < data.length; ++i)
		{

			// If null subarray, return null encoding

			if (data[i] == null)
			{
				result[dstIndex++] = (short) 0xFFFF;
				continue;
			}

			// Store the length of the subarray

			if (data[i].length <= 0x7FFF)
			{
				result[dstIndex++] = (short) data[i].length;
			}
			else
			{
				result[dstIndex++] = (short)((data[i].length >> 16) + 0x8000);
				result[dstIndex++] = (short) data[i].length;
			}

			// Convert to RLE

			dstIndex += (int) shortToRLE (
				data[i], 0, data[i].length, result, dstIndex, result.length - dstIndex );
		}

		// Return result

		return result;
	}




	// Convert two-dimensional array of short to short RLE.

	public static short[] short2DToShortRLE (short[][] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			short[] nullResult = new short[1];
			nullResult[0] = (short) 0xFFFF;
			return nullResult;
		}

		// Length of top level array

		int dstIndex = ((data.length <= 0x7FFF) ? 1 : 2);

		// Add lengths of subarrays

		for (int i = 0; i < data.length; ++i)
		{
			dstIndex += ( ((data[i].length <= 0x7FFF) ? 1 : 2)
				+ (((int) shortToRLE (data[i], 0, data[i].length)) / 2) );
		}

		// Allocate the array

		short[] result = new short[dstIndex];

		// Store the length of the top level array

		if (data.length <= 0x7FFF)
		{
			result[0] = (short) data.length;
			dstIndex = 1;
		}
		else
		{
			result[0] = (short)((data.length >> 16) + 0x8000);
			result[1] = (short) data.length;
			dstIndex = 2;
		}

		// For each subarray ...

		for (int i = 0; i < data.length; ++i)
		{

			// If null subarray, return null encoding

			if (data[i] == null)
			{
				result[dstIndex++] = (short) 0xFFFF;
				continue;
			}

			// Store the length of the subarray

			if (data[i].length <= 0x7FFF)
			{
				result[dstIndex++] = (short) data[i].length;
			}
			else
			{
				result[dstIndex++] = (short)((data[i].length >> 16) + 0x8000);
				result[dstIndex++] = (short) data[i].length;
			}

			// Convert to RLE

			dstIndex += (int) shortToRLE (
				data[i], 0, data[i].length, result, dstIndex, result.length - dstIndex );
		}

		// Return result

		return result;
	}




	// Convert two-dimensional array of int to short RLE.
	//
	// Only the low 16 bits of each int are used.

	public static short[] int2DToShortRLE (int[][] data)
	{

		// If null, return null encoding

		if (data == null)
		{
			short[] nullResult = new short[1];
			nullResult[0] = (short) 0xFFFF;
			return nullResult;
		}

		// Length of top level array

		int dstIndex = ((data.length <= 0x7FFF) ? 1 : 2);

		// Add lengths of subarrays

		for (int i = 0; i < data.length; ++i)
		{
			dstIndex += ( ((data[i].length <= 0x7FFF) ? 1 : 2)
				+ (((int) shortToRLE (data[i], 0, data[i].length)) / 2) );
		}

		// Allocate the array

		short[] result = new short[dstIndex];

		// Store the length of the top level array

		if (data.length <= 0x7FFF)
		{
			result[0] = (short) data.length;
			dstIndex = 1;
		}
		else
		{
			result[0] = (short)((data.length >> 16) + 0x8000);
			result[1] = (short) data.length;
			dstIndex = 2;
		}

		// For each subarray ...

		for (int i = 0; i < data.length; ++i)
		{

			// If null subarray, return null encoding

			if (data[i] == null)
			{
				result[dstIndex++] = (short) 0xFFFF;
				continue;
			}

			// Store the length of the subarray

			if (data[i].length <= 0x7FFF)
			{
				result[dstIndex++] = (short) data[i].length;
			}
			else
			{
				result[dstIndex++] = (short)((data[i].length >> 16) + 0x8000);
				result[dstIndex++] = (short) data[i].length;
			}

			// Convert to RLE

			dstIndex += (int) shortToRLE (
				data[i], 0, data[i].length, result, dstIndex, result.length - dstIndex );
		}

		// Return result

		return result;
	}




	// Convert short RLE to one-dimensional array of char.

	public static char[] shortRLEToChar1D (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		char[] result = new char[resultLength];

		// Convert from RLE

		RLEToShort (data, srcIndex, data.length - srcIndex, result, 0, resultLength);

		// Return result

		return result;
	}




	// Convert short RLE to one-dimensional array of short.

	public static short[] shortRLEToShort1D (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		short[] result = new short[resultLength];

		// Convert from RLE

		RLEToShort (data, srcIndex, data.length - srcIndex, result, 0, resultLength);

		// Return result

		return result;
	}




	// Convert short RLE to one-dimensional array of int.
	//
	// Each resulting short is sign-extended to int.

	public static int[] shortRLEToInt1D (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		int[] result = new int[resultLength];

		// Convert from RLE

		RLEToShort (data, srcIndex, data.length - srcIndex, result, 0, resultLength);

		// Return result

		return result;
	}




	// Convert short RLE to one-dimensional array of int.
	//
	// Each resulting short is zero-extended to int.

	public static int[] shortRLEToInt1DUnsigned (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		int[] result = new int[resultLength];

		// Convert from RLE

		RLEToShortUnsigned (data, srcIndex, data.length - srcIndex, result, 0, resultLength);

		// Return result

		return result;
	}




	// Convert short RLE to two-dimensional array of char.

	public static char[][] shortRLEToChar2D (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		char[][] result = new char[resultLength][];

		// For each subarray ...

		for (int i = 0; i < result.length; ++i)
		{

			// Get array length

			resultLength = data[srcIndex++];

			// If it's the null flag, return null

			if (resultLength == -1)
			{
				result[i] = null;
				continue;
			}

			// If the large length flag is set, get the second word of length

			if ((resultLength & 0x8000) != 0)
			{
				resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
			}

			// Allocate the result array

			result[i] = new char[resultLength];

			// Convert from RLE

			srcIndex += (int)(RLEToShort (
				data, srcIndex, data.length - srcIndex, result[i], 0, resultLength ) >> 32L);
		}

		// Return result

		return result;
	}




	// Convert short RLE to two-dimensional array of short.

	public static short[][] shortRLEToShort2D (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		short[][] result = new short[resultLength][];

		// For each subarray ...

		for (int i = 0; i < result.length; ++i)
		{

			// Get array length

			resultLength = data[srcIndex++];

			// If it's the null flag, return null

			if (resultLength == -1)
			{
				result[i] = null;
				continue;
			}

			// If the large length flag is set, get the second word of length

			if ((resultLength & 0x8000) != 0)
			{
				resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
			}

			// Allocate the result array

			result[i] = new short[resultLength];

			// Convert from RLE

			srcIndex += (int)(RLEToShort (
				data, srcIndex, data.length - srcIndex, result[i], 0, resultLength ) >> 32L);
		}

		// Return result

		return result;
	}




	// Convert short RLE to two-dimensional array of int.
	//
	// Each resulting short is sign-extended to int.

	public static int[][] shortRLEToInt2D (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		int[][] result = new int[resultLength][];

		// For each subarray ...

		for (int i = 0; i < result.length; ++i)
		{

			// Get array length

			resultLength = data[srcIndex++];

			// If it's the null flag, return null

			if (resultLength == -1)
			{
				result[i] = null;
				continue;
			}

			// If the large length flag is set, get the second word of length

			if ((resultLength & 0x8000) != 0)
			{
				resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
			}

			// Allocate the result array

			result[i] = new int[resultLength];

			// Convert from RLE

			srcIndex += (int)(RLEToShort (
				data, srcIndex, data.length - srcIndex, result[i], 0, resultLength ) >> 32L);
		}

		// Return result

		return result;
	}




	// Convert short RLE to two-dimensional array of int.
	//
	// Each resulting short is zero-extended to int.

	public static int[][] shortRLEToInt2DUnsigned (short[] data)
	{

		// Index into array

		int srcIndex = 0;

		// Get array length

		int resultLength = data[srcIndex++];

		// If it's the null flag, return null

		if (resultLength == -1)
		{
			return null;
		}

		// If the large length flag is set, get the second word of length

		if ((resultLength & 0x8000) != 0)
		{
			resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
		}

		// Allocate the result array

		int[][] result = new int[resultLength][];

		// For each subarray ...

		for (int i = 0; i < result.length; ++i)
		{

			// Get array length

			resultLength = data[srcIndex++];

			// If it's the null flag, return null

			if (resultLength == -1)
			{
				result[i] = null;
				continue;
			}

			// If the large length flag is set, get the second word of length

			if ((resultLength & 0x8000) != 0)
			{
				resultLength = ((resultLength & 0x7FFF) << 16) + (data[srcIndex++] & 0xFFFF);
			}

			// Allocate the result array

			result[i] = new int[resultLength];

			// Convert from RLE

			srcIndex += (int)(RLEToShortUnsigned (
				data, srcIndex, data.length - srcIndex, result[i], 0, resultLength ) >> 32L);
		}

		// Return result

		return result;
	}




}

