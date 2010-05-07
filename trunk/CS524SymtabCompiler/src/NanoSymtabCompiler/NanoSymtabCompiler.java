package NanoSymtabCompiler;

import java.io.IOException;
import java.util.Iterator;

import invisible.jacc.parse.CompilerModel;
import invisible.jacc.parse.NonterminalFactory;
import invisible.jacc.parse.Parser;
import invisible.jacc.parse.Scanner;
import invisible.jacc.parse.SyntaxException;
import invisible.jacc.parse.Token;
import invisible.jacc.parse.TokenFactory;

public class NanoSymtabCompiler extends CompilerModel 
{

	// This flag enables the use of debug token and nonterminal factories.

	static final boolean _debug = false;
	
	private boolean symbolTableVerbose = false;
	private boolean showReductions = true;
	private boolean showSymbolTable = true;
	
	int _conditionNotInComment;
	int _conditionInLineComment;
	int _conditionInBracketedComment;
	
	private NanoSymbolTable symtab;

	// Constructor must create the scanner and parser tables.

	public NanoSymtabCompiler ()
	{
		super();

		//Instantiate the NanoSymbolTable
		symtab = new NanoSymbolTable(symbolTableVerbose);
		
		// Get our scanner table
		_scannerTable = new NanoGrammarScannerTable ();

		// Link the token factories to the scanner table
		_scannerTable.linkFactory ("id",			"", new idT());
		_scannerTable.linkFactory ("intConst", 		"", new intConstT());
		_scannerTable.linkFactory ("boolConst", 	"", new boolConstT());
		_scannerTable.linkFactory ("stringConst", 	"", new stringConstT());
		
		// Handling of comments
		_scannerTable.linkFactory("beginLineComment", 		"", new beginLineCommentT());
		_scannerTable.linkFactory("beginBracketedComment", 	"", new beginBracketedCommentT());
		_scannerTable.linkFactory("bracketedCommentLineEnd","", new LineEnd ());
		_scannerTable.linkFactory("endLineComment", 		"", new endLineCommentT());
		_scannerTable.linkFactory("endBracketedComment", 	"", new endBracketedCommentT());
		_scannerTable.linkFactory("lineEnd", 				"", new LineEnd ());
		_scannerTable.linkFactory("stringConstRunOn", 		"", new stringConstRunOnT ());
		
		// Link condition numbers (used in handling comments)
		_conditionNotInComment = _scannerTable.lookupCondition ("notInComment");
		_conditionInLineComment = _scannerTable.lookupCondition ("inLineComment");
		_conditionInBracketedComment = _scannerTable.lookupCondition ("inBracketedComment");
		
		// Get our parser table
		_parserTable = new NanoGrammarParserTable ();

		// Link the nonterminal factories to the parser table
		_parserTable.linkFactory("Goal", 			"", 			new GoalNT());
		_parserTable.linkFactory("program", 		"", 			new programNT());
		
		_parserTable.linkFactory("statementList", 	"nonempty",		new statementListNonemptyNT());
		_parserTable.linkFactory("statementList", 	"empty", 		new statementListEmptyNT());
		
		_parserTable.linkFactory("constDecList", 	"nonempty",		new constDecListNonemptyNT());
		_parserTable.linkFactory("constDecList", 	"empty", 		new constDecListEmptyNT());
		
		_parserTable.linkFactory("varDecList", 		"nonempty",		new varDecListNonemptyNT());
		_parserTable.linkFactory("varDecList", 		"empty", 		new varDecListEmptyNT());
		
		_parserTable.linkFactory("procDecList", 	"nonempty",		new procDecListNonemptyNT());
		_parserTable.linkFactory("procDecList", 	"empty", 		new procDecListEmptyNT());		
		
		_parserTable.linkFactory("startMainBlock", 	"", 			new startMainBlockNT());
		_parserTable.linkFactory("endMainBlock", 	"", 			new endMainBlockNT());
		
		_parserTable.linkFactory("constDec", 		"idList", 	new constDecNonemptyNT());
		
		_parserTable.linkFactory("idList", 			"list", 		new idListListNT());
		_parserTable.linkFactory("idList", 			"single", 		new idListSingleNT());
		
		_parserTable.linkFactory("varDec", 			"idList", 		new varDecIdListNT());
		_parserTable.linkFactory("varDec", 			"arrayIdList", 	new varDecArrayIdListNT());
		
		_parserTable.linkFactory("arrayIdList", 	"list", 		new arrayIdListListNT());
		_parserTable.linkFactory("arrayIdList", 	"single", 		new arrayIdListSingleNT());
		
		_parserTable.linkFactory("procDec", 		"", 			new procDecNT());
	
		_parserTable.linkFactory("formalList", 		"list", 		new formalListListNT());
		_parserTable.linkFactory("formalList", 		"single", 		new formalListSingleNT());
		_parserTable.linkFactory("formalList", 		"empty", 		new formalListEmptyNT());
		
		_parserTable.linkFactory("formal", 			"", 			new formalNT());
		
		_parserTable.linkFactory("scalarType",		"integer", 		new scalarTypeIntegerNT());
		_parserTable.linkFactory("scalarType", 		"boolean", 		new scalarTypeBooleanNT());
		
		_parserTable.linkFactory("statement", 		"blockStmnt", 	new statementBlockStmntNT());
		_parserTable.linkFactory("statement", 		"printStmnt", 	new statementPrintStmntNT());
		_parserTable.linkFactory("statement", 		"readStmnt", 	new statementReadStmntNT());
		_parserTable.linkFactory("statement", 		"asgnStmnt", 	new statementAsgnStmntNT());
		_parserTable.linkFactory("statement", 		"condStmnt", 	new statementCondStmntNT());
		_parserTable.linkFactory("statement", 		"forStmnt", 	new statementForStmntNT());
		_parserTable.linkFactory("statement", 		"returnStmnt", 	new statementReturnStmntNT());
		_parserTable.linkFactory("statement", 		"callStmnt", 	new statementCallStmntNT());
		
		_parserTable.linkFactory("blockStmnt", 		"", 			new blockStmntNT());
		
		_parserTable.linkFactory("startNewBlock", 	"", 			new startNewBlockNT());
		
		_parserTable.linkFactory("endCurrentBlock", "", 			new endCurrentBlockNT());
		
		_parserTable.linkFactory("showSymbolTable", "",				new showSymbolTableNT());
		
		_parserTable.linkFactory("printStmnt", 		"", 			new printStmntNT());

		_parserTable.linkFactory("printExprList", 	"nonempty",		new printExprListNonemptyNT());
		_parserTable.linkFactory("printExprList", 	"empty", 		new printExprListEmptyNT());		
		
		_parserTable.linkFactory("readStmnt", 		"", 			new readStmntNT());

		_parserTable.linkFactory("inputTargetList",	"nonempty",		new inputTargetListNonemptyNT());
		_parserTable.linkFactory("inputTargetList",	"empty", 		new inputTargetListEmptyNT());		
		
		_parserTable.linkFactory("inputTarget", 	"id", 			new inputTargetIdNT());
		_parserTable.linkFactory("inputTarget", 	"idArray", 		new inputTargetIdArrayNT());
		
		_parserTable.linkFactory("asgnStmnt", 		"int", 			new asgnStmntIntNT());
		_parserTable.linkFactory("asgnStmnt", 		"intArray", 	new asgnStmntIntArrayNT());
		
		_parserTable.linkFactory("condStmnt", 		"ifThen", 		new condStmntIfThenNT());
		_parserTable.linkFactory("condStmnt", 		"ifThenElse", 	new condStmntIfThenElseNT());
		
		_parserTable.linkFactory("forStmnt", 		"", 			new forStmntNT());
		
		_parserTable.linkFactory("returnStmnt", 	"", 			new returnStmntNT());
		
		_parserTable.linkFactory("callStmnt", 		"nothing", 		new callStmntNothingNT());
		_parserTable.linkFactory("callStmnt", 		"exprList", 	new callStmntExprListNT());
		
		_parserTable.linkFactory("exprList", 		"list", 		new exprListListNT());
		_parserTable.linkFactory("exprList", 		"single", 		new exprListSingleNT());
		
		_parserTable.linkFactory("expr", 			"plus", 		new exprPlusNT());
		_parserTable.linkFactory("expr", 			"minus", 		new exprMinusNT());
		_parserTable.linkFactory("expr", 			"or", 			new exprOrNT());
		_parserTable.linkFactory("expr", 			"term", 		new exprTermNT());
		
		_parserTable.linkFactory("term", 			"star", 		new termStarNT());
		_parserTable.linkFactory("term", 			"slash", 		new termSlashNT());
		_parserTable.linkFactory("term", 			"and", 			new termAndNT());
		_parserTable.linkFactory("term", 			"factor", 		new termFactorNT());
		
		_parserTable.linkFactory("factor", 			"positive", 	new factorPositiveNT());
		_parserTable.linkFactory("factor", 			"negative", 	new factorNegativeNT());
		_parserTable.linkFactory("factor", 			"not", 			new factorNotNT());
		
		_parserTable.linkFactory("prim", 			"const", 		new primConstNT());
		_parserTable.linkFactory("prim", 			"boolConst", 	new primBoolConstNT());
		_parserTable.linkFactory("prim", 			"value", 		new primValueNT());
		_parserTable.linkFactory("prim", 			"expr", 		new primExprNT());
		_parserTable.linkFactory("prim", 			"relop", 		new primRelopNT());
		
		_parserTable.linkFactory("value", 			"id", 			new valueIdNT());
		_parserTable.linkFactory("value", 			"expr", 		new valueExprNT());
		
		_parserTable.linkFactory("relop", 			"equals", 		new relopEqualsNT());
		_parserTable.linkFactory("relop", 			"lessThan", 	new relopLessThanNT());
		_parserTable.linkFactory("relop", 			"greaterThan", 	new relopGreaterThanNT());
		_parserTable.linkFactory("relop", 			"lessThanEquals", new relopLessThanEqualsNT());
		_parserTable.linkFactory("relop", 			"greaterThanEquals", new relopGreaterThanEqualsNT());
		_parserTable.linkFactory("relop", 			"notEquals", 	new relopNotEqualsNT());
		
		if (_debug)
		{
			if (setDebugMode (true, true))
			{
				throw new InternalError ("NanoCompiler: Consistency check failed.");
			}
		}
		
		return;
	}
	
