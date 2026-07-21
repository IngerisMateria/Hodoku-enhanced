/*
 * This file is part of the HoDoKu fork verification harness (milestone 0.3).
 *
 * HoDoKu is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * HoDoKu is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with HoDoKu. If not, see <http://www.gnu.org/licenses/>.
 */
package harness;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

/**
 * The v2 technique-fixture mechanism (milestone 0.3): every library-format case
 * in test/fixtures/libs/ must hold — the expected technique is found at the
 * given state, or, for {@code -x} fail cases, no step of the technique exists.
 * Phase 1 technique milestones add their fixtures as lib files here.
 */
public class LibraryCaseRunnerTest {

	/**
	 * Every lib file under test/fixtures/libs/. Phase 1 technique milestones
	 * register their fixture files here (see docs/plantilla-tecnica.md).
	 */
	private static final String[] LIB_FILES = {
			"phase1-examples.txt",
			"bent-quad.txt", // milestone 1.1 (como WXYZ-Wing; renombrado en 1.2)
			"wxyz-wing.txt", // milestone 1.2 (WXYZ-Wing canonico)
			"vwxyz-wing.txt", // milestone 1.3 (bent subsets n=5..9)
			"uvwxyz-wing.txt",
			"tuvwxyz-wing.txt",
			"stuvwxyz-wing.txt",
			"rstuvwxyz-wing.txt",
			"broken-wing.txt", // milestone 1.6 (oddagons I)
			"bivalue-oddagon.txt",
			"tridagon.txt", // milestone 1.7 (oddagons II)
	};

	@TestFactory
	public Stream<DynamicTest> libraryCasesHold() {
		return java.util.Arrays.stream(LIB_FILES).flatMap(file -> {
			List<String> cases = Fixtures.lines("/fixtures/libs/" + file);
			assertTrue(cases.size() >= 4, "expected at least 4 cases in " + file + ", got " + cases.size());
			return cases.stream().map(line -> dynamicTest(file + " " + caseName(line), () -> {
				LibraryCaseRunner.Result result = LibraryCaseRunner.run(line);
				if (result.failCase) {
					assertFalse(result.found, "technique must NOT be found for fail case: " + line
							+ "\nbut found: " + result.step);
				} else {
					assertTrue(result.found, "technique not found for case: " + line);
				}
			}));
		});
	}

	@Test
	public void malformedLinesAreRejected() {
		assertThrows(IllegalArgumentException.class, () -> LibraryCaseRunner.run("not a library line"));
		// unknown technique code
		assertThrows(IllegalArgumentException.class, () -> LibraryCaseRunner.run(":9999:1:" + ".".repeat(81) + ":::"));
		// numeric RegressionTester sub-variants are explicitly unsupported
		assertThrows(IllegalArgumentException.class, () -> LibraryCaseRunner.run(":0803-1:1:" + ".".repeat(81) + ":::"));
	}

	private static String caseName(String line) {
		int secondColon = line.indexOf(':', 1);
		return "case " + line.substring(1, secondColon) + " @" + Integer.toHexString(line.hashCode());
	}
}
