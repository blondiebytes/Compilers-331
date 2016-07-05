package semanticActions;

import java.lang.*;

//import com.sun.org.apache.bcel.internal.generic.ArithmeticInstruction;
//import com.sun.org.apache.xpath.internal.operations.Variable;
// import com.sun.tools.javac.code.Attribute;
import errors.*;
import java.util.*;

import grammar.GrammarSymbol;
import jdk.nashorn.internal.ir.Symbol;
import jdk.nashorn.internal.parser.*;
import lex.*;
//import sun.jvm.hotspot.memory.*;
//import sun.jvm.hotspot.debugger.cdbg.Sym;
//import sun.security.krb5.internal.crypto.EType;
import lex.Token;
import lex.TokenType;
import symbolTable.*;
import symbolTable.SymbolTable;

public class SemanticActions {

	private Stack<Object> semanticStack ;
	private boolean insert ; // flags insertion/search mode in symbol table
	private boolean isArray ; // flags array vs. simple variable
	private boolean global ; // flags global vs local environment
	private int globalMemory ;
	private int localMemory ;

	private SymbolTable globalTable ;
	private SymbolTable localTable ;
	private int GLOBAL_STORE = 0; //{ quadruple array location of ALLOC statement
	//	for global memory }
	private int LOCAL_STORE = 0;
	private SymbolTableEntry CURRENTFUNCTION = null;
	private Stack<Integer> PARAMCOUNT;// { stack for no. of parameters in proc declaration or call }
	private NextParameter nextParameter;
	private Quadruples quadruples = new Quadruples();
	int counterTemp = 0;

	private SymbolTable constantTable ;

	private int tableSize = 97;
	private boolean isParam;
	private SymbolTableEntry nullEntry = null;
	private Tokenizer tokenizer;

	// TODO:
	// - generate PARAMS (^, %, etc)
	// - what to do with unary-minus?

	public SemanticActions(Tokenizer tokenizer) throws SemanticError, SymbolTableError{
		semanticStack = new Stack<Object>();
		insert = false;
		isArray = false;
		isParam = false;
		global = true;
		globalMemory = 0 ;
		localMemory = 0;
		globalTable = new SymbolTable(tableSize);
		localTable = new SymbolTable(tableSize);
		constantTable = new SymbolTable(tableSize);
		PARAMCOUNT = new Stack();
		nextParameter = new NextParameter();
		SymbolTable.installBuiltins(globalTable);
		this.tokenizer = tokenizer;
	}