	public void scannerEOF (Scanner scanner, Token token)
	{
		// If we are in the middle of a comment ...
		if (scanner.condition() == _conditionInBracketedComment)
		{
			// Report a run-on comment error
			reportError (token, null, "Run-on comment." );
		}
		return;
	}


	public static void main (String[] args) throws Exception
	{

		// Create the compiler object
		NanoSymtabCompiler compiler = new NanoSymtabCompiler();

		System.out.println ("IJACC Nano Compiler for Testing Symbol Table-----------------");
		System.out.println ("Compiling " + args[0] + " ...\n");
		compiler.compile (args[0]);
		System.out.println ("\n\nIJACC Nano Compiler complete.");
		
	}

	/**************************************************************************
	 * Terminal classes********************************************************
	 * ************************************************************************
	 */
	
	final class idT extends TokenFactory
	{
		public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
		{
			String idString = scanner.tokenToString ();
			token.value = idString;
		
			// Assembled token
			return assemble;
		}
	}
	
	final class intConstT extends TokenFactory
	{
		public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
		{
			String idString = scanner.tokenToString ();
			token.value = idString;
		
			// Assembled token
			return assemble;
		}
	}

	final class boolConstT extends TokenFactory
	{
		public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
		{
			String idString = scanner.tokenToString ();
			token.value = idString;
		
			// Assembled token
			return assemble;
		}
	}

