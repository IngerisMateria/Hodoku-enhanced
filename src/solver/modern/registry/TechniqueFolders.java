/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.9).
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

import java.util.List;

import sudoku.SolutionType;

/**
 * The folder model of the configuration surfaces (milestone 1.9): a purely
 * visual grouping of techniques over the {@link TechniqueRegistry}, applied to
 * the four tree views (solver, all-possible-steps, progress, training).
 *
 * <p>Folders are a VIEW, not the solve order — the execution order lives only
 * in the flat exhaustive list (docs/pulido.md P-004). The grouping axis is the
 * registry {@link Family}; the "Oddagons" folder gathers Broken Wing, Bivalue
 * Oddagon and Tridagon, and the "Uniqueness" folder gathers the legacy UR
 * family plus the Uniqueness Pack (with Extended UR already split).
 *
 * <p>The owner's decision allows a technique to appear in several folders. This
 * is modeled by {@link #foldersOf(SolutionType)} returning a list, but every
 * technique currently resolves to exactly one folder (its family): a leaf
 * duplicated across folders would desynchronize the tri-state checkboxes on
 * toggle until the next rebuild, so Broken Wing is placed in Oddagons only
 * (the owner's stated fallback, prompt point 2) rather than also in Wings.
 */
public final class TechniqueFolders {

	private TechniqueFolders() {
	}

	/**
	 * The single visual folder of a technique: its registry family, or
	 * {@link Family#LAST_RESORT} for the rowless pseudo steps
	 * ({@code INCOMPLETE}/{@code GIVE_UP}, both configured at that category), so
	 * the surfaces never crash on a type without a registry row.
	 *
	 * @param type the technique
	 * @return the folder it belongs to
	 */
	public static Family folderOf(SolutionType type) {
		TechniqueInfo info = TechniqueRegistry.getInstance().get(type);
		return info != null ? info.getFamily() : Family.LAST_RESORT;
	}

	/**
	 * The visual folders of a technique. Purely visual; a technique MAY belong
	 * to several, though every technique resolves to exactly one today (see the
	 * class note on the duplicated-leaf caveat). Kept as a list so a future
	 * multi-folder assignment is a one-line change.
	 *
	 * @param type the technique
	 * @return the folders it appears in (never empty)
	 */
	public static List<Family> foldersOf(SolutionType type) {
		return List.of(folderOf(type));
	}

	/**
	 * The label of a folder (the family's display name).
	 *
	 * @param family the folder
	 * @return the folder label
	 */
	public static String folderName(Family family) {
		return family.getFolderName();
	}
}
