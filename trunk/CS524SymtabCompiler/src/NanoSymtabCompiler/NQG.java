package nano;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

public class NQG //Nano Quad Generator
{
	//This class is a factory for quads, handling immediate as well as addressed
	//values and provides the machinery to handle backpatching easily
	//A main function is provided at the end to illustrate "manual" use of the NQG
	//Its use in the actual compiler will be discussed in class
	//The pattern here is to define first the inner class definitions just so 
	//the capabilities of the tool are apparent, but one can, after quickly 
	//becoming familiar with those basic definitions, move down in the file and focus
	//on the public interface of the tool for making and managing the quads.
	//There are public static final int constants which may be useful to the compiler
	//defined along with a static "pretty print" function for them at the end
	
	//Internal variables and structures
	private int currentQuadNum;
	private int nextBackpatchLabelNum;
	private ArrayList quads;
	private Hashtable quadsToBackpatch;
	
	//Default constructor chosen with size sufficient for our examples (as stated below)
	//This is not managed as an instance variabel and has no safety checks!
	public NQG()
	{ new NQG(10000); }
	
	public NQG(int size)	//For our purposes a standard safe choice is 10000
	{
		currentQuadNum = 0;
		nextBackpatchLabelNum = 0;
		quads = new ArrayList(size);
		quadsToBackpatch = new Hashtable(size);
	}
	
	//A base abstract class for quads in general handles common features
	//Two other derived abstract classes that cover the common behavior of all quads are 
	//then defined.	Following the von Neumann convention a quad either produces a result, 
	//i.e., has an effect on data memory OR it ultimately adjusts the instruction pointer, 
	//i.e. has an effect on instruction memory, hence the names. There are admittedly a lot
	//of the concrete subclasses but their specificity greatly facilitates both flexibility
	//in the compiling and precision in the emulators actions (product forthcoming).
	
	//Ahh, yes, and before looking at the quad class structure, consider the utility 
	//class that is the value stored by quadLabel key in the backpatching hashtable
	
	//Many of the public interface methods on these classes may not be of much use
	//in the compiler, other than during the development stage, but accessor (at
	//least "getters") are generally always provided
	
	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//

	public final class BackpatchEntry
	{
		private int quadId = -1; //Once set, this identifies the quad that needs a jump address
								 //entered (associated with the key this entry is stored with)
		private int backpatchAddress = -1; //Once discovered and updated the compiler can
										   //at any moment, but generally at the end of the
										   //compile iterate over all those in the table 
		public BackpatchEntry(int quadId, int backpatchAddress)
		{
			this.quadId = quadId;
			this.backpatchAddress = backpatchAddress;
		}
		public int getQuadId() {return quadId; }
		public int getBackpatchAddress() { return backpatchAddress; }
		public void setBackpatchAddress(int backpatchAddress) 
		{ this.backpatchAddress = backpatchAddress; }
	}

	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public Quad(int quadId, int quadType) -- attributes always stored in any quad
	public abstract class Quad
	{
		protected int quadId = -1;			//This is the auto generated index of the quad 
		protected int quadType = UNK;	//This is an integer indicator of quad type
											//Because of the specificity of quad type to
											//argument details, class identity will be 
											//more likely used in the emulator to select
											//actions than this code; but it is "legacy"
											//to include it and it does facilitate the 
											//easy printout of quad type names
		public Quad(int quadId, int quadType)
		{
			this.quadId = quadId;
			this.quadType = quadType;			
		}
		public int getQuadId() { return quadId; }
		public int getQuadType() { return quadType; }
		public String getQuadTypeName() { return quadTypeNameMap(quadType); }
		public String toString() { return "< " + quadId + " | " +
										  getQuadTypeName() + " | "; }
	}

	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public MemModQuad(..., int resultAddress) 
	// - attributes for memory modifications, in addition to those in Quad 
	// result will go in slot just after those in Quad
	public abstract class MemModQuad extends Quad
	{
		protected int resultAddress = -1;		//Temporary value until assigned
		public MemModQuad(int quadId, int quadType, int resultAddress)
		{
			super(quadId,quadType);
			this.resultAddress = resultAddress;
		}
		public int getResultAddress() { return resultAddress; }
		public String toString() { return (super.toString() + resultAddress + " | "); }
	}
	
	//public InstrModQuad(..., int targetQuadIndex)
	// - attributes for instruction modification, in addition to those in Quad
	// index will go in slot just after those in Quad (where result went for MemModQuad)
	public abstract class InstrModQuad extends Quad
	{
		protected int targetQuadIndex = -1;		//Temporary value until assigned
		protected String backpatchQuadLabel = "";
		public InstrModQuad(int quadId, int quadType, int targetQuadIndex)
		{
			super(quadId,quadType);
			this.targetQuadIndex = targetQuadIndex;
			if (this.targetQuadIndex==-1) backpatchQuadLabel = getNewQuadLabel();
			quadsToBackpatch.put(backpatchQuadLabel, new BackpatchEntry(quadId,-1));
		}
		public int getTargetQuadIndex() { return targetQuadIndex; }
		public String getBackpatchQuadLabel() { return backpatchQuadLabel; }
		public void setTargetQuadIndex(int index) { targetQuadIndex = index; }
		public String toString() { return (super.toString() + targetQuadIndex + " | "); }
	}

	public final class Start extends Quad
	{
		private int targetQuadIndex = -1; //Temporary value until assigned
										  //Typically by compiler as last instruction
		public Start(int quadId, int targetQuadIndex)
		{
			super(quadId,NQG.START);
			this.targetQuadIndex = targetQuadIndex;
		}
		public int getTargetQuadIndex() { return targetQuadIndex; }
		public String toString() { return (super.toString() + targetQuadIndex + 
											" | - | - | - >"); }
	}
	
	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public MulBothImmediate
	//	(..., int valueA, int valueB) 
	final class MulBothImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int valueB = -1;
		public MulBothImmediate
				(int quadId, int resultAddress, int valueA, int valueB) 
		{
			super(quadId,NQG.MUL,resultAddress);
			this.valueA = valueA;
			this.valueB = valueB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | #" + valueB + " >"); }
	}

	//public  MulLeftImmediate
	//		(..., int valueA, int addressB) 
	final class MulLeftImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int addressB = -1;
		public  MulLeftImmediate
				(int quadId, int resultAddress, int valueA, int addressB) 
		{
			super(quadId,NQG.MUL,resultAddress);
			this.valueA = valueA;
			this.addressB = addressB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | " + addressB + " >"); }
	}

	//public MulRightImmediate
	//		(..., int addressA, int valueB) 
	final class MulRightImmediate extends MemModQuad
	{
		private int addressA = -1;
		private int valueB = -1;
		public MulRightImmediate
				(int quadId, int resultAddress, int addressA, int valueB) 
		{
			super(quadId,NQG.MUL,resultAddress);
			this.addressA = addressA;
			this.valueB = valueB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | #" + valueB + " >"); }
	}