	public void execute(int actionNumber, Token token)  throws SemanticError, SymbolTableError {

		debug("calling action : " + actionNumber + " with token " + token.getType() + " with value " + token.getValue());
		ETYPE etype;
		SymbolTableEntry id1;
		SymbolTableEntry id2;
		SymbolTableEntry offset;
		FunctionEntry functionEntry;
		Token operator;
		List EFALSE_1;
		List ETRUE_1;
		List EFALSE_2;
		List ETRUE_2;
		List skipElse;
		Integer beginLoop;

		switch (actionNumber)
		{

			case 1:
				// set INSERT flag to true
				insert = true;
				break;

			case 2:
				// set INSERT flag to false -> b/c we are searching
				insert = false;
				break;

			case 3:
				// PSEUDO CODE!
				// ------------
				// first pop the type from the semanticStack
				Object obj = semanticStack.pop();
				// we know what we pop is a token because all we push on is a token
				// if this isn't true, then we'll get errors that we can fix later.
				Token t = (Token) obj;
				TokenType TYP =  t.getType();
				// If the array flag is true... then we are in array mode
				if (isArray) {
					// pop upper and lower bounds off of the semantic stack
					// we know what we pop is a intconstant token because of the grammar
					// if this isn't true, then we'll get errors that we can fix later.
					System.out.println("ARRAY is true");
					ConstantEntry ub = (ConstantEntry) semanticStack.pop();
					ConstantEntry lb = (ConstantEntry) semanticStack.pop();
					int msize = calculateArraySize(ub, lb);
					// Save the local and global ID's
					saveArrayIDs(TYP, ub, lb);

				} else { // if the array flag is false.. then we are in simple variable mode
					System.out.println("ARRAY is false : SETTING VARIABLES");
					saveVariableIDs(TYP);
				}
				isArray = false;
				break;

			case 4:
				// push TYPE onto the semanticStack
				// Here TYPE evaluates to a token (either integer, real, array, etc)
				// (token -> passed as a param to this func)
				semanticStack.push(token);
				break;

			case 5: // TODO: phase 4
				this.insert = false;
				id1 = (SymbolTableEntry) this.semanticStack.pop();
				generate("PROCBEGIN", opToAddress("PROCBEGIN", id1));
				LOCAL_STORE = quadruples.getNextQuad();
				generate("alloc", "_");
				break;

			case 6:
				// set ARRAY flag to true
				isArray = true;
				break;

			case 7:
				// push CONSTANT (a token passed as a param to this func) onto the semanticStack
				ConstantEntry constant = new ConstantEntry(token.getValue(), token.getType());
				semanticStack.push(constant);
				break;

			case 9:
				// Insert file IO ID's and pop related entries
				insertFileIOID((Token) semanticStack.pop());
				insertFileIOID((Token) semanticStack.pop());
				// Insert this procedure into the global table
				insertProcedure((Token) semanticStack.pop());
				//  Set insert flag to false because searching now
				// TODO: Maybe pop ids
				insert = false;
				// Generating code for these commands -> initing it.
				generate("CODE");
				generate("call", (globalTable.lookup("MAIN")).getName().toLowerCase(), 0);
				generate("exit");
				break;

			case 11: // TODO: phase 4
				// Set global/local flag to GLOBAL
				global = true;
				// delete local symbol table entries
				localTable.clear();
			    // set CURRENTFUNCTION to nil
				CURRENTFUNCTION = null;
				// Fill in quadruple at location LOCAL_STORE with value of LOCAL_MEM
				backpatch(LOCAL_STORE, localMemory);
				// GEN(free,LOCAL_MEM)
				generate("free", localMemory);
				// GEN(PROCEND)
				generate("PROCEND");
				break;

			case 13:
				// push ID (the token passed as a param to this func) onto the semanticStack
				semanticStack.push(token);
				break;

			case 15: // TODO: phase 4
				// Create function entry from token
				functionEntry = new FunctionEntry(token.getValue());

				// CREATE(FUN_NAME,INTEGER)  {dummy type until we know the real one}
				VariableEntry funcResult = create("$$" + token.getValue(), TokenType.INTEGER);
				funcResult.setIsFunctionResult(true);
				// id.result = $$FUN_NAME
				functionEntry.setResult(funcResult);

				// insert id (Token) in symbol table (function_entry)
				if (global) {
					insert(globalTable, functionEntry);
				} else {
					insert(localTable, functionEntry);
				}

				// push id
				semanticStack.push(functionEntry);
				// GLOBAL/LOCAL = LOCAL
				global = false;
				// LOCAL_MEM = 0
				localMemory = 0;
				break;

			case 16: // TODO: phase4 --> ?? maybe pop
				// pop TYPE
				TokenType TYPE = ((Token) semanticStack.pop()).getType();
				id1 = (SymbolTableEntry) semanticStack.peek();
				// id.type = TYPE
				id1.setType(TYPE);
				//  CURRENTFUNCTION = id
				CURRENTFUNCTION = id1;
				// $$FUN_NAME.type = TYPE --> we aren't in the right scope
				lookup("$$" + CURRENTFUNCTION.getName()).setType(TYPE);
				break;

			case 17: // TODO: phase4
				// insert id in symbol table (procedure_entry)
				ProcedureEntry procedureEntry = new ProcedureEntry(token.getValue());
				if (global) {
					insert(globalTable, procedureEntry);
				} else {
					insert(localTable, procedureEntry);
				}
				// push id
				semanticStack.push(procedureEntry);
				// GLOBAL/LOCAL = LOCAL
				global = false;
				// LOCAL_MEM = 0
				localMemory = 0;
				break;

			case 19: // TODO: phase 4
				// PARMCOUNT = 0
				PARAMCOUNT.push(0);
				break;

			case 20: // TODO: phase 4
				// pop PARMCOUNT
				Integer parmsNum = PARAMCOUNT.pop();
				// get access to id
				id1 = (SymbolTableEntry) semanticStack.peek();
				//id.number_of_parameters = PARMCOUNT
				id1.setNumberOfParms(parmsNum);
				break;

			case 21: // TODO: phase 4
				// pop token
				token = (Token)this.semanticStack.pop();
				TokenType type = token.getType();

				// pop parm count
				int count = this.PARAMCOUNT.pop();
				int ub = -1;
				int lb = -1;

				// set ub and lb if array
				if (this.isArray){
					ConstantEntry uBound = (ConstantEntry) semanticStack.pop();
					ConstantEntry lBound = (ConstantEntry) semanticStack.pop();
					ub = getNumberValueOfToken(uBound);
					lb = getNumberValueOfToken(lBound);
				}

				// Create a linked list for param info
				LinkedList<ParmInformation> info = new LinkedList();

				//for each id (parameter) on stack:
				if (this.semanticStack.peek() instanceof Token) {
					Token id = (Token) this.semanticStack.peek();
					while (id.getType() == TokenType.IDENTIFIER){
						ParmInformation thisParm = new ParmInformation();

						if(this.isArray){
							// insert symbol table entry (array, is_parameter returns true)
							ArrayEntry entry = new ArrayEntry(id.getValue(), type);
							entry.setIsParameter(true);
							entry.setUBound(ub); // id.upper_bound = CONSTANT(1)
							entry.setLBound(lb); //  id.lower_bound = CONSTANT(2)
							thisParm.setUpperBound(ub); // set UBOUND of ParamInfo to id.ubound
							thisParm.setLowerBound(lb); // set LBOUND of ParamInfo to id.lbound
							thisParm.setArray(true); // set array flag in current PARMINFO element to TRUE
							if (global) {
								insert(globalTable, entry);
							} else {
								insert(localTable, entry);
							}
						} else {
							// create new symbol table entry (variable entry, with is_parameter returning true)
							VariableEntry entry = new VariableEntry(id.getValue(), type);
							entry.setIsParameter(true);
							// - set array flag in current PARMINFO entry to FALSE
							thisParm.setArray(false);
							// insert into the appropriate table
							if (global) {
								insert(globalTable, entry);
							} else {
								insert(localTable, entry);
							}
						}

						SymbolTableEntry entry = this.lookup(id.getValue());
						// id.address = LOCAL_MEM
						entry.setAddress(this.localMemory);
						// LOCAL_MEM = LOCAL_MEM + 1
						this.localMemory++;
						// id.type = TYPE {on stack}
						entry.setType(type);
						// set TYPE in current entry of PARMINFO to TYPE
						thisParm.setType(type);
						//increment PARMCOUNT
						count++;
						info.add(thisParm);

						this.semanticStack.pop(); // --> popping off ID
						// getting the next possible ID ready
						if (this.semanticStack.peek() instanceof Token){
							id = (Token) this.semanticStack.peek();
						}
						else {
							break;
						}
					}
					//  ARRAY/SIMPLE = SIMPLE
					this.isArray = false;
				}

				SymbolTableEntry thisProc = (SymbolTableEntry) this.semanticStack.peek();
				thisProc.addParamInformation(info);
				PARAMCOUNT.push(count);
				break;

			case 22:
				// pop etype
				etype = (ETYPE) semanticStack.pop();
				//: if ETYPE <> RELATIONAL, ERROR
				checkEtypeRelational(etype);
				// get access to what we need
				EFALSE_1 = (List) semanticStack.pop();
				ETRUE_1 = (List) semanticStack.peek();
				// put things back
				semanticStack.push(EFALSE_1);
				// BACKPATCH(E.TRUE, NEXTQUAD)
				backpatch(ETRUE_1, quadruples.getNextQuad());
				break;

			case 24:
				// set BEGINLOOP = NEXTQUAD
				beginLoop = new Integer(quadruples.getNextQuad());
				// push BEGINLOOP
				semanticStack.push(beginLoop);
				break;

			case 25:
				// pop etype
				etype = (ETYPE) semanticStack.pop();
				//if ETYPE <> RELATIONAL, ERROR
				checkEtypeRelational(etype);
				EFALSE_1 = (List) semanticStack.pop();
				ETRUE_1 = (List) semanticStack.peek();
				semanticStack.push(EFALSE_1);
				// BACKPATCH(E.TRUE, NEXTQUAD)
				backpatch(ETRUE_1, quadruples.getNextQuad());
				break;

			case 26:
				// pop etrue, efalse, begin loop
				EFALSE_1 = (List) semanticStack.pop();
				ETRUE_1 = (List) semanticStack.pop();
				beginLoop = new Integer((int) semanticStack.pop());
				// GEN(goto BEGINLOOP)            // pushed on stack in #24
				generate("goto", beginLoop.toString());
				// BACKPATCH(E.FALSE, NEXTQUAD)
				backpatch(EFALSE_1, quadruples.getNextQuad());
				break;

			case 27:
//				: set SKIP_ELSE = makelist(NEXTQUAD)
				skipElse = makeList(quadruples.getNextQuad());
				// : GEN(goto _ )
				generate("goto", "_");
				// get access to E-false
				EFALSE_1 = (List) semanticStack.peek();
//				: BACKPATCH(E.FALSE, NEXTQUAD)
				backpatch(EFALSE_1,quadruples.getNextQuad());
				// push SKIP_ELSE
				semanticStack.push(skipElse);
				break;

			case 28:
				// pop skipElse, EFALSE, and ETRUE
				skipElse = (List) semanticStack.pop();
				EFALSE_1 = (List) semanticStack.pop();
				ETRUE_1 = (List) semanticStack.pop();
//				: BACKPATCH(SKIP_ELSE, NEXTQUAD)  // pushed on stack in #27
				backpatch(skipElse, quadruples.getNextQuad());
				break;

			case 29:
				// pop EFALSE and ETRUE
				EFALSE_1 = (List) semanticStack.pop();
				ETRUE_1 = (List) semanticStack.pop();
//				: BACKPATCH(E.FALSE,NEXTQUAD)
				backpatch(EFALSE_1, quadruples.getNextQuad());
				break;

			case 30:
				// lookup id in symbol table --> id is our token
				id1 = lookup(token.getValue());
				if (id1 == null) {
					// if not found, throw undeclared variable error
					throw SemanticError.UndeclaredVariable(token.getValue(),
							tokenizer.getLineNumber());
				}
				// push id onto the semantic stack
				semanticStack.push(id1);
				// push ETYPE(ARITHMETIC) onto the semantic stack --> created enum for it
				semanticStack.push(ETYPE.arithmetic);
				break;

			case 31: // sign problem ??
				// pop off e, id1, offset, and id2
				etype = (ETYPE) semanticStack.pop();
				checkEtypeArithmetic(etype);
				// Value that we will put into id1
				id2 = (SymbolTableEntry) semanticStack.pop();
				// // TODO: for unary minus
//				Object ob = semanticStack.peek();
//				// pop off unary operator
//				if (ob instanceof Token) {
//					if (((Token) ob).getType() == TokenType.UNARYMINUS) {
//						// all is good
//						// TODO: WHERE DO WE PUT OFFSET>> set offset ????
//						semanticStack.pop();
//					}
//				}
				// offset is just symbtable entry for whatever the subscript is
				offset = (SymbolTableEntry) semanticStack.pop();
				id1 = (SymbolTableEntry) semanticStack.pop();
				// check for errors with etype and typecheck
				checkForErrors(etype, id1, id2);
				// according to id types and offsets, generate code and store variables
				generateAndStoreSM31(id1, id2, offset);
				break;

			case 32:
				// pop ETYPE
				etype = (ETYPE) semanticStack.pop();
				// check ETYPE = ARITHMETIC
				checkEtypeArithmetic(etype);
				// get id1 from semanticStack
				id1 = (SymbolTableEntry) semanticStack.peek();
				// if not id^.is_array, ERROR
				if (!id1.isArray()) {
					throw SemanticError.NotArray(tokenizer.getLineNumber());
				}
				break;

			case 33:
				// pop ETYPE
				etype = (ETYPE) semanticStack.pop();
				// if ETYPE <> ARITHMETIC, ERROR
				checkEtypeArithmetic(etype);
				// pop ID
				id1 = (SymbolTableEntry) semanticStack.pop();
				// peek at array entry
				ArrayEntry arrayEntry = (ArrayEntry) semanticStack.peek();
				// if id^.type <> INTEGER, ERROR  {id is pointer on top of stack}
				if (id1.getType() != TokenType.INTEGER) {
					throw SemanticError.BadArrayBounds(tokenizer.getLineNumber());
				}
				// CREATE(TEMP, INTEGER)
				SymbolTableEntry $$TEMP = create("$$TEMP" + counterTemp, TokenType.INTEGER);
				counterTemp++;
				// GEN(sub, id, array_name.lbound, $$TEMP) {array_name is id on bottom of stack}
				generate("sub", id1, arrayEntry.getLBound(), $$TEMP);
				// push $$TEMP
				semanticStack.push($$TEMP);
				break;

			case 34:
				if (semanticStack.peek() instanceof ETYPE) {
					// pop etype
					etype = (ETYPE) semanticStack.pop();
				}
				if (!semanticStack.isEmpty()) {
					id1 = (SymbolTableEntry) semanticStack.peek();
					// if id on stack is a function
					if (id1.isFunction()) {
						// call action 52
						execute(52, token);
					} else {
						semanticStack.push(nullEntry);
					}
				} else {
					semanticStack.push(nullEntry);
				}
				break;

			case 35:// --> TODO phase 4
				//	push new element on PARMCOUNT stack --> PARMCOUNT.top = 0
				PARAMCOUNT.push(0);
				// get access to procedure
				 etype = (ETYPE) this.semanticStack.pop();
				 ProcedureEntry entry = (ProcedureEntry) this.semanticStack.peek();
				// Put etype back
				 this.semanticStack.push(etype);
				//	: push new procedure element on NEXTPARM stack
				 nextParameter.push(entry.getParameterInfo()); // set NEXTPARM = id.parminfo {info about parameters}
				 break;

			case 36: // TODO: phase 4
				// check for etypes
				if (semanticStack.peek() instanceof ETYPE) {
					// pop the etype
					this.semanticStack.pop();
				}
				// get the entry --> pop id
				ProcedureEntry entry1 = (ProcedureEntry) this.semanticStack.pop();
				// if id.number_of_parameters <> 0, ERROR
				if(entry1.getNumberOfParms() != 0){
					throw SemanticError.WrongNumParameters(entry1.getName(), tokenizer.getLineNumber());
				}
				//gen("call", id, 0);
				generate("call", entry1, 0);
				break;

			case 37: // TODO: phase 4
				// pop etype
				etype = (ETYPE) this.semanticStack.pop();
				// if ETYPE <> ARITHMETIC, ERROR
				checkEtypeArithmetic(etype);
				// get rid of extra etypes
				if (this.semanticStack.peek() instanceof ETYPE){
					this.semanticStack.pop();
				}

				id1 = (SymbolTableEntry) this.semanticStack.peek();
				// if  NOT (id.is_variable OR id.is_constant OR id.is_function_result OR id.is_array), ERROR
				if(!(id1.isVariable() || id1.isConstant() || id1.isFunctionResult() || id1.isArray())){
					throw SemanticError.TypeMismatch(tokenizer.getLineNumber());
				}

				// increment PARMCOUNT.top
				int newCount = this.PARAMCOUNT.pop();
				this.PARAMCOUNT.push(newCount + 1);

				// if proc_or_fun.name <> READ or WRITE:
				SymbolTableEntry procedureOrFunc = (SymbolTableEntry) this.procedureOrFunction();
				if(!(procedureOrFunc.getName().equals("READ") || procedureOrFunc.getName().equals("WRITE"))){
					// if PARMCOUNT.top > proc_or_fun.number_of_parameters, ERROR
					if(this.PARAMCOUNT.peek() > procedureOrFunc.getNumberOfParms()){
						throw SemanticError.WrongNumberParms(procedureOrFunc.getName(), tokenizer.getLineNumber());
					}

					ParmInformation info1 = nextParameter.getNextParm();
					// if id.type <> NEXTPARM.type, ERROR
					if(!(this.typecheckParams(id1.getType(), info1.getType()))){
						throw SemanticError.TypeMismatch(tokenizer.getLineNumber());
					}

					// if NEXTPARM.array = TRUE,
					if(info1.getArray()){
						ArrayEntry array = (ArrayEntry) this.bottomStackArray();
						// if id.lbound <> NEXTPARM.lbound --> ERROR
						if(array.getLBound() != info1.getLowerBound()){
							throw SemanticError.BadArrayBounds(tokenizer.getLineNumber());
						}
						// id.ubound <> NEXTPARM.ubound --> ERROR
						if(array.getUBound() != info1.getUpperBound()) {
							throw SemanticError.BadArrayBounds(tokenizer.getLineNumber());
						}
					}

					// increment NEXTPARM
					nextParameter.increment();
				}
			break;

			case 38:
				// pop etype
				etype = (ETYPE) semanticStack.pop();
				// make sure it's arithmetic
				checkEtypeArithmetic(etype);
				// Push the token operator
				semanticStack.push(token);
				break;

			case 39:
				// pop etype
				etype = (ETYPE) semanticStack.pop();
				//  if ETYPE <> ARITHMETIC, ERROR
				checkEtypeArithmetic(etype);
				// pop id's and operator
				id2 = (SymbolTableEntry) semanticStack.pop();
				operator = (Token) semanticStack.pop();
				id1 = (SymbolTableEntry) semanticStack.pop();
				// if TYPECHECK(id1,id2) = 2,
				if (typecheck(id1, id2) == 2) {
					// CREATE(TEMP1,REAL)
					$$TEMP = create("$$TEMP" + counterTemp, TokenType.REAL);
					counterTemp++;
					// GEN(ltof,id2,$$TEMP1)
					generate("ltof", id2, $$TEMP);
					// GEN(***,id1,$$TEMP1,_)   {*** replaced by blt, ble, bgt, etc.}
					generate(operatorToOperandString(operator), id1, $$TEMP, "_");
				} else if (typecheck(id1, id2) == 3) {
					// CREATE(TEMP1,REAL)
					$$TEMP = create("$$TEMP" + counterTemp, TokenType.REAL);
					counterTemp++;
					// GEN(ltof,id1,$$TEMP1)
					generate("ltof", id1, $$TEMP);
					// GEN(***,$$TEMP1,id2,_)
					generate(operatorToOperandString(operator), $$TEMP, id2, "_");
				} else {
					// GEN(***,id1,id2,_)
					generate(operatorToOperandString(operator), id1, id2, "_");
				}
				// GEN(goto _)
				generate("goto", "_");
				//  E.TRUE  = MAKELIST(NEXTQUAD - 2)
				ETRUE_1 = makeList(quadruples.getNextQuad() - 2);
				//  E.FALSE = MAKELIST(NEXTQUAD - 1)
				EFALSE_1 = makeList(quadruples.getNextQuad() - 1);
				// push E.TRUE, E.FALSE
				semanticStack.push(ETRUE_1);
				semanticStack.push(EFALSE_1);
				// push ETYPE(RELATIONAL)
				semanticStack.push(ETYPE.relational);
			break;

			case 40:
				// push the sign (which is the token) onto the semantic stack
				semanticStack.push(token);
				break;

			case 41:
				// pop etype
				etype = (ETYPE) semanticStack.pop();
				// if Etype <> ARITHMETIC --> ERROR
				checkEtypeArithmetic(etype);
				id1 = (SymbolTableEntry) semanticStack.pop();
				// if sign {on stack} = UNARYMINUS:
				Token sign = (Token) semanticStack.pop();
				if (sign.getType() == TokenType.UNARYMINUS) {
					// CREATE(TEMP)
					$$TEMP = create("$$TEMP" + counterTemp, TokenType.INTEGER);
					counterTemp++;
					// if id.type = INTEGER,
					if (id1.getType() == TokenType.INTEGER) {
						// GEN(uminus, id, temp)
						generate("uminus", id1, $$TEMP);
					} else {
						// else GEN (fuminus, id, temp)
						generate("fuminus", id1, $$TEMP);
					}
					//-push TEMP on stack
					semanticStack.push($$TEMP);
				} else {
					// else pop sign, id; push id
					semanticStack.push(id1);
				}

				// push Etype(ARITHMETIC)
				semanticStack.push(ETYPE.arithmetic);

			break;

			case 42:
				// pop etype
				etype = (ETYPE) semanticStack.pop();
				// if our token operator is OR
				if (token.getOpType() == Token.OperatorType.OR) {
					// if ETYPE != RELATIONAL -> error
					checkEtypeRelational(etype);
					EFALSE_1 = (List) semanticStack.peek();
					backpatch(EFALSE_1, quadruples.getNextQuad());
				} else {
					// check that etype == arithmetic
					checkEtypeArithmetic(etype);
				}
				// push operator -->aka token
				semanticStack.push(token);
				break;

			case 43:
				// pop id's, operator, ETYPE
				etype = (ETYPE) semanticStack.pop();
				// if etype == relational
				if (etype == ETYPE.relational) {
					// Pop E(2) FALSE
					EFALSE_2 = (List) semanticStack.pop();
					// Pop E2 TRUE
					ETRUE_2 = (List) semanticStack.pop();
					// Pop operator
					operator = (Token) semanticStack.pop();
					// if operator is OR
					if (operator.getOpType() == Token.OperatorType.OR) {
						// pop E(1) FALSE
						EFALSE_1 = (List) semanticStack.pop();
						// pop E(1) TRUE
						ETRUE_1 = (List) semanticStack.pop();
					    // E.TRUE = MERGE (E(1).TRUE, E(2).TRUE)
						List ETRUE = merge(ETRUE_1, ETRUE_2);
						// E.FALSE = E(2).FALSE {on stack}
						List EFALSE = EFALSE_2;
						// push E.TRUE, E.FALSE, ETYPE(RELATIONAL)
						semanticStack.push(ETRUE);
						semanticStack.push(EFALSE);
						// push ETYPE(RELATIONAL)
						semanticStack.push(ETYPE.relational);
					} else {
						// put things back if not OR
						semanticStack.push(operator);
						semanticStack.push(ETRUE_2);
						semanticStack.push(EFALSE_2);
					}
				} else {
					id2 = (SymbolTableEntry) semanticStack.pop();
					operator = (Token) semanticStack.pop();
					id1 = (SymbolTableEntry) semanticStack.pop();
					// if ETYPE <> ARITHMETIC, ERROR
					checkEtypeArithmetic(etype);
					generateAndStoreSM43(id1, id2, operator);
					// push ETYPE(ARITHMETIC)
					semanticStack.push(ETYPE.arithmetic);
				}
				break;

			case 44:
				// pop ETYPE
				etype = (ETYPE) semanticStack.pop();
				// if etype is relational
				if (etype == ETYPE.relational) {
					// if operator is AND
					if (token.getOpType() == Token.OperatorType.AND) {
						// Get access to what we need
						EFALSE_1 = (List) semanticStack.pop();
						ETRUE_1 = (List) semanticStack.peek();
						// push this back where we found it
						semanticStack.push(EFALSE_1);
						//  BACKPATCH (E.TRUE, NEXTQUAD)
						backpatch(ETRUE_1, quadruples.getNextQuad());
					}
				}
				// push operator --> assuming Token
				semanticStack.push(token);
				break;

			case 45:
				// pop etype
				etype = (ETYPE) semanticStack.pop();
				// if etype is relational
				if (etype == ETYPE.relational) {
					// pop E2FALSE
					EFALSE_2 = (List) semanticStack.pop();
					// pop E2TRUE
					ETRUE_2 = (List) semanticStack.pop();
					// pop Operator
					operator = (Token) semanticStack.pop();
					// if operator is AND
					if (operator.getOpType() == Token.OperatorType.AND) {
						// pop EFALSE1
						EFALSE_1 = (List) semanticStack.pop();
						// pop ETRUE1
						ETRUE_1 = (List) semanticStack.pop();
						// E.TRUE =  E(2).TRUE
						List ETRUE = ETRUE_2;
						// E.FALSE = MERGE (E(1).FALSE, E(2).FALSE)
						List EFALSE = merge(EFALSE_1, EFALSE_2);
						// push E.TRUE, E.FALSE, ETYPE(RELATIONAL)
						semanticStack.push(ETRUE);
						semanticStack.push(EFALSE);
						// push ETYPE(RELATIONAL)
						semanticStack.push(ETYPE.relational);
					}
				} else {
					//  if ETYPE <> ARITHMETIC, ERROR
					checkEtypeArithmetic(etype);
					// pop id's and operator
					id2 = (SymbolTableEntry) semanticStack.pop();
					// // for unary minus
				//	Object object = semanticStack.peek();
					// pop off unary operator
//					if (object instanceof Token) { --> TODO: MAYBE PUT BACK ??
//						if (((Token) object).getType() == TokenType.UNARYMINUS) {
//							// all is good
//							// TODO: WHERE DO WE PUT OFFSET>> set offset ????
//							semanticStack.pop();
//						}
//					}
					operator = (Token) semanticStack.pop();
					id1 = (SymbolTableEntry) semanticStack.pop();
					// if TYPECHECK(id1,id2) <> 0 and operator = MOD,
					if (typecheck(id1, id2) != 0 && operator.getOpType() == Token.OperatorType.MOD) {
						// Error b/c mod requires integer operands
						throw SemanticError.BadMODoperands(tokenizer.getLineNumber());
					}
					// handle generation and storing of code
					generateAndStoreSM45(id1, id2, operator);
					//: push ETYPE(ARITHMETIC)
					semanticStack.push(ETYPE.arithmetic);
				}
				break;

			case 46:
				// if token is an identifier,
				if (token.getType() == TokenType.IDENTIFIER) {
					lookupIdentifierAndPushOrError(token);
				// if token is a constant
				} else if (token.getType() == TokenType.INTCONSTANT ||
					token.getType() == TokenType.REALCONSTANT) {
					lookupAndHandleConstant(token);
				}
				// push ETYPE(ARITHMETIC)
				semanticStack.push(ETYPE.arithmetic);
				break;

			case 47:
				//  pop etype
				etype = (ETYPE) semanticStack.pop();
				//  if ETYPE <> RELATIONAL, ERROR
				checkEtypeRelational(etype);
				// Popping EFALSE and setting it equal to ETRUE_1
				ETRUE_1 = (List) semanticStack.pop();
				//  Popping ETRUE and setting it equal to EFALSE_1
				EFALSE_1 = (List) semanticStack.pop();
				// push new E.TRUE_1, E.FALSE_1
				semanticStack.push(ETRUE_1);
				semanticStack.push(EFALSE_1);
				//  push ETYPE(RELATIONAL)
				semanticStack.push(ETYPE.relational);
				break;

			case 48:
				// pop offset, ETYPE
				if (semanticStack.peek() instanceof ETYPE) {
					etype = (ETYPE) semanticStack.pop();
				}
				offset = (SymbolTableEntry) semanticStack.pop();
				// if offset (on stack) <> NULL,
				if (offset != null) {
					// Check if it's a function
					if (offset.isFunction()) {
						// if so, execute action 52
						execute(52, token);
					} else {
						// pop id
						id1 = (SymbolTableEntry) semanticStack.pop();
						// CREATE(TEMP,id.type)
						$$TEMP = create("$$TEMP" + counterTemp, id1.getType());
						counterTemp++;
						// GEN(load id,offset,$$TEMP)
						generate("load", id1, offset, $$TEMP);
						// push $$TEMP
						semanticStack.push($$TEMP);
					}
				}
				// push ETYPE(ARITHMETIC))
				semanticStack.push(ETYPE.arithmetic);
				break;

			case 49: // TODO: PHASE 4
				// check etype
				etype = (ETYPE) this.semanticStack.pop();
				checkEtypeArithmetic(etype);
				// get access to id
				SymbolTableEntry id = (SymbolTableEntry) this.semanticStack.peek();
				this.semanticStack.push(etype);

				// if not id.is_function, ERROR
				if (!(id.isFunction())) {
					throw SemanticError.TypeMismatch(tokenizer.getLineNumber());
				}

				//  PARMCOUNT.top = 0
				PARAMCOUNT.push(0);
				FunctionEntry funID = (FunctionEntry) id;
				// push new element on NEXTPARM stack --> set NEXTPARM = id.parminfo
				this.nextParameter.push(funID.getParameterInfo());
				break;

			case 50: // TODO: PHASE 4
				// for each id on stack: {NOTE: must be done from bottom to top}
				if(this.semanticStack.peek() instanceof SymbolTableEntry){
					id = (SymbolTableEntry) this.semanticStack.peek();
					Stack<SymbolTableEntry> newStack = new Stack<SymbolTableEntry>();
					while(true){
						newStack.push(id);
						this.semanticStack.pop();
						if(this.semanticStack.peek() instanceof SymbolTableEntry){
							id = (SymbolTableEntry) this.semanticStack.peek();
						}
						else break;
					}

					while(!(newStack.empty())){
						// -pop id
						SymbolTableEntry tok = newStack.pop();
						// generate(param, id)
						generate("param", tok);
						// -LOCAL_MEM = LOCAL_MEM + 1
						this.localMemory++;
					}

					count = this.PARAMCOUNT.pop();

					//pop ETYPE
					etype = (ETYPE) this.semanticStack.pop();
					// pop entry
					SymbolTableEntry entry2 = (SymbolTableEntry) this.semanticStack.pop();

					// f PARMCOUNT.top > id.number_of_parameters, ERROR
					if(count > entry2.getNumberOfParms()){
						throw SemanticError.WrongNumParameters(entry2.getName(), tokenizer.getLineNumber());
					}

					// GEN(call id, PARMCOUNT)
					generate("call", entry2, count);
					this.nextParameter.pop();

					// CREATE(TEMP,id.type)
					SymbolTableEntry temp = create("$$TEMP" + counterTemp, entry2.getType());
					counterTemp++;

					// GEN(move,id.result,$$TEMP) {id.result is $$function-name}
					generate("move", entry2.getFunctionResult(), temp);
					// push $$TEMP
					this.semanticStack.push(temp);
					// push ETYPE(ARITHMETIC)
					this.semanticStack.push(ETYPE.arithmetic);
				}
				break;


			case 51: // --> TODO: PHASE 4
				 entry = (ProcedureEntry) procedureOrFunction();
				 // if id.name = READ call #51READ
				 if(entry.getName().equals("READ")){
					 read51(token);
					 // if id.name = WRITE call #51WRITE
				 } else if (entry.getName().equals("WRITE")) {
					 write51(token);
				 } else {
					 // if PARMCOUNT.top <> id.number_of_parameters, ERROR --> ??paramNUms
					 if (this.PARAMCOUNT.peek() != entry.getParameterInfo().size()) {
						 throw SemanticError.WrongNumberParms(entry.getName(), tokenizer.getLineNumber());
					 }
					 Stack<SymbolTableEntry> tempStack = new Stack();
					 //  for each parameter (id) on stack:
					 if (this.semanticStack.peek() instanceof SymbolTableEntry) {
						 //  (NOTE: must be done from bottom to top)
						 SymbolTableEntry id3 = (SymbolTableEntry) semanticStack.peek();
						 while (true) {
							 tempStack.push(id3);
							 this.semanticStack.pop();
							 if (semanticStack.peek() instanceof SymbolTableEntry) {
								 id = (SymbolTableEntry) semanticStack.peek();
							 } else {
								 break;
							 }
						 }
					 }

					 while (!tempStack.empty()) {
						 // -pop id
						 SymbolTableEntry ent = tempStack.pop();
						 // GEN(param id)
						 generate("param", ent);
						 // LOCAL_MEM = LOCAL_MEM + 1
						 this.localMemory++;
					 }

					 // GEN(call,id, PARMCOUNT.top) --> pop PARMCOUNT.top
					 generate("call", entry, this.PARAMCOUNT.pop());

					 // pop NEXTPARM.top,
					 this.nextParameter.pop();

					 //pop ETYPE
					 if(this.semanticStack.peek() instanceof ETYPE){
						 this.semanticStack.pop();
					 }

					 //pop procedureEntry
					 this.semanticStack.pop();
				 }
				 break;

			case 52:// TODO: Phase 4
				// pop ETYPE, id
				etype = (ETYPE) semanticStack.pop();
				id1 = (SymbolTableEntry) semanticStack.pop();
				//if not id.is_function, ERROR
				if (!id1.isFunction()) {
					throw SemanticError.TypeMismatch(tokenizer.getLineNumber());
				}
				if (id1.getNumberOfParms() > 0) {
					throw SemanticError.WrongNumberParms(id1.getName(), tokenizer.getLineNumber());
				}
				// GEN(call id, 0)
				generate("call", id1, 0);
				// CREATE(TEMP,id.type)
				SymbolTableEntry $$TEMP1 = create("$$TEMP" + counterTemp, id1.getType());
				// GEN(move,id.result,$$TEMP) {id.result is $$function-name} TODO: why not getResult??
				generate("move", id1.getFunctionResult(), $$TEMP1);
				// push $$TEMP
				semanticStack.push($$TEMP1);
				//  push ETYPE(ARITHMETIC)
				semanticStack.push(ETYPE.arithmetic);
				break;

			case 53:
				// pop ETYPE, id
				etype = (ETYPE) semanticStack.pop();
				id1 = (SymbolTableEntry) semanticStack.pop();
				// if id.is_function
				if (id1.isFunction()) {
					// if id <> CURRENTFUNCTION, ERROR
					if (id1 != CURRENTFUNCTION) {
						throw SemanticError.IllegalFunctionName(id1.getName(), tokenizer.getLineNumber());
					}
					// -push id.result {i.e., $$function-name}
					functionEntry = (FunctionEntry) id1;
					semanticStack.push(functionEntry.getFunctionResult());
				} else {
					semanticStack.push(id1);
					// -push ETYPE(ARITHMETIC)
					semanticStack.push(etype);
				}
				break;

			case 54:
				// maybe we actually pop it? TODO
				Object topStack = this.semanticStack.peek();
				// getting access to the stuff we need
				if (this.semanticStack.peek() instanceof ETYPE) {
					etype = (ETYPE) this.semanticStack.pop();
					topStack = this.semanticStack.peek();
					this.semanticStack.push(etype);
				}
				id1 = (SymbolTableEntry) topStack;
				// if not id.is_procedure, ERROR
				if (!(id1.isProcedure())) {
					throw SemanticError.IllegalProcedureCall(id1.toString(), tokenizer.getLineNumber());
				}
				break;

			case 55:
				backpatch(GLOBAL_STORE, globalMemory);
				// Free global memory
				generate("free", globalMemory);
				generate("PROCEND");
				break;

			case 56:
				// GEN(PROCBEGIN main)
				generate("PROCBEGIN", "main");
				// GLOBAL_STORE = NEXTQUAD
				GLOBAL_STORE = quadruples.getNextQuad();
				// GEN(alloc,_)
				generate("alloc", "_");
				break;

			case 57:
				// lookup token's value in the constant table
				ConstantEntry constantEntry = lookupConstant(token);
					// if null, insert value into constant table with type INTEGER
				if (constantEntry == null) {
					constantEntry = new ConstantEntry(token.getValue(), TokenType.INTEGER);
					insert(constantTable, constantEntry);
				}
				// set token.entry to constant table entry for the value
				token.setEntry(constantEntry);
				break;

			case 58:
				// lookup token's value in the constant table
				ConstantEntry constEntry = lookupConstant(token);
				// if null, insert value into constant table with type REAL
				if (constEntry == null) {
					constEntry = new ConstantEntry(token.getValue(), TokenType.REAL);
				}
				// set token.entry to constant table entry for the value
				token.setEntry(constEntry);
				break;

			default:
				// TODO Eventually (i.e. final project) this should throw an exception.
				debug("Action " + actionNumber + " not yet implemented.");

		}
		System.out.print("AFTER STACK: ");
		semanticStackDump();
	}

