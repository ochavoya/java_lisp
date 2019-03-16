package com.ochavoya.languages.model;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

public class Symbol implements Serializable
{
	/**
	 * Author: Oscar Chavoya Aceves (c) 2017
	 */
	private static final long   serialVersionUID = 201706110509L;

	private static final Logger log              = Logger.getLogger(Symbol.class);
	private String              code             = null;
	private int                 type             = Token.ISLIST;
	private Symbol              car              = null;
	private Symbol              cdr              = null;

	public Symbol()
	{
	}

	public Symbol(int type, String code)
	{
		this.type = type;
		this.code = code;
	}

	public Symbol(Token token)
	{
		super();
		this.code = token.getCode();
		this.type = token.getType();
	}

	public static final Symbol TRUE   = new Symbol(new Token(Token.TRUE));
	public static final Symbol NIL    = new Symbol(new Token(Token.NIL));
	public static final Symbol QUOTE  = new Symbol(new Token(Token.QUOTE));
	public static final Symbol EVAL   = new Symbol(new Token(Token.EVAL));
	public static final Symbol LAMBDA = new Symbol(new Token(Token.LAMBDA));
	public static final Symbol DEFUN  = new Symbol(new Token(Token.DEFUN));

	public String getCode()
	{
		return code;
	}

	public int getType()
	{
		return type;
	}

	public Symbol getCar()
	{
		if (isNil())
			return this;
		return car;
	}

	public void setCar(Symbol car)
	{
		this.car = car;
	}

	public Symbol getCdr()
	{
		if (isNil())
			return this;
		return cdr;
	}

	public void setCdr(Symbol cdr)
	{
		this.cdr = cdr;
	}

	public boolean isNil()
	{
		return type == Token.NIL;
	}

	public boolean isAtom()
	{
		return type != Token.ISLIST;
	}

	public boolean isId()
	{
		return type == Token.ID;
	}

	public boolean isString()
	{
		return type == Token.STRING;
	}

	public boolean isList()
	{
		return isNil() || type == Token.ISLIST;
	}

	public boolean isListOfIds()
	{
		if (!isList())
			return false;
		Symbol pointer = this;
		while (!pointer.isNil()) {
			if (!pointer.car.isId())
				return false;
			pointer = pointer.cdr;
		}
		return true;
	}

	private Symbol substitute(Map<Symbol, Symbol> map)
	{
		Symbol pointer = this;
		if (pointer.isNil())
			return pointer;
		if (pointer.car == null && pointer.cdr == null && pointer.isId()) {
			Symbol value = map.get(pointer);
			if (log.isDebugEnabled()) {
				log.debug("substitute () - value: " + value);
			}
			if (value == null)
				return pointer;
			return value.copy();
		}
		if (!(pointer.car == null || pointer.car.isNil()))
			pointer.car = pointer.car.substitute(map);
		if (!(pointer.cdr == null || pointer.cdr.isNil()))
			pointer.cdr = pointer.cdr.substitute(map);
		return pointer;
	}

	public static Symbol map(List<Symbol> variables, List<Symbol> values, Symbol code)
	{
		if (variables.size() == 0)
			return code;
		Map<Symbol, Symbol> map = new HashMap<Symbol, Symbol>();

		for (int i = 0, size = variables.size(); i < size; ++i)
			map.put(variables.get(i), values.get(i));

		return code.substitute(map);
	}

	public Symbol copy()
	{
		switch (type) {
		case Token.ISLIST:
		case Token.ID:
		case Token.NUMBER:
		case Token.STRING:
			Symbol symbol = new Symbol();
			symbol.type = type;
			symbol.code = code;
			if (car != null)
				symbol.car = car.copy();
			if (cdr != null)
				symbol.cdr = cdr.copy();
			return symbol;
		default:
			return this;
		}
	}

	public static final Symbol push(Symbol car, Symbol cdr)
	{
		Symbol node = new Symbol();
		node.type = Token.ISLIST;
		node.setCar(car);
		node.setCdr(cdr);
		return node;
	}

	public static Symbol reverse(Symbol symbol)
	{
		Symbol reversed = NIL;
		while (!symbol.isNil()) {
			reversed = push(symbol.car, reversed);
			symbol = symbol.getCdr();
		}
		return reversed;
	}

	public static final Symbol memberP(Symbol symbol, Symbol list)
	{
		while (!list.isNil()) {
			if (symbol.equals(list.getCar()))
				return TRUE;
			list = list.getCdr();
		}
		return NIL;
	}

	public int getLength()
	{
		int length = 0;
		Symbol symbol = this;
		while (!symbol.isNil()) {
			++length;
			symbol = symbol.getCdr();
		}
		return length;
	}

	public List<Symbol> getSymbolList()
	{
		if (!this.isList())
			return null;
		List<Symbol> list = new ArrayList<Symbol>();
		Symbol symbol = this;
		while (!symbol.isNil()) {
			list.add(symbol.getCar());
			symbol = symbol.getCdr();
		}
		return list;
	}

	public static final Symbol number(BigInteger number)
	{
		Symbol symbol = new Symbol();
		symbol.type = Token.NUMBER;
		symbol.code = number.toString();
		return symbol;
	}

	public final boolean isNumber()
	{
		return this.type == Token.NUMBER;
	}

	public static final Symbol append(Symbol left, Symbol right)
	{
		left = reverse(left);
		while (!left.isNil()) {
			right = push(left.getCar(), right);
			left = left.getCdr();
		}
		return right;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((car == null) ? 0 : car.hashCode());
		result = prime * result + ((cdr == null) ? 0 : cdr.hashCode());
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + type;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Symbol other = (Symbol) obj;
		if (car == null) {
			if (other.car != null)
				return false;
		} else if (!car.equals(other.car))
			return false;
		if (cdr == null) {
			if (other.cdr != null)
				return false;
		} else if (!cdr.equals(other.cdr))
			return false;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (type != other.type)
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		if (isAtom())
			return code;
		StringBuilder builder = new StringBuilder();
		builder.append("(");
		List<Symbol> list = getSymbolList();
		if (list.size() > 1) {
			builder.append(list.get(0).toString());
		}
		list.remove(0);
		for (Symbol s : list) {
			if (Token.QUOTE == s.getType()) {
				builder.append("'");
			} else
				builder.append(" ");
			builder.append(s.toString());
		}
		builder.append(")");
		return builder.toString();
	}
}