	//public MulRegular
	//		(..., int addressA, int addressB) 
	final class MulRegular extends MemModQuad
	{
		private int addressA = -1;
		private int addressB = -1;
		public MulRegular
				(int quadId, int resultAddress, int addressA, int addressB) 
		{
			super(quadId,NQG.MUL,resultAddress);
			this.addressA = addressA;
			this.addressB = addressB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | " + addressB + " >"); }
	}

	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public DivBothImmediate
	//		(..., int valueA, int valueB) 
	final class DivBothImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int valueB = -1;
		public DivBothImmediate
				(int quadId, int resultAddress, int valueA, int valueB) 
		{
			super(quadId,NQG.DIV,resultAddress);
			this.valueA = valueA;
			this.valueB = valueB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | #" + valueB + " >"); }
	}

	//public  DivLeftImmediate
	//		(..., int valueA, int addressB) 
	final class DivLeftImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int addressB = -1;
		public  DivLeftImmediate
				(int quadId, int resultAddress, int valueA, int addressB) 
		{
			super(quadId,NQG.DIV,resultAddress);
			this.valueA = valueA;
			this.addressB = addressB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | " + addressB + " >"); }
	}

	//public DivRightImmediate
	//		(..., int addressA, int valueB) 
	final class DivRightImmediate extends MemModQuad
	{
		private int addressA = -1;
		private int valueB = -1;
		public DivRightImmediate
				(int quadId, int resultAddress, int addressA, int valueB) 
		{
			super(quadId,NQG.DIV,resultAddress);
			this.addressA = addressA;
			this.valueB = valueB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | #" + valueB + " >"); }
	}

	//public DivRegular
	//		(..., int addressA, int addressB) 
	final class DivRegular extends MemModQuad
	{
		private int addressA = -1;
		private int addressB = -1;
		public DivRegular
				(int quadId, int resultAddress, int addressA, int addressB) 
		{
			super(quadId,NQG.DIV,resultAddress);
			this.addressA = addressA;
			this.addressB = addressB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | " + addressB + " >"); }
	}

	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public AndBothImmediate
	//		(..., boolean valueA, boolean valueB) 
	final class AndBothImmediate extends MemModQuad
	{
		private boolean valueA = false;
		private boolean valueB = false;
		public AndBothImmediate
				(int quadId, int resultAddress, boolean valueA, boolean valueB) 
		{
			super(quadId,NQG.AND,resultAddress);
			this.valueA = valueA;
			this.valueB = valueB;
		}
		public boolean getActualValueA() { return valueA; }
		public boolean getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | #" + valueB + " >"); }
	}

	//public AndLeftImmediate
	//		(..., boolean valueA, int addressB) 
	final class AndLeftImmediate extends MemModQuad
	{
		private boolean valueA = false;
		private int addressB = -1;
		public AndLeftImmediate
				(int quadId, int resultAddress, boolean valueA, int addressB) 
		{
			super(quadId,NQG.AND,resultAddress);
			this.valueA = valueA;
			this.addressB = addressB;
		}
		public boolean getActualValueA() { return valueA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | " + addressB + " >"); }
	}

	//public AndRightImmediate
	//		(..., int addressA, boolean valueB) 
	final class AndRightImmediate extends MemModQuad
	{
		private int addressA = -1;
		private boolean valueB = false;
		public AndRightImmediate
				(int quadId, int resultAddress, int addressA, boolean valueB) 
		{
			super(quadId,NQG.AND,resultAddress);
			this.addressA = addressA;
			this.valueB = valueB;
		}
		public int getActualAddressA() { return addressA; }
		public boolean getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | #" + valueB + " >"); }
	}

	//public AndRegular
	//		(..., int addressA, int addressB) 
	final class AndRegular extends MemModQuad
	{
		private int addressA = -1;
		private int addressB = -1;
		public AndRegular
				(int quadId, int resultAddress, int addressA, int addressB) 
		{
			super(quadId,NQG.AND,resultAddress);
			this.addressA = addressA;
			this.addressB = addressB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | " + addressB + " >"); }
	}

	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public AddBothImmediate
	//		(..., int valueA, int valueB) 
	final class AddBothImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int valueB = -1;
		public AddBothImmediate
				(int quadId, int resultAddress, int valueA, int valueB) 
		{
			super(quadId,NQG.ADD,resultAddress);
			this.valueA = valueA;
			this.valueB = valueB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | #" + valueB + " >"); }
	}

	//public AddLeftImmediate
	//		(..., int valueA, int addressB) 
	final class AddLeftImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int addressB = -1;
		public AddLeftImmediate
				(int quadId, int resultAddress, int valueA, int addressB) 
		{
			super(quadId,NQG.ADD,resultAddress);
			this.valueA = valueA;
			this.addressB = addressB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | " + addressB + " >"); }
	}
	
	//public AddRightImmediate
	//		(..., int addressA, int valueB) 
	final class AddRightImmediate extends MemModQuad
	{
		private int addressA = -1;
		private int valueB = -1;
		public AddRightImmediate
				(int quadId, int resultAddress, int addressA, int valueB) 
		{
			super(quadId,NQG.ADD,resultAddress);
			this.addressA = addressA;
			this.valueB = valueB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | #" + valueB + " >"); }
	}

	//public AddRegular
	//		(..., int addressA, int addressB) 
	final class AddRegular extends MemModQuad
	{
		private int addressA = -1;
		private int addressB = -1;
		public AddRegular
				(int quadId, int resultAddress, int addressA, int addressB) 
		{
			super(quadId,NQG.ADD,resultAddress);
			this.addressA = addressA;
			this.addressB = addressB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | " + addressB + " >"); }
	}
	
	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public SubBothImmediate
	//		(..., int valueA, int valueB) 
	final class SubBothImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int valueB = -1;
		public SubBothImmediate
				(int quadId, int resultAddress, int valueA, int valueB) 
		{
			super(quadId,NQG.SUB,resultAddress);
			this.valueA = valueA;
			this.valueB = valueB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | #" + valueB + " >"); }
	}
	
	//public SubLeftImmediate
	//		(..., int valueA, int addressB) 
	final class SubLeftImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int addressB = -1;
		public SubLeftImmediate
				(int quadId, int resultAddress, int valueA, int addressB) 
		{
			super(quadId,NQG.SUB,resultAddress);
			this.valueA = valueA;
			this.addressB = addressB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | " + addressB + " >"); }
	}

	//public SubRightImmediate
	//		(..., int addressA, int valueB) 
	final class SubRightImmediate extends MemModQuad
	{
		private int addressA = -1;
		private int valueB = -1;
		public SubRightImmediate
				(int quadId, int resultAddress, int addressA, int valueB) 
		{
			super(quadId,NQG.SUB,resultAddress);
			this.addressA = addressA;
			this.valueB = valueB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | #" + valueB + " >"); }
	}

