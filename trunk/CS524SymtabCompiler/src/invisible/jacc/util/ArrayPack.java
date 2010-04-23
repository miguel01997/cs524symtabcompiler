// Copyright 1997 Invisible Software, Inc.

package invisible.jacc.util;


/*->

  ArrayPack is a class that defines a set of pack/unpack functions for arrays.

  Packing consists of taking a group of consecutive elements from a source
  array, concatenating their bits, and then storing the result into an element
  of a destination array.  If the concatenated source elements have fewer bits
  than the destination element, then the result can be either sign-extended or
  zero-extended.

  Unpacking is the reverse process.  Unpacking consists of taking an element
  from a source array, and splitting its bits into a group of consecutive
  elements of a destination array.  If the source element has more bits than
  the combined destination elements, then high-order bits of the source element
  are discarded.

  Among other uses, packing and unpacking can be used to convert from one array
  type to another.  To round out the set of conversions, this class also
  defines boolean-integral, integral-floating, and floating-floating conversion
  functions.
  
  Since all functions are static, there is no need to ever create an instance
  of this class.

->*/


public final class ArrayPack 
{




	// ----- Array pack routines -----


	// pack (X[] src, int srcOff, Y[] dst, int dstOff, int len, int pf)
	//
	// Packs elements from src to dst.  Source elements are packed beginning
	// at src[srcOff], and destination elements are packed beginning at
	// dst[dstOff].  The number of destination elements packed is len.  The
	// number of source elements packed is len*pf.
	//
	// Each group of pf consecutive source elements is packed into a single
	// destination element, most significant element first, and sign-extended
	// if necessary.
	//
	// This function is defined for the following combinations of types:
	//
	// 1.  X and Y are both integral types (byte, char, short, int, or long),
	// where Y has at least as many bits as X, but X and Y are different types.
	// In this case, pf can range from 1 to bits(Y)/bits(X).
	//
	// 2.  X is an integral type and Y is a floating type (float or double),
	// where Y has at least as many bits as X.  In this case, pf must equal
	// bits(Y)/bits(X).
	//
	// 3.  X is a floating type and Y is an integral type, where Y has at the
	// same number of bits as X.  In this case, pf must equal 1.




