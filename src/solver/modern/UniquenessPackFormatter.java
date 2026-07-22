/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.8).
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

import java.util.List;

import sudoku.Candidate;
import sudoku.SolutionStep;
import sudoku.SolutionType;

/**
 * Hint formatter for the Uniqueness Pack techniques (milestone 1.8),
 * registered in {@link ModernStep}. Data layout per
 * {@link UniquenessPackSolver}: values = the pattern digits, indices = the
 * pattern cells (UL: loop order; Reverse BUG: solved pair cells then the
 * target cell), fins = the guardians (Reverse BUG: none).
 *
 * Guardian techniques carry the WXYZ-style subtype: Type 1 = all guardians in
 * one cell (the pattern digits are stripped there), Type 2 = uniform-digit
 * guardians (the digit is eliminated from cells seeing them all).
 *
 * Examples:
 *
 * <pre>
 * Unique Loop Type 1: 4/9 in r1c2,r1c5,r4c5,r4c8,r7c8,r7c2 (guardian: 3 in r4c5) =&gt; r4c5&lt;&gt;4, r4c5&lt;&gt;9
 * Extended Unique Rectangle Type 2: 1/4/8 in r2c7,r2c8,r5c7,r5c8,r8c7,r8c8 (guardians: 3 in r2c7,r5c8) =&gt; r2c9&lt;&gt;3
 * Reverse BUG: 4/9 =&gt; r5c6&lt;&gt;4 (placing it would leave the solved 4/9 cells as an unavoidable set - the puzzle would not be unique)
 * </pre>
 */
class UniquenessPackFormatter implements ModernStep.HintFormatter {

	@Override
	public String format(ModernStep step, int art) {
		StringBuilder tmp = new StringBuilder(step.getDisplayName());
		appendSubtype(tmp, step);
		List<Integer> values = step.getValues();
		if (art >= 1 && !values.isEmpty()) {
			tmp.append(": ").append(values.get(0));
			for (int i = 1; i < values.size(); i++) {
				tmp.append('/').append(values.get(i));
			}
		}
		if (art >= 2) {
			if (step.getType() != SolutionType.REVERSE_BUG) {
				tmp.append(' ')
						.append(java.util.ResourceBundle.getBundle("intl/SolutionStep").getString("SolutionStep.in"))
						.append(' ');
				List<Integer> indices = step.getIndices();
				if (isUniqueLoop(step.getType())) {
					// loop order matters: print as-is, no compact grouping
					for (int i = 0; i < indices.size(); i++) {
						if (i > 0) {
							tmp.append(',');
						}
						tmp.append(SolutionStep.getCellPrint(indices.get(i), false));
					}
				} else {
					tmp.append(SolutionStep.getCompactCellPrint(indices));
				}
				appendGuardians(tmp, step);
			}
			step.appendCandidatesToDelete(tmp);
			appendJustification(tmp, step);
		}
		return tmp.toString();
	}

	/**
	 * " Type 1" (one guardian cell) / " Type 2" (uniform digit). Not appended for
	 * Reverse BUG, nor for the split subtypes — since the milestone 1.9 desglose
	 * the guardian techniques always emit a subtype whose display name already
	 * carries "Type 1/2" (so appending here would duplicate it). Kept as a
	 * defensive derivation for any generic-anchor step.
	 */
	private static void appendSubtype(StringBuilder tmp, ModernStep step) {
		if (step.getType() == SolutionType.REVERSE_BUG || step.getFins().isEmpty() || isSplitSubtype(step.getType())) {
			return;
		}
		List<Candidate> fins = step.getFins();
		int cell = fins.get(0).getIndex();
		boolean oneCell = true;
		for (int i = 1; i < fins.size(); i++) {
			if (fins.get(i).getIndex() != cell) {
				oneCell = false;
				break;
			}
		}
		tmp.append(oneCell ? " Type 1" : " Type 2");
	}

	/** True for the Type 1 / Type 2 subtype labels of the guardian techniques. */
	private static boolean isSplitSubtype(SolutionType t) {
		switch (t) {
		case UNIQUE_LOOP_TYPE_1:
		case UNIQUE_LOOP_TYPE_2:
		case EXTENDED_UR_TYPE_1:
		case EXTENDED_UR_TYPE_2:
		case BUG_LITE_TYPE_1:
		case BUG_LITE_TYPE_2:
		case MUG_TYPE_1:
		case MUG_TYPE_2:
			return true;
		default:
			return false;
		}
	}

	/** True for Unique Loop and its two subtypes (loop-order cell printing). */
	private static boolean isUniqueLoop(SolutionType t) {
		return t == SolutionType.UNIQUE_LOOP || t == SolutionType.UNIQUE_LOOP_TYPE_1
				|| t == SolutionType.UNIQUE_LOOP_TYPE_2;
	}

	/** Names the guardians: " (guardian: 3 in r4c5)" / " (guardians: 3 in r2c7,r5c8)". */
	private static void appendGuardians(StringBuilder tmp, ModernStep step) {
		List<Candidate> fins = step.getFins();
		if (fins.isEmpty()) {
			return;
		}
		tmp.append(" (guardian").append(fins.size() > 1 ? "s" : "").append(": ");
		for (int i = 0; i < fins.size(); i++) {
			if (i > 0) {
				tmp.append(',');
			}
			tmp.append(fins.get(i).getValue()).append(" in ")
					.append(SolutionStep.getCellPrint(fins.get(i).getIndex(), false));
		}
		tmp.append(')');
	}

	/** One-line why: the deadly pattern the guardians (or the theorem) avert. */
	private static void appendJustification(StringBuilder tmp, ModernStep step) {
		switch (step.getType()) {
		case UNIQUE_LOOP:
		case UNIQUE_LOOP_TYPE_1:
		case UNIQUE_LOOP_TYPE_2:
			tmp.append(" (without a true guardian the loop cells could swap the pair - two solutions)");
			break;
		case EXTENDED_UR:
		case EXTENDED_UR_TYPE_1:
		case EXTENDED_UR_TYPE_2:
			tmp.append(" (without a true guardian the six cells could permute the triple - two solutions)");
			break;
		case BUG_LITE:
		case BUG_LITE_TYPE_1:
		case BUG_LITE_TYPE_2:
			tmp.append(" (without a true guardian the pattern admits a second valid arrangement - two solutions)");
			break;
		case REVERSE_BUG:
			tmp.append(" (placing it would leave the solved pair cells as an unavoidable set - the puzzle would not be unique)");
			break;
		case MUG:
		case MUG_TYPE_1:
		case MUG_TYPE_2:
			tmp.append(" (without a true guardian the catalog pattern admits a second valid arrangement - two solutions)");
			break;
		default:
			break;
		}
	}
}
