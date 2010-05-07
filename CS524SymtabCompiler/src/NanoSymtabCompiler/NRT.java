package nano;

import java.util.ArrayList;
import nano.NST;



public class NRT 
{
	public abstract class NanoMemoryCell
	{
		protected int type = NanoSymbolTable.UNK_TYPE;
		public NanoMemoryCell() {}
		public boolean isInteger() { return (type == NanoSymbolTable.INT_TYPE); }
		public boolean isBoolean() { return (type == NanoSymbolTable.BOOL_TYPE); }
	}
	
	public final class NanoIntegerMemoryCell extends NanoMemoryCell
	{
		private int integerValue;
		public NanoIntegerMemoryCell(int value)
		{	
			type = NanoSymbolTable.INT_TYPE;
			integerValue = value;
		}
		public int getValue() { return integerValue; }
		public void setValue(int value) { integerValue = value; }
	}

	public final class NanoBooleanMemoryCell extends NanoMemoryCell
	{
		private boolean booleanValue;
		public NanoBooleanMemoryCell(boolean value)
		{	
			type = NanoSymbolTable.BOOL_TYPE;
			booleanValue = value;
		}
		public boolean getValue() { return booleanValue; }
		public void setValue(boolean value) { booleanValue = value; }

	}

	private ArrayList memory;

	//Default constructor designed to be big enough for our examples
	//Notice this is a minimalistic class
	//Size isn't even managed as an instance variable and no bounds checking is done
	//Tsk, tsk...
	public NRT()
	{ new NRT(10000); }
	
	public NRT(int size)
	{
		memory = new ArrayList(size);
	}
		
	public void addAt(int value, int index)
	{
		memory.add(index,new NanoIntegerMemoryCell(value));
	}	
		
	public void addAt(boolean value, int index)
	{
		memory.add(index,new NanoBooleanMemoryCell(value));
	}
		
	public int getIntValueAt(int index)
	{
		NanoMemoryCell memCell = (NanoMemoryCell) memory.get(index);
		int result = 0; 
		if (memCell.isInteger()) result = ((NanoIntegerMemoryCell) memCell).getValue();
		else System.out.println("Access error: Memory cell does not contain an integer");
		return result;
	}

	public boolean getBoolValueAt(int index)
	{
		NanoMemoryCell memCell = (NanoMemoryCell) memory.get(index);
		boolean result = false; 
		if (memCell.isBoolean()) result = ((NanoBooleanMemoryCell) memCell).getValue();
		else System.out.println("Access error: Memory cell does not contain a boolean");
		return result;
	}
	
}