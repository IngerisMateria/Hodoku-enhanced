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
package harness;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import solver.SudokuSolverFactory;
import solver.SudokuStepFinder;
import sudoku.FindAllSteps;
import sudoku.Options;
import sudoku.SolutionStep;
import sudoku.SolutionType;
import sudoku.StepConfig;
import sudoku.Sudoku2;

/**
 * The Kraken Type 1/2 split (milestone 1.5, P-002) is honored by the
 * find-all-steps search and by the single-step solver path: on a te3 state
 * that contains kraken fish of both types (fixtures/kraken-split.txt), turning
 * Type 2 off must leave only Type 1 steps, and asking the finder for one type
 * must never return the other.
 */
public class KrakenSplitFixtureTest {

	@BeforeEach
	public void resetOptions() {
		Options.resetAll();
	}

	@AfterEach
	public void restoreOptions() {
		// the test mutates allSteps flags and kraken search params
		Options.resetAll();
	}

	private static Sudoku2 loadFixtureState() {
		List<String> lines = Fixtures.lines("/fixtures/kraken-split.txt");
		assertEquals(1, lines.size(), "kraken-split.txt must hold exactly one state line");
		Sudoku2 sudoku = new Sudoku2();
		sudoku.setSudoku(lines.get(0));
		return sudoku;
	}

	/** Sets allStepsEnabled on every entry: kraken types as given, rest off. */
	private static void configureAllSteps(boolean type1, boolean type2) {
		for (StepConfig config : Options.getInstance().solverSteps) {
			if (config.getType() == SolutionType.KRAKEN_FISH_TYPE_1) {
				config.setAllStepsEnabled(type1);
			} else if (config.getType() == SolutionType.KRAKEN_FISH_TYPE_2) {
				config.setAllStepsEnabled(type2);
			} else {
				config.setAllStepsEnabled(false);
			}
		}
		// keep the search itself within the harvest parameters of the fixture
		Options.getInstance().setAllStepsKrakenMinFishSize(2);
		Options.getInstance().setAllStepsKrakenMaxFishSize(4);
		Options.getInstance().setAllStepsMaxKrakenFins(2);
		Options.getInstance().setAllStepsMaxKrakenEndoFins(0);
		Options.getInstance().setAllStepsKrakenMaxFishType(1);
	}

	private static List<SolutionStep> runFindAllSteps(Sudoku2 sudoku) {
		List<SolutionStep> steps = new ArrayList<>();
		FindAllSteps findAllSteps = new FindAllSteps(steps, sudoku, null);
		findAllSteps.run();
		// run() ends by interrupting its thread (it expects to be a worker);
		// clear the flag so it cannot leak into the test runner
		Thread.interrupted();
		return steps;
	}

	@Test
	public void allStepsWithTypeTwoOffReturnsOnlyTypeOne() {
		Sudoku2 sudoku = loadFixtureState();
		configureAllSteps(true, false);
		List<SolutionStep> steps = runFindAllSteps(sudoku);
		assertFalse(steps.isEmpty(), "the fixture state must yield kraken Type 1 steps");
		for (SolutionStep step : steps) {
			assertEquals(SolutionType.KRAKEN_FISH_TYPE_1, step.getType(),
					"with Type 2 disabled only Type 1 steps may be returned, got: " + step);
		}
	}

	@Test
	public void fixtureStateContainsBothTypes() {
		// guard: the "only Type 1" assertion above is meaningful only if the
		// state actually contains Type 2 steps when both are enabled
		Sudoku2 sudoku = loadFixtureState();
		configureAllSteps(true, true);
		List<SolutionStep> steps = runFindAllSteps(sudoku);
		assertTrue(steps.stream().anyMatch(s -> s.getType() == SolutionType.KRAKEN_FISH_TYPE_1),
				"fixture must contain Type 1 steps");
		assertTrue(steps.stream().anyMatch(s -> s.getType() == SolutionType.KRAKEN_FISH_TYPE_2),
				"fixture must contain Type 2 steps");
	}

	@Test
	public void singleStepFinderHonorsTheRequestedType() {
		Sudoku2 sudoku = loadFixtureState();
		// solver-path search params (the single-step search reads the kraken
		// options, not the allSteps ones)
		Options.getInstance().setKrakenMaxFishType(1);
		Options.getInstance().setKrakenMaxFishSize(4);
		Options.getInstance().setMaxKrakenFins(2);
		Options.getInstance().setMaxKrakenEndoFins(0);
		SudokuStepFinder finder = SudokuSolverFactory.getDefaultSolverInstance().getStepFinder();
		finder.setSudoku(sudoku);
		SolutionStep type1 = finder.getStep(SolutionType.KRAKEN_FISH_TYPE_1);
		assertNotNull(type1, "a Type 1 step must be found on the fixture state");
		assertEquals(SolutionType.KRAKEN_FISH_TYPE_1, type1.getType());
		finder.setSudoku(sudoku);
		SolutionStep type2 = finder.getStep(SolutionType.KRAKEN_FISH_TYPE_2);
		assertNotNull(type2, "a Type 2 step must be found on the fixture state");
		assertEquals(SolutionType.KRAKEN_FISH_TYPE_2, type2.getType());
	}
}
