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

import java.util.ArrayList;
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
 * Custody of the whole Uniqueness Pack desglose (milestone 1.9): on every
 * positive fixture state of a guardian technique (Unique Loop, Extended UR,
 * BUG-Lite, MUG) the generic anchor still finds a step, that step now carries
 * one of the two subtype labels, and the per-subtype pushdown filter is
 * consistent — asking for the subtype the generic search returned finds it, and
 * any step returned for a specific subtype actually carries that subtype. This
 * is the "same deduction, different label" contract of the split. Reverse BUG
 * has no subtypes and is not covered here.
 */
public class UniquenessPackSplitTest {

	/** anchor type, its two subtypes, and the fixture file of positives. */
	private static final Object[][] SPLITS = {
			{ SolutionType.UNIQUE_LOOP, SolutionType.UNIQUE_LOOP_TYPE_1, SolutionType.UNIQUE_LOOP_TYPE_2,
					"unique-loop.txt" },
			{ SolutionType.EXTENDED_UR, SolutionType.EXTENDED_UR_TYPE_1, SolutionType.EXTENDED_UR_TYPE_2,
					"extended-ur.txt" },
			{ SolutionType.BUG_LITE, SolutionType.BUG_LITE_TYPE_1, SolutionType.BUG_LITE_TYPE_2, "bug-lite.txt" },
			{ SolutionType.MUG, SolutionType.MUG_TYPE_1, SolutionType.MUG_TYPE_2, "mug.txt" } };

	@TestFactory
	public Stream<DynamicTest> anchorEmitsSubtypesAndFilterIsConsistent() {
		List<DynamicTest> tests = new ArrayList<>();
		for (Object[] split : SPLITS) {
			SolutionType anchor = (SolutionType) split[0];
			SolutionType type1 = (SolutionType) split[1];
			SolutionType type2 = (SolutionType) split[2];
			String file = (String) split[3];
			for (String line : Fixtures.lines("/fixtures/libs/" + file)) {
				if (isFailCase(line)) {
					continue; // -x negatives: the technique must be absent, not split
				}
				tests.add(dynamicTest(file + " " + anchor + " @" + Integer.toHexString(line.hashCode()), () -> {
					SolutionStep generic = newFinder(line).getStep(anchor);
					assertNotNull(generic, "the generic anchor must still find a step: " + line);
					SolutionType found = generic.getType();
					assertTrue(found == type1 || found == type2,
							"the generic search must emit a subtype label, not the bare anchor: " + found);

					SolutionStep s1 = newFinder(line).getStep(type1);
					SolutionStep s2 = newFinder(line).getStep(type2);
					if (s1 != null) {
						assertEquals(type1, s1.getType(), "a step returned for Type 1 must carry the Type 1 label");
					}
					if (s2 != null) {
						assertEquals(type2, s2.getType(), "a step returned for Type 2 must carry the Type 2 label");
					}
					// the subtype the generic returned must be reachable via its filter
					if (found == type1) {
						assertNotNull(s1, "generic found Type 1, so the Type 1 filter must find it too: " + line);
					} else {
						assertNotNull(s2, "generic found Type 2, so the Type 2 filter must find it too: " + line);
					}
				}));
			}
		}
		return tests.stream();
	}

	/** true if the library code carries the {@code -x} fail-case suffix. */
	private static boolean isFailCase(String line) {
		String[] parts = line.trim().split(":");
		return parts.length > 1 && parts[1].endsWith("-x");
	}

	/** Builds a fresh finder on the state of one library-format fixture line. */
	private static SudokuStepFinder newFinder(String libraryLine) {
		HarnessRunner.initialize();
		String[] parts = libraryLine.trim().split(":");
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
