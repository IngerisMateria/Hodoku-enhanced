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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Which buttons a configurable toolbar shows, and in which order (P-009 Part 1/2,
 * milestone 1.10).
 * <p>
 * The owner's model is a single one: <em>"a set of available buttons; the user
 * picks which ones are shown and in which order"</em>. Show/hide and reorder are
 * therefore not two features but two reads of the same list - a button that is
 * taken out of the toolbar goes back to the pool of available buttons and can be
 * put back later.
 * <p>
 * Persisted form (one string per toolbar, stored in {@code Options}):
 *
 * <pre>
 *   id=1,id=1,id=0,...
 * </pre>
 *
 * where {@code 1} means shown and {@code 0} means available-but-hidden. The
 * catalogue of legal ids lives in {@link ToolBarItems}; ids that are not in the
 * catalogue are dropped on read (a button the fork removed), and catalogue ids
 * that the string does not mention are appended as shown (a button the fork
 * added). That is the whole migration story: an empty string means "defaults",
 * and any older string keeps working.
 * <p>
 * No Swing here on purpose - the class is plain data so it can be unit tested
 * headless.
 */
public final class ToolBarLayout {

	private static final String ENTRY_SEPARATOR = ",";
	private static final String FLAG_SEPARATOR = "=";
	private static final String SHOWN = "1";
	private static final String HIDDEN = "0";

	/** every id this toolbar knows about, in the order the fork ships them. */
	private final List<String> known;
	/** ids currently on the toolbar, in display order. */
	private final List<String> shown = new ArrayList<String>();
	/** ids currently off the toolbar, in the order they are offered. */
	private final List<String> available = new ArrayList<String>();

	private ToolBarLayout(String[] knownIds) {
		this.known = new ArrayList<String>(Arrays.asList(knownIds));
	}

	/**
	 * Reads a stored layout.
	 *
	 * @param raw      the persisted string; null or empty means "defaults"
	 * @param knownIds the catalogue of this toolbar, in default order
	 * @return the layout; never null
	 */
	public static ToolBarLayout parse(String raw, String[] knownIds) {
		ToolBarLayout layout = new ToolBarLayout(knownIds);
		Set<String> catalogue = new LinkedHashSet<String>(layout.known);
		Set<String> seen = new LinkedHashSet<String>();
		if (raw != null) {
			for (String entry : raw.split(ENTRY_SEPARATOR)) {
				entry = entry.trim();
				if (entry.isEmpty()) {
					continue;
				}
				int sep = entry.indexOf(FLAG_SEPARATOR);
				String id = (sep < 0 ? entry : entry.substring(0, sep)).trim();
				boolean visible = sep < 0 || !HIDDEN.equals(entry.substring(sep + 1).trim());
				if (!catalogue.contains(id) || !seen.add(id)) {
					// unknown (removed button) or repeated: ignore
					continue;
				}
				if (visible) {
					layout.shown.add(id);
				} else {
					layout.available.add(id);
				}
			}
		}
		// buttons the stored string never heard of are new: show them
		for (String id : layout.known) {
			if (!seen.contains(id)) {
				layout.shown.add(id);
			}
		}
		return layout;
	}

	/** The default layout of a toolbar: every button shown, in catalogue order. */
	public static ToolBarLayout defaults(String[] knownIds) {
		return parse(null, knownIds);
	}

	/** @return the persisted form of this layout, for {@code Options} */
	public String format() {
		StringBuilder sb = new StringBuilder();
		for (String id : shown) {
			append(sb, id, SHOWN);
		}
		for (String id : available) {
			append(sb, id, HIDDEN);
		}
		return sb.toString();
	}

	private static void append(StringBuilder sb, String id, String flag) {
		if (sb.length() > 0) {
			sb.append(ENTRY_SEPARATOR);
		}
		sb.append(id).append(FLAG_SEPARATOR).append(flag);
	}

	/** @return the ids on the toolbar, in display order (live list, do not modify) */
	public List<String> getShown() {
		return shown;
	}

	/** @return the ids off the toolbar, in the order they are offered */
	public List<String> getAvailable() {
		return available;
	}

	/** @return every id this toolbar knows about, in default order */
	public List<String> getKnown() {
		return known;
	}

	/**
	 * Puts an available button on the toolbar, at the end.
	 *
	 * @param id the button
	 * @return true if something changed
	 */
	public boolean show(String id) {
		if (!available.remove(id)) {
			return false;
		}
		shown.add(id);
		return true;
	}

	/**
	 * Takes a button off the toolbar; it stays available.
	 *
	 * @param id the button
	 * @return true if something changed
	 */
	public boolean hide(String id) {
		if (!shown.remove(id)) {
			return false;
		}
		available.add(id);
		return true;
	}

	/**
	 * Moves a shown button one position towards the start of the toolbar.
	 *
	 * @param id the button
	 * @return true if something changed
	 */
	public boolean moveUp(String id) {
		int index = shown.indexOf(id);
		if (index <= 0) {
			return false;
		}
		shown.remove(index);
		shown.add(index - 1, id);
		return true;
	}

	/**
	 * Moves a shown button one position towards the end of the toolbar.
	 *
	 * @param id the button
	 * @return true if something changed
	 */
	public boolean moveDown(String id) {
		int index = shown.indexOf(id);
		if (index < 0 || index >= shown.size() - 1) {
			return false;
		}
		shown.remove(index);
		shown.add(index + 1, id);
		return true;
	}

	/** Back to "every button shown, in catalogue order". */
	public void reset() {
		shown.clear();
		available.clear();
		shown.addAll(known);
	}
}
