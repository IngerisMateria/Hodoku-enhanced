/*
 * This file is part of the modern-techniques fork of HoDoKu (milestones 1.1/1.2).
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import solver.AbstractSolver;
import solver.SudokuStepFinder;
import sudoku.Candidate;
import sudoku.SolutionStep;
import sudoku.SolutionType;
import sudoku.Sudoku2;
import sudoku.SudokuSet;

/**
 * Parametric finder for bent naked subsets of size n (milestone 1.2; the n = 4
 * case was milestone 1.1's "WXYZ-Wing", renamed to Bent Quad).
 *
 * A bent region is the union of a line L (row or column) and a box B with a
 * non-empty intersection (54 pairs). A bent subset of size n is a set of n
 * unsolved cells of L&#x222a;B with at least one cell in L\B and at least one
 * in B\L (a set contained in a single house is a classical naked subset and is
 * skipped; cells of L&#x2229;B may participate), whose candidate union has
 * exactly n digits. A candidate is <i>restricted</i> if all set cells
 * containing it see each other (all in L or all in B), <i>non-restricted</i>
 * otherwise. The pattern is valid with exactly one non-restricted candidate Z
 * (the "0 non-restricted" case, a plain locked-set elimination, stays out of
 * scope).
 *
 * Justification: if no set cell held Z, the n cells would only hold the n-1
 * restricted candidates, and each restricted candidate can appear at most once
 * (its cells see each other) - n-1 values for n cells, contradiction. So Z is
 * in the set, and Z can be eliminated from every external cell that sees all
 * set cells containing Z.
 *
 * Only sizes with an entry in {@link #TYPE_BY_SIZE} are searched; milestone
 * 1.2 registered n = 4 ({@link SolutionType#BENT_QUAD}), milestone 1.3 added
 * n = 5..9 (VWXYZ- .. RSTUVWXYZ-Wing, one {@link SolutionType} per size). In
 * all-steps mode equal steps (type + cells + eliminations) found
 * through different bent regions are emitted only once.
 *
 * Structure mirrors {@link solver.WingSolver} (single-step and all-steps
 * mode); registered in {@link SudokuStepFinder}.
 */
public class BentSubsetSolver extends AbstractSolver {

	/** A line/box union has at most 15 cells. */
	private static final int MAX_REGION_CELLS = 15;
	/** Membership flag: cell belongs to the line. */
	private static final int IN_LINE = 1;
	/** Membership flag: cell belongs to the box. */
	private static final int IN_BOX = 2;
	/**
	 * The {@link SolutionType} emitted per subset size, null = size not
	 * searched. Milestone 1.3 registers sizes 5..9 here.
	 */
	private static final SolutionType[] TYPE_BY_SIZE = new SolutionType[MAX_REGION_CELLS + 1];

	static {
		TYPE_BY_SIZE[4] = SolutionType.BENT_QUAD;
		// milestone 1.3: sizes 5..9, one SolutionType per size
		TYPE_BY_SIZE[5] = SolutionType.VWXYZ_WING;
		TYPE_BY_SIZE[6] = SolutionType.UVWXYZ_WING;
		TYPE_BY_SIZE[7] = SolutionType.TUVWXYZ_WING;
		TYPE_BY_SIZE[8] = SolutionType.STUVWXYZ_WING;
		TYPE_BY_SIZE[9] = SolutionType.RSTUVWXYZ_WING;
	}

	/** One global step for eliminations (cloned when a subset is found). */
	private SolutionStep globalStep = new ModernStep();
	/** A list for all steps found in one search. */
	private List<SolutionStep> steps = new ArrayList<SolutionStep>();
	/** Dedup keys (type + cells + eliminations) of one all-steps search. */
	private Set<String> stepKeys = new HashSet<String>();
	/** A set for elimination checks. */
	private SudokuSet elimSet = new SudokuSet();
	/** The unsolved cells of the current bent region. */
	private int[] regionCells = new int[MAX_REGION_CELLS];
	/** The candidate mask of each region cell. */
	private short[] regionCands = new short[MAX_REGION_CELLS];
	/** Membership flags of each region cell ({@link #IN_LINE}/{@link #IN_BOX}). */
	private int[] regionFlags = new int[MAX_REGION_CELLS];
	/** Indices into {@link #regionCells} of the current subset. */
	private int[] subset = new int[MAX_REGION_CELLS];
	/** The cells of the current subset that contain Z. */
	private int[] zCells = new int[MAX_REGION_CELLS];
	/** The cells of the current subset, sorted for the step. */
	private int[] setCells = new int[MAX_REGION_CELLS];

	/**
	 * Creates a new instance of BentSubsetSolver
	 *
	 * @param finder
	 */
	public BentSubsetSolver(SudokuStepFinder finder) {
		super(finder);
	}

	@Override
	protected SolutionStep getStep(SolutionType type) {
		sudoku = finder.getSudoku();
		for (int size = 0; size < TYPE_BY_SIZE.length; size++) {
			if (TYPE_BY_SIZE[size] == type) {
				return findBentSubset(size, true);
			}
		}
		return null;
	}

	@Override
	protected boolean doStep(SolutionStep step) {
		boolean handled = false;
		sudoku = finder.getSudoku();
		for (int size = 0; size < TYPE_BY_SIZE.length; size++) {
			if (TYPE_BY_SIZE[size] == step.getType()) {
				for (Candidate cand : step.getCandidatesToDelete()) {
					sudoku.delCandidate(cand.getIndex(), cand.getValue());
				}
				handled = true;
				break;
			}
		}
		return handled;
	}

