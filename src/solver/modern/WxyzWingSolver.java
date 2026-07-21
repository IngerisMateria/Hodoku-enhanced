/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.1).
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import solver.AbstractSolver;
import solver.SudokuStepFinder;
import sudoku.Candidate;
import sudoku.SolutionStep;
import sudoku.SolutionType;
import sudoku.Sudoku2;
import sudoku.SudokuSet;

/**
 * Finder for WXYZ-Wings in the "bent naked subset" formulation (n = 4).
 *
 * A bent region is the union of a line L (row or column) and a box B with a
 * non-empty intersection (54 pairs). A WXYZ-Wing is a set of 4 unsolved cells
 * of L&#x222a;B with at least one cell in L\B and at least one in B\L (a set
 * contained in a single house is a classical Naked Quadruple and is skipped;
 * cells of L&#x2229;B may participate), whose candidate union has exactly 4
 * digits. A candidate is <i>restricted</i> if all set cells containing it see
 * each other (all in L or all in B), <i>non-restricted</i> otherwise. The
 * pattern is valid with exactly one non-restricted candidate Z (the "0
 * non-restricted" case, a plain locked-set elimination, is out of scope of
 * milestone 1.1 and a possible future extension).
 *
 * Justification: if no set cell held Z, the 4 cells would only hold the 3
 * restricted candidates, and each restricted candidate can appear at most once
 * (its cells see each other) - 3 values for 4 cells, contradiction. So Z is in
 * the set, and Z can be eliminated from every external cell that sees all set
 * cells containing Z.
 *
 * Structure mirrors {@link solver.WingSolver} (single-step and all-steps
 * mode); registered in {@link SudokuStepFinder}.
 */
public class WxyzWingSolver extends AbstractSolver {

	/** A line/box union has at most 15 cells. */
	private static final int MAX_REGION_CELLS = 15;
	/** Membership flag: cell belongs to the line. */
	private static final int IN_LINE = 1;
	/** Membership flag: cell belongs to the box. */
	private static final int IN_BOX = 2;

	/** One global step for eliminations (cloned when a wing is found). */
	private SolutionStep globalStep = new WxyzWingStep();
	/** A list for all steps found in one search. */
	private List<SolutionStep> steps = new ArrayList<SolutionStep>();
	/** A set for elimination checks. */
	private SudokuSet elimSet = new SudokuSet();
	/** The unsolved cells of the current bent region. */
	private int[] regionCells = new int[MAX_REGION_CELLS];
	/** The candidate mask of each region cell. */
	private short[] regionCands = new short[MAX_REGION_CELLS];
	/** Membership flags of each region cell ({@link #IN_LINE}/{@link #IN_BOX}). */
	private int[] regionFlags = new int[MAX_REGION_CELLS];
	/** The cells of the current 4-cell set that contain Z. */
	private int[] zCells = new int[4];
	/** The cells of the current 4-cell set, sorted for the step. */
	private int[] setCells = new int[4];

	/**
	 * Creates a new instance of WxyzWingSolver
	 *
	 * @param finder
	 */
	public WxyzWingSolver(SudokuStepFinder finder) {
		super(finder);
	}

	@Override
	protected SolutionStep getStep(SolutionType type) {
		SolutionStep result = null;
		sudoku = finder.getSudoku();
		if (type == SolutionType.WXYZ_WING) {
			result = findWxyzWing(true);
		}
		return result;
	}

	@Override
	protected boolean doStep(SolutionStep step) {
		boolean handled = true;
		sudoku = finder.getSudoku();
		if (step.getType() == SolutionType.WXYZ_WING) {
			for (Candidate cand : step.getCandidatesToDelete()) {
				sudoku.delCandidate(cand.getIndex(), cand.getValue());
			}
		} else {
			handled = false;
		}
		return handled;
	}

	/**
	 * Finds all WXYZ-Wings in the current sudoku (all-steps mode).
	 *
	 * @return all steps found
	 */
	public List<SolutionStep> getAllWxyzWings() {
		sudoku = finder.getSudoku();
		List<SolutionStep> newSteps = new ArrayList<SolutionStep>();
		List<SolutionStep> oldSteps = steps;
		steps = newSteps;
		findWxyzWing(false);
		steps = oldSteps;
		return newSteps;
	}

	/**
	 * Iterates all 54 bent regions.
	 *
	 * @param onlyOne return the first step found instead of collecting all
	 * @return the first step found if <code>onlyOne</code> is set, else null
	 */
	private SolutionStep findWxyzWing(boolean onlyOne) {
		// ALL_UNITS: 0-8 rows, 9-17 columns, 18-26 blocks
		for (int line = 0; line < 18; line++) {
			for (int box = 18; box < 27; box++) {
				if (!intersects(line, box)) {
					continue;
				}
				SolutionStep step = searchRegion(line, box, onlyOne);
				if (onlyOne && step != null) {
					return step;
				}
			}
		}
		return null;
	}

	/**
	 * Does line unit <code>line</code> intersect block unit
	 * <code>boxUnit</code> (both {@link Sudoku2#ALL_UNITS} indices)?
	 */
	private static boolean intersects(int line, int boxUnit) {
		int box = boxUnit - 18;
		if (line < 9) {
			// rows: same band
			return line / 3 == box / 3;
		}
		// columns: same stack
		return (line - 9) / 3 == box % 3;
	}

