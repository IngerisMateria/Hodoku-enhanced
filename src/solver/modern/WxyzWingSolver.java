/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.2).
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

import solver.AbstractSolver;
import solver.SudokuStepFinder;
import sudoku.Candidate;
import sudoku.SolutionStep;
import sudoku.SolutionType;
import sudoku.Sudoku2;
import sudoku.SudokuSet;

/**
 * Finder for canonical WXYZ-Wings (hinge + three bivalue wings; milestone
 * 1.2).
 *
 * A canonical WXYZ-Wing is a hinge cell H and three wing cells w1, w2, w3,
 * each of which sees H, such that the candidate union of the four cells is
 * exactly four digits {W,X,Y,Z} and every wing is bivalue {a,Z} with a
 * pairwise distinct letter a covering {W,X,Y}. Type 1: Z in H (H holds all
 * four candidates); type 2: Z not in H (H = {W,X,Y}). The degenerate case
 * where the four cells all see each other is a classical Naked Quadruple and
 * is skipped. Z can be eliminated from every external cell that sees all Z
 * carriers of the pattern (the wings, plus H for type 1).
 *
 * Justification: if no pattern cell held Z, every wing would be forced to its
 * letter, and H would have to take one of those three letters, clashing with
 * the wing that holds it (which sees H) - contradiction.
 *
 * This is a separate finder and not a filter of {@link BentSubsetSolver}: the
 * hinge may spread its wings over its row, column and box at once, so there
 * are canonical instances the bent (line + box) formulation does not cover.
 * The overlap in the other direction is expected and wanted (desglose
 * principle): where both exist, the solver shows the canonical one (solver
 * order decides) and all-steps mode lists both, like Skyscraper / 2-String
 * Kite / Empty Rectangle next to Turbot Fish.
 *
 * Structure mirrors {@link solver.WingSolver} (single-step and all-steps
 * mode); registered in {@link SudokuStepFinder}.
 */
public class WxyzWingSolver extends AbstractSolver {

	static {
		ModernStep.registerFormatter(SolutionType.WXYZ_WING, new WxyzWingFormatter());
	}

	/** One global step for eliminations (cloned when a wing is found). */
	private SolutionStep globalStep = new ModernStep();
	/** A list for all steps found in one search. */
	private List<SolutionStep> steps = new ArrayList<SolutionStep>();
	/** Dedup keys (type + cells + eliminations) of one all-steps search. */
	private Set<String> stepKeys = new HashSet<String>();
	/** A set for elimination checks. */
	private SudokuSet elimSet = new SudokuSet();
	/** Buffers for the eligible wing cells of each of the three letters. */
	private int[][] wingBuffers = new int[3][81];
	/** Number of eligible wing cells per letter. */
	private int[] wingCounts = new int[3];
	/** The three letters {W,X,Y} of the current pattern, ascending. */
	private int[] letters = new int[3];
	/** The wing cells of the current combination, sorted ascending. */
	private int[] wings = new int[3];

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
	 * Finds all canonical WXYZ-Wings in the current sudoku (all-steps mode).
	 *
	 * @return all steps found
	 */
	public List<SolutionStep> getAllWxyzWings() {
		sudoku = finder.getSudoku();
		List<SolutionStep> newSteps = new ArrayList<SolutionStep>();
		List<SolutionStep> oldSteps = steps;
		steps = newSteps;
		stepKeys.clear();
		findWxyzWing(false);
		steps = oldSteps;
		return newSteps;
	}