	//public SubRegular
	//		(..., int addressA, int addressB)
	final class SubRegular extends MemModQuad
	{
		private int addressA = -1;
		private int addressB = -1;
		public SubRegular
				(int quadId, int resultAddress, int addressA, int addressB) 
		{
			super(quadId,NQG.SUB,resultAddress);
			this.addressA = addressA;
			this.addressB = addressB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | " + addressB + " >"); }
	}
	
	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public OrBothImmediate
	//		(..., boolean valueA, boolean valueB)
	final class OrBothImmediate extends MemModQuad
	{
		private boolean valueA = false;
		private boolean valueB = false;
		public OrBothImmediate
				(int quadId, int resultAddress, boolean valueA, boolean valueB) 
		{
			super(quadId,NQG.OR,resultAddress);
			this.valueA = valueA;
			this.valueB = valueB;
		}
		public boolean getActualValueA() { return valueA; }
		public boolean getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | #" + valueB + " >"); }
	}

	//public OrLeftImmediate
	//		(..., boolean valueA, int addressB) 
	final class OrLeftImmediate extends MemModQuad
	{
		private boolean valueA = false;
		private int addressB = -1;
		public OrLeftImmediate
				(int quadId, int resultAddress, boolean valueA, int addressB) 
		{
			super(quadId,NQG.OR,resultAddress);
			this.valueA = valueA;
			this.addressB = addressB;
		}
		public boolean getActualValueA() { return valueA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | " + addressB + " >"); }
	}

	//public OrRightImmediate
	//		(..., int addressA, boolean valueB) 
	final class OrRightImmediate extends MemModQuad
	{
		private int addressA = -1;
		private boolean valueB = false;
		public OrRightImmediate
				(int quadId, int resultAddress, int addressA, boolean valueB) 
		{
			super(quadId,NQG.OR,resultAddress);
			this.addressA = addressA;
			this.valueB = valueB;
		}
		public int getActualAddressA() { return addressA; }
		public boolean getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | #" + valueB + " >"); }
	}

	//public OrRegular
	//		(..., int addressA, int addressB) 
	final class OrRegular extends MemModQuad
	{
		private int addressA = -1;
		private int addressB = -1;
		public OrRegular
				(int quadId, int resultAddress, int addressA, int addressB) 
		{
			super(quadId,NQG.OR,resultAddress);
			this.addressA = addressA;
			this.addressB = addressB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | " + addressB + " >"); }
	}

	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public NegImmediate
	//	(..., int value)
	final class NegImmediate extends MemModQuad
	{
		private int value = -1;
		public NegImmediate
			(int quadId, int resultAddress, int value)
		{
			super(quadId,NQG.NEG,resultAddress);
			this.value = value;
		}
		public int getActualValue() { return value; }
		public String toString()
		{ return (super.toString() + " | - | #" + value + " >" ); }
	}

	//public NegRegular
	//		(..., int address)
	final class NegRegular extends MemModQuad
	{
		private int address = -1;
		public NegRegular
			(int quadId, int resultAddress, int address)
		{
			super(quadId,NQG.NEG,resultAddress);
			this.address= address;
		}
		public int getActualAddress() { return address; }
		public String toString()
		{ return (super.toString() + " | - | " + address + " >" ); }
	}
		
	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public NotImmediate
	//		(..., boolean value)
	final class NotImmediate extends MemModQuad
	{
		private boolean value = false;
		public NotImmediate
			(int quadId, int resultAddress, boolean value)
		{
			super(quadId,NQG.NOT,resultAddress);
			this.value = value;
		}
		public boolean getActualValue() { return value; }
		public String toString()
		{ return (super.toString() + " | - #" + value + " >" );}
	}

	//public NotRegular
	//		(..., int address)
	final class NotRegular extends MemModQuad
	{
		private int address = -1;
		public NotRegular
			(int quadId, int resultAddress, int address)
		{
			super(quadId,NQG.NOT,resultAddress);
			this.address= address;
		}
		public int getActualAddress() { return address; }
		public String toString()
		{ return (super.toString() + " | - | " + address + " >" );}
	}

	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public RelopEqualsBothImmediate
	//		(..., int valueA, int valueB) 
	final class RelopEqualsBothImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int valueB = -1;
		public RelopEqualsBothImmediate
					(int quadId, int resultAddress, int valueA, int valueB) 
		{
				super(quadId,NQG.RLEQ,resultAddress);
				this.valueA = valueA;
				this.valueB = valueB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | #" + valueB + " >"); }
	}	

	//public RelopEqualsLeftImmediate
	//		(..., int valueA, int addressB) 
	final class RelopEqualsLeftImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int addressB = -1;
		public RelopEqualsLeftImmediate
					(int quadId, int resultAddress, int valueA, int addressB) 
		{
				super(quadId,NQG.RLEQ,resultAddress);
				this.valueA = valueA;
				this.addressB = addressB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | " + addressB + " >"); }
	}	

	//public RelopEqualsRightImmediate
	//		(..., int addressA, int valueB) 
	final class RelopEqualsRightImmediate extends MemModQuad
	{
		private int addressA = -1;
		private int valueB = -1;
		public RelopEqualsRightImmediate
					(int quadId, int resultAddress, int addressA, int valueB) 
		{
				super(quadId,NQG.RLEQ,resultAddress);
				this.addressA = addressA;
				this.valueB = valueB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | #" + valueB + " >"); }
	}	

	//public RelopEqualsRegular
	//		(..., int addressA, int addressB) 
	final class RelopEqualsRegular extends MemModQuad
	{
		private int addressA = -1;
		private int addressB = -1;
		public RelopEqualsRegular
					(int quadId, int resultAddress, int addressA, int addressB) 
		{
				super(quadId,NQG.RLEQ,resultAddress);
				this.addressA = addressA;
				this.addressB = addressB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | " + addressB + " >"); }
	}	

	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public RelopNotEqualsBothImmediate
	//		(..., int valueA, int valueB) 
	final class RelopNotEqualsBothImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int valueB = -1;
		public RelopNotEqualsBothImmediate
					(int quadId, int resultAddress, int valueA, int valueB) 
		{
				super(quadId,NQG.RLNTEQ,resultAddress);
				this.valueA = valueA;
				this.valueB = valueB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | #" + valueB + " >"); }
	}	

	//public RelopNotEqualsLeftImmediate
	//		(..., int valueA, int addressB)
	final class RelopNotEqualsLeftImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int addressB = -1;
		public RelopNotEqualsLeftImmediate
					(int quadId, int resultAddress, int valueA, int addressB) 
		{
				super(quadId,NQG.RLNTEQ,resultAddress);
				this.valueA = valueA;
				this.addressB = addressB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | " + addressB + " >"); }
	}	

