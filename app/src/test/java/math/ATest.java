package math;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ATest {
	ASCIIMathTeXImg cut = new ASCIIMathTeXImg();

	@ParameterizedTest
	@CsvSource(value = {
		" a ",
	})
	void atest(String input) {
		final String res = cut.getTeX(input);
		System.out.println(input + " -> " + res);
		assertNotNull(res, "Result must be not null");
	}
}