package NanoSymtabCompiler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Hashtable;

import NanoSymtabCompiler.NQG.*;
import NanoSymtabCompiler.NanoSymbolTable.*;

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
	private boolean showReductions = false;
	private boolean showSymbolTable = false;
	private boolean showQuads = true;
	
	int _conditionNotInComment;
	int _conditionInLineComment;
	int _conditionInBracketedComment;
	
	private NanoSymbolTable symtab;
	private NQG quadGen;

	// Constructor must create the scanner and parser tables.

	public NanoSymtabCompiler ()
	{
		super();

		//Instantiate the NanoSymbolTable
		symtab = new NanoSymbolTable();
		quadGen = new NQG();
		
		
		// Get our scanner table
		_scannerTable = new NanoGrammarScannerTable ();

		// Link the token factories to the scanner table
		_scannerTable.linkFactory ("id",			"", new idT());
		_scannerTable.linkFactory ("intConst", 		"", new intConstT());
		_scannerTable.linkFactory ("stringConst", 	"", new stringConstT());
		
		//relop tokens
		_scannerTable.linkFactory("isEquals",         "",        new isEqualsT());
		_scannerTable.linkFactory("lessThan",         "",     new lessThanT());
		_scannerTable.linkFactory("greaterThan",        "",     new greaterThanT());
		_scannerTable.linkFactory("lessThanEquals",         "",  new lessThanEqualsT());
		_scannerTable.linkFactory("greaterThanEquals",         "",  new greaterThanEqualsT());
		_scannerTable.linkFactory("notEquals",        "",    new notEqualsT());
		
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
		_parserTable.linkFactory("boolConst",        "true",         new boolConstTrueNT());
		_parserTable.linkFactory("boolConst",        "false",         new boolConstFalseNT());
		
		_parserTable.linkFactory("Goal", 			"", 			new GoalNT());
		_parserTable.linkFactory("program", 		"", 			new programNT());
		
		_parserTable.linkFactory("statementList", 	"nonempty",		new statementListNonemptyNT());
		_parserTable.linkFactory("statementList", 	"empty", 		new statementListEmptyNT());
		
		//Rich: I think this is a duplicate with below
	//	_parserTable.linkFactory("constDec", 		"idList", 		new constDecNonemptyNT());
		
		_parserTable.linkFactory("constDec", 		"idList",		new constDecIdListNT());
		
		_parserTable.linkFactory("constDecList", 	"nonempty",		new constDecListNonemptyNT());
		_parserTable.linkFactory("constDecList", 	"empty", 		new constDecListEmptyNT());
		
		_parserTable.linkFactory("varDecList", 		"nonempty",		new varDecListNonemptyNT());
		_parserTable.linkFactory("varDecList", 		"empty", 		new varDecListEmptyNT());
		
		_parserTable.linkFactory("startMainBlock", 	"", 			new startMainBlockNT());
		_parserTable.linkFactory("endMainBlock", 	"", 			new endMainBlockNT());
		
		_parserTable.linkFactory("idList", 			"list", 		new idListListNT());
		_parserTable.linkFactory("idList", 			"single", 		new idListSingleNT());
		
		_parserTable.linkFactory("varDec", 			"idList", 		new varDecIdListNT());
		_parserTable.linkFactory("varDec", 			"arrayIdList", 	new varDecArrayIdListNT());
		
		_parserTable.linkFactory("arrayIdList", 	"list", 		new arrayIdListListNT());
		_parserTable.linkFactory("arrayIdList", 	"single", 		new arrayIdListSingleNT());
		
		_parserTable.linkFactory("procDec", 		"", 			new procDecNT());
		_parserTable.linkFactory("procHeader",     "",         new procHeaderNT());
		_parserTable.linkFactory("procBody",     "",         new procBodyNT());
	
		_parserTable.linkFactory("formalList", 		"list", 		new formalListListNT());
		_parserTable.linkFactory("formalList", 		"single", 		new formalListSingleNT());
		_parserTable.linkFactory("formalList", 		"empty", 		new formalListEmptyNT());
		
		_parserTable.linkFactory("formal", 			"", 			new formalNT());
		
		_parserTable.linkFactory("scalarType",		"integer", 		new scalarTypeIntegerNT());
		_parserTable.linkFactory("scalarType", 		"boolean", 		new scalarTypeBooleanNT());
		
		_parserTable.linkFactory("arrayType", 		"integer", 		new arrayTypeIntegerNT());
		_parserTable.linkFactory("arrayType", 		"boolean", 		new arrayTypeBooleanNT());
		
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
		
		_parserTable.linkFactory("AddConstQuads", "",            new addConstQuadsNT());
		
		_parserTable.linkFactory("showSymbolTable", "",				new showSymbolTableNT());
		
		_parserTable.linkFactory("StartMarker", "",              new StartMarkerNT());
		
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
		
		/*
		_parserTable.linkFactory("condStmnt", 		"ifThen", 		new condStmntIfThenNT());
		_parserTable.linkFactory("condStmnt", 		"ifThenElse", 	new condStmntIfThenElseNT());
		*/
		_parserTable.linkFactory("Cond",          "unmatched",      new NanoCondUnmatched());
		_parserTable.linkFactory("Cond",          "matched",      new NanoCondMatched());
		_parserTable.linkFactory("CondIfPart",      "",      new NanoCondIfPart());
		_parserTable.linkFactory("CondThenPartUM",      "",      new NanoCondThenPartUM());
		_parserTable.linkFactory("CondThenPartM",      "",      new NanoCondThenPartM());
		_parserTable.linkFactory("CondElseJump",      "",      new NanoCondElseJump());
		
		
		_parserTable.linkFactory("forStmnt", 		"", 			new forStmntNT());
		_parserTable.linkFactory("forHeader",       "",         new forHeaderNT());
		
		_parserTable.linkFactory("returnStmnt", 	"", 			new returnStmntNT());
		
		//_parserTable.linkFactory("callStmnt", 		"nothing", 		new callStmntNothingNT());
		//_parserTable.linkFactory("callStmnt", 		"exprList", 	new callStmntExprListNT());
		
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
		
		_parserTable.linkFactory("relop", 			"isEquals", 		new relopIsEqualsNT());
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
	
   final class isEqualsT extends TokenFactory
   {
      public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
      {
         String idString = scanner.tokenToString ();
         token.value = idString;
      
         // Assembled token
         return assemble;
      }
   }	
	
   final class lessThanT extends TokenFactory
   {
      public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
      {
         String idString = scanner.tokenToString ();
         token.value = idString;
      
         // Assembled token
         return assemble;
      }
   }   
   
   final class greaterThanT extends TokenFactory
   {
      public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
      {
         String idString = scanner.tokenToString ();
         token.value = idString;
      
         // Assembled token
         return assemble;
      }
   }
   
   final class lessThanEqualsT extends TokenFactory
   {
      public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
      {
         String idString = scanner.tokenToString ();
         token.value = idString;
      
         // Assembled token
         return assemble;
      }
   }
   
   final class greaterThanEqualsT extends TokenFactory
   {
      public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
      {
         String idString = scanner.tokenToString ();
         token.value = idString;
      
         // Assembled token
         return assemble;
      }
   }
   
   final class notEqualsT extends TokenFactory
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
			
			if (showReductions) {
			   System.out.print(token.line + ": ");
			   System.out.print("Beginning line comment... ");
			}
			
			// Assembled token
			return discard;
		}
	}
	
	final class beginBracketedCommentT extends TokenFactory
	{
		public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
		{
			scanner.setCondition(_conditionInBracketedComment);
			if (showReductions) {
			   System.out.print(token.line + ": ");
			   System.out.print("Beginning bracketed comment... ");
			}
			// Assembled token
			return discard;
		}
	}
	
	final class endLineCommentT extends TokenFactory
	{
		public int makeToken (Scanner scanner, Token token) throws IOException, SyntaxException
		{
			scanner.setCondition(_conditionNotInComment);
			if (showReductions)
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
			
			if (showReductions) {
			   System.out.print(token.line + ": ");
			   System.out.println("...End of bracketed comment\n");
			}
			
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

	final class boolConstTrueNT extends NonterminalFactory
   {
      public Object makeNonterminal (Parser parser, int param) 
         throws IOException, SyntaxException
         {
         if (showReductions) {
            System.out.print(parser.token().line + ": ");
            System.out.println("bool const true\n");
         }
         return "true";
         }
   }
	
   final class boolConstFalseNT extends NonterminalFactory
   {
      public Object makeNonterminal (Parser parser, int param) 
         throws IOException, SyntaxException
         {
         if (showReductions) {
            System.out.print(parser.token().line + ": ");
            System.out.println("bool const false\n");
         }
         return "false";
         }
   }

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
		   if (showReductions) {
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
		   }
			return null;
			}
	}
	
	//statementList (empty,list)
	final class statementListEmptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("statementList {empty} -> /* empty */\n");
		   }
			return null;
			}
	}
	final class statementListNonemptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("statementList {nonempty} -> statementList statement\n");
		   }
			return null;
			}
	}
	
	//constDec (idList)
	final class constDecIdListNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			Object value = (Object) parser.rhsValue(3);
			if (showReductions) 	
				System.out.println("\nReduced by rule: ConstantDeclaration -> const IdList constEquals Factor semicolon");
			if (value==null) {
				return null; //discard error insertions
			}
			if (showReductions) 
				System.out.println("intConst value: "+value+"\n");
			
			Iterator tempIdListIterator = symtab.getTempIdListIterator();
			boolean notAlreadyDefined = true;
			String nameToDefine = "";
			while (tempIdListIterator.hasNext())
			{			
				nameToDefine = (String)tempIdListIterator.next();
				if (symtab.addConstIntToCurrentBlock(nameToDefine) == null){
					notAlreadyDefined = false;
				}
				else {
					notAlreadyDefined = true;
				
					//Get the value
               NSTIndImmediateEntry e = (NSTIndImmediateEntry)parser.rhsValue(3);
               
               //Get the symbol table entry for the identifier
               NSTIndScalarEntry i = (NSTIndScalarEntry) symtab.get(nameToDefine);
               
               //If the symbol table doesn't contain an entry for id
               if (i==null){
                 reportError("","Constant not defined in this scope.");
                 return null;
               //If the id and expression types don't match
               }
               else if (e.getActualType()!=i.getActualType()){
                  reportError("","Type mismatch in constant assignment statement");
                  return null;
               }
               
               NSTIndImmediateEntry imm = (NSTIndImmediateEntry) e;
               //If the assignment value is a boolean
               if (imm.isBoolean())
               {
                  MemModQuad aqb = quadGen.makeAssignImmediateBoolean(i.getAddress(),imm.getBoolValue());
                  quadGen.addQuad(aqb);
                  return new Integer(aqb.getQuadId());
               }
               //If the assignment value is an integer
               else if (imm.isInteger())
               {
                  MemModQuad aqi = quadGen.makeAssignImmediateInteger(i.getAddress(),imm.getIntValue());
                  quadGen.addQuad(aqi);
                  return new Integer(aqi.getQuadId());
               //Otherwise we messed up
               }else{
                  reportError("","Compiler developer: invalid type of immediate assignment");
                  return null;
               }
				
				}   
				
				
				if (!notAlreadyDefined)
				{
				reportError("","Duplicate declaration in this block of"+nameToDefine);
				}
			}
			symtab.tempIdListClear();
			
			// Return null value
			return null;
			}
	}

	//constDecList (empty,list)
	final class constDecListEmptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("constDecList {empty} -> /* empty */\n");
		   }
			return null;
			}
	}
	final class constDecListNonemptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("constDecList {nonempty} -> constDecList constDec\n");
		   }
			return null;
			}
	}	
	
	//varDecList (empty,list)
	final class varDecListEmptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("varDecList {empty} -> /* empty */\n");
		   }
			return null;
			}
	}
	final class varDecListNonemptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("varDecList {nonempty} -> varDecList varDec\n");
		   }
			return null;
			}
	}	
	
	//startMainBlock
	final class startMainBlockNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param)
		throws IOException, SyntaxException
		{
			//Show the reductions
			if (showReductions) 									
				System.out.println("\nReduced by rule: StartMainBlock -> /* empty */\n");
			symtab.startNewBlock();
		
			//We need to generate a start quad for the code
			//Initial start seems to be -1 from Lewis' example
			Quad quad = quadGen.makeStart(1);
			quadGen.addQuad(quad);
			
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
			//Show the reductions
			if (showReductions) 									
				System.out.println("\nReduced by rule: EndMainBlock -> /* empty */\n");
			symtab.endCurrentBlock();
		
			//We need to generate an end quad for the program
			Quad quad = quadGen.makeEnd();
			quadGen.addQuad(quad);
			
			quadGen.performFinalBackpatching();
			
			if(showQuads)
				quadGen.showQuads();
			
			//Return null value
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
			
			System.out.println("IdList {recurring} ->identifier lexeme: "+idLexeme+"\n");
			symtab.tempIdListAdd(idLexeme);
			return idLexeme;

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
			
			System.out.println("IdList {single} ->identifier lexeme: "+idLexeme+"\n");
			symtab.tempIdListClear();
			symtab.tempIdListAdd(idLexeme);
			
			return idLexeme;
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
				reportError("","Duplicate declaration in this block of "+nameToDefine);
				}
			}
			symtab.tempIdListClear();
			
         if (showReductions) {
            System.out.print(parser.token().line + ": ");
            System.out.println("varDec {idList} -> var idList colon scalarType semicolon\n");
         }
			
			// Return null value
			return null;
			}
	}
	final class varDecArrayIdListNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			int NanoSymbolTableTypeFlag = ((Integer) parser.rhsValue(3)).intValue();
			//int ArraySize = Integer.parseInt((String) parser.rhsValue(1));
			
			Iterator tempArrayIdListIterator = symtab.getTempIdListIterator();
			boolean notAlreadyDefined = true;
			String nameToDefine = "";
			int ArraySize = 0;
			while (tempArrayIdListIterator.hasNext())
			{			
				nameToDefine = (String)tempArrayIdListIterator.next();
				ArraySize = Integer.parseInt((String)tempArrayIdListIterator.next());
				if (symtab.addArrayToCurrentBlock(nameToDefine,NanoSymbolTableTypeFlag,ArraySize) == null)
					notAlreadyDefined = false;
				else
					notAlreadyDefined = true;
				if (!notAlreadyDefined)
				{
				reportError("","Duplicate declaration in this block of"+nameToDefine);
				}
			}
			symtab.tempIdListClear();
			
			if (showReductions) {
			   System.out.print(parser.token().line + ": ");
			   System.out.println("VarDec {arrayIdList} -> var arrayIdList colon scalarType semicolon\n");
			}
         
			// Return null value
			return null;
			}
	}
	
	
	//arrayIdList (list, single)
	final class arrayIdListListNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
				String idLexeme = (String) parser.rhsValue(0);
				String intConstLexeme = (String)parser.rhsValue(2);
				if (showReductions) 									
					System.out.println("\nReduced by rule: ArrayIdList {recurring} -> id lbracket intConst rbracket comma ArrayIdList");
				if (idLexeme==null) 
					return null; //discard error insertions
				if (showReductions) 									
					System.out.println("identifier lexeme: "+idLexeme+"\n");

				symtab.tempIdListAdd(idLexeme);
				//using alternating method...how icky can it be
            symtab.tempIdListAdd(intConstLexeme);
				
				// Return null value
				return null;

				}
	}
	final class arrayIdListSingleNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
				String idLexeme = (String) parser.rhsValue(0);
				String intConstLexeme = (String)parser.rhsValue(2);
			if (showReductions) 									
				System.out.println("\nReduced by rule: ArrayIdList {single} -> id lbracket intConst rbracket");
			if (idLexeme==null) 
				return null; //discard error insertions
			if (showReductions) 									
				System.out.println("identifier lexeme: "+idLexeme+"\n");
				symtab.tempIdListClear();
				symtab.tempIdListAdd(idLexeme);
				//using alternating method...how icky can it be
				symtab.tempIdListAdd(intConstLexeme);
	
			return intConstLexeme;
			}
	}
	
	//Add the three part proc stuff procDec, procHeader, procBody
	//procDec
	final class procDecNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   
         if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("procDec -> procHeader procBody");
			}
			
         //not sure what to return
			return parser.rhsValue(0);
			}
	}
	final class procHeaderNT extends NonterminalFactory
   {
      public Object makeNonterminal (Parser parser, int param) 
         throws IOException, SyntaxException
         {
         boolean notAlreadyDefined = true;
         String nameToDefine = "";
         int countNumberOfIds = ((Integer) parser.rhsValue(3)).intValue();
         nameToDefine = (String)parser.rhsValue(1);
         
         //need to get hashtable of formals
         Hashtable formalHashtable = new Hashtable();
         
         //if (symtab.addProcedureToSymbolTable(nameToDefine,countNumberOfIds,formalHashtable) == null)
         //   notAlreadyDefined = false;
         //else
            notAlreadyDefined = true;
         if (!notAlreadyDefined)
         {
         reportError("","Duplicate declaration in this block of "+nameToDefine);
         }
         
         if (showReductions) {
            System.out.print(parser.token().line + ": ");
            System.out.println("procHeader ->  procedure id lparen formalList rparen semicolon");
            String idString = (String) parser.rhsValue (1);
            System.out.println("proc id lexeme: " + idString + "\n");
         }
         
         return null;
         }
   }
	final class procBodyNT extends NonterminalFactory
   {
      public Object makeNonterminal (Parser parser, int param) 
         throws IOException, SyntaxException
         {
         
         if (showReductions) {
            System.out.print(parser.token().line + ": ");
            System.out.println("procBody ->  blockStmnt");
         }
         
         //this should be the last quad index for the statement
         return parser.rhsValue(0);
         }
   }
	
	//formalList (empty, list, single)
	//Rich: not sure what to do here for symtab
	final class formalListEmptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("formalList {empty} -> /* empty */\n");
		   }
		   //I think we need to return 0 so we have count of param being 0
			return 0;
			}
	}
	final class formalListListNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   int countNumberOfIds = ((Integer) parser.rhsValue(0)).intValue() + 
		   ((Integer) parser.rhsValue(2)).intValue();
		   
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("formalList {list} -> formal semicolon formalList\n");
		   }
			return countNumberOfIds;
			}
	}
	final class formalListSingleNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   int countNumberOfIds = ((Integer) parser.rhsValue(0)).intValue();
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("formalList {single} -> formal\n");
		   }
			return countNumberOfIds;
			}
	}
	
	//Rich: I added this like the one from var dec 
	//formal
	final class formalNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   int NanoSymbolTableTypeFlag = ((Integer) parser.rhsValue(2)).intValue();
         Iterator tempIdListIterator = symtab.getTempIdListIterator();
         boolean notAlreadyDefined = true;
         String nameToDefine = "";
         int countNumberOfIds = 0;
         while (tempIdListIterator.hasNext())
         {  
            countNumberOfIds++;
            nameToDefine = (String)tempIdListIterator.next();
            if (symtab.addScalarToCurrentBlock(nameToDefine,NanoSymbolTableTypeFlag) == null)
               notAlreadyDefined = false;
            else
               notAlreadyDefined = true;
            if (!notAlreadyDefined)
            {
            reportError("","Duplicate declaration in this procedure of"+nameToDefine);
            }
         }
         symtab.tempIdListClear();
         
         if (showReductions) {
            System.out.print(parser.token().line + ": ");
            System.out.println("formal -> idList colon scalarType\n");
			}
			
			return countNumberOfIds;
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
	
	final class arrayTypeIntegerNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			if (showReductions) 
				System.out.println("\nReduced by rule: ArrayType {integer} -> integer\n");

			//Return this as an object then up above pass it to symbol table as an Object
			return new Integer(NanoSymbolTable.INT_ARRAY_TYPE);
			}
	}
	
	final class arrayTypeBooleanNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			if (showReductions) 
				System.out.println("\nReduced by rule: ArrayType {boolean} -> boolean\n");

			//Return this as an object then up above pass it to symbol table as an Object
			return new Integer(NanoSymbolTable.BOOL_ARRAY_TYPE);
			}
	}
	
	//statement (blockStmnt, printStmnt, readStmnt, asgnStmnt, condStmnt, forStmnt, returnStmnt, callStmnt)
	
	final class statementBlockStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("statement {blockStmnt} -> blockStmnt\n");
		   }
		   //this should be the last quad index for the statement
         return parser.rhsValue(0);
			}
	}
	final class statementPrintStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("statement {printStmnt} -> printStmnt\n");
		   }
         //this should be the last quad index for the statement
         return parser.rhsValue(0);
			}
	}
	final class statementReadStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("statement {readStmnt} -> readStmnt\n");
		   }
         //this should be the last quad index for the statement
         return parser.rhsValue(0);
			}
	}
	final class statementAsgnStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("statement {asgnStmnt} -> asgnStmnt\n");
		   }
		   Integer lastQuadIndex = (Integer) parser.rhsValue(0);
         return lastQuadIndex;
			}
	}
	final class statementCondStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("statement {condStmnt} -> condStmnt\n");
		   }
		   Integer lastQuadIndex = (Integer) parser.rhsValue(0);
         return lastQuadIndex;
			}
	}
	final class statementForStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("statement {forStmnt} -> forStmnt\n");
		   }
		   Integer lastQuadIndex = (Integer) parser.rhsValue(0);
         return lastQuadIndex;
			}
	}
	final class statementReturnStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("statement {returnStmnt} -> returnStmnt\n");
		   }
		   Integer lastQuadIndex = (Integer) parser.rhsValue(0);
         return lastQuadIndex;
			}
	}
	final class statementCallStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
		      System.out.print(parser.token().line + ": ");
		      System.out.println("statement {callStmnt} -> callStmnt\n");
		   }
		   Integer lastQuadIndex = (Integer) parser.rhsValue(0);
         return lastQuadIndex;
			}
	}
	
	//blockStmnt
	final class blockStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			   if (showReductions) {
	   			System.out.print(parser.token().line + ": ");
	   			System.out.println("blockStmnt -> begin");
	   			System.out.println("              startNewBlock");
	   			System.out.println("              constDecList");
	   			System.out.println("              varDecList");
	   			System.out.println("              StatementList");
	   			System.out.println("              end");
	   			System.out.println("              endCurrentBlock");
	   			System.out.println("              showSymbolTable");
	   			System.out.println("              semicolon\n");
			   	}
			   Integer lastQuadIndex = (Integer) parser.rhsValue(4);
	         return lastQuadIndex;
			}
	}
	
	//startNewBlock
	final class startNewBlockNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
				if (showReductions) {
			      System.out.print(parser.token().line + ": ");
			      System.out.println("startNewBlock ->  /* empty */\n");
				}
				
				//Start new block in the symbol table
				symtab.startNewBlock();
				
				return null;
			}
	}
	
	//endCurrentBlock
	final class endCurrentBlockNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
				if (showReductions) {
					System.out.print(parser.token().line + ": ");
					System.out.println("endCurrentBlock ->   /* empty */\n");
				}
		   
			   //End the block in the symbol table
				symtab.endCurrentBlock();
				
				return null;
			}
	}
	
	//addConstQuads  -not sure what to do with this yet
   final class addConstQuadsNT extends NonterminalFactory
   {
      public Object makeNonterminal (Parser parser, int param) 
         throws IOException, SyntaxException
         {
            if (showReductions) {
               System.out.print(parser.token().line + ": ");
               System.out.println("addConstQuads ->   /* empty */\n");
            }
            
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
	
	final class StartMarkerNT extends NonterminalFactory
   {
      public Object makeNonterminal (Parser parser, int param)
      throws IOException, SyntaxException
      {
         if (showReductions)
            System.out.println("\nReduced by rule: StartMarker -> /* empty */\n");
         
         //Not sure what to do with this yet
         
         //if (showSymbolTable) 
         //   symtab.showContents();
      
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
				//Get the output string constant
				String outputString = (String) parser.rhsValue(2);
				
				//Make sure the output string exists
				if(outputString == null){
					reportError("","printStmnt() - No output string for print statement.");
					return null;
				}
				
				//Show the reductions
				if (showReductions) {
		   			System.out.print(parser.token().line + ": ");
		   			System.out.println("printStmnt -> print lparen stringConst printExprList rparen semicolon\n");
		   			System.out.println("string lexeme: " + outputString + "\n");
				}
				
				//Get the iterator for the expression list
				//Iterator is of type NSTIndEntry
				Iterator<NSTIndEntry> exprList = symtab.getTempExprListIterator();
				
				//If there is nothing in the expression list
				//Generate a standard print quad
				if(parser.rhsValue(3) == null){
					MemModQuad quad = quadGen.makePrint(-1, outputString);
					quadGen.addQuad(quad);
					return new Integer(quad.getQuadId());
				}
				
				//Determine if the stringConst is for a boolean or an integer or is invalid
				//Flags for boolean or integer type
				boolean isInteger = false;
				boolean isBoolean = false;
				
			    //Check the output string for either integer or boolean type
				if(outputString.equals("\"B\"")){
					isBoolean = true;
				} 
				if(outputString.equals("\"I\"")){
					isInteger = true;
				}
				
				//If the string is both boolean and integer
				if(isInteger && isBoolean){
					reportError("","printStmnt() - Problem in print statement. String cannot be both boolean and integer.");
					return null;
				}
				
				//If the string is neither boolean nor integer
				if(!(isInteger || isBoolean)){
					reportError("","printStmnt() - Problem in print statement. String is of invalid type.");
					return null;
				}
				
				//Check the expression list for immediate entries
				NSTIndEntry expr;
				MemModQuad quad = null;
				while(exprList.hasNext()){
					expr = exprList.next();
					//can't have immediate expressions
					if (expr.isImmediate()){
						reportError("","printStmnt() - Cannot print immediate value.");
						return null;
					}
					//We're not dealing with an array
					//if (expr.isScalar()){
						NSTIndScalarEntry entry = (NSTIndScalarEntry) expr;
						if ((expr.isBoolean()||expr.isBooleanArray()) && isBoolean){
							quad = quadGen.makePrint(entry.getAddress(), "B");
							quadGen.addQuad(quad);
						}
						else if ((expr.isInteger()||expr.isIntArray()) && isInteger){
							quad = quadGen.makePrint(entry.getAddress(), "I");
							quadGen.addQuad(quad);
						}
						else{
							reportError("","printStmnt() - String expression type and expression do not match.");
						}
					//}
					//We're dealing with an array - this does not make a difference right now
					/*else{
						NSTIndArrayEntry entry = (NSTIndArrayEntry) expr;
						if (expr.isBooleanArray() && isBoolean){
							quad = quadGen.makePrint(entry.getAddress(), "B");
							quadGen.addQuad(quad);
						}
						else if (expr.isIntArray() && isInteger){
							quad = quadGen.makePrint(entry.getAddress(), "I");
							quadGen.addQuad(quad);
						}
						else{
							reportError("","printStmnt() - String expression type and expression do not match.");
						}
					}*/
					
				}
				
				//need to return the last quad's ID
				return new Integer(quad.getQuadId());
				
			}
	}

	//printExprList (empty, nonempty)
	final class printExprListNonemptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			   	//Get the expression
				NSTIndEntry expr = (NSTIndEntry) parser.rhsValue(2);
				
				//If the expression is null
				if(expr == null){
					reportError("","printExprListNonemptyNT() - Expression cannot be empty.");
					return null;
				}
			   
			   //Show the reductions
			   if (showReductions) {
	   			System.out.print(parser.token().line + ": ");
	   			System.out.println("printExprList {nonempty} -> printExprList comma expr\n");
			   }
			   
			   
			   //can't have an immediate expression in a print
			   if(expr.isImmediate()){
				   reportError("","printExprListNonemptyNT() - Cannot print an immediate expression");
				   return null;
			   }
			   
			   symtab.tempExprListAdd(expr);
			   
			   return expr;
			}
	}
	
	final class printExprListEmptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			   if (showReductions) {
	   			System.out.print(parser.token().line + ": ");
	   			System.out.println("printExprList {empty} -> /* empty */\n");
			   }
			   
			   symtab.tempExprListClear();
			   
				return null;
			}
	}	
	
	//readStmnt
	final class readStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
				//Get the string constant
				String stringConst = (String) parser.rhsValue(2);
				
				//Check to make sure there's a stringConst
				if(stringConst == null){
					reportError("","readStmntNT() - Read has no string constant.");
					return null;	
				}
				
				//Print the reductions
				if (showReductions) {
		   			System.out.print(parser.token().line + ": ");
		   			System.out.println("readStmnt -> read lparen stringConst inputTargetList rparen semicolon;");
		   			System.out.println("string lexeme: " + stringConst + "\n");
				}
				
				//flags for input type
				boolean isBoolean = false;
				boolean isInteger = false;
				
				//Check if boolean
				if(stringConst.equals("\"B\"")){
					isBoolean = true;
				}
				//Check if integer
				else if(stringConst.equals("\"I\"")){
					isInteger = true;
				}
				//If neither it is an error
				else
				{
					reportError("","readStmntNT() - Invalid string for read statement.");
					return null;
				}
				//If both, it is an error
				if(isBoolean && isInteger){
					reportError("","readStmntNT() - String cannot be both boolean and integer type.");
					return null;
				}
				
				//Get the targetList to check against the string
				Iterator<NSTIndEntry> targetList = symtab.getTempTargetListIterator();

				//Process the targetList
				NSTIndEntry target;
				MemModQuad quad = null;
				while(targetList.hasNext()){
					target = (NSTIndEntry) targetList.next();
					//Can't assign to an immediate
					if(target.isImmediate()){
						reportError("","readStmnt() - Cannot read to immediate value");
						return null;
					}
					//Can't assign to a constant
					if(target.isConstant()){
						reportError("","readStmnt() - Cannot read to constant variable");
						return null;
					}
					//We're not dealing with an array
					//if (target.isScalar()){
						NSTIndScalarEntry entry = (NSTIndScalarEntry) target;
						if (target.isBoolean() && isBoolean){
							quad = quadGen.makeRead(entry.getAddress(), "B");
							quadGen.addQuad(quad);
						}
						else if (target.isInteger() && isInteger){
							quad = quadGen.makeRead(entry.getAddress(), "I");
							quadGen.addQuad(quad);
						}
						else{
							reportError("","printStmnt() - String expression type and expression do not match.");
							return null;
						}
					//}
					//We're dealing with an array
					/*else{
						NSTIndArrayEntry entry = (NSTIndArrayEntry) target;
						if (target.isBooleanArray() && isBoolean){
							quad = quadGen.makeRead(entry.getAddress(), "B");
							quadGen.addQuad(quad);
						}
						else if (target.isIntArray() && isInteger){
							quad = quadGen.makeRead(entry.getAddress(), "I");
							quadGen.addQuad(quad);
						}
						else{
							reportError("","printStmnt() - String expression type and expression do not match.");
							return null;
						}
					}*/	
				}
				
				//need to return the last quad's ID
				return new Integer(quad.getQuadId());
			}
	}
	
	//inputTargetList (empty, nonempty)
	final class inputTargetListNonemptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
				NSTIndEntry inputTarget = (NSTIndEntry) parser.rhsValue(2);
				
				if(inputTarget == null){
					reportError("","inputTargetListNonempty() - input target cannot be null");
					return null;
				}
			
				if (showReductions) {
		   			System.out.print(parser.token().line + ": ");
		   			System.out.println("inputTargetList {nonempty} -> inputTargetList comma inputTarget\n");
				}
				
				if(inputTarget.isConstant()){
					reportError("","inputTargetListNonempty() - cannot assign to constant variable");
					return null;
				}
				
				if(inputTarget.isImmediate()){
					reportError("","inputTargetListNonempty() - cannot assign to immediate value");
					return null;
				}
				
				symtab.tempTargetListAdd(inputTarget);
				
				return inputTarget;
			}
	}
	
	final class inputTargetListEmptyNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			   if (showReductions) {
	   			System.out.print(parser.token().line + ": ");
	   			System.out.println("inputTargetList {empty} -> /* empty */\n");
			   }
			   
			   symtab.tempTargetListClear();
			   
			   return null;
			}
	}
	
	//inputTarget (id, idArray)
	final class inputTargetIdNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
				//Get the id string
				String idString = (String) parser.rhsValue (0);
				
				//Check to make sure string isn't null
				if(idString == null){
					reportError("","inputTargetIdNT() - Invalid id for inputTarget.");
					return null;
				}
			
				//Show the reductions
			    if (showReductions) {
		   			System.out.print(parser.token().line + ": ");
		   			System.out.println("inputTarget {id} -> id");
		   			System.out.println("identifier lexeme: " + idString + "\n");
			    }
			   
			    //Find the id in the symbol table
			    NSTIndEntry idEntry = (NSTIndEntry) symtab.get(idString);
			    
			    //If it isn't in the symbol table
			    if(idEntry == null){
			    	reportError("","inputTargetIdNT() - Identifier not declared in scope.");
			    	return null;
			    }
			    
			    //Can't assign to a constant variable
			    if(idEntry.isConstant()){
			    	reportError("","inputTargetIdNT() - Can't assign to constant variable");
			    	return null;
			    }
			    
			    //Can't have an immediate entry here
			    if(idEntry.isImmediate()){
			    	reportError("","inputTargetIdNT() - Cannot read to immediate entry.");
			    	return null;
			    }
			    
			    //Cast to NSTIndScalarEntry
			    NSTIndScalarEntry entry = (NSTIndScalarEntry) idEntry;
			    
			    if(entry == null){
			    	reportError("","inputTargetIdNT() - Entry in symbol table was not of correct type");
			    	return null;
			    }
			    
			    return entry;
			}
	}
	final class inputTargetIdArrayNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			
				
				
				//Show the reductions
   		   if (showReductions) {
   	   		System.out.print(parser.token().line + ": ");
   	   		System.out.println("inputTarget {idArray} -> id lbracket expr rbracket\n");
   		   }
			    
			    /*
			    
			    //Get the id string
            String idString = (String) parser.rhsValue (0);
            
            //Check to make sure idString is valid
            if(idString == null){
               reportError("","inputTargetIdArrayNT() - Id not declared in this scope.");
               return null;
            }
			    
			    //Get the expression for the index
			    NSTIndEntry exprEntry = (NSTIndEntry) parser.rhsValue(2);
			    
			    //Check to make sure expression exists
			    if(exprEntry == null){
			    	reportError("","inputTargetIdArrayNT() - Array index expression is invalid.");
			    	return null;
			    }
			    
			    //Something is wrong, expressions can't be immediates as they are temp variables in the block
			    if(exprEntry.isImmediate()){
			    	 NSTIndImmediateEntry expr = (NSTIndImmediateEntry) exprEntry;
			    }
			    if(exprEntry.isScalar()){
			    	 NSTIndScalarEntry expr = (NSTIndScalarEntry) exprEntry;
			    }
			    
			    //Cast the expression to a more specific NSTIndScalarEntry type
			    NSTIndScalarEntry expr = (NSTIndScalarEntry) exprEntry;
			   
			    //Find the id in the symbol table
			    NSTIndEntry idEntry = (NSTIndEntry) symtab.get(idString);
			    
			    //If it isn't in the symbol table
			    if(idEntry == null){
			    	reportError("","inputTargetIdArrayNT() - Identifier not declared in scope.");
			    	return null;
			    }
			    
			    //Can't have an immediate entry here
			    if(idEntry.isImmediate()){
			    	reportError("","inputTargetIdArrayNT() - Cannot read to immediate entry.");
			    	return null;
			    }
			    
			  //Can't assign to a constant variable
			    if(idEntry.isConstant()){
			    	reportError("","inputTargetIdArrayNT() - Can't assign to constant variable");
			    	return null;
			    }
			    
			    //Create an entry to return
			    NSTIndArrayEntry entry = (NSTIndArrayEntry) idEntry;
			    
			    if(entry == null){
			    	reportError("","inputTargetIdArrayNT() - Entry in symbol table was not of the correct type.");
			    	return null;
			    }
			    
			    return entry;
			    */
			    
   		   //get the array id and the index offset
	         NSTIndArrayEntry array = (NSTIndArrayEntry)symtab.get((String)parser.rhsValue(0));
	         NSTIndEntry indexExpr = (NSTIndEntry)parser.rhsValue(2);
	         MemModQuad indexCalcQuad = null;
	         
	         //make sure array is valid id and is an array.  
	         //Check that the index is an integer
	         if (array==null) 
	         {
	            reportError("","Array identifier not found in scope");
	            return null; 
	         }
	         else if (array.isScalar())
	         {
	            reportError("","Attempt to use scalar identifier as array base address");
	            return null;
	         }
	         else if (!indexExpr.isInteger())
	         {
	            reportError("","Non-integer index in array element assignment");
	            return null;
	         }
	         
	         //make an indexCalcQuad to calc the index offset
	         //store the result in a tmpIndex symbol table entry
	         if (indexExpr.isImmediate())
	         {
	            NSTIndImmediateEntry immIndex = (NSTIndImmediateEntry) indexExpr;
	            NSTIndScalarEntry tmpIndex = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.INT_TYPE);
	            indexCalcQuad = quadGen.makeOffsetImmediate(tmpIndex.getAddress(),          
	                                 array.getAddress(), immIndex.getIntValue()); 
	         }
	         else if (indexExpr.isScalar())
	         {
	            NSTIndScalarEntry calculatedIndex = (NSTIndScalarEntry) indexExpr;
	            NSTIndScalarEntry tmpIndex = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.INT_TYPE);
	            indexCalcQuad = quadGen.makeOffsetRegular(tmpIndex.getAddress(), 
	                           array.getAddress(), calculatedIndex.getAddress());
	         }
	         
	         quadGen.addQuad(indexCalcQuad);
	         //make a new symbol table entry for the array location with the offset
	         return symtab.new NSTIndScalarEntry(array.getName(), array.getActualType(), false, indexCalcQuad.getResultAddress()); 
	         
			}
	}
	
	//asgnStmnt (int, intArray)
	final class asgnStmntIntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			//Get the id name and check for null
			String idLexeme = (String) parser.rhsValue(0);
		    if (idLexeme==null) 
		    	return null;
			
		    //Show the reductions
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("asgnStmnt {int} -> id assign expr semicolon");
   			String idString = (String) parser.rhsValue (0);
   			System.out.println("identifier lexeme: " + idLexeme + "\n");
		   }
	      
		   //Get the expression entry from the parsr
	      NSTIndEntry e = (NSTIndEntry)parser.rhsValue(2);
	      
	      //Get the symbol table entry for the identifier
	      NSTIndScalarEntry i = (NSTIndScalarEntry) symtab.get(idLexeme);
	      
	      //If the symbol table doesn't contain an entry for id
	      if (i==null){
	    	  reportError("","Id not defined in this scope.");
	    	  return null;
	      //If the id and expression types don't match
	      }else if (e.getActualType()!=i.getActualType()){
	         reportError("","Type mismatch in assignment statement");
	         return null;
	      //If the value we're trying to assign to is constant, a no-no
	      }else if (i.isConstant()){
	         reportError("","Attempt to assign to a constant identifier");
	         return null;
	      
	         
	      //Otherwise we passed the tests, so let's move on
	      }else{
	    	 //If our value to assign is immediate
	         if (e.isImmediate())
	         {
	            NSTIndImmediateEntry imm = (NSTIndImmediateEntry) e;
	            //If the assignment value is a boolean
	            if (imm.isBoolean())
	            {
	               MemModQuad aqb = quadGen.makeAssignImmediateBoolean(i.getAddress(),imm.getBoolValue());
	               quadGen.addQuad(aqb);
	               return new Integer(aqb.getQuadId());
	            }
	            //If the assignment value is an integer
	            else if (imm.isInteger())
	            {
	               MemModQuad aqi = quadGen.makeAssignImmediateInteger(i.getAddress(),imm.getIntValue());
	               quadGen.addQuad(aqi);
	               return new Integer(aqi.getQuadId());
	            //Otherwise we messed up
	            }else {
	            	reportError("","Compiler developer: invalid type of immediate assignment");
	            	return null;
	            }
	            //We messed up but in a different way
	         	}else if (!e.isScalar()){
	         		reportError("","Attempt to assign non-scalar memory to scalar identifier");
	         		return null;
	         	//Value is a scalar, so everything's cool
	         	}else{
	            NSTIndScalarEntry es = (NSTIndScalarEntry) e;
	            MemModQuad aqr = quadGen.makeAssignRegular(i.getAddress(),es.getAddress());
	            quadGen.addQuad(aqr);
	            return new Integer(aqr.getQuadId());
	         	}
	      	}
		}
	}
	final class asgnStmntIntArrayNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			//Get the id name and check for null
			String idLexeme = (String) parser.rhsValue(0);
		    if (idLexeme==null) 
		    	return null;
		    
			//Show the reductions
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("asgnStmnt {intArray} -> id lbracket expr rbracket assign expr semicolon");
   			System.out.println("identifier lexeme: " + idLexeme + "\n");
		   }
		   
		  //Set the type flag
	      int typeFlag = NanoSymbolTable.UNK_TYPE;
	      
	      //Get the expression for the array index
	      NSTIndEntry indexExpr = (NSTIndEntry)parser.rhsValue(2);
	      
	      //Lookup the identifier in the symbol table
	      NSTIndArrayEntry array = (NSTIndArrayEntry) symtab.get(idLexeme);
	      
	      //Get the value expression
	      NSTIndEntry e2 = (NSTIndEntry) parser.rhsValue(5);
	      
	      //Create some temporary storage for values
	      NSTIndScalarEntry valToAssign = null; 
	      NSTIndImmediateEntry immToAssign = null;
	      MemModQuad indexCalcQuad = null;
	      
	      //Flag for immedate value in value expression
	      boolean isImmediateValToAssign = false;
	      
	      //If the value expression doesn't exist
	      if (e2==null) 
	    	  return null;
	      
	      //If the value is scalar and not immediate
	      if ((e2.isScalar() || e2.isIntArray())&& !e2.isImmediate()) 
	      {
	         valToAssign = (NSTIndScalarEntry) e2;
	         isImmediateValToAssign = false;
	      }
	      
	      //If the value is a scalar and is immediate
	      else if ((e2.isScalar() || e2.isIntArray())&& e2.isImmediate()) 
	      {
	         immToAssign = (NSTIndImmediateEntry) e2;
	         isImmediateValToAssign = true;
	      }
	      
	      //If the array identifier does not exist in the symbol table
	      if (array==null) 
	      {
	         reportError("","Array identifier not found in scope");
	         return null; 
	      }
	      
	      //If the array identifier is not actually an integer array
	      else if (!array.isIntArray() && !array.isBooleanArray())
	      {
	         reportError("","Attempt to use scalar identifier as array base address");
	         return null;
	      }
	      
	      //If the array index expression doesn't contain a value
	      else if (!indexExpr.isInteger()&&!indexExpr.isIntArray())
	      {
	         reportError("","Non-integer index in array element assignment");
	         return null;
	      }
	      
	      //If the value to assign is not immedate , the array is a boolean array, and the value is a boolean
	      if ( !isImmediateValToAssign && array.isBooleanArray() && valToAssign.isBoolean() )
	      {
	         typeFlag = NanoSymbolTable.BOOL_TYPE;
	      }
	      
	      //If the balue to assign is not imediate, the array is an integer array, and the value is an integer
	      else if ( !isImmediateValToAssign && array.isIntArray() && valToAssign.isInteger() )
	      {
	         typeFlag = NanoSymbolTable.INT_TYPE;
	      }
	      
	      //If the value to assign is an immediate integer, and the array is an int array
	      else if ( isImmediateValToAssign && array.isIntArray() && immToAssign.isInteger() )
	      {
	         typeFlag = NanoSymbolTable.INT_TYPE;
	      }
	      //If the value to assign is an immediate boolean, and the array is a boolean array
	      else if ( isImmediateValToAssign && array.isBooleanArray() && immToAssign.isBoolean() )
	      {
	         typeFlag = NanoSymbolTable.BOOL_TYPE;
	      }
	      
	      //Calculate the memory location to modify
	      if (indexExpr.isImmediate())
	      {
	         NSTIndImmediateEntry immIndex = (NSTIndImmediateEntry) indexExpr;
	         NSTIndScalarEntry tmpIndex = (NSTIndScalarEntry) symtab.addNewTempToCurrentBlock(NanoSymbolTable.INT_TYPE);
	         indexCalcQuad = quadGen.makeOffsetImmediate(tmpIndex.getAddress(), array.getAddress(), immIndex.getIntValue()); 
	      }
	      //Calculate the address of the index we're using
	      else if (indexExpr.isScalar() || indexExpr.isIntArray())
	      {
	         NSTIndScalarEntry calculatedIndex = (NSTIndScalarEntry) indexExpr;
	         NSTIndScalarEntry tmpIndex = (NSTIndScalarEntry) symtab.addNewTempToCurrentBlock(NanoSymbolTable.INT_TYPE);
	         indexCalcQuad = quadGen.makeOffsetRegular(tmpIndex.getAddress(), array.getAddress(), calculatedIndex.getAddress());
	      }
	      else {
	         reportError("","Unknown address in array");
            return null;
	      }
	     
	      //Add the index quad
	      quadGen.addQuad(indexCalcQuad);
	      
	      //Use the immediate value flag
	      if (isImmediateValToAssign)
	      {
	    	 //If it's an integer type, make an integer assignment quad
	         if (typeFlag==NanoSymbolTable.INT_TYPE)
	         {
	            MemModQuad immassgnIntQuad = quadGen.makeAssignImmediateInteger(indexCalcQuad.getResultAddress(),immToAssign.getIntValue() );
	            quadGen.addQuad(immassgnIntQuad);
	            return new Integer(immassgnIntQuad.getQuadId());
	         }
	         //If it's a boolean type, make a boolean assignment quad
	         else if (typeFlag==NanoSymbolTable.BOOL_TYPE)
	         {
	            MemModQuad immassgnBoolQuad = quadGen.makeAssignImmediateBoolean(indexCalcQuad.getResultAddress(),immToAssign.getBoolValue() );
	            quadGen.addQuad(immassgnBoolQuad);
	            return new Integer(immassgnBoolQuad.getQuadId());
	         }
	         //Otherwise we screwed up royally
	         else
	         {
	            reportError("","Some unknown use of type in array elt assignment");
	            return null;
	         }
	      }
	      //value is not immediate; use regular assignment, types already checked
	      else
	      {
	         MemModQuad aq = quadGen.makeAssignRegular(indexCalcQuad.getResultAddress(), valToAssign.getAddress());
	         quadGen.addQuad(aq);
	         return new Integer(aq.getQuadId());
	      }

		}
	}
	
	public final class NanoCondUnmatched extends NonterminalFactory
	{
	   public Object makeNonterminal (Parser parser, int param)
	   throws IOException, SyntaxException
	   {
	      if (showReductions) System.out.println(
	         "\nReduced by rule: Cond {unmatched} -> CondIfPart CondThenPartUM");
	      InstrModQuad imq = (InstrModQuad) parser.rhsValue(0);
	      if (imq==null)
	      {
	         return null;
	      }
	      else
	      {
	         String jumpQuadLabel = imq.getBackpatchQuadLabel();
	         //A statement, whether single or block, should return the index of the
	         //last quad produced for it (single--the index, block--the last index)
	         Integer lastQuadIndex = (Integer) parser.rhsValue(1);
	         quadGen.updateBackpatching(jumpQuadLabel, lastQuadIndex.intValue()+1);
	         return lastQuadIndex;
	      }
	   }
	}

	public final class NanoCondMatched extends NonterminalFactory
	{
	   public Object makeNonterminal (Parser parser, int param)
	   throws IOException, SyntaxException
	   {
	      if (showReductions) System.out.println(
	         "\nReduced by rule: Cond {matched} -> CondIfPart CondThenPartM");
	      InstrModQuad imq = (InstrModQuad) parser.rhsValue(0);
	      if (imq==null)
	      {
	         return null;
	      }
	      else
	      {
	         String jumpQuadLabel = imq.getBackpatchQuadLabel();
	         //A statement, whether single or block, should return the index of the
	         //last quad produced for it (single--the index, block--the last index)
	         Integer lastQuadIndex = (Integer) parser.rhsValue(1);
	         
	         //quadGen.updateBackpatching(jumpQuadLabel, lastQuadIndex.intValue()+2);
	         return lastQuadIndex;
	      }
	   }
	}

	public final class NanoCondIfPart extends NonterminalFactory
	{
	   public Object makeNonterminal (Parser parser, int param)
	      throws IOException, SyntaxException
	   {
	      if (showReductions) System.out.println(
	            "\nReduced by rule: CondIfPart -> if Expr");
	      NSTIndEntry e = (NSTIndEntry) parser.rhsValue(1);
	      if (e==null)
	      {
	         return null;
	      }
	      else if (!e.isBoolean())
	      {
	         reportError("","Conditional expression in if/then not boolean");
	         return null;
	      }
	      else
	      {
	         if (e.isImmediate())
	         {
	            NSTIndImmediateEntry imm = (NSTIndImmediateEntry) e;
	            InstrModQuad iqi = quadGen.makeIfFalseImmediate(-1, imm.getBoolValue());
	            quadGen.addQuad(iqi);
	            return iqi;
	         }
	         else if (!e.isImmediate()) //Should perhaps actually be checking for other mistakes
	         {
	            NSTIndScalarEntry es = (NSTIndScalarEntry) e;
	            InstrModQuad iqs = quadGen.makeIfFalseRegular(-1, es.getAddress());
	            quadGen.addQuad(iqs);
	            return iqs;
	         }
	         else return null;
	      }
	   }
	}

	public final class NanoCondThenPartUM extends NonterminalFactory
	{
	   public Object makeNonterminal (Parser parser, int param)
	      throws IOException, SyntaxException
	   {
	      if (showReductions) System.out.println(
	      "\nReduced by rule: CondThenPartUM -> then Statement");
	      
	      Integer lastQuadIndex = (Integer) parser.rhsValue(1);
	      if (lastQuadIndex==null)
	      {
	         reportError("","Compiler developer (CondUM): Statement not passing up last index");
	         return null;
	      }
	      else return lastQuadIndex;
	   }
	}

	public final class NanoCondThenPartM extends NonterminalFactory
	{
	   public Object makeNonterminal (Parser parser, int param)
	      throws IOException, SyntaxException
	   {
	      if (showReductions) System.out.println(
	      "\nReduced by rule: CondThenPartM -> then Statement else CondElseJump Statement");
	      
	      Integer lastStmtQuadIndex = (Integer) parser.rhsValue(1);
	      if (lastStmtQuadIndex==null)
	      {
	         reportError("","Compiler developer (CondM): Statement not passing up last index");
	         return null;
	      }
	      else 
	      {
	         InstrModQuad imq = (InstrModQuad) parser.rhsValue(3);
	         if (imq==null)
	         {
	            reportError("","Compiler developer: no quad passed up from CondElseJump");
	            return null;
	         }
	         else
	         {
	            String jumpQuadLabel = imq.getBackpatchQuadLabel();
	            //A statement, whether single or block, should return the index of the
	            //last quad produced for it (single--the index, block--the last index)
	            Integer lastElseQuadIndex = (Integer) parser.rhsValue(4);
	            quadGen.updateBackpatching(jumpQuadLabel, lastElseQuadIndex.intValue()+1);
	            return lastStmtQuadIndex; //To be used for the previous backpatching
	         }
	      }
	   }
	}

	public final class NanoCondElseJump extends NonterminalFactory
	{
	   public Object makeNonterminal (Parser parser, int param)
	      throws IOException, SyntaxException
	   {
	      if (showReductions) System.out.println(
	      "\nReduced by rule: CondElseJump -> /* empty */");
	      InstrModQuad gotoq = quadGen.makeUnconditionalJump(-1);
	      quadGen.addQuad(gotoq);
	      return gotoq;
	   }
	}
   
   //for statement
   final class forStmntNT extends NonterminalFactory
   {
      public Object makeNonterminal (Parser parser, int param) 
         throws IOException, SyntaxException
         {
         if (showReductions) {
            System.out.print(parser.token().line + ": ");
            System.out.println("forStmnt -> forHeader statement");
         }
         
         //Use the passed up quad to get the address of the for loop id
         MemModQuad assgForStart = (MemModQuad) parser.rhsValue(0);
         int forIdAddress = assgForStart.getResultAddress();
         
         //create the incrementing quad
         int amountToIncrement = 1;
         MemModQuad incrementForCounterQuad;
         incrementForCounterQuad = quadGen.makeAddRightImmediate(forIdAddress, 
               forIdAddress, amountToIncrement);
         quadGen.addQuad(incrementForCounterQuad);
         
         //create jump quad one index past the assign quad passed up from below
         InstrModQuad jumpToStartofFor;
         jumpToStartofFor = quadGen.makeUnconditionalJump(assgForStart.getQuadId()+1);
         quadGen.addQuad(jumpToStartofFor);
         
         int quadIndexforIfTrue = assgForStart.getQuadId() + 2;
         InstrModQuad imq = (InstrModQuad)quadGen.getQuadList().get(quadIndexforIfTrue);
         String ifTrueLabel = imq.getBackpatchQuadLabel();
         Integer lastQuadIndex = (Integer) parser.rhsValue(1);
    
         if (lastQuadIndex==null)
         {
            reportError("","Compiler developer(For Stmt): Statement not passing up last index");
            return null;
         }
         else {
            quadGen.updateBackpatching(ifTrueLabel, lastQuadIndex.intValue()+3);
            return new Integer(jumpToStartofFor.getQuadId());
         }
         }
   }
   
   //for Header
   final class forHeaderNT extends NonterminalFactory
   {
      public Object makeNonterminal (Parser parser, int param) 
         throws IOException, SyntaxException
         {
         if (showReductions) {
            System.out.print(parser.token().line + ": ");
            System.out.println("forHeader -> for id assign expr to expr do ");
            String idString = (String) parser.rhsValue (1);
            System.out.println("identifier lexeme: " + idString + "\n");
         }
         
         NSTIndScalarEntry i = (NSTIndScalarEntry)symtab.get((String)parser.rhsValue(1));
         if (i==null)
         {
            reportError("","For statement Identifier not recognized.");
            return null; 
         }
         
         if (!i.isInteger()) {
            reportError("","For statement Identifier not an integer.");
            return null;
         }
         
         //Get the left and right expressions
         NSTIndEntry eLeft = (NSTIndEntry) parser.rhsValue(3);
         NSTIndEntry eRight = (NSTIndEntry) parser.rhsValue(5);
         
         if (eLeft == null || eRight==null) return null;
         
         //quad generator does not allow boolean arguments in for statements
         if (eLeft.isBoolean() || eRight.isBoolean()) {
            reportError("","Invalid for statement ranges - must be integers.");
            return null;
         }
         
         //need to check range for for loops and going up or down
         
         //push quad to assign the starting index to the for loop
         MemModQuad assgForStart;
         if (eLeft.isImmediate())
         {
            NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) eLeft;
            assgForStart = quadGen.makeAssignImmediateInteger(i.getAddress(), eLeftImm.getIntValue());
         }
         else if (eLeft.isScalar() || eLeft.isIntArray())
         {
            NSTIndScalarEntry eLeftScalar = (NSTIndScalarEntry) eLeft;
            assgForStart = quadGen.makeAssignRegular(i.getAddress(), eLeftScalar.getAddress());
         }
         else {
            reportError("","Invalid for left statement range");
            return null;
         }
         quadGen.addQuad(assgForStart);
         
         //make a quad to evaluate the current index count against the end of the loop counter
         MemModQuad relopForQuad;
         NSTIndScalarEntry tmpForCountRelop = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.BOOL_TYPE);
         if (eRight.isImmediate())
         {
            NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) eRight;
            relopForQuad = quadGen.makeRelopGreaterThanRightImmediate(
                  tmpForCountRelop.getAddress(),i.getAddress(), eRightImm.getIntValue());
         }
         else if (eRight.isScalar()|| eRight.isIntArray())
         {
            NSTIndScalarEntry eRightScalar = (NSTIndScalarEntry) eRight;
            relopForQuad = quadGen.makeRelopGreaterThanRegular(
                  tmpForCountRelop.getAddress(),i.getAddress(), eRightScalar.getAddress());
         }
         else {
            reportError("","Invalid for right statement ranges");
            return null;
         }
         quadGen.addQuad(relopForQuad);
         
         //if for loop is done then goto past the return jump quad
         InstrModQuad testLoopEndQuad;
         testLoopEndQuad = quadGen.makeIfTrueRegular(-1, tmpForCountRelop.getAddress());
         quadGen.addQuad(testLoopEndQuad);
         
         return assgForStart;
         
         }
   }
   
	//returnStmnt
	final class returnStmntNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("returnStmnt -> return semicolon\n");
		   }
		   
		   MemModQuad returnQuad = null;
		   
		   return new Integer(returnQuad.getQuadId());
			}
	}
	
	
	
	//exprList (list, single)
	final class exprListListNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("exprList {list} -> expr comma exprList\n");
		   }
		   
		   NSTIndEntry expr = (NSTIndEntry)parser.rhsValue(0);
         if (expr==null) {return null; }
         
         int countNumberofParams = ((Integer) parser.rhsValue(2)).intValue() + 1;
         
         
         MemModQuad paramQuad;
         MemModQuad immedTransferQuad;
         
         //int topStack = symtab.getStackTopOffset();
         
         if (expr.isImmediate() && expr.isBoolean())
         {
            NSTIndImmediateEntry immExpr = (NSTIndImmediateEntry) expr;
            NSTIndScalarEntry tmpExpr = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.BOOL_TYPE);
            immedTransferQuad = quadGen.makeAssignImmediateBoolean(tmpExpr.getAddress(), immExpr.getBoolValue());
         //   paramQuad = quadGen.makeParam(topStack, tmpExpr.getAddress());
            quadGen.addQuad(immedTransferQuad);
         }
         else if (expr.isImmediate() && expr.isInteger())
         {
            NSTIndImmediateEntry immExpr = (NSTIndImmediateEntry) expr;
            NSTIndScalarEntry tmpExpr = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.INT_TYPE);
            immedTransferQuad = quadGen.makeAssignImmediateInteger(tmpExpr.getAddress(), immExpr.getIntValue());
         //   paramQuad = quadGen.makeParam(topStack, tmpExpr.getAddress());
            quadGen.addQuad(immedTransferQuad);
         }
         else if (expr.isScalar())
         {
            NSTIndScalarEntry scalarExpr = (NSTIndScalarEntry) expr;
         //   paramQuad = quadGen.makeParam(topStack, scalarExpr.getAddress());
         }
         else
         {
            reportError("","Invalid procedure call.");
            return null;
         }
         
         //quadGen.addQuad(paramQuad);
         //not sure if this is the right thing to return
         return countNumberofParams;
			}
	}
	final class exprListSingleNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		  
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("exprList {single} -> expr\n");
		   }
		   
		   NSTIndEntry expr = (NSTIndEntry)parser.rhsValue(0);
         if (expr==null) {return null; }
         
         MemModQuad paramQuad;
         MemModQuad immedTransferQuad;
         
         //int topStack = symtab.getStackTopOffset();
         
         if (expr.isImmediate() && expr.isBoolean())
         {
            NSTIndImmediateEntry immExpr = (NSTIndImmediateEntry) expr;
            NSTIndScalarEntry tmpExpr = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.BOOL_TYPE);
            immedTransferQuad = quadGen.makeAssignImmediateBoolean(tmpExpr.getAddress(), immExpr.getBoolValue());
         //   paramQuad = quadGen.makeParam(topStack, tmpExpr.getAddress());
            quadGen.addQuad(immedTransferQuad);
         }
         else if (expr.isImmediate() && expr.isInteger())
         {
            NSTIndImmediateEntry immExpr = (NSTIndImmediateEntry) expr;
            NSTIndScalarEntry tmpExpr = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.INT_TYPE);
            immedTransferQuad = quadGen.makeAssignImmediateInteger(tmpExpr.getAddress(), immExpr.getIntValue());
         //   paramQuad = quadGen.makeParam(topStack, tmpExpr.getAddress());
            quadGen.addQuad(immedTransferQuad);
         }
         else if (expr.isScalar())
         {
            NSTIndScalarEntry scalarExpr = (NSTIndScalarEntry) expr;
         //   paramQuad = quadGen.makeParam(topStack, scalarExpr.getAddress());
         }
         else
         {
            reportError("","Invalid procedure call.");
            return null;
         }
         
         //quadGen.addQuad(paramQuad);
         //not sure if this is the right thing to return
         return 1;
			}
	}
	
	//expr (plus, minus, or, term)
	final class exprPlusNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("expr {plus} -> expr plus term\n");
		   }
		   
		   //get expr plus term left and right hand sides
         NSTIndEntry leftExpr = (NSTIndEntry) parser.rhsValue(0);
         NSTIndEntry rightTerm = (NSTIndEntry) parser.rhsValue(2);
         
         //check if null
         if (leftExpr == null || rightTerm==null) return null;     
         
         //check if not integers
         if ((!leftExpr.isInteger()&& !leftExpr.isIntArray())||
               (!rightTerm.isInteger()&& !rightTerm.isIntArray())) {
            reportError("","Invalid addition operation arguements - must be integer.");
            return null;
         }
         
         //declare the quad and get temp symtab address
         MemModQuad exprPlusQuad;
         NSTIndScalarEntry tmpExprPlusResult = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.INT_TYPE);
         
         //make quads depending on immediate values
         if (leftExpr.isImmediate() && rightTerm.isImmediate())
         {
            NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) leftExpr;
            NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) rightTerm;
            exprPlusQuad = quadGen.makeAddBothImmediate(tmpExprPlusResult.getAddress(),          
                  eLeftImm.getIntValue(), eRightImm.getIntValue());
         }
         else if (leftExpr.isImmediate() && !rightTerm.isImmediate())
         {
            NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) leftExpr;
            NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) rightTerm;
            exprPlusQuad = quadGen.makeAddLeftImmediate(tmpExprPlusResult.getAddress(),          
                  eLeftImm.getIntValue(), eRightImm.getAddress());
         }
         else if (!leftExpr.isImmediate() && rightTerm.isImmediate())
         {
            NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) leftExpr;
            NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) rightTerm;
            exprPlusQuad = quadGen.makeAddRightImmediate(tmpExprPlusResult.getAddress(),          
                  eLeftImm.getAddress(), eRightImm.getIntValue());
         }
         else if (!leftExpr.isImmediate() && !rightTerm.isImmediate())
         {
            NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) leftExpr;
            NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) rightTerm;
            exprPlusQuad = quadGen.makeAddRegular(tmpExprPlusResult.getAddress(),          
                  eLeftImm.getAddress(), eRightImm.getAddress());
         }
         else {
            reportError("","Invalid addition operation arguements.");
            return null;
         }
         
         quadGen.addQuad(exprPlusQuad);
         return tmpExprPlusResult;
			}
	}
	final class exprMinusNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("expr {minus} -> expr minus term\n");
		   }

         //get expr minus term left and right hand sides
         NSTIndEntry leftExpr = (NSTIndEntry) parser.rhsValue(0);
         NSTIndEntry rightTerm = (NSTIndEntry) parser.rhsValue(2);
         
         //check if null
         if (leftExpr == null || rightTerm==null) return null;     
         
         //check if not integers
         if ((!leftExpr.isInteger()&& !leftExpr.isIntArray())||
               (!rightTerm.isInteger()&& !rightTerm.isIntArray())) {
            reportError("","Invalid subtraction operation arguements - must be integer.");
            return null;
         }
         
         //declare the quad and get temp symtab address
         MemModQuad exprMinusQuad;
         NSTIndScalarEntry tmpExprMinusResult = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.INT_TYPE);
         
         //make quads depending on immediate values
         if (leftExpr.isImmediate() && rightTerm.isImmediate())
         {
            NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) leftExpr;
            NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) rightTerm;
            exprMinusQuad = quadGen.makeSubBothImmediate(tmpExprMinusResult.getAddress(),          
                  eLeftImm.getIntValue(), eRightImm.getIntValue());
         }
         else if (leftExpr.isImmediate() && !rightTerm.isImmediate())
         {
            NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) leftExpr;
            NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) rightTerm;
            exprMinusQuad = quadGen.makeSubLeftImmediate(tmpExprMinusResult.getAddress(),          
                  eLeftImm.getIntValue(), eRightImm.getAddress());
         }
         else if (!leftExpr.isImmediate() && rightTerm.isImmediate())
         {
            NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) leftExpr;
            NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) rightTerm;
            exprMinusQuad = quadGen.makeSubRightImmediate(tmpExprMinusResult.getAddress(),          
                  eLeftImm.getAddress(), eRightImm.getIntValue());
         }
         else if (!leftExpr.isImmediate() && !rightTerm.isImmediate())
         {
            NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) leftExpr;
            NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) rightTerm;
            exprMinusQuad = quadGen.makeSubRegular(tmpExprMinusResult.getAddress(),          
                  eLeftImm.getAddress(), eRightImm.getAddress());
         }
         else {
            reportError("","Invalid subtraction operation arguements.");
            return null;
         }
         
         quadGen.addQuad(exprMinusQuad);
         return tmpExprMinusResult;
			}
	}
	final class exprOrNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("expr {or} -> expr or term\n");
		   }
		   
		   //get expr or term left and right hand sides
         NSTIndEntry leftExpr = (NSTIndEntry) parser.rhsValue(0);
         NSTIndEntry rightTerm = (NSTIndEntry) parser.rhsValue(2);
         
         //check if null
         if (leftExpr == null || rightTerm==null) return null;     
         
         //check if not booleans
         if ((!leftExpr.isBoolean()&& !leftExpr.isBooleanArray())||
               (!rightTerm.isBoolean()&& !rightTerm.isBooleanArray())) {
            reportError("","Invalid OR operation arguements - must be boolean.");
            return null;
         }
         
         //declare the quad and get temp symtab address
         MemModQuad exprOrQuad;
         NSTIndScalarEntry tmpExprOrResult = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.BOOL_TYPE);
         
         //make quads depending on immediate values
         if (leftExpr.isImmediate() && rightTerm.isImmediate())
         {
            NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) leftExpr;
            NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) rightTerm;
            exprOrQuad = quadGen.makeOrBothImmediate(tmpExprOrResult.getAddress(),          
                  eLeftImm.getBoolValue(), eRightImm.getBoolValue());
         }
         else if (leftExpr.isImmediate() && !rightTerm.isImmediate())
         {
            NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) leftExpr;
            NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) rightTerm;
            exprOrQuad = quadGen.makeOrLeftImmediate(tmpExprOrResult.getAddress(),          
                  eLeftImm.getBoolValue(), eRightImm.getAddress());
         }
         else if (!leftExpr.isImmediate() && rightTerm.isImmediate())
         {
            NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) leftExpr;
            NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) rightTerm;
            exprOrQuad = quadGen.makeOrRightImmediate(tmpExprOrResult.getAddress(),          
                  eLeftImm.getAddress(), eRightImm.getBoolValue());
         }
         else if (!leftExpr.isImmediate() && !rightTerm.isImmediate())
         {
            NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) leftExpr;
            NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) rightTerm;
            exprOrQuad = quadGen.makeOrRegular(tmpExprOrResult.getAddress(),          
                  eLeftImm.getAddress(), eRightImm.getAddress());
         }
         else {
            reportError("","Invalid AND operation arguements.");
            return null;
         }
         
         quadGen.addQuad(exprOrQuad);
         return tmpExprOrResult;
			}
	}
	final class exprTermNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("expr {term} -> term\n");
		   }
		   NSTIndEntry term = (NSTIndEntry)parser.rhsValue(0);
         if (term==null) {return null; }
         return term;
			}
	}
	
	//term (star, slash, and, factor)
	final class termStarNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("term {star} -> term star factor\n");
		   }
		   
		   //get term star factor left and right hand sides
         NSTIndEntry leftTerm = (NSTIndEntry) parser.rhsValue(0);
         NSTIndEntry rightFactor = (NSTIndEntry) parser.rhsValue(2);
         
         //check if null
         if (leftTerm == null || rightFactor==null) return null;     
         
         //check if not integers
         if ((!leftTerm.isInteger()&& !leftTerm.isIntArray())||
               (!rightFactor.isInteger()&& !rightFactor.isIntArray())) {
            reportError("","Invalid multiplication operation arguements - must be integer.");
            return null;
         }
         
         //declare the quad and get temp symtab address
         MemModQuad termStarQuad;
         NSTIndScalarEntry tmpTermStarResult = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.INT_TYPE);
         
         //make quads depending on immediate values
         if (leftTerm.isImmediate() && rightFactor.isImmediate())
         {
            NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) leftTerm;
            NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) rightFactor;
            termStarQuad = quadGen.makeMulBothImmediate(tmpTermStarResult.getAddress(),          
                  eLeftImm.getIntValue(), eRightImm.getIntValue());
         }
         else if (leftTerm.isImmediate() && !rightFactor.isImmediate())
         {
            NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) leftTerm;
            NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) rightFactor;
            termStarQuad = quadGen.makeMulLeftImmediate(tmpTermStarResult.getAddress(),          
                  eLeftImm.getIntValue(), eRightImm.getAddress());
         }
         else if (!leftTerm.isImmediate() && rightFactor.isImmediate())
         {
            NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) leftTerm;
            NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) rightFactor;
            termStarQuad = quadGen.makeMulRightImmediate(tmpTermStarResult.getAddress(),          
                  eLeftImm.getAddress(), eRightImm.getIntValue());
         }
         else if (!leftTerm.isImmediate() && !rightFactor.isImmediate())
         {
            NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) leftTerm;
            NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) rightFactor;
            termStarQuad = quadGen.makeMulRegular(tmpTermStarResult.getAddress(),          
                  eLeftImm.getAddress(), eRightImm.getAddress());
         }
         else {
            reportError("","Invalid multiplication operation arguements.");
            return null;
         }
         
         quadGen.addQuad(termStarQuad);
         return tmpTermStarResult;
			}
	}
	final class termSlashNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("term {slash} -> term slash factor\n");
		   }
		   
		   //get term slash factor left and right hand sides
         NSTIndEntry leftTerm = (NSTIndEntry) parser.rhsValue(0);
         NSTIndEntry rightFactor = (NSTIndEntry) parser.rhsValue(2);
         
         //check if null
         if (leftTerm == null || rightFactor==null) return null;     
         
         //check if not integers
         if ((!leftTerm.isInteger()&& !leftTerm.isIntArray())||
               (!rightFactor.isInteger()&& !rightFactor.isIntArray())) {
            reportError("","Invalid division operation arguements - must be integer.");
            return null;
         }
         
         //declare the quad and get temp symtab address
         MemModQuad termSlashQuad;
         NSTIndScalarEntry tmpTermSlashResult = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.INT_TYPE);
         
         //make quads depending on immediate values
         if (leftTerm.isImmediate() && rightFactor.isImmediate())
         {
            NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) leftTerm;
            NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) rightFactor;
            termSlashQuad = quadGen.makeDivBothImmediate(tmpTermSlashResult.getAddress(),          
                  eLeftImm.getIntValue(), eRightImm.getIntValue());
         }
         else if (leftTerm.isImmediate() && !rightFactor.isImmediate())
         {
            NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) leftTerm;
            NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) rightFactor;
            termSlashQuad = quadGen.makeDivLeftImmediate(tmpTermSlashResult.getAddress(),          
                  eLeftImm.getIntValue(), eRightImm.getAddress());
         }
         else if (!leftTerm.isImmediate() && rightFactor.isImmediate())
         {
            NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) leftTerm;
            NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) rightFactor;
            termSlashQuad = quadGen.makeDivRightImmediate(tmpTermSlashResult.getAddress(),          
                  eLeftImm.getAddress(), eRightImm.getIntValue());
         }
         else if (!leftTerm.isImmediate() && !rightFactor.isImmediate())
         {
            NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) leftTerm;
            NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) rightFactor;
            termSlashQuad = quadGen.makeDivRegular(tmpTermSlashResult.getAddress(),          
                  eLeftImm.getAddress(), eRightImm.getAddress());
         }
         else {
            reportError("","Invalid division operation arguements.");
            return null;
         }
         
         quadGen.addQuad(termSlashQuad);
         return tmpTermSlashResult;
			}
	}
	final class termAndNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("term {and} -> term and factor\n");
		   }
		   
		   //get term and factor left and right hand sides
		   NSTIndEntry leftTerm = (NSTIndEntry) parser.rhsValue(0);
         NSTIndEntry rightFactor = (NSTIndEntry) parser.rhsValue(2);
         
         //check if null
         if (leftTerm == null || rightFactor==null) return null;		
		   
		   //check if not booleans
         if ((!leftTerm.isBoolean()&& !leftTerm.isBooleanArray())||
               (!rightFactor.isBoolean()&& !rightFactor.isBooleanArray())) {
            reportError("","Invalid AND operation arguements - must be boolean.");
            return null;
         }
         
         //declare the quad and get temp symtab address
         MemModQuad termAndQuad;
         NSTIndScalarEntry tmpTermAndResult = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.BOOL_TYPE);
         
		   //make quads depending on immediate values
         if (leftTerm.isImmediate() && rightFactor.isImmediate())
         {
            NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) leftTerm;
            NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) rightFactor;
            termAndQuad = quadGen.makeAndBothImmediate(tmpTermAndResult.getAddress(),          
                  eLeftImm.getBoolValue(), eRightImm.getBoolValue());
         }
         else if (leftTerm.isImmediate() && !rightFactor.isImmediate())
         {
            NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) leftTerm;
            NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) rightFactor;
            termAndQuad = quadGen.makeAndLeftImmediate(tmpTermAndResult.getAddress(),          
                  eLeftImm.getBoolValue(), eRightImm.getAddress());
         }
         else if (!leftTerm.isImmediate() && rightFactor.isImmediate())
         {
            NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) leftTerm;
            NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) rightFactor;
            termAndQuad = quadGen.makeAndRightImmediate(tmpTermAndResult.getAddress(),          
                  eLeftImm.getAddress(), eRightImm.getBoolValue());
         }
         else if (!leftTerm.isImmediate() && !rightFactor.isImmediate())
         {
            NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) leftTerm;
            NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) rightFactor;
            termAndQuad = quadGen.makeAndRegular(tmpTermAndResult.getAddress(),          
                  eLeftImm.getAddress(), eRightImm.getAddress());
         }
         else {
            reportError("","Invalid AND operation arguements.");
            return null;
         }
		   
         quadGen.addQuad(termAndQuad);
         return tmpTermAndResult;
			}
	}
	final class termFactorNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   
		   if (showReductions) {
   		   System.out.print(parser.token().line + ": ");
   			System.out.println("term {factor} -> factor\n");
		   }
		   NSTIndEntry factor = (NSTIndEntry)parser.rhsValue(0);
         if (factor==null) {return null; }
         return factor;
			}
	}
	
	//factor (positive, negative, not)
	final class factorPositiveNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			
			if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("factor {positive} -> prim\n");
			}
			NSTIndEntry prim = (NSTIndEntry)parser.rhsValue(0);
			if (prim==null) {return null; }
			return prim;
			}
	}
	final class factorNegativeNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			
			if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("factor {negative} -> minus prim\n");
			}
			NSTIndEntry prim = (NSTIndEntry)parser.rhsValue(1);
			MemModQuad negativeQuad;
         if (prim==null) {return null; }
         if (!prim.isInteger()&&!prim.isIntArray())
         {
            reportError("","Can not make non-integer negative");
            return null;
         }
         if (prim.isImmediate())
         {
            NSTIndImmediateEntry immPrim = (NSTIndImmediateEntry) prim;
            NSTIndScalarEntry tmpPrim = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.INT_TYPE);
            negativeQuad = quadGen.makeNegImmediate(tmpPrim.getAddress(),          
                  immPrim.getIntValue()); 
            quadGen.addQuad(negativeQuad);
            return tmpPrim;
            
         }
         else
         {
            NSTIndScalarEntry scalarPrim = (NSTIndScalarEntry) prim;
            NSTIndScalarEntry tmpPrim = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.INT_TYPE);
            negativeQuad = quadGen.makeNegRegular(tmpPrim.getAddress(),          
                  scalarPrim.getAddress());
            quadGen.addQuad(negativeQuad);
            return tmpPrim;
         }
			}
	}
	final class factorNotNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
			
			if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("factor {not} -> not prim\n");
			}
			NSTIndEntry prim = (NSTIndEntry)parser.rhsValue(1);
         MemModQuad notQuad;
         if (prim==null) {return null; }
         if (!prim.isBoolean()&&!prim.isBooleanArray())
         {
            reportError("","Can not make non-boolean a not");
            return null;
         }
         if (prim.isImmediate())
         {
            NSTIndImmediateEntry immPrim = (NSTIndImmediateEntry) prim;
            NSTIndScalarEntry tmpPrim = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.BOOL_TYPE);
            notQuad = quadGen.makeNotImmediate(tmpPrim.getAddress(),          
                  immPrim.getBoolValue()); 
            quadGen.addQuad(notQuad);
            return tmpPrim;
         }
         else
         {
            NSTIndScalarEntry scalarPrim = (NSTIndScalarEntry) prim;
            NSTIndScalarEntry tmpPrim = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.BOOL_TYPE);
            notQuad = quadGen.makeNotRegular(tmpPrim.getAddress(),          
                  scalarPrim.getAddress());
            quadGen.addQuad(notQuad);
            return tmpPrim;
         }
			}
	}
	
	//prim (const, boolConst, value, expr, relop)
   final class primConstNT extends NonterminalFactory
   {
      public Object makeNonterminal (Parser parser, int param) 
         throws IOException, SyntaxException
         {
         if (showReductions) {
            System.out.print(parser.token().line + ": ");
            System.out.println("prim {const} -> intConst");
            String intString = (String) parser.rhsValue (0);
            System.out.println("intConst lexeme: " + intString + "\n");
         }
         
         String stringValue = (String) parser.rhsValue (0);
         int integerValue = Integer.parseInt(stringValue);
         
         if (stringValue==null) {
            reportError("","Not valid integer value.");
            return null;
         }
         return symtab.new NSTIndImmediateEntry(NanoSymbolTable.INT_TYPE, integerValue);
   }
   }
      
   final class primBoolConstNT extends NonterminalFactory
   {
      public Object makeNonterminal (Parser parser, int param) 
         throws IOException, SyntaxException
         {
         String boolString = (String) parser.rhsValue (0);
         if (showReductions) {
            System.out.print(parser.token().line + ": ");
            System.out.println("prim {boolConst} -> boolConst");
            System.out.println("boolean value: " + boolString + "\n");
         }
      
         Boolean boolObj = Boolean.valueOf((String)parser.rhsValue(0));  
         if (boolObj==null) {
            return null;
         }
         return symtab.new NSTIndImmediateEntry(NanoSymbolTable.BOOL_TYPE, boolObj.booleanValue());
         }
   }
   final class primValueNT extends NonterminalFactory
   {
      public Object makeNonterminal (Parser parser, int param) 
         throws IOException, SyntaxException
         {

         if (showReductions) {
            System.out.print(parser.token().line + ": ");
            System.out.println("prim {value} -> value\n");
         }
         //I do not think I need to cast to scalar or immediate, etc.
         NSTIndEntry value = (NSTIndEntry)parser.rhsValue(0);
         if (value==null) {return null; }
         return value;
         }
   }
   final class primExprNT extends NonterminalFactory
   {
      public Object makeNonterminal (Parser parser, int param) 
         throws IOException, SyntaxException
         {
         if (showReductions) {
            System.out.print(parser.token().line + ": ");
            System.out.println("prim {expr} -> lparen expr rparen\n");
         }
         NSTIndEntry expr = (NSTIndEntry)parser.rhsValue(1);
         if (expr==null) {return null; }
         return expr;
         }
   }
   
   final class primRelopNT extends NonterminalFactory
   {
      public Object makeNonterminal (Parser parser, int param) 
         throws IOException, SyntaxException
         {
         if (showReductions) {
            System.out.print(parser.token().line + ": ");
            System.out.println("prim {relop} -> lparen expr relop expr rparen\n");
         }
         
         //Get the left and right expressions
         NSTIndEntry eLeft = (NSTIndEntry) parser.rhsValue(1);
         NSTIndEntry eRight = (NSTIndEntry) parser.rhsValue(3);
         
         String relopString = (String)parser.rhsValue(2);
         if (relopString==null) {
            reportError("","Invalid relational operator in primative assignment");
            return null;
         }
         
         if (eLeft == null || eRight==null) return null;
         
         //quad generator does not allow boolean arguments in relops
         if (eLeft.isBoolean() || eRight.isBoolean()) {
            reportError("","Invalid relational operator arguements - must be integer.");
            return null;
         }
         
         //declare the relop quad and get temp symtab address
         MemModQuad relopQuad;
         NSTIndScalarEntry tmpRelopResult = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.BOOL_TYPE);
         
         String relopName = (String)parser.rhsValue (2);
         
         if (relopName.equals("==")) { 
             if (eLeft.isImmediate() && eRight.isImmediate())
             {
                NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) eLeft;
                NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) eRight;
                relopQuad = quadGen.makeRelopEqualsBothImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getIntValue(), eRightImm.getIntValue());
             }
             else if (eLeft.isImmediate() && !eRight.isImmediate())
             {
                NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) eLeft;
                NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) eRight;
                relopQuad = quadGen.makeRelopEqualsLeftImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getIntValue(), eRightImm.getAddress());
             }
             else if (!eLeft.isImmediate() && eRight.isImmediate())
             {
                NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) eLeft;
                NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) eRight;
                relopQuad = quadGen.makeRelopEqualsBothImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getAddress(), eRightImm.getIntValue());
             }
             else if (!eLeft.isImmediate() && !eRight.isImmediate())
             {
                NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) eLeft;
                NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) eRight;
                relopQuad = quadGen.makeRelopEqualsRegular(tmpRelopResult.getAddress(),          
                      eLeftImm.getAddress(), eRightImm.getAddress());
             }
             else {
                reportError("","Invalid relational operator arguements.");
                return null;
             }
          }
          else if (relopName.equals("<>")) {
             if (eLeft.isImmediate() && eRight.isImmediate())
             {
                NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) eLeft;
                NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) eRight;
                relopQuad = quadGen.makeRelopNotEqualsBothImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getIntValue(), eRightImm.getIntValue());
             }
             else if (eLeft.isImmediate() && !eRight.isImmediate())
             {
                NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) eLeft;
                NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) eRight;
                relopQuad = quadGen.makeRelopNotEqualsLeftImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getIntValue(), eRightImm.getAddress());
             }
             else if (!eLeft.isImmediate() && eRight.isImmediate())
             {
                NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) eLeft;
                NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) eRight;
                relopQuad = quadGen.makeRelopNotEqualsBothImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getAddress(), eRightImm.getIntValue());
             }
             else if (!eLeft.isImmediate() && !eRight.isImmediate())
             {
                NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) eLeft;
                NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) eRight;
                relopQuad = quadGen.makeRelopNotEqualsRegular(tmpRelopResult.getAddress(),          
                      eLeftImm.getAddress(), eRightImm.getAddress());
             }
             else {
                reportError("","Invalid relational operator arguements.");
                return null;
             }
          }
          else if (relopName.equals("<")) {
             if (eLeft.isImmediate() && eRight.isImmediate())
             {
                NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) eLeft;
                NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) eRight;
                relopQuad = quadGen.makeRelopLessThanBothImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getIntValue(), eRightImm.getIntValue());
             }
             else if (eLeft.isImmediate() && !eRight.isImmediate())
             {
                NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) eLeft;
                NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) eRight;
                relopQuad = quadGen.makeRelopLessThanLeftImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getIntValue(), eRightImm.getAddress());
             }
             else if (!eLeft.isImmediate() && eRight.isImmediate())
             {
                NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) eLeft;
                NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) eRight;
                relopQuad = quadGen.makeRelopLessThanBothImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getAddress(), eRightImm.getIntValue());
             }
             else if (!eLeft.isImmediate() && !eRight.isImmediate())
             {
                NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) eLeft;
                NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) eRight;
                relopQuad = quadGen.makeRelopLessThanRegular(tmpRelopResult.getAddress(),          
                      eLeftImm.getAddress(), eRightImm.getAddress());
             }
             else {
                reportError("","Invalid relational operator arguements.");
                return null;
             }
          }
          else if (relopName.equals("<=")) {
             if (eLeft.isImmediate() && eRight.isImmediate())
             {
                NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) eLeft;
                NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) eRight;
                relopQuad = quadGen.makeRelopLessThanEqualsBothImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getIntValue(), eRightImm.getIntValue());
             }
             else if (eLeft.isImmediate() && !eRight.isImmediate())
             {
                NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) eLeft;
                NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) eRight;
                relopQuad = quadGen.makeRelopLessThanEqualsLeftImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getIntValue(), eRightImm.getAddress());
             }
             else if (!eLeft.isImmediate() && eRight.isImmediate())
             {
                NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) eLeft;
                NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) eRight;
                relopQuad = quadGen.makeRelopLessThanEqualsBothImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getAddress(), eRightImm.getIntValue());
             }
             else if (!eLeft.isImmediate() && !eRight.isImmediate())
             {
                NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) eLeft;
                NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) eRight;
                relopQuad = quadGen.makeRelopLessThanEqualsRegular(tmpRelopResult.getAddress(),          
                      eLeftImm.getAddress(), eRightImm.getAddress());
             }
             else {
                reportError("","Invalid relational operator arguements.");
                return null;
             }
          }
          else if (relopName.equals(">")) {
             if (eLeft.isImmediate() && eRight.isImmediate())
             {
                NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) eLeft;
                NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) eRight;
                relopQuad = quadGen.makeRelopGreaterThanBothImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getIntValue(), eRightImm.getIntValue());
             }
             else if (eLeft.isImmediate() && !eRight.isImmediate())
             {
                NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) eLeft;
                NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) eRight;
                relopQuad = quadGen.makeRelopGreaterThanLeftImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getIntValue(), eRightImm.getAddress());
             }
             else if (!eLeft.isImmediate() && eRight.isImmediate())
             {
                NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) eLeft;
                NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) eRight;
                relopQuad = quadGen.makeRelopGreaterThanBothImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getAddress(), eRightImm.getIntValue());
             }
             else if (!eLeft.isImmediate() && !eRight.isImmediate())
             {
                NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) eLeft;
                NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) eRight;
                relopQuad = quadGen.makeRelopGreaterThanRegular(tmpRelopResult.getAddress(),          
                      eLeftImm.getAddress(), eRightImm.getAddress());
             }
             else {
                reportError("","Invalid relational operator arguements.");
                return null;
             }
          }
          else if (relopName.equals(">=")) {
             if (eLeft.isImmediate() && eRight.isImmediate())
             {
                NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) eLeft;
                NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) eRight;
                relopQuad = quadGen.makeRelopGreaterThanEqualsBothImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getIntValue(), eRightImm.getIntValue());
             }
             else if (eLeft.isImmediate() && !eRight.isImmediate())
             {
                NSTIndImmediateEntry eLeftImm = (NSTIndImmediateEntry) eLeft;
                NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) eRight;
                relopQuad = quadGen.makeRelopGreaterThanEqualsLeftImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getIntValue(), eRightImm.getAddress());
             }
             else if (!eLeft.isImmediate() && eRight.isImmediate())
             {
                NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) eLeft;
                NSTIndImmediateEntry eRightImm = (NSTIndImmediateEntry) eRight;
                relopQuad = quadGen.makeRelopGreaterThanEqualsBothImmediate(tmpRelopResult.getAddress(),          
                      eLeftImm.getAddress(), eRightImm.getIntValue());
             }
             else if (!eLeft.isImmediate() && !eRight.isImmediate())
             {
                NSTIndScalarEntry eLeftImm = (NSTIndScalarEntry) eLeft;
                NSTIndScalarEntry eRightImm = (NSTIndScalarEntry) eRight;
                relopQuad = quadGen.makeRelopGreaterThanEqualsRegular(tmpRelopResult.getAddress(),          
                      eLeftImm.getAddress(), eRightImm.getAddress());
             }
             else {
                reportError("","Invalid relational operator arguements.");
                return null;
             }
          }
          else {
             reportError("","Unable to identify relational operator.");
             return null;
          }
            
            quadGen.addQuad(relopQuad);
            return tmpRelopResult;  
            }
   }
   
   //value (id, expr)
   final class valueIdNT extends NonterminalFactory
   {
      public Object makeNonterminal (Parser parser, int param)
         throws IOException, SyntaxException
         {
         if (showReductions) {
            System.out.print(parser.token().line + ": ");
            System.out.println("value {id} -> id");
            String idString = (String) parser.rhsValue (0);
            System.out.println("identifier lexeme: " + idString + "\n");
         }
         
         NSTIndScalarEntry i = (NSTIndScalarEntry)symtab.get((String)parser.rhsValue(0));
         if (i==null)
         {
            reportError("","Identifier not recognized");
            return null; //Or something better?
         }
         else
            //No quad to generate, just want to pass up the nst 
            return i;
         }
   }
   final class valueExprNT extends NonterminalFactory
   {
      public Object makeNonterminal (Parser parser, int param) 
         throws IOException, SyntaxException
         {
         if (showReductions) {
            System.out.print(parser.token().line + ": ");
            System.out.println("value {expr} -> id lbracket expr rbracket");
            String idString = (String) parser.rhsValue (0);
            System.out.println("identifier lexeme: " + idString + "\n");
         }
         
         //get the array id and the index offset
         NSTIndArrayEntry array = (NSTIndArrayEntry)symtab.get((String)parser.rhsValue(0));
         NSTIndEntry indexExpr = (NSTIndEntry)parser.rhsValue(2);
         MemModQuad indexCalcQuad = null;
         
         //make sure array is valid id and is an array.  
         //Check that the index is an integer
         if (array==null) 
         {
            reportError("","Array identifier not found in scope");
            return null; 
         }
         else if (array.isScalar())
         {
            reportError("","Attempt to use scalar identifier as array base address");
            return null;
         }
         else if (!indexExpr.isInteger()&&!indexExpr.isIntArray())
         {
            
            reportError("","Non-integer index in array element assignment");
            return null;
         }
         
         //make an indexCalcQuad to calc the index offset
         //store the result in a tmpIndex symbol table entry
         if (indexExpr.isImmediate())
         {
            NSTIndImmediateEntry immIndex = (NSTIndImmediateEntry) indexExpr;
            NSTIndScalarEntry tmpIndex = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.INT_TYPE);
            indexCalcQuad = quadGen.makeOffsetImmediate(tmpIndex.getAddress(),          
                                 array.getAddress(), immIndex.getIntValue()); 
         }
         else if (indexExpr.isScalar() || indexExpr.isIntArray())
         {
            NSTIndScalarEntry calculatedIndex = (NSTIndScalarEntry) indexExpr;
            NSTIndScalarEntry tmpIndex = (NSTIndScalarEntry)symtab.addNewTempToCurrentBlock(NanoSymbolTable.INT_TYPE);
            indexCalcQuad = quadGen.makeOffsetRegular(tmpIndex.getAddress(), 
                           array.getAddress(), calculatedIndex.getAddress());
         }
         
         quadGen.addQuad(indexCalcQuad);
         //make a new symbol table entry for the array location with the offset
         if (array.isIntArray()) 
            return symtab.new NSTIndScalarEntry(array.getName(), NanoSymbolTable.INT_TYPE, false, indexCalcQuad.getResultAddress());
         else
            return symtab.new NSTIndScalarEntry(array.getName(), NanoSymbolTable.BOOL_TYPE, false, indexCalcQuad.getResultAddress());
         }
   }

	
	//relop (equals, lessThan, greaterThan, lessThanEquals, greaterThanEquals, notEquals)
	final class relopIsEqualsNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("relop{isEquals} -> isEquals\n");
		   }
		   return (String) parser.rhsValue (0);
			}
	}
	final class relopLessThanNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("relop{lessThan} -> lessThan\n");
		   }
		   return (String) parser.rhsValue (0);
			}
	}
	final class relopGreaterThanNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("relop{greaterThan} -> greaterThan\n");
		   }
		   return (String) parser.rhsValue (0);
			}
	}
	final class relopLessThanEqualsNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("relop{lessThanEquals} -> lessThanEquals\n");
		   }
		   return (String) parser.rhsValue (0);
			}
	}
	final class relopGreaterThanEqualsNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("relop{greaterThanEquals} -> greaterThanEquals\n");
		   }
		   return (String) parser.rhsValue (0);
			}
	}
	final class relopNotEqualsNT extends NonterminalFactory
	{
		public Object makeNonterminal (Parser parser, int param) 
			throws IOException, SyntaxException
			{
		   if (showReductions) {
   			System.out.print(parser.token().line + ": ");
   			System.out.println("relop{notEquals} -> notEquals\n");
		   }
		   return (String) parser.rhsValue (0);
			}
	}

}

