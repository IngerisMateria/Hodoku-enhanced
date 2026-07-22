/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.9).
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import solver.SudokuSolverFactory;
import solver.SudokuStepFinder;
import sudoku.SolutionStep;
import sudoku.SolutionType;
import sudoku.Sudoku2;

/**
 * Custody of the Extended UR desglose (milestone 1.9): on every positive
 * Extended UR fixture state the generic {@link SolutionType#EXTENDED_UR} anchor
 * (library code 0621) still finds a step, that step now carries one of the two
 * subtype labels, and the per-subtype pushdown filter is consistent — asking
 * for the subtype the generic search returned finds it, and any step returned
 * for a specific subtype actually carries that subtype. This is the "same
 * deduction, different label" contract of the split.
 */
public class ExtendedUrSplitTest {

	@TestFactory
	public Stream<DynamicTest> extendedUrStatesEmitSubtypesAndFilterConsistently() {
		List<String> cases = Fixtures.lines("/fixtures/libs/extended-ur.txt");
		assertTrue(cases.size() >= 4, "expected the Extended UR fixture positives");
		return cases.stream().map(line -> dynamicTest("xur split @" + Integer.toHexString(line.hashCode()), () -> {
			SudokuStepFinder finder = newFinder(line);
			SolutionStep generic = finder.getStep(SolutionType.EXTENDED_UR);
			assertNotNull(generic, "the generic anchor must still find an Extended UR: " + line);
			SolutionType found = generic.getType();
			assertTrue(found == SolutionType.EXTENDED_UR_TYPE_1 || found == SolutionType.EXTENDED_UR_TYPE_2,
					"the generic search must emit a subtype label, not the bare anchor: " + found);

			SolutionStep t1 = newFinder(line).getStep(SolutionType.EXTENDED_UR_TYPE_1);
			SolutionStep t2 = newFinder(line).getStep(SolutionType.EXTENDED_UR_TYPE_2);
			if (t1 != null) {
				assertEquals(SolutionType.EXTENDED_UR_TYPE_1, t1.getType(),
						"a step returned for Type 1 must carry the Type 1 label");
			}
			if (t2 != null) {
				assertEquals(SolutionType.EXTENDED_UR_TYPE_2, t2.getType(),
						"a step returned for Type 2 must carry the Type 2 label");
			}
			// filter consistency: the subtype the generic search returned must be
			// reachable through its own pushdown filter
			if (found == SolutionType.EXTENDED_UR_TYPE_1) {
				assertNotNull(t1, "generic found Type 1, so the Type 1 filter must find it too: " + line);
			} else {
				assertNotNull(t2, "generic found Type 2, so the Type 2 filter must find it too: " + line);
			}
		}));
	}

	/** Builds a fresh finder on the state of one library-format fixture line. */
	private static SudokuStepFinder newFinder(String libraryLine) {
		HarnessRunner.initialize();
		String[] parts = libraryLine.trim().split(":");
		// rebuild without the (bare) code, padded to the six-colon library shape
		StringBuilder rebuilt = new StringBuilder(":").append(parts[1]);
		for (int i = 2; i < parts.length; i++) {
			rebuilt.append(':').append(parts[i]);
		}
		while (rebuilt.chars().filter(ch -> ch == ':').count() < 6) {
			rebuilt.append(':');
		}
		Sudoku2 sudoku = new Sudoku2();
		sudoku.setSudoku(rebuilt.toString());
		SudokuStepFinder finder = SudokuSolverFactory.getDefaultSolverInstance().getStepFinder();
		finder.setSudoku(sudoku);
		return finder;
	}
}
