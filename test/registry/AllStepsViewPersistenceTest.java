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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sudoku.Options;

/**
 * Custody of the All Possible Steps view preference (milestone 1.9, prompt
 * point 4): the flat-list / folder toggle is remembered — it round-trips
 * through the hcfg write/read so it survives dialog reopens and app restarts.
 */
public class AllStepsViewPersistenceTest {

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
	public void viewPreferenceRoundTrips() throws Exception {
		// default is the folder (tree) view
		assertFalse(Options.getInstance().isAllStepsShowList(), "default view must be folders");
		Options.getInstance().setAllStepsShowList(true);
		file = File.createTempFile("allsteps-view-", ".hcfg");
		Options.getInstance().writeOptions(file.getAbsolutePath());

		Options.readOptions(file.getAbsolutePath());
		assertTrue(Options.getInstance().isAllStepsShowList(),
				"the remembered list view must survive a write/read cycle");
	}
}