	//public RelopNotEqualsRightImmediate
	//		(..., int addressA, int valueB)
	final class RelopNotEqualsRightImmediate extends MemModQuad
	{
		private int addressA = -1;
		private int valueB = -1;
		public RelopNotEqualsRightImmediate
					(int quadId, int resultAddress, int addressA, int valueB) 
		{
				super(quadId,NQG.RLNTEQ,resultAddress);
				this.addressA = addressA;
				this.valueB = valueB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | #" + valueB + " >"); }
	}	

	//public RelopNotEqualsRegular
	//		(..., int addressA, int addressB) 
	final class RelopNotEqualsRegular extends MemModQuad
	{
		private int addressA = -1;
		private int addressB = -1;
		public RelopNotEqualsRegular
					(int quadId, int resultAddress, int addressA, int addressB) 
		{
				super(quadId,NQG.RLNTEQ,resultAddress);
				this.addressA = addressA;
				this.addressB = addressB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | " + addressB + " >"); }
	}	

	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public RelopLessThanBothImmediate
	//	(..., int valueA, int valueB) 
	final class RelopLessThanBothImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int valueB = -1;
		public RelopLessThanBothImmediate
					(int quadId, int resultAddress, int valueA, int valueB) 
		{
				super(quadId,NQG.RLST,resultAddress);
				this.valueA = valueA;
				this.valueB = valueB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | #" + valueB + " >"); }
	}	

	//public RelopLessThanLeftImmediate
	//		(..., int valueA, int addressB) 
	final class RelopLessThanLeftImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int addressB = -1;
		public RelopLessThanLeftImmediate
					(int quadId, int resultAddress, int valueA, int addressB) 
		{
				super(quadId,NQG.RLST,resultAddress);
				this.valueA = valueA;
				this.addressB = addressB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | " + addressB + " >"); }
	}	

	//public RelopLessThanRightImmediate
	//		(..., int addressA, int valueB) 
	final class RelopLessThanRightImmediate extends MemModQuad
	{
		private int addressA = -1;
		private int valueB = -1;
		public RelopLessThanRightImmediate
					(int quadId, int resultAddress, int addressA, int valueB) 
		{
				super(quadId,NQG.RLST,resultAddress);
				this.addressA = addressA;
				this.valueB = valueB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | #" + valueB + " >"); }
	}	

	//public RelopLessThanRegular
	//		(..., int addressA, int addressB) 
	final class RelopLessThanRegular extends MemModQuad
	{
		private int addressA = -1;
		private int addressB = -1;
		public RelopLessThanRegular
					(int quadId, int resultAddress, int addressA, int addressB) 
		{
				super(quadId,NQG.RLST,resultAddress);
				this.addressA = addressA;
				this.addressB = addressB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | " + addressB + " >"); }
	}	

	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public RelopLessThanEqualsBothImmediate
	//		(..., int valueA, int valueB) 
	final class RelopLessThanEqualsBothImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int valueB = -1;
		public RelopLessThanEqualsBothImmediate
					(int quadId, int resultAddress, int valueA, int valueB) 
		{
				super(quadId,NQG.RLSTEQ,resultAddress);
				this.valueA = valueA;
				this.valueB = valueB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | #" + valueB + " >"); }
	}	

	//public RelopLessThanEqualsLeftImmediate
	//		(..., int valueA, int addressB) 
	final class RelopLessThanEqualsLeftImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int addressB = -1;
		public RelopLessThanEqualsLeftImmediate
					(int quadId, int resultAddress, int valueA, int addressB) 
		{
				super(quadId,NQG.RLST,resultAddress);
				this.valueA = valueA;
				this.addressB = addressB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | " + addressB + " >"); }
	}	

	//public RelopLessThanEqualsRightImmediate
	//		(..., int addressA, int valueB) 
	final class RelopLessThanEqualsRightImmediate extends MemModQuad
	{
		private int addressA = -1;
		private int valueB = -1;
		public RelopLessThanEqualsRightImmediate
					(int quadId, int resultAddress, int addressA, int valueB) 
		{
				super(quadId,NQG.RLST,resultAddress);
				this.addressA = addressA;
				this.valueB = valueB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | #" + valueB + " >"); }
	}	

	//public RelopLessThanEqualsRegular
	//		(..., int addressA, int addressB) 
	final class RelopLessThanEqualsRegular extends MemModQuad
	{
		private int addressA = -1;
		private int addressB = -1;
		public RelopLessThanEqualsRegular
					(int quadId, int resultAddress, int addressA, int addressB) 
		{
				super(quadId,NQG.RLST,resultAddress);
				this.addressA = addressA;
				this.addressB = addressB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | " + addressB + " >"); }
	}	

	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public RelopGreaterThanBothImmediate
	//		(..., int valueA, int valueB) 
	final class RelopGreaterThanBothImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int valueB = -1;
		public RelopGreaterThanBothImmediate
					(int quadId, int resultAddress, int valueA, int valueB) 
		{
				super(quadId,NQG.RLGT,resultAddress);
				this.valueA = valueA;
				this.valueB = valueB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | #" + valueB + " >"); }
	}
	
	//public RelopGreaterThanLeftImmediate
	//		(..., int valueA, int addressB) 
	final class RelopGreaterThanLeftImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int addressB = -1;
		public RelopGreaterThanLeftImmediate
					(int quadId, int resultAddress, int valueA, int addressB) 
		{
				super(quadId,NQG.RLGT,resultAddress);
				this.valueA = valueA;
				this.addressB = addressB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | " + addressB + " >"); }
	}	

	//public RelopGreaterThanRightImmediate
	//		(..., int addressA, int valueB) 
	final class RelopGreaterThanRightImmediate extends MemModQuad
	{
		private int addressA = -1;
		private int valueB = -1;
		public RelopGreaterThanRightImmediate
					(int quadId, int resultAddress, int addressA, int valueB) 
		{
				super(quadId,NQG.RLGT,resultAddress);
				this.addressA = addressA;
				this.valueB = valueB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | #" + valueB + " >"); }
	}	
	
	//		public RelopGreaterThanRegular
	//			(..., int addressA, int addressB)
	final class RelopGreaterThanRegular extends MemModQuad
	{
		private int addressA = -1;
		private int addressB = -1;
		public RelopGreaterThanRegular
					(int quadId, int resultAddress, int addressA, int addressB) 
		{
				super(quadId,NQG.RLGT,resultAddress);
				this.addressA = addressA;
				this.addressB = addressB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | " + addressB + " >"); }
	}	

	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//	public RelopGreaterThanEqualsBothImmediate
	//		(..., int valueA, int valueB)	
	final class RelopGreaterThanEqualsBothImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int valueB = -1;
		public RelopGreaterThanEqualsBothImmediate
					(int quadId, int resultAddress, int valueA, int valueB) 
		{
				super(quadId,NQG.RLGTEQ,resultAddress);
				this.valueA = valueA;
				this.valueB = valueB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | #" + valueB + " >"); }
	}	

