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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import solver.AbstractSolver;
import solver.SudokuStepFinder;
import sudoku.Candidate;
import sudoku.Chain;
import sudoku.SolutionStep;
import sudoku.SolutionType;
import sudoku.Sudoku2;
import sudoku.SudokuSet;

/**
 * The oddagon engine (milestone 1.6): odd-length cell cycles that would be an
 * impossible pattern, saved only by their {@link Guardians}.
 *
 * A cycle is a sequence of cells x1..xL, L odd &isin; {5, 7}, where
 * consecutive cells (including xL..x1) share a house, every link is assigned
 * a DISTINCT house, and neither cells nor houses repeat. Additionally each
 * link house may contain no cycle cell besides its two endpoints — for the
 * Broken Wing this is a hard soundness requirement (a third cycle cell inside
 * a link house would break the conjugate-link argument); for the Bivalue
 * Oddagon the restriction is conservative but keeps both techniques on the
 * canonical pattern definition. Lengths 9+ are out of scope of this v1
 * (enumeration cost and no fixture coverage); L=3 is excluded because it
 * collapses into basic techniques (a 3-cycle needs two cells in one house
 * twice over, i.e. locked-candidate territory).
 *
 * The two techniques:
 * <ul>
 * <li><b>Broken Wing</b> ({@link SolutionType#BROKEN_WING}, one digit d): all
 * cycle cells hold candidate d. If no guardian (= other d candidates of the
 * link houses, outside the cycle) were true, every link house would hold d in
 * exactly its two cycle cells — conjugate links; an odd cycle of conjugate
 * links forces a cell to be true and false at once. Impossible, so some
 * guardian is true.</li>
 * <li><b>Bivalue Oddagon</b> ({@link SolutionType#BIVALUE_ODDAGON}, a digit
 * pair {a,b}): all cycle cells hold a and b. Without guardians (= the
 * candidates &ne; a,b inside the cycle cells) every cell would be exactly
 * {a,b} and consecutive cells would have to alternate — an odd cycle has no
 * 2-coloring. Impossible, so some guardian is true.</li>
 * </ul>
 *
 * Deductions are the standard guardian ladder documented in
 * {@link Guardians}; |G|=1 places d in the guardian cell (Broken Wing) or
 * removes a,b from the guardian cell (Bivalue Oddagon); |G|&gt;1 with mixed
 * digits (Bivalue Oddagon only) yields no direct deduction in 1.6 — that is
 * chain territory.
 *
 * Step data layout (kept strictly in {@link SolutionStep} base fields, see
 * {@link ModernStep}):
 * <ul>
 * <li>{@code values}: [d] (Broken Wing) or [a, b] ascending (Bivalue
 * Oddagon);</li>
 * <li>{@code indices}: the cycle cells in cycle order (NOT sorted — the
 * order is the loop);</li>
 * <li>{@code fins}: the guardian candidates;</li>
 * <li>{@code candidatesToDelete}: the eliminations; empty exactly for the
 * Broken Wing placement case (|G|=1), where the placement target is
 * {@code fins.get(0)};</li>
 * <li>one display {@link Chain} with the cycle cells (first cell repeated at
 * the end) so the GUI draws the closed loop without SudokuPanel
 * changes.</li>
 * </ul>
 *
 * Structure mirrors {@link BentSubsetSolver} (single-step and all-steps mode
 * with type+cells+eliminations dedup); registered in {@link SudokuStepFinder}.
 */
public class OddagonSolver extends AbstractSolver {

	/** The searched cycle lengths (odd; 9+ out of scope v1, 3 degenerate). */
	private static final int[] CYCLE_LENGTHS = { 5, 7 };
	/** The longest searched cycle. */
	private static final int MAX_LENGTH = 7;

	private static final Logger LOGGER = Logger.getLogger(OddagonSolver.class.getName());

	/** One global step for creation (cloned when a pattern is found). */
	private SolutionStep globalStep = new ModernStep();
	/** A list for all steps found in one search. */
	private List<SolutionStep> steps = new ArrayList<SolutionStep>();
	/** Dedup keys (type + cells + eliminations) of one all-steps search. */
	private Set<String> stepKeys = new HashSet<String>();
	/** The guardians of the current cycle. */
	private Guardians guardians = new Guardians();
	/** The cells of the current cycle (path) in cycle order. */
	private int[] cycleCells = new int[MAX_LENGTH];
	/** The link houses: linkHouses[i] joins cycleCells[i] and cycleCells[i+1] (mod L). */
	private int[] linkHouses = new int[MAX_LENGTH];
	/** Fast cycle membership per cell. */
	private boolean[] inCycle = new boolean[Sudoku2.LENGTH];
	/** Bitmask over the 27 constraints: houses already used as links. */
	private int usedHouses = 0;
	/** A set for elimination checks. */
	private SudokuSet elimSet = new SudokuSet();
	/** The cells of the current cycle as a set (for exclusions). */
	private SudokuSet cycleSet = new SudokuSet();

