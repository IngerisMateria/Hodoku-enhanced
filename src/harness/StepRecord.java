/*
 * This file is part of the HoDoKu fork verification harness (milestone 0.2).
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
package harness;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import sudoku.Candidate;
import sudoku.SolutionStep;

/**
 * Locale-independent extract of a {@link SolutionStep}: the technique plus the
 * changes the step makes to the grid. The mapping from step to grid changes
 * mirrors exactly what the doStep() implementations execute (SimpleSolver,
 * BruteForceSolver, TemplateSolver, TablingSolver; every other solver only
 * eliminates the candidates in getCandidatesToDelete()).
 */
public final class StepRecord {

	/** {@link sudoku.SolutionType#name()} — stable, locale-independent. */
	public final String technique;
	/** Values placed by this step (empty for pure elimination steps). */
	public final List<CellValue> placements;
	/** Candidates eliminated by this step. */
	public final List<CellValue> eliminations;

	public StepRecord(String technique, List<CellValue> placements, List<CellValue> eliminations) {
		this.technique = technique;
		this.placements = Collections.unmodifiableList(placements);
		this.eliminations = Collections.unmodifiableList(eliminations);
	}

	/** Extracts the grid changes of <code>step</code>, mirroring doStep(). */
	public static StepRecord from(SolutionStep step) {
		List<CellValue> placements = new ArrayList<CellValue>();
		List<CellValue> eliminations = new ArrayList<CellValue>();
		switch (step.getType()) {
		case FULL_HOUSE:
		case HIDDEN_SINGLE:
		case NAKED_SINGLE:
			placements.add(new CellValue(step.getIndices().get(0), step.getValues().get(0)));
			break;
		case TEMPLATE_SET:
		case BRUTE_FORCE: {
			int value = step.getValues().get(0);
			for (int index : step.getIndices()) {
				placements.add(new CellValue(index, value));
			}
			break;
		}
		case FORCING_CHAIN:
		case FORCING_CHAIN_CONTRADICTION:
		case FORCING_CHAIN_VERITY:
		case FORCING_NET:
		case FORCING_NET_CONTRADICTION:
		case FORCING_NET_VERITY:
			if (step.getValues().size() > 0) {
				for (int i = 0; i < step.getValues().size(); i++) {
					placements.add(new CellValue(step.getIndices().get(i), step.getValues().get(i)));
				}
			} else {
				addEliminations(step, eliminations);
			}
			break;
		case GIVE_UP:
		case INCOMPLETE:
			break;
		case BROKEN_WING:
			// modern fork (milestone 1.6): |G|=1 places the single guardian
			// (kept in fins); otherwise a plain elimination step — mirrors
			// OddagonSolver.doStep()
			if (step.getCandidatesToDelete().isEmpty()) {
				placements.add(new CellValue(step.getFins().get(0).getIndex(), step.getFins().get(0).getValue()));
			} else {
				addEliminations(step, eliminations);
			}
			break;
		default:
			addEliminations(step, eliminations);
			break;
		}
		return new StepRecord(step.getType().name(), placements, eliminations);
	}

	private static void addEliminations(SolutionStep step, List<CellValue> eliminations) {
		for (Candidate cand : step.getCandidatesToDelete()) {
			eliminations.add(new CellValue(cand.getIndex(), cand.getValue()));
		}
	}

	public String toJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{\"technique\":").append(Json.str(technique));
		sb.append(",\"placements\":");
		appendCellValues(sb, placements);
		sb.append(",\"eliminations\":");
		appendCellValues(sb, eliminations);
		sb.append('}');
		return sb.toString();
	}

	private static void appendCellValues(StringBuilder sb, List<CellValue> list) {
		sb.append('[');
		for (int i = 0; i < list.size(); i++) {
			if (i > 0) {
				sb.append(',');
			}
			CellValue cv = list.get(i);
			sb.append("{\"cell\":").append(Json.str(cv.cell())).append(",\"value\":").append(cv.value).append('}');
		}
		sb.append(']');
	}

	@Override
	public String toString() {
		return technique + " set" + placements + " del" + eliminations;
	}
}
