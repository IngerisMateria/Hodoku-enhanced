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

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * Catalogue of the buttons the user can arrange in each configurable toolbar
 * (P-009 Part 1/2, milestone 1.10).
 * <p>
 * These ids are a persistence contract: they end up in the options file, so they
 * are stable strings, independent of the Swing field names. Nothing here creates
 * a button - every id names a control that already exists in the app. Adding a
 * <em>new</em> toolbar button is explicitly out of scope of this milestone (that
 * belongs to P-005 / P-009 Part 2/2).
 */
public final class ToolBarItems {

	private ToolBarItems() {
		// constants only
	}

	/** Bundle key prefix of the human readable names. */
	private static final String BUNDLE = "intl/ConfigToolBarPanel";

	// --- main window toolbar (the configurable trailing block: the filters) ---

	/** the red/green toggle: filter shows possible or excluded cells. */
	public static final String MAIN_FILTER_MODE = "filterMode";
	/** the nine digit filters; index 0 is digit 1. */
	public static final String[] MAIN_DIGITS = { "digit1", "digit2", "digit3", "digit4", "digit5", "digit6", "digit7",
			"digit8", "digit9" };
	/** the bivalue cell filter (the "XY" button). */
	public static final String MAIN_BIVALUE = "bivalue";

	/**
	 * The main toolbar's configurable buttons, in the order the fork ships them
	 * (identical to the historic hard-coded order).
	 */
	public static final String[] MAIN = { MAIN_FILTER_MODE, MAIN_DIGITS[0], MAIN_DIGITS[1], MAIN_DIGITS[2],
			MAIN_DIGITS[3], MAIN_DIGITS[4], MAIN_DIGITS[5], MAIN_DIGITS[6], MAIN_DIGITS[7], MAIN_DIGITS[8],
			MAIN_BIVALUE };

	// --- All Possible Steps panel toolbar ---

	/** opens the "All possible Steps" configuration tab. */
	public static final String ALL_STEPS_CONFIGURE = "configure";
	/** the five sort modes; the index is the sort mode itself. */
	public static final String[] ALL_STEPS_SORTS = { "sortDirectSingles", "sortSingles", "sortCells",
			"sortEliminations", "sortType" };

	/** The all-steps toolbar's buttons, in the order the fork ships them. */
	public static final String[] ALL_STEPS = { ALL_STEPS_CONFIGURE, ALL_STEPS_SORTS[0], ALL_STEPS_SORTS[1],
			ALL_STEPS_SORTS[2], ALL_STEPS_SORTS[3], ALL_STEPS_SORTS[4] };

	/**
	 * Visual group of an item. The toolbars separate groups with a separator, the
	 * same way the hand-written code did; deriving the separators from the groups
	 * means a reordered toolbar still looks deliberate instead of losing (or
	 * stranding) its separators.
	 *
	 * @param id the item
	 * @return the group name; items of the same group sit together
	 */
	public static String groupOf(String id) {
		if (ALL_STEPS_CONFIGURE.equals(id)) {
			return "configure";
		}
		for (String sort : ALL_STEPS_SORTS) {
			if (sort.equals(id)) {
				return "sort";
			}
		}
		// every button of the main toolbar block belongs to the filter group
		return "filter";
	}

	/**
	 * The name shown in the configuration list.
	 *
	 * @param id the item
	 * @return the localized name, or the raw id if the bundle has no entry
	 */
	public static String nameOf(String id) {
		try {
			return ResourceBundle.getBundle(BUNDLE).getString("ConfigToolBarPanel.item." + id);
		} catch (MissingResourceException ex) {
			return id;
		}
	}

	/**
	 * The classpath resource of the icon the button wears in the toolbar. The
	 * configuration list shows it next to the name, because every one of these
	 * buttons is icon-only: a list of names alone would not tell the user which
	 * button is which.
	 *
	 * @param id the item
	 * @return the resource path, or null if the item has no icon
	 */
	public static String iconOf(String id) {
		if (MAIN_FILTER_MODE.equals(id)) {
			return "/img/rgSelected1.png";
		}
		if (MAIN_BIVALUE.equals(id)) {
			return "/img/f_xyc.png";
		}
		for (int i = 0; i < MAIN_DIGITS.length; i++) {
			if (MAIN_DIGITS[i].equals(id)) {
				return "/img/f_" + (i + 1) + "c.png";
			}
		}
		if (ALL_STEPS_CONFIGURE.equals(id)) {
			return "/img/settings.png";
		}
		if (ALL_STEPS_SORTS[0].equals(id)) {
			return "/img/search_d1.png";
		}
		if (ALL_STEPS_SORTS[1].equals(id)) {
			return "/img/search_s1.png";
		}
		if (ALL_STEPS_SORTS[2].equals(id)) {
			return "/img/search_c1.png";
		}
		if (ALL_STEPS_SORTS[3].equals(id)) {
			return "/img/search_e1.png";
		}
		if (ALL_STEPS_SORTS[4].equals(id)) {
			return "/img/search_t1.png";
		}
		return null;
	}
}
