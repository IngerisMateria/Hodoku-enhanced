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

import solver.SudokuSolverFactory;
import solver.SudokuStepFinder;
import sudoku.SolutionStep;
import sudoku.SolutionType;
import sudoku.Sudoku2;

/**
 * Runs one technique fixture case in the HoDoKu library format (the format of
 * {@code sudoku.RegressionTester} / {@code ClipboardMode.LIBRARY}):
 *
 * <pre>
 * :libcode[-x]:cands:grid81:already-deleted-candidates:step-eliminations:comment
 * </pre>
 *
 * The grid (field 3, with {@code +} prefixing solved non-given cells) plus the
 * already-deleted candidates (field 4, {@code crc} triples) define the puzzle
 * state; the library code (field 1) names the expected technique via
 * {@link SolutionType#getTypeFromLibraryType(String)}. A {@code -x} suffix
 * marks a fail case: no step of that technique may exist at that state.
 *
 * The runner only asserts presence/absence of the technique (via
 * {@link SudokuStepFinder#getStep(SolutionType)} with the default option set of
 * the harness); it does not compare the concrete eliminations, and the numeric
 * {@code -N} sub-variants of RegressionTester (which toggle solver options per
 * case) are not supported. See docs/harness.md.
 */
public final class LibraryCaseRunner {

	/** Outcome of one library case. */
	public static final class Result {
		/** the parsed technique. */
		public final SolutionType type;
		/** true if the case demands the technique to be absent. */
		public final boolean failCase;
		/** true if a step of the technique exists at the given state. */
		public final boolean found;
		/** the first found step, or null. */
		public final SolutionStep step;

		private Result(SolutionType type, boolean failCase, boolean found, SolutionStep step) {
			this.type = type;
			this.failCase = failCase;
			this.found = found;
			this.step = step;
		}

		/** true if the case expectation holds (found XOR failCase). */
		public boolean passed() {
			return found != failCase;
		}
	}

	private LibraryCaseRunner() {
	}

	/**
	 * Parses and runs one library-format line.
	 *
	 * @throws IllegalArgumentException on malformed lines, unknown library codes
	 *                                  or unsupported {@code -N} sub-variants
	 */
	public static synchronized Result run(String libraryLine) {
		HarnessRunner.initialize();
		String line = libraryLine == null ? "" : libraryLine.trim();
		String[] parts = line.split(":");
		if (parts.length < 4 || !parts[0].isEmpty()) {
			throw new IllegalArgumentException("not a library format line: " + line);
		}

		String code = parts[1];
		boolean failCase = false;
		int suffix = code.indexOf('-');
		if (suffix >= 0) {
			if (code.length() == suffix + 2 && code.charAt(suffix + 1) == 'x') {
				failCase = true;
			} else {
				throw new IllegalArgumentException(
						"numeric sub-variants of RegressionTester are not supported: " + code);
			}
			code = code.substring(0, suffix);
		}
		SolutionType type = SolutionType.getTypeFromLibraryType(code);
		if (type == null) {
			throw new IllegalArgumentException("unknown library technique code: " + code);
		}

		// Sudoku2.setSudoku() parses the library format itself, but chokes on the
		// fail-case suffix, so rebuild the line with the bare code (same trick as
		// RegressionTester).
		StringBuilder rebuilt = new StringBuilder(":").append(code);
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
		SolutionStep step = finder.getStep(type);
		return new Result(type, failCase, step != null, step);
	}
}