	public SymbolTableEntry lookup(String name)
	{
		if (global) {
			return globalTable.lookup(name.toUpperCase());
		} else {
			SymbolTableEntry entry = localTable.lookup(name.toUpperCase());
			// if not in local table
			if (entry == null) {
				// check global table
				return globalTable.lookup(name.toUpperCase());
			} else {
			return entry;
			}
		}
	}

	public ConstantEntry lookupConstant(Token token)
	{
		return (ConstantEntry) constantTable.lookup((token.getValue()).toUpperCase());
	}

	// For inserting in the tables:
	private void insert(SymbolTable table, SymbolTableEntry entry) throws SymbolTableError, SemanticError{
		// Conform everything to uppercase
		entry.setName(entry.getName().toUpperCase());
		if (table.lookup(entry.getName()) != null) {
			// If the name is reserved, throw an error because we can't have reserved things
			if (lookup(entry.getName()).isReserved()) {
				throw SemanticError.ReservedName(entry.getName(), tokenizer.getLineNumber());
			} else {
				// If the name has already been taken, throw an error because we can't declare
				// stuff multiple times
				throw SemanticError.MultipleDeclaration(entry.getName(), tokenizer.getLineNumber());
			}
		}
		// Insert the entry in the table if all is good and no errors are thrown.
		table.insert(entry);
	}

