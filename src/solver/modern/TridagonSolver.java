/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.7).
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
 * The Tridagon (Trivalue Oddagon, "Thor's Hammer"; milestone 1.7): an
 * impossible pattern of a digit triple {a,b,c} over four boxes, saved only by
 * its {@link Guardians}. Spec: docs/specs/tridagon.md (mith's "Chromatic
 * Patterns" t39885, Berthier's "The tridagon rule" t39859).
 *
 * The pattern (all three conditions mandatory):
 * <ol>
 * <li>four boxes in two bands &times; two stacks (9 box rectangles);</li>
 * <li>in each box a <i>transversal</i>: 3 cells covering the 3 rows and the 3
 * columns of the box — a local permutation rows&rarr;columns (6 per box);</li>
 * <li>odd parity: the product of the signs of the four local permutations is
 * &minus;1. With even parity the pattern is colorable and there is NO
 * deduction.</li>
 * </ol>
 * Cell eligibility (v1): unsolved and containing ALL THREE digits of the
 * triple; guardians are the candidates &notin; {a,b,c} inside the 12 pattern
 * cells. Variants with 2-of-3 cells are out of scope of this v1 (they tend to
 * collapse into simpler logic; documented extension).
 *
 * Impossibility: without a true guardian each box confines the triple to a
 * bijection rows&rarr;digits; the row links (same band) and column links (same
 * stack) around the box rectangle compose the four bijections into a
 * permutation that cannot close consistently when the parity is odd. Some
 * guardian must be true. Equivalently: the 12 cells, 2-regular under
 * row/column links, decompose into exactly one 4-cell rectangle plus one
 * 8-cell loop iff the monodromy permutation is a transposition iff the sign
 * product is &minus;1 (even parity yields three 4-cycles or one 12-cycle
 * instead) — the detector computes that decomposition for the display.
 *
 * Deductions are the standard guardian ladder of {@link Guardians}: |G|=1
 * removes the triple digits from the guardian cell (mith: "the three digits
 * are eliminated from that cell"); several guardians of one digit g eliminate
 * g from every external cell seeing all guardian cells; mixed digits give no
 * direct deduction in 1.7 — that is "tridagon links" / ORk territory (phase 4
 * chains); a guardianless pattern is a contradictory state (log and skip).
 *
 * Step data layout (kept strictly in {@link SolutionStep} base fields, see
 * {@link ModernStep}): {@code values} = the triple ascending; {@code indices}
 * = the 8 loop cells in loop order, then the 4 rectangle cells;
 * {@code fins} = the guardian candidates (digits &notin; triple), then the
 * rectangle cells with the triple digits (display); one display {@link Chain}
 * traces the closed 8-loop so the GUI draws it without SudokuPanel changes.
 *
 * Enumeration (bounded, spec &sect;4): 84 triples &times; per box the
 * eligible transversals &times; 9 box rectangles &times; the (typically tiny)
 * cartesian product of transversals; triples without 4 populated boxes are
 * pruned before the product. Dedup by type + cells + deduction, mirroring
 * {@link OddagonSolver}.
 */
public class TridagonSolver extends AbstractSolver {

	/** The 6 permutations of {0,1,2}: local row r maps to local column PERMS[p][r]. */
	private static final int[][] PERMS = { { 0, 1, 2 }, { 0, 2, 1 }, { 1, 0, 2 }, { 1, 2, 0 }, { 2, 0, 1 },
			{ 2, 1, 0 } };
	/** The sign of each permutation in {@link #PERMS}. */
	private static final int[] SIGNS = { 1, -1, -1, 1, 1, -1 };
	/** INVERSE[p][c] = the local row that permutation p maps to column c. */
	private static final int[][] INVERSE = new int[6][3];
	static {
		for (int p = 0; p < 6; p++) {
			for (int r = 0; r < 3; r++) {
				INVERSE[p][PERMS[p][r]] = r;
			}
		}
	}

	private static final Logger LOGGER = Logger.getLogger(TridagonSolver.class.getName());

	/** One global step for creation (cloned when a pattern is found). */
	private SolutionStep globalStep = new ModernStep();
	/** A list for all steps found in one search. */
	private List<SolutionStep> steps = new ArrayList<SolutionStep>();
	/** Dedup keys (type + cells + eliminations) of one all-steps search. */
	private Set<String> stepKeys = new HashSet<String>();
	/** The guardians of the current pattern. */
	private Guardians guardians = new Guardians();
	/** Per box, the eligible transversals of the current triple (indices into PERMS). */
	private int[][] transversals = new int[9][6];
	/** Per box, the number of eligible transversals. */
	private int[] transversalCount = new int[9];
	/** The 12 cells of the current pattern. */
	private int[] patternCells = new int[12];
	/** The 8-cell loop of the current pattern, in loop order. */
	private int[] loopCells = new int[8];
	/** The 4-cell rectangle of the current pattern. */
	private int[] rectCells = new int[4];
	/** The pattern cells as a set (for elimination exclusion). */
	private SudokuSet patternSet = new SudokuSet();
	/** A set for elimination checks. */
	private SudokuSet elimSet = new SudokuSet();

	// parameters of the current search
	/** The triple (a < b < c). */
	private int digitA, digitB, digitC;
	/** The candidate mask every pattern cell must contain. */
	private short cellMask;
	/** Single-step mode: stop at the first step found. */
	private boolean onlyOne;
	/** The first step found in single-step mode. */
	private SolutionStep result;

	/**
	 * Creates a new instance of TridagonSolver
	 *
	 * @param finder
	 */
	public TridagonSolver(SudokuStepFinder finder) {
		super(finder);
	}

	@Override
	protected SolutionStep getStep(SolutionType type) {
		sudoku = finder.getSudoku();
		switch (type) {
		case TRIDAGON:
			return findTridagon(true);
		default:
			return null;
		}
	}

	@Override
	protected boolean doStep(SolutionStep step) {
		sudoku = finder.getSudoku();
		switch (step.getType()) {
		case TRIDAGON:
			for (Candidate cand : step.getCandidatesToDelete()) {
				sudoku.delCandidate(cand.getIndex(), cand.getValue());
			}
			return true;
		default:
			return false;
		}
	}

	/**
	 * Finds all Tridagons in the current sudoku (all-steps mode).
	 *
	 * @return all steps found
	 */
	public List<SolutionStep> getAllTridagons() {
		sudoku = finder.getSudoku();
		List<SolutionStep> newSteps = new ArrayList<SolutionStep>();
		List<SolutionStep> oldSteps = steps;
		steps = newSteps;
		stepKeys.clear();
		findTridagon(false);
		steps = oldSteps;
		return newSteps;
	}

	/**
	 * Searches Tridagons for every triple {a,b,c} (84 = C(9,3)).
	 *
	 * @param onlyOne return the first step found instead of collecting all
	 * @return the first step found if <code>onlyOne</code> is set, else null
	 */
	private SolutionStep findTridagon(boolean onlyOne) {
		this.onlyOne = onlyOne;
		result = null;
		for (int a = 1; a <= 7; a++) {
			for (int b = a + 1; b <= 8; b++) {
				for (int c = b + 1; c <= 9; c++) {
					digitA = a;
					digitB = b;
					digitC = c;
					cellMask = (short) (Sudoku2.MASKS[a] | Sudoku2.MASKS[b] | Sudoku2.MASKS[c]);
					searchTriple();
					if (onlyOne && result != null) {
						return result;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Is the cell part of the current cell pool (unsolved and containing all
	 * three digits of the triple)?
	 */
	private boolean isEligible(int cell) {
		return sudoku.getValue(cell) == 0 && (sudoku.getCell(cell) & cellMask) == cellMask;
	}

	/**
	 * Enumerates the box rectangles of the current triple: computes the
	 * eligible transversals per box, prunes triples without four populated
	 * boxes, and walks the (band pair) &times; (stack pair) rectangles with
	 * the cartesian product of their transversals.
	 */
	private void searchTriple() {
		int populated = 0;
		for (int box = 0; box < 9; box++) {
			transversalCount[box] = 0;
			for (int p = 0; p < 6; p++) {
				if (isEligible(cellAt(box, 0, PERMS[p][0])) && isEligible(cellAt(box, 1, PERMS[p][1]))
						&& isEligible(cellAt(box, 2, PERMS[p][2]))) {
					transversals[box][transversalCount[box]++] = p;
				}
			}
			if (transversalCount[box] > 0) {
				populated++;
			}
		}
		if (populated < 4) {
			return;
		}
		for (int band1 = 0; band1 < 2; band1++) {
			for (int band2 = band1 + 1; band2 < 3; band2++) {
				for (int stack1 = 0; stack1 < 2; stack1++) {
					for (int stack2 = stack1 + 1; stack2 < 3; stack2++) {
						int tl = band1 * 3 + stack1;
						int tr = band1 * 3 + stack2;
						int bl = band2 * 3 + stack1;
						int br = band2 * 3 + stack2;
						if (transversalCount[tl] == 0 || transversalCount[tr] == 0 || transversalCount[bl] == 0
								|| transversalCount[br] == 0) {
							continue;
						}
						for (int i = 0; i < transversalCount[tl]; i++) {
							for (int j = 0; j < transversalCount[tr]; j++) {
								for (int k = 0; k < transversalCount[bl]; k++) {
									for (int l = 0; l < transversalCount[br]; l++) {
										int pTl = transversals[tl][i];
										int pTr = transversals[tr][j];
										int pBl = transversals[bl][k];
										int pBr = transversals[br][l];
										if (SIGNS[pTl] * SIGNS[pTr] * SIGNS[pBl] * SIGNS[pBr] != -1) {
											// even parity: colorable, no deduction
											continue;
										}
										checkPattern(tl, tr, bl, br, pTl, pTr, pBl, pBr);
										if (onlyOne && result != null) {
											return;
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/** The cell of <code>box</code> at local row/column coordinates. */
	private static int cellAt(int box, int localRow, int localCol) {
		return Sudoku2.BLOCKS[box][localRow * 3 + localCol];
	}

	/**
	 * Materializes one odd-parity pattern: the 12 cells, the loop/rectangle
	 * decomposition via the monodromy permutation, the guardians, and the
	 * guardian-ladder deductions.
	 */
	private void checkPattern(int tl, int tr, int bl, int br, int pTl, int pTr, int pBl, int pBr) {
		int[] boxes = { tl, tr, bl, br };
		int[] perms = { pTl, pTr, pBl, pBr };
		int n = 0;
		patternSet.clear();
		for (int i = 0; i < 4; i++) {
			for (int r = 0; r < 3; r++) {
				int cell = cellAt(boxes[i], r, PERMS[perms[i]][r]);
				patternCells[n++] = cell;
				patternSet.add(cell);
			}
		}
		// monodromy on the local rows of the top-left box: row link to TR,
		// column link down to BR, row link to BL, column link back to TL
		int[] pi = new int[3];
		for (int r = 0; r < 3; r++) {
			pi[r] = INVERSE[pTl][PERMS[pBl][INVERSE[pBr][PERMS[pTr][r]]]];
		}
		// odd parity <=> pi is a transposition: fixed row = the rectangle,
		// swapped rows = the 8-loop
		int fixed = -1;
		for (int r = 0; r < 3; r++) {
			if (pi[r] == r) {
				fixed = r;
			}
		}
		int r1 = fixed == 0 ? 1 : 0;
		int r2 = pi[r1];
		int fp = INVERSE[pBr][PERMS[pTr][fixed]];
		rectCells[0] = cellAt(tl, fixed, PERMS[pTl][fixed]);
		rectCells[1] = cellAt(tr, fixed, PERMS[pTr][fixed]);
		rectCells[2] = cellAt(br, fp, PERMS[pBr][fp]);
		rectCells[3] = cellAt(bl, fp, PERMS[pBl][fp]);
		int s = INVERSE[pBr][PERMS[pTr][r1]];
		int t = INVERSE[pBr][PERMS[pTr][r2]];
		loopCells[0] = cellAt(tl, r1, PERMS[pTl][r1]);
		loopCells[1] = cellAt(tr, r1, PERMS[pTr][r1]);
		loopCells[2] = cellAt(br, s, PERMS[pBr][s]);
		loopCells[3] = cellAt(bl, s, PERMS[pBl][s]);
		loopCells[4] = cellAt(tl, r2, PERMS[pTl][r2]);
		loopCells[5] = cellAt(tr, r2, PERMS[pTr][r2]);
		loopCells[6] = cellAt(br, t, PERMS[pBr][t]);
		loopCells[7] = cellAt(bl, t, PERMS[pBl][t]);
		// collect the guardians: candidates outside the triple in the 12 cells
		guardians.clear();
		for (int i = 0; i < 12; i++) {
			int cell = patternCells[i];
			int[] cands = Sudoku2.POSSIBLE_VALUES[sudoku.getCell(cell)];
			for (int j = 0; j < cands.length; j++) {
				if (cands[j] != digitA && cands[j] != digitB && cands[j] != digitC) {
					guardians.add(cell, cands[j]);
				}
			}
		}
		if (guardians.size() == 0) {
			// a guardianless impossible pattern cannot occur in a valid
			// puzzle: the state is already contradictory. Never "eliminate
			// everything" — log and skip (contract of milestone 1.6).
			LOGGER.log(Level.WARNING, "Tridagon {0}/{1}/{2} without guardians: contradictory state, pattern skipped",
					new Object[] { digitA, digitB, digitC });
			return;
		}
		if (guardians.size() == 1) {
			// the single guardian is TRUE: the triple digits are eliminated
			// from its cell (all three are present by eligibility)
			int cell = guardians.getCell(0);
			initStep();
			globalStep.addCandidateToDelete(cell, digitA);
			globalStep.addCandidateToDelete(cell, digitB);
			globalStep.addCandidateToDelete(cell, digitC);
			emitStep();
			return;
		}
		int digit = guardians.uniformDigit();
		if (digit == -1) {
			// mixed guardian digits: no direct deduction in 1.7 — that is
			// tridagon-links / ORk territory (phase 4 chains)
			return;
		}
		// some guardian of the uniform digit is true: eliminate the digit
		// from every external cell that sees ALL guardian cells
		guardians.collectBuddies(elimSet);
		elimSet.and(finder.getCandidates()[digit]);
		elimSet.andNot(guardians.getCellSet());
		elimSet.andNot(patternSet);
		if (elimSet.isEmpty()) {
			return;
		}
		initStep();
		for (int i = 0; i < elimSet.size(); i++) {
			globalStep.addCandidateToDelete(elimSet.get(i), digit);
		}
		emitStep();
	}

	/**
	 * Fills {@link #globalStep} with the pattern data (type, triple, loop +
	 * rectangle cells, guardians and rectangle as fins, display chain of the
	 * 8-loop).
	 */
	private void initStep() {
		globalStep.reset();
		globalStep.setType(SolutionType.TRIDAGON);
		globalStep.addValue(digitA);
		globalStep.addValue(digitB);
		globalStep.addValue(digitC);
		for (int i = 0; i < 8; i++) {
			globalStep.addIndex(loopCells[i]);
		}
		for (int i = 0; i < 4; i++) {
			globalStep.addIndex(rectCells[i]);
		}
		// fins: the guardians first (digits outside the triple), then the
		// rectangle cells with the triple digits so the GUI highlights them
		for (int i = 0; i < guardians.size(); i++) {
			globalStep.addFin(guardians.getCell(i), guardians.getDigit(i));
		}
		for (int i = 0; i < 4; i++) {
			globalStep.addFin(rectCells[i], digitA);
			globalStep.addFin(rectCells[i], digitB);
			globalStep.addFin(rectCells[i], digitC);
		}
		// display chain: the closed 8-loop (first cell repeated), all links
		// drawn strong; lets SudokuPanel paint the loop without changes
		int[] chain = new int[9];
		for (int i = 0; i < 8; i++) {
			chain[i] = Chain.makeSEntry(loopCells[i], digitA, true);
		}
		chain[8] = chain[0];
		globalStep.addChain(0, 8, chain);
	}

	/**
	 * Emits {@link #globalStep}: in single-step mode as {@link #result}, in
	 * all-steps mode into {@link #steps} deduplicated by
	 * type + cells + eliminations (mirrors {@link OddagonSolver}).
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

	/** Equality key of a step: type + cells (order-insensitive) + eliminations. */
	private static String stepKey(SolutionStep step) {
		StringBuilder key = new StringBuilder();
		key.append(step.getType().ordinal());
		List<Integer> cells = new ArrayList<Integer>(step.getIndices());
		java.util.Collections.sort(cells);
		key.append('|').append(cells);
		key.append('|');
		for (Candidate cand : step.getCandidatesToDelete()) {
			key.append(cand.getIndex()).append(':').append(cand.getValue()).append(',');
		}
		return key.toString();
	}
}