	/**
	 * Iterates all possible hinges: unsolved cells with 4 candidates (type 1:
	 * Z among them) or 3 candidates (type 2: Z any digit not in the hinge).
	 *
	 * @param onlyOne return the first step found instead of collecting all
	 * @return the first step found if <code>onlyOne</code> is set, else null
	 */
	private SolutionStep findWxyzWing(boolean onlyOne) {
		for (int hinge = 0; hinge < Sudoku2.LENGTH; hinge++) {
			if (sudoku.getValue(hinge) != 0) {
				continue;
			}
			short hingeCands = sudoku.getCell(hinge);
			int anz = Sudoku2.ANZ_VALUES[hingeCands];
			if (anz != 3 && anz != 4) {
				continue;
			}
			for (int z = 1; z <= 9; z++) {
				boolean zInHinge = (hingeCands & Sudoku2.MASKS[z]) != 0;
				if (anz == 4 && !zInHinge || anz == 3 && zInHinge) {
					// type 1 needs Z in the hinge (H = {W,X,Y,Z}),
					// type 2 needs Z outside (H = {W,X,Y})
					continue;
				}
				SolutionStep step = checkHinge(hinge, hingeCands, z, onlyOne);
				if (onlyOne && step != null) {
					return step;
				}
			}
		}
		return null;
	}