	// For Semantic Action #3:

	private int calculateArraySize(ConstantEntry ub, ConstantEntry lb) {
		// Calculate the size of the array
		System.out.println("Upper Bound: " + ub + " Lower Bound: " + lb);
		return (getNumberValueOfToken(ub) - getNumberValueOfToken(lb)) + 1;

	}

	private int getNumberValueOfToken(ConstantEntry t) {
		return Integer.parseInt(t.getName());
	}

	private void insertArrayEntriesIntoTable(int size, Token top, TokenType TYP, ConstantEntry ub, ConstantEntry lb) throws SemanticError, SymbolTableError {
		// add it to the appropriate symbol table
		if (global) { // if global flag is set to true -> global id
			// Insert ID as Array Entry & set ID address to globalMemory & set the type to TYP
			ArrayEntry entry = new ArrayEntry (top.getValue(), globalMemory, TYP,
					getNumberValueOfToken(ub), getNumberValueOfToken(lb));
			// Insert it into the table
			insert(globalTable, entry);
			// Increment globalMemory appropriately
			globalMemory = globalMemory + size;
		} else { // if global flag is set to false -> local id
			// Insert ID as Array Entry & set ID address to localMemory & set the type to TYP
			ArrayEntry entry = new ArrayEntry(top.getValue(), localMemory, TYP,
					getNumberValueOfToken(ub),getNumberValueOfToken(lb));
			// Insert it into the table
			insert(localTable, entry);
			// increment localMemory appropriately
			localMemory = localMemory + size;
		}
	}

