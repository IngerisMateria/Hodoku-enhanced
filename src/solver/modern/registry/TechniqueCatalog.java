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

import sudoku.SolutionType;

/**
 * The technique rows of the {@link TechniqueRegistry}: one
 * {@link TechniqueInfo} per configured technique (every SolutionType that has
 * its own StepConfig in {@link sudoku.Options#DEFAULT_SOLVER_STEPS}, except
 * the INCOMPLETE/GIVE_UP pseudo steps). Completeness is enforced by test —
 * adding a technique without a row here breaks the build.
 *
 * The subsumption seed ("is a case of", multi-parent) comes from the owner's
 * relation map (docs/sudoku_mapa_relacional.md) as arbitrated by
 * docs/estrategia-taxonomia.md — notably: the bent subsets n=4..9 are R1
 * (single non-restricted candidate) and therefore ALS-XZ cases, NOT Sue de
 * Coq / ESDC (which is the R0 locked regime); see the T2 experiment
 * (docs/experimentos/t2-regimen.md).
 *
 * For fish, the class-inclusion order of the definitions is basic ⊂ franken
 * ⊂ mutant (a pure line fish satisfies the franken definition, etc.) and
 * sashimi ⊂ finned; finned and non-finned entries are disjoint by
 * definition, so they form two parallel chains.
 */
final class TechniqueCatalog {

	private TechniqueCatalog() {
	}

	// the fish matrix, sizes 2..7 per column
	private static final SolutionType[] FISH_BASIC = { X_WING, SWORDFISH, JELLYFISH, SQUIRMBAG, WHALE, LEVIATHAN };
	private static final SolutionType[] FISH_FINNED = { FINNED_X_WING, FINNED_SWORDFISH, FINNED_JELLYFISH,
			FINNED_SQUIRMBAG, FINNED_WHALE, FINNED_LEVIATHAN };
	private static final SolutionType[] FISH_SASHIMI = { SASHIMI_X_WING, SASHIMI_SWORDFISH, SASHIMI_JELLYFISH,
			SASHIMI_SQUIRMBAG, SASHIMI_WHALE, SASHIMI_LEVIATHAN };
	private static final SolutionType[] FISH_FRANKEN = { FRANKEN_X_WING, FRANKEN_SWORDFISH, FRANKEN_JELLYFISH,
			FRANKEN_SQUIRMBAG, FRANKEN_WHALE, FRANKEN_LEVIATHAN };
	private static final SolutionType[] FISH_FINNED_FRANKEN = { FINNED_FRANKEN_X_WING, FINNED_FRANKEN_SWORDFISH,
			FINNED_FRANKEN_JELLYFISH, FINNED_FRANKEN_SQUIRMBAG, FINNED_FRANKEN_WHALE, FINNED_FRANKEN_LEVIATHAN };
	private static final SolutionType[] FISH_MUTANT = { MUTANT_X_WING, MUTANT_SWORDFISH, MUTANT_JELLYFISH,
			MUTANT_SQUIRMBAG, MUTANT_WHALE, MUTANT_LEVIATHAN };
	private static final SolutionType[] FISH_FINNED_MUTANT = { FINNED_MUTANT_X_WING, FINNED_MUTANT_SWORDFISH,
			FINNED_MUTANT_JELLYFISH, FINNED_MUTANT_SQUIRMBAG, FINNED_MUTANT_WHALE, FINNED_MUTANT_LEVIATHAN };

	/** The bent subset entries by size (4..9) with their literature names. */
	private static final SolutionType[] BENT_BY_SIZE = { BENT_QUAD, VWXYZ_WING, UVWXYZ_WING, TUVWXYZ_WING,
			STUVWXYZ_WING, RSTUVWXYZ_WING };

