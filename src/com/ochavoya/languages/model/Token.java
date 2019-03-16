package com.ochavoya.languages.model;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Token
{
	public static final int                   AND        = 1;
	public static final int                   APPEND     = 2;
	public static final int                   ATOMP      = 3;
	public static final int                   BLOCK      = 4;
	public static final int                   CADDR      = 5;
	public static final int                   CADR       = 6;
	public static final int                   CAR        = 7;
	public static final int                   CDR        = 8;
	public static final int                   CLEAR      = 9;
	public static final int                   COND       = 10;
	public static final int                   CONS       = 11;
	public static final int                   COUNT      = 12;
	public static final int                   DEFUN      = 13;
	public static final int                   DISCARD    = 14;
	public static final int                   DIV        = 15;
	public static final int                   DO         = 16;
	public static final int                   DOT        = 17;
	public static final int                   END        = 18;
	public static final int                   EQUAL      = 19;
	public static final int                   ERROR      = 20;
	public static final int                   EVAL       = 21;
	public static final int                   EXIT       = 22;
	public static final int                   FALSE      = 23;
	public static final int                   FOR        = 24;
	public static final int                   FOR_LIST   = 25;
	public static final int                   GE         = 26;
	public static final int                   GT         = 27;
	public static final int                   ID         = 28;
	public static final int                   IF         = 29;
	public static final int                   INVALID    = 30;
	public static final int                   ISLIST     = 31;
	public static final int                   LAMBDA     = 32;
	public static final int                   LE         = 33;
	public static final int                   LEFT       = 34;
	public static final int                   LET        = 35;
	public static final int                   LIST       = 36;
	public static final int                   LISTP      = 37;
	public static final int                   LOAD       = 38;
	public static final int                   LT         = 39;
	public static final int                   MINUS      = 40;
	public static final int                   MEMBERP    = 41;
	public static final int                   MOD        = 42;
	public static final int                   NAND       = 43;
	public static final int                   NIL        = 44;
	public static final int                   NOR        = 45;
	public static final int                   NOT        = 46;
	public static final int                   NUMBER     = 47;
	public static final int                   NUMBERP    = 48;
	public static final int                   OR         = 49;
	public static final int                   PLUS       = 50;
	public static final int                   PUSH       = 51;
	public static final int                   QUIT       = 52;
	public static final int                   QUOTE      = 53;
	public static final int                   READ       = 54;
	public static final int                   REVERSE    = 55;
	public static final int                   RIGHT      = 56;
	public static final int                   SAVE       = 57;
	public static final int                   SET        = 58;
	public static final int                   SETQ       = 59;
	public static final int                   STRING     = 60;
	public static final int                   STRINGP    = 61;
	public static final int                   TIMES      = 62;
	public static final int                   TRACE      = 63;
	public static final int                   TRUE       = 64;
	public static final int                   UNSET      = 65;
	public static final int                   WHILE      = 66;
	public static final int                   WRITE      = 67;

	private static final Map<String, Integer> tokenIndex = new HashMap<String, Integer>();
	private static final Map<Integer, String> tokenCode  = new HashMap<Integer, String>();

	static {
		Class<Token> clazz = Token.class;
		Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			String name = field.getName();
			if (name.startsWith("token") || "type".equals(name) || "code".equals(name))
				continue;
			try {
				int value = (int) field.get(clazz);
				tokenIndex.put(name, value);
				tokenCode.put(value, name);
			} catch (IllegalArgumentException | IllegalAccessException e) {

			}
		}
	}

	// ******************************************************

	private int                               type       = ISLIST;
	private String                            code;

	public int getType()
	{
		return type;
	}

	public void setType(int type)
	{
		this.type = type;
	}

	public String getCode()
	{
		return code;
	}

	public void setCode(String code)
	{
		this.code = code;
	}

	public Token(int tokenType)
	{
		String code = tokenCode.get(tokenType);
		if (code == null) {
			throw new RuntimeException("Invalid token code");
		}
		this.type = tokenType;
		this.code = code;
	}

	public Token(int type, String code)
	{
		switch (type) {
		case ID:
			Integer registered = tokenIndex.get(code.toUpperCase());
			if (registered != null) {
				this.type = registered;
				this.code = code.toUpperCase();
				return;
			}
		default:
			this.type = type;
			this.code = code;
		}
	}

	@Override
	public String toString()
	{
		return "type: " + type + ", code: \"" + code + "\"";
	}
}