	/**
	 * Checks one (hinge, Z) pair: collects the eligible bivalue wing cells
	 * {letter, Z} among the buddies of the hinge for each of the three letters
	 * and enumerates all combinations.
	 */
	private SolutionStep checkHinge(int hinge, short hingeCands, int z, boolean onlyOne) {
		// the three letters {W,X,Y}: the hinge candidates without Z
		int[] hingeValues = Sudoku2.POSSIBLE_VALUES[hingeCands];
		int anzLetters = 0;
		for (int i = 0; i < hingeValues.length; i++) {
			if (hingeValues[i] != z) {
				letters[anzLetters++] = hingeValues[i];
			}
		}
		// eligible wings per letter: bivalue buddies of the hinge with
		// candidates exactly {letter, Z}
		SudokuSet buddies = Sudoku2.buddies[hinge];
		wingCounts[0] = wingCounts[1] = wingCounts[2] = 0;
		for (int i = 0; i < buddies.size(); i++) {
			int cell = buddies.get(i);
			if (sudoku.getValue(cell) != 0) {
				continue;
			}
			short cands = sudoku.getCell(cell);
			for (int j = 0; j < 3; j++) {
				if (cands == (short) (Sudoku2.MASKS[letters[j]] | Sudoku2.MASKS[z])) {
					wingBuffers[j][wingCounts[j]++] = cell;
				}
			}
		}
		if (wingCounts[0] == 0 || wingCounts[1] == 0 || wingCounts[2] == 0) {
			return null;
		}
		for (int i = 0; i < wingCounts[0]; i++) {
			for (int j = 0; j < wingCounts[1]; j++) {
				for (int k = 0; k < wingCounts[2]; k++) {
					SolutionStep step = checkCombination(hinge, z, wingBuffers[0][i], wingBuffers[1][j],
							wingBuffers[2][k], onlyOne);
					if (onlyOne && step != null) {
						return step;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Checks one concrete (hinge, Z, w1, w2, w3) combination: skips the naked
	 * quad degenerate, computes the eliminations and creates the step.
	 */
	private SolutionStep checkCombination(int hinge, int z, int w1, int w2, int w3, boolean onlyOne) {
		// degenerate: all four cells see each other -> classical Naked
		// Quadruple (the wings see the hinge by construction)
		if (Sudoku2.buddies[w1].contains(w2) && Sudoku2.buddies[w1].contains(w3)
				&& Sudoku2.buddies[w2].contains(w3)) {
			return null;
		}
		// Z can be eliminated from every external cell that sees all Z
		// carriers: the three wings, plus the hinge for type 1 (pattern cells
		// are never in the intersection: no cell is its own buddy)
		boolean type1 = (sudoku.getCell(hinge) & Sudoku2.MASKS[z]) != 0;
		elimSet.setAnd(Sudoku2.buddies[w1], Sudoku2.buddies[w2]);
		elimSet.and(Sudoku2.buddies[w3]);
		if (type1) {
			elimSet.and(Sudoku2.buddies[hinge]);
		}
		elimSet.and(finder.getCandidates()[z]);
		// the hinge holds Z in type 1 but is a pattern cell, never eliminated;
		// in type 2 it has no Z; either way it cannot survive in elimSet
		elimSet.remove(hinge);
		if (elimSet.isEmpty()) {
			return null;
		}
		// ok, wing found!
		globalStep.reset();
		globalStep.setType(SolutionType.WXYZ_WING);
		// values: W/X/Y ascending (letters already are), Z last
		globalStep.addValue(letters[0]);
		globalStep.addValue(letters[1]);
		globalStep.addValue(letters[2]);
		globalStep.addValue(z);
		// indices: hinge first, then the wings ascending
		globalStep.addIndex(hinge);
		wings[0] = w1;
		wings[1] = w2;
		wings[2] = w3;
		java.util.Arrays.sort(wings);
		for (int i = 0; i < 3; i++) {
			globalStep.addIndex(wings[i]);
		}
		// fins: the Z candidates of the pattern (type derivation: the step is
		// type 1 iff the hinge appears in the fins)
		if (type1) {
			globalStep.addFin(hinge, z);
		}
		for (int i = 0; i < 3; i++) {
			globalStep.addFin(wings[i], z);
		}
		for (int i = 0; i < elimSet.size(); i++) {
			globalStep.addCandidateToDelete(elimSet.get(i), z);
		}
		if (onlyOne) {
			return (SolutionStep) globalStep.clone();
		}
		// all-steps mode: emit equal steps (type + cells + eliminations) once
		if (stepKeys.add(stepKey(globalStep))) {
			steps.add((SolutionStep) globalStep.clone());
		}
		return null;
	}

	/**
	 * Equality key of a step: type + cells + eliminations (the milestone 1.2
	 * dedup contract). The cell list is order-sensitive by convention (hinge
	 * first), so it is normalized here.
	 */
	private static String stepKey(SolutionStep step) {
		StringBuilder key = new StringBuilder();
		key.append(step.getType().ordinal());
		List<Integer> sorted = new ArrayList<Integer>(step.getIndices());
		java.util.Collections.sort(sorted);
		key.append('|').append(sorted);
		key.append('|');
		for (Candidate cand : step.getCandidatesToDelete()) {
			key.append(cand.getIndex()).append(':').append(cand.getValue()).append(',');
		}
		return key.toString();
	}

	/**
	 * Formatter for canonical WXYZ-Wings. The type is part of the name, like
	 * the fish subtypes. Example:
	 * {@code WXYZ-Wing Type 2: 3/4/7/5 in r2c8,r156c7 (Z=5) => r3c7<>5}
	 * (the first cell of {@code indices} is the hinge).
	 */
	private static class WxyzWingFormatter implements ModernStep.HintFormatter {
		@Override
		public String format(ModernStep step, int art) {
			StringBuilder tmp = new StringBuilder(step.getDisplayName());
			List<Integer> indices = step.getIndices();
			if (!indices.isEmpty()) {
				// type 1 iff the hinge (first index) carries Z (is in the fins)
				int hinge = indices.get(0);
				boolean type1 = false;
				for (Candidate fin : step.getFins()) {
					if (fin.getIndex() == hinge) {
						type1 = true;
						break;
					}
				}
				tmp.append(type1 ? " Type 1" : " Type 2");
			}
			List<Integer> values = step.getValues();
			if (art >= 1 && values.size() >= 4) {
				tmp.append(": ").append(values.get(0)).append('/').append(values.get(1)).append('/')
						.append(values.get(2)).append('/').append(values.get(3));
			}
			if (art >= 2 && !indices.isEmpty()) {
				tmp.append(' ')
						.append(java.util.ResourceBundle.getBundle("intl/SolutionStep").getString("SolutionStep.in"))
						.append(' ').append(SolutionStep.getCompactCellPrint(indices));
				if (values.size() >= 4) {
					tmp.append(" (Z=").append(values.get(3)).append(')');
				}
				step.appendCandidatesToDelete(tmp);
			}
			return tmp.toString();
		}
	}
}
