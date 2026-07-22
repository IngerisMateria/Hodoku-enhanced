/*
 * Copyright (C) 2026  HoDoKu modern fork
 *
 * This file is part of HoDoKu.
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
package sudoku.ui;

/**
 * Shared sizes of the configuration tabs (milestone 1.10, task C).
 * <p>
 * The tabs of {@code ConfigDialog} are dense NetBeans-generated {@code GroupLayout}
 * code, written over a decade by different hands: every control carries its own
 * ad-hoc constraints, so the same kind of widget ends up a different size on every
 * tab (and sometimes inside the same tab). This class is the single place where the
 * fork states how big each kind of widget is; the tabs reference these constants
 * from their {@code addComponent(...)} calls instead of repeating magic numbers.
 * <p>
 * Vocabulary (the project owner's, kept verbatim so the docs and the code agree):
 * <ul>
 * <li><b>bullet</b> ({@code viñeta}) - a category group box, i.e. a {@code JPanel}
 * with a {@code TitledBorder} ("Fish general", "Startup", "Coloring"...). Bullets
 * that share a column must be equally wide and must fill that column.</li>
 * <li><b>toggle</b> - a choice control, in practice a {@code JComboBox}. The good
 * pattern (the one the Steps tab already had) is: fixed width, anchored to the
 * <em>right</em> edge of its bullet, with the slack absorbed by the gap that
 * follows the label - so the control grows leftwards, never to the full width of
 * the bullet.</li>
 * <li><b>input</b> - a text field. Numeric ones use the width of the
 * {@code factor} / score fields of the Level/Font tab, which the owner picked as
 * the reference.</li>
 * </ul>
 * Purely cosmetic: nothing here reaches the solver.
 */
public final class UiMetrics {

	private UiMetrics() {
		// constants only
	}

	/**
	 * Width of a toggle (combo box) that shows words, e.g. "Basic/Franken" or
	 * "System default". Wide enough for the longest entry any of them holds today.
	 */
	public static final int TOGGLE_WIDTH = 130;

	/**
	 * Width of a toggle that only shows a number or a very short token, e.g. the
	 * fin counts and the chain length. Kept clearly narrower than
	 * {@link #TOGGLE_WIDTH} so numeric choosers do not look like text choosers.
	 */
	public static final int TOGGLE_WIDTH_COMPACT = 60;

	/**
	 * Width of a numeric input field. Reference chosen by the owner: the 'factor'
	 * and score fields of the Level/Font tab.
	 */
	public static final int NUMERIC_INPUT_WIDTH = 60;

	/** Width of a free-text input field (same visual weight as a text toggle). */
	public static final int TEXT_INPUT_WIDTH = 130;

	/**
	 * Width of the small color swatch buttons (Level/Font difficulty colors). The
	 * owner asked explicitly for one small, consistent size.
	 */
	public static final int COLOR_BUTTON_WIDTH = 30;

	/** Height of the small color swatch buttons. */
	public static final int COLOR_BUTTON_HEIGHT = 20;

	/**
	 * Space between the contents of a bullet and its border. Matches the
	 * container gap NetBeans used, so applying it changes nothing where the tab
	 * was already right.
	 */
	public static final int BULLET_INSET = 10;

	/** Vertical space between two bullets stacked in the same column. */
	public static final int BULLET_GAP = 6;
}
