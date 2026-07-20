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

import java.util.Collections;
import java.util.List;

/**
 * Result of running the logical solver over one puzzle, plus the brute-force
 * ground truth. Serializes to one JSON Lines object (see docs/harness.md for
 * the schema).
 */
public final class PuzzleResult {

	/** The 81 character puzzle string as read from the input. */
	public final String puzzle;
	/** Number of brute-force solutions: 0, 1 or 2 (2 means "2 or more"). */
	public final int solutions;
	/** The unique solution as an array of 81 digits 1-9; null if not unique. */
	public final int[] solution;
	/** true if the logical solve reached the full solution. */
	public final boolean solved;
	/** DifficultyType enum name after solving (EASY..EXTREME); null on error. */
	public final String level;
	/** The full logical solve path; empty on error. */
	public final List<StepRecord> steps;
	/** Error description for invalid input / non-unique puzzles; else null. */
	public final String error;

	private PuzzleResult(String puzzle, int solutions, int[] solution, boolean solved, String level,
			List<StepRecord> steps, String error) {
		this.puzzle = puzzle;
		this.solutions = solutions;
		this.solution = solution;
		this.solved = solved;
		this.level = level;
		this.steps = Collections.unmodifiableList(steps);
		this.error = error;
	}

	public static PuzzleResult of(String puzzle, int[] solution, boolean solved, String level,
			List<StepRecord> steps) {
		return new PuzzleResult(puzzle, 1, solution, solved, level, steps, null);
	}

	public static PuzzleResult invalid(String puzzle, String error) {
		return new PuzzleResult(puzzle, -1, null, false, null, Collections.<StepRecord>emptyList(), error);
	}

	public static PuzzleResult notUnique(String puzzle, int solutions) {
		String error = solutions == 0 ? "puzzle has no solution" : "puzzle has more than one solution";
		return new PuzzleResult(puzzle, solutions, null, false, null, Collections.<StepRecord>emptyList(), error);
	}

	/** The solution as an 81 character digit string. */
	public String solutionString() {
		if (solution == null) {
			return null;
		}
		StringBuilder sb = new StringBuilder(81);
		for (int i = 0; i < solution.length; i++) {
			sb.append(solution[i]);
		}
		return sb.toString();
	}

	/** Serializes this result as a single JSON line (no trailing newline). */
	public String toJsonLine() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"puzzle\":").append(Json.str(puzzle));
		if (error != null) {
			if (solutions >= 0) {
				sb.append(",\"solutions\":").append(solutions);
			}
			sb.append(",\"error\":").append(Json.str(error));
			sb.append('}');
			return sb.toString();
		}
		sb.append(",\"solutions\":").append(solutions);
		sb.append(",\"solution\":").append(Json.str(solutionString()));
		sb.append(",\"solved\":").append(solved);
		sb.append(",\"level\":").append(Json.str(level));
		sb.append(",\"steps\":[");
		for (int i = 0; i < steps.size(); i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(steps.get(i).toJson());
		}
		sb.append("]}");
		return sb.toString();
	}
}
