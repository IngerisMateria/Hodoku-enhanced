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
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import sudoku.Candidate;
import sudoku.SolutionStep;
import sudoku.SolutionType;

/**
 * The single {@link SolutionStep} subclass for all techniques of the modern
 * fork.
 *
 * The legacy {@code SolutionStep.toString(int)} builds the hint text in one
 * big switch over {@link SolutionType} and <b>throws</b> for types it does not
 * know. Instead of growing that legacy switch (or one step subclass per
 * technique, as milestone 1.1 did with {@code WxyzWingStep}), modern
 * techniques register a {@link HintFormatter} per {@link SolutionType} here
 * and share this one subclass. The step data stays entirely in the
 * {@code SolutionStep} base fields (no new instance state), so {@code clone()}
 * (via {@code super.clone()}) and XMLEncoder persistence keep working
 * unchanged.
 *
 * Data layout conventions per technique (kept strictly in base fields):
 * <ul>
 * <li>Bent subsets ({@link SolutionType#BENT_QUAD} and the size 5..9 entries
 * VWXYZ_WING .. RSTUVWXYZ_WING): {@code values} holds the n candidates with the n-1
 * restricted ones first (ascending) and the non-restricted candidate Z last;
 * {@code indices} holds the n cells ascending; {@code fins} holds the Z
 * candidates inside the set.</li>
 * <li>Canonical WXYZ-Wing ({@link SolutionType#WXYZ_WING}): {@code values}
 * holds W/X/Y ascending and Z last; {@code indices} holds the hinge first,
 * then the three wings ascending; {@code fins} holds the Z candidates of the
 * pattern. The subtype is derived, not stored: type 1 iff the hinge carries Z
 * (the hinge index appears in {@code fins}).</li>
 * <li>Oddagons ({@link SolutionType#BROKEN_WING} and
 * {@link SolutionType#BIVALUE_ODDAGON}, milestone 1.6): {@code values} holds
 * the digit d resp. the pair a,b ascending; {@code indices} holds the cycle
 * cells in cycle order (NOT sorted); {@code fins} holds the guardians; empty
 * {@code candidatesToDelete} marks the Broken Wing placement case (the
 * target is {@code fins.get(0)}); one display chain traces the closed
 * loop.</li>
 * <li>Tridagon ({@link SolutionType#TRIDAGON}, milestone 1.7): {@code values}
 * holds the triple a,b,c ascending; {@code indices} holds the 8 loop cells in
 * loop order, then the 4 rectangle cells; {@code fins} holds the guardians
 * (digits outside the triple) followed by the rectangle cells with the triple
 * digits (display); one display chain traces the closed 8-loop.</li>
 * <li>Uniqueness Pack ({@link SolutionType#UNIQUE_LOOP},
 * {@link SolutionType#EXTENDED_UR}, {@link SolutionType#BUG_LITE},
 * {@link SolutionType#REVERSE_BUG}, {@link SolutionType#MUG}, milestone 1.8):
 * {@code values} holds the pattern digits ascending; {@code indices} holds the
 * pattern cells (Unique Loop: loop order; Reverse BUG: the solved cells of the
 * pair, then the target cell last); {@code fins} holds the guardians (Reverse
 * BUG: none); the Unique Loop adds one display chain tracing the closed
 * loop.</li>
 * </ul>
 */
public class ModernStep extends SolutionStep {

	/** Builds the hint text of one technique (the {@code toString(int)} body). */
	public interface HintFormatter {
		/**
		 * @param step the step to format
		 * @param art 0 = short, 1 = medium, 2 = full
		 * @return the hint text
		 */
		String format(ModernStep step, int art);
	}

	/** One formatter per modern technique; filled by static registration. */
	private static final Map<SolutionType, HintFormatter> FORMATTERS = new EnumMap<SolutionType, HintFormatter>(
			SolutionType.class);

