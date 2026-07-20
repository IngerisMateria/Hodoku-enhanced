/*
 * This file is part of the HoDoKu fork verification harness (milestone 0.2).
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Point 6 of the milestone: prove that the soundness detector actually fires.
 * Synthetic steps with known-bad placements/eliminations must be rejected; a
 * detector that never rang is untested.
 */
public class SoundnessValidatorTest {

	/** Solution of the reference puzzle (any valid grid works). */
	private static final int[] SOLUTION = parse(
			"672145398145983672389762451263574819958621743714398526597236184426817935831459267");

	private static int[] parse(String s) {
		int[] solution = new int[81];
		for (int i = 0; i < 81; i++) {
			solution[i] = s.charAt(i) - '0';
		}
		return solution;
	}

	@Test
	public void soundStepPassesValidation() {
		// r1c1=6 matches the solution; eliminating 5 from r1c1 is fine (digit is 6)
		StepRecord step = new StepRecord("HIDDEN_SINGLE",
				Collections.singletonList(new CellValue(0, 6)),
				Collections.singletonList(new CellValue(0, 5)));
		assertTrue(SoundnessValidator.validate(step, SOLUTION).isEmpty(),
				"a sound step must produce no violations");
	}

	@Test
	public void invalidEliminationIsRejected() {
		// r1c1 has solution digit 6: eliminating candidate 6 there is unsound
		StepRecord step = new StepRecord("NAKED_PAIR",
				Collections.<CellValue>emptyList(),
				Arrays.asList(new CellValue(0, 5), new CellValue(0, 6)));
		List<String> violations = SoundnessValidator.validate(step, SOLUTION);
		assertEquals(1, violations.size(), "exactly the bad elimination must be flagged");
		assertTrue(violations.get(0).contains("r1c1"), "violation must name the offending cell");
		assertTrue(violations.get(0).contains("NAKED_PAIR"), "violation must name the technique");
	}

	@Test
	public void invalidPlacementIsRejected() {
		// r1c2 has solution digit 7: placing 3 there is unsound
		StepRecord step = new StepRecord("FULL_HOUSE",
				Collections.singletonList(new CellValue(1, 3)),
				Collections.<CellValue>emptyList());
		List<String> violations = SoundnessValidator.validate(step, SOLUTION);
		assertEquals(1, violations.size());
		assertTrue(violations.get(0).contains("r1c2"), "violation must name the offending cell");
	}

	@Test
	public void puzzleLevelReportNamesPuzzleAndStepNumber() {
		StepRecord bad = new StepRecord("X_WING",
				Collections.<CellValue>emptyList(),
				Collections.singletonList(new CellValue(80, SOLUTION[80])));
		PuzzleResult result = PuzzleResult.of("dummy-puzzle", SOLUTION, false, "EASY",
				Arrays.asList(
						new StepRecord("NAKED_SINGLE", Collections.singletonList(new CellValue(0, 6)),
								Collections.<CellValue>emptyList()),
						bad));
		List<String> violations = SoundnessValidator.validate(result);
		assertEquals(1, violations.size());
		assertTrue(violations.get(0).contains("dummy-puzzle"), "report must include the puzzle");
		assertTrue(violations.get(0).contains("step 2"), "report must include the step number");
		assertTrue(violations.get(0).contains("r9c9"), "report must include the offending elimination");
	}
}
