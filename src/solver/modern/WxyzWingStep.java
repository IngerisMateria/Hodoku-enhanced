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
import java.util.Iterator;
import java.util.List;

import sudoku.Candidate;
import sudoku.SolutionStep;
import sudoku.SolutionType;

/**
 * {@link SolutionStep} for WXYZ-Wings.
 *
 * The legacy {@code SolutionStep.toString(int)} builds the hint text in one
 * big switch over {@link SolutionType} and <b>throws</b> for types it does not
 * know. Techniques of the modern fork therefore bring their own step subclass
 * with the hint text, instead of growing that legacy switch: the step data
 * stays entirely in the {@code SolutionStep} base fields (no new state), so
 * {@code clone()} (via {@code super.clone()}) and XMLEncoder persistence keep
 * working unchanged.
 *
 * Data layout used by {@link WxyzWingSolver}: {@code values} holds the four
 * candidates with the three restricted ones first (ascending) and the
 * non-restricted candidate Z last; {@code indices} holds the four cells;
 * {@code fins} holds the Z candidates inside the set (mirrors XYZ-Wing).
 */
public class WxyzWingStep extends SolutionStep {

	/** Required for cloning via super.clone() and for XMLEncoder. */
	public WxyzWingStep() {
		super(SolutionType.WXYZ_WING);
	}

	/**
	 * Mirrors the XY-/XYZ-Wing format: name, candidates, cells, eliminations.
	 * Example: {@code WXYZ-Wing: 1/2/5/9 in r1c25,r2c12 (Z=9) => r1c1<>9}
	 *
	 * @param art 0 = short, 1 = medium, 2 = full
	 * @return the hint text for the step
	 */
	@Override
	public String toString(int art) {
		StringBuilder tmp = new StringBuilder(getStepName());
		List<Integer> values = getValues();
		if (art >= 1 && values.size() >= 4) {
			tmp.append(": ").append(values.get(0)).append('/').append(values.get(1)).append('/')
					.append(values.get(2)).append('/').append(values.get(3));
		}
		if (art >= 2 && !getIndices().isEmpty()) {
			tmp.append(' ')
					.append(java.util.ResourceBundle.getBundle("intl/SolutionStep").getString("SolutionStep.in"))
					.append(' ').append(getCompactCellPrint(getIndices()));
			if (values.size() >= 4) {
				tmp.append(" (Z=").append(values.get(3)).append(')');
			}
			appendCandidatesToDelete(tmp);
		}
		return tmp.toString();
	}

	/**
	 * Same output format as the private
	 * {@code SolutionStep.getCandidatesToDelete(StringBuffer)}: eliminations
	 * grouped by candidate, e.g. {@code " => r1c23<>5, r4c5<>7"}.
	 */
	private void appendCandidatesToDelete(StringBuilder tmp) {
		tmp.append(" => ");
		List<Candidate> tmpList = new ArrayList<Candidate>(getCandidatesToDelete());
		List<Integer> candList = new ArrayList<Integer>();
		boolean first = true;
		while (!tmpList.isEmpty()) {
			Candidate firstCand = tmpList.remove(0);
			candList.clear();
			candList.add(firstCand.getIndex());
			Iterator<Candidate> it = tmpList.iterator();
			while (it.hasNext()) {
				Candidate c1 = it.next();
				if (c1.getValue() == firstCand.getValue()) {
					candList.add(c1.getIndex());
					it.remove();
				}
			}
			if (first) {
				first = false;
			} else {
				tmp.append(", ");
			}
			tmp.append(getCompactCellPrint(candList));
			tmp.append("<>");
			tmp.append(firstCand.getValue());
		}
	}
}
