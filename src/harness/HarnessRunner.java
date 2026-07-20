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

import generator.SudokuGenerator;
import generator.SudokuGeneratorFactory;
import solver.SudokuSolver;
import sudoku.Options;
import sudoku.SolutionStep;
import sudoku.Sudoku2;

/**
 * Runs one puzzle through brute force (uniqueness + ground-truth solution) and
 * through the full logical solver with the default technique configuration,
 * producing a {@link PuzzleResult}.
 *
 * Forces {@link Options#resetAll()} once: Options.getInstance() would silently
 * load a hodoku.hcfg from java.io.tmpdir if one exists, which would make the
 * solve path depend on the machine. Not for use from the GUI.
 */
public final class HarnessRunner {

	private static boolean initialized = false;
	private static SudokuSolver solver = null;

	private HarnessRunner() {
	}

	private static synchronized void initialize() {
		if (!initialized) {
			Options.resetAll();
			// creating one Sudoku2 up front initializes the static tables
			new Sudoku2();
			solver = new SudokuSolver();
			initialized = true;
		}
	}

	/** true if <code>line</code> is 81 chars of digits and '.' (0 = empty). */
	public static boolean isValidPuzzleLine(String line) {
		if (line == null || line.length() != 81) {
			return false;
		}
		for (int i = 0; i < 81; i++) {
			char ch = line.charAt(i);
			if (ch != '.' && (ch < '0' || ch > '9')) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Analyzes one puzzle line: uniqueness check via brute force, then the
	 * complete logical solve path with the default technique configuration.
	 */
	public static synchronized PuzzleResult analyze(String line) {
		initialize();
		String puzzle = line == null ? "" : line.trim();
		if (!isValidPuzzleLine(puzzle)) {
			return PuzzleResult.invalid(puzzle, "invalid puzzle line (expected 81 chars of [0-9.])");
		}

		Sudoku2 sudoku = new Sudoku2();
		sudoku.setSudoku(puzzle);

		SudokuGenerator generator = SudokuGeneratorFactory.getDefaultGeneratorInstance();
		int solutions = generator.getNumberOfSolutions(sudoku, 2);
		if (solutions != 1) {
			return PuzzleResult.notUnique(puzzle, solutions);
		}
		// getNumberOfSolutions() stored the solution in the sudoku
		int[] solution = sudoku.getSolution().clone();

		Sudoku2 work = sudoku.clone();
		solver.setSudoku(work);
		solver.solve();
		// extract the steps immediately: the next setSudoku() clears the list
		List<StepRecord> steps = new ArrayList<StepRecord>();
		for (SolutionStep step : solver.getSteps()) {
			steps.add(StepRecord.from(step));
		}

		String level = work.getLevel().getType().name();
		return PuzzleResult.of(puzzle, solution, work.isSolved(), level, steps);
	}
}
