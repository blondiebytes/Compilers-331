package lex;

import errors.LexicalError;
import static lex.TokenType.*;

import org.junit.After;
import org.junit.Test;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.Assert.*;

public class TokenizerTest
{
	// TODO Change this to reflect the maximum token size you have decided on.
	static final int MAX_IDENTIFIER_SIZE = 64;
	File temp;
	Tokenizer tokenizer;

	@After
	public void cleanup() {
		if (temp != null) {
			if (!temp.delete()) {
				System.err.println("Unable to delete temp file " + temp.getPath());
				System.err.println("It should be delete when the JVM exits.");
				temp.deleteOnExit();
			}
		}
		if (tokenizer != null) {
			tokenizer = null;
		}
	}

	@Test
	public void testTempFile() throws IOException, LexicalError
	{
		System.out.println("TokenizerTest.testTempFile");
		createTokenizerFor("+35.\n");
		expect(UNARYPLUS);
		expect(INTCONSTANT);
		expect(ENDMARKER);
		expect(ENDOFFILE);
	}

	@Test
	public void testLineCounter() throws IOException, LexicalError
	{
		System.out.println("TokenizerTest.testLineCounter");
		createTokenizerFor("program\n\nbegin\n\n\nend\n");
		expect(1, PROGRAM);
		expect(3, BEGIN);
		expect(6, END);
	}

	@Test
	public void testLineCounter2() throws IOException, LexicalError
	{
		System.out.println("TokenizerTest.testLineCounter2");
		createTokenizerFor("a b c\n\nd\n");
		expect(1, IDENTIFIER);
		expect(1, IDENTIFIER);
		expect(1, IDENTIFIER);
		expect(3, IDENTIFIER);
		expect(ENDOFFILE);
	}

	@Test(expected = LexicalError.class)
	public void testUnterminatedComment() throws IOException, LexicalError
	{
		System.out.println("TokenizerTest.testUnterminatedComment");
		createTokenizerFor(" program { unterminated comment ");
		tokenizer.getNextToken();
	}

	@Test(expected = LexicalError.class)
	public void testOnlyUnterminatedComment() throws IOException, LexicalError
	{
		System.out.println("TokenizerTest.testUnterminatedComment");
		createTokenizerFor(" { unterminated comment ");
		tokenizer.getNextToken();
	}

	@Test
	public void testMultipleComments() throws IOException, LexicalError
	{
		System.out.println("TokenizerTest.testMultipleComments");
		createTokenizerFor(" program\n{ comment 1 }\nbegin\n{ comment #2 }\n end\n");
		expect(PROGRAM);
		expect(BEGIN);
		expect(END);

	}

	@Test(expected = LexicalError.class)
	public void testBadComment() throws IOException, LexicalError
	{
		System.out.println("TokenizerTest.testBadComment");
		createTokenizerFor(" { { bad comment } }");
		tokenizer.getNextToken();
	}

	@Test(expected = LexicalError.class)
	public void testBadComment2() throws IOException, LexicalError
	{
		System.out.println("TokenizerTest.testBadComment");
		createTokenizerFor(" { { ");
		tokenizer.getNextToken();
	}


	@Test
	public void testIllegalCharacter() throws IOException, LexicalError
	{
		System.out.println("TokenizerTest.testIllegalCharacter");
		String chars = "$@#&`";
		int expected = chars.length();
		int actual = 0;
		for (char c : chars.toCharArray())
		{
			createTokenizerFor(String.valueOf(c));
			try
			{
				tokenizer.getNextToken();
				System.out.println("No LexicalError for: " + c);
			}
			catch (LexicalError lexicalError)
			{
				++actual;
			}
			tokenizer = null;
		}
		assertEquals("Some illegal characters were accepted.", expected, actual);
	}

	@Test(expected = LexicalError.class)
	public void testLongIdentifier() throws IOException, LexicalError
	{
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < MAX_IDENTIFIER_SIZE + 1; ++i) {
			builder.append('a');
		}
		createTokenizerFor(builder.toString());
		tokenizer.getNextToken();
	}

	@Test
	public void testMathOps() throws IOException, LexicalError
	{
		System.out.println("TokenizerTest.testMathOps");
		createTokenizerFor("+-*/");

		expect(UNARYPLUS);
		expect(UNARYMINUS);
		expect(MULOP);
		expect(MULOP);
	}

	@Test
	public void testNumbers() throws IOException, LexicalError
	{
		System.out.println("TokenizerTest.testNumbers");
		createTokenizerFor("1 2\n3 3.14 6e-7 -1 07");
		expect(INTCONSTANT);
		expect(INTCONSTANT);
		expect(INTCONSTANT);
		expect(REALCONSTANT);
		expect(REALCONSTANT);
		expect(ADDOP);
		expect(INTCONSTANT);
		expect(INTCONSTANT);
		expect(ENDOFFILE);
	}

	@Test
	public void theBigTest() throws IOException, LexicalError
	{
		int count = 0;
		URL url = getUrl();
		assertNotNull("Could not load test file.", url);
		Tokenizer tokenizer = new Tokenizer(url);
		try
		{
			Token token = tokenizer.getNextToken();
			assertNotNull("Tokenizer did not return a token.", token);
			while (token.getType() != ENDOFFILE)
			{
				++count;
				String message = String.format("%d : %s", tokenizer.getLineNumber(), token.toString());
				System.out.println(message);
				token = tokenizer.getNextToken();
				if (count > 100) {
					throw new IOException("We should have encounted ENDOFFILE by now...");
				}
			}
		}
		catch (LexicalError e)
		{
			fail(e.getMessage());
		}
	}

	@Test
	public void numTest() throws LexicalError, IOException {
		System.out.println("TokenizerTest.numTest");
		createTokenizerFor("6e- 6.5E+");
		expect(INTCONSTANT);
		expect(IDENTIFIER);
		expect(ADDOP);
		expect(REALCONSTANT);
		expect(IDENTIFIER);
		expect(ADDOP);
		expect(ENDOFFILE);
	}

	private void expect(int line, TokenType type) throws LexicalError
	{
		assertEquals("Wrong line number.", line, tokenizer.getLineNumber());
		expect(type);
	}

	private void expect(TokenType expected) throws LexicalError
	{
		assertNotNull(expected);
		Token actual = tokenizer.getNextToken();
		assertNotNull("Tokenizer did not return a token.", actual);
		assertEquals("Wrong token type", expected, actual.getType());
	}

	private void createTokenizerFor(String contents) throws IOException, LexicalError
	{
		temp = makeTempFile(contents);
		tokenizer = new Tokenizer(temp);
	}

	private File makeTempFile(String contents) throws IOException
	{
		Path temp = Files.createTempFile("test", ".dat");
		BufferedWriter writer = Files.newBufferedWriter(temp);
		writer.write(contents);
		writer.close();
		return temp.toFile();
	}

	private URL getUrl()
	{
		return getUrl("/lextest.dat");
	}

	private URL getUrl(String url)
	{
		return TokenizerTest.class.getResource(url);
	}
}
