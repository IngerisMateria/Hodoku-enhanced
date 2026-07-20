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

import java.util.ArrayList;
import java.util.List;

/**
 * Checks solve steps against the brute-force solution of the puzzle:
 * <ul>
 * <li>no step may eliminate a candidate that matches the solution digit of its
 * cell, and</li>
 * <li>no step may place a value that differs from the solution digit of its
 * cell.</li>
 * </ul>
 * Any violation means the technique implementation is buggy (unsound).
 */
public final class SoundnessValidator {

	private SoundnessValidator() {
	}

	/**
	 * Validates one step against the solution (81 digits 1-9). Returns one
	 * human-readable violation description per offending placement or
	 * elimination; an empty list means the step is sound.
	 */
	public static List<String> validate(StepRecord step, int[] solution) {
		List<String> violations = new ArrayList<String>();
		for (CellValue placement : step.placements) {
			int expected = solution[placement.index];
			if (placement.value != expected) {
				violations.add(step.technique + " places " + placement.value + " in " + placement.cell()
						+ " but the solution digit is " + expected);
			}
		}
		for (CellValue elimination : step.eliminations) {
			if (elimination.value == solution[elimination.index]) {
				violations.add(step.technique + " eliminates candidate " + elimination.value + " from "
						+ elimination.cell() + " which is the solution digit of that cell");
			}
		}
		return violations;
	}

	/**
	 * Validates the complete solve path of <code>result</code>. Each violation
	 * message includes the puzzle and the (1-based) step number.
	 */
	public static List<String> validate(PuzzleResult result) {
		List<String> violations = new ArrayList<String>();
		if (result.solution == null) {
			return violations;
		}
		for (int i = 0; i < result.steps.size(); i++) {
			for (String violation : validate(result.steps.get(i), result.solution)) {
				violations.add("puzzle " + result.puzzle + " step " + (i + 1) + ": " + violation);
			}
		}
		return violations;
	}
}
