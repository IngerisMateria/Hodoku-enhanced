/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.4).
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
package solver.modern.registry;

import java.util.Set;

import sudoku.SolutionType;

/**
 * Metadata of one configuration option of {@link sudoku.Options}: what it
 * really does (description derived from reading the consuming code, never
 * guessed), which techniques own it, and the canonical classic tab it lives
 * in today.
 *
 * "Owners" is the configuration-ownership relation ("the behavior of these
 * techniques depends on this option") — the milestone 1.5 aside of a
 * technique shows the options that list it here. It is intentionally
 * separate from the subsumption taxonomy of {@link TechniqueInfo}. A witness
 * case: {@code allowDualsAndSiamese} is owned by 2-String Kite + Skyscraper +
 * Empty Rectangle (dual forms) and by every fish (Siamese).
 *
 * Owners may be empty only for options that are genuinely
 * technique-independent (witness: {@code useZeroInsteadOfDot}, a clipboard
 * format toggle that upstream parked in the Steps tab).
 */
public final class OptionInfo {

	private final String key;
	private final String description;
	private final Set<SolutionType> owners;
	private final ConfigTab tab;

	OptionInfo(String key, String description, Set<SolutionType> owners, ConfigTab tab) {
		this.key = key;
		this.description = description;
		this.owners = owners.isEmpty() ? Set.of() : Set.copyOf(owners);
		this.tab = tab;
	}

	/** Bean property name in {@link sudoku.Options} (e.g. "allowAlsOverlap"). */
	public String getKey() {
		return key;
	}

	/** Plain-language description of the real behavior, from the code. */
	public String getDescription() {
		return description;
	}

	/** Techniques whose behavior depends on this option (may be empty). */
	public Set<SolutionType> getOwners() {
		return owners;
	}

	/** The canonical classic tab where the option currently lives. */
	public ConfigTab getTab() {
		return tab;
	}

	@Override
	public String toString() {
		return "OptionInfo[" + key + " @" + tab + " owners=" + owners.size() + "]";
	}
}
