package parser;

import errors.CompilerError;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Ignore
public class ErrorTest
{
	private File file;

	@After
	public void cleanup() {
		if (file != null) {
			if (!file.delete()) {
				file.deleteOnExit();
			}
			file = null;
		}
	}

	@Test
	public void unexpectedEnd() throws IOException, CompilerError
	{
		String code = "Program theProgram(input,output);\n" +
				  "var\n" +
				  "i : integer;" +
				  "end\n" +
				  "i := 1\n" +
				  "begin\n";

		createTempFile(code);
		Parser parser = new Parser(file);
		parser.parse();
		assertTrue(parser.error());
		for (CompilerError error : parser.getErrorList()) {
			System.out.println(error);
		}
	}

	@Test
	public void missingSemiColon() throws IOException, CompilerError
	{
		String code = "Program theProgram(input,output);\n" +
				  "var\n" +
				  "i,j : integer;" +
				  "begin\n" +
				  "i := 1\n" +
				  "j := 2\n" +
				  "end\n";

		createTempFile(code);
		Parser parser = new Parser(file);
		parser.parse();
		assertTrue(parser.error());
		for (CompilerError error : parser.getErrorList()) {
			System.out.println(error);
		}
	}

	private void createTempFile(String[] lines) throws IOException
	{
		file = File.createTempFile("parse", ".pas");
		try(PrintWriter writer = new PrintWriter(new FileWriter(file))) {
			Arrays.stream(lines).forEach(writer::println);
		}
	}

	private void createTempFile(String contents) throws IOException
	{
		file = File.createTempFile("parse", ".pas");
		FileWriter writer = new FileWriter(file);
		writer.write(contents);
		writer.close();

	}


}