	//		public RelopGreaterThanEqualsLeftImmediate
	//			(..., int valueA, int addressB)
	final class RelopGreaterThanEqualsLeftImmediate extends MemModQuad
	{
		private int valueA = -1;
		private int addressB = -1;
		public RelopGreaterThanEqualsLeftImmediate
					(int quadId, int resultAddress, int valueA, int addressB) 
		{
				super(quadId,NQG.RLGTEQ,resultAddress);
				this.valueA = valueA;
				this.addressB = addressB;
		}
		public int getActualValueA() { return valueA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " #" + valueA + " | " + addressB + " >"); }
	}	

	//		public RelopGreaterThanEqualsRightImmediate
	//			(..., int addressA, int valueB) 
	final class RelopGreaterThanEqualsRightImmediate extends MemModQuad
	{
		private int addressA = -1;
		private int valueB = -1;
		public RelopGreaterThanEqualsRightImmediate
					(int quadId, int resultAddress, int addressA, int valueB) 
		{
				super(quadId,NQG.RLGTEQ,resultAddress);
				this.addressA = addressA;
				this.valueB = valueB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualValueB() { return valueB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | #" + valueB + " >"); }
	}	
	
	//		public RelopGreaterThanEqualsRegular
	//				(..., int addressA, int addressB)
	final class RelopGreaterThanEqualsRegular extends MemModQuad
	{
		private int addressA = -1;
		private int addressB = -1;
		public RelopGreaterThanEqualsRegular
					(int quadId, int resultAddress, int addressA, int addressB) 
		{
				super(quadId,NQG.RLGTEQ,resultAddress);
				this.addressA = addressA;
				this.addressB = addressB;
		}
		public int getActualAddressA() { return addressA; }
		public int getActualAddressB() { return addressB; }
		public String toString()
		{ return (super.toString() + " " + addressA + " | " + addressB + " >"); }
	}	

	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public AssignImmediate (..., int value)
	final class AssignImmediateInteger extends MemModQuad
	{
		private int value = -1;
		public AssignImmediateInteger(int quadId, int resultAddress, int value)
		{
			super(quadId,NQG.ASGN,resultAddress);
			this.value = value;
		}
		public int getValue() { return value; }
		public String toString()
		{ return (super.toString() + " |  -  | " + value + " >"); }
	}

	//public AssignImmediate (..., boolean value)
	final class AssignImmediateBoolean extends MemModQuad
	{
		private boolean value = false;
		public AssignImmediateBoolean (int quadId, int resultAddress, boolean value)
		{
			super(quadId,NQG.ASGN,resultAddress);
			this.value = value;
		}
		public boolean getValue() { return value; }
		public String toString()
		{ return (super.toString() + " |  -  | " + value + " >"); }
	}
		
	//public AssignRegular (..., int address)
	final class AssignRegular extends MemModQuad
	{
		private int address = -1;
		public AssignRegular (int quadId, int resultAddress, int address)
		{
			super(quadId,NQG.ASGN,resultAddress);
			this.address = address;
		}
		public int getAddress() { return address; }
		public String toString()
		{ return (super.toString() + " |  -  | " + address + " >"); }
	}
		
	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public UnconditionalJump(...)
	final class UnconditionalJump extends InstrModQuad
	{
		//This class adds no behavior to InstrModQuad
		//since all that's required for an unconditional jump is
		//quadId, quadType and the targetQuadAddress which are part of
		//the parent class. For symmetry of interface this subclass is provided
		private String specializer = null;  //From the upcoming film "Being Pedantic"
		public UnconditionalJump(int quadId, int targetQuadIndex)
		{
			super(quadId,NQG.DOJMP,targetQuadIndex);
			specializer = "This makes this class unique, just like all its other instantiations";
		}
		public String getSpecializer() { return specializer; }
		public String toString()
		{ return (super.toString() + " | - | - >"); }
	}

	//public IfTrueImmediate(..., boolean value)
	final class IfTrueImmediate extends InstrModQuad
	{
		private boolean value = false;
		public IfTrueImmediate(int quadId, int targetQuadIndex, boolean value)
		{
			super(quadId,NQG.IFTRU,targetQuadIndex);
			this.value = value; 
		}
		public boolean getValue() { return value; }
		public String toString()
		{ return (super.toString() + " | - | " + value + " >"); }
	}
	
	//public IfTrueRegular(..., int address)
	final class IfTrueRegular extends InstrModQuad
	{
		private int address = -1;
		public IfTrueRegular(int quadId, int targetQuadIndex, int address)
		{
			super(quadId,NQG.IFTRU,targetQuadIndex);
			this.address = address; 
		}
		public int getAddress() { return address; }
		public String toString()
		{ return (super.toString() + " | - | " + address + " >"); }
	}
	
	//public IfFalseImmediate(..., boolean value)
	final class IfFalseImmediate extends InstrModQuad
	{
		private boolean value = false;
		public IfFalseImmediate(int quadId, int targetQuadIndex, boolean value)
		{
			super(quadId,NQG.IFFAL,targetQuadIndex);
			this.value = value; 
		}
		public boolean getValue() { return value; }
		public String toString()
		{ return (super.toString() + " | - | " + value + " >"); }
	}

	//public IfFalseRegular(..., int address)
	final class IfFalseRegular extends InstrModQuad
	{
		private int address = -1;
		public IfFalseRegular(int quadId, int targetQuadIndex, int address)
		{
			super(quadId,NQG.IFFAL,targetQuadIndex);
			this.address = address; 
		}
		public int getAddress() { return address; }
		public String toString()
		{ return (super.toString() + " | - | " + address + " >"); }
	}
	
	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public Call(..., int numEltsToPopAtEnd)
	final class Call extends InstrModQuad
	{
		private int numEltsToPopAtEnd = -1;
		public Call(int quadId, int targetQuadIndex, int numEltsToPopAtEnd)
		{
			super(quadId,NQG.CALL,targetQuadIndex);
			this.numEltsToPopAtEnd = numEltsToPopAtEnd;
		}
		public int getNumEltsToPopAtEnd() { return numEltsToPopAtEnd; }
		public String toString()
		{ return (super.toString() + " | - | " + numEltsToPopAtEnd + " >"); }
	}
	
	//public Param(..., int parameterAddress)
	final class Param extends MemModQuad //Notice this inheritance (NOT InstrModQuad)
	{
		private int parameterAddress = -1;
		//Note resultAddress is top of the stack, determined between
		//the symbol table and the compiler, a special requirement we
		//have not encountered before now; and literally parameterAddress
		//is like a pointer it is an integer that is an index into runtime
		//memory to refer to an actual value stored after that one-level indirection
		public Param(int quadId, int resultAddress, int parameterAddress)
		{
			super(quadId, NQG.PARM, resultAddress);
			this.parameterAddress = parameterAddress;
		}
		public int getParameterAddress() { return parameterAddress; }
		public String toString() 
		{ return (super.toString() + " | - | " + parameterAddress + " >"); }
	}
	
	//public Prelude(..., int newStackTopOffset)
	final class Prelude extends MemModQuad //Again notice this inheritance (NOT InstrModQuad)
	{
		private int newStackTopOffset = -1;
		//Note "resultAddress" is ignored; pass in -1 (the alternative
		//is yet another anomalous subclass of the Quad class tree)
		//The purpose of this quad is simply to move to runtime memory stacktop pointer
		//This value is compute once the number of parameters is known, just before a call
		public Prelude(int quadId, int newStackTopOffset)
		{
			super(quadId, NQG.PREL, -1);
			this.newStackTopOffset = newStackTopOffset;
		}
		public int getNewStackTopOffset() { return newStackTopOffset; }
		public String toString() { return "< " + quadId + " | " +
			  getQuadTypeName() + " | - | - | " + newStackTopOffset + " >"; }
	}
	
	//public Return(..., int newStackTopOffset)
	final class Return extends InstrModQuad 
	{
		private int newStackTopOffset = -1;
		//Note targetQuadIndex is used to move control back to the call location
		//The newStackTopOffset is used simply to move to runtime memory stacktop pointer back
		//The compiler has to remember the original quad address to return to 
		//and put it in this quad
		public Return(int quadId, int targetQuadIndex, int newStackTopOffset)
		{
			super(quadId, NQG.RET, targetQuadIndex);
			this.newStackTopOffset = newStackTopOffset;
		}
		public int getNewStackTopOffset() { return newStackTopOffset; }
		public String toString() 
		{ return (super.toString() + " | - | " + newStackTopOffset + " >"); }
	}

	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	//public Print(..., String formatString)
	final class Print extends MemModQuad
	{
		//Only the READ quad actually modifies memory but it does READ from that address
		//for symmetry the PRINT quad extends the MemModQuad as well
		private String formatString = ""; //Preinitialized as a boundary condition
		public Print(int quadId, int resultAddress, String formatString)
		{
			super(quadId,NQG.PRNT,resultAddress);
			this.formatString = formatString;
		}
		public String getFormatString() { return formatString; }
		public String toString()
		{ return (super.toString() + " | " + formatString + " | - >"); }
	}
	
	//public Read(..., String formatString)
	final class Read extends MemModQuad
	{
		private String formatString = ""; //Preinitialized as a boundary condition
		public Read(int quadId, int resultAddress, String formatString)
		{
			super(quadId,NQG.READ,resultAddress);
			this.formatString = formatString;
			//It is only at runtime of the quad execution emulator that the "value"
			//of the string is checked and decisions are thereby made about how
			//to modify memory or simply to print the string value itself, since
			//our "machine" has no model for string storage (a serious limitation)
		}
		public String getFormatString() { return formatString; }
		public String toString()
		{ return (super.toString() + " | " + formatString + " | - >"); }
	}

	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	/*
	 * You were warned! It took a while if you read through and digested the details
	 * of all those quad subclasses. The the whole thing is made simpler by the 
	 * following factory interfaces, which is really the use indended for the quad generator
	 * These abstract away a lot of the extra information that quads have that the
	 * generator handles for you 
	 */
	
	public int getCurrentQuadNumber() { return currentQuadNum; }
	
	public ArrayList getQuadList() { return quads; }
	
	public void addQuad(Quad q) { quads.add(currentQuadNum++,q); }
	
	public MemModQuad makeMulBothImmediate(int resultAddress, int value1, int value2)
	{ return new MulBothImmediate(currentQuadNum,resultAddress,value1,value2); }

	public MemModQuad makeMulLeftImmediate(int resultAddress, int value, int address)
	{ return new MulLeftImmediate(currentQuadNum,resultAddress,value,address); }

	public MemModQuad makeMulRightImmediate(int resultAddress, int address, int value)
	{ return new MulRightImmediate(currentQuadNum,resultAddress,address,value); }

	public MemModQuad makeMulRegular(int resultAddress, int address1, int address2)
	{ return new MulRegular(currentQuadNum,resultAddress,address1,address2); }

	public MemModQuad makeDivBothImmediate(int resultAddress, int value1, int value2)
	{ return new DivBothImmediate(currentQuadNum,resultAddress,value1,value2); }

	public MemModQuad makeDivLeftImmediate(int resultAddress, int value, int address)
	{ return new DivLeftImmediate(currentQuadNum,resultAddress,value,address); }

	public MemModQuad makeDivRightImmediate(int resultAddress, int address, int value)
	{ return new DivRightImmediate(currentQuadNum,resultAddress,address,value); }

	public MemModQuad makeDivRegular(int resultAddress, int address1, int address2)
	{ return new DivRegular(currentQuadNum,resultAddress,address1,address2); }

	public MemModQuad makeAndBothImmediate(int resultAddress, boolean value1, boolean value2)
	{ return new AndBothImmediate(currentQuadNum,resultAddress,value1,value2); }

	public MemModQuad makeAndLeftImmediate(int resultAddress, boolean value, int address)
	{ return new AndLeftImmediate(currentQuadNum,resultAddress,value,address); }

	public MemModQuad makeAndRightImmediate(int resultAddress, int address, boolean  value)
	{ return new AndRightImmediate(currentQuadNum,resultAddress,address,value); }

	public MemModQuad makeAndRegular(int resultAddress, int address1, int address2)
	{ return new AndRegular(currentQuadNum,resultAddress,address1,address2); }

	public MemModQuad makeAddBothImmediate(int resultAddress, int value1, int value2)
	{ return new AddBothImmediate(currentQuadNum,resultAddress,value1,value2); }

	public MemModQuad makeAddLeftImmediate(int resultAddress, int value, int address)
	{ return new AddLeftImmediate(currentQuadNum,resultAddress,value,address); }

	public MemModQuad makeAddRightImmediate(int resultAddress, int address, int value)
	{ return new AddRightImmediate(currentQuadNum,resultAddress,address,value); }

	public MemModQuad makeAddRegular(int resultAddress, int address1, int address2)
	{ return new AddRegular(currentQuadNum,resultAddress,address1,address2); }

	public MemModQuad makeSubBothImmediate(int resultAddress, int value1, int value2)
	{ return new SubBothImmediate(currentQuadNum,resultAddress,value1,value2); }

	public MemModQuad makeSubLeftImmediate(int resultAddress, int value, int address)
	{ return new SubLeftImmediate(currentQuadNum,resultAddress,value,address); }

	public MemModQuad makeSubRightImmediate(int resultAddress, int address, int value)
	{ return new SubRightImmediate(currentQuadNum,resultAddress,address,value); }

	public MemModQuad makeSubRegular(int resultAddress, int address1, int address2)
	{ return new SubRegular(currentQuadNum,resultAddress,address1,address2); }

	public MemModQuad makeOrBothImmediate(int resultAddress, boolean value1, boolean value2)
	{ return new OrBothImmediate(currentQuadNum,resultAddress,value1,value2); }

	public MemModQuad makeOrLeftImmediate(int resultAddress, boolean value, int address)
	{ return new OrLeftImmediate(currentQuadNum,resultAddress,value,address); }

	public MemModQuad makeOrRightImmediate(int resultAddress, int address, boolean  value)
	{ return new OrRightImmediate(currentQuadNum,resultAddress,address,value); }

	public MemModQuad makeOrRegular(int resultAddress, int address1, int address2)
	{ return new OrRegular(currentQuadNum,resultAddress,address1,address2); }

	public MemModQuad makeRelopEqualsBothImmediate(int resultAddress, int value1, int value2)
	{ return new RelopEqualsBothImmediate(currentQuadNum,resultAddress,value1,value2); }

	public MemModQuad makeRelopEqualsLeftImmediate
		(int resultAddress, int value, int address)
	{ return new RelopEqualsLeftImmediate
			(currentQuadNum,resultAddress,value,address); }

	public MemModQuad makeRelopEqualsRightImmediate
		(int resultAddress, int address, int value)
	{ return new RelopEqualsRightImmediate
			(currentQuadNum,resultAddress,address,value); }

	public MemModQuad makeRelopEqualsRegular
		(int resultAddress, int address1, int address2)
	{ return new RelopEqualsRegular
			(currentQuadNum,resultAddress,address1,address2); }

	public MemModQuad makeRelopNotEqualsBothImmediate
		(int resultAddress, int value1, int value2)
	{ return new RelopNotEqualsBothImmediate
			(currentQuadNum,resultAddress,value1,value2); }

	public MemModQuad makeRelopNotEqualsLeftImmediate
		(int resultAddress, int value, int address)
	{ return new RelopNotEqualsLeftImmediate
			(currentQuadNum,resultAddress,value,address); }

	public MemModQuad makeRelopNotEqualsRightImmediate
		(int resultAddress, int address, int value)
	{ return new RelopNotEqualsRightImmediate
			(currentQuadNum,resultAddress,address,value); }

	public MemModQuad makeRelopNotEqualsRegular
		(int resultAddress, int address1, int address2)
	{ return new RelopNotEqualsRegular
			(currentQuadNum,resultAddress,address1,address2); }
	
	public MemModQuad makeRelopLessThanBothImmediate
		(int resultAddress, int value1, int value2)
	{ return new RelopLessThanBothImmediate
			(currentQuadNum,resultAddress,value1,value2); }

	public MemModQuad makeRelopLessThanLeftImmediate
		(int resultAddress, int value, int address)
	{ return new RelopLessThanLeftImmediate
			(currentQuadNum,resultAddress,value,address); }

	public MemModQuad makeRelopLessThanRightImmediate
		(int resultAddress, int address, int value)
	{ return new RelopLessThanRightImmediate
			(currentQuadNum,resultAddress,address,value); }

	public MemModQuad makeRelopLessThanRegular
		(int resultAddress, int address1, int address2)
	{ return new RelopLessThanRegular
			(currentQuadNum,resultAddress,address1,address2); }

	public MemModQuad makeRelopLessEqualsThanBothImmediate
		(int resultAddress, int value1, int value2)
	{ return new RelopLessThanEqualsBothImmediate
		(currentQuadNum,resultAddress,value1,value2); }

	public MemModQuad makeRelopLessThanEqualsLeftImmediate
		(int resultAddress, int value, int address)
	{ return new RelopLessThanEqualsLeftImmediate
			(currentQuadNum,resultAddress,value,address); }

	public MemModQuad makeRelopLessThanEqualsRightImmediate
		(int resultAddress, int address, int value)
	{ return new RelopLessThanEqualsRightImmediate
			(currentQuadNum,resultAddress,address,value); }

	public MemModQuad makeRelopLessThanEqualsRegular
		(int resultAddress, int address1, int address2)
	{ return new RelopLessThanEqualsRegular
			(currentQuadNum,resultAddress,address1,address2); }
	
	public MemModQuad makeRelopGreaterThanBothImmediate
		(int resultAddress, int value1, int value2)
	{ return new RelopGreaterThanBothImmediate
			(currentQuadNum,resultAddress,value1,value2); }

	public MemModQuad makeRelopGreaterThanLeftImmediate
		(int resultAddress, int value, int address)
	{ return new RelopGreaterThanLeftImmediate
			(currentQuadNum,resultAddress,value,address); }

	public MemModQuad makeRelopGreaterThanRightImmediate
		(int resultAddress, int address, int value)
	{ return new RelopGreaterThanRightImmediate
		(currentQuadNum,resultAddress,address,value); }

	public MemModQuad makeRelopGreaterThanRegular
		(int resultAddress, int address1, int address2)
	{ return new RelopGreaterThanRegular
			(currentQuadNum,resultAddress,address1,address2); }

	public MemModQuad makeRelopGreaterEqualsThanBothImmediate
		(int resultAddress, int value1, int value2)
	{ return new RelopGreaterThanEqualsBothImmediate
			(currentQuadNum,resultAddress,value1,value2); }

	public MemModQuad makeRelopGreaterThanEqualsLeftImmediate
		(int resultAddress, int value, int address)
	{ return new RelopGreaterThanEqualsLeftImmediate
			(currentQuadNum,resultAddress,value,address); }

	public MemModQuad makeRelopGreaterThanEqualsRightImmediate
		(int resultAddress, int address, int value)
	{ return new RelopGreaterThanEqualsRightImmediate
			(currentQuadNum,resultAddress,address,value); }

	public MemModQuad makeRelopGreaterThanEqualsRegular
		(int resultAddress, int address1, int address2)
	{ return new RelopGreaterThanEqualsRegular
			(currentQuadNum,resultAddress,address1,address2); }
	
	public MemModQuad makeAssignImmediateInteger(int resultAddress, int value)
	{ return new AssignImmediateInteger(currentQuadNum,resultAddress,value); }
	
	public MemModQuad makeAssignImmediateBoolean(int resultAddress, boolean value)
	{ return new AssignImmediateBoolean(currentQuadNum,resultAddress,value); }
	
	public MemModQuad makeAssignRegular(int resultAddress, int address)
	{ return new AssignRegular(currentQuadNum,resultAddress,address); }
	
	public InstrModQuad makeUnconditionalJump(int targetQuadIndex)
	{ return new UnconditionalJump(currentQuadNum,targetQuadIndex); }
	
	public InstrModQuad makeIfTrueImmediate(int targetQuadIndex,boolean value)
	{ return new IfTrueImmediate(currentQuadNum,targetQuadIndex,value); }
	
	public InstrModQuad makeIfTrueRegular(int targetQuadIndex,int address)
	{ return new IfTrueRegular(currentQuadNum,targetQuadIndex,address); }

	public InstrModQuad makeIfFalseImmediate(int targetQuadIndex,boolean value)
	{ return new IfFalseImmediate(currentQuadNum,targetQuadIndex,value); }
	
	public InstrModQuad makeIfFalseRegular(int targetQuadIndex,int address)
	{ return new IfFalseRegular(currentQuadNum,targetQuadIndex,address); }
	
	public InstrModQuad makeCall(int targetQuadIndex, int numEltsToPopAtEnd)
	{ return new Call(currentQuadNum,targetQuadIndex,numEltsToPopAtEnd); }
	
	public MemModQuad makeParam(int resultAddress, int parameterAddress)
	{ return new Param(currentQuadNum,resultAddress,parameterAddress); }
	
	public MemModQuad makePrelude(int newStackTopOffset)
	{ return new Prelude(currentQuadNum,newStackTopOffset); }
	
	public InstrModQuad makeReturn(int targetQuadIndex, int newStackTopOffset)
	{ return new Return(currentQuadNum,targetQuadIndex,newStackTopOffset); }
	
	public MemModQuad makePrint(int resultAddress, String formatString)
	{ return new Print(currentQuadNum,resultAddress,formatString); }
	
	public MemModQuad makeRead(int resultAddress, String formatString)
	{ return new Read(currentQuadNum,resultAddress,formatString); }
	
	public Quad makeStart(int targetQuadIndex)
	{ return new Start(currentQuadNum,targetQuadIndex); }
	
	//\\///\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//\\//
	
	/*
	 * The remaining public interface deals with backpatching and display
	 */

	public void updateBackpatching(String quadLabel, int newlyDeterminedQuadAddress)
	{
		BackpatchEntry b = (BackpatchEntry) quadsToBackpatch.get(quadLabel);
		b.setBackpatchAddress(newlyDeterminedQuadAddress);
		quadsToBackpatch.put(quadLabel, b);
	}
	
	public void performFinalBackpatching()
	{
		Iterator i = (Iterator) quadsToBackpatch.values().iterator();
		BackpatchEntry b = null;
		InstrModQuad q = null;
		while (i.hasNext())
		{
			b = (BackpatchEntry) i.next();
			q = (InstrModQuad) quads.get(b.getQuadId());
			q.setTargetQuadIndex(b.getBackpatchAddress());
		}
	}
	
	public void showQuads()
	{
		System.out.println("<<<<<<<<<<< Quad List >>>>>>>>>>>>");
		for (int i=0; i<currentQuadNum; i++)
		{
			System.out.println((Quad)quads.get(i));
		}
		System.out.println();
	}

	public static final int UNK =  -1;		//For initialization/boundary checking
	public static final int MUL = 	0;		//Two arg addresses/literals and 1 target address
	public static final int DIV = 	1;		//Same as above
	public static final int AND = 	2;		//Same as above
	public static final int ADD = 	3;		//Same as above
	public static final int SUB = 	4;		//Same as above
	public static final int OR  = 	5;		//Same as above
	public static final int NEG = 	6;		//1 address/literal (int) and 1 target address
	public static final int NOT = 	7;		//1 address/literal (bool) and 1 target address
	public static final int RLEQ = 	8;		//Two arg addresses/literals + 1 target addr
	public static final int RLNTEQ = 9;		//Same as above
	public static final int RLST = 	10;		//Same as above
	public static final int RLSTEQ = 11;	//Same as above
	public static final int RLGT = 	12;		//Same as above
	public static final int RLGTEQ = 13;	//Same as above
	public static final int DOJMP = 14;		//1 target quad index
	public static final int IFTRU = 15;		//Takes an address/literal and target quad index
	public static final int IFFAL = 16;		//Same as above
	public static final int ASGN = 	17;		//1 address/literal and a target address
	public static final int PRNT = 	18;		//Complex function for print statements
	public static final int READ = 	19;		//Complex function for read statements
	public static final int CALL = 	20;		//1 target quad index and num elts to pop on return 
	public static final int PARM = 	21;		//1 address (literals get runtime location) to push 
	public static final int PREL = 	22;		//1 integer to move the runtime stack top
	public static final int RET = 	23;		//1 arg indicating how many to pop on return
	public static final int START =	24;		//indicates targetQuadIndex where emulator begins


	//Used in verbose output for tracing contents of symbol table
	public static final String quadTypeNameMap(int type)
	{
		String result = null;
		if (type==UNK) 			result = "UNK    ";
		else if (type==MUL) 	result = "MUL    ";
		else if (type==DIV)		result = "DIV    ";
		else if (type==AND) 	result = "AND    ";
		else if (type==ADD) 	result = "ADD    ";
		else if (type==SUB) 	result = "SUB    ";
		else if (type==OR)  	result = "OR     ";
		else if (type==NEG) 	result = "NEG    ";
		else if (type==NOT) 	result = "NOT    ";
		else if (type==RLEQ) 	result = "(==)   ";
		else if (type==RLNTEQ) 	result = "(!=)   ";
		else if (type==RLST) 	result = "(<)    ";
		else if (type==RLSTEQ) 	result = "(<=)   ";
		else if (type==RLGT) 	result = "(>)    ";
		else if (type==RLGTEQ) 	result = "(>=)   ";		
		else if (type==DOJMP) 	result = "JMP    ";
		else if (type==IFTRU) 	result = "IFTRUE ";
		else if (type==IFFAL) 	result = "IFFALSE";
		else if (type==ASGN) 	result = "ASSIGN ";
		else if (type==PRNT) 	result = "PRINT  ";
		else if (type==READ) 	result = "READ   ";
		else if (type==CALL) 	result = "CALL   ";
		else if (type==PARM) 	result = "PARAM  ";
		else if (type==PREL) 	result = "PRELUDE";
		else if (type==RET) 	result = "RETURN ";
		else if (type==START)	result = "START  ";
		return result;
	}	
	

	
	
	public static void main(String[] argv)
	{
		NQG quadGen = new NQG(100); //small example
		MemModQuad mq;
		mq = quadGen.makeMulRightImmediate(5,2,3);
		quadGen.addQuad(mq);
		mq = quadGen.makeDivRegular(6,1,4);
		quadGen.addQuad(mq);
		mq = quadGen.makeAddLeftImmediate(9,11,7);
		quadGen.addQuad(mq);
		mq = quadGen.makeSubBothImmediate(12,417,890);
		quadGen.addQuad(mq);
		
		InstrModQuad iq = (InstrModQuad) 
						quadGen.makeIfTrueImmediate(-1,true); //-1 means forward reference
		String label = iq.getBackpatchQuadLabel();
		quadGen.addQuad(iq);
		
		Quad q = quadGen.makeStart(0);
		quadGen.addQuad(q);
		quadGen.showQuads();
		
		quadGen.updateBackpatching(label, 17);
		quadGen.performFinalBackpatching();
		quadGen.showQuads();
	}
	
	/*Private service method triggered by "addForwardLabeledQuad".
	 *At end of a parse the list of forward labels must be traversed and 
	 *their addresses assigned.
	 */
	private String getNewQuadLabel()
	{
		String name = "Q"+(new Integer(nextBackpatchLabelNum++)).toString();
		return name;
	}

	
}
