/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.6).
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
package solver.modern;

import sudoku.Sudoku2;
import sudoku.SudokuSet;

/**
 * The guardian set of an impossible pattern P (milestone 1.6, the shared core
 * of the oddagon family; Tridagon in milestone 1.7 reuses it).
 *
 * A <i>guardian</i> is a candidate whose simultaneous falseness together with
 * all other guardians would make the impossible pattern P real. Since P cannot
 * occur in a valid puzzle, <b>at least one guardian is true</b>. The
 * deductions a finder can draw from this class:
 * <ul>
 * <li>{@link #size()} == 0: the state itself is contradictory — cannot happen
 * in a valid puzzle. Callers must log and skip, never "eliminate
 * everything".</li>
 * <li>{@link #size()} == 1: the single guardian is TRUE. What that means is
 * technique-specific (Broken Wing: place the digit; Bivalue Oddagon: remove
 * the cycle pair from the guardian cell).</li>
 * <li>{@link #size()} &gt; 1 and {@link #uniformDigit()} != -1: some guardian
 * of digit g is true, so g can be eliminated from every external cell that
 * sees ALL guardian cells ({@link #collectBuddies(SudokuSet)}).</li>
 * <li>{@link #size()} &gt; 1 with mixed digits: no direct deduction in 1.6
 * (chain territory).</li>
 * </ul>
 *
 * Plain reusable buffer, no allocation per pattern: {@link #clear()} between
 * patterns.
 */
public class Guardians {

	/**
	 * More than enough for every client pattern: oddagon cycles of length
	 * &le; 7, and the Tridagon's 12 cells &times; up to 6 non-triple
	 * candidates each (milestone 1.7).
	 */
	private static final int MAX_GUARDIANS = 128;

	/** The guardian cells (parallel to {@link #digits}; cells may repeat). */
	private final int[] cells = new int[MAX_GUARDIANS];
	/** The guardian digits (parallel to {@link #cells}). */
	private final int[] digits = new int[MAX_GUARDIANS];
	/** The number of guardians collected. */
	private int size = 0;
	/** The distinct guardian cells as a set. */
	private final SudokuSet cellSet = new SudokuSet();

	/** Removes all guardians (call before collecting a new pattern). */
	public void clear() {
		size = 0;
		cellSet.clear();
	}

	/**
	 * Adds one guardian candidate.
	 *
	 * @param cell the cell index
	 * @param digit the candidate digit
	 */
	public void add(int cell, int digit) {
		cells[size] = cell;
		digits[size] = digit;
		size++;
		cellSet.add(cell);
	}

	/** @return the number of guardian candidates */
	public int size() {
		return size;
	}

	/** @param i guardian index; @return its cell */
	public int getCell(int i) {
		return cells[i];
	}

	/** @param i guardian index; @return its digit */
	public int getDigit(int i) {
		return digits[i];
	}

	/** @return true if the cell holds at least one guardian */
	public boolean isGuardianCell(int cell) {
		return cellSet.contains(cell);
	}

	/** @return the distinct guardian cells */
	public SudokuSet getCellSet() {
		return cellSet;
	}

	/**
	 * The digit shared by ALL guardians, or -1 if the set is empty or mixed.
	 *
	 * @return the uniform guardian digit or -1
	 */
	public int uniformDigit() {
		if (size == 0) {
			return -1;
		}
		for (int i = 1; i < size; i++) {
			if (digits[i] != digits[0]) {
				return -1;
			}
		}
		return digits[0];
	}

	/**
	 * Sets <code>result</code> to the cells that see EVERY guardian cell (the
	 * intersection of the buddies of all guardian cells; no cell is its own
	 * buddy, so guardian cells that do not see each other are excluded
	 * automatically — callers still have to remove the pattern/guardian cells
	 * themselves before eliminating).
	 *
	 * @param result the set to fill
	 */
	public void collectBuddies(SudokuSet result) {
		if (size == 0) {
			result.clear();
			return;
		}
		result.set(Sudoku2.buddies[cells[0]]);
		for (int i = 1; i < size; i++) {
			result.and(Sudoku2.buddies[cells[i]]);
		}
	}
}
