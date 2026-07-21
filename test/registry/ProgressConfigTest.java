/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.8).
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.EnumSet;

import javax.swing.DefaultListModel;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import solver.modern.registry.TechniqueInfo;
import solver.modern.registry.TechniqueRegistry;
import sudoku.ConfigProgressPanel;
import sudoku.Options;
import sudoku.SolutionType;
import sudoku.StepConfig;

/**
 * Custody tests of the progress-rating configuration surface (milestone 1.8,
 * Parte A):
 * <ul>
 * <li>the Progress tab lists EVERY configured technique — the legacy panel
 * silently filtered out all EXTREME techniques (A3); a future technique or a
 * reintroduced filter breaks this test;</li>
 * <li>{@link Options#MODERN_TECHNIQUES} stays consistent with the registry
 * (every technique whose engine lives in {@code solver.modern} is in the set
 * and vice versa);</li>
 * <li>every modern technique ships with {@code enabledProgress=true} and
 * {@code indexProgress} mirroring the solver order (A2);</li>
 * <li>the one-shot migration of pre-1.8 configs forces the modern progress
 * flags exactly once and preserves post-1.8 user choices.</li>
 * </ul>
 */
public class ProgressConfigTest {

	private static final EnumSet<SolutionType> PSEUDO_STEPS = EnumSet.of(SolutionType.INCOMPLETE,
			SolutionType.GIVE_UP);

	@BeforeAll
	public static void resetOptions() {
		Options.resetAll();
	}

	/** Every configured technique except the pseudo steps. */
	private static EnumSet<SolutionType> configuredTechniques() {
		EnumSet<SolutionType> expected = EnumSet.noneOf(SolutionType.class);
		for (StepConfig config : Options.DEFAULT_SOLVER_STEPS) {
			if (!PSEUDO_STEPS.contains(config.getType())) {
				expected.add(config.getType());
			}
		}
		return expected;
	}

	@Test
	public void progressTabListsEveryConfiguredTechnique() throws Exception {
		Options.resetAll();
		ConfigProgressPanel panel = new ConfigProgressPanel();

		Field modelField = ConfigProgressPanel.class.getDeclaredField("model");
		modelField.setAccessible(true);
		@SuppressWarnings("unchecked")
		DefaultListModel<StepConfig> model = (DefaultListModel<StepConfig>) modelField.get(panel);

		EnumSet<SolutionType> listed = EnumSet.noneOf(SolutionType.class);
		for (int i = 0; i < model.getSize(); i++) {
			listed.add(model.getElementAt(i).getType());
		}
		EnumSet<SolutionType> expected = configuredTechniques();
		EnumSet<SolutionType> missing = EnumSet.copyOf(expected);
		missing.removeAll(listed);
		assertTrue(missing.isEmpty(), "techniques missing from the Progress tab list: " + missing);
		EnumSet<SolutionType> extra = EnumSet.copyOf(listed);
		extra.removeAll(expected);
		assertTrue(extra.isEmpty(), "pseudo steps leaked into the Progress tab list: " + extra);
	}

	@Test
	public void progressTreeCoversEveryConfiguredTechnique() throws Exception {
		Options.resetAll();
		ConfigProgressPanel panel = new ConfigProgressPanel();
		panel.buildTree();

		Field treeField = ConfigProgressPanel.class.getDeclaredField("stepTree");
		treeField.setAccessible(true);
		javax.swing.JTree tree = (javax.swing.JTree) treeField.get(panel);
		Object root = tree.getModel().getRoot();

		EnumSet<SolutionType> listed = EnumSet.noneOf(SolutionType.class);
		collectTreeSteps(tree, root, listed);
		assertEquals(configuredTechniques(), listed,
				"the Progress tab tree must cover exactly the configured techniques");
	}

	private static void collectTreeSteps(javax.swing.JTree tree, Object node, EnumSet<SolutionType> out)
			throws Exception {
		Field stepField = sudoku.CheckNode.class.getDeclaredField("step");
		stepField.setAccessible(true);
		int count = tree.getModel().getChildCount(node);
		for (int i = 0; i < count; i++) {
			Object child = tree.getModel().getChild(node, i);
			if (child instanceof sudoku.CheckNode) {
				StepConfig step = (StepConfig) stepField.get(child);
				if (step != null) {
					out.add(step.getType());
				}
			}
			collectTreeSteps(tree, child, out);
		}
	}

	@Test
	public void modernTechniquesSetMatchesRegistryEngines() {
		TechniqueRegistry registry = TechniqueRegistry.getInstance();
		for (SolutionType type : configuredTechniques()) {
			TechniqueInfo info = registry.get(type);
			if (info == null) {
				continue; // completeness is TechniqueRegistryTest territory
			}
			boolean modernEngine = isModernEngine(info.getEngine());
			assertEquals(modernEngine, Options.MODERN_TECHNIQUES.contains(type),
					"Options.MODERN_TECHNIQUES out of sync with registry engine for " + type + " (engine "
							+ info.getEngine() + ")");
		}
	}

	/** true if the engine class lives in the solver.modern package. */
	private static boolean isModernEngine(String engine) {
		try {
			Class.forName("solver.modern." + engine);
			return true;
		} catch (ClassNotFoundException ex) {
			return false;
		}
	}

	@Test
	public void modernDefaultsAreEnabledInProgressAndMirrorSolverOrder() {
		EnumSet<SolutionType> seen = EnumSet.noneOf(SolutionType.class);
		for (StepConfig config : Options.DEFAULT_SOLVER_STEPS) {
			if (!Options.MODERN_TECHNIQUES.contains(config.getType())) {
				continue;
			}
			seen.add(config.getType());
			assertTrue(config.isEnabledProgress(),
					"modern technique must ship with enabledProgress=true (A2): " + config.getType());
			assertEquals(config.getIndex(), config.getIndexProgress(),
					"modern technique must mirror the solver order in indexProgress (A2): " + config.getType());
		}
		assertEquals(Options.MODERN_TECHNIQUES, seen, "modern techniques without a default StepConfig");
	}

	@Test
	public void migrationForcesModernFlagsOnceAndPreservesLaterChoices() {
		Options options = new Options();
		// simulate a pre-1.8 config file: modern entries carry
		// enabledProgress=false, the migration flag property is absent (false)
		for (StepConfig config : options.getOrgSolverSteps()) {
			if (Options.MODERN_TECHNIQUES.contains(config.getType())) {
				config.setEnabledProgress(false);
			}
		}
		options.setModernProgressFlagsMigrated(false);
		options.migrateModernProgressFlags();
		assertTrue(options.isModernProgressFlagsMigrated(), "migration must set the one-shot flag");
		for (StepConfig config : options.getOrgSolverSteps()) {
			if (Options.MODERN_TECHNIQUES.contains(config.getType())) {
				assertTrue(config.isEnabledProgress(), "migration must force enabledProgress: " + config.getType());
				assertEquals(config.getIndex(), config.getIndexProgress(),
						"migration must mirror the solver order: " + config.getType());
			}
		}
		// simulate a post-1.8 user choice: disable one modern technique for
		// progress; a second migration run must NOT clobber it
		for (StepConfig config : options.getOrgSolverSteps()) {
			if (config.getType() == SolutionType.TRIDAGON) {
				config.setEnabledProgress(false);
			}
		}
		options.migrateModernProgressFlags();
		for (StepConfig config : options.getOrgSolverSteps()) {
			if (config.getType() == SolutionType.TRIDAGON) {
				assertFalse(config.isEnabledProgress(),
						"a post-1.8 user choice must survive later loads (one-shot migration)");
			}
		}
	}
}