	// parameters of the current search
	/** The type being searched. */
	private SolutionType searchType;
	/** Broken Wing: the digit d; -1 for Bivalue Oddagon. */
	private int searchDigit;
	/** Bivalue Oddagon: the pair (a < b); unused for Broken Wing. */
	private int digitA, digitB;
	/** The candidate mask every cycle cell must contain. */
	private short cellMask;
	/** Single-step mode: stop at the first step found. */
	private boolean onlyOne;
	/** The first step found in single-step mode. */
	private SolutionStep result;

	/**
	 * Creates a new instance of OddagonSolver
	 *
	 * @param finder
	 */
	public OddagonSolver(SudokuStepFinder finder) {
		super(finder);
	}

	@Override
	protected SolutionStep getStep(SolutionType type) {
		sudoku = finder.getSudoku();
		switch (type) {
		case BROKEN_WING:
			return findBrokenWing(true);
		case BIVALUE_ODDAGON:
			return findBivalueOddagon(true);
		default:
			return null;
		}
	}

	@Override
	protected boolean doStep(SolutionStep step) {
		sudoku = finder.getSudoku();
		switch (step.getType()) {
		case BROKEN_WING:
			if (step.getCandidatesToDelete().isEmpty()) {
				// |G|=1: the single guardian (kept in fins) is true
				Candidate guardian = step.getFins().get(0);
				sudoku.setCell(guardian.getIndex(), guardian.getValue());
			} else {
				for (Candidate cand : step.getCandidatesToDelete()) {
					sudoku.delCandidate(cand.getIndex(), cand.getValue());
				}
			}
			return true;
		case BIVALUE_ODDAGON:
			for (Candidate cand : step.getCandidatesToDelete()) {
				sudoku.delCandidate(cand.getIndex(), cand.getValue());
			}
			return true;
		default:
			return false;
		}
	}

	/**
	 * Finds all Broken Wings and Bivalue Oddagons in the current sudoku
	 * (all-steps mode).
	 *
	 * @return all steps found
	 */
	public List<SolutionStep> getAllOddagons() {
		sudoku = finder.getSudoku();
		List<SolutionStep> newSteps = new ArrayList<SolutionStep>();
		List<SolutionStep> oldSteps = steps;
		steps = newSteps;
		stepKeys.clear();
		findBrokenWing(false);
		findBivalueOddagon(false);
		steps = oldSteps;
		return newSteps;
	}

