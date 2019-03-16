package com.ochavoya.languages.controller;


@SuppressWarnings("serial")
public class EvaluationError extends RuntimeException
{
	String error;

	public EvaluationError(String errorMessage)
	{
		this.error = errorMessage;
	}

	public String getError()
	{
		return error;
	}

	public void setError(String error)
	{
		this.error = error;
	}

	@Override
	public String toString()
	{
		return error;
	}

}
