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
package solver.modern.registry;

import static sudoku.SolutionType.*;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import sudoku.SolutionType;

/**
 * The option rows of the {@link TechniqueRegistry}: one {@link OptionInfo}
 * per configuration option, with the real behavior derived from reading the
 * consuming code (class named in each description). Milestone 1.4 coverage:
 * the complete ConfigStepPanel (canonical tab STEPS for all of them); the
 * other tabs are inventoried when their aside work lands (1.5).
 *
 * Deliberately NOT covered here: the per-technique StepConfig switches
 * (enable/score/order) — those already live structured in
 * {@link sudoku.Options#solverSteps} — and the all-steps overrides
 * (allSteps*), which mirror options listed here for the find-all-steps
 * panel.
 */
final class OptionCatalog {

	private OptionCatalog() {
	}

	/** All 42 fish entries of the config (basic..finned mutant, sizes 2..7). */
	private static final Set<SolutionType> ALL_FISH = Set.of(X_WING, SWORDFISH, JELLYFISH, SQUIRMBAG, WHALE, LEVIATHAN,
			FINNED_X_WING, FINNED_SWORDFISH, FINNED_JELLYFISH, FINNED_SQUIRMBAG, FINNED_WHALE, FINNED_LEVIATHAN,
			SASHIMI_X_WING, SASHIMI_SWORDFISH, SASHIMI_JELLYFISH, SASHIMI_SQUIRMBAG, SASHIMI_WHALE, SASHIMI_LEVIATHAN,
			FRANKEN_X_WING, FRANKEN_SWORDFISH, FRANKEN_JELLYFISH, FRANKEN_SQUIRMBAG, FRANKEN_WHALE, FRANKEN_LEVIATHAN,
			FINNED_FRANKEN_X_WING, FINNED_FRANKEN_SWORDFISH, FINNED_FRANKEN_JELLYFISH, FINNED_FRANKEN_SQUIRMBAG,
			FINNED_FRANKEN_WHALE, FINNED_FRANKEN_LEVIATHAN, MUTANT_X_WING, MUTANT_SWORDFISH, MUTANT_JELLYFISH,
			MUTANT_SQUIRMBAG, MUTANT_WHALE, MUTANT_LEVIATHAN, FINNED_MUTANT_X_WING, FINNED_MUTANT_SWORDFISH,
			FINNED_MUTANT_JELLYFISH, FINNED_MUTANT_SQUIRMBAG, FINNED_MUTANT_WHALE, FINNED_MUTANT_LEVIATHAN);

	/** The finned/sashimi entries (the ones whose search is capped by maxFins). */
	private static final Set<SolutionType> FINNED_FISH = Set.of(FINNED_X_WING, FINNED_SWORDFISH, FINNED_JELLYFISH,
			FINNED_SQUIRMBAG, FINNED_WHALE, FINNED_LEVIATHAN, SASHIMI_X_WING, SASHIMI_SWORDFISH, SASHIMI_JELLYFISH,
			SASHIMI_SQUIRMBAG, SASHIMI_WHALE, SASHIMI_LEVIATHAN, FINNED_FRANKEN_X_WING, FINNED_FRANKEN_SWORDFISH,
			FINNED_FRANKEN_JELLYFISH, FINNED_FRANKEN_SQUIRMBAG, FINNED_FRANKEN_WHALE, FINNED_FRANKEN_LEVIATHAN,
			FINNED_MUTANT_X_WING, FINNED_MUTANT_SWORDFISH, FINNED_MUTANT_JELLYFISH, FINNED_MUTANT_SQUIRMBAG,
			FINNED_MUTANT_WHALE, FINNED_MUTANT_LEVIATHAN);

	/** Endo fins only exist in franken/mutant fish (see the relation map). */
	private static final Set<SolutionType> ENDO_FIN_FISH = Set.of(FINNED_FRANKEN_X_WING, FINNED_FRANKEN_SWORDFISH,
			FINNED_FRANKEN_JELLYFISH, FINNED_FRANKEN_SQUIRMBAG, FINNED_FRANKEN_WHALE, FINNED_FRANKEN_LEVIATHAN,
			FINNED_MUTANT_X_WING, FINNED_MUTANT_SWORDFISH, FINNED_MUTANT_JELLYFISH, FINNED_MUTANT_SQUIRMBAG,
			FINNED_MUTANT_WHALE, FINNED_MUTANT_LEVIATHAN);

