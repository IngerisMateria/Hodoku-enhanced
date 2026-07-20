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

/**
 * A (cell, digit) pair: either a value placed in a cell or a candidate
 * eliminated from a cell.
 */
public final class CellValue {

	/** Cell index 0-80 (row * 9 + column). */
	public final int index;
	/** Digit 1-9. */
	public final int value;

	public CellValue(int index, int value) {
		this.index = index;
		this.value = value;
	}

	/** Cell in r#c# notation, 1-based (e.g. index 0 is "r1c1"). */
	public String cell() {
		return "r" + (index / 9 + 1) + "c" + (index % 9 + 1);
	}

	@Override
	public String toString() {
		return cell() + "=" + value;
	}
}