	/**
	 * Finds all bent subsets of all registered sizes in the current sudoku
	 * (all-steps mode).
	 *
	 * @return all steps found
	 */
	public List<SolutionStep> getAllBentSubsets() {
		sudoku = finder.getSudoku();
		List<SolutionStep> newSteps = new ArrayList<SolutionStep>();
		List<SolutionStep> oldSteps = steps;
		steps = newSteps;
		stepKeys.clear();
		for (int size = 0; size < TYPE_BY_SIZE.length; size++) {
			if (TYPE_BY_SIZE[size] != null) {
				findBentSubset(size, false);
			}
		}
		steps = oldSteps;
		return newSteps;
	}

	/**
	 * Iterates all 54 bent regions searching for subsets of one size.
	 *
	 * @param size the subset size (a registered index of {@link #TYPE_BY_SIZE})
	 * @param onlyOne return the first step found instead of collecting all
	 * @return the first step found if <code>onlyOne</code> is set, else null
	 */
	private SolutionStep findBentSubset(int size, boolean onlyOne) {
		// ALL_UNITS: 0-8 rows, 9-17 columns, 18-26 blocks
		for (int line = 0; line < 18; line++) {
			for (int box = 18; box < 27; box++) {
				if (!intersects(line, box)) {
					continue;
				}
				SolutionStep step = searchRegion(line, box, size, onlyOne);
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
	 * Collects the unsolved cells of one bent region and checks all its
	 * subsets of the given size (lexicographic order, the same order the fixed
	 * nested loops of milestone 1.1 produced for n = 4).
	 */
	private SolutionStep searchRegion(int line, int box, int size, boolean onlyOne) {
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
		return enumerateSubsets(0, 0, count, size, onlyOne);
	}

	/**
	 * Recursively enumerates all <code>size</code>-subsets of the current
	 * region in lexicographic order (<code>depth</code> cells already chosen
	 * in {@link #subset}, indices <code>next..count-1</code> still available).
	 */
	private SolutionStep enumerateSubsets(int depth, int next, int count, int size, boolean onlyOne) {
		if (depth == size) {
			return checkSubset(size, onlyOne);
		}
		for (int i = next; i <= count - (size - depth); i++) {
			subset[depth] = i;
			SolutionStep step = enumerateSubsets(depth + 1, i + 1, count, size, onlyOne);
			if (onlyOne && step != null) {
				return step;
			}
		}
		return null;
	}

	/**
	 * Checks the current subset (indices into {@link #regionCells} in
	 * {@link #subset}) for a bent naked subset and creates the step if valid.
	 */
	private SolutionStep checkSubset(int size, boolean onlyOne) {
		short union = 0;
		for (int i = 0; i < size; i++) {
			union |= regionCands[subset[i]];
		}
		if (Sudoku2.ANZ_VALUES[union] != size) {
			return null;
		}
		// really bent? otherwise it is a classical naked subset
		boolean lineOnly = false;
		boolean boxOnly = false;
		for (int i = 0; i < size; i++) {
			int flags = regionFlags[subset[i]];
			if (flags == IN_LINE) {
				lineOnly = true;
			} else if (flags == IN_BOX) {
				boxOnly = true;
			}
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
			for (int j = 0; j < size; j++) {
				if ((regionCands[subset[j]] & mask) != 0) {
					flagsAnd &= regionFlags[subset[j]];
				}
			}
			if (flagsAnd == 0) {
				// non-restricted
				if (zCand != -1) {
					// second one -> no bent subset
					return null;
				}
				zCand = cands[i];
			}
		}
		if (zCand == -1) {
			// all restricted: locked set case, out of scope
			return null;
		}
		// Z can be eliminated from all external cells that see every set cell
		// containing Z (a non-restricted candidate is in at least two cells)
		short zMask = Sudoku2.MASKS[zCand];
		int zAnz = 0;
		for (int i = 0; i < size; i++) {
			if ((regionCands[subset[i]] & zMask) != 0) {
				zCells[zAnz++] = regionCells[subset[i]];
			}
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
		// ok, bent subset found!
		globalStep.reset();
		globalStep.setType(TYPE_BY_SIZE[size]);
		// values: the restricted candidates ascending, Z last
		for (int i = 0; i < cands.length; i++) {
			if (cands[i] != zCand) {
				globalStep.addValue(cands[i]);
			}
		}
		globalStep.addValue(zCand);
		for (int i = 0; i < size; i++) {
			setCells[i] = regionCells[subset[i]];
		}
		Arrays.sort(setCells, 0, size);
		for (int i = 0; i < size; i++) {
			globalStep.addIndex(setCells[i]);
		}
		// fins: the Z candidates inside the set (mirrors XYZ-Wing)
		for (int i = 0; i < size; i++) {
			if (sudoku.isCandidate(setCells[i], zCand)) {
				globalStep.addFin(setCells[i], zCand);
			}
		}
		for (int i = 0; i < elimSet.size(); i++) {
			globalStep.addCandidateToDelete(elimSet.get(i), zCand);
		}
		if (onlyOne) {
			return (SolutionStep) globalStep.clone();
		}
		// all-steps mode: the same subset can be reachable through more than
		// one bent region - emit equal steps (type + cells + eliminations)
		// only once
		if (stepKeys.add(stepKey(globalStep))) {
			steps.add((SolutionStep) globalStep.clone());
		}
		return null;
	}

	/**
	 * Equality key of a step: type + cells + eliminations (the milestone 1.2
	 * dedup contract). Cells and eliminations are added in ascending order by
	 * construction, so plain concatenation is canonical.
	 */
	private static String stepKey(SolutionStep step) {
		StringBuilder key = new StringBuilder();
		key.append(step.getType().ordinal());
		key.append('|').append(step.getIndices());
		key.append('|');
		for (Candidate cand : step.getCandidatesToDelete()) {
			key.append(cand.getIndex()).append(':').append(cand.getValue()).append(',');
		}
		return key.toString();
	}
}