	static List<TechniqueInfo> load() {
		List<TechniqueInfo> rows = new ArrayList<>();

		// --- singles (SimpleSolver) ---
		rows.add(t(FULL_HOUSE, Family.SINGLES, "SimpleSolver",
				"The last empty cell of a row, column or box; the missing digit is placed.")
				.aliases("Last Digit").build());
		rows.add(t(NAKED_SINGLE, Family.SINGLES, "SimpleSolver",
				"A cell with a single remaining candidate; that digit is placed.").build());
		rows.add(t(HIDDEN_SINGLE, Family.SINGLES, "SimpleSolver",
				"A digit with a single remaining position in a row, column or box; it is placed there.").build());

		// --- intersections (SimpleSolver) ---
		rows.add(t(LOCKED_CANDIDATES_1, Family.INTERSECTIONS, "SimpleSolver",
				"All candidates of a digit in a box lie on one line: the digit is eliminated from the rest of that line.")
				.aliases("Pointing").build());
		rows.add(t(LOCKED_CANDIDATES_2, Family.INTERSECTIONS, "SimpleSolver",
				"All candidates of a digit in a line lie in one box: the digit is eliminated from the rest of that box.")
				.aliases("Claiming", "Box/Line Reduction").build());

		// --- subsets (SimpleSolver) ---
		rows.add(t(NAKED_PAIR, Family.SUBSETS, "SimpleSolver",
				"Two cells of one house holding exactly two candidates between them; both digits are eliminated from the rest of the house.")
				.build());
		rows.add(t(NAKED_TRIPLE, Family.SUBSETS, "SimpleSolver",
				"Three cells of one house holding exactly three candidates between them; those digits are eliminated from the rest of the house.")
				.build());
		rows.add(t(NAKED_QUADRUPLE, Family.SUBSETS, "SimpleSolver",
				"Four cells of one house holding exactly four candidates between them; those digits are eliminated from the rest of the house.")
				.build());
		rows.add(t(HIDDEN_PAIR, Family.SUBSETS, "SimpleSolver",
				"Two digits confined to two cells of one house; every other candidate is eliminated from those cells.")
				.build());
		rows.add(t(HIDDEN_TRIPLE, Family.SUBSETS, "SimpleSolver",
				"Three digits confined to three cells of one house; every other candidate is eliminated from those cells.")
				.build());
		rows.add(t(HIDDEN_QUADRUPLE, Family.SUBSETS, "SimpleSolver",
				"Four digits confined to four cells of one house; every other candidate is eliminated from those cells.")
				.build());
		rows.add(t(LOCKED_PAIR, Family.SUBSETS, "SimpleSolver",
				"A naked pair lying in a box-line intersection: it eliminates in both houses at once.")
				.subsumedBy(NAKED_PAIR).build());
		rows.add(t(LOCKED_TRIPLE, Family.SUBSETS, "SimpleSolver",
				"A naked triple lying in a box-line intersection: it eliminates in both houses at once.")
				.subsumedBy(NAKED_TRIPLE).build());

		// --- fish matrix (FishSolver) ---
		addFishRows(rows);
		// Generic Kraken Fish: taxonomy anchor only since the P-002 split (milestone
		// 1.5) — the configured entries are KRAKEN_FISH_TYPE_1/2 below; this row has
		// no StepConfig anymore (documented exception in TechniqueRegistryTest).
		rows.add(t(KRAKEN_FISH, Family.FISH, "FishSolver",
				"A fish whose eliminations are justified by chains instead of direct sight. Split into Type 1 and "
						+ "Type 2 config entries (P-002, milestone 1.5); this generic entry remains as the common "
						+ "taxonomy parent of both.")
				.subsumedBy(FORCING_CHAIN).refs("docs/pulido.md P-002").build());
		rows.add(t(KRAKEN_FISH_TYPE_1, Family.FISH, "FishSolver",
				"A finned fish without direct eliminations where every fin reaches one candidate through a chain: "
						+ "either the fish is true or a fin is, so that candidate is eliminated.")
				.aliases("Kraken Fish (Type 1)").subsumedBy(KRAKEN_FISH).build());
		rows.add(t(KRAKEN_FISH_TYPE_2, Family.FISH, "FishSolver",
				"A fish where every base candidate of one cover sector, plus every fin, chains to the same candidate "
						+ "elsewhere: that candidate is eliminated.")
				.aliases("Kraken Fish (Type 2)").subsumedBy(KRAKEN_FISH).build());

		// --- single digit patterns ---
		rows.add(t(TURBOT_FISH, Family.SINGLE_DIGIT_PATTERNS, "ChainSolver",
				"Single-digit chain of length 3 (two strong links joined by a weak link) — the generic form of the "
						+ "Skyscraper / 2-String Kite / Empty Rectangle triad.")
				.aliases("Turbot Crane").subsumedBy(X_CHAIN)
				.refs("docs/sudoku_mapa_relacional.md §6-2").build());
		rows.add(t(SKYSCRAPER, Family.SINGLE_DIGIT_PATTERNS, "SingleDigitPatternSolver",
				"Two parallel strong links of one digit whose aligned ends share a house: the digit is eliminated from "
						+ "cells seeing both loose ends. Equivalent to a pair of Sashimi X-Wings.")
				.subsumedBy(TURBOT_FISH).build());
		rows.add(t(TWO_STRING_KITE, Family.SINGLE_DIGIT_PATTERNS, "SingleDigitPatternSolver",
				"Two perpendicular strong links of one digit (a row and a column) joined through a box: the digit is "
						+ "eliminated from the cell seeing both loose ends. The dual form is gated by allowDualsAndSiamese.")
				.subsumedBy(TURBOT_FISH).build());
		rows.add(t(EMPTY_RECTANGLE, Family.SINGLE_DIGIT_PATTERNS, "SingleDigitPatternSolver",
				"All candidates of a digit in a box confined to one row/column cross; combined with a conjugate pair "
						+ "it eliminates the digit at the crossing. The dual form is gated by allowDualsAndSiamese.")
				.aliases("ER").subsumedBy(TURBOT_FISH).build());

		// --- uniqueness (UniquenessSolver) ---
		rows.add(t(UNIQUENESS_1, Family.UNIQUENESS, "UniquenessSolver",
				"Unique Rectangle with one corner holding extra candidates: the UR digits are eliminated from that corner.")
				.aliases("Unique Rectangle Type 1", "UR Type 1").build());
		rows.add(t(UNIQUENESS_2, Family.UNIQUENESS, "UniquenessSolver",
				"Unique Rectangle with one identical extra candidate in two corners: that digit is eliminated from cells seeing both.")
				.aliases("Unique Rectangle Type 2", "UR Type 2").build());
		rows.add(t(UNIQUENESS_3, Family.UNIQUENESS, "UniquenessSolver",
				"Unique Rectangle whose two extra-candidate corners form a virtual naked subset with other cells of a common house.")
				.aliases("Unique Rectangle Type 3", "UR Type 3").build());
		rows.add(t(UNIQUENESS_4, Family.UNIQUENESS, "UniquenessSolver",
				"Unique Rectangle where one UR digit is strongly linked between the extra-candidate corners: the other UR digit is eliminated from them.")
				.aliases("Unique Rectangle Type 4", "UR Type 4").build());
		rows.add(t(UNIQUENESS_5, Family.UNIQUENESS, "UniquenessSolver",
				"Unique Rectangle with the same extra candidate in two diagonal (or three) corners: that digit is eliminated from "
						+ "cells seeing all of them. Numbering caveat: HoDoKu and SudokuWiki disagree on Type 5/6 labels.")
				.aliases("Unique Rectangle Type 5", "UR Type 5")
				.refs("docs/sudoku_mapa_relacional.md §3.a (conflicto Type 5/6)").build());
		rows.add(t(UNIQUENESS_6, Family.UNIQUENESS, "UniquenessSolver",
				"Unique Rectangle with one UR digit restricted to a diagonal of the rectangle: it is eliminated from the other "
						+ "diagonal. HoDoKu's Type 6 = SudokuWiki's Type 5 (documented numbering conflict).")
				.aliases("Unique Rectangle Type 6", "UR Type 6")
				.refs("docs/sudoku_mapa_relacional.md §3.a (conflicto Type 5/6)").build());
		rows.add(t(HIDDEN_RECTANGLE, Family.UNIQUENESS, "UniquenessSolver",
				"Unique Rectangle variant using strong links to rule out the deadly pattern when extra candidates hide it.")
				.aliases("Hidden Unique Rectangle").build());
		rows.add(t(AVOIDABLE_RECTANGLE_1, Family.UNIQUENESS, "UniquenessSolver",
				"Rectangle of solved cells (no givens) that would form a deadly pattern: the completing candidate is eliminated.")
				.aliases("AR Type 1").build());
		rows.add(t(AVOIDABLE_RECTANGLE_2, Family.UNIQUENESS, "UniquenessSolver",
				"Avoidable Rectangle with two unsolved corners sharing an extra candidate: it is eliminated from cells seeing both.")
				.aliases("AR Type 2").build());
		rows.add(t(BUG_PLUS_1, Family.UNIQUENESS, "UniquenessSolver",
				"Bivalue Universal Grave + 1: if placing anything but the triple candidate left every unsolved cell bivalue, "
						+ "the puzzle would have multiple solutions; the extra candidate is placed.")
				.aliases("BUG+1").build());

		// --- named wings (WingSolver / WxyzWingSolver) ---
		rows.add(t(XY_WING, Family.WINGS, "WingSolver",
				"A bivalue pivot {X,Y} with pincers {X,Z} and {Y,Z}: Z is eliminated from cells seeing both pincers. "
						+ "The minimal bent set (R1) and the minimal ALS-XZ.")
				.aliases("Y-Wing", "Bent Triple").subsumedBy(XY_CHAIN, ALS_XZ).regimes(Regime.R1).build());
		rows.add(t(XYZ_WING, Family.WINGS, "WingSolver",
				"An XY-Wing whose pivot also holds Z ({X,Y,Z}): Z is eliminated from cells seeing the pivot and both pincers.")
				.subsumedBy(ALS_XZ).regimes(Regime.R1).build());
		rows.add(t(W_WING, Family.WINGS, "WingSolver",
				"Two remote bivalue cells {X,Y} joined by a strong link on Y: X is eliminated from cells seeing both bivalue cells.")
				.aliases("Semi-Remote Naked Pair").subsumedBy(NICE_LOOP).build());
		rows.add(t(WXYZ_WING, Family.WINGS, "WxyzWingSolver",
				"Canonical WXYZ-Wing: a hinge and three bivalue wings {letter, Z} seeing it, four digits total; Z is "
						+ "eliminated from cells seeing every Z of the pattern. Type 1: Z in the hinge; Type 2: Z only in the wings.")
				.subsumedBy(BENT_QUAD).regimes(Regime.R1)
				.refs("docs/pulido.md P-001", "docs/experimentos/t2-regimen.md").build());

		// --- bent subsets n=4..9 (BentSubsetSolver) ---
		addBentRows(rows);

		// --- oddagons (OddagonSolver, milestone 1.6) ---
		// The contradiction+guardians branch of the owner's map ("rank -1 /
		// dark logic", §2.d): its own taxonomy root, no subsumption parents
		// among the registered techniques (chains re-derive the eliminations
		// but are a different framework).
		rows.add(t(BROKEN_WING, Family.ODDAGON, "OddagonSolver",
				"An odd loop (length 5 or 7) of one digit's candidates that would be an impossible ring of conjugate "
						+ "links: some guardian (another candidate of the digit in a link house) must be true. A single "
						+ "guardian is placed; several same-digit guardians eliminate the digit from cells seeing them all.")
				.aliases("Guardians", "Single-Digit Oddagon")
				.refs("docs/sudoku_mapa_relacional.md §2.d").build());
		rows.add(t(BIVALUE_ODDAGON, Family.ODDAGON, "OddagonSolver",
				"An odd loop (length 5 or 7) of cells all holding the pair {a,b}: without extra candidates the loop "
						+ "would need a 2-coloring an odd cycle cannot have. The extra candidates inside the loop are the "
						+ "guardians: a single one strips {a,b} from its cell; several of one digit eliminate that digit "
						+ "from cells seeing them all. Mixed-digit guardian sets give no direct elimination (chain territory).")
				.aliases("Bi-Value Oddagon")
				.refs("docs/sudoku_mapa_relacional.md §2.d").build());

		// --- coloring (ColoringSolver) ---
		rows.add(t(SIMPLE_COLORS, Family.COLORING, "ColoringSolver",
				"Two-coloring of the conjugate-link cluster of one digit: candidates seeing both colors (trap) or a "
						+ "color seeing itself (wrap) are eliminated. Color notation for a single-digit chain.")
				.aliases("Simple Colouring", "Single's Chains").subsumedBy(MULTI_COLORS).build());
		rows.add(t(MULTI_COLORS, Family.COLORING, "ColoringSolver",
				"Coloring with several clusters of one digit and the weak links between them.")
				.aliases("Multi-Colouring").build());

		// --- simple chains (ChainSolver) ---
		rows.add(t(REMOTE_PAIR, Family.CHAINS, "ChainSolver",
				"A chain of identical bivalue cells {X,Y}: both digits are eliminated from cells seeing two chain cells "
						+ "of opposite parity.")
				.subsumedBy(XY_CHAIN).build());
		rows.add(t(X_CHAIN, Family.CHAINS, "ChainSolver",
				"An alternating chain on a single digit: the digit is eliminated from cells seeing both open ends.")
				.aliases("X-Cycle", "X-Loop").subsumedBy(NICE_LOOP).build());
		rows.add(t(XY_CHAIN, Family.CHAINS, "ChainSolver",
				"A chain through bivalue cells whose ends assert the same digit: it is eliminated from cells seeing both ends.")
				.subsumedBy(NICE_LOOP).build());
		rows.add(t(NICE_LOOP, Family.CHAINS, "TablingSolver",
				"General alternating inference chains/loops with cell nodes. One config entry covers the continuous and "
						+ "discontinuous loop forms and the AIC presentation.")
				.aliases("AIC", "Alternating Inference Chain").subsumedBy(GROUPED_NICE_LOOP).build());
		rows.add(t(GROUPED_NICE_LOOP, Family.CHAINS, "TablingSolver",
				"Nice Loops / AICs whose nodes may be groups of candidates (line-box intersections). One config entry "
						+ "covers the continuous/discontinuous/AIC forms.")
				.aliases("Grouped AIC").subsumedBy(FORCING_CHAIN).build());

		// --- ALS (AlsSolver / MiscellaneousSolver) ---
		rows.add(t(ALS_XZ, Family.ALS, "AlsSolver",
				"Two Almost Locked Sets sharing a restricted common candidate: every other common digit is eliminated "
						+ "from cells seeing all its instances (R1). The doubly linked case (2 RCCs, R0) is always "
						+ "checked as part of this entry and eliminates from the rest of each confining sector.")
				.aliases("ALS-XZ Rule", "Bent Almost Restricted Naked Set (BARN)")
				.subsumedBy(ALS_XY_CHAIN).regimes(Regime.R1, Regime.R0)
				.refs("docs/estrategia-taxonomia.md §1").build());
		rows.add(t(ALS_XY_WING, Family.ALS, "AlsSolver",
				"Three ALSs linked pivot-style by two restricted commons: digits common to the outer ALSs are eliminated "
						+ "from cells seeing all their instances.")
				.aliases("ALS-XY-Wing").subsumedBy(ALS_XY_CHAIN).build());
		rows.add(t(ALS_XY_CHAIN, Family.ALS, "AlsSolver",
				"A chain of four or more ALSs joined by restricted commons; digits common to both end ALSs are "
						+ "eliminated outside the chain.")
				.aliases("ALS-Chain").build());
		rows.add(t(DEATH_BLOSSOM, Family.ALS, "AlsSolver",
				"A stem cell whose every candidate is a restricted common with its own ALS petal; digits common to all "
						+ "petals are eliminated outside. With a bivalue stem it degenerates to an ALS-XY-Wing.")
				.refs("docs/sudoku_mapa_relacional.md §5-10").build());
		rows.add(t(SUE_DE_COQ, Family.ALS, "MiscellaneousSolver",
				"Two-Sector Disjoint Subsets: n cells of a box-line intersection region with n digits, every digit "
						+ "restricted to one of the two sectors (R0 locked regime): each digit is eliminated from the "
						+ "rest of its confining sector. Algebraically a doubly linked ALS-XZ.")
				.aliases("Two-Sector Disjoint Subsets", "TSDS", "SDC").subsumedBy(ALS_XZ).regimes(Regime.R0)
				.refs("docs/estrategia-taxonomia.md §1").build());

		// --- forcing (TablingSolver) ---
		rows.add(t(FORCING_CHAIN, Family.FORCING, "TablingSolver",
				"Chains from every candidate of a cell/house reaching the same conclusion (verity) or a single "
						+ "assumption reaching a contradiction. One config entry covers both variants.")
				.subsumedBy(FORCING_NET).build());
		rows.add(t(FORCING_NET, Family.FORCING, "TablingSolver",
				"Forcing with non-linear branching (nets). One config entry covers the contradiction and verity variants.")
				.build());

		// --- templates (TemplateSolver) ---
		rows.add(t(TEMPLATE_SET, Family.TEMPLATES, "TemplateSolver",
				"Digit placed because it belongs to every remaining valid template (full placement pattern) of that digit.")
				.build());
		rows.add(t(TEMPLATE_DEL, Family.TEMPLATES, "TemplateSolver",
				"Candidate eliminated because it belongs to no remaining valid template of its digit.").build());

		// --- last resort ---
		rows.add(t(BRUTE_FORCE, Family.LAST_RESORT, "BruteForceSolver",
				"Backdoor placement taken from the brute-force solution; the last resort when every enabled technique fails.")
				.build());

		return rows;
	}