	/**
	 * Collects the unsolved cells of one bent region and checks all their
	 * 4-cell subsets.
	 */
	private SolutionStep searchRegion(int line, int box, boolean onlyOne) {
		int count = 0;
		int[] lineCells = Sudoku2.ALL_UNITS[line];
		for (int i = 0; i < lineCells.length; i++) {
			int index = lineCells[i];
			if (sudoku.getValue(index) != 0) {
				continue;
			}
			regionCells[count] = index;
			regionCands[count] = sudoku.getCell(index);
			regionFlags[count] = Sudoku2.ALL_CONSTRAINTS_TEMPLATES[box].contains(index) ? (IN_LINE | IN_BOX) : IN_LINE;
			count++;
		}
		int[] boxCells = Sudoku2.ALL_UNITS[box];
		for (int i = 0; i < boxCells.length; i++) {
			int index = boxCells[i];
			if (sudoku.getValue(index) != 0 || Sudoku2.ALL_CONSTRAINTS_TEMPLATES[line].contains(index)) {
				// solved, or already collected via the line
				continue;
			}
			regionCells[count] = index;
			regionCands[count] = sudoku.getCell(index);
			regionFlags[count] = IN_BOX;
			count++;
		}
		for (int a = 0; a < count - 3; a++) {
			for (int b = a + 1; b < count - 2; b++) {
				for (int c = b + 1; c < count - 1; c++) {
					for (int d = c + 1; d < count; d++) {
						SolutionStep step = checkSet(a, b, c, d, onlyOne);
						if (onlyOne && step != null) {
							return step;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * Checks one 4-cell subset of the current bent region (indices into
	 * {@link #regionCells}) for a WXYZ-Wing and creates the step if valid.
	 */
	private SolutionStep checkSet(int a, int b, int c, int d, boolean onlyOne) {
		short union = (short) (regionCands[a] | regionCands[b] | regionCands[c] | regionCands[d]);
		if (Sudoku2.ANZ_VALUES[union] != 4) {
			return null;
		}
		// really bent? otherwise it is a classical naked subset
		boolean lineOnly = false;
		boolean boxOnly = false;
		if (regionFlags[a] == IN_LINE || regionFlags[b] == IN_LINE || regionFlags[c] == IN_LINE
				|| regionFlags[d] == IN_LINE) {
			lineOnly = true;
		}
		if (regionFlags[a] == IN_BOX || regionFlags[b] == IN_BOX || regionFlags[c] == IN_BOX
				|| regionFlags[d] == IN_BOX) {
			boxOnly = true;
		}
		if (!lineOnly || !boxOnly) {
			return null;
		}
		// exactly one candidate may be non-restricted; a candidate is
		// restricted if the AND of the membership flags of its cells is != 0
		// (all cells in the line or all in the box)
		int[] cands = Sudoku2.POSSIBLE_VALUES[union];
		int zCand = -1;
		for (int i = 0; i < cands.length; i++) {
			short mask = Sudoku2.MASKS[cands[i]];
			int flagsAnd = IN_LINE | IN_BOX;
			if ((regionCands[a] & mask) != 0) {
				flagsAnd &= regionFlags[a];
			}
			if ((regionCands[b] & mask) != 0) {
				flagsAnd &= regionFlags[b];
			}
			if ((regionCands[c] & mask) != 0) {
				flagsAnd &= regionFlags[c];
			}
			if ((regionCands[d] & mask) != 0) {
				flagsAnd &= regionFlags[d];
			}
			if (flagsAnd == 0) {
				// non-restricted
				if (zCand != -1) {
					// second one -> no wing
					return null;
				}
				zCand = cands[i];
			}
		}
		if (zCand == -1) {
			// all restricted: locked set case, out of scope of 1.1
			return null;
		}
		// Z can be eliminated from all external cells that see every set cell
		// containing Z (a non-restricted candidate is in at least two cells)
		short zMask = Sudoku2.MASKS[zCand];
		int zAnz = 0;
		if ((regionCands[a] & zMask) != 0) {
			zCells[zAnz++] = regionCells[a];
		}
		if ((regionCands[b] & zMask) != 0) {
			zCells[zAnz++] = regionCells[b];
		}
		if ((regionCands[c] & zMask) != 0) {
			zCells[zAnz++] = regionCells[c];
		}
		if ((regionCands[d] & zMask) != 0) {
			zCells[zAnz++] = regionCells[d];
		}
		elimSet.setAnd(Sudoku2.buddies[zCells[0]], Sudoku2.buddies[zCells[1]]);
		for (int i = 2; i < zAnz; i++) {
			elimSet.and(Sudoku2.buddies[zCells[i]]);
		}
		// set cells themselves are never in the buddy intersection of the Z
		// cells (no cell is its own buddy, and set cells without Z have no Z)
		elimSet.and(finder.getCandidates()[zCand]);
		if (elimSet.isEmpty()) {
			return null;
		}
		// ok, wing found!
		globalStep.reset();
		globalStep.setType(SolutionType.WXYZ_WING);
		// values: the three restricted candidates ascending, Z last
		for (int i = 0; i < cands.length; i++) {
			if (cands[i] != zCand) {
				globalStep.addValue(cands[i]);
			}
		}
		globalStep.addValue(zCand);
		setCells[0] = regionCells[a];
		setCells[1] = regionCells[b];
		setCells[2] = regionCells[c];
		setCells[3] = regionCells[d];
		Arrays.sort(setCells);
		for (int i = 0; i < setCells.length; i++) {
			globalStep.addIndex(setCells[i]);
		}
		// fins: the Z candidates inside the set (mirrors XYZ-Wing)
		for (int i = 0; i < setCells.length; i++) {
			if (sudoku.isCandidate(setCells[i], zCand)) {
				globalStep.addFin(setCells[i], zCand);
			}
		}
		for (int i = 0; i < elimSet.size(); i++) {
			globalStep.addCandidateToDelete(elimSet.get(i), zCand);
		}
		SolutionStep step = (SolutionStep) globalStep.clone();
		if (onlyOne) {
			return step;
		}
		steps.add(step);
		return null;
	}
}
