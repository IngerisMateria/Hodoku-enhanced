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

import java.util.List;

import sudoku.Candidate;
import sudoku.SolutionStep;

/**
 * Hint formatter for the Tridagon (milestone 1.7), registered in
 * {@link ModernStep}. Data layout per {@link TridagonSolver}: values = the
 * triple ascending, indices = the 8 loop cells in loop order then the 4
 * rectangle cells, fins = the guardians (digits outside the triple) followed
 * by the rectangle cells with the triple digits (display only).
 *
 * Example:
 *
 * <pre>
 * Tridagon: 4/5/6 in r1c2,r1c5,r2c4,r2c1,r3c3,r3c6,r1c4,... (rectangle: r2c2,r3c5,r7c5,r7c2) (guardian: 7 in r2c2) =&gt; r2c2&lt;&gt;4, r2c2&lt;&gt;5, r2c2&lt;&gt;6 (without a true guardian the four box transversals would compose to an odd permutation that cannot close - impossible)
 * </pre>
 */
class TridagonFormatter implements ModernStep.HintFormatter {

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
			// loop order matters: print the 8-loop as-is, then the rectangle
			List<Integer> indices = step.getIndices();
			int loopLength = Math.min(8, indices.size());
			for (int i = 0; i < loopLength; i++) {
				if (i > 0) {
					tmp.append(',');
				}
				tmp.append(SolutionStep.getCellPrint(indices.get(i), false));
			}
			if (indices.size() > loopLength) {
				tmp.append(" (rectangle: ");
				for (int i = loopLength; i < indices.size(); i++) {
					if (i > loopLength) {
						tmp.append(',');
					}
					tmp.append(SolutionStep.getCellPrint(indices.get(i), false));
				}
				tmp.append(')');
			}
			appendGuardians(tmp, step);
			step.appendCandidatesToDelete(tmp);
			tmp.append(" (without a true guardian the four box transversals would compose to an odd permutation "
					+ "that cannot close - impossible)");
		}
		return tmp.toString();
	}

	/**
	 * Names the guardians: " (guardian: 7 in r2c2)". Guardians are the fins
	 * whose digit lies outside the triple — the remaining fins are the
	 * rectangle highlight and are not printed.
	 */
	private static void appendGuardians(StringBuilder tmp, ModernStep step) {
		List<Candidate> fins = step.getFins();
		List<Integer> values = step.getValues();
		int count = 0;
		for (Candidate fin : fins) {
			if (!values.contains(fin.getValue())) {
				count++;
			}
		}
		if (count == 0) {
			return;
		}
		tmp.append(" (guardian").append(count > 1 ? "s" : "").append(": ");
		boolean first = true;
		for (Candidate fin : fins) {
			if (values.contains(fin.getValue())) {
				continue;
			}
			if (!first) {
				tmp.append(',');
			}
			first = false;
			tmp.append(fin.getValue()).append(" in ").append(SolutionStep.getCellPrint(fin.getIndex(), false));
		}
		tmp.append(')');
	}
}
