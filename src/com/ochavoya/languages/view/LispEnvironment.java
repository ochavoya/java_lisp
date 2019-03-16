package com.ochavoya.languages.view;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.ochavoya.languages.controller.Interpreter;

public class LispEnvironment
{

	public static void main(String[] args)
	{

		String presentation = null;
		String credits = null;
		String message = null;

		try (InputStream input = new FileInputStream(new File("application.properties"))) {

			Properties prop = new Properties();
			prop.load(input);

			presentation = prop.getProperty("presentation");
			credits = prop.getProperty("credits");
			message = prop.getProperty("message");

		} catch (IOException ex) {
			ex.printStackTrace();
		}
		System.out.println(String.format("%s\n%s\n%s\n\n", presentation, credits, message));
		Interpreter interpreter = new Interpreter(System.in);
		{
			while (true) {
				interpreter.evalNext();
			}
		}
	}
}