	/** The 42 fish rows: 7 shapes x 6 sizes, built uniformly. */
	private static void addFishRows(List<TechniqueInfo> rows) {
		for (int i = 0; i < FISH_BASIC.length; i++) {
			int size = i + 2;
			Row basic = t(FISH_BASIC[i], Family.FISH, "FishSolver",
					"Basic fish of size " + size + ": the candidates of one digit in " + size
							+ " base lines are covered by " + size
							+ " cover lines; the digit is eliminated from the covers outside the base.")
					.subsumedBy(FISH_FRANKEN[i]);
			if (FISH_BASIC[i] == SQUIRMBAG) {
				basic.aliases("Starfish");
			}
			rows.add(basic.build());
			rows.add(t(FISH_FINNED[i], Family.FISH, "FishSolver",
					"Basic fish of size " + size + " with fins (extra base candidates); eliminations are restricted "
							+ "to cover cells that see every fin.")
					.subsumedBy(FISH_FINNED_FRANKEN[i]).build());
			rows.add(t(FISH_SASHIMI[i], Family.FISH, "FishSolver",
					"Finned fish of size " + size + " that would degenerate without its fins (a base line holds "
							+ "too few candidates outside the fins).")
					.subsumedBy(FISH_FINNED[i]).build());
			rows.add(t(FISH_FRANKEN[i], Family.FISH, "FishSolver",
					"Fish of size " + size + " whose base or cover sets may mix lines and boxes.")
					.subsumedBy(FISH_MUTANT[i]).build());
			rows.add(t(FISH_FINNED_FRANKEN[i], Family.FISH, "FishSolver",
					"Franken fish of size " + size + " with fins (exo or endo).")
					.subsumedBy(FISH_FINNED_MUTANT[i]).build());
			rows.add(t(FISH_MUTANT[i], Family.FISH, "FishSolver",
					"Fish of size " + size + " whose base and cover sets mix rows, columns and boxes freely.").build());
			rows.add(t(FISH_FINNED_MUTANT[i], Family.FISH, "FishSolver",
					"Mutant fish of size " + size + " with fins (exo or endo).").build());
		}
	}