	final class stringConstT extends TokenFactory
	{
		public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
		{
			String idString = scanner.tokenToString ();
			token.value = idString;
		
			// Assembled token
			return assemble;
		}
	}
	
	final class beginLineCommentT extends TokenFactory
	{
		public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
		{
			scanner.setCondition(_conditionInLineComment);
			System.out.print(token.line + ": ");
			System.out.print("Beginning line comment... ");
		
			// Assembled token
			return discard;
		}
	}
	
	final class beginBracketedCommentT extends TokenFactory
	{
		public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
		{
			scanner.setCondition(_conditionInBracketedComment);
			System.out.print(token.line + ": ");
			System.out.print("Beginning bracketed comment... ");
	
			// Assembled token
			return discard;
		}
	}
	
	final class endLineCommentT extends TokenFactory
	{
		public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
		{
			scanner.setCondition(_conditionNotInComment);
			System.out.println("...End of line comment\n");
			
			// Bump the line number
			scanner.countLine ();
			
			
			// Assembled token
			return discard;
		}
	}
	
	final class endBracketedCommentT extends TokenFactory
	{
		public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
		{
			scanner.setCondition(_conditionNotInComment);
			System.out.print(token.line + ": ");
			System.out.println("...End of bracketed comment\n");
			
			// Assembled token
			return discard;
		}
	}

