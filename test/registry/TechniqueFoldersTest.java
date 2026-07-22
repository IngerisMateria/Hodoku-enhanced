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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import solver.modern.registry.Family;
import solver.modern.registry.TechniqueFolders;
import sudoku.Options;
import sudoku.SolutionType;
import sudoku.StepConfig;

/**
 * Custody of the folder model (milestone 1.9): the visual grouping the four
 * config surfaces use (solver, all-possible-steps, progress, training) is the
 * registry family. Guards the two folders the owner asked for by name — the
 * "Oddagons" folder gathering Broken Wing, Bivalue Oddagon and Tridagon, and
 * the single "Uniqueness" folder gathering the legacy UR family plus the whole
 * Uniqueness Pack (with Extended UR already split).
 */
public class TechniqueFoldersTest {

	@BeforeAll
	public static void resetOptions() {
		Options.resetAll();
	}

	/** Every configured technique grouped by its folder (family). */
	private static Map<Family, Set<SolutionType>> foldersByConfiguredType() {
		Map<Family, Set<SolutionType>> folders = new EnumMap<>(Family.class);
		for (StepConfig config : Options.DEFAULT_SOLVER_STEPS) {
			Family folder = TechniqueFolders.folderOf(config.getType());
			assertNotNull(folder, "every configured type must resolve to a folder: " + config.getType());
			folders.computeIfAbsent(folder, f -> EnumSet.noneOf(SolutionType.class)).add(config.getType());
		}
		return folders;
	}

	@Test
	public void oddagonsFolderHoldsExactlyTheThreeOddagons() {
		Map<Family, Set<SolutionType>> folders = foldersByConfiguredType();
		assertEquals(
				EnumSet.of(SolutionType.BROKEN_WING, SolutionType.BIVALUE_ODDAGON, SolutionType.TRIDAGON),
				folders.get(Family.ODDAGON),
				"the Oddagons folder must hold exactly Broken Wing, Bivalue Oddagon and Tridagon");
		assertEquals("Oddagons", TechniqueFolders.folderName(Family.ODDAGON));
	}

	@Test
	public void uniquenessFolderHoldsLegacyUrAndTheWholePack() {
		Set<SolutionType> uniqueness = foldersByConfiguredType().get(Family.UNIQUENESS);
		assertNotNull(uniqueness, "the Uniqueness folder must exist");
		// legacy UR family
		for (SolutionType legacy : new SolutionType[] { SolutionType.UNIQUENESS_1, SolutionType.UNIQUENESS_2,
				SolutionType.UNIQUENESS_3, SolutionType.UNIQUENESS_4, SolutionType.UNIQUENESS_5,
				SolutionType.UNIQUENESS_6, SolutionType.HIDDEN_RECTANGLE, SolutionType.AVOIDABLE_RECTANGLE_1,
				SolutionType.AVOIDABLE_RECTANGLE_2, SolutionType.BUG_PLUS_1 }) {
			assertTrue(uniqueness.contains(legacy), "legacy UR type missing from the Uniqueness folder: " + legacy);
		}
		// the pack, with Extended UR split into its two configured subtypes
		for (SolutionType pack : new SolutionType[] { SolutionType.UNIQUE_LOOP, SolutionType.EXTENDED_UR_TYPE_1,
				SolutionType.EXTENDED_UR_TYPE_2, SolutionType.BUG_LITE, SolutionType.REVERSE_BUG, SolutionType.MUG }) {
			assertTrue(uniqueness.contains(pack), "pack technique missing from the Uniqueness folder: " + pack);
		}
		assertEquals("Uniqueness", TechniqueFolders.folderName(Family.UNIQUENESS));
		// Broken Wing lives in Oddagons, never in Uniqueness (single-folder model)
		assertTrue(!uniqueness.contains(SolutionType.BROKEN_WING));
	}

	@Test
	public void pseudoStepsFallBackToLastResort() {
		// INCOMPLETE / GIVE_UP have no registry row; they must still resolve
		assertEquals(Family.LAST_RESORT, TechniqueFolders.folderOf(SolutionType.INCOMPLETE));
		assertEquals(Family.LAST_RESORT, TechniqueFolders.folderOf(SolutionType.GIVE_UP));
	}
}
