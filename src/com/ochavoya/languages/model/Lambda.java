package com.ochavoya.languages.model;

import java.util.List;

public class Lambda
{
	Symbol       code;
	List<Symbol> varsList;

	public Lambda()
	{

	}

	@Override
	public String toString()
	{
		return "Lambda [code=" + code + ", varsList=" + varsList + "]";
	}

	public Lambda(Symbol code, List<Symbol> varsList)
	{
		this.code = code;
		this.varsList = varsList;
	}

	public Symbol getCode()
	{
		return code.copy();
	}

	public void setCode(Symbol code)
	{
		this.code = code;
	}

	public List<Symbol> getVarsList()
	{
		return varsList;
	}

	public void setVarsList(List<Symbol> varsList)
	{
		this.varsList = varsList;
	}

}
