package com.ochavoya.languages.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.ochavoya.languages.model.Token;

public class Scanner
{
	private char[]         buffer;
	private int            bufferPointer = 0;
	private boolean        nextCharFlag  = false;
	private Character      storedNextChar;
	private BufferedReader br;

	/**
	 * 
	 * @param in
	 *            Use System.in for console
	 */
	public Scanner(InputStream in)
	{
		br = new BufferedReader(new InputStreamReader(in));
	}

	private final Character nextChar()
	{
		if (nextCharFlag) {
			nextCharFlag = false;
			return storedNextChar;
		}
		while (buffer == null || bufferPointer == buffer.length) {
			try {
				String line = br.readLine();
				if (line == null)
					return null;
				buffer = (line + "\n").toCharArray();
				bufferPointer = 0;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return buffer[bufferPointer++];
	}

	private void ungetChar(char c)
	{
		nextCharFlag = true;
		storedNextChar = c;
	}

	private void skipWhiteSpace()
	{
		Character c;
		do {
			c = nextChar();
		} while (Character.isWhitespace(c) || c == '@');
		ungetChar(c);
	}

	private boolean isValidIdChar(char c)
	{
		switch (c) {
		case '-':
		case '_':
		case ':':
		case '*':
			return true;
		default:
			if (Character.isAlphabetic(c) || Character.isDigit(c)) {
				return true;
			}
			return false;
		}
	}

	private final Token lookAhead(int shortCode, int longCode, Character toCheck, StringBuilder buffer)
	{
		Character lookAhead = nextChar();
		if (toCheck.equals(lookAhead)) {
			buffer.append(lookAhead);
			return new Token(longCode, buffer.toString());
		}
		ungetChar(lookAhead);
		return new Token(shortCode, buffer.toString());
	}

	@SuppressWarnings("null")
	public final Token getToken()
	{
		StringBuilder buffer = new StringBuilder();
		int state = 0;
		skipWhiteSpace();
		while (true) {
			Character c = nextChar();
			switch (state) {
			case 0:
				if (c == null) {
					try {
						br.close();
					} catch (IOException e) {
						System.out.println("IOException while closeing the input stream");
					}
					return new Token(Token.END, buffer.toString());
				}
				switch (c) {
				case '(':
					skipWhiteSpace();
					buffer.append(c);
					return lookAhead(Token.LEFT, Token.NIL, ')', buffer);
				case ')':
					return new Token(Token.RIGHT, ")");
				case '=':
					return new Token(Token.EQUAL, "=");
				case '+':
					return new Token(Token.PLUS, "+");
				case '-':
					buffer.append(c);
					state = 6;
					break;
				case '*':
					return new Token(Token.TIMES, "*");
				case '/':
					return new Token(Token.DIV, "/");
				case '%':
					return new Token(Token.MOD, "%");
				case '\'':
					return new Token(Token.QUOTE, "'");
				case '.':
					return new Token(Token.DOT, ".");
				case '<':
					buffer.append(c);
					return lookAhead(Token.LT, Token.LE, '=', buffer);
				case '>':
					buffer.append(c);
					return lookAhead(Token.GT, Token.GE, '=', buffer);
				case '"':
					state = 3;
					break;
				case ';':
					state = 4;
					break;
				default:
					buffer.append(c);
					if (Character.isDigit(c)) {
						state = 2;
					} else if (isValidIdChar(c)) {
						state = 1;
					} else
						state = 5;
				}
				break;
			case 6:
				if (c == null || Character.isWhitespace(c)) {
					ungetChar(c);
					return new Token(Token.MINUS, "-");
				}
				if (Character.isDigit(c)) {
					buffer.append(c);
					state = 2;
					break;
				}
				buffer.append(c);
				state = 5;
				break;
			case 1:
				if (c == null || !isValidIdChar(c)) {
					ungetChar(c);
					return new Token(Token.ID, buffer.toString());
				}
				buffer.append(c);
				break;
			case 2:
				if (c == null || !Character.isDigit(c)) {
					ungetChar(c);
					return new Token(Token.NUMBER, buffer.toString());
				}
				buffer.append(c);
				break;
			case 3:
				if (c == null || c == '\n') {
					ungetChar(c);
					state = 5;
					break;
				}
				if (c == '"') {
					return new Token(Token.STRING, buffer.toString());
				}
				buffer.append(c);
				break;
			case 4:
				if (c == null) {
					ungetChar(c);
					state = 5;
					break;
				}
				if (c == ';') {
					do {
						c = nextChar();
					} while (!(c == null || c == '\n'));
					ungetChar(c);
					state = 0;
					skipWhiteSpace();
					break;
				}
				buffer.append(";" + c);
				state = 5;
				break;
			case 5:
				if (c == null || Character.isWhitespace(c)) {
					ungetChar(c);
					return new Token(Token.INVALID, buffer.toString());
				}
				buffer.append(c);
				break;
			}
		}
	}
}