	private void saveArrayIDs(TokenType TYP, ConstantEntry ub, ConstantEntry lb) throws SymbolTableError, SemanticError {
		// Assuming Token on top of the stack --> if we get errors, we'll add in more conditions
		int size = calculateArraySize(ub, lb);
		do {
			Token top = (Token) semanticStack.pop();
			System.out.println("SAVING ARRAY ENTRIES: " + top);
			insertArrayEntriesIntoTable(size, top, TYP, ub, lb);
		} while (!semanticStack.isEmpty());
	}

	private void insertVariableIntoTable(Token top, TokenType TYP) throws SemanticError, SymbolTableError{
		// add it to the appropriate symbol table
		if (global) { // if global flag is set to true -> global id
			// Insert ID as Variable Entry & set ID address to globalMemory & set the type to TYP
			VariableEntry entry = new VariableEntry(top.getValue(), globalMemory, TYP);
			// Insert the entry into the global table
			insert(globalTable, entry);
			// Increment globalMemory appropriately
			globalMemory = globalMemory + 1;
		} else { // if global flag is set to false -> local id
			System.out.println("SAVING AS LOCAL");
			// Insert ID as Variable Entry & set ID address to localMemory & set the type to TYP
			VariableEntry entry = new VariableEntry(top.getValue(), localMemory, TYP);
			// Insert the entry into the local table
			insert(localTable, entry);
			// increment localMemory appropriately
			localMemory = localMemory + 1;
		}
	}

	private void saveVariableIDs(TokenType TYP) throws SymbolTableError, SemanticError {
		// Assuming Token on top of the stack --> if we get errors, we'll add in more conditions
		do {
			Token top = (Token) semanticStack.pop();
			System.out.println("SAVING VARIABLE IDs: " + top);
			insertVariableIntoTable(top, TYP);
		} while (!semanticStack.isEmpty() && semanticStack.peek() instanceof Token);
	}

	// For Semantic Action #9:
	// Figure out what we are supposed to do here ??

	private void insertFileIOID(Token t) throws SymbolTableError, SemanticError {
		// Create IODeviceEntry entry with given id as name
		IODeviceEntry entry = new IODeviceEntry(t.getValue());
		// Mark as reserved
		entry.setIsReserved(true);
		// Mark as a variable
		entry.setIsVariable(true);
		// Insert the device into the globalTable
		insert(globalTable, entry);
	}

	private void insertProcedure(Token t) throws SymbolTableError, SemanticError {
		// Create a procedure with 0 entries
		ProcedureEntry entry = new ProcedureEntry(t.getValue(), 0, new LinkedList<>());
		// Mark it as restricted / reserved
		entry.setIsReserved(true);
		// Insert it into the global table
		insert(globalTable, entry);
	}

	// For generating code:
	// Generates a new quadruple containing the instruction given in TVICODE.
	// Overloaded function

	// Creates a new memory location -->
	private VariableEntry create(String NAME, TokenType TYPE) throws SymbolTableError, SemanticError {
		// Create variable entry named $$NAME with NAME as name, TYPE as type, and
		// address as negative value of GLOBAL_MEM
		// --- Using $$ so it cannot clash with programmer's var names
		// --- Negative mem location so we know it’s temporary variable.
		VariableEntry $$NAME = new VariableEntry(NAME, -globalMemory, TYPE);
		// insert into global table
		insert(globalTable, $$NAME);
		// increment GLOBAL_MEM
		globalMemory++;
		// return $$NAME^
		return $$NAME;
	}

	// For Semantic Action #31:
	private void checkForErrors(ETYPE e, SymbolTableEntry id1, SymbolTableEntry id2) throws SemanticError{
		// if ETYPE not equal to ARITHMETIC... ERROR
		checkEtypeArithmetic(e);
		// if TYPECHECK(id1,id2) = 3 ...
		if (typecheck(id1, id2) == 3) {
			// ERROR
			throw SemanticError.TypeMismatch(tokenizer.getLineNumber());
		}
	}

