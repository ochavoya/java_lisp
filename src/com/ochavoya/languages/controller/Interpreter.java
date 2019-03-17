package com.ochavoya.languages.controller;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.ochavoya.languages.model.Lambda;
import com.ochavoya.languages.model.Symbol;
import com.ochavoya.languages.model.Token;

public class Interpreter implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Parser            parser;
	private long              id               = 0;
	private boolean           trace            = false;

	public Interpreter(InputStream in)
	{
		parser = new Parser(in);
	}

	public Parser getParser()
	{
		return parser;
	}

	private String                                              prompt        = ":: ";

	private Map<String, Symbol>                                 symbolTable   = new HashMap<String, Symbol>();
	private Map<String, Lambda>                                 functionTable = new HashMap<String, Lambda>();
	private Set<Symbol>                                         knowledgeBase = new HashSet<Symbol>();
	private Set<Symbol>                                         ruleBase      = new HashSet<Symbol>();
	private static final Map<String, String>                    help          = new HashMap<String, String>();
	private static final Map<Integer, Function<Symbol, Symbol>> predefined    = new HashMap<Integer, Function<Symbol, Symbol>>();

	public void setSymbolTable()
	{
		symbolTable = new HashMap<String, Symbol>();
		symbolTable.put("prompt", new Symbol(new Token(Token.ID, "::")));
	}

	private String getId()
	{
		return "@" + (id++);
	}

	public Symbol eval(Symbol symbol)
	{
		Symbol value;
		if (symbol.isAtom()) {
			if (trace) {
				System.out.print("atom: " + symbol);
			}
			value = processAtom.apply(symbol);
			if (trace) {
				System.out.println(", value: " + value);
			}
			return value;
		}

		Symbol function = symbol.getCar();
		if (trace) {
			System.out.println("function: " + function + ", expression: " + symbol);
		}
		if (function.isList()) {
			if (function.getCar().getType() == Token.LAMBDA)
				return predefined.get(Token.LAMBDA).apply(symbol);
		}

		int functionType = function.getType();

		switch (functionType) {
		case Token.END:
		case Token.NIL:
		case Token.QUIT:
			throw new EvaluationError(function + " is not a valid function");
		case Token.INVALID:
			throw new EvaluationError(function + " is not a valid symbol");
		case Token.NAND:
			value = predefined.get(Token.NOT).apply(predefined.get(Token.AND).apply(symbol));
			break;
		case Token.NOR:
			value = predefined.get(Token.NOT).apply(predefined.get(Token.OR).apply(symbol));
			break;
		default:
			Function<Symbol, Symbol> operation = predefined.get(functionType);
			if (operation == null) {
				throw new EvaluationError("Undefined: " + function);
			}
			value = operation.apply(symbol);
		}
		if (trace) {
			System.out.println("function: " + function + ", value: " + value);
		}
		return value;
	}

	private void checkId(Symbol id)
	{
		if (!id.isId())
			throw new EvaluationError(id + " is not a valid identifier");
	}

	private void checkList(Symbol list)
	{
		if (!list.isList())
			throw new EvaluationError(list + " is not a list");
	}

	private List<Symbol> getArgs(Symbol symbol, int n)
	{
		List<Symbol> list = symbol.getSymbolList();
		Symbol function = list.get(0);
		list.remove(0);

		if (list.size() != n + 1) {
			String errorMessage = help.get(function.getCode());
			if (errorMessage != null) {
				errorMessage = "There is no help about " + function + " at this moment";
				throw new EvaluationError(errorMessage);
			}
		}
		return list;
	}

	private void checkListOfIds(Symbol listOfVariables)
	{
		if (!listOfVariables.isListOfIds())
			throw new EvaluationError(listOfVariables + " is not a list of ids");
	}

	private static final String            unquotedArgumentsMessage = "Unquoted arguments are evaluated first. ";

	static {
		help.put("APPEND", "(APPEND <list1> <list2> ) merges the lists <list1> and <list2> into a single list: "
		        + "(APPEND '(a b c) '(c d e) ) results in the list (a b c c d e)." + unquotedArgumentsMessage + " So "
		        + "If we execute (APPEND (a b c) '(c d e)), the unquoted argument (a b c) will be interpreted as "
		        + "a call to execute a function named \"a\" (with arguments b and c) before doing the APPEND.");
	}

	private final Function<Symbol, Symbol> processAppend            = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 2);
		                                                                Symbol first = eval(list.get(0));
		                                                                checkList(first);
		                                                                Symbol second = eval(list.get(1));
		                                                                checkList(second);
		                                                                return Symbol.append(first, second);
	                                                                };

	static {
		help.put("AND", "(AND <arg1> <arg2> ...) returns NIL when it finds the first argument that evaluates to NIL, from left to right. "
		        + "Thus, the expression (and nil (a b c d)) evaluates to NIL even if there is no function name \"a\" because the "
		        + "second argument is not evaluated. Accordingly the expression (AND) evaluates to TRUE" + unquotedArgumentsMessage);
	}
	private final Function<Symbol, Symbol> processAnd               = (symbol) -> {
		                                                                List<Symbol> list = symbol.getCdr().getSymbolList();
		                                                                for (Symbol s : list) {
			                                                                Symbol value = eval(s);
			                                                                if (value.isNil())
				                                                                return value;
		                                                                }
		                                                                return Symbol.TRUE;
	                                                                };

	private final Function<Symbol, Symbol> processAtom              = (symbol) -> {
		                                                                switch (symbol.getType()) {
																		case Token.NUMBER:
																		case Token.STRING:
																			return symbol;
																		case Token.FALSE:
																			return Symbol.NIL;
																		case Token.EXIT:
																		case Token.QUIT:
																			System.exit(0);
																		case Token.END:
																			break;
																		case Token.ID:
																			Symbol value = symbolTable.get(symbol.getCode());
																			if (value == null)
																				return Symbol.TRUE;
																			if (value.isAtom())
																				return value;
																			return eval(value);
																		}
																		return symbol;
																	};

	private final Function<Symbol, Symbol> processAtomp             = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 1);
		                                                                Symbol first = eval(list.get(0));
		                                                                return first.isAtom() ? Symbol.TRUE : Symbol.NIL;
	                                                                };

	private final Function<Symbol, Symbol> processBlock             = (symbol) -> {
		                                                                List<Symbol> list = symbol.getSymbolList();
		                                                                list.remove(0);
		                                                                Symbol value = Symbol.NIL;
		                                                                for (Symbol s : list) {
			                                                                value = eval(s);
		                                                                }
		                                                                return value;
	                                                                };

	private final Function<Symbol, Symbol> processCar               = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 1);
		                                                                Symbol first = eval(list.get(0));
		                                                                checkList(first);
		                                                                return first.getCar();
	                                                                };

	private final Function<Symbol, Symbol> processCdr               = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 1);
		                                                                Symbol first = eval(list.get(0));
		                                                                checkList(first);
		                                                                return first.getCdr();
	                                                                };

	private final Function<Symbol, Symbol> processClear             = (symbol) -> {
		                                                                symbolTable = new HashMap<String, Symbol>();
		                                                                functionTable = new HashMap<String, Lambda>();
		                                                                return Symbol.TRUE;
	                                                                };

	private final Function<Symbol, Symbol> processCond              = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 1);

		                                                                list = list.get(0).getSymbolList();

		                                                                for (Symbol s : list) {
			                                                                checkList(s);
			                                                                List<Symbol> condCase = s.getSymbolList();
			                                                                if (condCase.size() != 2) {
				                                                                throw new EvaluationError("Bad case in conditional: " + s);
			                                                                }
			                                                                if (eval(condCase.get(0)).isNil())
				                                                                continue;
			                                                                return eval(condCase.get(1));
		                                                                }
		                                                                return Symbol.NIL;
	                                                                };

	private final Function<Symbol, Symbol> processCompare           = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 2);
		                                                                Symbol first = eval(list.get(0));
		                                                                Symbol second = eval(list.get(1));
		                                                                int functionType = symbol.getCar().getType();

		                                                                if (first.getType() != second.getType()) {
			                                                                throw new EvaluationError(
			                                                                        "Comparators expect two numbers or two strings as arguments");
		                                                                }
		                                                                if (!(first.getType() == Token.NUMBER || first.getType() == Token.STRING)) {
			                                                                throw new EvaluationError(
			                                                                        "Comparators expect two numbers or two strings as arguments");
		                                                                }
		                                                                int comparatorValue;
		                                                                if (first.getType() == Token.NUMBER) {
			                                                                comparatorValue = new BigInteger(first.getCode()).compareTo(new BigInteger(second
			                                                                        .getCode()));
		                                                                } else {
			                                                                comparatorValue = first.getCode().compareTo(second.getCode());
		                                                                }
		                                                                switch (functionType) {
																		case Token.LT:
																			return comparatorValue < 0 ? Symbol.TRUE : Symbol.NIL;
																		case Token.GT:
																			return comparatorValue > 0 ? Symbol.TRUE : Symbol.NIL;
																		case Token.LE:
																			return comparatorValue <= 0 ? Symbol.TRUE : Symbol.NIL;
																		case Token.GE:
																			return comparatorValue >= 0 ? Symbol.TRUE : Symbol.NIL;
																		}
																		throw new EvaluationError("processCompare() error: " + symbol);
																	};

	private final Function<Symbol, Symbol> processCount             = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 1);
		                                                                Symbol first = eval(list.get(0));
		                                                                checkList(first);
		                                                                return new Symbol(new Token(Token.NUMBER, "" + first.getLength()));
	                                                                };

	private final Function<Symbol, Symbol> processDefun             = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 3);
		                                                                Symbol id = list.get(0);
		                                                                checkId(id);
		                                                                checkListOfIds(list.get(1));
		                                                                List<Symbol> varsList = list.get(1).getSymbolList();
		                                                                Symbol code = list.get(2);
		                                                                functionTable.put(id.getCode(), new Lambda(code, varsList));
		                                                                return id;
	                                                                };

	private final Function<Symbol, Symbol> processDiv               = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 2);
		                                                                BigInteger value;
		                                                                try {
			                                                                value = new BigInteger(eval(list.get(0)).getCode());
			                                                                value = value.divide(new BigInteger(eval(list.get(1)).getCode()));
			                                                                return new Symbol(new Token(Token.NUMBER, value.toString()));
		                                                                } catch (Exception e) {
			                                                                throw new EvaluationError("processDiv() error: " + symbol);
		                                                                }
	                                                                };

	private final Function<Symbol, Symbol> processDo                = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 2);
		                                                                Symbol first = list.get(0);
		                                                                checkList(first);
		                                                                List<Symbol> loopList = first.getSymbolList();
		                                                                Symbol whileCondition = list.get(1);
		                                                                Symbol doResult = Symbol.NIL;
		                                                                do {
			                                                                for (Symbol s : loopList) {
				                                                                doResult = eval(s);
			                                                                }
		                                                                } while (!eval(whileCondition).isNil());
		                                                                return doResult;
	                                                                };
	private final Function<Symbol, Symbol> processDot               = (symbol) -> {
		                                                                symbol = symbol.copy();
		                                                                List<Symbol> list = symbol.getSymbolList();
		                                                                if (list.size() == 1)
			                                                                throw new EvaluationError("dot expects at least one argument");
		                                                                list.remove(0);
		                                                                switch (list.size()) {
																		case 1:
																			symbol = list.get(0);
																			knowledgeBase.add(symbol);
																			return symbol;
																		default:
																			symbol = Symbol.NIL;
																			for (int i = list.size() - 1; i >= 0; --i) {
																				Symbol link = new Symbol();
																				link.setCdr(symbol);
																				link.setCar(list.get(i));
																				symbol = link;
																			}
																		}
																		ruleBase.add(symbol);
																		return symbol;
																	};

	private final Function<Symbol, Symbol> processEqual             = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 2);
		                                                                Symbol first = eval(list.get(0));
		                                                                Symbol second = eval(list.get(1));
		                                                                return first.equals(second) ? Symbol.TRUE : Symbol.NIL;
	                                                                };

	private final Function<Symbol, Symbol> processEval              = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 1);
		                                                                return eval(list.get(0));
	                                                                };

	private final Function<Symbol, Symbol> processFor               = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 2);
		                                                                Symbol first = list.get(0);
		                                                                if (!(first.isList() && first.getLength() == 3)) {
			                                                                throw new EvaluationError(
			                                                                        "for expects a list of three elements as its first argument");
		                                                                }
		                                                                Symbol second = list.get(1);

		                                                                checkList(second);

		                                                                List<Symbol> forArgs = first.getSymbolList();
		                                                                Symbol prepareFor = forArgs.get(0);
		                                                                Symbol whileCondition = forArgs.get(1);
		                                                                Symbol updateFor = forArgs.get(2);

		                                                                list = second.getSymbolList();
		                                                                eval(prepareFor);
		                                                                Symbol loopValue = Symbol.NIL;

		                                                                while (!eval(whileCondition).isNil()) {
			                                                                for (Symbol s : list) {
				                                                                loopValue = eval(s);
			                                                                }
			                                                                eval(updateFor);
		                                                                }
		                                                                return loopValue;
	                                                                };

	private final Function<Symbol, Symbol> processForList           = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 2);
		                                                                Symbol first = eval(list.get(0));
		                                                                checkList(first);
		                                                                Symbol second = list.get(1);
		                                                                List<Symbol> loopList = first.getSymbolList();
		                                                                Symbol loopValue = Symbol.NIL;
		                                                                list = null;
		                                                                for (Symbol s : loopList) {
			                                                                Symbol pair = Symbol.NIL;
			                                                                pair = Symbol.push(s, pair);
			                                                                pair = Symbol.push(second, pair);
			                                                                loopValue = Symbol.push(eval(pair), loopValue);
		                                                                }
		                                                                return Symbol.reverse(loopValue);
	                                                                };

	private final Function<Symbol, Symbol> processId                = (symbol) -> {
		                                                                Symbol function = symbol.getCar();
		                                                                Lambda lambda = functionTable.get(function.getCode());
		                                                                if (lambda == null) {
			                                                                throw new EvaluationError(function + " is not defined");
		                                                                }
		                                                                Symbol code = lambda.getCode();
		                                                                List<Symbol> variables = lambda.getVarsList();
		                                                                List<Symbol> values = symbol.getCdr().getSymbolList();
		                                                                for (int i = 0; i < values.size(); ++i) {
			                                                                Symbol value = (Symbol.push(values.get(i), Symbol.NIL));
			                                                                value = (Symbol.push(Symbol.EVAL, value));
			                                                                values.set(i, eval(value));
		                                                                }
		                                                                if (variables.size() != values.size()) {
			                                                                throw new EvaluationError(function + " requires " + variables.size() + " arguments");
		                                                                }
		                                                                if (code.isNil())
			                                                                return Symbol.NIL;
		                                                                return eval(Symbol.map(variables, values, code));
	                                                                };

	private final Function<Symbol, Symbol> processIf                = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 3);
		                                                                Symbol condition = list.get(0);
		                                                                Symbol ifTrue = list.get(1);
		                                                                Symbol ifFalse = list.get(2);
		                                                                if (eval(condition).isNil())
			                                                                return eval(ifFalse);
		                                                                return eval(ifTrue);
	                                                                };

	private final Function<Symbol, Symbol> processLambda            = (symbol) -> {
		                                                                Symbol idSymbol = new Symbol(Token.ID, getId());
		                                                                Symbol lambda = symbol.getCar().getCdr();
		                                                                Symbol args = symbol.getCdr();
		                                                                Symbol defun = Symbol.DEFUN;
		                                                                processDefun.apply(Symbol.push(defun, Symbol.push(idSymbol, lambda)));
		                                                                return eval(Symbol.push(idSymbol, args));
	                                                                };

	private final Function<Symbol, Symbol> processLet               = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 2);
		                                                                checkList(list.get(0));
		                                                                List<Symbol> assoc = list.get(0).getSymbolList();
		                                                                Symbol code = list.get(1);
		                                                                checkList(code);
		                                                                if (assoc.size() == 0)
			                                                                return eval(code);
		                                                                List<Symbol> variables = new ArrayList<Symbol>();
		                                                                List<Symbol> values = new ArrayList<Symbol>();
		                                                                for (Symbol s : assoc) {
			                                                                checkList(s);
			                                                                List<Symbol> pair = s.getSymbolList();
			                                                                if (pair.size() != 2 || !pair.get(0).isId()) {
				                                                                throw new EvaluationError(
				                                                                        "The first argument to let must be a list of pairs (id value)");
			                                                                }
			                                                                String id = getId();
			                                                                Symbol idSymbol = new Symbol(Token.ID, id);
			                                                                variables.add(pair.get(0));
			                                                                values.add(idSymbol);
			                                                                symbolTable.put(id, eval(pair.get(1)));
		                                                                }
		                                                                Symbol.map(variables, values, code);
		                                                                Symbol value = Symbol.NIL;
		                                                                List<Symbol> codes = code.getSymbolList();
		                                                                for (Symbol s : codes) {
			                                                                value = eval(s);
		                                                                }
		                                                                return value;
	                                                                };

	private final Function<Symbol, Symbol> processLoad              = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 1);
		                                                                if (list.get(0).getType() != Token.STRING) {
			                                                                throw new EvaluationError("Load expects the name of a file as an argument");
		                                                                }
		                                                                String filename = list.get(0).getCode();
		                                                                boolean traceValue = trace;
		                                                                try (InputStream fileInput = new FileInputStream(filename)) {
			                                                                Parser fileParser = new Parser(fileInput);
			                                                                trace = true;
			                                                                Symbol parsedSymbol;
			                                                                do {
				                                                                eval(parsedSymbol = fileParser.getExpression());
			                                                                } while (parsedSymbol.getType() != Token.END);
			                                                                return Symbol.TRUE;
		                                                                } catch (FileNotFoundException e) {
			                                                                throw new EvaluationError("Load expects the name of a file as an argument");
		                                                                } catch (IOException e) {
			                                                                throw new EvaluationError("I/O Error");
		                                                                } catch (NullPointerException npe) {
			                                                                return Symbol.TRUE;
		                                                                } finally {
			                                                                trace = traceValue;
		                                                                }
	                                                                };

	private final Function<Symbol, Symbol> processListp             = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 1);
		                                                                return list.get(0).isList() ? Symbol.TRUE : Symbol.NIL;
	                                                                };
	private final Function<Symbol, Symbol> processMemberp           = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 2);
		                                                                Symbol first = eval(list.get(0));
		                                                                Symbol second = eval(list.get(1));
		                                                                checkList(second);
		                                                                return Symbol.memberP(first, second);
	                                                                };

	private final Function<Symbol, Symbol> processMinus             = (symbol) -> {
		                                                                List<Symbol> list = symbol.getCdr().getSymbolList();
		                                                                if (!(list.size() == 1 || list.size() == 2)) {
			                                                                throw new EvaluationError("Minus expects one or two arguments");
		                                                                }
		                                                                Symbol first = null;
		                                                                Symbol second = null;
		                                                                BigInteger value = new BigInteger("0");
		                                                                try {
			                                                                switch (list.size()) {
																			case 1:
																				first = eval(list.get(0));
																				value = value.subtract(new BigInteger(first.getCode()));
																				break;
																			case 2:
																				first = eval(list.get(0));
																				value = new BigInteger(first.getCode());
																				second = eval(list.get(1));
																				value = value.subtract(new BigInteger(second.getCode()));
																			}
																			return new Symbol(new Token(Token.NUMBER, value.toString()));
																		} catch (Exception e1) {
																			throw new EvaluationError("processMinus(): " + symbol);
																		}
																	};

	private final Function<Symbol, Symbol> processModule            = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 2);
		                                                                Symbol first = eval(list.get(0));
		                                                                Symbol second = eval(list.get(1));
		                                                                BigInteger value;
		                                                                try {
			                                                                value = new BigInteger(first.getCode());
			                                                                value = value.mod(new BigInteger(second.getCode()));
			                                                                return new Symbol(new Token(Token.NUMBER, value.toString()));
		                                                                } catch (Exception e1) {
			                                                                throw new EvaluationError("processModule(): " + symbol);
		                                                                }
	                                                                };

	private final Function<Symbol, Symbol> processNot               = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 1);
		                                                                return eval(list.get(0)).isNil() ? Symbol.TRUE : Symbol.NIL;
	                                                                };

	private final Function<Symbol, Symbol> processNumberp           = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 1);
		                                                                Symbol first = eval(list.get(0));
		                                                                return first.isNumber() ? Symbol.TRUE : Symbol.NIL;
	                                                                };

	private final Function<Symbol, Symbol> processOr                = (symbol) -> {
		                                                                List<Symbol> list = symbol.getCdr().getSymbolList();
		                                                                for (Symbol s : list) {
			                                                                Symbol value = eval(s);
			                                                                if (!value.isNil())
				                                                                return Symbol.TRUE;
		                                                                }
		                                                                return Symbol.NIL;
	                                                                };

	private final Function<Symbol, Symbol> processPlus              = (symbol) -> {
		                                                                List<Symbol> list = symbol.getCdr().getSymbolList();
		                                                                try {
			                                                                BigInteger value = new BigInteger("0");
			                                                                for (Symbol s : list) {
				                                                                value = value.add(new BigInteger(eval(s).getCode()));
			                                                                }
			                                                                return new Symbol(new Token(Token.NUMBER, value.toString()));
		                                                                } catch (Exception e1) {
			                                                                throw new EvaluationError("Invalid expression " + symbol);
		                                                                }
	                                                                };

	private final Function<Symbol, Symbol> processPush              = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 2);
		                                                                Symbol first = eval(list.get(0));
		                                                                Symbol second = eval(list.get(1));
		                                                                checkList(second);
		                                                                return Symbol.push(first, second);
	                                                                };

	private final Function<Symbol, Symbol> processQuote             = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 1);
		                                                                return list.get(0);
	                                                                };

	private final Function<Symbol, Symbol> processRead              = (symbol) -> {
		                                                                List<Symbol> list = symbol.getCdr().getSymbolList();
		                                                                switch (list.size()) {
																		case 1:
																			System.out.println(eval(list.get(0)));
																		case 0:
																			return eval(parser.getExpression());
																		}
																		throw new EvaluationError("processRead(): " + symbol);
																	};

	private final Function<Symbol, Symbol> processReverse           = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 1);
		                                                                Symbol first = eval(list.get(0));
		                                                                checkList(first);
		                                                                return Symbol.reverse(first);
	                                                                };

	private final Function<Symbol, Symbol> processSetq              = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 2);
		                                                                Symbol id = list.get(0);
		                                                                checkId(id);
		                                                                Symbol second = eval(list.get(1));
		                                                                symbolTable.put(id.getCode(), second);
		                                                                return second;
	                                                                };

	private final Function<Symbol, Symbol> processSet               = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 2);
		                                                                Symbol id = eval(list.get(0));
		                                                                checkId(id);
		                                                                Symbol second = eval(list.get(1));
		                                                                symbolTable.put(id.getCode(), second);
		                                                                return second;
	                                                                };

	private final Function<Symbol, Symbol> processStringp           = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 1);
		                                                                Symbol first = eval(list.get(0));
		                                                                return first.getType() == Token.STRING ? Symbol.TRUE : Symbol.NIL;
	                                                                };

	private final Function<Symbol, Symbol> processTrace             = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 1);
		                                                                if (eval(list.get(0)).isNil())
			                                                                trace = false;
		                                                                else
			                                                                trace = true;
		                                                                return Symbol.TRUE;
	                                                                };

	private final Function<Symbol, Symbol> processTimes             = (symbol) -> {
		                                                                List<Symbol> list = symbol.getCdr().getSymbolList();

		                                                                try {
			                                                                BigInteger value = new BigInteger("1");
			                                                                for (Symbol s : list) {
				                                                                value = value.multiply(new BigInteger(eval(s).getCode()));
			                                                                }
			                                                                return new Symbol(new Token(Token.NUMBER, value.toString()));
		                                                                } catch (Exception e1) {
			                                                                throw new EvaluationError("processTimes(): " + symbol);
		                                                                }
	                                                                };
	private final Function<Symbol, Symbol> processUnset             = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 1);
		                                                                Symbol toUnset = eval(list.get(0));
		                                                                if (!toUnset.isId()) {
			                                                                throw new EvaluationError(toUnset + " is not a valid id");
		                                                                }
		                                                                symbolTable.remove(toUnset.getCode());
		                                                                return Symbol.TRUE;
	                                                                };

	private final Function<Symbol, Symbol> processWhile             = (symbol) -> {
		                                                                List<Symbol> list = getArgs(symbol, 2);
		                                                                Symbol loopCondition = list.get(0);
		                                                                checkList(list.get(1));
		                                                                Symbol loopBody = list.get(1);
		                                                                Symbol third = Symbol.NIL;
		                                                                while (!eval(loopCondition).isNil()) {
			                                                                for (Symbol s : loopBody.getSymbolList()) {
				                                                                third = eval(s);
			                                                                }
		                                                                }
		                                                                return third;
	                                                                };

	private final Function<Symbol, Symbol> processWrite             = (symbol) -> {
		                                                                symbol.getCdr().getSymbolList().stream()
		                                                                        .forEach((s) -> System.out.print(eval(s) + " "));
		                                                                System.out.println();
		                                                                return Symbol.TRUE;
	                                                                };
	private final Function<Symbol, Symbol> processList              = (symbol) -> {
		                                                                Symbol result = Symbol.NIL;
		                                                                for (Symbol s : symbol.getSymbolList()) {
			                                                                result = Symbol.push(eval(s), result);
		                                                                }
		                                                                return Symbol.reverse(result);
	                                                                };

	{
		predefined.put(Token.APPEND, processAppend);
		predefined.put(Token.AND, processAnd);
		predefined.put(Token.ATOMP, processAtomp);
		predefined.put(Token.BLOCK, processBlock);
		predefined.put(Token.CADDR, processCdr);
		predefined.put(Token.CADR, processCdr);
		predefined.put(Token.CAR, processCar);
		predefined.put(Token.CDR, processCdr);
		predefined.put(Token.CLEAR, processClear);
		predefined.put(Token.COND, processCond);
		predefined.put(Token.COUNT, processCount);
		predefined.put(Token.DEFUN, processDefun);
		predefined.put(Token.DIV, processDiv);
		predefined.put(Token.DO, processDo);
		predefined.put(Token.DOT, processDot);
		predefined.put(Token.EQUAL, processEqual);
		predefined.put(Token.EVAL, processEval);
		predefined.put(Token.FOR, processFor);
		predefined.put(Token.FOR_LIST, processForList);
		predefined.put(Token.GE, processCompare);
		predefined.put(Token.GT, processCompare);
		predefined.put(Token.ID, processId);
		predefined.put(Token.IF, processIf);
		predefined.put(Token.LAMBDA, processLambda);
		predefined.put(Token.LE, processCompare);
		predefined.put(Token.LET, processLet);
		predefined.put(Token.LISTP, processListp);
		predefined.put(Token.LOAD, processLoad);
		predefined.put(Token.LT, processCompare);
		predefined.put(Token.MEMBERP, processMemberp);
		predefined.put(Token.MINUS, processMinus);
		predefined.put(Token.MOD, processModule);
		predefined.put(Token.NOT, processNot);
		predefined.put(Token.NUMBER, processList);
		predefined.put(Token.NUMBERP, processNumberp);
		predefined.put(Token.OR, processOr);
		predefined.put(Token.PLUS, processPlus);
		predefined.put(Token.PUSH, processPush);
		predefined.put(Token.QUOTE, processQuote);
		predefined.put(Token.READ, processRead);
		predefined.put(Token.REVERSE, processReverse);
		predefined.put(Token.SET, processSet);
		predefined.put(Token.SETQ, processSetq);
		predefined.put(Token.STRING, processList);
		predefined.put(Token.STRINGP, processStringp);
		predefined.put(Token.TIMES, processTimes);
		predefined.put(Token.TRACE, processTrace);
		predefined.put(Token.UNSET, processUnset);
		predefined.put(Token.WHILE, processWhile);
		predefined.put(Token.WRITE, processWrite);
	}

	private void cleanUp()
	{
		for (int i = 0; symbolTable.get("@" + i) != null; ++i) {
			symbolTable.remove("@" + i);
		}
		id = 0;
	}

	public void evalNext()
	{
		try {
			System.out.print(prompt);
			Symbol output = eval(getParser().getExpression());
			if (output.getType() == Token.QUOTE)
				System.out.print(output.getCdr().getCar());
			else if (output.getType() == Token.STRING)
				System.out.println("\"" + output + "\"");
			else
				System.out.println(output);
		} catch (Exception e) {
			System.out.println(e.toString());
		} finally {
			cleanUp();
		}
	}
}