	static {
		// one shared formatter instance for all bent subset sizes (n=4..9)
		HintFormatter bentSubsets = new BentSubsetFormatter();
		registerFormatter(SolutionType.BENT_QUAD, bentSubsets);
		registerFormatter(SolutionType.VWXYZ_WING, bentSubsets);
		registerFormatter(SolutionType.UVWXYZ_WING, bentSubsets);
		registerFormatter(SolutionType.TUVWXYZ_WING, bentSubsets);
		registerFormatter(SolutionType.STUVWXYZ_WING, bentSubsets);
		registerFormatter(SolutionType.RSTUVWXYZ_WING, bentSubsets);
		// one shared formatter for both oddagon techniques (milestone 1.6)
		HintFormatter oddagons = new OddagonFormatter();
		registerFormatter(SolutionType.BROKEN_WING, oddagons);
		registerFormatter(SolutionType.BIVALUE_ODDAGON, oddagons);
		// the Tridagon (milestone 1.7)
		registerFormatter(SolutionType.TRIDAGON, new TridagonFormatter());
		// the Uniqueness Pack (milestone 1.8): one shared formatter
		HintFormatter uniquenessPack = new UniquenessPackFormatter();
		// Uniqueness Pack desglose (milestone 1.9): the finder emits the Type 1 /
		// Type 2 subtypes; the generic anchors stay registered defensively
		registerFormatter(SolutionType.UNIQUE_LOOP, uniquenessPack);
		registerFormatter(SolutionType.UNIQUE_LOOP_TYPE_1, uniquenessPack);
		registerFormatter(SolutionType.UNIQUE_LOOP_TYPE_2, uniquenessPack);
		registerFormatter(SolutionType.EXTENDED_UR, uniquenessPack);
		registerFormatter(SolutionType.EXTENDED_UR_TYPE_1, uniquenessPack);
		registerFormatter(SolutionType.EXTENDED_UR_TYPE_2, uniquenessPack);
		registerFormatter(SolutionType.BUG_LITE, uniquenessPack);
		registerFormatter(SolutionType.BUG_LITE_TYPE_1, uniquenessPack);
		registerFormatter(SolutionType.BUG_LITE_TYPE_2, uniquenessPack);
		registerFormatter(SolutionType.REVERSE_BUG, uniquenessPack);
		registerFormatter(SolutionType.MUG, uniquenessPack);
		registerFormatter(SolutionType.MUG_TYPE_1, uniquenessPack);
		registerFormatter(SolutionType.MUG_TYPE_2, uniquenessPack);
	}

	/**
	 * Registers the hint formatter for one modern technique. Every
	 * {@link SolutionType} a modern finder emits must have a formatter
	 * registered before the first hint is rendered (register from a static
	 * initializer, like the ones above).
	 *
	 * @param type the technique
	 * @param formatter the hint text builder
	 */
	public static void registerFormatter(SolutionType type, HintFormatter formatter) {
		FORMATTERS.put(type, formatter);
	}

	/** Required for cloning via super.clone() and for XMLEncoder. */
	public ModernStep() {
		super();
	}

	/**
	 * Creates a step of the given modern technique.
	 *
	 * @param type the technique
	 */
	public ModernStep(SolutionType type) {
		super(type);
	}

	@Override
	public String toString(int art) {
		HintFormatter formatter = FORMATTERS.get(getType());
		if (formatter == null) {
			throw new RuntimeException("Invalid type in ModernStep.toString(): " + getType());
		}
		return formatter.format(this, art);
	}

	/**
	 * The name to show for this step's technique: the user's preferred alias
	 * if one is persisted, the default name otherwise (milestone 1.4
	 * display-name indirection). All modern formatters use this instead of
	 * {@code getStepName()}; the legacy hint pipeline stays untouched.
	 *
	 * @return the display name of the technique
	 */
	public String getDisplayName() {
		return solver.modern.registry.TechniqueRegistry.getInstance().getDisplayName(getType());
	}

	/**
	 * Formatter for bent naked subsets of any size. Mirrors the XY-/XYZ-Wing
	 * format: name, candidates, cells, eliminations. Example:
	 * {@code Bent Quad: 1/2/5/9 in r1c25,r2c12 (Z=9) => r1c1<>9}
	 */
	private static class BentSubsetFormatter implements HintFormatter {
		@Override
		public String format(ModernStep step, int art) {
			StringBuilder tmp = new StringBuilder(step.getDisplayName());
			List<Integer> values = step.getValues();
			if (art >= 1 && values.size() >= 2) {
				tmp.append(": ").append(values.get(0));
				for (int i = 1; i < values.size(); i++) {
					tmp.append('/').append(values.get(i));
				}
			}
			if (art >= 2 && !step.getIndices().isEmpty()) {
				tmp.append(' ')
						.append(java.util.ResourceBundle.getBundle("intl/SolutionStep").getString("SolutionStep.in"))
						.append(' ').append(getCompactCellPrint(step.getIndices()));
				if (!values.isEmpty()) {
					tmp.append(" (Z=").append(values.get(values.size() - 1)).append(')');
				}
				step.appendCandidatesToDelete(tmp);
			}
			return tmp.toString();
		}
	}

	/**
	 * Same output format as the private
	 * {@code SolutionStep.getCandidatesToDelete(StringBuffer)}: eliminations
	 * grouped by candidate, e.g. {@code " => r1c23<>5, r4c5<>7"}. Shared
	 * helper for all formatters.
	 *
	 * @param tmp the builder to append to
	 */
	public void appendCandidatesToDelete(StringBuilder tmp) {
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
