package math;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ATest {
	ASCIIMathTeXImg cut = new ASCIIMathTeXImg();

	@ParameterizedTest
	@CsvSource(value = {
		" a ",
	})
	public void atest(String input) {
		final String res = cut.getTeX(input);
		System.out.println(input + " -> " + res);
		assert(true);
	}
}