package parser;

import errors.CompilerError;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This test runs all Pascal files in src/test/resources through the Parser.  Use this
 * instead of the ParserTest class if you want to add/remove a large number of tests
 * and not bother with having to edit the ParserTest class.  Be sure to @Ignore any tests
 * in ParserTest if you remove the Pascal source (not recommended).
 */


@Ignore
public class AllFilesTest
{
	@Test
	public void testAllFiles() {
		File directory = new File("src/test/resources");
		assertTrue(directory.exists());

		// Lambda expression that compiles the file and returns null
		// on success or an error message on failure.
		Function<File,String> compile = (File file) -> {
			try {
				System.out.println("Compiling " + file.getName());
				new Parser(file).parse();
			}
			catch(IOException | CompilerError e)
			{
				return e.getMessage();
			}
			return null;
		};
		List<String> errors = Arrays.stream(directory.listFiles())
				//.parallel()
				.filter((f) -> f.getName().endsWith(".pas"))
				.map(compile)
				.filter((s) -> s != null)
				.collect(Collectors.toList());
		if (errors.size() == 0) {
			System.out.println("All tests passed.");
		}
		else {
			errors.forEach(System.out::println);
			fail(errors.size() + " files failed to compile.");
		}
	}

}
