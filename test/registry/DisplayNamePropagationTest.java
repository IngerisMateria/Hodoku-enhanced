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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import solver.modern.registry.TechniqueRegistry;
import sudoku.Options;
import sudoku.SolutionStep;
import sudoku.SolutionType;
import sudoku.StepConfig;

/**
 * The preferred display name must reach every user-facing name source
 * (follow-up of milestone 1.5): the legacy hint pipeline resolves step names
 * through {@link SolutionStep#getStepName}, config lists through
 * {@link StepConfig#toString()}, and both go through the registry — while the
 * enum/library identities stay on the legacy names.
 */
public class DisplayNamePropagationTest {

	@BeforeEach
	public void resetOptions() {
		Options.resetAll();
	}

	@AfterEach
	public void restoreOptions() {
		Options.getInstance().setTechniqueDisplayNames("");
		Options.resetAll();
	}

	@Test
	public void legacyStepNamesFollowThePreferredDisplayName() {
		TechniqueRegistry registry = TechniqueRegistry.getInstance();
		String custom = "Doble Sashimi";
		registry.setPreferredDisplayName(SolutionType.SKYSCRAPER, custom);

		// the three legacy name entry points of the hint pipeline
		assertEquals(custom, SolutionStep.getStepName(SolutionType.SKYSCRAPER));
		assertEquals(custom, SolutionStep.getStepName(SolutionType.SKYSCRAPER.ordinal()));
		SolutionStep step = new SolutionStep(SolutionType.SKYSCRAPER);
		assertEquals(custom, step.getStepName());
		// the full hint text (vague/concrete hint dialogs, hint text area,
		// solution path) starts with the preferred name
		assertTrue(step.toString(0).contains(custom),
				"toString(0) must use the preferred name: " + step.toString(0));

		// config lists render through StepConfig.toString()
		StepConfig config = SolutionType.SKYSCRAPER.getStepConfig();
		assertEquals(custom, config.toString());

		// identities never change: enum name, library code, intl step name
		assertEquals("SKYSCRAPER", SolutionType.SKYSCRAPER.name());
		assertEquals("0400", SolutionType.SKYSCRAPER.getLibraryType());

		// removing the preference restores the default everywhere
		registry.setPreferredDisplayName(SolutionType.SKYSCRAPER, null);
		assertEquals(SolutionType.SKYSCRAPER.getStepName(), SolutionStep.getStepName(SolutionType.SKYSCRAPER));
	}
}
