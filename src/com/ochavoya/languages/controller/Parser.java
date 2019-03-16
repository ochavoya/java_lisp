package com.ochavoya.languages.controller;

import java.io.InputStream;
import java.util.Stack;

import com.ochavoya.languages.model.Symbol;
import com.ochavoya.languages.model.Token;

public class Parser
{
	private Scanner scanner;

	public Parser(InputStream in)
	{
		super();
		this.scanner = new Scanner(in);
	}

	Stack<Token> stack = new Stack<Token>();

	public Symbol getExpression()
	{
		Token token = scanner.getToken();
		Symbol result = Symbol.NIL;
		switch (token.getType()) {
		case Token.NIL:
			return Symbol.NIL;
		case Token.TRUE:
			return Symbol.TRUE;
		case Token.QUOTE: {
			Symbol quote = Symbol.NIL;
			quote = Symbol.push(getExpression(), quote);
			quote = Symbol.push(Symbol.QUOTE, quote);
			return quote;
		}
		case Token.LEFT:
			stack.push(token);
			while (true) {
				Symbol value = getExpression();
				if (value == null)
					break;
				result = Symbol.push(value, result);
			}
			break;
		case Token.RIGHT:
			if (stack.isEmpty()) {
				System.out.println("PARSER ERROR: unmatched right parenthesis");
				return new Symbol(new Token(Token.INVALID));
			} else {
				stack.pop();
				return null;
			}
		default:
			return new Symbol(token);
		}
		return Symbol.reverse(result);
	}

	public static void main(String[] args)
	{
		Symbol.NIL.toString();
		Parser parser = new Parser(System.in);
		while (true) {
			System.out.println(parser.getExpression());
		}
	}
}
