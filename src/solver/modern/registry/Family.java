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

/**
 * Coarse literature family of a technique — the axis used for grouping in the
 * UI (folder views come in a later milestone; see P-004). This is
 * deliberately NOT the subsumption taxonomy: the taxonomy lives in
 * {@link TechniqueInfo#getSubsumedBy()} and is a multi-parent DAG, while the
 * family is a single pedagogical bucket per technique.
 */
public enum Family {
	SINGLES("Singles"),
	/** Locked Candidates (pointing/claiming). */
	INTERSECTIONS("Intersections"),
	/** Naked/hidden/locked pairs, triples, quads. */
	SUBSETS("Subsets"),
	/** The base/cover fish matrix including kraken. */
	FISH("Fish"),
	/** Single-digit patterns: Skyscraper, 2-String Kite, ER, Turbot Fish. */
	SINGLE_DIGIT_PATTERNS("Single-Digit Patterns"),
	UNIQUENESS("Uniqueness"),
	/** Named wings of fixed shape: XY, XYZ, W, canonical WXYZ. */
	WINGS("Wings"),
	/** Bent naked subsets n=4..9 (the parametric bent engine, regime R1). */
	BENT_SUBSETS("Bent Subsets"),
	/**
	 * Guardian logic over impossible odd cycles (the "rank -1 / dark logic"
	 * branch of the owner's map, docs/sudoku_mapa_relacional.md §2.d):
	 * Broken Wing, Bivalue Oddagon, later Tridagon (milestone 1.7).
	 */
	ODDAGON("Oddagons"),
	COLORING("Coloring"),
	/** Simple chains: X-Chain, XY-Chain, Remote Pair, Nice Loops / AICs. */
	CHAINS("Chains"),
	/** ALS moves incl. Sue de Coq (doubly linked / R0 side of the algebra). */
	ALS("Almost Locked Sets"),
	FORCING("Forcing Chains & Nets"),
	TEMPLATES("Templates"),
	LAST_RESORT("Last Resort");

	private final String folderName;

	Family(String folderName) {
		this.folderName = folderName;
	}

	/**
	 * The label of the visual folder that groups this family in the config
	 * surfaces (milestone 1.9). English-only for now, like
	 * {@link sudoku.SolutionCategory#getCategoryName()}.
	 */
	public String getFolderName() {
		return folderName;
	}
}