	public static void pack (byte[] src, int srcOff, char[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = (char) src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (char) ((x << 8) + (src[srcIndex++] & 0xFF));
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (byte[] src, int srcOff, short[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = (short) src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (short) ((x << 8) + (src[srcIndex++] & 0xFF));
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (byte[] src, int srcOff, int[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (x << 8) + (src[srcIndex++] & 0xFF);
			}

			return;

		case 3:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 3)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = (x << 8) + (src[srcIndex++] & 0xFF);
			}

			return;

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 4)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = (x << 8) + (src[srcIndex++] & 0xFF);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (byte[] src, int srcOff, long[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (x << 8) + (src[srcIndex++] & 0xFF);
			}

			return;

		case 3:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 3)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = (x << 8) + (src[srcIndex++] & 0xFF);
			}

			return;

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 4)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = (x << 8) + (src[srcIndex++] & 0xFF);
			}

			return;

		case 5:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 5)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++];
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 8) + (src[srcIndex++] & 0xFF))) & 0xFFFFFFFFL);
			}

			return;

		case 6:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 6)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++];
				y = (y << 8) + (src[srcIndex++] & 0xFF);
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 8) + (src[srcIndex++] & 0xFF))) & 0xFFFFFFFFL);
			}

			return;

		case 7:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 7)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++];
				y = (y << 8) + (src[srcIndex++] & 0xFF);
				y = (y << 8) + (src[srcIndex++] & 0xFF);
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 8) + (src[srcIndex++] & 0xFF))) & 0xFFFFFFFFL);
			}

			return;

		case 8:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 8)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++];
				y = (y << 8) + (src[srcIndex++] & 0xFF);
				y = (y << 8) + (src[srcIndex++] & 0xFF);
				y = (y << 8) + (src[srcIndex++] & 0xFF);
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 8) + (src[srcIndex++] & 0xFF))) & 0xFFFFFFFFL);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (byte[] src, int srcOff, float[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 4)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = Float.intBitsToFloat ((x << 8) + (src[srcIndex++] & 0xFF));
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (byte[] src, int srcOff, double[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 8:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 8)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++];
				y = (y << 8) + (src[srcIndex++] & 0xFF);
				y = (y << 8) + (src[srcIndex++] & 0xFF);
				y = (y << 8) + (src[srcIndex++] & 0xFF);
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = Double.longBitsToDouble ( (((long) y) << 32L)
					+ (((long)((x << 8) + (src[srcIndex++] & 0xFF))) & 0xFFFFFFFFL) );
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (char[] src, int srcOff, short[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = (short) src[srcIndex++];
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (char[] src, int srcOff, int[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = (int)(short) src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (x << 16) + (src[srcIndex++] & 0xFFFF);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (char[] src, int srcOff, long[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = (long)(short) src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (x << 16) + (src[srcIndex++] & 0xFFFF);
			}

			return;

		case 3:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 3)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int y = (int)(short) src[srcIndex++];
				int x = src[srcIndex++];
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 16) + (src[srcIndex++] & 0xFFFF))) & 0xFFFFFFFFL);
			}

			return;

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 4)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++];
				y = (y << 16) + (src[srcIndex++] & 0xFFFF);
				int x = src[srcIndex++];
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 16) + (src[srcIndex++] & 0xFFFF))) & 0xFFFFFFFFL);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (char[] src, int srcOff, float[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = Float.intBitsToFloat ((x << 16) + (src[srcIndex++] & 0xFFFF));
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (char[] src, int srcOff, double[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 4)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++];
				y = (y << 16) + (src[srcIndex++] & 0xFFFF);
				int x = src[srcIndex++];
				dst[dstIndex++] = Double.longBitsToDouble ( (((long) y) << 32L)
					+ (((long)((x << 16) + (src[srcIndex++] & 0xFFFF))) & 0xFFFFFFFFL) );
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (short[] src, int srcOff, char[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = (char) src[srcIndex++];
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (short[] src, int srcOff, int[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (x << 16) + (src[srcIndex++] & 0xFFFF);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (short[] src, int srcOff, long[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (x << 16) + (src[srcIndex++] & 0xFFFF);
			}

			return;

		case 3:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 3)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++];
				int x = src[srcIndex++];
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 16) + (src[srcIndex++] & 0xFFFF))) & 0xFFFFFFFFL);
			}

			return;

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 4)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++];
				y = (y << 16) + (src[srcIndex++] & 0xFFFF);
				int x = src[srcIndex++];
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 16) + (src[srcIndex++] & 0xFFFF))) & 0xFFFFFFFFL);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (short[] src, int srcOff, float[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = Float.intBitsToFloat ((x << 16) + (src[srcIndex++] & 0xFFFF));
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (short[] src, int srcOff, double[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 4)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++];
				y = (y << 16) + (src[srcIndex++] & 0xFFFF);
				int x = src[srcIndex++];
				dst[dstIndex++] = Double.longBitsToDouble ( (((long) y) << 32L)
					+ (((long)((x << 16) + (src[srcIndex++] & 0xFFFF))) & 0xFFFFFFFFL) );
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (int[] src, int srcOff, long[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (((long) x) << 32L)
					+ (((long) src[srcIndex++]) & 0xFFFFFFFFL);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (int[] src, int srcOff, float[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = Float.intBitsToFloat (src[srcIndex++]);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (int[] src, int srcOff, double[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = Double.longBitsToDouble ( (((long) x) << 32L)
					+ (((long) src[srcIndex++]) & 0xFFFFFFFFL) );
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	public static void pack (long[] src, int srcOff, double[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.pack");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = Double.longBitsToDouble (src[srcIndex++]);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.pack");
		}
	}




	// ----- Array pack unsigned routines -----


	// packUnsigned (X[] src, int srcOff, Y[] dst, int dstOff, int len, int pf)
	//
	// Packs elements from src to dst.  Source elements are packed beginning
	// at src[srcOff], and destination elements are packed beginning at
	// dst[dstOff].  The number of destination elements packed is len.  The
	// number of source elements packed is len*pf.
	//
	// Each group of pf consecutive source elements is packed into a single
	// destination element, most significant element first, and zero-extended
	// if necessary.
	//
	// This function is defined for the following combinations of types:
	//
	// 1.  X and Y are both integral types (byte, char, short, int, or long),
	// where Y has at least as many bits as X, but X and Y are different types.
	// In this case, pf can range from 1 to bits(Y)/bits(X).




	public static void packUnsigned (byte[] src, int srcOff, char[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = (char) (src[srcIndex++] & 0xFF);
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (char) ((x << 8) + (src[srcIndex++] & 0xFF));
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
		}
	}




	public static void packUnsigned (byte[] src, int srcOff, short[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = (short) (src[srcIndex++] & 0xFF);
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (short) ((x << 8) + (src[srcIndex++] & 0xFF));
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
		}
	}




	public static void packUnsigned (byte[] src, int srcOff, int[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = src[srcIndex++] & 0xFF;
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++] & 0xFF;
				dst[dstIndex++] = (x << 8) + (src[srcIndex++] & 0xFF);
			}

			return;

		case 3:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 3)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++] & 0xFF;
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = (x << 8) + (src[srcIndex++] & 0xFF);
			}

			return;

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 4)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = (x << 8) + (src[srcIndex++] & 0xFF);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
		}
	}




	public static void packUnsigned (byte[] src, int srcOff, long[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = src[srcIndex++] & 0xFF;
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++] & 0xFF;
				dst[dstIndex++] = (x << 8) + (src[srcIndex++] & 0xFF);
			}

			return;

		case 3:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 3)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++] & 0xFF;
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = (x << 8) + (src[srcIndex++] & 0xFF);
			}

			return;

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 4)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = ((long)((x << 8) + (src[srcIndex++] & 0xFF))) & 0xFFFFFFFFL;
			}

			return;

		case 5:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 5)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++] & 0xFF;
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 8) + (src[srcIndex++] & 0xFF))) & 0xFFFFFFFFL);
			}

			return;

		case 6:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 6)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++] & 0xFF;
				y = (y << 8) + (src[srcIndex++] & 0xFF);
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 8) + (src[srcIndex++] & 0xFF))) & 0xFFFFFFFFL);
			}

			return;

		case 7:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 7)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++] & 0xFF;
				y = (y << 8) + (src[srcIndex++] & 0xFF);
				y = (y << 8) + (src[srcIndex++] & 0xFF);
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 8) + (src[srcIndex++] & 0xFF))) & 0xFFFFFFFFL);
			}

			return;

		case 8:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 8)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++];
				y = (y << 8) + (src[srcIndex++] & 0xFF);
				y = (y << 8) + (src[srcIndex++] & 0xFF);
				y = (y << 8) + (src[srcIndex++] & 0xFF);
				int x = src[srcIndex++];
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				x = (x << 8) + (src[srcIndex++] & 0xFF);
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 8) + (src[srcIndex++] & 0xFF))) & 0xFFFFFFFFL);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
		}
	}




	public static void packUnsigned (char[] src, int srcOff, short[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = (short) src[srcIndex++];
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
		}
	}




	public static void packUnsigned (char[] src, int srcOff, int[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = src[srcIndex++] & 0xFFFF;
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (x << 16) + (src[srcIndex++] & 0xFFFF);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
		}
	}




	public static void packUnsigned (char[] src, int srcOff, long[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = src[srcIndex++] & 0xFFFF;
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = ((long)((x << 16) + (src[srcIndex++] & 0xFFFF))) & 0xFFFFFFFFL;
			}

			return;

		case 3:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 3)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int y = (int)(short) src[srcIndex++] & 0xFFFF;
				int x = src[srcIndex++];
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 16) + (src[srcIndex++] & 0xFFFF))) & 0xFFFFFFFFL);
			}

			return;

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 4)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++];
				y = (y << 16) + (src[srcIndex++] & 0xFFFF);
				int x = src[srcIndex++];
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 16) + (src[srcIndex++] & 0xFFFF))) & 0xFFFFFFFFL);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
		}
	}




	public static void packUnsigned (short[] src, int srcOff, char[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = (char) src[srcIndex++];
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
		}
	}





	public static void packUnsigned (short[] src, int srcOff, int[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = src[srcIndex++] & 0xFFFF;
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (x << 16) + (src[srcIndex++] & 0xFFFF);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
		}
	}



	public static void packUnsigned (short[] src, int srcOff, long[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = src[srcIndex++] & 0xFFFF;
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = ((long)((x << 16) + (src[srcIndex++] & 0xFFFF))) & 0xFFFFFFFFL;
			}

			return;

		case 3:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 3)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++] & 0xFFFF;
				int x = src[srcIndex++];
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 16) + (src[srcIndex++] & 0xFFFF))) & 0xFFFFFFFFL);
			}

			return;

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 4)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int y = src[srcIndex++];
				y = (y << 16) + (src[srcIndex++] & 0xFFFF);
				int x = src[srcIndex++];
				dst[dstIndex++] = (((long) y) << 32L)
					+ (((long)((x << 16) + (src[srcIndex++] & 0xFFFF))) & 0xFFFFFFFFL);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
		}
	}




	public static void packUnsigned (int[] src, int srcOff, long[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int dstLimit = dstIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				dst[dstIndex++] = ((long) src[srcIndex++]) & 0xFFFFFFFFL;
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + (len * 2)) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
			}

			while (dstIndex < dstLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (((long) x) << 32L)
					+ (((long) src[srcIndex++]) & 0xFFFFFFFFL);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.packUnsigned");
		}
	}




	// ----- Array unpack routines -----


	// unpack (X[] src, int srcOff, Y[] dst, int dstOff, int len, int pf)
	//
	// Unpacks elements from src to dst.  Source elements are unpacked beginning
	// at src[srcOff], and destination elements are unpacked beginning at
	// dst[dstOff].  The number of source elements unpacked is len.  The number
	// of destination elements unpacked is len*pf.
	//
	// Each source element is truncated by discarding high-order bits, if
	// necessary.  Then, the truncated source element is unpacked into pf
	// consecutive destination elements, most significant element first.
	//
	// This function is defined for the following combinations of types:
	//
	// 1.  X and Y are both integral types (byte, char, short, int, or long),
	// where X has at least as many bits as Y, but X and Y are different types.
	// In this case, pf can range from 1 to bits(X)/bits(Y).
	//
	// 2.  X is a floating type (float or double) and Y is an integral type,
	// where X has at least as many bits as Y.  In this case, pf must equal
	// bits(X)/bits(Y).
	//
	// 3.  X is an integral type and Y is a floating type, where X has at the
	// same number of bits as Y.  In this case, pf must equal 1.




	public static void unpack (char[] src, int srcOff, byte[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				dst[dstIndex++] = (byte) src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 2)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (short[] src, int srcOff, byte[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				dst[dstIndex++] = (byte) src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 2)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (int[] src, int srcOff, byte[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				dst[dstIndex++] = (byte) src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 2)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
			}

			return;

		case 3:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 3)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (byte) (x >> 16);
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
			}

			return;

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 4)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (byte) (x >> 24);
				dst[dstIndex++] = (byte) (x >> 16);
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (long[] src, int srcOff, byte[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				dst[dstIndex++] = (byte) src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 2)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				int x = (int) src[srcIndex++];
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
			}

			return;

		case 3:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 3)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				int x = (int) src[srcIndex++];
				dst[dstIndex++] = (byte) (x >> 16);
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
			}

			return;

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 4)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				int x = (int) src[srcIndex++];
				dst[dstIndex++] = (byte) (x >> 24);
				dst[dstIndex++] = (byte) (x >> 16);
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
			}

			return;

		case 5:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 5)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				long y = src[srcIndex++];
				dst[dstIndex++] = (byte) (y >> 32L);
				int x = (int) y;
				dst[dstIndex++] = (byte) (x >> 24);
				dst[dstIndex++] = (byte) (x >> 16);
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
			}

			return;

		case 6:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 6)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				long y = src[srcIndex++];
				int x = (int) (y >> 32L);
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
				x = (int) y;
				dst[dstIndex++] = (byte) (x >> 24);
				dst[dstIndex++] = (byte) (x >> 16);
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
			}

			return;

		case 7:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 7)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				long y = src[srcIndex++];
				int x = (int) (y >> 32L);
				dst[dstIndex++] = (byte) (x >> 16);
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
				x = (int) y;
				dst[dstIndex++] = (byte) (x >> 24);
				dst[dstIndex++] = (byte) (x >> 16);
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
			}

			return;

		case 8:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 8)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				long y = src[srcIndex++];
				int x = (int) (y >> 32L);
				dst[dstIndex++] = (byte) (x >> 24);
				dst[dstIndex++] = (byte) (x >> 16);
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
				x = (int) y;
				dst[dstIndex++] = (byte) (x >> 24);
				dst[dstIndex++] = (byte) (x >> 16);
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (float[] src, int srcOff, byte[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 4)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				int x = Float.floatToIntBits (src[srcIndex++]);
				dst[dstIndex++] = (byte) (x >> 24);
				dst[dstIndex++] = (byte) (x >> 16);
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (double[] src, int srcOff, byte[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 8:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 8)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				long y = Double.doubleToLongBits (src[srcIndex++]);
				int x = (int) (y >> 32L);
				dst[dstIndex++] = (byte) (x >> 24);
				dst[dstIndex++] = (byte) (x >> 16);
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
				x = (int) y;
				dst[dstIndex++] = (byte) (x >> 24);
				dst[dstIndex++] = (byte) (x >> 16);
				dst[dstIndex++] = (byte) (x >> 8);
				dst[dstIndex++] = (byte) (x);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (short[] src, int srcOff, char[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				dst[dstIndex++] = (char) src[srcIndex++];
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (int[] src, int srcOff, char[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				dst[dstIndex++] = (char) src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 2)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (char) (x >> 16);
				dst[dstIndex++] = (char) (x);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (long[] src, int srcOff, char[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				dst[dstIndex++] = (char) src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 2)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				int x = (int) src[srcIndex++];
				dst[dstIndex++] = (char) (x >> 16);
				dst[dstIndex++] = (char) (x);
			}

			return;

		case 3:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 3)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				long y = src[srcIndex++];
				dst[dstIndex++] = (char) (y >> 32L);
				int x = (int) y;
				dst[dstIndex++] = (char) (x >> 16);
				dst[dstIndex++] = (char) (x);
			}

			return;

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 4)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				long y = src[srcIndex++];
				int x = (int) (y >> 32L);
				dst[dstIndex++] = (char) (x >> 16);
				dst[dstIndex++] = (char) (x);
				x = (int) y;
				dst[dstIndex++] = (char) (x >> 16);
				dst[dstIndex++] = (char) (x);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (float[] src, int srcOff, char[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 2)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				int x = Float.floatToIntBits (src[srcIndex++]);
				dst[dstIndex++] = (char) (x >> 16);
				dst[dstIndex++] = (char) (x);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (double[] src, int srcOff, char[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 4)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				long y = Double.doubleToLongBits (src[srcIndex++]);
				int x = (int) (y >> 32L);
				dst[dstIndex++] = (char) (x >> 16);
				dst[dstIndex++] = (char) (x);
				x = (int) y;
				dst[dstIndex++] = (char) (x >> 16);
				dst[dstIndex++] = (char) (x);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (char[] src, int srcOff, short[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				dst[dstIndex++] = (short) src[srcIndex++];
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (int[] src, int srcOff, short[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				dst[dstIndex++] = (short) src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 2)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				int x = src[srcIndex++];
				dst[dstIndex++] = (short) (x >> 16);
				dst[dstIndex++] = (short) (x);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (long[] src, int srcOff, short[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				dst[dstIndex++] = (short) src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 2)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				int x = (int) src[srcIndex++];
				dst[dstIndex++] = (short) (x >> 16);
				dst[dstIndex++] = (short) (x);
			}

			return;

		case 3:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 3)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				long y = src[srcIndex++];
				dst[dstIndex++] = (short) (y >> 32L);
				int x = (int) y;
				dst[dstIndex++] = (short) (x >> 16);
				dst[dstIndex++] = (short) (x);
			}

			return;

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 4)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				long y = src[srcIndex++];
				int x = (int) (y >> 32L);
				dst[dstIndex++] = (short) (x >> 16);
				dst[dstIndex++] = (short) (x);
				x = (int) y;
				dst[dstIndex++] = (short) (x >> 16);
				dst[dstIndex++] = (short) (x);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (float[] src, int srcOff, short[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 2)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				int x = Float.floatToIntBits (src[srcIndex++]);
				dst[dstIndex++] = (short) (x >> 16);
				dst[dstIndex++] = (short) (x);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (double[] src, int srcOff, short[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 4:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 4)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				long y = Double.doubleToLongBits (src[srcIndex++]);
				int x = (int) (y >> 32L);
				dst[dstIndex++] = (short) (x >> 16);
				dst[dstIndex++] = (short) (x);
				x = (int) y;
				dst[dstIndex++] = (short) (x >> 16);
				dst[dstIndex++] = (short) (x);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (long[] src, int srcOff, int[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				dst[dstIndex++] = (int) src[srcIndex++];
			}

			return;

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 2)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				long y = src[srcIndex++];
				dst[dstIndex++] = (int) (y >> 32L);
				dst[dstIndex++] = (int) (y);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (float[] src, int srcOff, int[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				dst[dstIndex++] = Float.floatToIntBits (src[srcIndex++]);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (double[] src, int srcOff, int[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 2:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + (len * 2)) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				long y = Double.doubleToLongBits (src[srcIndex++]);
				dst[dstIndex++] = (int) (y >> 32L);
				dst[dstIndex++] = (int) (y);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	public static void unpack (double[] src, int srcOff, long[] dst, int dstOff, int len, int pf)
	{
		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		switch (pf)
		{

		case 1:

			if ((len < 0)
				|| (srcOff < 0)
				|| (dstOff < 0)
				|| ((srcOff + len) > src.length)
				|| ((dstOff + len) > dst.length) )
			{
				throw new IndexOutOfBoundsException ("ArrayPack.unpack");
			}

			while (srcIndex < srcLimit)
			{
				dst[dstIndex++] = Double.doubleToLongBits (src[srcIndex++]);
			}

			return;

		default:
			throw new IndexOutOfBoundsException ("ArrayPack.unpack");
		}
	}




	// ----- Array convert routines -----


	// convert (X[] src, int srcOff, Y[] dst, int dstOff, int len)
	//
	// Converts elements from src to dst.  Source elements are converted beginning
	// at src[srcOff], and destination elements are converted beginning at
	// dst[dstOff].  The number of elements converted is len.
	//
	// This function is defined for the following combinations of types:
	//
	// 1.  X is an integral type (byte, char, short, int, or long) and Y is
	// boolean.  In this case, zero is converted to false, and any nonzero value
	// is converted to true.
	//
	// 2.  X is boolean and Y is an integral type.  In this case, false is
	// converted to 0, and true is converted to 1.
	//
	// 3.  X is an integral type, and Y is a floating type.  In this case, the
	// conversion is done by type cast.
	//
	// 4.  X is a floating type, and Y is an integral type.  In this case, the
	// conversion is done by type cast.
	//
	// 5.  X and Y are both floating types.  In this case, the conversion is
	// done by type cast.




	public static void convert (boolean[] src, int srcOff, byte[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = src[srcIndex++] ? ((byte) 1) : ((byte) 0);
		}

		return;
	}




	public static void convert (boolean[] src, int srcOff, char[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = src[srcIndex++] ? ((char) 1) : ((char) 0);
		}

		return;
	}




	public static void convert (boolean[] src, int srcOff, short[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = src[srcIndex++] ? ((short) 1) : ((short) 0);
		}

		return;
	}




	public static void convert (boolean[] src, int srcOff, int[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = src[srcIndex++] ? 1 : 0;
		}

		return;
	}




	public static void convert (boolean[] src, int srcOff, long[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = src[srcIndex++] ? 1L : 0L;
		}

		return;
	}




	public static void convert (byte[] src, int srcOff, boolean[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (src[srcIndex++] != 0);
		}

		return;
	}




	public static void convert (char[] src, int srcOff, boolean[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (src[srcIndex++] != 0);
		}

		return;
	}




	public static void convert (short[] src, int srcOff, boolean[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (src[srcIndex++] != 0);
		}

		return;
	}




	public static void convert (int[] src, int srcOff, boolean[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (src[srcIndex++] != 0);
		}

		return;
	}




	public static void convert (long[] src, int srcOff, boolean[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (src[srcIndex++] != 0L);
		}

		return;
	}




	public static void convert (float[] src, int srcOff, byte[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (byte) src[srcIndex++];
		}

		return;
	}




	public static void convert (float[] src, int srcOff, char[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (char) src[srcIndex++];
		}

		return;
	}




	public static void convert (float[] src, int srcOff, short[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (short) src[srcIndex++];
		}

		return;
	}




	public static void convert (float[] src, int srcOff, int[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (int) src[srcIndex++];
		}

		return;
	}




	public static void convert (float[] src, int srcOff, long[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (long) src[srcIndex++];
		}

		return;
	}




	public static void convert (double[] src, int srcOff, byte[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (byte) src[srcIndex++];
		}

		return;
	}




	public static void convert (double[] src, int srcOff, char[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (char) src[srcIndex++];
		}

		return;
	}




	public static void convert (double[] src, int srcOff, short[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (short) src[srcIndex++];
		}

		return;
	}




	public static void convert (double[] src, int srcOff, int[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (int) src[srcIndex++];
		}

		return;
	}




	public static void convert (double[] src, int srcOff, long[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (long) src[srcIndex++];
		}

		return;
	}




	public static void convert (byte[] src, int srcOff, float[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (float) src[srcIndex++];
		}

		return;
	}




	public static void convert (char[] src, int srcOff, float[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (float) src[srcIndex++];
		}

		return;
	}




	public static void convert (short[] src, int srcOff, float[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (float) src[srcIndex++];
		}

		return;
	}




	public static void convert (int[] src, int srcOff, float[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (float) src[srcIndex++];
		}

		return;
	}




	public static void convert (long[] src, int srcOff, float[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (float) src[srcIndex++];
		}

		return;
	}




	public static void convert (byte[] src, int srcOff, double[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (double) src[srcIndex++];
		}

		return;
	}




	public static void convert (char[] src, int srcOff, double[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (double) src[srcIndex++];
		}

		return;
	}




	public static void convert (short[] src, int srcOff, double[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (double) src[srcIndex++];
		}

		return;
	}




	public static void convert (int[] src, int srcOff, double[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (double) src[srcIndex++];
		}

		return;
	}




	public static void convert (long[] src, int srcOff, double[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (double) src[srcIndex++];
		}

		return;
	}




	public static void convert (float[] src, int srcOff, double[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (double) src[srcIndex++];
		}

		return;
	}




	public static void convert (double[] src, int srcOff, float[] dst, int dstOff, int len)
	{
		if ((len < 0)
			|| (srcOff < 0)
			|| (dstOff < 0)
			|| ((srcOff + len) > src.length)
			|| ((dstOff + len) > dst.length) )
		{
			throw new IndexOutOfBoundsException ("ArrayPack.convert");
		}

		int srcIndex = srcOff;
		int dstIndex = dstOff;

		int srcLimit = srcIndex + len;

		while (srcIndex < srcLimit)
		{
			dst[dstIndex++] = (float) src[srcIndex++];
		}

		return;
	}




}