	/** The bent subset rows n=4..9 (regime R1 by construction, milestone 1.1-1.3). */
	private static void addBentRows(List<TechniqueInfo> rows) {
		String[] extra = { "Bent Naked Quad", "Bent Naked Quint", null, null, null, null };
		String[] general = { "WXYZ-Wing (general form)", null, null, null, null, null };
		for (int i = 0; i < BENT_BY_SIZE.length; i++) {
			int size = i + 4;
			Row row = t(BENT_BY_SIZE[i], Family.BENT_SUBSETS, "BentSubsetSolver",
					"Bent naked subset of " + size + " cells with exactly " + size + " candidates over a line-box "
							+ "region, with exactly one non-restricted candidate Z (regime R1): Z is eliminated from "
							+ "cells seeing every Z of the set. Algebraically an ALS-XZ with a single RCC.")
					.subsumedBy(ALS_XZ).regimes(Regime.R1)
					.refs("docs/estrategia-taxonomia.md §1");
			List<String> aliases = new ArrayList<>();
			if (extra[i] != null) {
				aliases.add(extra[i]);
			}
			if (general[i] != null) {
				aliases.add(general[i]);
			}
			if (i > 0) {
				// size 4 keeps its historic subset name only; larger sizes get the generic one
				aliases.add("Bent Naked Subset (size " + size + ")");
			}
			aliases.add("BARN (size " + size + ")");
			row.aliases(aliases.toArray(new String[0]));
			rows.add(row.build());
		}
	}