	final class stringConstRunOnT extends TokenFactory
	{
		public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
		{
			System.out.println("Error String - Run on String Constant\n");
			
			// Assembled token
			return discard;
		}
	}
	
	// Token factory class for lineEnd.
	// A lineEnd is discarded after counting the line.
	final class LineEnd extends TokenFactory
	{
		public int makeToken (Scanner scanner, Token token)
		throws IOException, SyntaxException
		{
			// Bump the line number
			scanner.countLine ();
			
			// Discard token
			return discard;
		}
	}
	
	
	/******************************************************************************
	 * Nonterminal classes*********************************************************
	 * ****************************************************************************
	 */


	//Goal
	final class GoalNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			//Print nothing for this one
			
			return null;
			}
	}
	
	//program
	final class programNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("Program -> StartMainBlock");
			System.out.println("           constDecList");
			System.out.println("           varDecList");
			System.out.println("           procDecList");
			System.out.println("           begin");
			System.out.println("           Statement");
			System.out.println("           StatementList");
			System.out.println("           end");
			System.out.println("           EndMainBlock");
			System.out.println("           semicolon\n");
			return null;
			}
	}
	
	//statementList (empty,list)
	final class statementListEmptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("statementList {empty} -> /* empty */\n");
			
			return null;
			}
	}
	final class statementListNonemptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("statementList {nonempty} -> statementList statement\n");
			
			return null;
			}
	}

	//constDecList (empty,list)
	final class constDecListEmptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("constDecList {empty} -> /* empty */\n");
			
			return null;
			}
	}
	final class constDecListNonemptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("constDecList {nonempty} -> constDecList constDec\n");
			
			return null;
			}
	}	
	
	//varDecList (empty,list)
	final class varDecListEmptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("varDecList {empty} -> /* empty */\n");
			
			return null;
			}
	}
	final class varDecListNonemptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("varDecList {nonempty} -> varDecList varDec\n");
			
			return null;
			}
	}	
	
	
	//procDecList (empty,list)
	final class procDecListEmptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("procDecList {empty} -> /* empty */\n");
			
			return null;
			}
	}
	final class procDecListNonemptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("procDecList {nonempty} -> procDecList procDec\n");
			
			return null;
			}
	}	
	
	//startMainBlock
	final class startMainBlockNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
		{
			if (showReductions) 									
				System.out.println("\nReduced by rule: StartMainBlock -> /* empty */\n");
			symtab.startNewBlock();
		
			//Return null value
			return null;
		}
	}
	
	//endMainBlock
	final class endMainBlockNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
		{
			if (showReductions) 									
				System.out.println("\nReduced by rule: EndMainBlock -> /* empty */\n");
			symtab.endCurrentBlock();
		
			//Return null value
			return null;
		}
	}
	
	//constDec (idList)
	final class constDecNonemptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			Integer value = (Integer) parser.rhsValue(3);
			if (showReductions) 	System.out.println("\nReduced by rule: ConstantDeclaration -> const IdList equals intConst semicolon");
			if (value==null) return null; //discard error insertions
			if (showReductions) System.out.println("intConst value: "+value+"\n");

	/*
	 *	Eventually�
	 * 	int constValue = 
	 * 	 ((Integer)parser.rhsValue(3)).intValue();
	 *  addQuad(store,@some_index,const_value)
	 * 
	 */
	//This value cannot be stored in the symbol table without 
	//requiring that structure to be memory resident during 
	//execution of Nano programs.
	//While that may actually be the case we don't want to be 
	//restricted to this.
	//So we store the constant in memory like any other 
	//variable (later we will add this quad).
	//So the fact that it is const during compilation prevents 
	//the issuance of 
	//any instructions that can change that memory location's 
	//value.  Here we just print out what the symbol table saw 
	//but did not store (keeping with the idea that the symbol //table stores meta-data from compile-time
	//not data from execution time.

	// HERE IS THE CORE CODE::
			
		Iterator tempIdListIterator = symtab.getTempIdListIterator();
			while (tempIdListIterator.hasNext())
			{			
		symtab.addConstIntToCurrentBlock
				(((String)tempIdListIterator.next()));
			}
			symtab.tempIdListClear();

			// Return null value
			return null;
		}
	}
	
	//idList (list, single)
	final class idListListNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			String idLexeme = (String) parser.rhsValue(2);
			if (showReductions) 									
				System.out.println("\nReduced by rule: IdList {recurring} -> IdList comma identifier");
			if (idLexeme==null) 
				return null; //discard error insertions
			if (showReductions) 									
				System.out.println("identifier lexeme: "+idLexeme+"\n");

			symtab.tempIdListAdd(idLexeme);
			
			// Return null value
			return null;

			}
	}
	final class idListSingleNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param)
			throws IOException, SyntaxException
		{
			String idLexeme = (String) parser.rhsValue(0);
			if (showReductions) 									
				System.out.println("\nReduced by rule: IdList {single} -> identifier");
			if (idLexeme==null) 
				return null; //discard error insertions
			if (showReductions) 									
				System.out.println("identifier lexeme: "+idLexeme+"\n");
				symtab.tempIdListClear();
				symtab.tempIdListAdd(idLexeme);

			// Return null value
			return null;
		}
	}
	
	
	//varDec (idList, arrayIdList)
	final class varDecIdListNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			int NanoSymbolTableTypeFlag = ((Integer) parser.rhsValue(3)).intValue();
			Iterator tempIdListIterator = symtab.getTempIdListIterator();
			boolean notAlreadyDefined = true;
			String nameToDefine = "";
			while (tempIdListIterator.hasNext())
			{			
				nameToDefine = (String)tempIdListIterator.next();
				if (symtab.addScalarToCurrentBlock(nameToDefine,NanoSymbolTableTypeFlag) == null)
					notAlreadyDefined = false;
				else
					notAlreadyDefined = true;
				if (!notAlreadyDefined)
				{
				reportError("",
				"Duplicate declaration in this block of"
				+nameToDefine);
				}
			}
			symtab.tempIdListClear();
			
			// Return null value
			return null;

			}
	}
	final class varDecArrayIdListNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("VarDec {arrayIdList} -> var arrayIdList colon scalarType semicolon\n");
			
			return null;
			}
	}
	
	
	//arrayIdList (list, single)
	final class arrayIdListListNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("arrayIdList {list} -> id lbracket intConst rbracket comma arrayIdList");
			String idString = (String) parser.rhsValue (0);
			String intString = (String) parser.rhsValue(2);
			System.out.println("identifier lexeme: " + idString);
			System.out.println("intConst lexeme: " + intString + "\n");
			
			return null;
			}
	}
	final class arrayIdListSingleNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("arrayIdList {single} -> id lbracket intConst rbracket");
			String idString = (String) parser.rhsValue (0);
			String intString = (String) parser.rhsValue(2);
			System.out.println("identifier lexeme: " + idString);
			System.out.println("intConst lexeme: " + intString + "\n");
			
			return null;
			}
	}
	
	//procDec
	final class procDecNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("procDec ->  procedure id lparen formalList rparen semicolon blockStmnt");
			String idString = (String) parser.rhsValue (1);
			System.out.println("identifier lexeme: " + idString + "\n");
			
			return null;
			}
	}
	
	//formalList (empty, list, single)
	final class formalListEmptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("formalList {empty} -> /* empty */\n");
			
			return null;
			}
	}
	final class formalListListNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("formalList {list} -> formal semicolon formalList\n");
			
			return null;
			}
	}
	final class formalListSingleNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("formalList {single} -> formal\n");
			
			return null;
			}
	}
	
	//formal
	final class formalNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("formal -> idList colon scalarType\n");
			
			return null;
			}
	}
	//scalarType (integer, boolean)
	final class scalarTypeIntegerNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			if (showReductions) 
				System.out.println("\nReduced by rule: ScalarType {integer} -> integer\n");

			//Return this as an object then up above pass it to symbol table as an Object
			return new Integer(NanoSymbolTable.INT_TYPE);
			}
	}
	final class scalarTypeBooleanNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			if (showReductions) 
				System.out.println("\nReduced by rule: ScalarType {boolean} -> boolean\n");

			//Return this as an object then up above pass it to symbol table as an Object
			return new Integer(NanoSymbolTable.BOOL_TYPE);
			}
	}
	
	//statement (blockStmnt, printStmnt, readStmnt, asgnStmnt, condStmnt, forStmnt, returnStmnt, callStmnt)
	
	final class statementBlockStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("statement {blockStmnt} -> blockStmnt\n");
			
			return null;
			}
	}
	final class statementPrintStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("statement {printStmnt} -> printStmnt\n");
			
			return null;
			}
	}
	final class statementReadStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("statement {readStmnt} -> readStmnt\n");
			
			return null;
			}
	}
	final class statementAsgnStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("statement {asgnStmnt} -> asgnStmnt\n");
			
			return null;
			}
	}
	final class statementCondStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("statement {condStmnt} -> condStmnt\n");
			
			return null;
			}
	}
	final class statementForStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("statement {forStmnt} -> forStmnt\n");
			
			return null;
			}
	}
	final class statementReturnStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("statement {returnStmnt} -> returnStmnt\n");
			
			return null;
			}
	}
	final class statementCallStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("statement {callStmnt} -> callStmnt\n");
			
			return null;
			}
	}
	
	//blockStmnt
	final class blockStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("blockStmnt -> begin");
			System.out.println("              startNewBlock");
			System.out.println("              constDecList");
			System.out.println("              varDecList");
			System.out.println("              StatementList");
			System.out.println("              end");
			System.out.println("              endCurrentBlock");
			System.out.println("              semicolon\n");
			return null;
			}
	}
	
	//startNewBlock
	final class startNewBlockNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("startNewBlock ->  /* empty */\n");
			
			return null;
			}
	}
	
	//endCurrentBlock
	final class endCurrentBlockNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("endCurrentBlock ->   /* empty */\n");
			
			return null;
			}
	}
	
	final class showSymbolTableNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
		{
			if (showReductions)
				System.out.println("\nReduced by rule: ShowSymbolTable -> /* empty */\n");
			if (showSymbolTable) 
				symtab.showContents();
		
			//Return null value
			return null;
		}
	}
	
	//printStmnt
	final class printStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("printStmnt -> print lparen stringConst printExprList rparen semicolon\n");
			String stringString = (String) parser.rhsValue (2);
			System.out.println("string lexeme: " + stringString + "\n");
			
			return null;
			}
	}

	//printExprList (empty, nonempty)
	final class printExprListNonemptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("printExprList {nonempty} -> printExprList comma expr\n");
			
			return null;
			}
	}
	final class printExprListEmptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("printExprList {empty} -> /* empty */\n");
			
			return null;
			}
	}	
	
	//readStmnt
	final class readStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("readStmnt -> read lparen stringConst inputTargetList rparen semicolon;");
			String stringString = (String) parser.rhsValue (2);
			System.out.println("string lexeme: " + stringString + "\n");
			
			return null;
			}
	}
	
	//inputTargetList (empty, nonempty)
	final class inputTargetListNonemptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("inputTargetList {nonempty} -> inputTargetList comma expr\n");
			
			return null;
			}
	}
	final class inputTargetListEmptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("inputTargetList {empty} -> /* empty */\n");
			
			return null;
			}
	}
	
	//inputTarget (id, idArray)
	final class inputTargetIdNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("inputTarget {id} -> id");
			String idString = (String) parser.rhsValue (0);
			System.out.println("identifier lexeme: " + idString + "\n");
	
			return null;
			}
	}
	final class inputTargetIdArrayNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("inputTarget {idArray} -> id lbracket expr rbracket\n");
			
			return null;
			}
	}
	
	//asgnStmnt (int, intArray)
	final class asgnStmntIntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("asgnStmnt {int} -> id assign expr semicolon");
			String idString = (String) parser.rhsValue (0);
			System.out.println("identifier lexeme: " + idString + "\n");
			
			return null;
			}
	}
	final class asgnStmntIntArrayNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("asgnStmnt {intArray} -> id lbracket expr rbracket assign expr semicolon");
			String idString = (String) parser.rhsValue (0);
			System.out.println("identifier lexeme: " + idString + "\n");
			
			return null;
			}
	}
	
	//condStmnt (ifThen, ifThenElse)
	final class condStmntIfThenNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("condStmnt {ifThen} -> if expr then statement %shift else\n");
			
			return null;
			}
	}
	final class condStmntIfThenElseNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("condStmnt {ifThenElse} -> if expr then statement else statement\n");
			
			return null;
			}
	}
	
	//forStmnt
	final class forStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("forStmnt -> for id assign expr to expr do statement");
			String idString = (String) parser.rhsValue (1);
			System.out.println("identifier lexeme: " + idString + "\n");
			
			return null;
			}
	}
	
	//returnStmnt
	final class returnStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("returnStmnt -> return semicolon\n");
			
			return null;
			}
	}
	
	//callStmnt (nothing, exprList)
	final class callStmntNothingNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("callStmnt {nothing} -> call id lparen rparen semicolon");
			String idString = (String) parser.rhsValue (1);
			System.out.println("identifier lexeme: " + idString + "\n");
			
			return null;
			}
	}
	final class callStmntExprListNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("callStmnt {exprList} -> call id lparen exprList rparen semicolon");
			String idString = (String) parser.rhsValue (1);
			System.out.println("identifier lexeme: " + idString + "\n");
			
			return null;
			}
	}
	
	//exprList (list, single)
	final class exprListListNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("exprList {list} -> expr comma exprList\n");
			
			return null;
			}
	}
	final class exprListSingleNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("exprList {single} -> expr\n");
			
			return null;
			}
	}
	
	//expr (plus, minus, or, term)
	final class exprPlusNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("expr {plus} -> expr plus term\n");
			
			return null;
			}
	}
	final class exprMinusNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("expr {minus} -> expr minus term\n");
			
			return null;
			}
	}
	final class exprOrNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("expr {or} -> expr or term\n");
			
			return null;
			}
	}
	final class exprTermNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("expr {term} -> term\n");
			
			return null;
			}
	}
	
	//term (star, slash, and, factor)
	final class termStarNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("term {star} -> term star factor\n");
			
			return null;
			}
	}
	final class termSlashNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("term {slash} -> term slash factor\n");
			
			return null;
			}
	}
	final class termAndNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("term {and} -> term and factor\n");
			
			return null;
			}
	}
	final class termFactorNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("term {factor} -> factor\n");
			
			return null;
			}
	}
	
	//factor (positive, negative, not)
	final class factorPositiveNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("factor {positive} -> prim\n");
			
			return null;
			}
	}
	final class factorNegativeNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("factor {negative} -> minus prim\n");
			
			return null;
			}
	}
	final class factorNotNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("factor {not} -> not prim\n");
			
			return null;
			}
	}
	
	//prim (const, boolConst, value, expr, relop)
	final class primConstNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("prim {const} -> intConst");
			String intString = (String) parser.rhsValue (0);
			System.out.println("intConst lexeme: " + intString + "\n");
			
			return null;
			}
	}
	final class primBoolConstNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("prim {boolConst} -> boolConst");
			String boolString = (String) parser.rhsValue (0);
			System.out.println("identifier lexeme: " + boolString + "\n");
			
			return null;
			}
	}
	final class primValueNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("prim {value} -> value\n");
			
			return null;
			}
	}
	final class primExprNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("prim {expr} -> lparen expr rparen\n");
			
			return null;
			}
	}
	final class primRelopNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("prim {relop} -> lparen expr relop expr rparen\n");
			
			return null;
			}
	}
	
	//value (id, expr)
	final class valueIdNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param)
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("value {id} -> id");
			String idString = (String) parser.rhsValue (0);
			System.out.println("identifier lexeme: " + idString + "\n");
			
			return null;
			}
	}
	final class valueExprNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("value {expr} -> id lbracket expr rbracket");
			String idString = (String) parser.rhsValue (0);
			System.out.println("identifier lexeme: " + idString + "\n");
			
			return null;
			}
	}
	
	//relop (equals, lessThan, greaterThan, lessThanEquals, greaterThanEquals, notEquals)
	final class relopEqualsNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("relop{equals} -> equals\n");
			
			return null;
			}
	}
	final class relopLessThanNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("relop{lessThan} -> lessThan\n");
			
			return null;
			}
	}
	final class relopGreaterThanNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("relop{greaterThan} -> greaterThan\n");
			
			return null;
			}
	}
	final class relopLessThanEqualsNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("relop{lessThanEquals} -> lessThanEquals\n");
			
			return null;
			}
	}
	final class relopGreaterThanEqualsNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("relop{greaterThanEquals} -> greaterThanEquals\n");
			
			return null;
			}
	}
	final class relopNotEqualsNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			System.out.print(parser.token().line + ": ");
			System.out.println("relop{notEquals} -> notEquals\n");
			
			return null;
			}
	}

}
