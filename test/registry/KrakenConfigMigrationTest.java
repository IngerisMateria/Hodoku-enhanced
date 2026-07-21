/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.5).
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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import sudoku.Options;
import sudoku.SolutionType;
import sudoku.StepConfig;

/**
 * Migration of pre-split hcfg files (milestone 1.5, P-002): the single generic
 * KRAKEN_FISH StepConfig is replaced on load by KRAKEN_FISH_TYPE_1/2 entries
 * that preserve everything the user could have changed, and the migrated
 * config round-trips through write/read without further changes.
 */
public class KrakenConfigMigrationTest {

	private final List<File> tempFiles = new ArrayList<>();

	@BeforeEach
	public void resetOptions() {
		Options.resetAll();
	}

	@AfterEach
	public void cleanUp() {
		for (File file : tempFiles) {
			file.delete();
		}
		tempFiles.clear();
		// other tests must not see the mutated singleton
		Options.resetAll();
	}

	private File tempFile(String suffix) throws Exception {
		File file = File.createTempFile("kraken-migration-", suffix);
		tempFiles.add(file);
		return file;
	}

	/** Rebuilds orgSolverSteps as a pre-split config would have written it. */
	private static void makePreSplitConfig(boolean enabled, int baseScore) {
		Options options = Options.getInstance();
		List<StepConfig> steps = new ArrayList<>(Arrays.asList(options.getOrgSolverSteps()));
		StepConfig generic = null;
		for (int i = 0; i < steps.size(); i++) {
			StepConfig step = steps.get(i);
			if (step.getType() == SolutionType.KRAKEN_FISH_TYPE_1) {
				// the old generic entry lived at the slot of Type 1 (index 8450)
				generic = new StepConfig(step.getIndex(), SolutionType.KRAKEN_FISH, step.getLevel(),
						step.getCategory(), baseScore, step.getAdminScore(), enabled, step.isAllStepsEnabled(),
						step.getIndexProgress(), step.isEnabledProgress(), step.isEnabledTraining());
				steps.set(i, generic);
			}
		}
		assertNotNull(generic, "test setup: Type 1 entry not found in defaults");
		steps.removeIf(step -> step.getType() == SolutionType.KRAKEN_FISH_TYPE_2);
		options.setOrgSolverSteps(steps.toArray(new StepConfig[0]));
	}

	private static StepConfig find(StepConfig[] configs, SolutionType type) {
		for (StepConfig config : configs) {
			if (config.getType() == type) {
				return config;
			}
		}
		return null;
	}

	@Test
	public void preSplitConfigMigratesPreservingEnabledAndScore() throws Exception {
		makePreSplitConfig(true, 1234);
		File file = tempFile(".hcfg");
		Options.getInstance().writeOptions(file.getAbsolutePath());

		Options.readOptions(file.getAbsolutePath());
		Options migrated = Options.getInstance();

		assertNull(find(migrated.getOrgSolverSteps(), SolutionType.KRAKEN_FISH),
				"the generic entry must be gone after migration");
		StepConfig type1 = find(migrated.getOrgSolverSteps(), SolutionType.KRAKEN_FISH_TYPE_1);
		StepConfig type2 = find(migrated.getOrgSolverSteps(), SolutionType.KRAKEN_FISH_TYPE_2);
		assertNotNull(type1, "Type 1 entry missing after migration");
		assertNotNull(type2, "Type 2 entry missing after migration");
		assertTrue(type1.isEnabled(), "Type 1 must inherit enabled");
		assertTrue(type2.isEnabled(), "Type 2 must inherit enabled");
		assertEquals(1234, type1.getBaseScore(), "Type 1 must inherit the score");
		assertEquals(1234, type2.getBaseScore(), "Type 2 must inherit the score");
		assertTrue(type1.getIndex() < type2.getIndex(), "Type 1 must sort before Type 2");

		// the working copies must see the migrated entries too
		assertNotNull(find(migrated.solverSteps, SolutionType.KRAKEN_FISH_TYPE_1));
		assertNotNull(find(migrated.solverSteps, SolutionType.KRAKEN_FISH_TYPE_2));
		assertNull(find(migrated.solverSteps, SolutionType.KRAKEN_FISH));
	}

	@Test
	public void migratedConfigRoundTripsUnchanged() throws Exception {
		makePreSplitConfig(true, 777);
		File file1 = tempFile(".hcfg");
		Options.getInstance().writeOptions(file1.getAbsolutePath());
		Options.readOptions(file1.getAbsolutePath());

		// write the migrated config and read it back: idempotent
		File file2 = tempFile(".hcfg");
		Options.getInstance().writeOptions(file2.getAbsolutePath());
		Options.readOptions(file2.getAbsolutePath());
		Options roundTripped = Options.getInstance();

		assertNull(find(roundTripped.getOrgSolverSteps(), SolutionType.KRAKEN_FISH));
		StepConfig type1 = find(roundTripped.getOrgSolverSteps(), SolutionType.KRAKEN_FISH_TYPE_1);
		StepConfig type2 = find(roundTripped.getOrgSolverSteps(), SolutionType.KRAKEN_FISH_TYPE_2);
		assertNotNull(type1);
		assertNotNull(type2);
		assertTrue(type1.isEnabled());
		assertTrue(type2.isEnabled());
		assertEquals(777, type1.getBaseScore());
		assertEquals(777, type2.getBaseScore());
		assertEquals(Options.getInstance().getOrgSolverSteps().length,
				Options.DEFAULT_SOLVER_STEPS.length, "migrated config must have the default number of entries");
	}

	@Test
	public void postSplitConfigIsLeftAlone() throws Exception {
		// current defaults already hold the split entries: write/read must not
		// duplicate or drop anything
		File file = tempFile(".hcfg");
		Options.getInstance().writeOptions(file.getAbsolutePath());
		Options.readOptions(file.getAbsolutePath());
		StepConfig[] steps = Options.getInstance().getOrgSolverSteps();
		assertNull(find(steps, SolutionType.KRAKEN_FISH));
		assertNotNull(find(steps, SolutionType.KRAKEN_FISH_TYPE_1));
		assertNotNull(find(steps, SolutionType.KRAKEN_FISH_TYPE_2));
		assertEquals(Options.DEFAULT_SOLVER_STEPS.length, steps.length);
	}
}