	private void generateAndStoreSM31(SymbolTableEntry id1, SymbolTableEntry id2,
								 SymbolTableEntry offset) throws SemanticError, SymbolTableError {

		// if typecheck(id1, id2) == 3
		if (typecheck(id1, id2) == 3) {
			// CREATE(TEMP, REAL)
			SymbolTableEntry $$TEMP = create("$$TEMP" + counterTemp, TokenType.INTEGER);
			counterTemp++;
			generate("ftol", id2, $$TEMP);
			if(offset == null){
				generate("move", $$TEMP, id1);
			}
			else{
				generate("stor", $$TEMP, offset, id1);
			}

			// if TYPECHECK(id1, id2) = 2 --> id1 = real and id2 = integer
		} else if (typecheck(id1, id2) == 2) {
			// CREATE(TEMP, REAL)
			SymbolTableEntry $$TEMP = create("$$TEMP" + counterTemp, TokenType.REAL);
			counterTemp++;
			// GEN(ltof,id2,$$TEMP)
			generate("ltof", id2, $$TEMP);
			// if offset == null
			if (offset == null) {
				// GEN(move,$$TEMP,id1)
				generate("move", $$TEMP, id1);
			} else {
				//	GEN(stor $$TEMP,offset,id1)
				generate("stor", $$TEMP, offset, id1);
			}
		} else {
			if (offset == null) {
				// GEN(move,id2,id1)
				generate("move", id2, id1);
			} else {
				// GEN(stor id2,offset,id1)
				generate("stor", id2, offset, id1);
			}
		}
	}

	// For semantic action #43
	private void generateAndStoreSM43(SymbolTableEntry id1, SymbolTableEntry id2, Token operator)
			throws SemanticError, SymbolTableError{
		SymbolTableEntry $$TEMP1 = nullEntry;
		SymbolTableEntry $$TEMP2 = nullEntry;
		switch(typecheck(id1, id2)) {
			//if TYPECHECK(id1,id2) = 0 --> both id1 and id2 are integers
			case 0:
				// CREATE(TEMP,INTEGER)
				$$TEMP1 = create("$$TEMP" +
						counterTemp, TokenType.INTEGER);
				counterTemp++;
				// GEN(***,identifier1,identifier2,$$TEMP) {*** replaced by add, sub, etc.}
				generate(operatorToOperandString(operator), id1, id2, $$TEMP1);
				// push result variable
				semanticStack.push($$TEMP1);
				break;
			// if TYPECHECK(id1,id2) = 1
			case 1:
				// CREATE(TEMP,REAL)
				$$TEMP1 = create("$$TEMP" +
						counterTemp, TokenType.REAL);
				counterTemp++;
				// GEN(f***,identifier1,identifier2,$$TEMP)
				generate("f" + operatorToOperandString(operator), id1, id2, $$TEMP1);
				// push result variable
				semanticStack.push($$TEMP1);
				break;
			// if TYPECHECK(id1,id2) = 2
			case 2:
				// CREATE(TEMP1,REAL)
				$$TEMP1 = create("$$TEMP" + counterTemp, TokenType.REAL);
				counterTemp++;
				// GEN(ltof,id2,$$TEMP1)
				generate("ltof", id2, $$TEMP1);
				// CREATE(TEMP2,REAL)
				$$TEMP2 = create("$$TEMP" + counterTemp, TokenType.REAL);
				counterTemp++;
				// GEN(f***,id1,$$TEMP1,$$TEMP2)
				generate("f" + operatorToOperandString(operator), id1, $$TEMP1, $$TEMP2);
				// push result variable
				semanticStack.push($$TEMP2);
				break;
			// TYPECHECK(id1,id2) = 3
			case 3:
				// CREATE(TEMP1,REAL)
				$$TEMP1 = create("$$TEMP" + counterTemp, TokenType.REAL);
				counterTemp++;
				// GEN(ltof,id1,$$TEMP1)
				generate("ltof", id1, $$TEMP1);
				// CREATE(TEMP2,REAL)
				$$TEMP2 = create("$$TEMP" + counterTemp, TokenType.REAL);
				counterTemp++;
				// GEN(f***,$$TEMP1,identifier2,$$TEMP2)
				generate("f" + operatorToOperandString(operator), $$TEMP1, id2, $$TEMP2);
				// push result variable
				semanticStack.push($$TEMP2);
				break;

		}
	}

	private void generateAndStoreSM45(SymbolTableEntry id1, SymbolTableEntry id2, Token operator)
			throws SemanticError, SymbolTableError{

		switch(typecheck(id1,id2)) {
			// if TYPECHECK(id1,id2) = 0
			case 0:
				generateAndStore45A(id1, id2, operator);
				break;
			// if TYPECHECK(id1,id2) = 1,
			case 1:
				generateAndStore45B(id1, id2, operator);
				break;
			// if TYPECHECK(id1,id2) = 2,
			case 2:
				generateAndStore45C(id1, id2, operator);
				break;
			// if TYPECHECK(id1,id2) = 3,
			case 3:
				generateAndStore45D(id1, id2, operator);
				break;
		}

	}

	private void generateAndStore45A(SymbolTableEntry id1, SymbolTableEntry id2, Token operator) throws SemanticError, SymbolTableError{
		SymbolTableEntry $$TEMP1;
		SymbolTableEntry $$TEMP2;
		SymbolTableEntry $$TEMP3;
		/*if (operater == MOD) */
		if (operator.getOpType() == Token.OperatorType.MOD) {
			//	CREATE(TEMP1,INTEGER)
			$$TEMP1 = create("$$TEMP" + counterTemp, TokenType.INTEGER);
			counterTemp++;
			// GEN(move,id1,$$TEMP1)
			generate("move", id1, $$TEMP1);
			// CREATE(TEMP2,INTEGER)
			$$TEMP2 = create("$$TEMP"+counterTemp, TokenType.INTEGER);
			counterTemp++;
			// GEN(move,$$TEMP1,$$TEMP2)
			generate("move", $$TEMP1, $$TEMP2);
			// GEN(sub,$$TEMP2,id2,$$TEMP1)
			generate("sub", $$TEMP2, id2, $$TEMP1);
			// GEN(bge,$$TEMP1,id2,NEXTQUAD-2)
			generate("bge", $$TEMP1, id2, quadruples.getNextQuad() - 2); //{result will be in $$TEMP1}
			// push result variable
			semanticStack.push($$TEMP1);
		/*else if (operator == / ) */
		} else if (operator.getOpType() == Token.OperatorType.DIVIDE) {
			// CREATE(TEMP1,REAL)
			$$TEMP1 = create("$$TEMP" + counterTemp, TokenType.REAL);
			counterTemp++;
			// GEN(ltof,id1,$$TEMP1)
			generate("ltof", id1, $$TEMP1);
			// CREATE(TEMP2,REAL)
			$$TEMP2 = create("$$TEMP" + counterTemp, TokenType.REAL);
			counterTemp++;
			// GEN(ltof,id2,$$TEMP2)
			generate("ltof", id2, $$TEMP2);
			// CREATE(TEMP3,REAL)
			$$TEMP3 = create("$$TEMP" + counterTemp, TokenType.REAL);
			counterTemp++;
			// GEN(fdiv,$$TEMP1,$$TEMP2,$$TEMP3)
			generate("fdiv", $$TEMP1, $$TEMP2, $$TEMP3);
			// push result variable
			semanticStack.push($$TEMP3);
		} else {
			// CREATE(TEMP,INTEGER)
			$$TEMP1 = create("$$TEMP" + counterTemp, TokenType.INTEGER);
			counterTemp++;
			// GEN(***,id1,id2,$$TEMP)
			generate(operatorToOperandString(operator), id1, id2, $$TEMP1);
			// push result variable
			semanticStack.push($$TEMP1);
		}
	}

	private void generateAndStore45B(SymbolTableEntry id1, SymbolTableEntry id2, Token operator)
			throws SymbolTableError, SemanticError{
		SymbolTableEntry $$TEMP1;
		SymbolTableEntry $$TEMP2;
		SymbolTableEntry $$TEMP3;
		// if (operator == DIV) {
		if (operator.getOpType() == Token.OperatorType.INTEGERDIVIDE)  {
			// CREATE(TEMP1,INTEGER)
			$$TEMP1 = create("$$TEMP" + counterTemp, TokenType.INTEGER);
			counterTemp++;
			//GEN(ftol,id1,$$TEMP1)
			generate("ftol", id1, $$TEMP1);
			// CREATE(TEMP2,INTEGER)
			$$TEMP2 = create("$$TEMP" + counterTemp, TokenType.INTEGER);
			counterTemp++;
			// GEN(ftol,id2,$$TEMP2)
			generate("ftol", id2, $$TEMP2);
			// CREATE(TEMP3,INTEGER)
			$$TEMP3 = create("$$TEMP" + counterTemp, TokenType.INTEGER);
			counterTemp++;
			// GEN(div,$$TEMP1,$$TEMP2,$$TEMP3)
			generate("div", $$TEMP1, $$TEMP2, $$TEMP3);
			// push result variable
			semanticStack.push($$TEMP3);
		} else {
			// CREATE(TEMP,REAL)
			$$TEMP1 = create("$$TEMP" + counterTemp, TokenType.REAL);
			counterTemp++;
			// GEN(f***,id1,id2,$$TEMP)
			generate("f" + operatorToOperandString(operator), id1, id2, $$TEMP1);
			// push result variable
			semanticStack.push($$TEMP1);
		}
	}