	/** Techniques produced by the tabling engine. */
	private static final Set<SolutionType> TABLING = Set.of(NICE_LOOP, GROUPED_NICE_LOOP, FORCING_CHAIN, FORCING_NET);

	/** Techniques produced by the ALS solver. */
	private static final Set<SolutionType> ALS_TECHNIQUES = Set.of(ALS_XZ, ALS_XY_WING, ALS_XY_CHAIN, DEATH_BLOSSOM);

	/** The simple chains of ChainSolver (turbot has a fixed length of 3). */
	private static final Set<SolutionType> SIMPLE_CHAINS = Set.of(X_CHAIN, XY_CHAIN, REMOTE_PAIR);

	/** The UR-based uniqueness searches of UniquenessSolver. */
	private static final Set<SolutionType> UR_TECHNIQUES = Set.of(UNIQUENESS_1, UNIQUENESS_2, UNIQUENESS_3,
			UNIQUENESS_4, UNIQUENESS_5, UNIQUENESS_6, HIDDEN_RECTANGLE);

	static List<OptionInfo> load() {
		List<OptionInfo> options = new ArrayList<>();

		// --- fish panel ---
		options.add(o("maxFins",
				"Maximum number of fins a fish may have in the regular fish search (FishSolver; the kraken search has "
						+ "its own cap, maxKrakenFins).",
				FINNED_FISH));
		options.add(o("maxEndoFins",
				"Maximum number of endo fins (base candidates shared by two base sectors) in the fish search "
						+ "(FishSolver); endo fins only exist in franken and mutant fish.",
				ENDO_FIN_FISH));
		options.add(o("checkTemplates",
				"Prunes the fish search with a template check: candidates no valid placement template allows are "
						+ "excluded up front (FishSolver.doTemplates). Pure search-space pruning, does not change "
						+ "which steps are valid.",
				plus(ALL_FISH, KRAKEN_FISH_TYPE_1, KRAKEN_FISH_TYPE_2)));
		options.add(o("onlyOneFishPerStep",
				"Keeps only the smallest fish for every distinct set of eliminations when collecting fish steps "
						+ "(FishSolver dedup cache).",
				ALL_FISH));
		options.add(o("fishDisplayMode",
				"How fish hints are printed (SolutionStep.toString): 0 = normal, 1 = statistics numbers, "
						+ "2 = statistics cells. Display only.",
				plus(ALL_FISH, KRAKEN_FISH_TYPE_1, KRAKEN_FISH_TYPE_2)));

		// --- kraken fish panel (owners: both configured kraken types since P-002) ---
		options.add(o("krakenMaxFishType",
				"Largest fish class tried by the kraken search (FishSolver): 0 = basic, 1 = basic+franken, "
						+ "2 = basic+franken+mutant.",
				Set.of(KRAKEN_FISH_TYPE_1, KRAKEN_FISH_TYPE_2)));
		options.add(o("krakenMaxFishSize",
				"Maximum number of base/cover units of a kraken fish (FishSolver).",
				Set.of(KRAKEN_FISH_TYPE_1, KRAKEN_FISH_TYPE_2)));
		options.add(o("maxKrakenFins",
				"Maximum number of fins in the kraken fish search (FishSolver).",
				Set.of(KRAKEN_FISH_TYPE_1, KRAKEN_FISH_TYPE_2)));
		options.add(o("maxKrakenEndoFins",
				"Maximum number of endo fins in the kraken fish search (FishSolver).",
				Set.of(KRAKEN_FISH_TYPE_1, KRAKEN_FISH_TYPE_2)));

		// --- chain panel ---
		options.add(o("restrictChainLength",
				"Maximum number of links of X-Chains, XY-Chains and Remote Pairs when restrictChainSize is enabled "
						+ "(ChainSolver.getChains).",
				SIMPLE_CHAINS));
		options.add(o("restrictChainSize",
				"Whether the chain length limit is applied at all (ChainSolver); when off, chains may grow to the "
						+ "engine maximum. The separate nice-loop limit (restrictNiceLoopLength) is not exposed in "
						+ "the panel.",
				SIMPLE_CHAINS));

		// --- tabling panel (Nice Loops / AICs / forcing) ---
		options.add(o("maxTableEntryLength",
				"Size of the implication tables of the tabling engine (TableEntry buffers, TablingSolver): caps how "
						+ "many entries one chain/net derivation can hold.",
				TABLING));
		options.add(o("anzTableLookAhead",
				"Number of expansion passes over the implication tables (TablingSolver look-ahead loop): deeper "
						+ "look-ahead finds longer derivations at higher cost.",
				TABLING));
		options.add(o("onlyOneChainPerStep",
				"Keeps only one chain per distinct set of eliminations when collecting tabling steps "
						+ "(TablingSolver dedup cache).",
				TABLING));
		options.add(o("allowAlsInTablingChains",
				"Allows ALS nodes inside tabling chains and nets (TablingSolver.withAlsNodes), producing ALS "
						+ "chain/net variants of the Nice Loop / forcing steps.",
				TABLING));

		// --- ALS panel ---
		options.add(o("allowAlsOverlap",
				"Allows overlapping ALS pairs in the ALS searches (AlsSolver): when off, restricted commons are only "
						+ "collected for disjoint ALSs, ALS-XY-Wing pincers must be disjoint and Death Blossom petals "
						+ "must not overlap. NOT the doubly-linked switch: the doubly linked ALS-XZ (2 RCCs) is always "
						+ "checked. Enabling it enlarges the search noticeably (runtime warning in the legacy code).",
				ALS_TECHNIQUES));
		options.add(o("onlyOneAlsPerStep",
				"Despite its name: dedups ALS-XY-Chain and Death Blossom steps that produce exactly the same "
						+ "eliminations, keeping the one with the fewest ALS cells (AlsSolver deletesMap). ALS-XZ and "
						+ "ALS-XY-Wing are not affected.",
				Set.of(ALS_XY_CHAIN, DEATH_BLOSSOM)));

		// --- miscellaneous panel ---
		options.add(o("useZeroInsteadOfDot",
				"Clipboard/export format toggle: empty cells are written as '0' instead of '.'. "
						+ "Technique-independent; upstream parked it in the Steps tab.",
				Set.of()));
		options.add(o("allowErsWithOnlyTwoCandidates",
				"Allows Empty Rectangles whose box holds only two candidates of the digit "
						+ "(SingleDigitPatternSolver skips them when off).",
				Set.of(EMPTY_RECTANGLE)));
		options.add(o("allowDualsAndSiamese",
				"Enables the dual forms of 2-String Kite, Skyscraper and Empty Rectangle "
						+ "(SingleDigitPatternSolver) and Siamese fish (FishSolver.siamese): two patterns over the "
						+ "same cells reported as one step. Witness case of multi-technique ownership.",
				plus(ALL_FISH, SKYSCRAPER, TWO_STRING_KITE, EMPTY_RECTANGLE)));
		options.add(o("allowUniquenessMissingCandidates",
				"Allows UR corners with additional candidates to lack some UR candidates (already eliminated ones) "
						+ "and still count as part of the rectangle (UniquenessSolver.allowMissing): finds more URs.",
				UR_TECHNIQUES));

		return options;
	}

	private static OptionInfo o(String key, String description, Set<SolutionType> owners) {
		// milestone 1.4 covers ConfigStepPanel: canonical tab STEPS for all rows
		return new OptionInfo(key, description, owners, ConfigTab.STEPS);
	}

	private static Set<SolutionType> plus(Set<SolutionType> base, SolutionType... extra) {
		EnumSet<SolutionType> set = EnumSet.copyOf(base);
		set.addAll(List.of(extra));
		return set;
	}
}
