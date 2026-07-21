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
 * Regime of a bent-set style technique, classified by the number of
 * non-restricted candidates of the set (see docs/estrategia-taxonomia.md §1).
 * A candidate is RESTRICTED if all its occurrences inside the set share a
 * sector. The two regimes are mutually exclusive over the same cells.
 */
public enum Regime {
	/**
	 * Exactly one non-restricted candidate Z ("wing" regime): only Z is
	 * eliminated, from the cells that see every Z of the set. XY-Wing,
	 * XYZ-Wing, the bent subsets n=4..9; in ALS algebra an ALS-XZ with a
	 * single RCC.
	 */
	R1,
	/**
	 * Zero non-restricted candidates ("locked" regime): every digit is
	 * eliminated from the rest of its confining sector. Sue de Coq and the
	 * future extended SDC family; in ALS algebra a doubly linked ALS-XZ
	 * (2 RCCs); rank 0.
	 */
	R0
}