	private void generateAndStore45C(SymbolTableEntry id1, SymbolTableEntry id2, Token operator)
			throws SemanticError, SymbolTableError {
		SymbolTableEntry $$TEMP1;
		SymbolTableEntry $$TEMP2;
		// if operator = DIV
		if (operator.getOpType() == Token.OperatorType.INTEGERDIVIDE) {
			// CREATE(TEMP1,INTEGER)
			$$TEMP1 = create("$$TEMP" + counterTemp, TokenType.INTEGER);
			counterTemp++;
			// GEN(ftol,id1,$$TEMP1)
			generate("ftol", id1, $$TEMP1);
			// CREATE(TEMP2,INTEGER)
			$$TEMP2 = create("$$TEMP" + counterTemp, TokenType.INTEGER);
			counterTemp++;
			// GEN(div,$$TEMP1,id2,$$TEMP2)
			generate("div", $$TEMP1, id2, $$TEMP2);
		} else {
			// CREATE(TEMP1,REAL)
			$$TEMP1 = create("$$TEMP" + counterTemp, TokenType.REAL);
			counterTemp++;
			// GEN(ltof,id2,$$TEMP1)
			generate("ltof", id2, $$TEMP1);
			// CREATE(TEMP2,REAL)
			$$TEMP2 = create("$$TEMP" + counterTemp, TokenType.REAL);
			counterTemp++;
			// GEN(f***,id1,$$TEMP1,$$TEMP2)
			generate("f" + operatorToOperandString(operator), id1, $$TEMP1, $$TEMP2);
		}
		// push result variable
		semanticStack.push($$TEMP2);
	}

	private void generateAndStore45D(SymbolTableEntry id1, SymbolTableEntry id2, Token operator) throws SymbolTableError, SemanticError{
		SymbolTableEntry $$TEMP1;
		SymbolTableEntry $$TEMP2;
		// if (operator == DIV) {
		if (operator.getOpType() == Token.OperatorType.INTEGERDIVIDE) {
			// CREATE(TEMP1,INTEGER)
			$$TEMP1 = create("$$TEMP" + counterTemp, TokenType.INTEGER);
			counterTemp++;
			// GEN(ftol,id2,$$TEMP1)
			generate("ftol", id2, $$TEMP1);
			// CREATE(TEMP2,INTEGER)
			$$TEMP2 = create("$$TEMP" + counterTemp, TokenType.INTEGER);
			counterTemp++;
			// GEN(div,id1,$$TEMP1,$$TEMP2)
			generate("div", id1, $$TEMP1, $$TEMP2);
		} else {
			// CREATE(TEMP1,REAL)
			$$TEMP1 = create("$$TEMP" + counterTemp, TokenType.REAL);
			counterTemp++;
			// GEN(ltof,id1,$$TEMP1)
			generate("ltof", id1, $$TEMP1);
			// CREATE(TEMP2,REAL)
			$$TEMP2 = create("$$TEMP" + counterTemp, TokenType.REAL);
			counterTemp++;
			// GEN(f***,$$TEMP1,id2,$$TEMP2)
			generate("f" + operatorToOperandString(operator), $$TEMP1, id2, $$TEMP2);
		}
		// push result variable
		semanticStack.push($$TEMP2);
	}

	// Semantic Action 46
	private void lookupIdentifierAndPushOrError(Token token) throws SemanticError {
		// - lookup in symbol table
		SymbolTableEntry id = lookup(token.getValue());
		// - if not found, ERROR (undeclared variable)
		if (id == nullEntry) {
			throw SemanticError.UndeclaredVariable(token.getValue(), tokenizer.getLineNumber());
		}
		// push id
		semanticStack.push(id);
	}

	private void lookupAndHandleConstant(Token token) throws SemanticError, SymbolTableError {
		// lookup in constant (symbol) table
		ConstantEntry c = lookupConstant(token);
		// - if not found, -->
		if (c == null) {
			// if tokentype = INTCONSTANT ->
			if (token.getType() == TokenType.INTCONSTANT) {
				//set type field to INTEGER
				c = new ConstantEntry(token.getValue(), TokenType.INTEGER);
			} else {
				// else set type field to REAL
				c = new ConstantEntry(token.getValue(), TokenType.REAL);
			}
			// insert in constant table
			insert(constantTable, c);
		}
		// push constant entry
		semanticStack.push(c);

	}

	// For semantic action 42
	// if ETYPE not equal to ARITHMETIC... ERROR
	private void checkEtypeArithmetic(ETYPE etype) throws SemanticError{
		if (etype != ETYPE.arithmetic) {
			throw SemanticError.badRelETYPE(tokenizer.getLineNumber());
		}
	}

	private void checkEtypeRelational(ETYPE etype) throws SemanticError {
		if (etype != ETYPE.relational) {
			throw SemanticError.ETypeMismatch(tokenizer.getLineNumber());
		}
	}

	// For semantic action 51
	private Object procedureOrFunction() {
		// convert semantic stack into array
		Object[] semanticArray = this.semanticStack.toArray();
		Object entry = null;
		// go retrieve the procedure or function
		for(int i = 0; i <semanticArray.length; i++){
			if(semanticArray[i] instanceof ProcedureEntry || semanticArray[i] instanceof FunctionEntry){
				entry = semanticArray[i];
				break;
			}
		}
		return entry;
	}

	public Object bottomStackArray(){
		Object[] semanticArray = this.semanticStack.toArray();
		Object entry = null;
		for(int i = 0; i < semanticArray.length; i++){
			if(semanticArray[i] instanceof ArrayEntry){
				entry = semanticArray[i];
				break;
			}
		}
		return entry;
	}

	public void killETYPES() {
		Object item = semanticStack.pop();
		while (item instanceof ETYPE && !semanticStack.isEmpty()) {
			item = semanticStack.pop();
		}
		// we've gone too far with our etypes
		semanticStack.push(item);
	}


	private boolean typecheckParams(TokenType typ1, TokenType typ2) {
		TokenType type1 = typ1;
		TokenType type2 = typ2;

		if (typ1 == TokenType.INTCONSTANT){
			type1 = TokenType.INTEGER;
		}
		if (typ2 == TokenType.INTCONSTANT){
			type2 = TokenType.INTEGER;
		}
		if (typ1 == TokenType.REALCONSTANT){
			type1 = TokenType.REAL;
		}
		if (typ2 == TokenType.REALCONSTANT){
			type2 = TokenType.REAL;
		}

		return type1 == type2;
	}

	// Used for generate
	// Checks the types of id1 and id2, and returns the following :
	private int typecheck(SymbolTableEntry id1, SymbolTableEntry id2) {
		if (id1.getType() == TokenType.INTEGER) {
			if (id2.getType() == TokenType.INTEGER) {
				// if id1 and id2 are both integers --> return 0
				return 0;
			} else {
				// if id1 is a Integer and id2 is a real --> return 3
				return 3;
			}
		} else {
			if (id2.getType() == TokenType.INTEGER) {
				// if id1 is a real and id2 is an integer --> return 2
				return 2;
			} else {
				// if id1 and id2 are reals --> return 1
				return 1;
			}
		}
	}

	public String getStringPrefix(SymbolTableEntry entry, String tvi) {
		String finalString = "ERROR_";
		String pString = "ERROR_";
		String gLString = "ERROR_";

		// getting pre-string
		if (tvi.equals("param")) {
			if (entry.isParameter()) {
				pString = "";
			}
			else {
				pString = "@";
			}
		} else {
			if (entry.isParameter()) {
				pString = "^";
			}
			else {
				pString = "";
			}
		}

		// getting next part of the string
		if (global) {
			if (this.globalTable.lookup(entry.getName()) != null) {
				gLString = "_";
			}
		} else {
			if (this.localTable.lookup(entry.getName()) != null) {
				gLString = "%";
			} else if (this.globalTable.lookup(entry.getName()) != null) {
				gLString = "_";
			}
		}

		finalString = pString + gLString;

		return finalString;
	}


	// Used for generate
	// 1. Handles putting constants in actual memory locations
	// 2. Replaces all id references with memory addresses
	private String opToAddress(String tvicode, SymbolTableEntry id) throws SymbolTableError, SemanticError {
		String address = "";
		if (id.isFunction() || id.isProcedure()) {
			address = address + id.getName().toLowerCase();
		} else {
			// // Check if id is a constant
			if (id.isConstant()) {
				// create(TEMP,id.type)
				SymbolTableEntry $$TEMP = create("$$TEMP" + counterTemp, id.getType());
				// increment counterTemp
				counterTemp++;
				generate("move", id.getName(), $$TEMP);
				Integer addr = Math.abs(new Integer($$TEMP.getAddress()));
				String addrString = addr.toString();
				// generate(move, id.value() == id.getValue() ,$$TEMP)
				address = address + this.getStringPrefix($$TEMP, tvicode) + addrString;
			} else {
				Integer addr = Math.abs(new Integer(id.getAddress()));
				String addrString = addr.toString();
				address = address + this.getStringPrefix(id, tvicode) + addrString;
			}
		}
		return address;
	}

	private String operatorToOperandString(Token operator) {
		operator.print();
		switch(operator.getOpType().toString()) {
			case "ADD": return "add";
			case "DIVIDE": return "div";
			case "MULTIPLY": return "mul";
			case "SUBTRACT": return "sub";
			case "LESSTHAN": return "blt";
			case "LESSTHANOREQUAL": return "ble";
			case "GREATERTHAN": return "bgt";
			case "GREATERTHANOREQUAL": return "bge";
			case "EQUAL": return "beq";
			case "NOTEQUAL": return "bne";
		}
		operator.print();
		return null;
	}


