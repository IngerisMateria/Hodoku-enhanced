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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import sudoku.Options;
import sudoku.SolutionType;
import sudoku.StepConfig;

/**
 * Migration of pre-desglose hcfg files for the whole guardian Uniqueness Pack
 * (milestone 1.9): the single generic Unique Loop / Extended UR / BUG-Lite / MUG
 * StepConfig is replaced on load by its Type 1 / Type 2 entries, preserving
 * everything the user could have changed, and the migrated config round-trips
 * through write/read unchanged. Mirrors {@code KrakenConfigMigrationTest}.
 * Reverse BUG has no subtypes and is not migrated.
 */
public class UniquenessPackConfigMigrationTest {

	static List<Arguments> splits() {
		return List.of(
				Arguments.of(SolutionType.UNIQUE_LOOP, SolutionType.UNIQUE_LOOP_TYPE_1, SolutionType.UNIQUE_LOOP_TYPE_2),
				Arguments.of(SolutionType.EXTENDED_UR, SolutionType.EXTENDED_UR_TYPE_1, SolutionType.EXTENDED_UR_TYPE_2),
				Arguments.of(SolutionType.BUG_LITE, SolutionType.BUG_LITE_TYPE_1, SolutionType.BUG_LITE_TYPE_2),
				Arguments.of(SolutionType.MUG, SolutionType.MUG_TYPE_1, SolutionType.MUG_TYPE_2));
	}

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
		Options.resetAll();
	}

	private File tempFile(String suffix) throws Exception {
		File file = File.createTempFile("uniqueness-pack-migration-", suffix);
		tempFiles.add(file);
		return file;
	}

	/** Rebuilds orgSolverSteps as a pre-desglose config would have written it. */
	private static void makePreSplitConfig(SolutionType generic, SolutionType type1, SolutionType type2, boolean enabled,
			int baseScore) {
		Options options = Options.getInstance();
		List<StepConfig> steps = new ArrayList<>(Arrays.asList(options.getOrgSolverSteps()));
		StepConfig old = null;
		for (int i = 0; i < steps.size(); i++) {
			StepConfig step = steps.get(i);
			if (step.getType() == type1) {
				// the old generic entry lived at the slot of Type 1
				old = new StepConfig(step.getIndex(), generic, step.getLevel(), step.getCategory(), baseScore,
						step.getAdminScore(), enabled, step.isAllStepsEnabled(), step.getIndexProgress(),
						step.isEnabledProgress(), step.isEnabledTraining());
				steps.set(i, old);
			}
		}
		assertNotNull(old, "test setup: Type 1 entry not found in defaults for " + generic);
		steps.removeIf(step -> step.getType() == type2);
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

	@ParameterizedTest
	@MethodSource("splits")
	public void preSplitConfigMigratesPreservingEnabledAndScore(SolutionType generic, SolutionType type1,
			SolutionType type2) throws Exception {
		makePreSplitConfig(generic, type1, type2, true, 1234);
		File file = tempFile(".hcfg");
		Options.getInstance().writeOptions(file.getAbsolutePath());

		Options.readOptions(file.getAbsolutePath());
		Options migrated = Options.getInstance();

		assertNull(find(migrated.getOrgSolverSteps(), generic), "the generic entry must be gone after migration");
		StepConfig c1 = find(migrated.getOrgSolverSteps(), type1);
		StepConfig c2 = find(migrated.getOrgSolverSteps(), type2);
		assertNotNull(c1, "Type 1 entry missing after migration: " + type1);
		assertNotNull(c2, "Type 2 entry missing after migration: " + type2);
		assertTrue(c1.isEnabled(), "Type 1 must inherit enabled");
		assertTrue(c2.isEnabled(), "Type 2 must inherit enabled");
		assertEquals(1234, c1.getBaseScore(), "Type 1 must inherit the score");
		assertEquals(1234, c2.getBaseScore(), "Type 2 must inherit the score");
		assertTrue(c1.getIndex() < c2.getIndex(), "Type 1 must sort before Type 2");
		assertNull(find(migrated.solverSteps, generic));
		assertNotNull(find(migrated.solverSteps, type1));
		assertNotNull(find(migrated.solverSteps, type2));
	}

	@ParameterizedTest
	@MethodSource("splits")
	public void migratedConfigRoundTripsUnchanged(SolutionType generic, SolutionType type1, SolutionType type2)
			throws Exception {
		makePreSplitConfig(generic, type1, type2, true, 777);
		File file1 = tempFile(".hcfg");
		Options.getInstance().writeOptions(file1.getAbsolutePath());
		Options.readOptions(file1.getAbsolutePath());

		File file2 = tempFile(".hcfg");
		Options.getInstance().writeOptions(file2.getAbsolutePath());
		Options.readOptions(file2.getAbsolutePath());
		Options roundTripped = Options.getInstance();

		assertNull(find(roundTripped.getOrgSolverSteps(), generic));
		assertNotNull(find(roundTripped.getOrgSolverSteps(), type1));
		assertNotNull(find(roundTripped.getOrgSolverSteps(), type2));
		assertEquals(Options.DEFAULT_SOLVER_STEPS.length, roundTripped.getOrgSolverSteps().length,
				"migrated config must have the default number of entries");
	}
}