	// ------------------------------------------------------------------
	// row builder
	// ------------------------------------------------------------------

	private static Row t(SolutionType id, Family family, String engine, String description) {
		return new Row(id, family, engine, description);
	}

	/** Minimal fluent builder so the catalog reads as data. */
	private static final class Row {
		private final SolutionType id;
		private final Family family;
		private final String engine;
		private final String description;
		private List<String> aliases = List.of();
		private EnumSet<SolutionType> subsumedBy = EnumSet.noneOf(SolutionType.class);
		private EnumSet<Regime> regimes = EnumSet.noneOf(Regime.class);
		private List<String> references = List.of();

		Row(SolutionType id, Family family, String engine, String description) {
			this.id = id;
			this.family = family;
			this.engine = engine;
			this.description = description;
		}

		Row aliases(String... aliases) {
			this.aliases = List.of(aliases);
			return this;
		}

		Row subsumedBy(SolutionType... parents) {
			this.subsumedBy = EnumSet.of(parents[0], parents);
			return this;
		}

		Row regimes(Regime... regimes) {
			this.regimes = EnumSet.of(regimes[0], regimes);
			return this;
		}

		Row refs(String... references) {
			this.references = List.of(references);
			return this;
		}

		TechniqueInfo build() {
			// display name default = the legacy intl name, so they cannot drift
			return new TechniqueInfo(id, id.getStepName(), aliases, description, family, engine, subsumedBy, regimes,
					references);
		}
	}
}
