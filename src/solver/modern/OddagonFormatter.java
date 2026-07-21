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

import java.util.List;

import sudoku.Candidate;
import sudoku.SolutionStep;
import sudoku.SolutionType;

/**
 * Hint formatter for the oddagon techniques (Broken Wing and Bivalue
 * Oddagon), registered in {@link ModernStep}. Data layout per
 * {@link OddagonSolver}: values = digit(s), indices = cycle cells in cycle
 * order, fins = guardians, empty candidatesToDelete = Broken Wing placement
 * of fins[0].
 *
 * Examples:
 *
 * <pre>
 * Broken Wing: 5 in r1c2,r2c5,r4c5,r4c8,r1c8 (guardians: r1c5,r4c2) =&gt; r7c2&lt;&gt;5
 * Broken Wing: 5 in ... (guardian: r1c5) =&gt; r1c5=5 (the odd loop of conjugate links would be impossible)
 * Bivalue Oddagon: 3/7 in r1c2,r2c5,r4c5,r4c8,r1c8 (guardian: 4 in r2c5) =&gt; r2c5&lt;&gt;3, r2c5&lt;&gt;7
 * </pre>
 */
class OddagonFormatter implements ModernStep.HintFormatter {

	@Override
	public String format(ModernStep step, int art) {
		StringBuilder tmp = new StringBuilder(step.getDisplayName());
		List<Integer> values = step.getValues();
		if (art >= 1 && !values.isEmpty()) {
			tmp.append(": ").append(values.get(0));
			for (int i = 1; i < values.size(); i++) {
				tmp.append('/').append(values.get(i));
			}
		}
		if (art >= 2) {
			tmp.append(' ')
					.append(java.util.ResourceBundle.getBundle("intl/SolutionStep").getString("SolutionStep.in"))
					.append(' ');
			// cycle order matters: print the loop as-is, no compact grouping
			List<Integer> indices = step.getIndices();
			for (int i = 0; i < indices.size(); i++) {
				if (i > 0) {
					tmp.append(',');
				}
				tmp.append(SolutionStep.getCellPrint(indices.get(i), false));
			}
			appendGuardians(tmp, step);
			if (step.getCandidatesToDelete().isEmpty()) {
				// Broken Wing |G|=1: placement of the single guardian
				Candidate guardian = step.getFins().get(0);
				tmp.append(" => ").append(SolutionStep.getCellPrint(guardian.getIndex(), false)).append('=')
						.append(guardian.getValue());
			} else {
				step.appendCandidatesToDelete(tmp);
			}
			appendJustification(tmp, step);
		}
		return tmp.toString();
	}

	/** Names the guardians: " (guardians: r1c5,r4c2)" / " (guardian: 4 in r2c5)". */
	private static void appendGuardians(StringBuilder tmp, ModernStep step) {
		List<Candidate> fins = step.getFins();
		if (fins.isEmpty()) {
			return;
		}
		boolean brokenWing = step.getType() == SolutionType.BROKEN_WING;
		tmp.append(" (guardian").append(fins.size() > 1 ? "s" : "").append(": ");
		for (int i = 0; i < fins.size(); i++) {
			if (i > 0) {
				tmp.append(',');
			}
			if (!brokenWing) {
				// mixed digits possible: name digit and cell
				tmp.append(fins.get(i).getValue()).append(" in ");
			}
			tmp.append(SolutionStep.getCellPrint(fins.get(i).getIndex(), false));
		}
		tmp.append(')');
	}

	/** One-line why: the impossible pattern that the guardians avert. */
	private static void appendJustification(StringBuilder tmp, ModernStep step) {
		if (step.getType() == SolutionType.BROKEN_WING) {
			tmp.append(" (without a true guardian the loop would be an odd ring of conjugate links - impossible)");
		} else {
			tmp.append(" (without a true guardian the loop would be an odd ring of bivalue cells - impossible)");
		}
	}
}
