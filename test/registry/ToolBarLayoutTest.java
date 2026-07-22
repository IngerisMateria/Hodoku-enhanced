/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.10).
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sudoku.Options;
import sudoku.ui.ToolBarItems;
import sudoku.ui.ToolBarLayout;

/**
 * Custody of the configurable toolbars (milestone 1.10, P-009 Part 1/2). Headless
 * on purpose: everything the user can do to a toolbar is a operation on the
 * layout model, so the model is what gets tested — no Swing involved.
 */
public class ToolBarLayoutTest {

	private static final String[] IDS = { "a", "b", "c" };

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
	public void emptyPreferenceMeansEverythingShownInCatalogueOrder() {
		ToolBarLayout layout = ToolBarLayout.parse("", IDS);
		assertEquals(Arrays.asList("a", "b", "c"), layout.getShown());
		assertTrue(layout.getAvailable().isEmpty());
		// a null string (never written) must behave the same way
		assertEquals(Arrays.asList("a", "b", "c"), ToolBarLayout.parse(null, IDS).getShown());
	}

	@Test
	public void hidingAndShowingIsTheSameList() {
		ToolBarLayout layout = ToolBarLayout.parse("", IDS);
		assertTrue(layout.hide("b"));
		assertEquals(Arrays.asList("a", "c"), layout.getShown());
		assertEquals(Arrays.asList("b"), layout.getAvailable(), "a hidden button stays available");
		assertFalse(layout.hide("b"), "hiding twice does nothing");
		assertTrue(layout.show("b"));
		assertEquals(Arrays.asList("a", "c", "b"), layout.getShown());
		assertTrue(layout.getAvailable().isEmpty());
	}

	@Test
	public void movingRespectsTheEdges() {
		ToolBarLayout layout = ToolBarLayout.parse("", IDS);
		assertFalse(layout.moveUp("a"), "the first button cannot move up");
		assertFalse(layout.moveDown("c"), "the last button cannot move down");
		assertTrue(layout.moveDown("a"));
		assertEquals(Arrays.asList("b", "a", "c"), layout.getShown());
		assertTrue(layout.moveUp("c"));
		assertEquals(Arrays.asList("b", "c", "a"), layout.getShown());
	}

	@Test
	public void resetRestoresTheCatalogueOrder() {
		ToolBarLayout layout = ToolBarLayout.parse("", IDS);
		layout.hide("a");
		layout.moveDown("b");
		layout.reset();
		assertEquals(Arrays.asList("a", "b", "c"), layout.getShown());
		assertTrue(layout.getAvailable().isEmpty());
	}

	@Test
	public void formatRoundTrips() {
		ToolBarLayout layout = ToolBarLayout.parse("", IDS);
		layout.hide("a");
		layout.moveDown("b");
		String raw = layout.format();
		ToolBarLayout reread = ToolBarLayout.parse(raw, IDS);
		assertEquals(layout.getShown(), reread.getShown());
		assertEquals(layout.getAvailable(), reread.getAvailable());
	}

	@Test
	public void unknownIdsAreDroppedAndNewOnesAppended() {
		// "z" is a button this build does not have; "c" is one the stored string
		// never heard of. Both cases are the migration path when the catalogue
		// changes, and neither may lose the user's arrangement.
		ToolBarLayout layout = ToolBarLayout.parse("b=1,z=1,a=0", IDS);
		assertEquals(Arrays.asList("b", "c"), layout.getShown(), "new buttons are appended as shown");
		assertEquals(Arrays.asList("a"), layout.getAvailable(), "the hidden one stays hidden");
	}

	@Test
	public void catalogueIdsAreUniqueAndNamed() {
		for (String[] bar : new String[][] { ToolBarItems.MAIN, ToolBarItems.ALL_STEPS }) {
			List<String> ids = Arrays.asList(bar);
			assertEquals(ids.size(), new java.util.HashSet<String>(ids).size(), "ids must be unique: " + ids);
			for (String id : ids) {
				assertNotEqualsRaw(id, ToolBarItems.nameOf(id));
				assertNotNull(ToolBarItems.iconOf(id), "every toolbar button has an icon: " + id);
				assertNotNull(ToolBarItems.groupOf(id));
			}
		}
	}

	private static void assertNotEqualsRaw(String id, String name) {
		assertFalse(id.equals(name), "missing display name in intl/ConfigToolBarPanel for " + id);
	}

	@Test
	public void preferencesRoundTripThroughTheOptionsFile() throws Exception {
		assertEquals("", Options.getInstance().getToolBarMainItems(), "default is the shipped toolbar");
		assertEquals("", Options.getInstance().getToolBarAllStepsItems());

		ToolBarLayout main = ToolBarLayout.parse("", ToolBarItems.MAIN);
		main.hide(ToolBarItems.MAIN_DIGITS[4]);
		main.moveUp(ToolBarItems.MAIN_BIVALUE);
		ToolBarLayout allSteps = ToolBarLayout.parse("", ToolBarItems.ALL_STEPS);
		allSteps.hide(ToolBarItems.ALL_STEPS_SORTS[0]);
		Options.getInstance().setToolBarMainItems(main.format());
		Options.getInstance().setToolBarAllStepsItems(allSteps.format());

		file = File.createTempFile("toolbars-", ".hcfg");
		Options.getInstance().writeOptions(file.getAbsolutePath());
		Options.readOptions(file.getAbsolutePath());

		ToolBarLayout reread = ToolBarLayout.parse(Options.getInstance().getToolBarMainItems(), ToolBarItems.MAIN);
		assertEquals(main.getShown(), reread.getShown(), "the arrangement must survive a restart");
		assertEquals(Arrays.asList(ToolBarItems.MAIN_DIGITS[4]), reread.getAvailable());
		assertEquals(allSteps.getShown(),
				ToolBarLayout.parse(Options.getInstance().getToolBarAllStepsItems(), ToolBarItems.ALL_STEPS).getShown());
	}
}
