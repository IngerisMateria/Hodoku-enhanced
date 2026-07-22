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
package registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.awt.Point;
import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sudoku.Options;

/**
 * Custody of the popup position memory store (milestone 1.9, P-003): the
 * per-dialog location map encodes, overwrites and round-trips through the hcfg
 * so a moved dialog reopens where it was left even after an app restart. The
 * global {@code DialogPositionMemory} listener is Swing/EDT wiring on top of
 * this store and is verified in the GUI, not headless.
 */
public class DialogLocationMemoryTest {

	private File file;

	@BeforeEach
	public void resetOptions() {
		Options.resetAll();
	}

	@AfterEach
	public void cleanUp() {
		if (file != null) {
			file.delete();
			file = null;
		}
		Options.resetAll();
	}

	@Test
	public void unknownDialogHasNoStoredLocation() {
		assertNull(Options.getInstance().getDialogLocation("sudoku.ConfigDialog"));
	}

	@Test
	public void storesOverwritesAndKeepsSeveralDialogs() {
		Options options = Options.getInstance();
		options.setDialogLocation("sudoku.ConfigDialog", new Point(100, 200));
		options.setDialogLocation("sudoku.HistoryDialog", new Point(30, 40));
		assertEquals(new Point(100, 200), options.getDialogLocation("sudoku.ConfigDialog"));
		assertEquals(new Point(30, 40), options.getDialogLocation("sudoku.HistoryDialog"));
		// overwrite keeps the other entry intact
		options.setDialogLocation("sudoku.ConfigDialog", new Point(300, 400));
		assertEquals(new Point(300, 400), options.getDialogLocation("sudoku.ConfigDialog"));
		assertEquals(new Point(30, 40), options.getDialogLocation("sudoku.HistoryDialog"));
	}

	@Test
	public void locationsRoundTripThroughConfigFile() throws Exception {
		Options.getInstance().setDialogLocation("sudoku.ConfigDialog", new Point(123, 456));
		file = File.createTempFile("dialog-loc-", ".hcfg");
		Options.getInstance().writeOptions(file.getAbsolutePath());

		Options.readOptions(file.getAbsolutePath());
		assertEquals(new Point(123, 456), Options.getInstance().getDialogLocation("sudoku.ConfigDialog"),
				"a remembered dialog location must survive a write/read cycle");
	}
}