	/**
	 * Searches Broken Wings for every digit.
	 *
	 * @param onlyOne return the first step found instead of collecting all
	 * @return the first step found if <code>onlyOne</code> is set, else null
	 */
	private SolutionStep findBrokenWing(boolean onlyOne) {
		searchType = SolutionType.BROKEN_WING;
		this.onlyOne = onlyOne;
		result = null;
		for (int digit = 1; digit <= 9; digit++) {
			searchDigit = digit;
			cellMask = Sudoku2.MASKS[digit];
			enumerateCycles();
			if (onlyOne && result != null) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Searches Bivalue Oddagons for every digit pair.
	 *
	 * @param onlyOne return the first step found instead of collecting all
	 * @return the first step found if <code>onlyOne</code> is set, else null
	 */
	private SolutionStep findBivalueOddagon(boolean onlyOne) {
		searchType = SolutionType.BIVALUE_ODDAGON;
		this.onlyOne = onlyOne;
		result = null;
		searchDigit = -1;
		for (int a = 1; a <= 8; a++) {
			for (int b = a + 1; b <= 9; b++) {
				digitA = a;
				digitB = b;
				cellMask = (short) (Sudoku2.MASKS[a] | Sudoku2.MASKS[b]);
				enumerateCycles();
				if (onlyOne && result != null) {
					return result;
				}
			}
		}
		return null;
	}

	/**
	 * Is the cell part of the current cell pool (unsolved and containing the
	 * required candidates)?
	 */
	private boolean isEligible(int cell) {
		return sudoku.getValue(cell) == 0 && (sudoku.getCell(cell) & cellMask) == cellMask;
	}

	/**
	 * Enumerates every cycle of the searched lengths over the eligible cells.
	 * Canonical form: the start cell is the smallest cell of the cycle (all
	 * other cells must be larger) and the second cell is smaller than the
	 * last (kills the reverse traversal); the house assignment is part of the
	 * cycle identity — the same cell ring linked through different houses has
	 * different guardians and is enumerated separately.
	 */
	private void enumerateCycles() {
		for (int li = 0; li < CYCLE_LENGTHS.length; li++) {
			int length = CYCLE_LENGTHS[li];
			for (int start = 0; start < Sudoku2.LENGTH; start++) {
				if (!isEligible(start)) {
					continue;
				}
				cycleCells[0] = start;
				inCycle[start] = true;
				extendCycle(1, length);
				inCycle[start] = false;
				if (onlyOne && result != null) {
					return;
				}
			}
		}
	}

	/**
	 * Depth-first extension of the current path (<code>depth</code> cells
	 * chosen). The next cell must be eligible, larger than the start cell,
	 * not yet in the path, and share an unused house with the last cell; at
	 * full length the path must close back to the start through one more
	 * unused house.
	 */
	private void extendCycle(int depth, int length) {
		int last = cycleCells[depth - 1];
		int[] lastHouses = Sudoku2.CONSTRAINTS[last];
		if (depth == length) {
			// close the cycle: last -> start through an unused shared house
			int start = cycleCells[0];
			if (cycleCells[1] > cycleCells[length - 1]) {
				// reverse traversal of an already enumerated cycle
				return;
			}
			int[] startHouses = Sudoku2.CONSTRAINTS[start];
			for (int i = 0; i < lastHouses.length; i++) {
				int house = lastHouses[i];
				if ((usedHouses & (1 << house)) != 0) {
					continue;
				}
				boolean shared = false;
				for (int j = 0; j < startHouses.length; j++) {
					if (startHouses[j] == house) {
						shared = true;
						break;
					}
				}
				if (!shared) {
					continue;
				}
				linkHouses[length - 1] = house;
				usedHouses |= 1 << house;
				checkCycle(length);
				usedHouses &= ~(1 << house);
				if (onlyOne && result != null) {
					return;
				}
			}
			return;
		}
		for (int i = 0; i < lastHouses.length; i++) {
			int house = lastHouses[i];
			if ((usedHouses & (1 << house)) != 0) {
				continue;
			}
			int[] houseCells = Sudoku2.ALL_UNITS[house];
			for (int j = 0; j < houseCells.length; j++) {
				int next = houseCells[j];
				if (next <= cycleCells[0] || inCycle[next] || !isEligible(next)) {
					continue;
				}
				linkHouses[depth - 1] = house;
				usedHouses |= 1 << house;
				cycleCells[depth] = next;
				inCycle[next] = true;
				extendCycle(depth + 1, length);
				inCycle[next] = false;
				usedHouses &= ~(1 << house);
				if (onlyOne && result != null) {
					return;
				}
			}
		}
	}

	/**
	 * Validates the closed cycle (link-house purity), collects its guardians
	 * and applies the guardian deductions.
	 */
	private void checkCycle(int length) {
		// every link house must contain exactly its two endpoints from the
		// cycle (see class javadoc: soundness for BW, canonical form for BO)
		for (int i = 0; i < length; i++) {
			int[] houseCells = Sudoku2.ALL_UNITS[linkHouses[i]];
			int count = 0;
			for (int j = 0; j < houseCells.length; j++) {
				if (inCycle[houseCells[j]]) {
					count++;
				}
			}
			if (count != 2) {
				return;
			}
		}
		// collect the guardians
		guardians.clear();
		if (searchType == SolutionType.BROKEN_WING) {
			// the other candidates d of the link houses, outside the cycle
			for (int i = 0; i < length; i++) {
				int[] houseCells = Sudoku2.ALL_UNITS[linkHouses[i]];
				for (int j = 0; j < houseCells.length; j++) {
					int cell = houseCells[j];
					if (!inCycle[cell] && !guardians.isGuardianCell(cell) && sudoku.getValue(cell) == 0
							&& sudoku.isCandidate(cell, searchDigit)) {
						guardians.add(cell, searchDigit);
					}
				}
			}
		} else {
			// the candidates != a,b inside the cycle cells
			for (int i = 0; i < length; i++) {
				int cell = cycleCells[i];
				int[] cands = Sudoku2.POSSIBLE_VALUES[sudoku.getCell(cell)];
				for (int j = 0; j < cands.length; j++) {
					if (cands[j] != digitA && cands[j] != digitB) {
						guardians.add(cell, cands[j]);
					}
				}
			}
		}
		if (guardians.size() == 0) {
			// a guardianless impossible pattern cannot occur in a valid
			// puzzle: the state is already contradictory. Never "eliminate
			// everything" — log and skip.
			LOGGER.log(Level.WARNING, "{0} of length {1} without guardians: contradictory state, pattern skipped",
					new Object[] { searchType, length });
			return;
		}
		if (guardians.size() == 1) {
			createSingleGuardianStep(length);
			return;
		}
		int digit = guardians.uniformDigit();
		if (digit == -1) {
			// mixed guardian digits (Bivalue Oddagon only): no direct
			// deduction in 1.6, chain territory
			return;
		}
		// some guardian of the uniform digit is true: eliminate the digit
		// from every external cell that sees ALL guardian cells
		guardians.collectBuddies(elimSet);
		elimSet.and(finder.getCandidates()[digit]);
		elimSet.andNot(guardians.getCellSet());
		cycleSet.clear();
		for (int i = 0; i < length; i++) {
			cycleSet.add(cycleCells[i]);
		}
		elimSet.andNot(cycleSet);
		if (elimSet.isEmpty()) {
			return;
		}
		initStep(length);
		for (int i = 0; i < elimSet.size(); i++) {
			globalStep.addCandidateToDelete(elimSet.get(i), digit);
		}
		emitStep();
	}

	/**
	 * Creates the |G|=1 step: the single guardian is true. Broken Wing:
	 * placement of d in the guardian cell (no candidatesToDelete, the target
	 * is fins[0]). Bivalue Oddagon: a and b are eliminated from the guardian
	 * cell (if a single candidate remains that is placement-equivalent, but
	 * the step stays an elimination step).
	 */
	private void createSingleGuardianStep(int length) {
		initStep(length);
		if (searchType == SolutionType.BIVALUE_ODDAGON) {
			int cell = guardians.getCell(0);
			globalStep.addCandidateToDelete(cell, digitA);
			globalStep.addCandidateToDelete(cell, digitB);
		}
		// Broken Wing: placement step, nothing to delete
		emitStep();
	}

	/**
	 * Fills {@link #globalStep} with the pattern data (type, values, cycle
	 * cells in cycle order, guardians as fins, display chain).
	 */
	private void initStep(int length) {
		globalStep.reset();
		globalStep.setType(searchType);
		int displayDigit;
		if (searchType == SolutionType.BROKEN_WING) {
			globalStep.addValue(searchDigit);
			displayDigit = searchDigit;
		} else {
			globalStep.addValue(digitA);
			globalStep.addValue(digitB);
			displayDigit = digitA;
		}
		for (int i = 0; i < length; i++) {
			globalStep.addIndex(cycleCells[i]);
		}
		for (int i = 0; i < guardians.size(); i++) {
			globalStep.addFin(guardians.getCell(i), guardians.getDigit(i));
		}
		// display chain: the closed loop (first cell repeated), all links
		// drawn strong; lets SudokuPanel paint the cycle without changes
		int[] chain = new int[length + 1];
		for (int i = 0; i < length; i++) {
			chain[i] = Chain.makeSEntry(cycleCells[i], displayDigit, true);
		}
		chain[length] = chain[0];
		globalStep.addChain(0, length, chain);
	}

	/**
	 * Emits {@link #globalStep}: in single-step mode as {@link #result}, in
	 * all-steps mode into {@link #steps} deduplicated by
	 * type + cells + eliminations (the same cell ring through different house
	 * assignments or start digits may yield the same deduction).
	 */
	private void emitStep() {
		if (onlyOne) {
			result = (SolutionStep) globalStep.clone();
			return;
		}
		if (stepKeys.add(stepKey(globalStep))) {
			steps.add((SolutionStep) globalStep.clone());
		}
	}

	/**
	 * Equality key of a step: type + cells (order-insensitive: the same ring
	 * can be entered at different cells) + placements/eliminations.
	 */
	private static String stepKey(SolutionStep step) {
		StringBuilder key = new StringBuilder();
		key.append(step.getType().ordinal());
		List<Integer> cells = new ArrayList<Integer>(step.getIndices());
		java.util.Collections.sort(cells);
		key.append('|').append(cells);
		key.append('|');
		if (step.getCandidatesToDelete().isEmpty()) {
			// Broken Wing placement: the guardian is the deduction
			Candidate guardian = step.getFins().get(0);
			key.append(guardian.getIndex()).append('=').append(guardian.getValue());
		} else {
			for (Candidate cand : step.getCandidatesToDelete()) {
				key.append(cand.getIndex()).append(':').append(cand.getValue()).append(',');
			}
		}
		return key.toString();
	}
}
