/*
 * This file is part of the modern-techniques fork of HoDoKu (milestone 1.4).
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
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import solver.modern.registry.OptionInfo;
import solver.modern.registry.TechniqueInfo;
import solver.modern.registry.TechniqueRegistry;
import sudoku.Options;
import sudoku.SolutionType;
import sudoku.StepConfig;

/**
 * Custody tests of the technique/option registry (milestone 1.4):
 * <ul>
 * <li>completeness — every configured technique (StepConfig) has a registry
 * row, so a future technique cannot ship without registering;</li>
 * <li>the subsumption relation is a DAG (no cycles, parents registered);</li>
 * <li>no exact alias collisions between distinct techniques;</li>
 * <li>option rows point at real Options bean properties and registered
 * owners; the display-name preference round-trips through Options.</li>
 * </ul>
 */
public class TechniqueRegistryTest {

	/**
	 * Pseudo steps of the solver state machine, configured but not
	 * techniques: excluded from the completeness contract.
	 */
	private static final Set<SolutionType> PSEUDO_STEPS = EnumSet.of(SolutionType.INCOMPLETE, SolutionType.GIVE_UP);

	/**
	 * Options that are genuinely technique-independent and therefore may
	 * have no owners (documented in their OptionInfo description).
	 */
	private static final Set<String> OWNERLESS_OPTIONS = Set.of("useZeroInsteadOfDot");

	@BeforeAll
	public static void resetOptions() {
		// Options.getInstance() would silently load a hodoku.hcfg from
		// java.io.tmpdir; reset for machine-independent results (same
		// contract as HarnessRunner).
		Options.resetAll();
	}

	@Test
	public void everyConfiguredTechniqueHasARegistryRow() {
		TechniqueRegistry registry = TechniqueRegistry.getInstance();
		List<String> missing = new ArrayList<>();
		for (StepConfig config : Options.DEFAULT_SOLVER_STEPS) {
			SolutionType type = config.getType();
			if (PSEUDO_STEPS.contains(type)) {
				continue;
			}
			if (registry.get(type) == null) {
				missing.add(type.name());
			}
		}
		assertTrue(missing.isEmpty(),
				"configured techniques without a registry row (register them in TechniqueCatalog): " + missing);
	}

	@Test
	public void everyRegistryRowIsAConfiguredTechnique() {
		EnumSet<SolutionType> configured = EnumSet.noneOf(SolutionType.class);
		for (StepConfig config : Options.DEFAULT_SOLVER_STEPS) {
			configured.add(config.getType());
		}
		for (TechniqueInfo info : TechniqueRegistry.getInstance().all()) {
			assertTrue(configured.contains(info.getId()),
					"registry row without a StepConfig (stale entry?): " + info.getId());
		}
	}

	@Test
	public void registryRowsAreWellFormed() {
		for (TechniqueInfo info : TechniqueRegistry.getInstance().all()) {
			assertNotNull(info.getFamily(), "family missing: " + info.getId());
			assertFalse(info.getEngine().isEmpty(), "engine missing: " + info.getId());
			assertFalse(info.getDescription().isEmpty(), "description missing: " + info.getId());
			assertEquals(info.getId().getStepName(), info.getDisplayNameDefault(),
					"default display name must be the legacy intl name: " + info.getId());
			for (String alias : info.getAliases()) {
				assertFalse(alias.isEmpty(), "empty alias: " + info.getId());
				assertFalse(alias.contains("=") || alias.contains(";"),
						"alias contains a reserved character ('=' or ';'): " + info.getId() + " \"" + alias + "\"");
			}
		}
	}

	@Test
	public void subsumptionParentsAreRegistered() {
		TechniqueRegistry registry = TechniqueRegistry.getInstance();
		for (TechniqueInfo info : registry.all()) {
			for (SolutionType parent : info.getSubsumedBy()) {
				assertNotNull(registry.get(parent),
						"subsumption parent of " + info.getId() + " is not a registry row: " + parent);
				assertFalse(parent == info.getId(), "self-subsumption: " + info.getId());
			}
		}
	}

	@Test
	public void subsumptionIsAcyclic() {
		TechniqueRegistry registry = TechniqueRegistry.getInstance();
		// iterative DFS with three colors; any back edge is a cycle
		Map<SolutionType, Integer> color = new EnumMap<>(SolutionType.class); // 1 = visiting, 2 = done
		for (TechniqueInfo info : registry.all()) {
			if (color.getOrDefault(info.getId(), 0) != 0) {
				continue;
			}
			Deque<SolutionType> stack = new ArrayDeque<>();
			stack.push(info.getId());
			while (!stack.isEmpty()) {
				SolutionType current = stack.peek();
				int state = color.getOrDefault(current, 0);
				if (state == 0) {
					color.put(current, 1);
					for (SolutionType parent : registry.get(current).getSubsumedBy()) {
						int parentState = color.getOrDefault(parent, 0);
						if (parentState == 1) {
							fail("subsumption cycle through " + current + " -> " + parent);
						}
						if (parentState == 0) {
							stack.push(parent);
						}
					}
				} else {
					color.put(current, 2);
					stack.pop();
				}
			}
		}
	}