	// Creates a new list containing only i, an index into the array of quadruples. Returns
	// the list it has created
	public List makeList(int i) {
		LinkedList<Integer> list = new LinkedList();
		list.add(i);
		return list;
	}

	// Concatenates the lists pointed to by p1 and p2, returns a pointer to the
	// concatenated list.
	public List merge(List p1, List p2) {
		LinkedList<Integer> list = new LinkedList();
		list.addAll(p1);
		list.addAll(p2);
		return list;
	}

	// Inserts i in the second field of the statement at position p
	private void backpatch(int p, int i) {
		Integer field = new Integer(i);
		String[] quad = quadruples.getQuad(p);
		for (int j = 0; j < quad.length; j++) {
			if (quad[j] == "_") {
				quadruples.setField(p, j, field.toString());
			}
		}

	}

	// If given a list, fills in the target field of a given quadruple with values from an E.true or E.false list.
	private void backpatch(List<Integer> list, int i) {
		Integer field = new Integer(i);
		// Iterate through the list
		for (Integer p : list) {
			String[] quad = quadruples.getQuad(p);
			for (int j = 0; j < quad.length; j++) {
				if (quad[j] == "_") {
					quadruples.setField(p, j, field.toString());
				}
			}
		}
	}

	// Generate intermediate code

	// 4. All parameters are passed by reference. So, when generating PARAM statements,
	// GEN produces the following:
	// need an if param statement
	//
	//  PARAM @_n   for global memory
	//  PARAM @%n   for (local) variables in current stack frame
	//  PARAM %n    for parameters that are themselves parameters

	// Used in Semantic Action #9
	private void generate(String tviCode) {
		// Create the quad
		String[] quad = {tviCode, null, null, null};
		// Add the quad and increment
		quadruples.addQuad(quad);
	}

	// Used in Semantic Action #56
	private void generate(String tviCode, String operand1) {
		String[] quad = {tviCode, operand1, null, null};
		quadruples.addQuad(quad);
	}

	// Used in Semantic Action #55
	private void generate(String tviCode, int operand1) {
		String[] quad = {tviCode, String.valueOf(operand1), null, null};
		quadruples.addQuad(quad);
	}

	private void generate(String tviCode, SymbolTableEntry operand1) throws SymbolTableError, SemanticError {
		String operand1Address = opToAddress(tviCode, operand1);
		String[] quad = {tviCode, operand1Address, null, null};
		quadruples.addQuad(quad);
	}

	// Used in Semantic Action #31, 43, 45
	private void generate(String tviCode, SymbolTableEntry operand1, SymbolTableEntry operand2)
			throws SemanticError, SymbolTableError{
		// replaces all id references with memory addresses
		String operand1Address = opToAddress(tviCode, operand1);
		String operand2Address = opToAddress(tviCode, operand2);
		String[] quad = {tviCode, operand1Address, operand2Address, null};
		quadruples.addQuad(quad);
	}

	// Used for opToAddress
	private void generate(String tviCode1, String tviCode2, SymbolTableEntry operand1)
			throws SymbolTableError, SemanticError {
		String operand1Address = opToAddress(tviCode1, operand1);
		String[] quad = {tviCode1, tviCode2, operand1Address, null};
		quadruples.addQuad(quad);
	}

	// Used in Semantic Action #9. #55
	private void generate(String tviCode, SymbolTableEntry operand1, int operand2) throws SymbolTableError, SemanticError {
		String operand1Address = opToAddress(tviCode, operand1);
		String[] quad = {tviCode, operand1Address, String.valueOf(operand2), null};
		quadruples.addQuad(quad);
	}

	private void generate(String tviCode, String operand1, int operand2) throws SymbolTableError, SemanticError {
		String[] quad = {tviCode, operand1, String.valueOf(operand2), null};
		quadruples.addQuad(quad);
	}

	private void generate(String tviCode, SymbolTableEntry
			operand1, SymbolTableEntry operand2, SymbolTableEntry
								  operand3) throws SemanticError, SymbolTableError {
		// replaces all id references with memory addresses
		String operand1Address = opToAddress(tviCode, operand1);
		String operand2Address = opToAddress(tviCode, operand2); // for offset ??
		String operand3Address = opToAddress(tviCode, operand3);
		String[] quad = {tviCode, operand1Address, operand2Address, operand3Address};
		quadruples.addQuad(quad);
	}

	private void generate(String tviCode, SymbolTableEntry operand1, SymbolTableEntry operand2,
						  int operand3) throws SymbolTableError, SemanticError {
		String operand1Address = opToAddress(tviCode, operand1);
		String operand2Address = opToAddress(tviCode, operand2);
		String[] quad = {tviCode, operand1Address, operand2Address, String.valueOf(operand3)};
		quadruples.addQuad(quad);
	}

	// Semantic Action #33
	private void generate(String tviCode, SymbolTableEntry operand1, int operand2,
						  SymbolTableEntry operand3) throws SymbolTableError, SemanticError {
		String operand1Address = opToAddress(tviCode, operand1);
		String operand2Address = String.valueOf(operand2);
		String operand3Address = opToAddress(tviCode, operand3);
		String[] quad = {tviCode, operand1Address, operand2Address, operand3Address};
		quadruples.addQuad(quad);
	}

	// Semantic Action #39
	private void generate(String tviCode, SymbolTableEntry operand1, SymbolTableEntry operand2,
						  String s) throws SymbolTableError, SemanticError {
		String operand1Address = opToAddress(tviCode, operand1);
		String operand2Address = opToAddress(tviCode, operand2);
		String[] quad = {tviCode, operand1Address, operand2Address, s};
		quadruples.addQuad(quad);
	}

	private void read51(Token t) throws SymbolTableError, SemanticError {
		// for each parameter on stack:
		//(NOTE: must be done from bottom to top)
		if (semanticStack.peek() instanceof SymbolTableEntry) {
			SymbolTableEntry id = (SymbolTableEntry) this.semanticStack.peek();
			Stack<SymbolTableEntry> newStack = new Stack<SymbolTableEntry>();
			while (true) {
				newStack.push(id);
				this.semanticStack.pop();
				if (this.semanticStack.peek() instanceof SymbolTableEntry) {
					id = (SymbolTableEntry) this.semanticStack.peek();
				} else {
					break;
				}
			}

			// for each param on the stack..
			while (!(newStack.empty())) {
				// pop id
				SymbolTableEntry entry = newStack.pop();
				// -if id.type = REAL, GEN(finp,id)
				if (entry.getType() == TokenType.REAL) {
					generate("finp", entry);
				} else {
					// else GEN(inp,id)
					generate("inp", entry);
				}
			}

			// pop PARAMCOUNT
			this.PARAMCOUNT.pop();

			//pop ETYPE
			if (this.semanticStack.peek() instanceof ETYPE) {
				this.semanticStack.pop();
			}

			//pop proc entry
			ProcedureEntry proc = (ProcedureEntry) this.semanticStack.pop();
		}
	}

	private void write51(Token t) throws SymbolTableError, SemanticError {
		//	for each parameter on stack:
//		(NOTE: must be done from bottom to top)
		if (semanticStack.peek() instanceof SymbolTableEntry) {
			SymbolTableEntry id = (SymbolTableEntry) this.semanticStack.peek();
			Stack<SymbolTableEntry> newStack = new Stack<SymbolTableEntry>();
			while (true) {
				newStack.push(id);
				this.semanticStack.pop();
				if (this.semanticStack.peek() instanceof SymbolTableEntry) {
					id = (SymbolTableEntry) this.semanticStack.peek();
				} else {
					break;
				}
			}

			// for each parameter
			while (!(newStack.empty())) {
				// pop id
				SymbolTableEntry entry = newStack.pop();
				// if a real variable
			//	if (!(entry.getName().startsWith("$$"))) {
					// // -GEN(print,"<id.name> = ")   {<id.name> is the name of the variable}
					String tempString = ("\"" + entry.getName() + " = " + "\"");
					generate("print", tempString);
			//	}

				//-if id.type = REAL, GEN(foutp,id)
				if (entry.getType() == TokenType.REAL) {
					// go get the entries name
					generate("foutp", lookup(entry.getName()));
				} else {
					// else generate outp, id
					generate("outp", entry);
				}

				// generate newline
				generate("newl");
			}

			// pop PARAMCOUNT
			this.PARAMCOUNT.pop();

			//pop ETYPE
			if (this.semanticStack.peek() instanceof ETYPE) {
				this.semanticStack.pop();
			}

			//pop proc entry
			ProcedureEntry proc = (ProcedureEntry) this.semanticStack.pop();
		}
	}



	private void debug(String message) {
		System.out.println(message); // allows for debug output
	}

	// "Semantic stack dump" routine that prints the semantic stack contents
	public void semanticStackDump() {
		System.out.print("Contents of semantic stack: ");

		for(int i = 0; i<semanticStack.size(); i++){
			System.out.print(semanticStack.get(i) + " : ");
		}

		System.out.println();
		System.out.println("END OF CONTENTS");
	}

	public Quadruples getQuadruples() {
		return quadruples;
	}

	public void quadruplesDump() {
		System.out.println("QUADRUPLES:");
		quadruples.print();
	}



}