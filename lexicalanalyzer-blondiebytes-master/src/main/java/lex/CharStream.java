package lex;

import errors.LexicalError;

import java.io.*;
import java.net.URL;
import java.util.Stack;

/** The CharStream class reads a text file and presents "significant"
 * characters one at a time via the <code>currentChar()</code> method.
 * Comments are ignored (treated as white-space) and multiple white-space
 * characters are treated as a single space character.
 *
 * When the CharStream has reached end of the file the
 * <code>currentChar()</code> method will return <code>CharStream.EOF</code>.
 */
public class CharStream
{
	/** Character returned for all white-space characters. */
	public static final char BLANK = ' ';
	/** Character returned when end of file is reached. This is also the
	 * value that <code>BufferedReader.read()</code> returns when it reaches
	 * the end of file.
	 */
	public static final char EOF = (char)-1;

	/** Character used to mark the start of a comment. */
	private static final char L_CURLY = '{';
	/** Character used to mark the end of a comment. */
	private static final char R_CURLY = '}';

	/** A list of the valid characters that may appear in the source code. */
	private static final String VALID_CHARS =
			  "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890" +
						 ".,;:<>/*[]+-=(){\t ";

	/** Used to read the source code file. */
	private BufferedReader reader = null;
	/** The current line being processed. */
	private String currentLine = null;
	/** The current character that has been read from the file. */
	private char currentChar = 0;
	/** Index of the current character in the current line. */
	private int charIndex;
	/** The current line number being read. */
	private int lineNumber = 0;
	/** Stack used to hold characters that are "put back". */
	private Stack<Character> stack = new Stack<Character>();

	/** Creates a CharStream object and opens filename for reading.  The
	 * <code>isOpen()</code> method should be checked after constructing a
	 * CharStream object with this constructor to ensure that a
	 * <code>LexicalError</code> wasn't encounted during the
	 * <code>skipWhiteSpace()</code> method .
	 *
	 * @see LexicalError
	 */
	public CharStream(String filename) throws IOException, LexicalError
	{
		super();
		open(getReader(filename));
	}

	public CharStream(URL url) throws IOException, LexicalError
	{
		super();
		open(getReader(url));
	}

	public CharStream(File file) throws IOException, LexicalError
	{
		super();
		open(getReader(file));
	}

	/**
	 * Attempts to open the given file. Returns true if the file was opened
	 * successfully, or false otherwise.
	 *
	 * @param reader BufferedReader The input source.
	 * @return boolean True if the file was opened, false otherwise.
	 */
	public boolean open(BufferedReader reader) throws IOException, LexicalError
	{
//      try
//      {
		this.reader = reader;
		lineNumber = 1;
		currentLine = reader.readLine();
		currentChar = getChar();
		skipWhiteSpace();
//         System.out.println("No Exception");
//      }
//      catch (Exception ex)
//      {
//         System.out.println("Caught: " + ex.getClass().getName());
//         ex.printStackTrace(System.out);
//         reader = null;
//      }
		return reader != null;
	}

	public boolean open(URL url)
	{
		try
		{
			reader = new BufferedReader(new InputStreamReader(url.openStream()));
			lineNumber = 1;
			currentLine = reader.readLine();
			currentChar = getChar();
			skipWhiteSpace();
		}
		catch (IOException e)
		{
			System.out.println("Unable to open the URL for reading.");
			e.printStackTrace();
			reader = null;
		}
		catch (LexicalError e)
		{
			System.out.println("Error skippining initial whitespace in the file.");
			e.printStackTrace();
			close(reader);
			reader = null;
		}
		return reader != null;
	}

	protected BufferedReader getReader(String filename) throws FileNotFoundException
	{
		return new BufferedReader(new FileReader(filename));
	}

	protected BufferedReader getReader(File file) throws FileNotFoundException
	{
		return new BufferedReader(new FileReader(file));
	}

	protected BufferedReader getReader(URL url) throws IOException
	{
		return new BufferedReader(new InputStreamReader(url.openStream()));
	}
	/** Returns true if a file has be opened, false otherwise. */
	public boolean isOpen() { return reader != null; }

	/** Returns the number of the line currently being scanned. Returns
	 * 0 (zero) if no file is currently open.
	 */
	public int lineNumber() { return lineNumber; }

	/** Prints the current line to std output. */
	public void dumpLine()
	{
		System.out.println(currentLine);
	}

	/** Returns the current line. */
	public String getCurrentLine() { return currentLine; }

	/** Closes the currently open file.  Has no effect if no file is open. */
	public void close()
	{
		if (reader != null)
		{
			currentChar = (char) EOF;
			lineNumber = 0;
			close(reader);
		}
	}

	/** Returns a character to the stream.  The character returned to the
	 * stream will be the next character returned by the <code>currentChar<></code>
	 * method.
	 */
	public void pushBack(int ch)
	{
		stack.push((char)ch);
	}

	/** Returns true if the character ch is allowed to appear in a source
	 * file.  Returns false otherwise.
	 */
	public boolean valid(char ch)
	{
		return VALID_CHARS.indexOf(ch) >= 0;
	}

	/** Returns the next character from the file, or CharStream.EOF if the
	 * end of file has been reached.
	 */
	public char currentChar() throws LexicalError
	{
		// Return characters from the stack if there are any.
		if (!stack.empty())
		{
			return stack.pop();
		}
		// If the current character is a white space character then skip over
		// any following white space characters and return CharStream.BLANK.
		if (Character.isWhitespace(currentChar) || currentChar == L_CURLY)
		{
			skipWhiteSpace();
			return BLANK;
		}
		// Otherwise we will return the current character and prime the
		// currentChar variable for the next read.
		char ch = currentChar;
		currentChar = getChar();

		// Make sure ch is a valid character.
		if (ch != EOF && !valid(ch))
		{
			throw LexicalError.IllegalCharacter(ch, lineNumber);
		}
		return ch;
	}

	/** Skips over consecutive white space characters.  Comments are treated
	 * as white space.
	 */
	protected void skipWhiteSpace() throws LexicalError
	{
		while (currentChar == L_CURLY || Character.isWhitespace(currentChar))
		{
			if (currentChar == L_CURLY)
			{
				skipComment();
			}
			else
			{
				currentChar = getChar();
			}
		}
	}

	/** Skips characters until the right brace is encountered. */
	protected void skipComment() throws LexicalError
	{
		currentChar = getChar();
		while (currentChar != EOF && currentChar != R_CURLY)
		{
			if (currentChar == L_CURLY)
			{
				throw LexicalError.BadComment(lineNumber);
			}
			currentChar = getChar();
		}
		if (currentChar == EOF)
		{
			throw LexicalError.UnterminatedComment(lineNumber);
		}
		currentChar = getChar();
	}

	private void getLine()
	{
		while (currentLine != null && charIndex >= currentLine.length())
		{
			charIndex = 0;
			++lineNumber;

			try
			{
				currentLine = reader.readLine();
			}
			catch (IOException ex)
			{
				System.out.println(ex);
				ex.printStackTrace(System.out);
				currentLine = null;
			}
		}
	}

	private void close(Reader reader)
	{
		try
		{
			reader.close();
		}
		catch (IOException ignored)
		{
			// Ignore.
		}
	}

	/** Reads a character from the source file.  If an IOException occurs
	 * during the read an EOF will be returned.
	 */
	private char getChar()
	{
		if (currentLine != null && charIndex >= currentLine.length())
		{
			getLine();
			return '\n';
		}
		if (currentLine == null)
		{
			return EOF;
		}
		return currentLine.charAt(charIndex++);
	}
}