	@Test
	public void noExactAliasCollisionsBetweenTechniques() {
		Map<String, SolutionType> seen = new HashMap<>();
		for (TechniqueInfo info : TechniqueRegistry.getInstance().all()) {
			List<String> names = new ArrayList<>(info.getAliases());
			names.add(info.getDisplayNameDefault());
			for (String name : names) {
				String key = name.toLowerCase(Locale.ROOT);
				SolutionType other = seen.put(key, info.getId());
				assertTrue(other == null || other == info.getId(),
						"alias collision: \"" + name + "\" used by " + other + " and " + info.getId());
			}
		}
	}

	@Test
	public void optionRowsPointAtRealOptionsPropertiesAndRegisteredOwners() {
		TechniqueRegistry registry = TechniqueRegistry.getInstance();
		List<OptionInfo> options = registry.options();
		assertFalse(options.isEmpty());
		for (OptionInfo option : options) {
			assertTrue(hasBeanGetter(option.getKey()),
					"option key is not an Options bean property: " + option.getKey());
			assertNotNull(option.getTab(), "option without canonical tab: " + option.getKey());
			assertFalse(option.getDescription().isEmpty(), "option without description: " + option.getKey());
			if (!OWNERLESS_OPTIONS.contains(option.getKey())) {
				assertFalse(option.getOwners().isEmpty(), "option without owners: " + option.getKey());
			}
			for (SolutionType owner : option.getOwners()) {
				assertNotNull(registry.get(owner),
						"option owner is not a registry row: " + option.getKey() + " -> " + owner);
			}
		}
	}

	@Test
	public void configStepPanelInventoryIsComplete() {
		// the milestone 1.4 coverage contract: every option ConfigStepPanel
		// reads/writes has a registry row (list mirrors okPressed())
		List<String> panelOptions = List.of("maxFins", "maxEndoFins", "checkTemplates", "onlyOneFishPerStep",
				"fishDisplayMode", "restrictChainLength", "restrictChainSize", "maxTableEntryLength",
				"anzTableLookAhead", "onlyOneChainPerStep", "allowAlsInTablingChains", "useZeroInsteadOfDot",
				"allowErsWithOnlyTwoCandidates", "allowDualsAndSiamese", "allowUniquenessMissingCandidates",
				"krakenMaxFishType", "krakenMaxFishSize", "maxKrakenFins", "maxKrakenEndoFins", "allowAlsOverlap",
				"onlyOneAlsPerStep");
		Set<String> registered = new java.util.HashSet<>();
		for (OptionInfo option : TechniqueRegistry.getInstance().options()) {
			registered.add(option.getKey());
		}
		for (String key : panelOptions) {
			assertTrue(registered.contains(key), "ConfigStepPanel option not inventoried: " + key);
		}
	}

	@Test
	public void displayNamePreferenceRoundTripsThroughOptions() {
		TechniqueRegistry registry = TechniqueRegistry.getInstance();
		SolutionType type = SolutionType.VWXYZ_WING;
		String defaultName = type.getStepName();
		try {
			assertEquals(defaultName, registry.getDisplayName(type));
			registry.setPreferredDisplayName(type, "Bent Naked Quint");
			assertEquals("Bent Naked Quint", registry.getDisplayName(type));
			assertTrue(Options.getInstance().getTechniqueDisplayNames().contains("VWXYZ_WING=Bent Naked Quint"));
			// setting the default name again removes the preference
			registry.setPreferredDisplayName(type, defaultName);
			assertEquals(defaultName, registry.getDisplayName(type));
			assertFalse(Options.getInstance().getTechniqueDisplayNames().contains("VWXYZ_WING"));
		} finally {
			Options.getInstance().setTechniqueDisplayNames("");
		}
	}

	/** true if Options has a getX()/isX() bean getter for the property. */
	private static boolean hasBeanGetter(String property) {
		String suffix = Character.toUpperCase(property.charAt(0)) + property.substring(1);
		for (String prefix : new String[] { "get", "is" }) {
			try {
				Method method = Options.class.getMethod(prefix + suffix);
				if (method != null) {
					return true;
				}
			} catch (NoSuchMethodException ex) {
				// try the next prefix
			}
		}
		return false;
	}
}
